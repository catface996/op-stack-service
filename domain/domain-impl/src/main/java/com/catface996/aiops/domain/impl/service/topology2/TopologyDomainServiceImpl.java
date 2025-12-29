package com.catface996.aiops.domain.impl.service.topology2;

import com.catface996.aiops.domain.model.agent.Agent;
import com.catface996.aiops.domain.model.agent.AgentHierarchyLevel;
import com.catface996.aiops.domain.model.agent.AgentRole;
import com.catface996.aiops.domain.model.topology.Topology;
import com.catface996.aiops.domain.model.topology.TopologyGraphData;
import com.catface996.aiops.domain.model.topology.TopologyStatus;
import com.catface996.aiops.domain.service.topology2.TopologyDomainService;
import com.catface996.aiops.repository.agent.AgentRepository;
import com.catface996.aiops.repository.node.Node2NodeRepository;
import com.catface996.aiops.repository.node.NodeRepository;
import com.catface996.aiops.repository.topology2.Topology2NodeRepository;
import com.catface996.aiops.repository.topology2.TopologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 拓扑图领域服务实现（新版本）
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Service("topologyDomainServiceV2")
public class TopologyDomainServiceImpl implements TopologyDomainService {

    private static final Logger logger = LoggerFactory.getLogger(TopologyDomainServiceImpl.class);

    private final TopologyRepository topologyRepository;
    private final Topology2NodeRepository topology2NodeRepository;
    private final NodeRepository nodeRepository;
    private final Node2NodeRepository node2NodeRepository;
    private final AgentRepository agentRepository;

    public TopologyDomainServiceImpl(TopologyRepository topologyRepository,
                                     Topology2NodeRepository topology2NodeRepository,
                                     NodeRepository nodeRepository,
                                     Node2NodeRepository node2NodeRepository,
                                     AgentRepository agentRepository) {
        this.topologyRepository = topologyRepository;
        this.topology2NodeRepository = topology2NodeRepository;
        this.nodeRepository = nodeRepository;
        this.node2NodeRepository = node2NodeRepository;
        this.agentRepository = agentRepository;
    }

    @Override
    @Transactional
    public Topology createTopology(String name, String description, Long coordinatorAgentId, Long operatorId) {
        logger.info("创建拓扑图，name: {}, operatorId: {}", name, operatorId);

        // 检查名称是否已存在
        if (topologyRepository.existsByName(name)) {
            throw new IllegalArgumentException("拓扑图名称已存在: " + name);
        }

        Topology topology = Topology.create(name, description, coordinatorAgentId, null, operatorId);
        return topologyRepository.save(topology);
    }

    @Override
    public List<Topology> listTopologies(String name, TopologyStatus status, int page, int size) {
        return topologyRepository.findByCondition(name, status, page, size);
    }

    @Override
    public long countTopologies(String name, TopologyStatus status) {
        return topologyRepository.countByCondition(name, status);
    }

    @Override
    public Optional<Topology> getTopologyById(Long topologyId) {
        return topologyRepository.findByIdWithMemberCount(topologyId);
    }

    @Override
    @Transactional
    public Topology updateTopology(Long topologyId, String name, String description,
                                   Long coordinatorAgentId, Integer version, Long operatorId) {
        logger.info("更新拓扑图，topologyId: {}, operatorId: {}", topologyId, operatorId);

        Topology topology = topologyRepository.findById(topologyId)
                .orElseThrow(() -> new IllegalArgumentException("拓扑图不存在: " + topologyId));

        // 检查版本号
        if (version != null && !version.equals(topology.getVersion())) {
            throw new IllegalStateException("版本冲突，请刷新后重试");
        }

        // 如果修改名称，检查新名称是否已存在
        if (name != null && !name.equals(topology.getName()) && topologyRepository.existsByName(name)) {
            throw new IllegalArgumentException("拓扑图名称已存在: " + name);
        }

        topology.update(name, description, coordinatorAgentId, null);

        if (!topologyRepository.update(topology)) {
            throw new IllegalStateException("更新失败，版本冲突");
        }

        return topologyRepository.findById(topologyId).orElse(topology);
    }

    @Override
    @Transactional
    public void deleteTopology(Long topologyId, Long operatorId) {
        logger.info("删除拓扑图，topologyId: {}, operatorId: {}", topologyId, operatorId);

        if (!topologyRepository.existsById(topologyId)) {
            throw new IllegalArgumentException("拓扑图不存在: " + topologyId);
        }

        // 先删除成员关系
        topology2NodeRepository.removeAllByTopologyId(topologyId);

        // 再删除拓扑图
        topologyRepository.deleteById(topologyId);
    }

    @Override
    public int countMembers(Long topologyId) {
        return topology2NodeRepository.countByTopologyId(topologyId);
    }

    @Override
    @Transactional
    public void addMembers(Long topologyId, List<Long> nodeIds, Long operatorId) {
        logger.info("添加成员到拓扑图，topologyId: {}, nodeIds: {}", topologyId, nodeIds);

        if (!topologyRepository.existsById(topologyId)) {
            throw new IllegalArgumentException("拓扑图不存在: " + topologyId);
        }

        // 验证所有节点存在
        List<Long> existingIds = nodeRepository.findExistingIds(nodeIds);
        if (existingIds.size() != nodeIds.size()) {
            throw new IllegalArgumentException("部分节点不存在");
        }

        topology2NodeRepository.addMembers(topologyId, nodeIds, operatorId);
    }

