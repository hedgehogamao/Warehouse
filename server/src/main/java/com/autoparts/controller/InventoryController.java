package com.autoparts.controller;

import com.autoparts.common.Result;
import com.autoparts.model.InventoryLog;
import com.autoparts.service.InventoryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 库存管理接口
 */
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * 库存列表（分页 + 搜索 + 筛选）
     */
    @GetMapping
    public Result<IPage<Map<String, Object>>> listInventory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean lowStock) {
        IPage<Map<String, Object>> result = inventoryService.pageInventory(
                page, size, keyword, category, lowStock);
        return Result.success(result);
    }

    /**
     * 库存预警列表
     */
    @GetMapping("/low-stock")
    public Result<List<Map<String, Object>>> getLowStock() {
        List<Map<String, Object>> result = inventoryService.getLowStockProducts();
        return Result.success(result);
    }

    /**
     * 商品库存流水
     */
    @GetMapping("/{productId}/logs")
    public Result<IPage<InventoryLog>> getInventoryLogs(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<InventoryLog> result = inventoryService.pageInventoryLogs(
                page, size, productId, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 库存调整
     */
    @PutMapping("/{productId}/adjust")
    public Result<Void> adjustStock(@PathVariable Integer productId,
                                     @RequestBody Map<String, Object> body,
                                     Authentication authentication) {
        Integer userId = (Integer) authentication.getPrincipal();
        Integer quantity = (Integer) body.get("quantity");
        String reason = (String) body.get("reason");

        inventoryService.adjustStock(productId, quantity, reason, userId);
        return Result.success(null);
    }
}
