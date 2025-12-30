package com.catface996.aiops.application.impl.service.topology;

import com.catface996.aiops.application.api.dto.agent.AgentDTO;
import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.node.NodeDTO;
import com.catface996.aiops.application.api.dto.topology.HierarchicalTeamDTO;
import com.catface996.aiops.application.api.dto.topology.TeamDTO;
import com.catface996.aiops.application.api.dto.topology.TopologyDTO;
import com.catface996.aiops.application.api.dto.topology.TopologyGraphDTO;
import com.catface996.aiops.application.api.dto.topology.request.CreateTopologyRequest;
import com.catface996.aiops.application.api.dto.topology.request.HierarchicalTeamQueryRequest;
import com.catface996.aiops.application.api.dto.topology.request.QueryMembersRequest;
import com.catface996.aiops.application.api.dto.topology.request.QueryTopologiesRequest;
import com.catface996.aiops.application.api.dto.topology.request.QueryTopologyGraphRequest;
import com.catface996.aiops.application.api.dto.topology.request.UpdateTopologyRequest;
import com.catface996.aiops.application.api.service.topology.TopologyApplicationService;
import com.catface996.aiops.domain.model.agent.Agent;
import com.catface996.aiops.domain.model.node.Node;
import com.catface996.aiops.domain.model.node.NodeStatus;
import com.catface996.aiops.domain.model.topology.Topology;
import com.catface996.aiops.domain.model.topology.TopologyStatus;
import com.catface996.aiops.domain.model.topology.TopologyGraphData;
import com.catface996.aiops.domain.service.node.NodeDomainService;
import com.catface996.aiops.domain.service.topology2.TopologyDomainService;
import com.catface996.aiops.domain.model.agent.AgentHierarchyLevel;
import com.catface996.aiops.domain.model.agentbound.AgentBound;
import com.catface996.aiops.domain.model.agentbound.BoundEntityType;
import com.catface996.aiops.repository.agent.AgentRepository;
import com.catface996.aiops.repository.agentbound.AgentBoundRepository;
import com.catface996.aiops.repository.node.NodeTypeRepository;
import com.catface996.aiops.repository.topology2.Topology2NodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 拓扑图应用服务实现
 *
 * <p>协调领域层完成拓扑图管理业务逻辑，负责 DTO 转换。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: resource 表拆分为 topology 表和 node 表</li>
 *   <li>FR-006: 拓扑图 API 保持接口契约不变</li>
 *   <li>US1: 查询所有拓扑图</li>
 *   <li>US3: 创建拓扑图</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Service
