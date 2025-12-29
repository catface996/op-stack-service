package com.catface996.aiops.application.impl.service.node;

import com.catface996.aiops.application.api.dto.agent.AgentDTO;
import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.node.NodeAgentRelationDTO;
import com.catface996.aiops.application.api.dto.node.NodeDTO;
import com.catface996.aiops.application.api.dto.node.NodeTypeDTO;
import com.catface996.aiops.application.api.dto.node.request.CreateNodeRequest;
import com.catface996.aiops.application.api.dto.node.request.ListUnboundAgentsRequest;
import com.catface996.aiops.application.api.dto.node.request.QueryNodesRequest;
import com.catface996.aiops.application.api.dto.node.request.UpdateNodeRequest;
import com.catface996.aiops.application.api.service.node.NodeApplicationService;
import com.catface996.aiops.domain.model.agent.Agent;
import com.catface996.aiops.domain.model.agent.AgentHierarchyLevel;
import com.catface996.aiops.domain.model.node.Node;
import com.catface996.aiops.domain.model.node.NodeAgentRelation;
import com.catface996.aiops.domain.model.node.NodeStatus;
import com.catface996.aiops.domain.model.node.NodeType;
import com.catface996.aiops.domain.service.node.NodeDomainService;
import com.catface996.aiops.repository.agent.AgentRepository;
import com.catface996.aiops.repository.node.NodeAgentRelationRepository;
import com.catface996.aiops.repository.node.NodeTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资源节点应用服务实现
 *
 * <p>协调领域层完成资源节点管理业务逻辑，负责 DTO 转换。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: resource 表拆分为 topology 表和 node 表</li>
 *   <li>FR-005: 节点 API 保持接口契约不变</li>
 *   <li>US2: 查询所有节点</li>
 *   <li>US4: 节点管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Service
