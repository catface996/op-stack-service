package com.catface996.aiops.domain.model.agent;

/**
 * Agent 角色枚举
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public enum AgentRole {

    /**
     * 全局监管者（单例）
     */
    GLOBAL_SUPERVISOR("全局监管者"),

    /**
     * 团队监管者
     */
    TEAM_SUPERVISOR("团队监管者"),

    /**
     * 工作者
     */
    WORKER("工作者"),

    /**
     * 侦察者
     */
    SCOUTER("侦察者");

    private final String description;

    AgentRole(String description) {
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
    public static AgentRole fromName(String name) {
        if (name == null) {
            return null;
        }
        for (AgentRole role : values()) {
            if (role.name().equalsIgnoreCase(name)) {
                return role;
            }
        }
        return null;
    }
}
