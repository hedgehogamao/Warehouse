package com.autoparts.service.impl;

import com.autoparts.common.BusinessException;
import com.autoparts.dto.ProductSaveRequest;
import com.autoparts.model.Product;
import com.autoparts.repository.ProductMapper;
import com.autoparts.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品业务逻辑实现
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public IPage<Product> pageProducts(int page, int size, String keyword,
                                        String brand, String category, Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 关键词同时匹配 name、sku、car_model（OR 条件）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(Product::getName, keyword)
                    .or().like(Product::getSku, keyword)
                    .or().like(Product::getCarModel, keyword)
            );
        }
        // 精确筛选条件
        if (brand != null && !brand.trim().isEmpty()) {
            wrapper.eq(Product::getBrand, brand);
        }
        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq(Product::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        wrapper.orderByDesc(Product::getCreatedAt);

        return productMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Product getProductById(Integer id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createProduct(ProductSaveRequest request) {
        // SKU 唯一性校验
        if (request.getSku() == null || request.getSku().trim().isEmpty()) {
            throw new BusinessException(400, "SKU不能为空");
        }
        Long count = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getSku, request.getSku())
        );
        if (count > 0) {
            throw new BusinessException(400, "SKU已存在: " + request.getSku());
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setBrand(request.getBrand());
        product.setCarModel(request.getCarModel());
        product.setCategory(request.getCategory());
        product.setUnit(request.getUnit() != null ? request.getUnit() : "个");
        product.setCostPrice(request.getCostPrice());
        product.setSalePrice(request.getSalePrice());
        product.setStock(0); // 新商品库存默认为 0，通过入库增加
        product.setMinStock(request.getMinStock() != null ? request.getMinStock() : 0);
        product.setImageUrl(request.getImageUrl());
        product.setDescription(request.getDescription());
        product.setStatus(1); // 默认上架
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        productMapper.insert(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Integer id, ProductSaveRequest request) {
        Product product = getProductById(id);

        // SKU 不可修改
        if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
            throw new BusinessException(400, "SKU不可修改");
        }

        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setCarModel(request.getCarModel());
        product.setCategory(request.getCategory());
        if (request.getUnit() != null) {
            product.setUnit(request.getUnit());
        }
        product.setCostPrice(request.getCostPrice());
        product.setSalePrice(request.getSalePrice());
        if (request.getMinStock() != null) {
            product.setMinStock(request.getMinStock());
        }
        product.setImageUrl(request.getImageUrl());
        product.setDescription(request.getDescription());
        product.setUpdatedAt(LocalDateTime.now());

        productMapper.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Integer id) {
        Product product = getProductById(id);
        productMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductStatus(Integer id, Integer status) {
        Product product = getProductById(id);
        product.setStatus(status);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    public List<String> getCategories() {
        return productMapper.selectDistinctCategories();
    }

    @Override
    public List<Map<String, Object>> posProductSearch(String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1); // 仅上架商品

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(Product::getName, keyword)
                    .or().like(Product::getSku, keyword)
                    .or().like(Product::getBrand, keyword)
            );
        }

        wrapper.last("LIMIT 20");

        return productMapper.selectList(wrapper).stream().map(product -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", product.getId());
            map.put("name", product.getName());
            map.put("sku", product.getSku());
            map.put("salePrice", product.getSalePrice());
            map.put("stock", product.getStock());
            return map;
        }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importProducts(MultipartFile file) {
        int successCount = 0;
        int skippedCount = 0;
        List<Map<String, Object>> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            for (int i = 1; i <= lastRow; i++) { // 从第 2 行开始（跳过表头）
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String name = getCellStringValue(row.getCell(0));
                    String sku = getCellStringValue(row.getCell(1));
                    String brand = getCellStringValue(row.getCell(2));
                    String carModel = getCellStringValue(row.getCell(3));
                    String category = getCellStringValue(row.getCell(4));
                    String unit = getCellStringValue(row.getCell(5));
                    BigDecimal costPrice = getCellBigDecimalValue(row.getCell(6));
                    BigDecimal salePrice = getCellBigDecimalValue(row.getCell(7));
                    Integer minStock = getCellIntegerValue(row.getCell(8));
                    String description = getCellStringValue(row.getCell(9));

                    // 必填校验
                    if (name == null || name.isBlank()) {
                        addError(errors, i + 1, "商品名称不能为空");
                        continue;
                    }
                    if (sku == null || sku.isBlank()) {
                        addError(errors, i + 1, "SKU不能为空");
                        continue;
                    }
                    if (unit == null || unit.isBlank()) {
                        unit = "个"; // 默认单位
                    }
                    if (salePrice == null) {
                        addError(errors, i + 1, "售价不能为空");
                        continue;
                    }

                    // SKU 去重
                    Long count = productMapper.selectCount(
                            new LambdaQueryWrapper<Product>().eq(Product::getSku, sku)
                    );
                    if (count > 0) {
                        skippedCount++;
                        continue;
                    }

                    Product product = new Product();
                    product.setName(name);
                    product.setSku(sku);
                    product.setBrand(brand);
                    product.setCarModel(carModel);
                    product.setCategory(category);
                    product.setUnit(unit);
                    product.setCostPrice(costPrice);
                    product.setSalePrice(salePrice);
                    product.setStock(0);
                    product.setMinStock(minStock != null ? minStock : 0);
                    product.setDescription(description);
                    product.setStatus(1);
                    product.setCreatedAt(LocalDateTime.now());
                    product.setUpdatedAt(LocalDateTime.now());

                    productMapper.insert(product);
                    successCount++;
                } catch (Exception e) {
                    addError(errors, i + 1, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new BusinessException(400, "文件读取失败: " + e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", successCount);
        result.put("skipped", skippedCount);
        result.put("errors", errors);
        return result;
    }

    @Override
    public void generateImportTemplate(HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("商品导入模板");

            // 表头
            String[] headers = {"商品名称*", "SKU*", "品牌", "车型", "分类", "单位", "进价", "售价*", "库存预警", "描述"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 示例数据
            Row example1 = sheet.createRow(1);
            example1.createCell(0).setCellValue("嘉实多极护 5W-30 4L");
            example1.createCell(1).setCellValue("OIL-001");
            example1.createCell(2).setCellValue("嘉实多");
            example1.createCell(3).setCellValue("大众/奥迪");
            example1.createCell(4).setCellValue("机油");
            example1.createCell(5).setCellValue("桶");
            example1.createCell(6).setCellValue(180);
            example1.createCell(7).setCellValue(258);
            example1.createCell(8).setCellValue(10);
            example1.createCell(9).setCellValue("全合成机油");

            // 设置列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, 4000);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("商品导入模板.xlsx", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

            workbook.write(response.getOutputStream());
        }
    }

    /** 读取单元格为字符串 */
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        String value;
        switch (cell.getCellType()) {
            case STRING:
                value = cell.getStringCellValue().trim();
                break;
            case NUMERIC:
                value = String.valueOf((int) cell.getNumericCellValue());
                break;
            case BOOLEAN:
                value = String.valueOf(cell.getBooleanCellValue());
                break;
            default:
                value = "";
        }
        return value.isEmpty() ? null : value;
    }

    /** 读取单元格为 BigDecimal */
    private BigDecimal getCellBigDecimalValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            }
            if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) return null;
                return new BigDecimal(value);
            }
        } catch (Exception e) {
            // 转换失败返回 null
        }
        return null;
    }

    /** 读取单元格为 Integer */
    private Integer getCellIntegerValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            }
            if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) return null;
                return Integer.parseInt(value);
            }
        } catch (Exception e) {
            // 转换失败返回 null
        }
        return null;
    }

    /** 添加错误记录 */
    private void addError(List<Map<String, Object>> errors, int row, String reason) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("row", row);
        error.put("reason", reason);
        errors.add(error);
    }
}
