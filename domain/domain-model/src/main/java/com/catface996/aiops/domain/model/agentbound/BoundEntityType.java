package com.catface996.aiops.domain.model.agentbound;

import com.catface996.aiops.domain.model.agent.AgentHierarchyLevel;

/**
 * 可绑定实体类型枚举
 *
 * <p>定义可以绑定 Agent 的实体类型</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
public enum BoundEntityType {

    /**
     * 拓扑图 - 可绑定 GLOBAL_SUPERVISOR
     */
    TOPOLOGY("拓扑图"),

    /**
     * 资源节点 - 可绑定 TEAM_SUPERVISOR 和 TEAM_WORKER
     */
    NODE("资源节点");

    private final String description;

    BoundEntityType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 检查此实体类型是否支持指定的层级
     *
     * @param level Agent 层级
     * @return true 如果支持
     */
    public boolean supportsHierarchyLevel(AgentHierarchyLevel level) {
        if (level == null) {
            return false;
        }
        return switch (this) {
            case TOPOLOGY -> level == AgentHierarchyLevel.GLOBAL_SUPERVISOR;
            case NODE -> level == AgentHierarchyLevel.TEAM_SUPERVISOR || level == AgentHierarchyLevel.TEAM_WORKER;
        };
    }

    /**
     * 根据名称获取枚举值（忽略大小写）
     *
     * @param name 枚举名称
     * @return 枚举值，如果不存在返回 null
     */
    public static BoundEntityType fromName(String name) {
        if (name == null) {
            return null;
        }
        for (BoundEntityType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
