package com.autoparts.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 库存流水实体
 * 对应数据库 inventory_logs 表
 */
@TableName("inventory_logs")
public class InventoryLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 商品 ID */
    private Integer productId;

    /** 类型：STOCK_IN-入库，SALE-销售，ADJUST-调整 */
    private String type;

    /** 变动数量（正数增加，负数减少） */
    private Integer quantity;

    /** 变动前库存 */
    private Integer beforeStock;

    /** 变动后库存 */
    private Integer afterStock;

    /** 关联单据 ID（入库单 ID 或订单 ID） */
    private Integer refId;

    /** 备注 */
    private String remark;

    /** 操作人 ID */
    private Integer operatorId;

    /** 创建时间 */
    private LocalDateTime createdAt;

    public InventoryLog() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getBeforeStock() { return beforeStock; }
    public void setBeforeStock(Integer beforeStock) { this.beforeStock = beforeStock; }

    public Integer getAfterStock() { return afterStock; }
    public void setAfterStock(Integer afterStock) { this.afterStock = afterStock; }

    public Integer getRefId() { return refId; }
    public void setRefId(Integer refId) { this.refId = refId; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Integer getOperatorId() { return operatorId; }
    public void setOperatorId(Integer operatorId) { this.operatorId = operatorId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
