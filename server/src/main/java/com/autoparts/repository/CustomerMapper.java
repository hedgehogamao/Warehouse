package com.autoparts.repository;

import com.autoparts.model.Customer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户数据访问层
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
