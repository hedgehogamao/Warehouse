package com.autoparts.controller;

import com.autoparts.common.Result;
import com.autoparts.model.InventoryLog;
import com.autoparts.model.OperationLog;
import com.autoparts.service.HistoryService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.*;

/**
 * 历史记录接口
 */
@RestController
@RequestMapping("/api/v1")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    /**
     * 库存流水查询
     */
    @GetMapping("/inventory-logs")
    public Result<IPage<InventoryLog>> listInventoryLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<InventoryLog> result = historyService.pageInventoryLogs(
                page, size, type, productId, productName, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 操作日志查询
     */
    @GetMapping("/operation-logs")
    public Result<IPage<OperationLog>> listOperationLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer operatorId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        IPage<OperationLog> result = historyService.pageOperationLogs(
                page, size, module, operatorId, startDate, endDate);
        return Result.success(result);
    }
}
