package com.autoparts.service;

import com.autoparts.model.Order;
import com.autoparts.model.OrderItem;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * 订单业务逻辑接口
 */
public interface OrderService {

    /**
     * 创建订单（事务：校验库存 + 插入订单/明细 + 扣库存 + 记流水）
     */
    Map<String, Object> createOrder(Map<String, Object> request, Integer operatorId);

    /**
     * 订单列表（分页 + 搜索）
     */
    IPage<Map<String, Object>> pageOrders(int page, int size,
                                            String orderNo, String customerName,
                                            String startDate, String endDate);

    /**
     * 订单详情（含明细）
     */
    Map<String, Object> getOrderDetail(Integer id);

    /**
     * 订单退款（事务：状态变更 + 恢复库存 + 记流水）
     */
    void refundOrder(Integer id, String remark, Integer operatorId);

    /**
     * POS 快速搜索商品（仅返回 id, name, sku, salePrice, stock）
     */
    List<Map<String, Object>> posProductSearch(String keyword);
}
