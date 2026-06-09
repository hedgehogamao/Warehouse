package com.autoparts.service;

import com.autoparts.common.BusinessException;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单服务单元测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Product testProduct1;
    private Product testProduct2;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 创建测试商品1
        testProduct1 = new Product();
        testProduct1.setName("商品1");
        testProduct1.setSku("SKU-001");
        testProduct1.setBrand("品牌A");
        testProduct1.setCategory("分类A");
        testProduct1.setUnit("个");
        testProduct1.setCostPrice(new BigDecimal("100.00"));
        testProduct1.setSalePrice(new BigDecimal("150.00"));
        testProduct1.setStock(50);
        testProduct1.setMinStock(10);
        testProduct1.setStatus(1);
        productMapper.insert(testProduct1);

        // 创建测试商品2
        testProduct2 = new Product();
        testProduct2.setName("商品2");
        testProduct2.setSku("SKU-002");
        testProduct2.setBrand("品牌B");
        testProduct2.setCategory("分类B");
        testProduct2.setUnit("套");
        testProduct2.setCostPrice(new BigDecimal("200.00"));
        testProduct2.setSalePrice(new BigDecimal("300.00"));
        testProduct2.setStock(30);
        testProduct2.setMinStock(5);
        testProduct2.setStatus(1);
        productMapper.insert(testProduct2);

        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole("STAFF");
        testUser.setStatus(1);
        userMapper.insert(testUser);
    }

    @Test
    @DisplayName("下单成功 - 扣库存")
    void testCreateOrder() {
        int stock1Before = testProduct1.getStock();
        int stock2Before = testProduct2.getStock();
        int orderQty1 = 2;
        int orderQty2 = 3;

        Map<String, Object> request = new HashMap<>();
        request.put("customerName", "测试客户");
        request.put("customerPhone", "13800138000");
        request.put("paymentMethod", "CASH");

        List<Map<String, Object>> items = new ArrayList<>();

        Map<String, Object> item1 = new HashMap<>();
        item1.put("productId", testProduct1.getId());
        item1.put("quantity", orderQty1);
        item1.put("unitPrice", testProduct1.getSalePrice());
        items.add(item1);

        Map<String, Object> item2 = new HashMap<>();
        item2.put("productId", testProduct2.getId());
        item2.put("quantity", orderQty2);
        item2.put("unitPrice", testProduct2.getSalePrice());
        items.add(item2);

        request.put("items", items);

        Map<String, Object> result = orderService.createOrder(request, testUser.getId());

        assertNotNull(result);
        assertNotNull(result.get("orderNo"));
        assertNotNull(result.get("id"));

        // 验证库存减少
        Product updatedProduct1 = productMapper.selectById(testProduct1.getId());
        Product updatedProduct2 = productMapper.selectById(testProduct2.getId());
        assertEquals(stock1Before - orderQty1, updatedProduct1.getStock());
        assertEquals(stock2Before - orderQty2, updatedProduct2.getStock());
    }

    @Test
    @DisplayName("下单失败 - 库存不足")
    void testCreateOrder_NoStock() {
        Map<String, Object> request = new HashMap<>();
        request.put("customerName", "测试客户");
        request.put("paymentMethod", "CASH");

        List<Map<String, Object>> items = new ArrayList<>();

        Map<String, Object> item1 = new HashMap<>();
        item1.put("productId", testProduct1.getId());
        item1.put("quantity", 999); // 超过库存数量
        item1.put("unitPrice", testProduct1.getSalePrice());
        items.add(item1);

        request.put("items", items);

        assertThrows(BusinessException.class,
                () -> orderService.createOrder(request, testUser.getId()));
    }

    @Test
    @DisplayName("退款成功 - 恢复库存")
    void testRefundOrder() {
        int stockBefore = testProduct1.getStock();
        int orderQty = 2;

        // 先下单
        Map<String, Object> request = new HashMap<>();
        request.put("customerName", "测试客户");
        request.put("paymentMethod", "CASH");

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("productId", testProduct1.getId());
        item1.put("quantity", orderQty);
        item1.put("unitPrice", testProduct1.getSalePrice());
        items.add(item1);
        request.put("items", items);

        Map<String, Object> orderResult = orderService.createOrder(request, testUser.getId());
        Integer orderId = (Integer) orderResult.get("id");

        // 验证库存已减少
        Product afterOrderProduct = productMapper.selectById(testProduct1.getId());
        assertEquals(stockBefore - orderQty, afterOrderProduct.getStock());

        // 退款
        orderService.refundOrder(orderId, "测试退款", testUser.getId());

        // 验证库存恢复
        Product afterRefundProduct = productMapper.selectById(testProduct1.getId());
        assertEquals(stockBefore, afterRefundProduct.getStock());
    }

    @Test
    @DisplayName("订单详情查询")
    void testGetOrderDetail() {
        // 先下单
        Map<String, Object> request = new HashMap<>();
        request.put("customerName", "测试客户");
        request.put("paymentMethod", "WECHAT");

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("productId", testProduct1.getId());
        item1.put("quantity", 1);
        item1.put("unitPrice", testProduct1.getSalePrice());
        items.add(item1);
        request.put("items", items);

        Map<String, Object> orderResult = orderService.createOrder(request, testUser.getId());
        Integer orderId = (Integer) orderResult.get("id");

        // 查询详情
        Map<String, Object> detail = orderService.getOrderDetail(orderId);

        assertNotNull(detail);
        assertNotNull(detail.get("orderNo"));
        assertEquals("测试客户", detail.get("customerName"));
        assertNotNull(detail.get("items"));
    }

    @Test
    @DisplayName("订单列表查询")
    void testPageOrders() {
        // 先创建订单
        Map<String, Object> request = new HashMap<>();
        request.put("customerName", "查询客户");
        request.put("paymentMethod", "CASH");

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("productId", testProduct1.getId());
        item1.put("quantity", 1);
        item1.put("unitPrice", testProduct1.getSalePrice());
        items.add(item1);
        request.put("items", items);

        orderService.createOrder(request, testUser.getId());

        // 查询订单列表
        var result = orderService.pageOrders(1, 10, null, "查询客户", null, null);

        assertNotNull(result);
        assertTrue(result.getTotal() > 0);
    }
}
