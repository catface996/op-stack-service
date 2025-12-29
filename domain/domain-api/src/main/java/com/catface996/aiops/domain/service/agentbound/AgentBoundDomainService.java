package com.catface996.aiops.domain.service.agentbound;

import com.catface996.aiops.domain.model.agent.AgentHierarchyLevel;
import com.catface996.aiops.domain.model.agentbound.AgentBound;
import com.catface996.aiops.domain.model.agentbound.BoundEntityType;

import java.util.List;
import java.util.Optional;

/**
 * Agent 绑定关系领域服务接口
 *
 * <p>提供 Agent 与实体（Topology、Node）绑定关系的核心业务逻辑，包括：</p>
 * <ul>
 *   <li>绑定创建：验证实体类型与层级匹配、替换已有 Supervisor 绑定</li>
 *   <li>绑定查询：按实体查询、按 Agent 查询、层级结构查询</li>
 *   <li>绑定删除：软删除</li>
 * </ul>
 *
 * <p>业务规则：</p>
 * <ul>
 *   <li>BR-001: GLOBAL_SUPERVISOR 只能绑定到 TOPOLOGY</li>
 *   <li>BR-002: TEAM_SUPERVISOR 和 TEAM_WORKER 只能绑定到 NODE</li>
 *   <li>BR-003: Supervisor 绑定采用替换策略（同一实体只能有一个）</li>
 *   <li>BR-004: Worker 绑定采用追加策略（同一实体可以有多个）</li>
 *   <li>BR-005: 同一 Agent 不能重复绑定到同一实体</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
public interface AgentBoundDomainService {

    /**
     * 绑定 Agent 到实体
     *
     * <p>业务规则：</p>
     * <ul>
     *   <li>验证 entityType 与 hierarchyLevel 匹配</li>
     *   <li>Supervisor 绑定：如已存在则替换（软删除旧绑定）</li>
     *   <li>Worker 绑定：允许多个，但不允许重复</li>
     * </ul>
     *
     * @param agentId        Agent ID
     * @param hierarchyLevel Agent 层级
     * @param entityId       实体 ID
     * @param entityType     实体类型
     * @return 创建的绑定关系
     * @throws IllegalArgumentException 如果 entityType 与 hierarchyLevel 不匹配
     */
    AgentBound bindAgent(Long agentId, AgentHierarchyLevel hierarchyLevel,
                         Long entityId, BoundEntityType entityType);

    /**
     * 按实体查询绑定
     *
     * @param entityType     实体类型
     * @param entityId       实体 ID
     * @param hierarchyLevel 层级过滤（可选，为 null 返回所有层级）
     * @return 绑定列表
     */
    List<AgentBound> findByEntity(BoundEntityType entityType, Long entityId,
                                   AgentHierarchyLevel hierarchyLevel);

    /**
     * 按 Agent 查询绑定
     *
     * @param agentId    Agent ID
     * @param entityType 实体类型过滤（可选，为 null 返回所有类型）
     * @return 绑定列表
     */
    List<AgentBound> findByAgentId(Long agentId, BoundEntityType entityType);

    /**
     * 查询 Topology 的层级团队结构
     *
     * <p>返回 Topology 的完整层级结构：</p>
     * <ul>
     *   <li>Topology 绑定的 Global Supervisor</li>
     *   <li>所有成员 Node 绑定的 Team Supervisor 和 Workers</li>
     * </ul>
     *
     * @param topologyId Topology ID
     * @return 所有相关绑定列表
     */
    List<AgentBound> queryHierarchyByTopology(Long topologyId);

    /**
     * 解绑 Agent
     *
     * @param agentId    Agent ID
     * @param entityId   实体 ID
     * @param entityType 实体类型
     * @return 删除的记录数
     */
    int unbind(Long agentId, Long entityId, BoundEntityType entityType);

    /**
     * 检查绑定是否存在
     *
     * @param agentId    Agent ID
     * @param entityId   实体 ID
     * @param entityType 实体类型
     * @return 是否存在
     */
    boolean existsBinding(Long agentId, Long entityId, BoundEntityType entityType);

    /**
     * 查询指定实体的 Supervisor 绑定
     *
     * @param entityType     实体类型
     * @param entityId       实体 ID
     * @param hierarchyLevel 层级（GLOBAL_SUPERVISOR 或 TEAM_SUPERVISOR）
     * @return 绑定实体，不存在返回 empty
     */
    Optional<AgentBound> findSupervisorBinding(BoundEntityType entityType, Long entityId,
                                                AgentHierarchyLevel hierarchyLevel);
}
