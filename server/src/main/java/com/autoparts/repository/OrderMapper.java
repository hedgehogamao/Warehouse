package com.autoparts.repository;

import com.autoparts.model.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 订单数据访问层
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 获取指定日期的最大订单编号
     * @param datePrefix 日期前缀，如 "20260607"
     * @return 最大订单编号，如 "SO202606070003"
     */
    @Select("SELECT order_no FROM orders WHERE order_no LIKE CONCAT('SO', #{datePrefix}, '%') ORDER BY order_no DESC LIMIT 1")
    String getMaxOrderNo(@Param("datePrefix") String datePrefix);
}
