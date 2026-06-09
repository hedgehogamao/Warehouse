package com.autoparts.repository;

import com.autoparts.model.InventoryLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存流水数据访问层
 */
@Mapper
public interface InventoryLogMapper extends BaseMapper<InventoryLog> {
}
