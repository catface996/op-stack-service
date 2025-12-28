package com.catface996.aiops.domain.model.agent;

/**
 * Agent 状态枚举
 *
 * <p>状态是按 Team 区分的，存储在 agent_2_team 关联表中。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public enum AgentStatus {

    /**
     * 空闲，可接受新任务
     */
    IDLE("空闲"),

    /**
     * 思考中/推理中
     */
    THINKING("思考中"),

    /**
     * 执行任务中
     */
    WORKING("工作中"),

    /**
     * 任务执行完成
     */
    COMPLETED("已完成"),

    /**
     * 等待依赖项
     */
    WAITING("等待中"),

    /**
     * 执行出错
     */
    ERROR("错误");

    private final String description;

    AgentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据名称获取枚举值（忽略大小写）
     *
     * @param name 枚举名称
     * @return 枚举值，如果不存在返回 null
     */
    public static AgentStatus fromName(String name) {
        if (name == null) {
            return null;
        }
        for (AgentStatus status : values()) {
            if (status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为工作状态（不可更新/删除）
     *
     * @return true 如果状态为 WORKING 或 THINKING
     */
    public boolean isBusy() {
        return this == WORKING || this == THINKING;
    }
}
