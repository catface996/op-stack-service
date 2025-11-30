package com.catface996.aiops.domain.model.resource;

/**
 * 操作类型枚举
 *
 * 用于审计日志记录资源操作类型
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public enum OperationType {
    /**
     * 创建
     */
    CREATE("创建"),

    /**
     * 更新
     */
    UPDATE("更新"),

    /**
     * 删除
     */
    DELETE("删除"),

    /**
     * 状态变更
     */
    STATUS_CHANGE("状态变更");

    private final String description;

    OperationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
