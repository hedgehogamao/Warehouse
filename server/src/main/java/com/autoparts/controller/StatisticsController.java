package com.autoparts.controller;

import com.autoparts.common.Result;
import com.autoparts.service.StatisticsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据统计接口
 */
@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 今日概况
     */
    @GetMapping("/today")
    public Result<Map<String, Object>> getTodaySummary() {
        return Result.success(statisticsService.getTodaySummary());
    }

    /**
     * 销售趋势（近 N 天）
     */
    @GetMapping("/sales-trend")
    public Result<Map<String, Object>> getSalesTrend(
            @RequestParam(defaultValue = "7") int days) {
        return Result.success(statisticsService.getSalesTrend(days));
    }

    /**
     * 商品销售排行
     */
    @GetMapping("/top-products")
    public Result<List<Map<String, Object>>> getTopProducts(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(statisticsService.getTopProducts(days, limit));
    }

    /**
     * 分类占比
     */
    @GetMapping("/category-distribution")
    public Result<List<Map<String, Object>>> getCategoryDistribution(
            @RequestParam(defaultValue = "30") int days) {
        return Result.success(statisticsService.getCategoryDistribution(days));
    }
}
