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
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 商品业务逻辑实现
 */
@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductMapper productMapper;

    @Value("${upload.path:uploads/images}")
    private String uploadPath;

    @Value("${upload.url-prefix:/uploads/images}")
    private String uploadUrlPrefix;

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
        int imageCount = 0;
        List<Map<String, Object>> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            // 读取表头行，建立列名 → 列索引的映射
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException(400, "文件第一行（表头）为空");
            }
            Map<String, Integer> columnMap = buildColumnMap(headerRow);

            // 校验必填列是否存在
            if (!columnMap.containsKey("name") || !columnMap.containsKey("sku") || !columnMap.containsKey("salePrice")) {
                throw new BusinessException(400, "表头缺少必填列。需要：商品名称、SKU、售价。实际表头: " + getHeaderNames(headerRow));
            }

            // 提取 Excel 内嵌图片，建立 行号 → 图片数据 的映射
            Map<Integer, PictureData> rowImages = extractRowImages(sheet);
            if (!rowImages.isEmpty()) {
                log.info("从 Excel 中提取到 {} 张内嵌图片", rowImages.size());
            }

            for (int i = 1; i <= lastRow; i++) { // 从第 2 行开始（跳过表头）
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String name = getColumnStringValue(row, columnMap, "name");
                    String sku = getColumnStringValue(row, columnMap, "sku");
                    String brand = getColumnStringValue(row, columnMap, "brand");
                    String carModel = getColumnStringValue(row, columnMap, "carModel");
                    String category = getColumnStringValue(row, columnMap, "category");
                    String unit = getColumnStringValue(row, columnMap, "unit");
                    BigDecimal costPrice = getColumnBigDecimalValue(row, columnMap, "costPrice");
                    BigDecimal salePrice = getColumnBigDecimalValue(row, columnMap, "salePrice");
                    Integer minStock = getColumnIntegerValue(row, columnMap, "minStock");
                    String description = getColumnStringValue(row, columnMap, "description");

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

                    // 处理图片：优先使用 Excel 内嵌图片，其次使用"图片"列的 URL
                    PictureData pictureData = rowImages.get(i);
                    if (pictureData != null) {
                        String suffix = getFileSuffix(pictureData.getMimeType());
                        String imageUrl = saveImage(pictureData.getData(), suffix);
                        product.setImageUrl(imageUrl);
                        imageCount++;
                    } else {
                        // 检查是否有"图片"列
                        String imageUrlFromColumn = getColumnStringValue(row, columnMap, "image");
                        if (imageUrlFromColumn != null && !imageUrlFromColumn.isBlank()) {
                            product.setImageUrl(imageUrlFromColumn.trim());
                        }
                    }

                    productMapper.insert(product);
                    successCount++;
                } catch (Exception e) {
                    addError(errors, i + 1, e.getMessage());
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException(400, "文件读取失败: " + e.getMessage());
        }

        log.info("导入完成：成功 {} 条，跳过 {} 条，图片 {} 张，错误 {} 条",
                successCount, skippedCount, imageCount, errors.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", successCount);
        result.put("skipped", skippedCount);
        result.put("images", imageCount);
        result.put("errors", errors);
        return result;
    }

    /**
     * 根据表头行建立列名映射，支持中英文多种表头名称
     * 例如："商品名称"、"名称"、"name" 都映射到字段 "name"
     */
    private Map<String, Integer> buildColumnMap(Row headerRow) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) continue;
            String header = getCellStringValue(cell);
            if (header == null) continue;
            header = header.trim().toLowerCase().replace("*", "").replace(" ", "");

            // 商品名称
            if (header.contains("商品名称") || header.contains("名称") || header.equals("name") || header.contains("productname")) {
                map.putIfAbsent("name", i);
            }
            // SKU
            else if (header.equals("sku") || header.contains("商品编码") || header.contains("商品编号") || header.contains("productcode")) {
                map.putIfAbsent("sku", i);
            }
            // 品牌
            else if (header.contains("品牌") || header.equals("brand")) {
                map.putIfAbsent("brand", i);
            }
            // 车型
            else if (header.contains("车型") || header.contains("适配") || header.contains("carmodel") || header.contains("carmodels")) {
                map.putIfAbsent("carModel", i);
            }
            // 分类
            else if (header.contains("分类") || header.equals("category")) {
                map.putIfAbsent("category", i);
            }
            // 单位
            else if (header.contains("单位") || header.equals("unit")) {
                map.putIfAbsent("unit", i);
            }
            // 进价
            else if (header.contains("进价") || header.contains("成本") || header.contains("costprice") || header.contains("cost")) {
                map.putIfAbsent("costPrice", i);
            }
            // 售价
            else if (header.contains("售价") || header.contains("销售") || header.contains("saleprice") || header.contains("price")) {
                map.putIfAbsent("salePrice", i);
            }
            // 库存预警
            else if (header.contains("库存预警") || header.contains("最低库存") || header.contains("minstock") || header.contains("预警")) {
                map.putIfAbsent("minStock", i);
            }
            // 描述
            else if (header.contains("描述") || header.equals("description")) {
                map.putIfAbsent("description", i);
            }
            // 图片
            else if (header.contains("图片") || header.contains("image") || header.contains("photo") || header.contains("picture")) {
                map.putIfAbsent("image", i);
            }
        }
        return map;
    }

    /** 获取表头名称列表（用于错误提示） */
    private String getHeaderNames(Row headerRow) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            names.add(cell != null ? getCellStringValue(cell) : "空");
        }
        return String.join(", ", names);
    }

    /** 根据列映射读取字符串值 */
    private String getColumnStringValue(Row row, Map<String, Integer> columnMap, String field) {
        Integer colIndex = columnMap.get(field);
        if (colIndex == null) return null;
        return getCellStringValue(row.getCell(colIndex));
    }

    /** 根据列映射读取 BigDecimal 值 */
    private BigDecimal getColumnBigDecimalValue(Row row, Map<String, Integer> columnMap, String field) {
        Integer colIndex = columnMap.get(field);
        if (colIndex == null) return null;
        return getCellBigDecimalValue(row.getCell(colIndex));
    }

    /** 根据列映射读取 Integer 值 */
    private Integer getColumnIntegerValue(Row row, Map<String, Integer> columnMap, String field) {
        Integer colIndex = columnMap.get(field);
        if (colIndex == null) return null;
        return getCellIntegerValue(row.getCell(colIndex));
    }

    @Override
    public void generateImportTemplate(HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("商品导入模板");

            // 表头（最后一列为"图片"，用于填写图片URL；内嵌图片自动识别）
            String[] headers = {"商品名称*", "SKU*", "品牌", "车型", "分类", "单位", "进价", "售价*", "库存预警", "描述", "图片"};
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

    /**
     * 从 Excel 工作表中提取内嵌图片，建立 行号 → 图片数据 的映射
     * 根据图片锚点的左上角行号来确定图片属于哪一行
     */
    private Map<Integer, PictureData> extractRowImages(Sheet sheet) {
        Map<Integer, PictureData> rowImages = new HashMap<>();
        try {
            Drawing<?> drawing = sheet.getDrawingPatriarch();
            if (drawing == null) return rowImages;

            if (drawing instanceof XSSFDrawing xssfDrawing) {
                for (XSSFShape shape : xssfDrawing.getShapes()) {
                    if (shape instanceof XSSFPicture picture) {
                        XSSFAnchor anchor = picture.getAnchor();
                        int rowIndex;
                        if (anchor instanceof org.apache.poi.xssf.usermodel.XSSFClientAnchor clientAnchor) {
                            rowIndex = clientAnchor.getRow1();
                        } else {
                            // 非 ClientAnchor 类型的锚点，跳过
                            continue;
                        }
                        PictureData pictureData = picture.getPictureData();
                        if (pictureData != null && pictureData.getData() != null) {
                            rowImages.put(rowIndex, pictureData);
                            log.debug("找到图片：行 {}，类型 {}，大小 {} bytes",
                                    rowIndex, pictureData.getMimeType(), pictureData.getData().length);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取 Excel 内嵌图片失败（可能没有图片）: {}", e.getMessage());
        }
        return rowImages;
    }

    /**
     * 保存图片文件到 uploads/images/ 目录
     * @param data   图片二进制数据
     * @param suffix 文件后缀（如 .png, .jpg）
     * @return 图片的访问 URL（如 /uploads/images/xxx.png）
     */
    private String saveImage(byte[] data, String suffix) throws IOException {
        String basePath = System.getProperty("user.dir");
        File dir = new File(basePath, uploadPath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("无法创建图片目录: " + dir.getAbsolutePath());
        }

        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
        File file = new File(dir, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
            fos.flush();
        }

        return uploadUrlPrefix + "/" + fileName;
    }

    /** 根据 MIME 类型返回文件后缀 */
    private String getFileSuffix(String mimeType) {
        if (mimeType == null) return ".png";
        return switch (mimeType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "image/bmp" -> ".bmp";
            default -> ".png";
        };
    }
}
