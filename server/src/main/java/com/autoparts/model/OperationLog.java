package com.autoparts.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 * 对应数据库 operation_logs 表
 */
@TableName("operation_logs")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 模块：AUTH/PRODUCT/STOCK_IN/ORDER/INVENTORY */
    private String module;

    /** 操作类型 */
    private String action;

    /** 操作内容描述 */
    private String content;

    /** 关联记录 ID */
    private Integer refId;

    /** 操作人 ID */
    private Integer operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 创建时间 */
    private LocalDateTime createdAt;

    public OperationLog() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRefId() { return refId; }
    public void setRefId(Integer refId) { this.refId = refId; }

    public Integer getOperatorId() { return operatorId; }
    public void setOperatorId(Integer operatorId) { this.operatorId = operatorId; }

    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
