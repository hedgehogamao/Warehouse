package com.autoparts.controller;

import com.autoparts.common.BusinessException;
import com.autoparts.common.Result;
import com.autoparts.model.InventoryLog;
import com.autoparts.model.Product;
import com.autoparts.repository.InventoryLogMapper;
import com.autoparts.repository.ProductMapper;
import com.autoparts.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 出库管理接口 + 按 SKU 查询（扫码用）
 */
@RestController
@RequestMapping("/api/v1")
public class StockOutController {

    private final ProductMapper productMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final ProductService productService;

    public StockOutController(ProductMapper productMapper,
                               InventoryLogMapper inventoryLogMapper,
                               ProductService productService) {
        this.productMapper = productMapper;
        this.inventoryLogMapper = inventoryLogMapper;
        this.productService = productService;
    }

    /**
     * 按 SKU 查找商品（扫码用，无需认证）
     * GET /api/v1/products/by-sku/{sku}
     */
    @GetMapping("/products/by-sku/{sku}")
    public Result<Map<String, Object>> getProductBySku(@PathVariable String sku) {
        Product product = productMapper.selectOne(
                new LambdaQueryWrapper<Product>().eq(Product::getSku, sku));
        if (product == null) {
            return Result.error(404, "未找到该商品");
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", product.getId());
        map.put("name", product.getName());
        map.put("sku", product.getSku());
        map.put("brand", product.getBrand());
        map.put("category", product.getCategory());
        map.put("salePrice", product.getSalePrice());
        map.put("stock", product.getStock());
        map.put("imageUrl", product.getImageUrl());
        map.put("status", product.getStatus());

        return Result.success(map);
    }

    /**
     * 出库（扣减库存 + 记录流水）
     * POST /api/v1/stock-out
     */
    @PostMapping("/stock-out")
    public Result<Map<String, Object>> stockOut(@RequestBody Map<String, Object> body,
                                                 Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        Integer productId = (Integer) body.get("productId");
        Integer quantity = (Integer) body.get("quantity");
        String remark = (String) body.get("remark");

        if (productId == null || quantity == null || quantity <= 0) {
            throw new BusinessException(400, "商品ID和数量不能为空，数量必须大于0");
        }

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        int beforeStock = product.getStock();
        if (beforeStock < quantity) {
            throw new BusinessException(400, "库存不足，当前库存: " + beforeStock);
        }

        int afterStock = beforeStock - quantity;

        // 扣减库存
        product.setStock(afterStock);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);

        // 记录库存流水
        InventoryLog log = new InventoryLog();
        log.setProductId(productId);
        log.setType("SALE");
        log.setQuantity(-quantity);
        log.setBeforeStock(beforeStock);
        log.setAfterStock(afterStock);
        log.setRemark("出库" + (remark != null && !remark.isEmpty() ? ": " + remark : ""));
        log.setOperatorId(userId);
        log.setCreatedAt(LocalDateTime.now());
        inventoryLogMapper.insert(log);

        // 返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("productId", productId);
        result.put("productName", product.getName());
        result.put("quantity", quantity);
        result.put("beforeStock", beforeStock);
        result.put("afterStock", afterStock);

        return Result.success(result);
    }
}
