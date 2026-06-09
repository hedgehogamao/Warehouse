package com.autoparts.service;

import com.autoparts.common.BusinessException;
import com.autoparts.dto.ProductSaveRequest;
import com.autoparts.model.Product;
import com.autoparts.repository.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 商品服务单元测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        // 创建测试商品
        testProduct = new Product();
        testProduct.setName("测试商品");
        testProduct.setSku("TEST-001");
        testProduct.setBrand("测试品牌");
        testProduct.setCarModel("测试车型");
        testProduct.setCategory("测试分类");
        testProduct.setUnit("个");
        testProduct.setCostPrice(new BigDecimal("100.00"));
        testProduct.setSalePrice(new BigDecimal("150.00"));
        testProduct.setStock(50);
        testProduct.setMinStock(10);
        testProduct.setStatus(1);
        productMapper.insert(testProduct);
    }

    @Test
    @DisplayName("新增商品成功")
    void testCreateProduct() {
        ProductSaveRequest request = new ProductSaveRequest();
        request.setName("新商品");
        request.setSku("NEW-001");
        request.setBrand("新品牌");
        request.setSalePrice(new BigDecimal("200.00"));
        request.setUnit("个");

        productService.createProduct(request);

        Product savedProduct = productMapper.selectOne(
                new LambdaQueryWrapper<Product>().eq(Product::getSku, "NEW-001")
        );

        assertNotNull(savedProduct);
        assertEquals("新商品", savedProduct.getName());
        assertEquals("NEW-001", savedProduct.getSku());
    }

    @Test
    @DisplayName("新增商品失败 - SKU重复")
    void testCreateProduct_DuplicateSku() {
        ProductSaveRequest request = new ProductSaveRequest();
        request.setName("重复商品");
        request.setSku("TEST-001"); // 已存在的SKU
        request.setSalePrice(new BigDecimal("200.00"));
        request.setUnit("个");

        assertThrows(BusinessException.class, () -> productService.createProduct(request));
    }

    @Test
    @DisplayName("关键字搜索商品")
    void testSearchByKeyword() {
        IPage<Product> result = productService.pageProducts(1, 10, "测试", null, null, null);

        assertTrue(result.getTotal() > 0);
        assertTrue(result.getRecords().stream()
                .anyMatch(p -> p.getName().contains("测试")));
    }

    @Test
    @DisplayName("按品牌筛选商品")
    void testSearchByBrand() {
        IPage<Product> result = productService.pageProducts(1, 10, null, "测试品牌", null, null);

        assertTrue(result.getTotal() > 0);
        assertTrue(result.getRecords().stream()
                .allMatch(p -> "测试品牌".equals(p.getBrand())));
    }

    @Test
    @DisplayName("按分类筛选商品")
    void testSearchByCategory() {
        IPage<Product> result = productService.pageProducts(1, 10, null, null, "测试分类", null);

        assertTrue(result.getTotal() > 0);
        assertTrue(result.getRecords().stream()
                .allMatch(p -> "测试分类".equals(p.getCategory())));
    }

    @Test
    @DisplayName("按状态筛选商品")
    void testSearchByStatus() {
        IPage<Product> result = productService.pageProducts(1, 10, null, null, null, 1);

        assertTrue(result.getTotal() > 0);
        assertTrue(result.getRecords().stream()
                .allMatch(p -> p.getStatus() == 1));
    }

    @Test
    @DisplayName("修改商品成功")
    void testUpdateProduct() {
        ProductSaveRequest request = new ProductSaveRequest();
        request.setName("修改后的商品");
        request.setSalePrice(new BigDecimal("180.00"));

        productService.updateProduct(testProduct.getId(), request);

        Product updatedProduct = productMapper.selectById(testProduct.getId());
        assertEquals("修改后的商品", updatedProduct.getName());
        assertEquals(new BigDecimal("180.00"), updatedProduct.getSalePrice());
    }

    @Test
    @DisplayName("删除商品成功")
    void testDeleteProduct() {
        productService.deleteProduct(testProduct.getId());

        assertNull(productMapper.selectById(testProduct.getId()));
    }

    @Test
    @DisplayName("上架/下架商品成功")
    void testUpdateProductStatus() {
        // 下架
        productService.updateProductStatus(testProduct.getId(), 0);
        Product updatedProduct = productMapper.selectById(testProduct.getId());
        assertEquals(0, updatedProduct.getStatus());

        // 上架
        productService.updateProductStatus(testProduct.getId(), 1);
        updatedProduct = productMapper.selectById(testProduct.getId());
        assertEquals(1, updatedProduct.getStatus());
    }

    @Test
    @DisplayName("获取分类列表")
    void testGetCategories() {
        List<String> categories = productService.getCategories();

        assertNotNull(categories);
        assertTrue(categories.contains("测试分类"));
    }

    @Test
    @DisplayName("POS搜索商品")
    void testPosProductSearch() {
        var results = productService.posProductSearch("测试");

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.get(0).containsKey("id"));
        assertTrue(results.get(0).containsKey("name"));
        assertTrue(results.get(0).containsKey("sku"));
        assertTrue(results.get(0).containsKey("salePrice"));
        assertTrue(results.get(0).containsKey("stock"));
    }

    @Test
    @DisplayName("SKU唯一性校验")
    void testSkuUnique() {
        ProductSaveRequest request1 = new ProductSaveRequest();
        request1.setName("商品1");
        request1.setSku("UNIQUE-001");
        request1.setSalePrice(new BigDecimal("100.00"));
        request1.setUnit("个");
        productService.createProduct(request1);

        ProductSaveRequest request2 = new ProductSaveRequest();
        request2.setName("商品2");
        request2.setSku("UNIQUE-001"); // 相同SKU
        request2.setSalePrice(new BigDecimal("200.00"));
        request2.setUnit("个");

        assertThrows(BusinessException.class, () -> productService.createProduct(request2));
    }
}
