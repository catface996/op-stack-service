package com.catface996.aiops.domain.model.node;

/**
 * 资源节点状态枚举
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public enum NodeStatus {
    /**
     * 运行中
     */
    RUNNING("运行中"),

    /**
     * 已停止
     */
    STOPPED("已停止"),

    /**
     * 维护中
     */
    MAINTENANCE("维护中"),

    /**
     * 已下线
     */
    OFFLINE("已下线");

    private final String description;

    NodeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 判断节点是否可以被修改
     */
    public boolean canModify() {
        return this == RUNNING || this == STOPPED;
    }

    /**
     * 判断节点是否可以被删除
     */
    public boolean canDelete() {
        return this == STOPPED || this == OFFLINE;
    }
}
