package com.catface996.aiops.repository.mysql.impl.node;

import com.catface996.aiops.domain.model.node.NodeAgentRelation;
import com.catface996.aiops.repository.mysql.mapper.node.NodeAgentRelationMapper;
import com.catface996.aiops.repository.mysql.po.node.NodeAgentRelationPO;
import com.catface996.aiops.repository.node.NodeAgentRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Node-Agent 关联仓储实现
 *
 * <p>基于 MyBatis-Plus 实现 Node-Agent 关联关系的持久化操作</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Repository
@RequiredArgsConstructor
public class NodeAgentRelationRepositoryImpl implements NodeAgentRelationRepository {

    private final NodeAgentRelationMapper nodeAgentRelationMapper;

    @Override
    public void save(NodeAgentRelation relation) {
        NodeAgentRelationPO po = toPO(relation);
        nodeAgentRelationMapper.insert(po);
        relation.setId(po.getId());
    }

    @Override
    public Optional<NodeAgentRelation> findByNodeIdAndAgentId(Long nodeId, Long agentId) {
        NodeAgentRelationPO po = nodeAgentRelationMapper.selectByNodeIdAndAgentId(nodeId, agentId);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<Long> findAgentIdsByNodeId(Long nodeId) {
        return nodeAgentRelationMapper.selectAgentIdsByNodeId(nodeId);
    }

    @Override
    public List<Long> findNodeIdsByAgentId(Long agentId) {
        return nodeAgentRelationMapper.selectNodeIdsByAgentId(agentId);
    }

    @Override
    public void softDelete(Long id) {
        nodeAgentRelationMapper.softDeleteById(id);
    }

    @Override
    public void softDeleteByNodeId(Long nodeId) {
        nodeAgentRelationMapper.softDeleteByNodeId(nodeId);
    }

    @Override
    public void softDeleteByAgentId(Long agentId) {
        nodeAgentRelationMapper.softDeleteByAgentId(agentId);
    }

    @Override
    public boolean existsByNodeIdAndAgentId(Long nodeId, Long agentId) {
        return nodeAgentRelationMapper.selectByNodeIdAndAgentId(nodeId, agentId) != null;
    }

    // ==================== 转换方法 ====================

    /**
     * 领域模型转持久化对象
     */
    private NodeAgentRelationPO toPO(NodeAgentRelation domain) {
        NodeAgentRelationPO po = new NodeAgentRelationPO();
        po.setId(domain.getId());
        po.setNodeId(domain.getNodeId());
        po.setAgentId(domain.getAgentId());
        po.setCreatedAt(domain.getCreatedAt());
        po.setDeleted(Boolean.TRUE.equals(domain.getDeleted()) ? 1 : 0);
        return po;
    }

    /**
     * 持久化对象转领域模型
     */
    private NodeAgentRelation toDomain(NodeAgentRelationPO po) {
        return new NodeAgentRelation(
                po.getId(),
                po.getNodeId(),
                po.getAgentId(),
                po.getCreatedAt(),
                po.getDeleted() != null && po.getDeleted() == 1
        );
    }
}
