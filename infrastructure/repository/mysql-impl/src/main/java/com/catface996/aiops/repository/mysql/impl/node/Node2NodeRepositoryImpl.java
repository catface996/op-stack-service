package com.catface996.aiops.repository.mysql.impl.node;

import com.catface996.aiops.repository.mysql.mapper.node.Node2NodeMapper;
import com.catface996.aiops.repository.mysql.po.node.Node2NodePO;
import com.catface996.aiops.repository.node.Node2NodeRepository;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 节点关系仓储实现
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Repository
public class Node2NodeRepositoryImpl implements Node2NodeRepository {

    private final Node2NodeMapper node2NodeMapper;

    public Node2NodeRepositoryImpl(Node2NodeMapper node2NodeMapper) {
        this.node2NodeMapper = node2NodeMapper;
    }

    @Override
    public List<RelationshipInfo> findRelationshipsByNodeIds(List<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Node2NodePO> pos = node2NodeMapper.selectByNodeIds(nodeIds);
        return pos.stream()
                .map(this::toRelationshipInfo)
                .collect(Collectors.toList());
    }

    @Override
    public List<RelationshipInfo> findOutgoingBySourceId(Long sourceId) {
        List<Node2NodePO> pos = node2NodeMapper.selectOutgoingBySourceId(sourceId);
        return pos.stream()
                .map(this::toRelationshipInfo)
                .collect(Collectors.toList());
    }

    @Override
    public List<RelationshipInfo> findIncomingByTargetId(Long targetId) {
        List<Node2NodePO> pos = node2NodeMapper.selectIncomingByTargetId(targetId);
        return pos.stream()
                .map(this::toRelationshipInfo)
                .collect(Collectors.toList());
    }

    @Override
    public int deleteByNodeId(Long nodeId) {
        return node2NodeMapper.deleteByNodeId(nodeId);
    }

    private RelationshipInfo toRelationshipInfo(Node2NodePO po) {
        return new RelationshipInfo(
                po.getId(),
                po.getSourceId(),
                po.getTargetId(),
                po.getRelationshipType(),
                po.getDirection(),
                po.getStrength(),
                po.getStatus(),
                po.getDescription()
        );
    }
}
