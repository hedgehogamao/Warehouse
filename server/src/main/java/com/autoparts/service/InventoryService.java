package com.autoparts.service;

import com.autoparts.model.InventoryLog;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * 库存业务逻辑接口
 */
public interface InventoryService {

    /**
     * 库存列表（分页 + 搜索 + 筛选 + 仅显示库存不足）
     */
    IPage<Map<String, Object>> pageInventory(int page, int size,
                                               String keyword, String category,
                                               Boolean lowStock);

    /**
     * 库存预警列表（stock <= minStock 且 status=1）
     */
    List<Map<String, Object>> getLowStockProducts();

    /**
     * 商品库存流水（分页）
     */
    IPage<InventoryLog> pageInventoryLogs(int page, int size,
                                            Integer productId,
                                            String startDate, String endDate);

    /**
     * 库存调整（盘盈盘亏）
     */
    void adjustStock(Integer productId, Integer quantity, String reason, Integer operatorId);
}
