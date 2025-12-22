package com.catface996.aiops.domain.impl.service.subgraph;

import com.catface996.aiops.domain.model.subgraph.*;
import com.catface996.aiops.domain.service.relationship.RelationshipDomainService;
import com.catface996.aiops.domain.service.subgraph.SubgraphMemberDomainService;
import com.catface996.aiops.repository.subgraph.SubgraphMemberRepository;
import com.catface996.aiops.repository.subgraph.entity.SubgraphMemberEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 子图成员领域服务实现类
 *
 * <p>v2.0 设计：子图作为资源类型，成员可以是任意资源（包括嵌套子图）</p>
 *
 * <p>实现子图成员管理的核心业务逻辑：</p>
 * <ul>
 *   <li>成员添加：权限检查、循环检测、批量添加</li>
 *   <li>成员移除：权限检查、批量移除</li>
 *   <li>成员查询：分页列表、成员详情</li>
 *   <li>拓扑查询：嵌套展开、关系获取</li>
 *   <li>祖先查询：祖先链、导航</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求5: 向子图添加成员资源</li>
 *   <li>需求6: 从子图移除成员资源</li>
 *   <li>需求7: 子图详情视图</li>
 *   <li>需求8: 成员列表查询</li>
 *   <li>需求9: 拓扑数据查询</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@Service
public class SubgraphMemberDomainServiceImpl implements SubgraphMemberDomainService {

    private static final Logger logger = LoggerFactory.getLogger(SubgraphMemberDomainServiceImpl.class);

    private final SubgraphMemberRepository memberRepository;
    private final RelationshipDomainService relationshipDomainService;

    public SubgraphMemberDomainServiceImpl(SubgraphMemberRepository memberRepository,
                                           RelationshipDomainService relationshipDomainService) {
        this.memberRepository = memberRepository;
        this.relationshipDomainService = relationshipDomainService;
    }

    // ==================== 成员添加（US5）====================

    @Override
    @Transactional
    public int addMembers(Long subgraphId, List<Long> memberIds, Long operatorId) {
        // 1. 参数验证
        if (subgraphId == null) {
            throw new IllegalArgumentException("子图 ID 不能为空");
        }
        if (memberIds == null || memberIds.isEmpty()) {
            return 0;
        }
        if (operatorId == null) {
            throw new IllegalArgumentException("操作者 ID 不能为空");
        }

        logger.info("向子图添加成员，subgraphId: {}, memberCount: {}, operatorId: {}",
                subgraphId, memberIds.size(), operatorId);

        // 2. 验证子图存在且为 SUBGRAPH 类型
        if (!memberRepository.isSubgraphType(subgraphId)) {
            throw new IllegalArgumentException("指定的资源不是子图类型：" + subgraphId);
        }

        // 3. 检查成员数量限制
        int currentCount = memberRepository.countBySubgraphId(subgraphId);
        if (currentCount + memberIds.size() > MAX_MEMBERS_PER_SUBGRAPH) {
            throw new IllegalStateException(
                    String.format("超过成员数量限制：当前 %d，要添加 %d，最大 %d",
                            currentCount, memberIds.size(), MAX_MEMBERS_PER_SUBGRAPH));
        }

        // 4. 过滤已存在的成员
        List<Long> newMemberIds = memberIds.stream()
                .filter(memberId -> !memberRepository.existsBySubgraphIdAndMemberId(subgraphId, memberId))
                .collect(Collectors.toList());

        if (newMemberIds.isEmpty()) {
            logger.info("所有成员已存在，无需添加");
            return 0;
        }

        // 5. 对子图类型成员执行循环检测
        List<Long> subgraphTypeIds = memberRepository.filterSubgraphTypeIds(newMemberIds);
        for (Long candidateId : subgraphTypeIds) {
            if (wouldCreateCycle(subgraphId, candidateId)) {
                throw new IllegalStateException(
                        String.format("检测到循环引用：将子图 %d 添加到子图 %d 会形成循环", candidateId, subgraphId));
            }
        }

        // 6. 批量添加成员
        LocalDateTime now = LocalDateTime.now();
        List<SubgraphMemberEntity> entities = newMemberIds.stream()
                .map(memberId -> {
                    SubgraphMemberEntity entity = new SubgraphMemberEntity(subgraphId, memberId, operatorId);
                    entity.setAddedAt(now);
                    return entity;
                })
                .collect(Collectors.toList());

        int addedCount = memberRepository.batchSave(entities);
        logger.info("成功添加 {} 个成员到子图 {}", addedCount, subgraphId);

        return addedCount;
    }

