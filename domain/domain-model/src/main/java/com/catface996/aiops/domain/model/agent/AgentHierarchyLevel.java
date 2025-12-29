package com.catface996.aiops.domain.model.agent;

/**
 * Agent 层级枚举
 *
 * <p>定义 Agent 在团队中的层级位置</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
public enum AgentHierarchyLevel {

    /**
     * 全局监管者 - 最高层级，负责全局协调和监管
     */
    GLOBAL_SUPERVISOR("全局监管者", 1),

    /**
     * 团队监管者 - 中间层级，负责团队协调和管理
     */
    TEAM_SUPERVISOR("团队监管者", 2),

    /**
     * 团队工作者 - 基础层级，负责具体任务执行
     */
    TEAM_WORKER("团队工作者", 3);

    private final String description;
    private final int level;

    AgentHierarchyLevel(String description, int level) {
        this.description = description;
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 获取层级数值（数字越小层级越高）
     */
    public int getLevel() {
        return level;
    }

    /**
     * 根据名称获取枚举值（忽略大小写）
     *
     * @param name 枚举名称
     * @return 枚举值，如果不存在返回 null
     */
    public static AgentHierarchyLevel fromName(String name) {
        if (name == null) {
            return null;
        }
        for (AgentHierarchyLevel level : values()) {
            if (level.name().equalsIgnoreCase(name)) {
                return level;
            }
        }
        return null;
    }

    /**
     * 检查是否为监管者（Global 或 Team）
     */
    public boolean isSupervisor() {
        return this == GLOBAL_SUPERVISOR || this == TEAM_SUPERVISOR;
    }
}
