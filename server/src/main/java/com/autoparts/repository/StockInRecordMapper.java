package com.autoparts.repository;

import com.autoparts.model.StockInRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库记录数据访问层
 */
@Mapper
public interface StockInRecordMapper extends BaseMapper<StockInRecord> {
}