public class TopologyApplicationServiceImpl implements TopologyApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(TopologyApplicationServiceImpl.class);

    private final TopologyDomainService topologyDomainService;
    private final NodeDomainService nodeDomainService;
    private final NodeTypeRepository nodeTypeRepository;
    private final AgentRepository agentRepository;
    private final AgentBoundRepository agentBoundRepository;
    private final Topology2NodeRepository topology2NodeRepository;

    public TopologyApplicationServiceImpl(@Qualifier("topologyDomainServiceV2") TopologyDomainService topologyDomainService,
                                          NodeDomainService nodeDomainService,
                                          NodeTypeRepository nodeTypeRepository,
                                          AgentRepository agentRepository,
                                          AgentBoundRepository agentBoundRepository,
                                          Topology2NodeRepository topology2NodeRepository) {
        this.topologyDomainService = topologyDomainService;
        this.nodeDomainService = nodeDomainService;
        this.nodeTypeRepository = nodeTypeRepository;
        this.agentRepository = agentRepository;
        this.agentBoundRepository = agentBoundRepository;
        this.topology2NodeRepository = topology2NodeRepository;
    }

    @Override
    public TopologyDTO createTopology(CreateTopologyRequest request, Long operatorId, String operatorName) {
        logger.info("创建拓扑图，name: {}, operatorId: {}", request.getName(), operatorId);

        Topology topology = topologyDomainService.createTopology(
                request.getName(),
                request.getDescription(),
                operatorId
        );

        return toDTO(topology);
    }

    @Override
    public PageResult<TopologyDTO> listTopologies(QueryTopologiesRequest request) {
        TopologyStatus status = parseStatus(request.getStatus());

        List<Topology> topologies = topologyDomainService.listTopologies(
                request.getName(),
                status,
                request.getPage(),
                request.getSize()
        );

        long total = topologyDomainService.countTopologies(request.getName(), status);

        List<TopologyDTO> dtos = topologies.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtos, request.getPage(), request.getSize(), total);
    }

    @Override
    public TopologyDTO getTopologyById(Long topologyId) {
        return topologyDomainService.getTopologyById(topologyId)
                .map(this::toDTO)
                .orElse(null);
    }

    @Override
    public TopologyDTO updateTopology(Long topologyId, UpdateTopologyRequest request,
                                      Long operatorId, String operatorName) {
        logger.info("更新拓扑图，topologyId: {}, operatorId: {}", topologyId, operatorId);

        Topology topology = topologyDomainService.updateTopology(
                topologyId,
                request.getName(),
                request.getDescription(),
                request.getVersion(),
                operatorId
        );

        return toDTO(topology);
    }

    @Override
    public void deleteTopology(Long topologyId, Long operatorId, String operatorName) {
        logger.info("删除拓扑图，topologyId: {}, operatorId: {}", topologyId, operatorId);
        topologyDomainService.deleteTopology(topologyId, operatorId);
    }

    // ===== 成员管理方法 =====

    @Override
    public void addMembers(Long topologyId, List<Long> nodeIds, Long operatorId, String operatorName) {
        logger.info("添加成员到拓扑图，topologyId: {}, nodeIds: {}, operatorId: {}", topologyId, nodeIds, operatorId);
        topologyDomainService.addMembers(topologyId, nodeIds, operatorId);
    }

    @Override
    public void removeMembers(Long topologyId, List<Long> nodeIds, Long operatorId, String operatorName) {
        logger.info("从拓扑图移除成员，topologyId: {}, nodeIds: {}, operatorId: {}", topologyId, nodeIds, operatorId);
        topologyDomainService.removeMembers(topologyId, nodeIds, operatorId);
    }

    @Override
    public PageResult<NodeDTO> queryMembers(QueryMembersRequest request) {
        logger.info("查询拓扑图成员，topologyId: {}", request.getTopologyId());

        // 使用 nodeDomainService 查询指定拓扑图的成员
        NodeStatus status = null; // 成员查询不筛选状态
        List<Node> nodes = nodeDomainService.listNodes(
                request.getNodeTypeId(),
                status,
                null, // layer: 成员查询不筛选架构层级
                request.getKeyword(),
                request.getTopologyId(),
                request.getPage(),
                request.getSize()
        );

        long total = nodeDomainService.countNodes(
                request.getNodeTypeId(),
                status,
                null, // layer: 成员查询不筛选架构层级
                request.getKeyword(),
                request.getTopologyId()
        );

        List<NodeDTO> dtos = nodes.stream()
                .map(this::toNodeDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtos, request.getPage(), request.getSize(), total);
    }

    // ===== DTO 转换方法 =====

    private TopologyDTO toDTO(Topology topology) {
        if (topology == null) {
            return null;
        }

        TopologyDTO.TopologyDTOBuilder builder = TopologyDTO.builder()
                .id(topology.getId())
                .name(topology.getName())
                .description(topology.getDescription())
                .attributes(topology.getAttributes())
                .version(topology.getVersion())
                .createdBy(topology.getCreatedBy())
                .createdAt(topology.getCreatedAt())
                .updatedAt(topology.getUpdatedAt());

        if (topology.getStatus() != null) {
            builder.status(topology.getStatus().name())
                    .statusDisplay(topology.getStatus().getDescription());
        }

        // 获取成员数量
        int memberCount = topologyDomainService.countMembers(topology.getId());
        builder.memberCount(memberCount);

        return builder.build();
    }

    private TopologyStatus parseStatus(String status) {
        if (status == null || status.isEmpty()) {
            return null;
        }
        try {
            return TopologyStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            logger.warn("无效的拓扑图状态: {}", status);
            return null;
        }
    }

    private NodeDTO toNodeDTO(Node node) {
        if (node == null) {
            return null;
        }

        NodeDTO.NodeDTOBuilder builder = NodeDTO.builder()
                .id(node.getId())
                .name(node.getName())
                .description(node.getDescription())
                .nodeTypeId(node.getNodeTypeId())
                .attributes(node.getAttributes())
                .version(node.getVersion())
                .createdBy(node.getCreatedBy())
                .createdAt(node.getCreatedAt())
                .updatedAt(node.getUpdatedAt());

        if (node.getStatus() != null) {
            builder.status(node.getStatus().name())
                    .statusDisplay(node.getStatus().getDescription());
        }

        // 获取节点类型信息
        if (node.getNodeType() != null) {
            builder.nodeTypeName(node.getNodeType().getName())
                    .nodeTypeCode(node.getNodeType().getCode());
        } else if (node.getNodeTypeId() != null) {
            nodeTypeRepository.findById(node.getNodeTypeId()).ifPresent(nodeType -> {
                builder.nodeTypeName(nodeType.getName())
                        .nodeTypeCode(nodeType.getCode());
            });
        }

        return builder.build();
    }

    // ===== 拓扑图数据查询 =====

    @Override
    public TopologyGraphDTO getTopologyGraph(QueryTopologyGraphRequest request) {
        logger.info("获取拓扑图数据，topologyId: {}, depth: {}", request.getTopologyId(), request.getDepth());

        // 调用领域服务获取拓扑图数据
        TopologyGraphData graphData = topologyDomainService.getTopologyGraph(
                request.getTopologyId(),
                Boolean.TRUE.equals(request.getIncludeRelationships())
        );

        // 转换为 DTO
        return toTopologyGraphDTO(graphData);
    }

    // ===== Global Supervisor Agent 绑定方法 =====
    // Note: 绑定方法已移至 AgentBoundApplicationService (Feature 040)

    private TopologyGraphDTO toTopologyGraphDTO(TopologyGraphData graphData) {
        TopologyGraphDTO result = new TopologyGraphDTO();

        // 设置拓扑图基本信息
        result.setTopology(TopologyGraphDTO.TopologyInfo.builder()
                .id(graphData.getTopologyId())
                .name(graphData.getTopologyName())
                .build());

        // 转换节点列表
        List<TopologyGraphDTO.GraphNodeDTO> nodes = graphData.getNodes().stream()
                .map(node -> TopologyGraphDTO.GraphNodeDTO.builder()
                        .id(node.getId())
                        .name(node.getName())
                        .nodeTypeCode(node.getNodeTypeCode())
                        .status(node.getStatus())
                        .layer(node.getLayer())
                        .positionX(node.getPositionX())
                        .positionY(node.getPositionY())
                        .build())
                .collect(Collectors.toList());
        result.setNodes(nodes);

        // 转换边列表
        List<TopologyGraphDTO.GraphEdgeDTO> edges = graphData.getEdges().stream()
                .map(edge -> TopologyGraphDTO.GraphEdgeDTO.builder()
                        .sourceId(edge.getSourceId())
                        .targetId(edge.getTargetId())
                        .relationshipType(edge.getRelationshipType())
                        .direction(edge.getDirection())
                        .strength(edge.getStrength())
                        .status(edge.getStatus())
                        .build())
                .collect(Collectors.toList());
        result.setEdges(edges);

        return result;
    }

    // ===== 层级团队查询 =====

    @Override
    public HierarchicalTeamDTO queryHierarchicalTeam(HierarchicalTeamQueryRequest request) {
        logger.info("查询层级团队，topologyId: {}", request.getTopologyId());

        // 1. 获取拓扑图信息
        Topology topology = topologyDomainService.getTopologyById(request.getTopologyId())
                .orElseThrow(() -> new IllegalArgumentException("Topology not found: " + request.getTopologyId()));

        // 2. 获取 Global Supervisor Agent（从 agent_bound 表查询）
        AgentDTO globalSupervisorDTO = agentBoundRepository
                .findSupervisorBinding(BoundEntityType.TOPOLOGY, topology.getId(), AgentHierarchyLevel.GLOBAL_SUPERVISOR)
                .flatMap(binding -> agentRepository.findById(binding.getAgentId()))
                .map(this::toAgentDTO)
                .orElse(null);

        // 3. 获取拓扑图的成员节点列表
        List<Topology2NodeRepository.MemberInfo> members = topology2NodeRepository.findMembersByTopologyId(request.getTopologyId());

        if (members.isEmpty()) {
            // 空拓扑图，返回空 teams 列表
            return HierarchicalTeamDTO.builder()
                    .topologyId(topology.getId())
                    .topologyName(topology.getName())
                    .globalSupervisor(globalSupervisorDTO)
                    .teams(new ArrayList<>())
                    .build();
        }

        // 4. 提取所有节点ID
        List<Long> nodeIds = members.stream()
                .map(Topology2NodeRepository.MemberInfo::nodeId)
                .collect(Collectors.toList());

        // 5. 批量获取所有节点关联的 Agent 绑定（从 agent_bound 表查询）
        List<AgentBound> nodeAgentBindings = agentBoundRepository.findByEntityIds(BoundEntityType.NODE, nodeIds);

        // 6. 按节点ID分组
        Map<Long, List<AgentBound>> agentsByNodeId = new HashMap<>();
        for (AgentBound binding : nodeAgentBindings) {
            agentsByNodeId.computeIfAbsent(binding.getEntityId(), k -> new ArrayList<>()).add(binding);
        }

        // 7. 组装 Teams 列表
        List<TeamDTO> teams = new ArrayList<>();
        for (Topology2NodeRepository.MemberInfo member : members) {
            List<AgentBound> nodeAgents = agentsByNodeId.getOrDefault(member.nodeId(), new ArrayList<>());

            // 分离 Supervisor 和 Workers
            AgentDTO supervisor = null;
            List<AgentDTO> workers = new ArrayList<>();

            for (AgentBound binding : nodeAgents) {
                AgentDTO agentDTO = mapBoundToAgentDTO(binding);

                if (binding.getHierarchyLevel() == AgentHierarchyLevel.TEAM_SUPERVISOR) {
                    if (supervisor == null) {
                        // FR-009: 当节点绑定多个 TEAM_SUPERVISOR 时，取创建时间最早的一个
                        // 由于 SQL 已按 created_at 排序，第一个即为最早的
                        supervisor = agentDTO;
                    }
                } else if (binding.getHierarchyLevel() == AgentHierarchyLevel.TEAM_WORKER) {
                    workers.add(agentDTO);
                }
            }

            TeamDTO team = TeamDTO.builder()
                    .nodeId(member.nodeId())
                    .nodeName(member.nodeName())
                    .supervisor(supervisor)
                    .workers(workers)
                    .build();
            teams.add(team);
        }

        // 8. 返回结果
        return HierarchicalTeamDTO.builder()
                .topologyId(topology.getId())
                .topologyName(topology.getName())
                .globalSupervisor(globalSupervisorDTO)
                .teams(teams)
                .build();
    }

    private AgentDTO toAgentDTO(Agent agent) {
        if (agent == null) {
            return null;
        }
        return AgentDTO.builder()
                .id(agent.getId())
                .name(agent.getName())
                .role(agent.getRole() != null ? agent.getRole().name() : null)
                .hierarchyLevel(agent.getHierarchyLevel() != null ? agent.getHierarchyLevel().name() : null)
                .specialty(agent.getSpecialty())
                .model(agent.getModel())
                .promptTemplateId(agent.getPromptTemplateId())
                .promptTemplateName(agent.getPromptTemplateName())
                .temperature(agent.getTemperature())
                .topP(agent.getTopP())
                .maxTokens(agent.getMaxTokens())
                .maxRuntime(agent.getMaxRuntime())
                .warnings(agent.getWarnings())
                .critical(agent.getCritical())
                .createdAt(agent.getCreatedAt())
                .updatedAt(agent.getUpdatedAt())
                .build();
    }

    private AgentDTO mapBoundToAgentDTO(AgentBound binding) {
        return AgentDTO.builder()
                .id(binding.getAgentId())
                .name(binding.getAgentName())
                .role(binding.getAgentRole())
                .hierarchyLevel(binding.getHierarchyLevel() != null ? binding.getHierarchyLevel().name() : null)
                .specialty(binding.getAgentSpecialty())
                .model(binding.getAgentModel())
                .build();
    }
}
