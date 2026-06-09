package com.autoparts.repository;

import com.autoparts.model.OperationLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志数据访问层
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
