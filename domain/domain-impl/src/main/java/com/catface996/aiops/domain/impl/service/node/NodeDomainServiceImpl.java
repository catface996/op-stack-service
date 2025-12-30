package com.catface996.aiops.domain.impl.service.node;

import com.catface996.aiops.domain.model.node.Node;
import com.catface996.aiops.domain.model.node.NodeLayer;
import com.catface996.aiops.domain.model.node.NodeStatus;
import com.catface996.aiops.domain.model.node.NodeType;
import com.catface996.aiops.domain.service.node.NodeDomainService;
import com.catface996.aiops.repository.node.NodeRepository;
import com.catface996.aiops.repository.node.NodeTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 资源节点领域服务实现
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Service
public class NodeDomainServiceImpl implements NodeDomainService {

    private static final Logger logger = LoggerFactory.getLogger(NodeDomainServiceImpl.class);

    private final NodeRepository nodeRepository;
    private final NodeTypeRepository nodeTypeRepository;

    public NodeDomainServiceImpl(NodeRepository nodeRepository, NodeTypeRepository nodeTypeRepository) {
        this.nodeRepository = nodeRepository;
        this.nodeTypeRepository = nodeTypeRepository;
    }

    @Override
    @Transactional
    public Node createNode(String name, String description, Long nodeTypeId, NodeLayer layer,
                           String attributes, Long operatorId) {
        logger.info("创建节点，name: {}, nodeTypeId: {}, layer: {}, operatorId: {}", name, nodeTypeId, layer, operatorId);

        // 检查名称是否已存在
        if (nodeRepository.existsByName(name)) {
            throw new IllegalArgumentException("节点名称已存在: " + name);
        }

        // 检查节点类型是否存在
        if (!nodeTypeRepository.existsById(nodeTypeId)) {
            throw new IllegalArgumentException("节点类型不存在: " + nodeTypeId);
        }

        Node node = Node.create(name, description, nodeTypeId, layer, attributes, operatorId);
        return nodeRepository.save(node);
    }

    @Override
    public List<Node> listNodes(Long nodeTypeId, NodeStatus status, NodeLayer layer, String keyword,
                                Long topologyId, int page, int size) {
        return nodeRepository.findByCondition(nodeTypeId, status, layer, keyword, topologyId, page, size);
    }

    @Override
    public long countNodes(Long nodeTypeId, NodeStatus status, NodeLayer layer, String keyword, Long topologyId) {
        return nodeRepository.countByCondition(nodeTypeId, status, layer, keyword, topologyId);
    }

    @Override
    public Optional<Node> getNodeById(Long nodeId) {
        return nodeRepository.findById(nodeId);
    }

    @Override
    @Transactional
    public Node updateNode(Long nodeId, String name, String description,
                           String attributes, Integer version, Long operatorId) {
        logger.info("更新节点，nodeId: {}, operatorId: {}", nodeId, operatorId);

        Node node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("节点不存在: " + nodeId));

        // 检查版本号
        if (version != null && !version.equals(node.getVersion())) {
            throw new IllegalStateException("版本冲突，请刷新后重试");
        }

        // 如果修改名称，检查新名称是否已存在
        if (name != null && !name.equals(node.getName()) && nodeRepository.existsByName(name)) {
            throw new IllegalArgumentException("节点名称已存在: " + name);
        }

        node.update(name, description, attributes);

        if (!nodeRepository.update(node)) {
            throw new IllegalStateException("更新失败，版本冲突");
        }

        return nodeRepository.findById(nodeId).orElse(node);
    }

    @Override
    @Transactional
    public void deleteNode(Long nodeId, Long operatorId) {
        logger.info("删除节点，nodeId: {}, operatorId: {}", nodeId, operatorId);

        if (!nodeRepository.existsById(nodeId)) {
            throw new IllegalArgumentException("节点不存在: " + nodeId);
        }

        // 删除节点（关联的关系由数据库外键级联删除或在应用层处理）
        nodeRepository.deleteById(nodeId);
    }

    @Override
    public List<NodeType> listNodeTypes() {
        return nodeTypeRepository.findAll();
    }

    @Override
    public boolean existsById(Long nodeId) {
        return nodeRepository.existsById(nodeId);
    }

    @Override
    public List<Long> findExistingIds(List<Long> nodeIds) {
        return nodeRepository.findExistingIds(nodeIds);
    }
}
