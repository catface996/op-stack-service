package com.catface996.aiops.domain.model.agentbound;

import com.catface996.aiops.domain.model.agent.AgentHierarchyLevel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent 绑定关系领域实体
 *
 * <p>表示 Agent 与各类实体（Topology、Node）的绑定关系。</p>
 *
 * <p>业务规则：</p>
 * <ul>
 *   <li>每个 Topology 只能绑定一个 GLOBAL_SUPERVISOR</li>
 *   <li>每个 Node 只能绑定一个 TEAM_SUPERVISOR</li>
 *   <li>每个 Node 可以绑定多个 TEAM_WORKER</li>
 *   <li>绑定的 Agent 层级必须与 hierarchy_level 匹配</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AgentBound {

    /**
     * 绑定记录 ID
     */
    private Long id;

    /**
     * Agent ID
     */
    private Long agentId;

    /**
     * 层级（GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, TEAM_WORKER）
     */
    private AgentHierarchyLevel hierarchyLevel;

    /**
     * 绑定实体 ID
     */
    private Long entityId;

    /**
     * 实体类型（TOPOLOGY, NODE）
     */
    private BoundEntityType entityType;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 软删除标记
     */
    private Boolean deleted;

    // ==================== 派生字段（用于 JOIN 查询结果）====================

    /**
     * Agent 名称（JOIN 查询填充）
     */
    private String agentName;

    /**
     * Agent 角色（JOIN 查询填充）
     */
    private String agentRole;

    /**
     * 实体名称（JOIN 查询填充）
     */
    private String entityName;

    /**
     * 工厂方法：创建新的绑定关系
     *
     * @param agentId        Agent ID
     * @param hierarchyLevel 层级
     * @param entityId       实体 ID
     * @param entityType     实体类型
     * @return 新的绑定关系
     */
    public static AgentBound create(Long agentId, AgentHierarchyLevel hierarchyLevel,
                                     Long entityId, BoundEntityType entityType) {
        return AgentBound.builder()
                .agentId(agentId)
                .hierarchyLevel(hierarchyLevel)
                .entityId(entityId)
                .entityType(entityType)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    /**
     * 检查是否为 Global Supervisor 绑定
     */
    public boolean isGlobalSupervisorBinding() {
        return hierarchyLevel == AgentHierarchyLevel.GLOBAL_SUPERVISOR;
    }

    /**
     * 检查是否为 Team Supervisor 绑定
     */
    public boolean isTeamSupervisorBinding() {
        return hierarchyLevel == AgentHierarchyLevel.TEAM_SUPERVISOR;
    }

    /**
     * 检查是否为 Worker 绑定
     */
    public boolean isWorkerBinding() {
        return hierarchyLevel == AgentHierarchyLevel.TEAM_WORKER;
    }

    /**
     * 检查是否为 Supervisor 绑定（Global 或 Team）
     */
    public boolean isSupervisorBinding() {
        return hierarchyLevel != null && hierarchyLevel.isSupervisor();
    }

    /**
     * 标记为已删除（软删除）
     */
    public void markDeleted() {
        this.deleted = true;
    }

    /**
     * 设置派生字段（用于 DTO 转换）
     */
    public void setDerivedFields(String agentName, String agentRole, String entityName) {
        this.agentName = agentName;
        this.agentRole = agentRole;
        this.entityName = entityName;
    }
}
