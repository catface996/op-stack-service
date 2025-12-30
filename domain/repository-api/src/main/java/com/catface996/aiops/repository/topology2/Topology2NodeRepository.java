package com.catface996.aiops.repository.topology2;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拓扑图-节点关联仓储接口
 *
 * <p>提供拓扑图与节点关联关系的数据访问操作。</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public interface Topology2NodeRepository {

    /**
     * 添加成员关系
     *
     * @param topologyId 拓扑图ID
     * @param nodeId     节点ID
     * @param addedBy    添加者ID
     */
    void addMember(Long topologyId, Long nodeId, Long addedBy);

    /**
     * 批量添加成员关系
     *
     * @param topologyId 拓扑图ID
     * @param nodeIds    节点ID列表
     * @param addedBy    添加者ID
     */
    void addMembers(Long topologyId, List<Long> nodeIds, Long addedBy);

    /**
     * 移除成员关系
     *
     * @param topologyId 拓扑图ID
     * @param nodeId     节点ID
     */
    void removeMember(Long topologyId, Long nodeId);

    /**
     * 批量移除成员关系
     *
     * @param topologyId 拓扑图ID
     * @param nodeIds    节点ID列表
     */
    void removeMembers(Long topologyId, List<Long> nodeIds);

    /**
     * 删除拓扑图的所有成员关系
     *
     * @param topologyId 拓扑图ID
     */
    void removeAllByTopologyId(Long topologyId);

    /**
     * 删除节点相关的所有拓扑图关联
     *
     * @param nodeId 节点ID
     */
    void removeAllByNodeId(Long nodeId);

    /**
     * 查询拓扑图的成员ID列表
     *
     * @param topologyId 拓扑图ID
     * @return 节点ID列表
     */
    List<Long> findNodeIdsByTopologyId(Long topologyId);

    /**
     * 检查成员关系是否存在
     *
     * @param topologyId 拓扑图ID
     * @param nodeId     节点ID
     * @return true if member relationship exists
     */
    boolean existsByTopologyIdAndNodeId(Long topologyId, Long nodeId);

    /**
     * 统计拓扑图的成员数量
     *
     * @param topologyId 拓扑图ID
     * @return 成员数量
     */
    int countByTopologyId(Long topologyId);

    /**
     * 成员关联信息
     */
    record MemberInfo(
            Long id,
            Long topologyId,
            Long nodeId,
            String nodeName,
            String nodeTypeCode,
            String nodeTypeName,
            String nodeStatus,
            String nodeLayer,
            Integer positionX,
            Integer positionY,
            LocalDateTime addedAt,
            Long addedBy
    ) {}

    /**
     * 查询拓扑图的成员详情列表
     *
     * @param topologyId 拓扑图ID
     * @return 成员详情列表
     */
    List<MemberInfo> findMembersByTopologyId(Long topologyId);
}
