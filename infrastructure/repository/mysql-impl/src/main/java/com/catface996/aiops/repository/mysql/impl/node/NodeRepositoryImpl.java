package com.catface996.aiops.repository.mysql.impl.node;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.domain.model.node.Node;
import com.catface996.aiops.domain.model.node.NodeStatus;
import com.catface996.aiops.domain.model.node.NodeType;
import com.catface996.aiops.repository.mysql.mapper.node.NodeMapper;
import com.catface996.aiops.repository.mysql.po.node.NodePO;
import com.catface996.aiops.repository.node.NodeRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 资源节点仓储实现
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Repository
public class NodeRepositoryImpl implements NodeRepository {

    private final NodeMapper nodeMapper;

    public NodeRepositoryImpl(NodeMapper nodeMapper) {
        this.nodeMapper = nodeMapper;
    }

    @Override
    public Optional<Node> findById(Long id) {
        NodePO po = nodeMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<Node> findByIdWithType(Long id) {
        NodePO po = nodeMapper.selectByIdWithTypeInfo(id);
        return Optional.ofNullable(po).map(this::toDomainWithType);
    }

    @Override
    public Optional<Node> findByTypeIdAndName(Long nodeTypeId, String name) {
        NodePO po = nodeMapper.selectByTypeIdAndName(nodeTypeId, name);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<Node> findByName(String name) {
        NodePO po = nodeMapper.selectByName(name);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<Node> findByCondition(Long nodeTypeId, NodeStatus status, String keyword,
                                      Long topologyId, int page, int size) {
        Page<NodePO> pageParam = new Page<>(page, size);
        String statusStr = status != null ? status.name() : null;
        return nodeMapper.selectPageWithTypeInfo(pageParam, keyword, nodeTypeId, statusStr, topologyId)
                .getRecords()
                .stream()
                .map(this::toDomainWithType)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCondition(Long nodeTypeId, NodeStatus status, String keyword, Long topologyId) {
        return nodeMapper.countByCondition(keyword, nodeTypeId,
                status != null ? status.name() : null, topologyId);
    }

    @Override
    public Node save(Node node) {
        NodePO po = toPO(node);
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        nodeMapper.insert(po);
        node.setId(po.getId());
        return node;
    }

    @Override
    public boolean update(Node node) {
        NodePO po = toPO(node);
        po.setUpdatedAt(LocalDateTime.now());
        int rows = nodeMapper.updateById(po);
        return rows > 0;
    }

    @Override
    public void deleteById(Long id) {
        nodeMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return nodeMapper.selectById(id) != null;
    }

    @Override
    public boolean existsByName(String name) {
        return nodeMapper.selectByName(name) != null;
    }

    @Override
    public boolean existsByNameAndTypeId(String name, Long nodeTypeId) {
        return nodeMapper.selectByTypeIdAndName(nodeTypeId, name) != null;
    }

    @Override
    public List<Long> findExistingIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<NodePO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(NodePO::getId, ids);
        wrapper.select(NodePO::getId);
        return nodeMapper.selectList(wrapper)
                .stream()
                .map(NodePO::getId)
                .collect(Collectors.toList());
    }

    // ==================== 转换方法 ====================

    private Node toDomain(NodePO po) {
        if (po == null) {
            return null;
        }
        Node node = new Node();
        node.setId(po.getId());
        node.setName(po.getName());
        node.setDescription(po.getDescription());
        node.setNodeTypeId(po.getNodeTypeId());
        node.setStatus(NodeStatus.valueOf(po.getStatus()));
        node.setAgentTeamId(po.getAgentTeamId());
        node.setAttributes(po.getAttributes());
        node.setCreatedBy(po.getCreatedBy());
        node.setVersion(po.getVersion());
        node.setCreatedAt(po.getCreatedAt());
        node.setUpdatedAt(po.getUpdatedAt());
        return node;
    }

    private Node toDomainWithType(NodePO po) {
        Node node = toDomain(po);
        if (node != null && po.getNodeTypeName() != null) {
            NodeType nodeType = new NodeType();
            nodeType.setId(po.getNodeTypeId());
            nodeType.setCode(po.getNodeTypeCode());
            nodeType.setName(po.getNodeTypeName());
            node.setNodeType(nodeType);
        }
        return node;
    }

    private NodePO toPO(Node domain) {
        if (domain == null) {
            return null;
        }
        NodePO po = new NodePO();
        po.setId(domain.getId());
        po.setName(domain.getName());
        po.setDescription(domain.getDescription());
        po.setNodeTypeId(domain.getNodeTypeId());
        po.setStatus(domain.getStatus() != null ? domain.getStatus().name() : NodeStatus.RUNNING.name());
        po.setAgentTeamId(domain.getAgentTeamId());
        po.setAttributes(domain.getAttributes());
        po.setCreatedBy(domain.getCreatedBy());
        po.setVersion(domain.getVersion());
        po.setCreatedAt(domain.getCreatedAt());
        po.setUpdatedAt(domain.getUpdatedAt());
        return po;
    }
}
