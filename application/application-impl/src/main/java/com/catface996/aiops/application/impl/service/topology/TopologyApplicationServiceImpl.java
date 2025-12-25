package com.catface996.aiops.application.impl.service.topology;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.topology.TopologyDTO;
import com.catface996.aiops.application.api.dto.topology.request.CreateTopologyRequest;
import com.catface996.aiops.application.api.dto.topology.request.QueryTopologiesRequest;
import com.catface996.aiops.application.api.dto.topology.request.UpdateTopologyRequest;
import com.catface996.aiops.application.api.service.topology.TopologyApplicationService;
import com.catface996.aiops.domain.model.resource.Resource;
import com.catface996.aiops.domain.model.resource.ResourceStatus;
import com.catface996.aiops.domain.service.topology.TopologyDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 拓扑图应用服务实现
 *
 * <p>协调领域层完成拓扑图管理业务逻辑，负责 DTO 转换。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001~008: 资源分类体系设计</li>
 *   <li>US1: 查询所有拓扑图</li>
 *   <li>US3: 创建拓扑图</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-25
 */
@Service
public class TopologyApplicationServiceImpl implements TopologyApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(TopologyApplicationServiceImpl.class);

    private final TopologyDomainService topologyDomainService;

    public TopologyApplicationServiceImpl(TopologyDomainService topologyDomainService) {
        this.topologyDomainService = topologyDomainService;
    }

    @Override
    public TopologyDTO createTopology(CreateTopologyRequest request, Long operatorId, String operatorName) {
        logger.info("创建拓扑图，name: {}, operatorId: {}", request.getName(), operatorId);

        Resource topology = topologyDomainService.createTopology(
                request.getName(),
                request.getDescription(),
                operatorId,
                operatorName
        );

        return toDTO(topology);
    }

    @Override
    public PageResult<TopologyDTO> listTopologies(QueryTopologiesRequest request) {
        ResourceStatus status = parseStatus(request.getStatus());

        List<Resource> topologies = topologyDomainService.listTopologies(
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

        Resource topology = topologyDomainService.updateTopology(
                topologyId,
                request.getName(),
                request.getDescription(),
                request.getVersion(),
                operatorId,
                operatorName
        );

        return toDTO(topology);
    }

    @Override
    public void deleteTopology(Long topologyId, Long operatorId, String operatorName) {
        logger.info("删除拓扑图，topologyId: {}, operatorId: {}", topologyId, operatorId);
        topologyDomainService.deleteTopology(topologyId, operatorId, operatorName);
    }

    // ===== DTO 转换方法 =====

    private TopologyDTO toDTO(Resource resource) {
        if (resource == null) {
            return null;
        }

        TopologyDTO.TopologyDTOBuilder builder = TopologyDTO.builder()
                .id(resource.getId())
                .name(resource.getName())
                .description(resource.getDescription())
                .attributes(resource.getAttributes())
                .version(resource.getVersion())
                .createdBy(resource.getCreatedBy())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt());

        if (resource.getStatus() != null) {
            builder.status(resource.getStatus().name())
                    .statusDisplay(resource.getStatus().getDescription());
        }

        // 获取成员数量
        int memberCount = topologyDomainService.countMembers(resource.getId());
        builder.memberCount(memberCount);

        return builder.build();
    }

    private ResourceStatus parseStatus(String status) {
        if (status == null || status.isEmpty()) {
            return null;
        }
        try {
            return ResourceStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            logger.warn("无效的资源状态: {}", status);
            return null;
        }
    }
}
