package com.catface996.aiops.repository.agentbound;

import com.catface996.aiops.domain.model.agent.AgentHierarchyLevel;
import com.catface996.aiops.domain.model.agentbound.AgentBound;
import com.catface996.aiops.domain.model.agentbound.BoundEntityType;

import java.util.List;
import java.util.Optional;

/**
 * Agent 绑定关系仓储接口
 *
 * <p>提供 Agent 与实体（Topology、Node）绑定关系的数据访问操作。</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
public interface AgentBoundRepository {

    /**
     * 保存绑定关系
     *
     * @param agentBound 绑定关系实体
     * @return 保存后的实体（含ID）
     */
    AgentBound save(AgentBound agentBound);

    /**
     * 根据ID查询绑定
     *
     * @param id 绑定ID
     * @return 绑定实体
     */
    Optional<AgentBound> findById(Long id);

    /**
     * 按实体查询绑定
     *
     * @param entityType     实体类型
     * @param entityId       实体ID
     * @param hierarchyLevel 层级过滤（可选，为null时返回所有层级）
     * @return 绑定列表
     */
    List<AgentBound> findByEntity(BoundEntityType entityType, Long entityId, AgentHierarchyLevel hierarchyLevel);

    /**
     * 按 Agent 查询绑定
     *
     * @param agentId    Agent ID
     * @param entityType 实体类型过滤（可选，为null时返回所有类型）
     * @return 绑定列表
     */
    List<AgentBound> findByAgentId(Long agentId, BoundEntityType entityType);

    /**
     * 查询 Topology 的层级团队结构
     *
     * @param topologyId Topology ID
     * @return 所有相关绑定（Global Supervisor + 各 Node 的 Team Supervisor 和 Workers）
     */
    List<AgentBound> findHierarchyByTopologyId(Long topologyId);

    /**
     * 查询指定实体的 Supervisor 绑定
     *
     * @param entityType     实体类型
     * @param entityId       实体ID
     * @param hierarchyLevel 层级（GLOBAL_SUPERVISOR 或 TEAM_SUPERVISOR）
     * @return 绑定实体，不存在返回 empty
     */
    Optional<AgentBound> findSupervisorBinding(BoundEntityType entityType, Long entityId, AgentHierarchyLevel hierarchyLevel);

    /**
     * 检查是否存在指定类型的绑定
     *
     * @param entityType     实体类型
     * @param entityId       实体ID
     * @param hierarchyLevel 层级
     * @return 是否存在
     */
    boolean existsByEntityAndHierarchy(BoundEntityType entityType, Long entityId, AgentHierarchyLevel hierarchyLevel);

    /**
     * 检查绑定是否已存在
     *
     * @param agentId    Agent ID
     * @param entityId   实体ID
     * @param entityType 实体类型
     * @return 是否存在
     */
    boolean existsBinding(Long agentId, Long entityId, BoundEntityType entityType);

    /**
     * 物理删除绑定
     *
     * @param agentId    Agent ID
     * @param entityId   实体ID
     * @param entityType 实体类型
     * @return 删除的记录数
     */
    int deleteBinding(Long agentId, Long entityId, BoundEntityType entityType);

    /**
     * 根据ID物理删除绑定
     *
     * @param id 绑定ID
     */
    void deleteById(Long id);
}
