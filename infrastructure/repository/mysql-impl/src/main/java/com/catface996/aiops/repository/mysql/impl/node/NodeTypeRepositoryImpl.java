package com.catface996.aiops.repository.mysql.impl.node;

import com.catface996.aiops.domain.model.node.NodeType;
import com.catface996.aiops.repository.mysql.mapper.node.NodeTypeMapper;
import com.catface996.aiops.repository.mysql.po.node.NodeTypePO;
import com.catface996.aiops.repository.node.NodeTypeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 节点类型仓储实现
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Repository
public class NodeTypeRepositoryImpl implements NodeTypeRepository {

    private final NodeTypeMapper nodeTypeMapper;

    public NodeTypeRepositoryImpl(NodeTypeMapper nodeTypeMapper) {
        this.nodeTypeMapper = nodeTypeMapper;
    }

    @Override
    public Optional<NodeType> findById(Long id) {
        NodeTypePO po = nodeTypeMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<NodeType> findByCode(String code) {
        NodeTypePO po = nodeTypeMapper.selectByCode(code);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<NodeType> findAll() {
        return nodeTypeMapper.selectAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return nodeTypeMapper.selectById(id) != null;
    }

    @Override
    public boolean existsByCode(String code) {
        return nodeTypeMapper.selectByCode(code) != null;
    }

    // ==================== 转换方法 ====================

    private NodeType toDomain(NodeTypePO po) {
        if (po == null) {
            return null;
        }
        NodeType nodeType = new NodeType();
        nodeType.setId(po.getId());
        nodeType.setCode(po.getCode());
        nodeType.setName(po.getName());
        nodeType.setDescription(po.getDescription());
        nodeType.setIcon(po.getIcon());
        nodeType.setIsSystem(po.getIsSystem());
        nodeType.setAttributeSchema(po.getAttributeSchema());
        nodeType.setCreatedBy(po.getCreatedBy());
        nodeType.setCreatedAt(po.getCreatedAt());
        nodeType.setUpdatedAt(po.getUpdatedAt());
        return nodeType;
    }
}
