package com.autoparts.controller;

import com.autoparts.common.Result;
import com.autoparts.dto.ProductSaveRequest;
import com.autoparts.model.Product;
import com.autoparts.service.ProductService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 商品管理接口
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 商品列表（分页 + 搜索 + 筛选）
     */
    @GetMapping
    public Result<IPage<Product>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        IPage<Product> result = productService.pageProducts(page, size, keyword, brand, category, status);
        return Result.success(result);
    }

    /**
     * 商品详情
     */
    @GetMapping("/{id}")
    public Result<Product> getProduct(@PathVariable Integer id) {
        Product product = productService.getProductById(id);
        return Result.success(product);
    }

    /**
     * 新增商品
     */
    @PostMapping
    public Result<Void> createProduct(@Valid @RequestBody ProductSaveRequest request) {
        productService.createProduct(request);
        return Result.success(null);
    }

    /**
     * 修改商品
     */
    @PutMapping("/{id}")
    public Result<Void> updateProduct(@PathVariable Integer id,
                                       @Valid @RequestBody ProductSaveRequest request) {
        productService.updateProduct(id, request);
        return Result.success(null);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return Result.success(null);
    }

    /**
     * 上架/下架商品
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateProductStatus(@PathVariable Integer id,
                                             @RequestBody Map<String, Integer> body) {
        productService.updateProductStatus(id, body.get("status"));
        return Result.success(null);
    }

    /**
     * 获取商品分类列表（从已有数据中 DISTINCT）
     */
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        List<String> categories = productService.getCategories();
        return Result.success(categories);
    }

    /**
     * POS 快速搜索商品（仅返回 id, name, sku, salePrice, stock）
     */
    @GetMapping("/pos-list")
    public Result<List<Map<String, Object>>> posList(
            @RequestParam(required = false) String keyword) {
        List<Map<String, Object>> result = productService.posProductSearch(keyword);
        return Result.success(result);
    }

    /**
     * Excel 批量导入商品
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importProducts(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = productService.importProducts(file);
        return Result.success(result);
    }

    /**
     * 下载导入模板
     */
    @GetMapping("/import-template")
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        productService.generateImportTemplate(response);
    }
}
