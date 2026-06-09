package com.autoparts.service;

import com.autoparts.model.Order;
import com.autoparts.model.OrderItem;
import com.autoparts.model.Product;
import com.autoparts.model.User;
import com.autoparts.repository.ProductMapper;
import com.autoparts.repository.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 统计服务单元测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StatisticsServiceTest {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderService orderService;

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
        testProduct.setCategory("机油");
        testProduct.setUnit("桶");
        testProduct.setCostPrice(new BigDecimal("100.00"));
        testProduct.setSalePrice(new BigDecimal("150.00"));
        testProduct.setStock(100);
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
    @DisplayName("今日概况统计")
    void testTodaySummary() {
        // 先创建一些订单数据
        createTestOrder();

        Map<String, Object> summary = statisticsService.getTodaySummary();

        assertNotNull(summary);
        assertTrue(summary.containsKey("todaySalesAmount"));
        assertTrue(summary.containsKey("todayOrderCount"));
        assertTrue(summary.containsKey("todayStockInCount"));
    }

    @Test
    @DisplayName("销售趋势统计")
    void testSalesTrend() {
        // 先创建一些订单数据
        createTestOrder();

        Map<String, Object> trend = statisticsService.getSalesTrend(7);

        assertNotNull(trend);
        assertTrue(trend.containsKey("days"));
        assertTrue(trend.containsKey("amounts"));
        assertTrue(trend.containsKey("counts"));
    }

    @Test
    @DisplayName("商品销售排行")
    void testTopProducts() {
        // 先创建一些订单数据
        createTestOrder();

        List<Map<String, Object>> topProducts = statisticsService.getTopProducts(30, 10);

        assertNotNull(topProducts);
    }

    @Test
    @DisplayName("分类占比统计")
    void testCategoryDistribution() {
        // 先创建一些订单数据
        createTestOrder();

        List<Map<String, Object>> distribution = statisticsService.getCategoryDistribution(30);

        assertNotNull(distribution);
    }

    @Test
    @DisplayName("无数据时统计不报错")
    void testStatisticsWithNoData() {
        // 不创建任何订单，直接查询统计
        Map<String, Object> summary = statisticsService.getTodaySummary();
        assertNotNull(summary);

        Map<String, Object> trend = statisticsService.getSalesTrend(7);
        assertNotNull(trend);

        List<Map<String, Object>> topProducts = statisticsService.getTopProducts(30, 10);
        assertNotNull(topProducts);

        List<Map<String, Object>> distribution = statisticsService.getCategoryDistribution(30);
        assertNotNull(distribution);
    }

    private void createTestOrder() {
        Map<String, Object> request = new HashMap<>();
        request.put("customerName", "统计测试客户");
        request.put("customerPhone", "13800138000");
        request.put("paymentMethod", "CASH");

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("productId", testProduct.getId());
        item.put("quantity", 2);
        item.put("unitPrice", testProduct.getSalePrice());
        items.add(item);
        request.put("items", items);

        orderService.createOrder(request, testUser.getId());
    }
}
