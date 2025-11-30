package com.catface996.aiops.domain.model.resource;

/**
 * 资源状态枚举
 *
 * 定义资源的生命周期状态
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public enum ResourceStatus {
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

    ResourceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 判断资源是否可以被修改
     * 只有运行中和已停止状态的资源可以被修改
     */
    public boolean canModify() {
        return this == RUNNING || this == STOPPED;
    }

    /**
     * 判断资源是否可以被删除
     * 只有已停止和已下线状态的资源可以被删除
     */
    public boolean canDelete() {
        return this == STOPPED || this == OFFLINE;
    }
}
