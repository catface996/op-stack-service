package com.catface996.aiops.repository.mysql.po.resource;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资源审计日志持久化对象
 *
 * <p>数据库表 resource_audit_log 的映射对象</p>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Data
@TableName("resource_audit_log")
public class ResourceAuditLogPO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审计日志ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 资源ID
     */
    @TableField("resource_id")
    private Long resourceId;

    /**
     * 操作类型（CREATE, UPDATE, DELETE, STATUS_CHANGE）
     */
    @TableField("operation")
    private String operation;

    /**
     * 操作人ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 操作人用户名
     */
    @TableField("operator_name")
    private String operatorName;

    /**
     * 变更前数据（JSON格式）
     */
    @TableField("old_value")
    private String oldValue;

    /**
     * 变更后数据（JSON格式）
     */
    @TableField("new_value")
    private String newValue;

    /**
     * 操作描述/备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
