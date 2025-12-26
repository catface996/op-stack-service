package com.catface996.aiops.application.impl.service.topology;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.node.NodeDTO;
import com.catface996.aiops.application.api.dto.topology.TopologyDTO;
import com.catface996.aiops.application.api.dto.topology.TopologyGraphDTO;
import com.catface996.aiops.application.api.dto.topology.request.CreateTopologyRequest;
import com.catface996.aiops.application.api.dto.topology.request.QueryMembersRequest;
import com.catface996.aiops.application.api.dto.topology.request.QueryTopologiesRequest;
import com.catface996.aiops.application.api.dto.topology.request.QueryTopologyGraphRequest;
import com.catface996.aiops.application.api.dto.topology.request.UpdateTopologyRequest;
import com.catface996.aiops.application.api.service.topology.TopologyApplicationService;
import com.catface996.aiops.domain.model.node.Node;
import com.catface996.aiops.domain.model.node.NodeStatus;
import com.catface996.aiops.domain.model.topology.Topology;
import com.catface996.aiops.domain.model.topology.TopologyStatus;
import com.catface996.aiops.domain.model.topology.TopologyGraphData;
import com.catface996.aiops.domain.service.node.NodeDomainService;
import com.catface996.aiops.domain.service.topology2.TopologyDomainService;
import com.catface996.aiops.repository.node.NodeTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
 *   <li>FR-013: 支持 coordinator_agent_id 字段</li>
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

    public TopologyApplicationServiceImpl(@Qualifier("topologyDomainServiceV2") TopologyDomainService topologyDomainService,
                                          NodeDomainService nodeDomainService,
                                          NodeTypeRepository nodeTypeRepository) {
        this.topologyDomainService = topologyDomainService;
        this.nodeDomainService = nodeDomainService;
        this.nodeTypeRepository = nodeTypeRepository;
    }

    @Override
    public TopologyDTO createTopology(CreateTopologyRequest request, Long operatorId, String operatorName) {
        logger.info("创建拓扑图，name: {}, operatorId: {}", request.getName(), operatorId);

        Topology topology = topologyDomainService.createTopology(
                request.getName(),
                request.getDescription(),
                request.getCoordinatorAgentId(),
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
                request.getCoordinatorAgentId(),
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
                request.getKeyword(),
                request.getTopologyId(),
                request.getPage(),
                request.getSize()
        );

        long total = nodeDomainService.countNodes(
                request.getNodeTypeId(),
                status,
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
                .coordinatorAgentId(topology.getCoordinatorAgentId())
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
                .agentTeamId(node.getAgentTeamId())
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
}
