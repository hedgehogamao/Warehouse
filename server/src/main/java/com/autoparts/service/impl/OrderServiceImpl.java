package com.autoparts.service.impl;

import com.autoparts.common.BusinessException;
import com.autoparts.model.*;
import com.autoparts.repository.*;
import com.autoparts.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 订单业务逻辑实现
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final UserMapper userMapper;

    public OrderServiceImpl(OrderMapper orderMapper,
                             OrderItemMapper orderItemMapper,
                             ProductMapper productMapper,
                             InventoryLogMapper inventoryLogMapper,
                             UserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.inventoryLogMapper = inventoryLogMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(Map<String, Object> request, Integer operatorId) {
        // 解析请求
        String customerName = (String) request.get("customerName");
        String customerPhone = (String) request.get("customerPhone");
        String paymentMethod = (String) request.get("paymentMethod");
        String remark = (String) request.get("remark");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
        if (items == null || items.isEmpty()) {
            throw new BusinessException(400, "订单商品不能为空");
        }
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            throw new BusinessException(400, "请选择支付方式");
        }

        // 1. 校验商品和库存
        List<Product> products = new ArrayList<>();
        for (Map<String, Object> item : items) {
            Integer productId = (Integer) item.get("productId");
            Integer quantity = (Integer) item.get("quantity");
            if (productId == null || quantity == null || quantity <= 0) {
                throw new BusinessException(400, "商品信息不完整");
            }
            Product product = productMapper.selectById(productId);
            if (product == null || product.getStatus() != 1) {
                throw new BusinessException(400, "商品不存在或已下架: " + productId);
            }
            if (product.getStock() < quantity) {
                throw new BusinessException(400, "库存不足: " + product.getName() + "（剩余" + product.getStock() + "）");
            }
            products.add(product);
        }

        // 2. 生成订单编号
        String orderNo = generateOrderNo();

        // 3. 计算总金额并插入订单
        BigDecimal totalAmount = BigDecimal.ZERO;
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setCustomerName(customerName);
        order.setCustomerPhone(customerPhone);
        order.setPaymentMethod(paymentMethod);
        order.setStatus("PAID");
        order.setRemark(remark);
        order.setOperatorId(operatorId);
        order.setCreatedAt(LocalDateTime.now());

        // 4. 插入明细 + 扣库存 + 记流水
        List<Map<String, Object>> itemResults = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> item = items.get(i);
            Product product = products.get(i);
            Integer quantity = (Integer) item.get("quantity");
            BigDecimal unitPrice = new BigDecimal(item.get("unitPrice").toString());
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(subtotal);

            int beforeStock = product.getStock();

            // 扣减库存
            product.setStock(beforeStock - quantity);
            product.setUpdatedAt(LocalDateTime.now());
            productMapper.updateById(product);

            // 记录库存流水
            InventoryLog log = new InventoryLog();
            log.setProductId(product.getId());
            log.setType("SALE");
            log.setQuantity(-quantity);
            log.setBeforeStock(beforeStock);
            log.setAfterStock(beforeStock - quantity);
            log.setRemark("销售订单: " + orderNo);
            log.setOperatorId(operatorId);
            log.setCreatedAt(LocalDateTime.now());
            inventoryLogMapper.insert(log);

            Map<String, Object> itemResult = new LinkedHashMap<>();
            itemResult.put("productId", product.getId());
            itemResult.put("productName", product.getName());
            itemResult.put("quantity", quantity);
            itemResult.put("unitPrice", unitPrice);
            itemResult.put("subtotal", subtotal);
            itemResults.add(itemResult);
        }

        order.setTotalAmount(totalAmount);
        orderMapper.insert(order);

        // 插入订单明细
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> itemResult = itemResults.get(i);
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId((Integer) itemResult.get("productId"));
            orderItem.setProductName((String) itemResult.get("productName"));
            orderItem.setQuantity((Integer) itemResult.get("quantity"));
            orderItem.setUnitPrice((BigDecimal) itemResult.get("unitPrice"));
            orderItem.setSubtotal((BigDecimal) itemResult.get("subtotal"));
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItemMapper.insert(orderItem);
        }

        // 5. 返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", order.getId());
        result.put("orderNo", orderNo);
        result.put("customerName", customerName);
        result.put("customerPhone", customerPhone);
        result.put("totalAmount", totalAmount);
        result.put("paymentMethod", paymentMethod);
        result.put("status", "PAID");
        result.put("createdAt", order.getCreatedAt());
        result.put("items", itemResults);
        return result;
    }

    @Override
    public IPage<Map<String, Object>> pageOrders(int page, int size,
                                                   String orderNo, String customerName,
                                                   String startDate, String endDate) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        if (orderNo != null && !orderNo.trim().isEmpty()) {
            wrapper.like(Order::getOrderNo, orderNo);
        }
        if (customerName != null && !customerName.trim().isEmpty()) {
            wrapper.like(Order::getCustomerName, customerName);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(Order::getCreatedAt, LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(Order::getCreatedAt, LocalDateTime.parse(endDate + "T23:59:59"));
        }

        wrapper.orderByDesc(Order::getCreatedAt);

        IPage<Order> orderPage = orderMapper.selectPage(new Page<>(page, size), wrapper);

        IPage<Map<String, Object>> resultPage = new Page<>(page, size, orderPage.getTotal());
        resultPage.setRecords(orderPage.getRecords().stream().map(order -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("customerName", order.getCustomerName());
            map.put("totalAmount", order.getTotalAmount());
            map.put("paymentMethod", order.getPaymentMethod());
            map.put("paymentMethodName", getPaymentMethodName(order.getPaymentMethod()));
            map.put("status", order.getStatus());
            // 查询明细条数
            Long itemCount = orderItemMapper.selectCount(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            map.put("itemCount", itemCount);
            // 操作人姓名
            User operator = userMapper.selectById(order.getOperatorId());
            map.put("operatorName", operator != null ? operator.getRealName() : "未知");
            map.put("createdAt", order.getCreatedAt());
            return map;
        }).toList());

        return resultPage;
    }

    @Override
    public Map<String, Object> getOrderDetail(Integer id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", order.getId());
        map.put("orderNo", order.getOrderNo());
        map.put("customerName", order.getCustomerName());
        map.put("customerPhone", order.getCustomerPhone());
        map.put("totalAmount", order.getTotalAmount());
        map.put("paymentMethod", order.getPaymentMethod());
        map.put("paymentMethodName", getPaymentMethodName(order.getPaymentMethod()));
        map.put("status", order.getStatus());
        map.put("remark", order.getRemark());
        map.put("operatorId", order.getOperatorId());
        User operator = userMapper.selectById(order.getOperatorId());
        map.put("operatorName", operator != null ? operator.getRealName() : "未知");
        map.put("createdAt", order.getCreatedAt());

        // 查询明细
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
        List<Map<String, Object>> itemMaps = items.stream().map(item -> {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("id", item.getId());
            itemMap.put("productId", item.getProductId());
            itemMap.put("productName", item.getProductName());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("unitPrice", item.getUnitPrice());
            itemMap.put("subtotal", item.getSubtotal());
            return itemMap;
        }).toList();
        map.put("items", itemMaps);

        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(Integer id, String remark, Integer operatorId) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!"PAID".equals(order.getStatus())) {
            throw new BusinessException(400, "只有已支付的订单才能退款");
        }

        // 恢复库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));

        for (OrderItem item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                int beforeStock = product.getStock();
                product.setStock(beforeStock + item.getQuantity());
                product.setUpdatedAt(LocalDateTime.now());
                productMapper.updateById(product);

                // 记录库存流水
                InventoryLog log = new InventoryLog();
                log.setProductId(product.getId());
                log.setType("SALE");
                log.setQuantity(item.getQuantity()); // 正数表示恢复
                log.setBeforeStock(beforeStock);
                log.setAfterStock(beforeStock + item.getQuantity());
                log.setRefId(order.getId());
                log.setRemark("退款恢复: " + order.getOrderNo() + (remark != null ? " " + remark : ""));
                log.setOperatorId(operatorId);
                log.setCreatedAt(LocalDateTime.now());
                inventoryLogMapper.insert(log);
            }
        }

        // 更新订单状态
        order.setStatus("REFUNDED");
        if (remark != null && !remark.isEmpty()) {
            order.setRemark(remark);
        }
        orderMapper.updateById(order);
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

    /**
     * 生成订单编号：SO + yyyyMMdd + 4位序号
     */
    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String maxNo = orderMapper.getMaxOrderNo(date);
        int seq = 1;
        if (maxNo != null && maxNo.length() >= 14) {
            seq = Integer.parseInt(maxNo.substring(maxNo.length() - 4)) + 1;
        }
        return "SO" + date + String.format("%04d", seq);
    }

    /**
     * 支付方式中文名称
     */
    private String getPaymentMethodName(String method) {
        if (method == null) return "";
        return switch (method) {
            case "CASH" -> "现金";
            case "WECHAT" -> "微信支付";
            case "ALIPAY" -> "支付宝";
            case "CARD" -> "刷卡";
            default -> method;
        };
    }
}