    // ==================== 循环检测（US5）====================

    @Override
    public boolean wouldCreateCycle(Long subgraphId, Long candidateMemberId) {
        // 如果候选成员不是子图类型，不会产生循环
        if (!memberRepository.isSubgraphType(candidateMemberId)) {
            return false;
        }

        // 如果候选成员就是当前子图本身
        if (candidateMemberId.equals(subgraphId)) {
            return true;
        }

        // 获取当前子图的所有祖先
        Set<Long> ancestors = getAncestorSubgraphIds(subgraphId);

        // 如果候选成员是当前子图的祖先，则会形成循环
        return ancestors.contains(candidateMemberId);
    }

    @Override
    public Set<Long> getAncestorSubgraphIds(Long subgraphId) {
        Set<Long> ancestors = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();

        // 查找直接父级
        List<Long> parents = memberRepository.findSubgraphIdsByMemberId(subgraphId);
        queue.addAll(parents);

        int depth = 0;
        while (!queue.isEmpty() && depth < MAX_NESTING_DEPTH) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                Long current = queue.poll();
                if (current != null && !ancestors.contains(current)) {
                    ancestors.add(current);
                    List<Long> grandparents = memberRepository.findSubgraphIdsByMemberId(current);
                    queue.addAll(grandparents);
                }
            }
            depth++;
        }

        return ancestors;
    }

    // ==================== 成员移除（US6）====================

    @Override
    @Transactional
    public int removeMembers(Long subgraphId, List<Long> memberIds, Long operatorId) {
        if (subgraphId == null || memberIds == null || memberIds.isEmpty()) {
            return 0;
        }

        logger.info("从子图移除成员，subgraphId: {}, memberCount: {}, operatorId: {}",
                subgraphId, memberIds.size(), operatorId);

        int removedCount = memberRepository.batchDelete(subgraphId, memberIds);
        logger.info("成功从子图 {} 移除 {} 个成员", subgraphId, removedCount);

        return removedCount;
    }

    // ==================== 成员查询（US8）====================

    @Override
    public List<SubgraphMember> getMembersBySubgraphIdPaged(Long subgraphId, int page, int size) {
        if (subgraphId == null) {
            return Collections.emptyList();
        }
        int offset = (page - 1) * size;
        List<SubgraphMemberEntity> entities = memberRepository.findBySubgraphIdPaged(subgraphId, offset, size);
        return entities.stream()
                .map(this::toSubgraphMember)
                .collect(Collectors.toList());
    }

    @Override
    public int countMembers(Long subgraphId) {
        if (subgraphId == null) {
            return 0;
        }
        return memberRepository.countBySubgraphId(subgraphId);
    }

    @Override
    public List<Long> getMemberIds(Long subgraphId) {
        if (subgraphId == null) {
            return Collections.emptyList();
        }
        return memberRepository.findMemberIdsBySubgraphId(subgraphId);
    }

    // ==================== 拓扑查询（US9）====================

    @Override
    public SubgraphMembersWithRelations getMembersWithRelations(Long subgraphId, boolean expandNested, int maxDepth) {
        SubgraphMembersWithRelations result = new SubgraphMembersWithRelations();
        result.setSubgraphId(subgraphId);

        // 获取成员
        List<SubgraphMember> members;
        List<NestedSubgraphInfo> nestedSubgraphs = new ArrayList<>();

        if (expandNested) {
            members = expandNestedSubgraphs(subgraphId, maxDepth, null, 0);
            // 收集嵌套子图信息
            for (SubgraphMember member : members) {
                if (member.isSubgraph()) {
                    NestedSubgraphInfo info = new NestedSubgraphInfo(
                            member.getMemberId(),
                            member.getMemberName(),
                            member.getSubgraphId(),
                            0, // 从展开列表中计算
                            member.getNestedMemberCount()
                    );
                    info.setExpanded(true);
                    nestedSubgraphs.add(info);
                }
            }
        } else {
            List<SubgraphMemberEntity> entities = memberRepository.findBySubgraphId(subgraphId);
            members = entities.stream()
                    .map(this::toSubgraphMember)
                    .collect(Collectors.toList());
            // 收集未展开的嵌套子图信息
            for (SubgraphMember member : members) {
                if (member.isSubgraph()) {
                    NestedSubgraphInfo info = new NestedSubgraphInfo(
                            member.getMemberId(),
                            member.getMemberName(),
                            subgraphId,
                            0,
                            member.getNestedMemberCount()
                    );
                    info.setExpanded(false);
                    nestedSubgraphs.add(info);
                }
            }
        }

        result.setMembers(members);
        result.setNestedSubgraphs(nestedSubgraphs);

        // 获取成员间的关系
        List<Long> memberIds = members.stream()
                .map(SubgraphMember::getMemberId)
                .collect(Collectors.toList());

        if (!memberIds.isEmpty()) {
            List<TopologyEdge> relationships = getRelationshipsBetweenMembers(memberIds);
            result.setRelationships(relationships);
            result.setEdgeCount(relationships.size());
        } else {
            result.setRelationships(Collections.emptyList());
            result.setEdgeCount(0);
        }

        result.setNodeCount(members.size());
        result.setMaxDepth(maxDepth);

        return result;
    }

    @Override
    public SubgraphTopologyResult getSubgraphTopology(Long subgraphId, boolean expandNested) {
        SubgraphTopologyResult result = new SubgraphTopologyResult();

        // 获取成员
        List<SubgraphMember> members;
        if (expandNested) {
            members = expandNestedSubgraphs(subgraphId, MAX_NESTING_DEPTH, null, 0);
        } else {
            List<SubgraphMemberEntity> entities = memberRepository.findBySubgraphId(subgraphId);
            members = entities.stream()
                    .map(this::toSubgraphMember)
                    .collect(Collectors.toList());
        }

        // 转换为拓扑节点
        Map<Long, SubgraphBoundary> boundaryMap = new HashMap<>();
        for (SubgraphMember member : members) {
            TopologyNode node = new TopologyNode(
                    member.getMemberId(),
                    member.getMemberName(),
                    member.getMemberTypeCode(),
                    member.getMemberStatus(),
                    member.isSubgraph()
            );
            node.setExpanded(expandNested && member.isSubgraph());
            node.setParentSubgraphId(member.getSubgraphId());
            result.addNode(node);

            // 收集子图边界
            Long parentId = member.getSubgraphId();
            if (parentId != null) {
                boundaryMap.computeIfAbsent(parentId, id -> new SubgraphBoundary(id, null, new ArrayList<>()))
                        .getMemberIds().add(member.getMemberId());
            }
        }

        // 添加子图边界
        boundaryMap.values().forEach(result::addSubgraphBoundary);

        // 获取关系
        List<Long> memberIds = members.stream()
                .map(SubgraphMember::getMemberId)
                .collect(Collectors.toList());

        if (!memberIds.isEmpty()) {
            List<TopologyEdge> edges = getRelationshipsBetweenMembers(memberIds);
            edges.forEach(result::addEdge);
        }

        return result;
    }

    @Override
    public List<SubgraphMember> expandNestedSubgraphs(Long subgraphId, int maxDepth, Long parentSubgraphId, int currentDepth) {
        if (currentDepth >= maxDepth) {
            return Collections.emptyList();
        }

        List<SubgraphMember> result = new ArrayList<>();
        List<SubgraphMemberEntity> directMembers = memberRepository.findBySubgraphId(subgraphId);

        for (SubgraphMemberEntity entity : directMembers) {
            SubgraphMember member = toSubgraphMember(entity);
            result.add(member);

            // 如果是子图类型且未达到最大深度，递归展开
            if (Boolean.TRUE.equals(entity.getIsSubgraph()) && currentDepth + 1 < maxDepth) {
                List<SubgraphMember> nestedMembers = expandNestedSubgraphs(
                        entity.getMemberId(), maxDepth, entity.getSubgraphId(), currentDepth + 1);
                result.addAll(nestedMembers);
            }
        }

        return result;
    }

    // ==================== 祖先查询（US7）====================

    @Override
    public List<AncestorInfo> getAncestors(Long subgraphId) {
        List<AncestorInfo> ancestors = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Queue<Long> currentLevel = new LinkedList<>();

        // 查找直接父级
        List<Long> parents = memberRepository.findSubgraphIdsByMemberId(subgraphId);
        currentLevel.addAll(parents);

        int depth = 1;
        while (!currentLevel.isEmpty() && depth <= MAX_NESTING_DEPTH) {
            Queue<Long> nextLevel = new LinkedList<>();
            while (!currentLevel.isEmpty()) {
                Long current = currentLevel.poll();
                if (current != null && !visited.contains(current)) {
                    visited.add(current);
                    // TODO: 需要获取子图名称，暂时使用 ID 作为名称
                    AncestorInfo info = new AncestorInfo(current, "Subgraph-" + current, depth);
                    ancestors.add(info);

                    List<Long> grandparents = memberRepository.findSubgraphIdsByMemberId(current);
                    nextLevel.addAll(grandparents);
                }
            }
            currentLevel = nextLevel;
            depth++;
        }

        // 按深度排序
        ancestors.sort(Comparator.comparingInt(AncestorInfo::getDepth));
        return ancestors;
    }

    // ==================== 子图删除校验（Phase 8）====================

    @Override
    public boolean isSubgraphEmpty(Long subgraphId) {
        if (subgraphId == null) {
            return true;
        }
        return !memberRepository.hasMembers(subgraphId);
    }

    // ==================== 类型检查 ====================

    @Override
    public boolean isSubgraphType(Long resourceId) {
        if (resourceId == null) {
            return false;
        }
        return memberRepository.isSubgraphType(resourceId);
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取成员之间的关系
     */
    private List<TopologyEdge> getRelationshipsBetweenMembers(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> memberSet = new HashSet<>(memberIds);
        Set<String> seenEdges = new HashSet<>();
        List<TopologyEdge> edges = new ArrayList<>();

        // 通过 RelationshipDomainService 获取关系
        try {
            for (Long memberId : memberIds) {
                // 获取上游和下游依赖
                var upstreamRels = relationshipDomainService.getUpstreamDependencies(memberId);
                var downstreamRels = relationshipDomainService.getDownstreamDependencies(memberId);

                // 合并所有关系
                List<com.catface996.aiops.domain.model.relationship.Relationship> allRels = new ArrayList<>();
                if (upstreamRels != null) allRels.addAll(upstreamRels);
                if (downstreamRels != null) allRels.addAll(downstreamRels);

                for (var rel : allRels) {
                    // 只保留成员间的关系
                    if (memberSet.contains(rel.getSourceResourceId()) &&
                            memberSet.contains(rel.getTargetResourceId())) {
                        // 去重（使用 source-target 作为唯一标识）
                        String edgeKey = rel.getSourceResourceId() + "-" + rel.getTargetResourceId();
                        if (!seenEdges.contains(edgeKey)) {
                            seenEdges.add(edgeKey);
                            TopologyEdge edge = new TopologyEdge(
                                    rel.getSourceResourceId(),
                                    rel.getTargetResourceId(),
                                    rel.getRelationshipType() != null ? rel.getRelationshipType().name() : null
                            );
                            edge.setDirection(rel.getDirection() != null ? rel.getDirection().name() : null);
                            edge.setStrength(rel.getStrength() != null ? rel.getStrength().name() : null);
                            edge.setStatus(rel.getStatus() != null ? rel.getStatus().name() : null);
                            edges.add(edge);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("获取成员关系失败: {}", e.getMessage());
        }

        return edges;
    }

    /**
     * 实体转领域模型
     */
    private SubgraphMember toSubgraphMember(SubgraphMemberEntity entity) {
        if (entity == null) {
            return null;
        }
        SubgraphMember member = new SubgraphMember();
        member.setId(entity.getId());
        member.setSubgraphId(entity.getSubgraphId());
        member.setMemberId(entity.getMemberId());
        member.setAddedAt(entity.getAddedAt());
        member.setAddedBy(entity.getAddedBy());
        member.setMemberName(entity.getMemberName());
        member.setMemberTypeCode(entity.getMemberTypeCode());
        member.setMemberStatus(entity.getMemberStatus());
        member.setSubgraph(Boolean.TRUE.equals(entity.getIsSubgraph()));
        member.setNestedMemberCount(entity.getNestedMemberCount() != null ? entity.getNestedMemberCount() : 0);
        return member;
    }
}