public class NodeApplicationServiceImpl implements NodeApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(NodeApplicationServiceImpl.class);

    private final NodeDomainService nodeDomainService;
    private final NodeTypeRepository nodeTypeRepository;
    private final NodeAgentRelationRepository nodeAgentRelationRepository;
    private final AgentRepository agentRepository;

    public NodeApplicationServiceImpl(NodeDomainService nodeDomainService,
                                      NodeTypeRepository nodeTypeRepository,
                                      NodeAgentRelationRepository nodeAgentRelationRepository,
                                      AgentRepository agentRepository) {
        this.nodeDomainService = nodeDomainService;
        this.nodeTypeRepository = nodeTypeRepository;
        this.nodeAgentRelationRepository = nodeAgentRelationRepository;
        this.agentRepository = agentRepository;
    }

    @Override
    public NodeDTO createNode(CreateNodeRequest request, Long operatorId, String operatorName) {
        logger.info("创建节点，name: {}, operatorId: {}", request.getName(), operatorId);

        Node node = nodeDomainService.createNode(
                request.getName(),
                request.getDescription(),
                request.getNodeTypeId(),
                request.getAgentTeamId(),
                request.getAttributes(),
                operatorId
        );

        return toDTO(node);
    }

    @Override
    public PageResult<NodeDTO> listNodes(QueryNodesRequest request) {
        NodeStatus status = parseStatus(request.getStatus());

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
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtos, request.getPage(), request.getSize(), total);
    }

    @Override
    public NodeDTO getNodeById(Long nodeId) {
        return nodeDomainService.getNodeById(nodeId)
                .map(this::toDTO)
                .orElse(null);
    }

    @Override
    public NodeDTO updateNode(Long nodeId, UpdateNodeRequest request,
                              Long operatorId, String operatorName) {
        logger.info("更新节点，nodeId: {}, operatorId: {}", nodeId, operatorId);

        Node node = nodeDomainService.updateNode(
                nodeId,
                request.getName(),
                request.getDescription(),
                request.getAgentTeamId(),
                request.getAttributes(),
                request.getVersion(),
                operatorId
        );

        return toDTO(node);
    }

    @Override
    public void deleteNode(Long nodeId, Long operatorId, String operatorName) {
        logger.info("删除节点，nodeId: {}, operatorId: {}", nodeId, operatorId);
        nodeDomainService.deleteNode(nodeId, operatorId);
    }

    @Override
    public List<NodeTypeDTO> listNodeTypes() {
        return nodeDomainService.listNodeTypes().stream()
                .map(this::toNodeTypeDTO)
                .collect(Collectors.toList());
    }

    // ===== DTO 转换方法 =====

    private NodeDTO toDTO(Node node) {
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

    private NodeTypeDTO toNodeTypeDTO(NodeType nodeType) {
        if (nodeType == null) {
            return null;
        }
        return NodeTypeDTO.builder()
                .id(nodeType.getId())
                .code(nodeType.getCode())
                .name(nodeType.getName())
                .description(nodeType.getDescription())
                .icon(nodeType.getIcon())
                .createdAt(nodeType.getCreatedAt())
                .updatedAt(nodeType.getUpdatedAt())
                .build();
    }

    private NodeStatus parseStatus(String status) {
        if (status == null || status.isEmpty()) {
            return null;
        }
        try {
            return NodeStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            logger.warn("无效的节点状态: {}", status);
            return null;
        }
    }

    // ==================== Node-Agent 绑定相关方法实现 ====================

    @Override
    @Transactional
    public NodeAgentRelationDTO bindAgent(Long nodeId, Long agentId) {
        logger.info("绑定 Agent 到节点，nodeId: {}, agentId: {}", nodeId, agentId);

        // 验证节点是否存在
        if (nodeDomainService.getNodeById(nodeId).isEmpty()) {
            throw new IllegalArgumentException("节点不存在");
        }

        // 验证 Agent 是否存在
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent不存在"));

        // 验证 Agent 层级必须是 TEAM_SUPERVISOR 或 TEAM_WORKER（不能是 GLOBAL_SUPERVISOR）
        AgentHierarchyLevel level = agent.getHierarchyLevel();
        if (level == null || level == AgentHierarchyLevel.GLOBAL_SUPERVISOR) {
            throw new IllegalArgumentException("只能绑定层级为 TEAM_SUPERVISOR 或 TEAM_WORKER 的 Agent");
        }

        // 检查是否已绑定
        if (nodeAgentRelationRepository.existsByNodeIdAndAgentId(nodeId, agentId)) {
            throw new IllegalStateException("该节点与Agent已绑定");
        }

        // 创建绑定关系
        NodeAgentRelation relation = NodeAgentRelation.create(nodeId, agentId);
        nodeAgentRelationRepository.save(relation);

        logger.info("绑定成功，relationId: {}", relation.getId());
        return toRelationDTO(relation);
    }

    @Override
    @Transactional
    public void unbindAgent(Long nodeId, Long agentId) {
        logger.info("解绑 Agent 与节点，nodeId: {}, agentId: {}", nodeId, agentId);

        // 查找绑定关系
        NodeAgentRelation relation = nodeAgentRelationRepository
                .findByNodeIdAndAgentId(nodeId, agentId)
                .orElseThrow(() -> new IllegalArgumentException("绑定关系不存在"));

        // 软删除
        nodeAgentRelationRepository.softDelete(relation.getId());
        logger.info("解绑成功，relationId: {}", relation.getId());
    }

    @Override
    public List<AgentDTO> listAgentsByNode(Long nodeId) {
        logger.info("查询节点关联的 Agent 列表，nodeId: {}", nodeId);

        // 查询关联的 Agent ID 列表
        List<Long> agentIds = nodeAgentRelationRepository.findAgentIdsByNodeId(nodeId);
        if (agentIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询 Agent 详情
        List<AgentDTO> agents = new ArrayList<>();
        for (Long agentId : agentIds) {
            agentRepository.findById(agentId).ifPresent(agent -> agents.add(toAgentDTO(agent)));
        }

        return agents;
    }

    @Override
    public List<NodeDTO> listNodesByAgent(Long agentId) {
        logger.info("查询 Agent 关联的节点列表，agentId: {}", agentId);

        // 查询关联的 Node ID 列表
        List<Long> nodeIds = nodeAgentRelationRepository.findNodeIdsByAgentId(agentId);
        if (nodeIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询 Node 详情
        List<NodeDTO> nodes = new ArrayList<>();
        for (Long nodeId : nodeIds) {
            nodeDomainService.getNodeById(nodeId).ifPresent(node -> nodes.add(toDTO(node)));
        }

        return nodes;
    }

    // ===== Node-Agent 绑定 DTO 转换方法 =====

    private NodeAgentRelationDTO toRelationDTO(NodeAgentRelation relation) {
        if (relation == null) {
            return null;
        }
        NodeAgentRelationDTO dto = new NodeAgentRelationDTO();
        dto.setId(relation.getId());
        dto.setNodeId(relation.getNodeId());
        dto.setAgentId(relation.getAgentId());
        dto.setCreatedAt(relation.getCreatedAt());
        return dto;
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
                .promptTemplateId(agent.getPromptTemplateId())
                .promptTemplateName(agent.getPromptTemplateName())
                .model(agent.getModel())
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

    @Override
    public PageResult<AgentDTO> listUnboundAgents(ListUnboundAgentsRequest request) {
        logger.info("查询未绑定到节点的 Agent 列表，nodeId: {}, keyword: {}", request.getNodeId(), request.getKeyword());

        // 1. 获取已绑定到该节点的 Agent ID 列表
        List<Long> boundAgentIds = nodeAgentRelationRepository.findAgentIdsByNodeId(request.getNodeId());

        // 2. 查询未绑定的 Agent（排除已绑定的）
        List<Agent> unboundAgents = agentRepository.findUnboundByNodeId(
                request.getNodeId(),
                boundAgentIds,
                request.getKeyword(),
                request.getPage(),
                request.getSize()
        );

        // 3. 统计未绑定的 Agent 总数
        long total = agentRepository.countUnboundByNodeId(
                request.getNodeId(),
                boundAgentIds,
                request.getKeyword()
        );

        // 4. 转换为 DTO
        List<AgentDTO> dtos = unboundAgents.stream()
                .map(this::toAgentDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtos, request.getPage(), request.getSize(), total);
    }
}
