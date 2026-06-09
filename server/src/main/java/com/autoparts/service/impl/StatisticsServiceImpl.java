package com.autoparts.service.impl;

import com.autoparts.repository.InventoryLogMapper;
import com.autoparts.repository.OperationLogMapper;
import com.autoparts.repository.OrderItemMapper;
import com.autoparts.repository.OrderMapper;
import com.autoparts.repository.ProductMapper;
import com.autoparts.service.StatisticsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据统计业务逻辑实现
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final InventoryLogMapper inventoryLogMapper;

    public StatisticsServiceImpl(OrderMapper orderMapper,
                                  OrderItemMapper orderItemMapper,
                                  ProductMapper productMapper,
                                  InventoryLogMapper inventoryLogMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.inventoryLogMapper = inventoryLogMapper;
    }

    @Override
    public Map<String, Object> getTodaySummary() {
        Map<String, Object> data = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.atTime(LocalTime.MAX);

        // 今日销售额和订单数
        List<com.autoparts.model.Order> todayOrders = orderMapper.selectList(
                new LambdaQueryWrapper<com.autoparts.model.Order>()
                        .ge(com.autoparts.model.Order::getCreatedAt, dayStart)
                        .le(com.autoparts.model.Order::getCreatedAt, dayEnd)
                        .eq(com.autoparts.model.Order::getStatus, "PAID"));

        BigDecimal todaySalesAmount = todayOrders.stream()
                .map(com.autoparts.model.Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("todaySalesAmount", todaySalesAmount);
        data.put("todayOrderCount", todayOrders.size());

        // 今日入库次数
        Long todayStockInCount = inventoryLogMapper.selectCount(
                new LambdaQueryWrapper<com.autoparts.model.InventoryLog>()
                        .ge(com.autoparts.model.InventoryLog::getCreatedAt, dayStart)
                        .le(com.autoparts.model.InventoryLog::getCreatedAt, dayEnd)
                        .eq(com.autoparts.model.InventoryLog::getType, "STOCK_IN"));
        data.put("todayStockInCount", todayStockInCount);

        // 库存预警数
        Long lowStockCount = productMapper.selectCount(
                new LambdaQueryWrapper<com.autoparts.model.Product>()
                        .eq(com.autoparts.model.Product::getStatus, 1)
                        .and(w -> w.eq(com.autoparts.model.Product::getStock, 0)
                                .or().apply("stock <= min_stock AND min_stock > 0")));
        data.put("lowStockCount", lowStockCount);

        // 缺货数
        Long outOfStockCount = productMapper.selectCount(
                new LambdaQueryWrapper<com.autoparts.model.Product>()
                        .eq(com.autoparts.model.Product::getStatus, 1)
                        .eq(com.autoparts.model.Product::getStock, 0));
        data.put("outOfStockCount", outOfStockCount);

        // 商品总数
        Long totalProducts = productMapper.selectCount(
                new LambdaQueryWrapper<com.autoparts.model.Product>()
                        .eq(com.autoparts.model.Product::getStatus, 1));
        data.put("totalProducts", totalProducts);

        // 订单总数
        Long totalOrders = orderMapper.selectCount(null);
        data.put("totalOrders", totalOrders);

        return data;
    }

    @Override
    public Map<String, Object> getSalesTrend(int days) {
        Map<String, Object> data = new LinkedHashMap<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<String> dayList = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        // 查询区间内所有已支付订单
        List<com.autoparts.model.Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<com.autoparts.model.Order>()
                        .ge(com.autoparts.model.Order::getCreatedAt, startDate.atStartOfDay())
                        .le(com.autoparts.model.Order::getCreatedAt, endDate.atTime(LocalTime.MAX))
                        .eq(com.autoparts.model.Order::getStatus, "PAID"));

        // 按日期分组
        Map<LocalDate, List<com.autoparts.model.Order>> grouped = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            dayList.add(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            List<com.autoparts.model.Order> dayOrders = grouped.getOrDefault(date, List.of());
            amounts.add(dayOrders.stream()
                    .map(com.autoparts.model.Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            counts.add(dayOrders.size());
        }

        data.put("days", dayList);
        data.put("amounts", amounts);
        data.put("counts", counts);
        return data;
    }

    @Override
    public List<Map<String, Object>> getTopProducts(int days, int limit) {
        LocalDateTime startDate = LocalDate.now().minusDays(days).atStartOfDay();

        // 使用 MyBatis-Plus 的自定义 SQL 或手动聚合
        // 简化实现：查询 order_items 按 product_id 分组
        List<com.autoparts.model.Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<com.autoparts.model.Order>()
                        .ge(com.autoparts.model.Order::getCreatedAt, startDate)
                        .eq(com.autoparts.model.Order::getStatus, "PAID"));

        if (orders.isEmpty()) return List.of();

        List<Integer> orderIds = orders.stream().map(com.autoparts.model.Order::getId).toList();
        List<com.autoparts.model.OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<com.autoparts.model.OrderItem>()
                        .in(com.autoparts.model.OrderItem::getOrderId, orderIds));

        // 按商品聚合
        Map<Integer, Map<String, Object>> aggregated = new LinkedHashMap<>();
        for (com.autoparts.model.OrderItem item : items) {
            aggregated.computeIfAbsent(item.getProductId(), k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("productId", item.getProductId());
                m.put("productName", item.getProductName());
                m.put("totalQuantity", 0);
                m.put("totalAmount", BigDecimal.ZERO);
                return m;
            });
            Map<String, Object> m = aggregated.get(item.getProductId());
            m.put("totalQuantity", (int) m.get("totalQuantity") + item.getQuantity());
            m.put("totalAmount", ((BigDecimal) m.get("totalAmount")).add(item.getSubtotal()));
        }

        // 排序并截取
        return aggregated.values().stream()
                .sorted((a, b) -> ((BigDecimal) b.get("totalAmount")).compareTo((BigDecimal) a.get("totalAmount")))
                .limit(limit)
                .peek(m -> {
                    // 补充分类信息
                    com.autoparts.model.Product p = productMapper.selectById((int) m.get("productId"));
                    if (p != null) m.put("category", p.getCategory());
                })
                .toList();
    }

    @Override
    public List<Map<String, Object>> getCategoryDistribution(int days) {
        LocalDateTime startDate = LocalDate.now().minusDays(days).atStartOfDay();

        List<com.autoparts.model.Order> orders = orderMapper.selectList(
                new LambdaQueryWrapper<com.autoparts.model.Order>()
                        .ge(com.autoparts.model.Order::getCreatedAt, startDate)
                        .eq(com.autoparts.model.Order::getStatus, "PAID"));

        if (orders.isEmpty()) return List.of();

        List<Integer> orderIds = orders.stream().map(com.autoparts.model.Order::getId).toList();
        List<com.autoparts.model.OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<com.autoparts.model.OrderItem>()
                        .in(com.autoparts.model.OrderItem::getOrderId, orderIds));

        // 按分类聚合
        Map<String, BigDecimal> categoryAmounts = new LinkedHashMap<>();
        for (com.autoparts.model.OrderItem item : items) {
            com.autoparts.model.Product p = productMapper.selectById(item.getProductId());
            String category = (p != null && p.getCategory() != null) ? p.getCategory() : "其他";
            categoryAmounts.merge(category, item.getSubtotal(), BigDecimal::add);
        }

        BigDecimal totalAmount = categoryAmounts.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return categoryAmounts.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("category", entry.getKey());
                    m.put("amount", entry.getValue());
                    m.put("percentage", totalAmount.compareTo(BigDecimal.ZERO) > 0
                            ? entry.getValue().multiply(BigDecimal.valueOf(100))
                                    .divide(totalAmount, 1, java.math.RoundingMode.HALF_UP)
                            : BigDecimal.ZERO);
                    return m;
                })
                .sorted((a, b) -> ((BigDecimal) b.get("amount")).compareTo((BigDecimal) a.get("amount")))
                .toList();
    }
}
