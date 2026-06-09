package com.autoparts.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入库记录实体
 * 对应数据库 stock_in 表
 */
@TableName("stock_in")
public class StockInRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 商品 ID */
    private Integer productId;

    /** 入库数量 */
    private Integer quantity;

    /** 入库成本价 */
    private BigDecimal costPrice;

    /** 备注 */
    private String remark;

    /** 操作人 ID */
    private Integer operatorId;

    /** 创建时间 */
    private LocalDateTime createdAt;

    public StockInRecord() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Integer getOperatorId() { return operatorId; }
    public void setOperatorId(Integer operatorId) { this.operatorId = operatorId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
