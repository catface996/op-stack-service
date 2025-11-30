package com.catface996.aiops.domain.model.resource;

import java.time.LocalDateTime;

/**
 * 资源审计日志实体
 *
 * 记录资源的操作历史
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public class ResourceAuditLog {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 操作类型：CREATE, UPDATE, DELETE, STATUS_CHANGE
     */
    private OperationType operation;

    /**
     * 旧值（JSON格式）
     */
    private String oldValue;

    /**
     * 新值（JSON格式）
     */
    private String newValue;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作时间
     */
    private LocalDateTime createdAt;

    // 构造函数
    public ResourceAuditLog() {
    }

    public ResourceAuditLog(Long id, Long resourceId, OperationType operation,
                            String oldValue, String newValue,
                            Long operatorId, String operatorName, LocalDateTime createdAt) {
        this.id = id;
        this.resourceId = resourceId;
        this.operation = operation;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.operatorId = operatorId;
        this.operatorName = operatorName;
        this.createdAt = createdAt;
    }

    /**
     * 创建审计日志的工厂方法
     */
    public static ResourceAuditLog create(Long resourceId, OperationType operation,
                                          String oldValue, String newValue,
                                          Long operatorId, String operatorName) {
        ResourceAuditLog log = new ResourceAuditLog();
        log.setResourceId(resourceId);
        log.setOperation(operation);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ResourceAuditLog{" +
                "id=" + id +
                ", resourceId=" + resourceId +
                ", operation=" + operation +
                ", operatorId=" + operatorId +
                ", operatorName='" + operatorName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
