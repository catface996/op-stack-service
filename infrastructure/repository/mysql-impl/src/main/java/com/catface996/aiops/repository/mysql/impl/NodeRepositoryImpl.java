package com.catface996.aiops.repository.mysql.impl;

import com.catface996.aiops.domain.api.model.topology.Node;
import com.catface996.aiops.repository.NodeRepository;
import com.catface996.aiops.repository.mysql.mapper.NodeMapper;
import com.catface996.aiops.repository.mysql.po.NodePO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import java.util.Set;

/**
 * 节点仓储实现类
 */
@Repository
public class NodeRepositoryImpl implements NodeRepository {

    private final NodeMapper nodeMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Set<String> VALID_TYPES = Set.of("DATABASE", "APPLICATION", "API", "REPORT", "OTHER");

    public NodeRepositoryImpl(NodeMapper nodeMapper) {
        this.nodeMapper = nodeMapper;
    }

    @Override
    public Node save(Node entity, String operator) {
        validateNode(entity, operator);

        NodePO po = toPO(entity);
        po.setCreateBy(operator);
        po.setUpdateBy(operator);

        // 手动设置默认值（确保这些字段有值）
        po.setDeleted(0);
        po.setVersion(0);

        // 执行插入
        nodeMapper.insert(po);

        // 重新从数据库查询，确保返回完整准确的数据（包括自动填充的字段）
        NodePO savedPO = nodeMapper.selectById(po.getId());
        return toEntity(savedPO);
    }

    @Override
    public Node findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("节点 ID 不能为空");
        }
        return toEntity(nodeMapper.selectById(id));
    }

    @Override
    public Node findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("节点名称不能为空");
        }
        return toEntity(nodeMapper.selectByName(name));
    }

    @Override
    public void deleteById(Long id, String operator) {
        if (id == null) {
            throw new IllegalArgumentException("节点 ID 不能为空");
        }
        if (operator == null || operator.trim().isEmpty()) {
            throw new IllegalArgumentException("操作人不能为空");
        }

        NodePO po = nodeMapper.selectById(id);
        if (po != null) {
            po.setUpdateBy(operator);
            nodeMapper.deleteById(id);
        }
    }

    /**
     * 输入参数验证（FR-005, FR-019）
     */
    private void validateNode(Node entity, String operator) {
        if (entity == null) {
            throw new IllegalArgumentException("节点实体不能为空");
        }
        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("节点名称不能为空");
        }
        if (operator == null || operator.trim().isEmpty()) {
            throw new IllegalArgumentException("操作人不能为空");
        }
        if (entity.getName().length() > 100) {
            throw new IllegalArgumentException("节点名称长度不能超过 100 个字符");
        }
        if (entity.getType() == null || !VALID_TYPES.contains(entity.getType())) {
            throw new IllegalArgumentException("节点类型必须是以下之一: DATABASE, APPLICATION, API, REPORT, OTHER");
        }
        if (entity.getDescription() != null && entity.getDescription().length() > 500) {
            throw new IllegalArgumentException("节点描述长度不能超过 500 个字符");
        }
        if (entity.getProperties() != null && !entity.getProperties().trim().isEmpty()) {
            try {
                objectMapper.readTree(entity.getProperties());
            } catch (Exception e) {
                throw new IllegalArgumentException("properties 字段必须是有效的 JSON 格式: " + e.getMessage());
            }
        }
    }

    private NodePO toPO(Node entity) {
        if (entity == null) return null;
        
        NodePO po = new NodePO();
        po.setId(entity.getId());
        po.setName(entity.getName());
        po.setType(entity.getType());
        po.setDescription(entity.getDescription());
        po.setProperties(entity.getProperties());
        po.setCreateTime(entity.getCreateTime());
        po.setUpdateTime(entity.getUpdateTime());
        po.setCreateBy(entity.getCreateBy());
        po.setUpdateBy(entity.getUpdateBy());
        po.setDeleted(entity.getDeleted());
        po.setVersion(entity.getVersion());
        return po;
    }

    private Node toEntity(NodePO po) {
        if (po == null) return null;
        
        Node entity = new Node();
        entity.setId(po.getId());
        entity.setName(po.getName());
        entity.setType(po.getType());
        entity.setDescription(po.getDescription());
        entity.setProperties(po.getProperties());
        entity.setCreateTime(po.getCreateTime());
        entity.setUpdateTime(po.getUpdateTime());
        entity.setCreateBy(po.getCreateBy());
        entity.setUpdateBy(po.getUpdateBy());
        entity.setDeleted(po.getDeleted());
        entity.setVersion(po.getVersion());
        return entity;
    }
}
