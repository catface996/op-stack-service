package com.catface996.aiops.domain.impl.service.relationship;

import com.catface996.aiops.common.enums.RelationshipErrorCode;
import com.catface996.aiops.common.enums.ResourceErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.node.Node;
import com.catface996.aiops.domain.model.relationship.*;
import com.catface996.aiops.domain.service.relationship.RelationshipDomainService;
import com.catface996.aiops.repository.node.NodeRepository;
import com.catface996.aiops.domain.model.topology.Topology;
import com.catface996.aiops.repository.relationship.RelationshipRepository;
import com.catface996.aiops.repository.topology2.TopologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 资源关系领域服务实现
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
@Service
public class RelationshipDomainServiceImpl implements RelationshipDomainService {

    private static final Logger log = LoggerFactory.getLogger(RelationshipDomainServiceImpl.class);

    private static final int DEFAULT_MAX_DEPTH = 10;
    private static final int MAX_TRAVERSE_NODES = 1000;

    private final RelationshipRepository relationshipRepository;
    private final NodeRepository nodeRepository;
    private final TopologyRepository topologyRepository;

    public RelationshipDomainServiceImpl(RelationshipRepository relationshipRepository,
                                          NodeRepository nodeRepository,
                                          TopologyRepository topologyRepository) {
        this.relationshipRepository = relationshipRepository;
        this.nodeRepository = nodeRepository;
        this.topologyRepository = topologyRepository;
    }

