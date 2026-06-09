package com.autoparts.service;

import com.autoparts.common.BusinessException;
import com.autoparts.model.Product;
import com.autoparts.model.User;
import com.autoparts.repository.ProductMapper;
import com.autoparts.repository.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 入库服务单元测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StockInServiceTest {

    @Autowired
    private StockInService stockInService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Product testProduct;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 创建测试商品
        testProduct = new Product();
        testProduct.setName("测试商品");
        testProduct.setSku("TEST-001");
        testProduct.setBrand("测试品牌");
        testProduct.setCategory("测试分类");
        testProduct.setUnit("个");
        testProduct.setCostPrice(new BigDecimal("100.00"));
        testProduct.setSalePrice(new BigDecimal("150.00"));
        testProduct.setStock(50);
        testProduct.setMinStock(10);
        testProduct.setStatus(1);
        productMapper.insert(testProduct);

        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole("STAFF");
        testUser.setStatus(1);
        userMapper.insert(testUser);
    }

    @Test
    @DisplayName("入库成功 - 库存增加")
    void testStockIn() {
        int beforeStock = testProduct.getStock();
        int quantity = 20;

        Map<String, Object> result = stockInService.createStockIn(
                testProduct.getId(),
                quantity,
                new BigDecimal("95.00"),
                "测试入库",
                testUser.getId()
        );

        assertNotNull(result);
        assertEquals(quantity, result.get("quantity"));

        // 验证库存增加
        Product updatedProduct = productMapper.selectById(testProduct.getId());
        assertEquals(beforeStock + quantity, updatedProduct.getStock());
    }

    @Test
    @DisplayName("入库失败 - 商品不存在")
    void testStockIn_ProductNotFound() {
        assertThrows(BusinessException.class,
                () -> stockInService.createStockIn(
                        99999, // 不存在的商品ID
                        10,
                        new BigDecimal("100.00"),
                        "测试入库",
                        testUser.getId()
                ));
    }

    @Test
    @DisplayName("入库记录查询")
    void testPageStockInRecords() {
        // 先入库
        stockInService.createStockIn(
                testProduct.getId(),
                10,
                new BigDecimal("95.00"),
                "测试入库",
                testUser.getId()
        );

        // 查询入库记录
        var result = stockInService.pageStockInRecords(1, 10, testProduct.getId(), null, null);

        assertNotNull(result);
        assertTrue(result.getTotal() > 0);
    }

    @Test
    @DisplayName("入库详情查询")
    void testGetStockInDetail() {
        // 先入库
        Map<String, Object> stockInResult = stockInService.createStockIn(
                testProduct.getId(),
                10,
                new BigDecimal("95.00"),
                "测试入库",
                testUser.getId()
        );

        Integer stockInId = (Integer) stockInResult.get("id");

        // 查询详情
        Map<String, Object> detail = stockInService.getStockInDetail(stockInId);

        assertNotNull(detail);
        assertEquals(testProduct.getId(), detail.get("productId"));
        assertEquals(10, detail.get("quantity"));
    }

    @Test
    @DisplayName("多次入库累加库存")
    void testMultipleStockIn() {
        int initialStock = testProduct.getStock();

        // 第一次入库
        stockInService.createStockIn(
                testProduct.getId(),
                10,
                new BigDecimal("95.00"),
                "第一次入库",
                testUser.getId()
        );

        // 第二次入库
        stockInService.createStockIn(
                testProduct.getId(),
                20,
                new BigDecimal("90.00"),
                "第二次入库",
                testUser.getId()
        );

        // 验证库存累加
        Product updatedProduct = productMapper.selectById(testProduct.getId());
        assertEquals(initialStock + 10 + 20, updatedProduct.getStock());
    }
}
