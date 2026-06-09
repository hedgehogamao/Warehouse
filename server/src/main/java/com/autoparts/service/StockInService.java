package com.autoparts.service;

import com.autoparts.model.StockInRecord;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Map;

/**
 * 入库业务逻辑接口
 */
public interface StockInService {

    /**
     * 新建入库（事务：插入记录 + 更新商品库存 + 写库存流水）
     * @return 入库记录及库存变化信息
     */
    Map<String, Object> createStockIn(Integer productId, Integer quantity,
                                       java.math.BigDecimal costPrice, String remark,
                                       Integer operatorId);

    /**
     * 入库记录列表（分页 + 筛选）
     */
    IPage<Map<String, Object>> pageStockInRecords(int page, int size,
                                                    Integer productId,
                                                    String startDate, String endDate);

    /**
     * 入库单详情
     */
    Map<String, Object> getStockInDetail(Integer id);
}