    @Override
    @Transactional
    public Relationship createRelationship(Long sourceResourceId, Long targetResourceId,
                                            RelationshipType type, RelationshipDirection direction,
                                            RelationshipStrength strength, String description,
                                            Long topologyId, Long operatorId) {
        // 1. 参数验证
        if (sourceResourceId == null || targetResourceId == null) {
            throw new BusinessException(RelationshipErrorCode.SOURCE_RESOURCE_NOT_FOUND, "源资源ID和目标资源ID不能为空");
        }
        if (sourceResourceId.equals(targetResourceId)) {
            throw new BusinessException(RelationshipErrorCode.SELF_REFERENCE_NOT_ALLOWED);
        }
        if (type == null || direction == null || strength == null) {
            throw new BusinessException(RelationshipErrorCode.INVALID_RELATIONSHIP_TYPE, "关系类型、方向和强度不能为空");
        }

        // 2. 验证源节点存在
        Node sourceNode = nodeRepository.findById(sourceResourceId)
                .orElseThrow(() -> new BusinessException(RelationshipErrorCode.SOURCE_RESOURCE_NOT_FOUND, sourceResourceId));

        // 3. 验证目标节点存在
        Node targetNode = nodeRepository.findById(targetResourceId)
                .orElseThrow(() -> new BusinessException(RelationshipErrorCode.TARGET_RESOURCE_NOT_FOUND, targetResourceId));

        // 4. 验证拓扑存在和用户权限（只需要检查拓扑的所有者权限）
        Topology topology = topologyRepository.findById(topologyId)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.RESOURCE_NOT_FOUND, "拓扑不存在: " + topologyId));
        
        if (!topology.isOwner(operatorId)) {
            throw new BusinessException(ResourceErrorCode.FORBIDDEN);
        }

        // 5. 检查关系是否已存在
        if (relationshipRepository.existsBySourceAndTargetAndType(sourceResourceId, targetResourceId, type)) {
            throw new BusinessException(RelationshipErrorCode.RELATIONSHIP_ALREADY_EXISTS,
                    sourceResourceId, targetResourceId, type.name());
        }

        // 6. 创建关系
        Relationship relationship = Relationship.create(sourceResourceId, targetResourceId,
                type, direction, strength, description);
        relationship = relationshipRepository.save(relationship);

        log.info("关系创建成功: {} -> {} [{}]", sourceResourceId, targetResourceId, type);

        // 7. 如果是双向关系，创建反向关系
        if (direction.isBidirectional()) {
            if (!relationshipRepository.existsBySourceAndTargetAndType(targetResourceId, sourceResourceId, type)) {
                Relationship reverseRelationship = relationship.createReverseRelationship();
                relationshipRepository.save(reverseRelationship);
                log.info("反向关系创建成功: {} -> {} [{}]", targetResourceId, sourceResourceId, type);
            }
        }

        // 设置资源名称用于返回
        relationship.setSourceResourceName(sourceNode.getName());
        relationship.setTargetResourceName(targetNode.getName());

        return relationship;
    }

    @Override
    public List<Relationship> listRelationships(Long sourceResourceId, Long targetResourceId,
                                                 RelationshipType type, RelationshipStatus status,
                                                 int pageNum, int pageSize) {
        List<Relationship> relationships = relationshipRepository.findByConditions(
                sourceResourceId, targetResourceId, type, status, pageNum, pageSize);

        // 填充资源名称
        enrichRelationshipsWithNodeNames(relationships);

        return relationships;
    }

    @Override
    public long countRelationships(Long sourceResourceId, Long targetResourceId,
                                   RelationshipType type, RelationshipStatus status) {
        return relationshipRepository.countByConditions(sourceResourceId, targetResourceId, type, status);
    }

    @Override
    public Optional<Relationship> getRelationshipById(Long relationshipId) {
        Optional<Relationship> relationshipOpt = relationshipRepository.findById(relationshipId);
        relationshipOpt.ifPresent(r -> enrichRelationshipsWithNodeNames(Collections.singletonList(r)));
        return relationshipOpt;
    }

    @Override
    public List<Relationship> getUpstreamDependencies(Long resourceId) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为空");
        }
        List<Relationship> relationships = relationshipRepository.findByTargetResourceId(resourceId);
        enrichRelationshipsWithNodeNames(relationships);
        return relationships;
    }

    @Override
    public List<Relationship> getDownstreamDependencies(Long resourceId) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为空");
        }
        List<Relationship> relationships = relationshipRepository.findBySourceResourceId(resourceId);
        enrichRelationshipsWithNodeNames(relationships);
        return relationships;
    }

    @Override
    @Transactional
    public Relationship updateRelationship(Long relationshipId, RelationshipType type,
                                            RelationshipStrength strength, RelationshipStatus status,
                                            String description, Long operatorId) {
        // 1. 验证关系存在
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(RelationshipErrorCode.RELATIONSHIP_NOT_FOUND, relationshipId));

        // 2. 验证用户权限（只需要源节点的权限）
        Node sourceNode = nodeRepository.findById(relationship.getSourceResourceId())
                .orElse(null);
        if (sourceNode == null || !sourceNode.isOwner(operatorId)) {
            throw new BusinessException(ResourceErrorCode.FORBIDDEN);
        }

        // 3. 更新关系
        relationship.update(type, strength, status, description);
        relationship = relationshipRepository.update(relationship);

        log.info("关系更新成功: {}", relationshipId);

        // 设置资源名称
        relationship.setSourceResourceName(sourceNode.getName());
        Node targetNode = nodeRepository.findById(relationship.getTargetResourceId())
                .orElse(null);
        if (targetNode != null) {
            relationship.setTargetResourceName(targetNode.getName());
        }

        return relationship;
    }

    @Override
    @Transactional
    public void deleteRelationship(Long relationshipId, Long operatorId) {
        // 1. 验证关系存在
        Relationship relationship = relationshipRepository.findById(relationshipId)
                .orElseThrow(() -> new BusinessException(RelationshipErrorCode.RELATIONSHIP_NOT_FOUND, relationshipId));

        // 2. 验证用户权限（只需要源节点的权限）
        Node sourceNode = nodeRepository.findById(relationship.getSourceResourceId())
                .orElse(null);
        if (sourceNode == null || !sourceNode.isOwner(operatorId)) {
            throw new BusinessException(ResourceErrorCode.FORBIDDEN);
        }

        // 3. 删除关系
        relationshipRepository.deleteById(relationshipId);
        log.info("关系删除成功: {}", relationshipId);

        // 4. 如果是双向关系，删除反向关系
        if (relationship.isBidirectional()) {
            relationshipRepository.deleteBySourceAndTargetAndType(
                    relationship.getTargetResourceId(),
                    relationship.getSourceResourceId(),
                    relationship.getRelationshipType());
            log.info("反向关系删除成功");
        }
    }

    @Override
    @Transactional
    public void deleteRelationshipsByResource(Long resourceId) {
        if (resourceId == null) {
            return;
        }
        relationshipRepository.deleteByResourceId(resourceId);
        log.info("资源关联的所有关系已删除: {}", resourceId);
    }

    @Override
    public CycleDetectionResult detectCycle(Long resourceId) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为空");
        }

        Set<Long> visited = new HashSet<>();
        Set<Long> recursionStack = new HashSet<>();
        List<Long> cyclePath = new ArrayList<>();

        boolean hasCycle = dfsDetectCycle(resourceId, visited, recursionStack, cyclePath);

        if (hasCycle) {
            return CycleDetectionResult.withCycle(cyclePath);
        }
        return CycleDetectionResult.noCycle();
    }

    private boolean dfsDetectCycle(Long resourceId, Set<Long> visited,
                                    Set<Long> recursionStack, List<Long> cyclePath) {
        if (recursionStack.contains(resourceId)) {
            // 找到循环，构建循环路径
            cyclePath.add(resourceId);
            return true;
        }

        if (visited.contains(resourceId)) {
            return false;
        }

        visited.add(resourceId);
        recursionStack.add(resourceId);
        cyclePath.add(resourceId);

        // 获取下游依赖
        List<Relationship> downstreams = relationshipRepository.findBySourceResourceId(resourceId);
        for (Relationship rel : downstreams) {
            if (dfsDetectCycle(rel.getTargetResourceId(), visited, recursionStack, cyclePath)) {
                return true;
            }
        }

        recursionStack.remove(resourceId);
        cyclePath.remove(cyclePath.size() - 1);
        return false;
    }

    @Override
    public TraverseResult traverse(Long resourceId, int maxDepth) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为空");
        }
        if (maxDepth <= 0) {
            maxDepth = DEFAULT_MAX_DEPTH;
        }

        Map<Integer, List<Long>> nodesByLevel = new HashMap<>();
        List<Relationship> allRelationships = new ArrayList<>();
        Set<Long> visited = new HashSet<>();

        // BFS 遍历
        Queue<Long> queue = new LinkedList<>();
        Queue<Integer> levelQueue = new LinkedList<>();

        queue.offer(resourceId);
        levelQueue.offer(0);
        visited.add(resourceId);

        while (!queue.isEmpty() && visited.size() < MAX_TRAVERSE_NODES) {
            Long currentId = queue.poll();
            int currentLevel = levelQueue.poll();

            // 添加到当前层级
            nodesByLevel.computeIfAbsent(currentLevel, k -> new ArrayList<>()).add(currentId);

            // 如果还没到最大深度，继续遍历
            if (currentLevel < maxDepth) {
                List<Relationship> downstreams = relationshipRepository.findBySourceResourceId(currentId);
                for (Relationship rel : downstreams) {
                    allRelationships.add(rel);
                    Long targetId = rel.getTargetResourceId();
                    if (!visited.contains(targetId)) {
                        visited.add(targetId);
                        queue.offer(targetId);
                        levelQueue.offer(currentLevel + 1);
                    }
                }
            }
        }

        return new TraverseResult(resourceId, nodesByLevel, allRelationships);
    }

    /**
     * 为关系列表填充节点名称
     */
    private void enrichRelationshipsWithNodeNames(List<Relationship> relationships) {
        if (relationships == null || relationships.isEmpty()) {
            return;
        }

        // 收集所有资源ID
        Set<Long> nodeIds = new HashSet<>();
        for (Relationship rel : relationships) {
            nodeIds.add(rel.getSourceResourceId());
            nodeIds.add(rel.getTargetResourceId());
        }

        // 查询节点名称
        Map<Long, String> nodeNameMap = new HashMap<>();
        for (Long id : nodeIds) {
            nodeRepository.findById(id)
                    .ifPresent(n -> nodeNameMap.put(id, n.getName()));
        }

        // 填充名称
        for (Relationship rel : relationships) {
            rel.setSourceResourceName(nodeNameMap.get(rel.getSourceResourceId()));
            rel.setTargetResourceName(nodeNameMap.get(rel.getTargetResourceId()));
        }
    }
}
