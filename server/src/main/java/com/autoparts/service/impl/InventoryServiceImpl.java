package com.autoparts.service.impl;

import com.autoparts.common.BusinessException;
import com.autoparts.model.InventoryLog;
import com.autoparts.model.Product;
import com.autoparts.repository.InventoryLogMapper;
import com.autoparts.repository.ProductMapper;
import com.autoparts.service.InventoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存业务逻辑实现
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    private final ProductMapper productMapper;
    private final InventoryLogMapper inventoryLogMapper;

    public InventoryServiceImpl(ProductMapper productMapper,
                                 InventoryLogMapper inventoryLogMapper) {
        this.productMapper = productMapper;
        this.inventoryLogMapper = inventoryLogMapper;
    }

    @Override
    public IPage<Map<String, Object>> pageInventory(int page, int size,
                                                      String keyword, String category,
                                                      Boolean lowStock) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（名称/SKU/车型）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(Product::getName, keyword)
                    .or().like(Product::getSku, keyword)
                    .or().like(Product::getCarModel, keyword)
            );
        }
        // 分类筛选
        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq(Product::getCategory, category);
        }
        // 仅显示库存不足：stock = 0 或 (stock <= minStock 且 minStock > 0)
        if (Boolean.TRUE.equals(lowStock)) {
            wrapper.and(w -> w
                    .eq(Product::getStock, 0)
                    .or().apply("stock <= min_stock AND min_stock > 0")
            );
        }

        // 仅显示上架商品
        wrapper.eq(Product::getStatus, 1);
        wrapper.orderByAsc(Product::getStock);

        IPage<Product> productPage = productMapper.selectPage(new Page<>(page, size), wrapper);

        // 转换为库存视图
        IPage<Map<String, Object>> resultPage = new Page<>(page, size, productPage.getTotal());
        resultPage.setRecords(productPage.getRecords().stream().map(product -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("productId", product.getId());
            map.put("productName", product.getName());
            map.put("sku", product.getSku());
            map.put("brand", product.getBrand());
            map.put("category", product.getCategory());
            map.put("stock", product.getStock());
            map.put("minStock", product.getMinStock());
            // 计算库存状态
            map.put("status", calculateStockStatus(product.getStock(), product.getMinStock()));
            return map;
        }).toList());

        return resultPage;
    }

    @Override
    public List<Map<String, Object>> getLowStockProducts() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);
        // stock = 0 或 (stock <= minStock 且 minStock > 0)
        wrapper.and(w -> w
                .eq(Product::getStock, 0)
                .or().apply("stock <= min_stock AND min_stock > 0")
        );
        wrapper.orderByAsc(Product::getStock);

        List<Product> products = productMapper.selectList(wrapper);

        return products.stream().map(product -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("productId", product.getId());
            map.put("productName", product.getName());
            map.put("sku", product.getSku());
            map.put("stock", product.getStock());
            map.put("minStock", product.getMinStock());
            map.put("status", calculateStockStatus(product.getStock(), product.getMinStock()));
            return map;
        }).toList();
    }

    @Override
    public IPage<InventoryLog> pageInventoryLogs(int page, int size,
                                                   Integer productId,
                                                   String startDate, String endDate) {
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryLog::getProductId, productId);

        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(InventoryLog::getCreatedAt,
                    LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(InventoryLog::getCreatedAt,
                    LocalDateTime.parse(endDate + "T23:59:59"));
        }

        wrapper.orderByDesc(InventoryLog::getCreatedAt);

        return inventoryLogMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustStock(Integer productId, Integer quantity, String reason,
                             Integer operatorId) {
        if (quantity == null || quantity == 0) {
            throw new BusinessException(400, "调整数量不能为0");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException(400, "调整原因不能为空");
        }

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        int beforeStock = product.getStock();
        int afterStock = beforeStock + quantity;
        if (afterStock < 0) {
            throw new BusinessException(400, "库存不足，当前库存: " + beforeStock);
        }

        // 更新库存
        product.setStock(afterStock);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);

        // 记录流水
        InventoryLog log = new InventoryLog();
        log.setProductId(productId);
        log.setType("ADJUST");
        log.setQuantity(quantity);
        log.setBeforeStock(beforeStock);
        log.setAfterStock(afterStock);
        log.setRemark(reason);
        log.setOperatorId(operatorId);
        log.setCreatedAt(LocalDateTime.now());
        inventoryLogMapper.insert(log);
    }

    /**
     * 计算库存状态
     */
    private String calculateStockStatus(int stock, Integer minStock) {
        if (stock <= 0) return "OUT_OF_STOCK";
        if (minStock != null && minStock > 0 && stock <= minStock) return "LOW_STOCK";
        return "NORMAL";
    }
}
