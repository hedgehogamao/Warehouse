package com.autoparts.controller;

import com.autoparts.common.Result;
import com.autoparts.model.Product;
import com.autoparts.service.ProductService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 小程序商品接口
 */
@RestController
@RequestMapping("/api/v1/miniapp/products")
public class MiniAppProductController {

    private final ProductService productService;

    public MiniAppProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 商品列表（仅上架商品）
     */
    @GetMapping
    public Result<?> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand) {

        IPage<Product> productPage = productService.pageProducts(
                page, size, keyword, brand, category, 1); // status=1 仅上架

        // 转换为简练格式
        IPage<Map<String, Object>> resultPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                page, size, productPage.getTotal());
        resultPage.setRecords(productPage.getRecords().stream().map(p -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("sku", p.getSku());
            map.put("brand", p.getBrand());
            map.put("carModel", p.getCarModel());
            map.put("category", p.getCategory());
            map.put("salePrice", p.getSalePrice());
            map.put("stock", p.getStock());
            map.put("imageUrl", p.getImageUrl());
            map.put("stockStatus", calculateStockStatus(p.getStock(), p.getMinStock()));
            return map;
        }).toList());

        return Result.success(resultPage);
    }

    /**
     * 商品详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getProduct(@PathVariable Integer id) {
        Product p = productService.getProductById(id);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", p.getId());
        map.put("name", p.getName());
        map.put("sku", p.getSku());
        map.put("brand", p.getBrand());
        map.put("carModel", p.getCarModel());
        map.put("category", p.getCategory());
        map.put("salePrice", p.getSalePrice());
        map.put("stock", p.getStock());
        map.put("imageUrl", p.getImageUrl());
        map.put("description", p.getDescription());
        map.put("stockStatus", calculateStockStatus(p.getStock(), p.getMinStock()));

        return Result.success(map);
    }

    /**
     * 分类列表
     */
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        return Result.success(productService.getCategories());
    }

    private String calculateStockStatus(int stock, Integer minStock) {
        if (stock <= 0) return "OUT_OF_STOCK";
        if (minStock != null && minStock > 0 && stock <= minStock) return "LOW_STOCK";
        return "NORMAL";
    }
}
