package com.autoparts.repository;

import com.autoparts.model.OrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细数据访问层
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
