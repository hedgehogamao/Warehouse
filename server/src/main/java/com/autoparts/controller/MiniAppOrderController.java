package com.autoparts.controller;

import com.autoparts.common.Result;
import com.autoparts.model.Order;
import com.autoparts.model.OrderItem;
import com.autoparts.repository.OrderItemMapper;
import com.autoparts.repository.OrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 小程序订单查询接口
 */
@RestController
@RequestMapping("/api/v1/miniapp/orders")
public class MiniAppOrderController {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    public MiniAppOrderController(OrderMapper orderMapper, OrderItemMapper orderItemMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
    }

    /**
     * 客户购买记录（仅当前客户的订单）
     */
    @GetMapping
    public Result<IPage<Map<String, Object>>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        // 从 Token 中获取客户 ID（格式: CUSTOMER_{id}）
        Integer customerId = extractCustomerId(authentication);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getCustomerId, customerId);
        wrapper.orderByDesc(Order::getCreatedAt);

        IPage<Order> orderPage = orderMapper.selectPage(new Page<>(page, size), wrapper);

        IPage<Map<String, Object>> resultPage = new Page<>(page, size, orderPage.getTotal());
        resultPage.setRecords(orderPage.getRecords().stream().map(order -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("totalAmount", order.getTotalAmount());
            map.put("paymentMethod", order.getPaymentMethod());
            map.put("paymentMethodName", getPaymentMethodName(order.getPaymentMethod()));
            map.put("status", order.getStatus());
            map.put("createdAt", order.getCreatedAt());

            // 查询明细
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            map.put("itemCount", items.size());
            map.put("items", items.stream().map(item -> {
                Map<String, Object> itemMap = new LinkedHashMap<>();
                itemMap.put("productName", item.getProductName());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("unitPrice", item.getUnitPrice());
                itemMap.put("subtotal", item.getSubtotal());
                return itemMap;
            }).toList());

            return map;
        }).toList());

        return Result.success(resultPage);
    }

    private Integer extractCustomerId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Integer) {
            return (Integer) principal;
        }
        return null;
    }

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
