package com.catface996.aiops.repository.node;

import com.catface996.aiops.domain.model.node.NodeAgentRelation;

import java.util.List;
import java.util.Optional;

/**
 * Node-Agent 关联仓储接口
 *
 * <p>提供 Node-Agent 关联关系的持久化操作</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: 系统必须支持将 Agent 与 ResourceNode 建立多对多的关联关系</li>
 *   <li>FR-004: 系统必须提供查询接口，支持根据节点 ID 查询关联的 Agent 列表</li>
 *   <li>FR-005: 系统必须提供查询接口，支持根据 Agent ID 查询关联的节点列表</li>
 *   <li>FR-006: 系统必须防止重复绑定</li>
 *   <li>FR-007: 系统必须对绑定记录支持软删除</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public interface NodeAgentRelationRepository {

    /**
     * 保存关联关系
     *
     * @param relation 关联关系
     */
    void save(NodeAgentRelation relation);

    /**
     * 根据 nodeId 和 agentId 查找关联（未删除）
     *
     * @param nodeId  资源节点 ID
     * @param agentId Agent ID
     * @return 关联关系（如果存在）
     */
    Optional<NodeAgentRelation> findByNodeIdAndAgentId(Long nodeId, Long agentId);

    /**
     * 根据 nodeId 查询所有关联的 agentId 列表（未删除）
     *
     * @param nodeId 资源节点 ID
     * @return Agent ID 列表
     */
    List<Long> findAgentIdsByNodeId(Long nodeId);

    /**
     * 根据 agentId 查询所有关联的 nodeId 列表（未删除）
     *
     * @param agentId Agent ID
     * @return Node ID 列表
     */
    List<Long> findNodeIdsByAgentId(Long agentId);

    /**
     * 软删除关联关系
     *
     * @param id 关联记录 ID
     */
    void softDelete(Long id);

    /**
     * 根据 nodeId 软删除所有关联（级联删除）
     *
     * @param nodeId 资源节点 ID
     */
    void softDeleteByNodeId(Long nodeId);

    /**
     * 根据 agentId 软删除所有关联（级联删除）
     *
     * @param agentId Agent ID
     */
    void softDeleteByAgentId(Long agentId);

    /**
     * 检查关联是否存在（未删除）
     *
     * @param nodeId  资源节点 ID
     * @param agentId Agent ID
     * @return true 如果关联存在
     */
    boolean existsByNodeIdAndAgentId(Long nodeId, Long agentId);
}
