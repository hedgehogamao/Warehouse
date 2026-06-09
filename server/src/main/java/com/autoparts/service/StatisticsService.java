package com.autoparts.service;

import java.util.List;
import java.util.Map;

/**
 * 数据统计业务逻辑接口
 */
public interface StatisticsService {

    /**
     * 今日概况
     */
    Map<String, Object> getTodaySummary();

    /**
     * 销售趋势（近 N 天）
     */
    Map<String, Object> getSalesTrend(int days);

    /**
     * 商品销售排行
     */
    List<Map<String, Object>> getTopProducts(int days, int limit);

    /**
     * 分类占比
     */
    List<Map<String, Object>> getCategoryDistribution(int days);
}
