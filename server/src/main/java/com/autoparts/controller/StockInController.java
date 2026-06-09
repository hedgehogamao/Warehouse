package com.autoparts.controller;

import com.autoparts.common.Result;
import com.autoparts.service.StockInService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 入库管理接口
 */
@RestController
@RequestMapping("/api/v1/stock-in")
public class StockInController {

    private final StockInService stockInService;

    public StockInController(StockInService stockInService) {
        this.stockInService = stockInService;
    }

    /**
     * 新建入库
     */
    @PostMapping
    public Result<Map<String, Object>> createStockIn(@RequestBody Map<String, Object> body,
                                                      Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        Integer productId = (Integer) body.get("productId");
        Integer quantity = (Integer) body.get("quantity");
        BigDecimal costPrice = body.get("costPrice") != null
                ? new BigDecimal(body.get("costPrice").toString()) : null;
        String remark = (String) body.get("remark");

        Map<String, Object> result = stockInService.createStockIn(
                productId, quantity, costPrice, remark, userId);
        return Result.success(result);
    }

    /**
     * 入库记录列表
     */
    @GetMapping
    public Result<IPage<Map<String, Object>>> listStockInRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<Map<String, Object>> result = stockInService.pageStockInRecords(
                page, size, productId, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 入库单详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getStockInDetail(@PathVariable Integer id) {
        Map<String, Object> result = stockInService.getStockInDetail(id);
        return Result.success(result);
    }
}
