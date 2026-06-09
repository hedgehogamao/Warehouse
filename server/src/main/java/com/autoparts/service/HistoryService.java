package com.autoparts.service;

import com.autoparts.model.InventoryLog;
import com.autoparts.model.OperationLog;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 历史记录业务逻辑接口
 */
public interface HistoryService {

    /**
     * 库存流水查询（分页 + 多条件筛选）
     */
    IPage<InventoryLog> pageInventoryLogs(int page, int size,
                                            String type, Integer productId,
                                            String productName,
                                            String startDate, String endDate);

    /**
     * 操作日志查询（分页 + 多条件筛选）
     */
    IPage<OperationLog> pageOperationLogs(int page, int size,
                                            String module, Integer operatorId,
                                            String startDate, String endDate);
}
