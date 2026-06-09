package com.autoparts.controller;

import com.autoparts.common.Result;
import com.autoparts.service.OrderService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单管理接口
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单（销售下单）
     */
    @PostMapping
    public Result<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request,
                                                    Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        Map<String, Object> result = orderService.createOrder(request, userId);
        return Result.success(result);
    }

    /**
     * 订单列表
     */
    @GetMapping
    public Result<IPage<Map<String, Object>>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<Map<String, Object>> result = orderService.pageOrders(
                page, size, orderNo, customerName, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 订单详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Integer id) {
        Map<String, Object> result = orderService.getOrderDetail(id);
        return Result.success(result);
    }

    /**
     * 订单退款
     */
    @PutMapping("/{id}/refund")
    public Result<Void> refundOrder(@PathVariable Integer id,
                                     @RequestBody Map<String, String> body,
                                     Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        String remark = body.get("remark");
        orderService.refundOrder(id, remark, userId);
        return Result.success(null);
    }
}