    @Override
    @Transactional
    public void removeMembers(Long topologyId, List<Long> nodeIds, Long operatorId) {
        logger.info("从拓扑图移除成员，topologyId: {}, nodeIds: {}", topologyId, nodeIds);

        if (!topologyRepository.existsById(topologyId)) {
            throw new IllegalArgumentException("拓扑图不存在: " + topologyId);
        }

        topology2NodeRepository.removeMembers(topologyId, nodeIds);
    }

    @Override
    public boolean existsById(Long topologyId) {
        return topologyRepository.existsById(topologyId);
    }

    @Override
    public TopologyGraphData getTopologyGraph(Long topologyId, boolean includeRelationships) {
        logger.info("获取拓扑图数据，topologyId: {}, includeRelationships: {}", topologyId, includeRelationships);

        // 1. 获取拓扑图基本信息
        Topology topology = topologyRepository.findById(topologyId)
                .orElseThrow(() -> new IllegalArgumentException("拓扑图不存在: " + topologyId));

        // 2. 获取拓扑图的所有成员节点
        List<Topology2NodeRepository.MemberInfo> members = topology2NodeRepository.findMembersByTopologyId(topologyId);

        // 3. 构建响应
        TopologyGraphData.TopologyGraphDataBuilder builder = TopologyGraphData.builder()
                .topologyId(topology.getId())
                .topologyName(topology.getName());

        // 4. 转换节点列表
        List<TopologyGraphData.GraphNode> nodes = members.stream()
                .map(member -> TopologyGraphData.GraphNode.builder()
                        .id(member.nodeId())
                        .name(member.nodeName())
                        .nodeTypeCode(member.nodeTypeCode())
                        .status(member.nodeStatus())
                        .positionX(member.positionX())
                        .positionY(member.positionY())
                        .build())
                .collect(Collectors.toList());
        builder.nodes(nodes);

        // 5. 如果需要包含关系，查询节点间的边
        if (includeRelationships && !members.isEmpty()) {
            List<Long> nodeIds = members.stream()
                    .map(Topology2NodeRepository.MemberInfo::nodeId)
                    .collect(Collectors.toList());

            List<Node2NodeRepository.RelationshipInfo> relationships =
                    node2NodeRepository.findRelationshipsByNodeIds(nodeIds);

            List<TopologyGraphData.GraphEdge> edges = relationships.stream()
                    .map(rel -> TopologyGraphData.GraphEdge.builder()
                            .sourceId(rel.sourceId())
                            .targetId(rel.targetId())
                            .relationshipType(rel.relationshipType())
                            .direction(rel.direction())
                            .strength(rel.strength())
                            .status(rel.status())
                            .build())
                    .collect(Collectors.toList());
            builder.edges(edges);
        } else {
            builder.edges(new ArrayList<>());
        }

        return builder.build();
    }

    @Override
    @Transactional
    public Topology bindGlobalSupervisorAgent(Long topologyId, Long agentId, Long operatorId) {
        logger.info("绑定 Global Supervisor Agent，topologyId: {}, agentId: {}, operatorId: {}",
                topologyId, agentId, operatorId);

        // 1. 验证拓扑图存在
        Topology topology = topologyRepository.findById(topologyId)
                .orElseThrow(() -> new IllegalArgumentException("拓扑图不存在: " + topologyId));

        // 2. 验证 Agent 存在且层级为 GLOBAL_SUPERVISOR（使用 hierarchyLevel 而非 role）
        Agent agent = agentRepository.findByIdAndHierarchyLevel(agentId, AgentHierarchyLevel.GLOBAL_SUPERVISOR)
                .orElseThrow(() -> {
                    // 先检查 Agent 是否存在
                    if (!agentRepository.existsById(agentId)) {
                        return new IllegalArgumentException("Agent 不存在: " + agentId);
                    }
                    // Agent 存在但层级不匹配
                    return new IllegalArgumentException("Agent 层级不匹配，必须为 GLOBAL_SUPERVISOR");
                });

        // 3. 更新绑定关系
        boolean updated = topologyRepository.updateGlobalSupervisorAgentId(topologyId, agentId);
        if (!updated) {
            throw new IllegalStateException("绑定失败，请重试");
        }

        logger.info("Global Supervisor Agent 绑定成功，topologyId: {}, agentId: {}", topologyId, agentId);

        // 4. 返回更新后的拓扑图
        return topologyRepository.findById(topologyId).orElse(topology);
    }

    @Override
    @Transactional
    public Topology unbindGlobalSupervisorAgent(Long topologyId, Long operatorId) {
        logger.info("解绑 Global Supervisor Agent，topologyId: {}, operatorId: {}", topologyId, operatorId);

        // 1. 验证拓扑图存在
        Topology topology = topologyRepository.findById(topologyId)
                .orElseThrow(() -> new IllegalArgumentException("拓扑图不存在: " + topologyId));

        // 2. 更新绑定关系（设为 null）- 幂等操作
        boolean updated = topologyRepository.updateGlobalSupervisorAgentId(topologyId, null);
        if (!updated) {
            throw new IllegalStateException("解绑失败，请重试");
        }

        logger.info("Global Supervisor Agent 解绑成功，topologyId: {}", topologyId);

        // 3. 返回更新后的拓扑图
        return topologyRepository.findById(topologyId).orElse(topology);
    }
}
