package com.catface996.aiops.repository.node;

import java.util.List;

/**
 * 节点关系仓储接口
 *
 * <p>提供节点之间关系（边）的数据访问操作。</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public interface Node2NodeRepository {

    /**
     * 节点关系信息
     */
    record RelationshipInfo(
            Long id,
            Long sourceId,
            Long targetId,
            String relationshipType,
            String direction,
            String strength,
            String status,
            String description
    ) {}

    /**
     * 查询指定节点集合之间的关系
     *
     * @param nodeIds 节点ID列表
     * @return 关系列表
     */
    List<RelationshipInfo> findRelationshipsByNodeIds(List<Long> nodeIds);

    /**
     * 查询节点的出边关系
     *
     * @param sourceId 源节点ID
     * @return 关系列表
     */
    List<RelationshipInfo> findOutgoingBySourceId(Long sourceId);

    /**
     * 查询节点的入边关系
     *
     * @param targetId 目标节点ID
     * @return 关系列表
     */
    List<RelationshipInfo> findIncomingByTargetId(Long targetId);

    /**
     * 删除节点相关的所有关系
     *
     * @param nodeId 节点ID
     * @return 删除的记录数
     */
    int deleteByNodeId(Long nodeId);
}
