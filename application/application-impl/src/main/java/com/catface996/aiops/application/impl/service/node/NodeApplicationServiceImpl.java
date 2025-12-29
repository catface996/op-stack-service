package com.catface996.aiops.application.impl.service.node;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.node.NodeDTO;
import com.catface996.aiops.application.api.dto.node.NodeTypeDTO;
import com.catface996.aiops.application.api.dto.node.request.CreateNodeRequest;
import com.catface996.aiops.application.api.dto.node.request.QueryNodesRequest;
import com.catface996.aiops.application.api.dto.node.request.UpdateNodeRequest;
import com.catface996.aiops.application.api.service.node.NodeApplicationService;
import com.catface996.aiops.domain.model.node.Node;
import com.catface996.aiops.domain.model.node.NodeStatus;
import com.catface996.aiops.domain.model.node.NodeType;
import com.catface996.aiops.domain.service.node.NodeDomainService;
import com.catface996.aiops.repository.node.NodeTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
 * <p>Note: Node-Agent 绑定相关方法已移至 AgentBoundApplicationService (Feature 040)</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Service
public class NodeApplicationServiceImpl implements NodeApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(NodeApplicationServiceImpl.class);

    private final NodeDomainService nodeDomainService;
    private final NodeTypeRepository nodeTypeRepository;

    public NodeApplicationServiceImpl(NodeDomainService nodeDomainService,
                                      NodeTypeRepository nodeTypeRepository) {
        this.nodeDomainService = nodeDomainService;
        this.nodeTypeRepository = nodeTypeRepository;
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
}
