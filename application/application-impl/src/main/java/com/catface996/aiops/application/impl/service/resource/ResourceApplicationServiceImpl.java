package com.catface996.aiops.application.impl.service.resource;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.resource.ResourceAuditLogDTO;
import com.catface996.aiops.application.api.dto.resource.ResourceDTO;
import com.catface996.aiops.application.api.dto.resource.ResourceTypeDTO;
import com.catface996.aiops.application.api.dto.resource.request.CreateResourceRequest;
import com.catface996.aiops.application.api.dto.resource.request.DeleteResourceRequest;
import com.catface996.aiops.application.api.dto.resource.request.ListResourcesRequest;
import com.catface996.aiops.application.api.dto.resource.request.UpdateResourceRequest;
import com.catface996.aiops.application.api.dto.resource.request.UpdateResourceStatusRequest;
import com.catface996.aiops.application.api.service.resource.ResourceApplicationService;
import com.catface996.aiops.domain.constant.ResourceTypeConstants;
import com.catface996.aiops.domain.model.resource.OperationType;
import com.catface996.aiops.domain.model.resource.Resource;
import com.catface996.aiops.domain.model.resource.ResourceAuditLog;
import com.catface996.aiops.domain.model.resource.ResourceStatus;
import com.catface996.aiops.domain.model.resource.ResourceType;
import com.catface996.aiops.domain.service.resource.AuditLogService;
import com.catface996.aiops.domain.service.resource.ResourceDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 资源应用服务实现
 *
 * <p>协调领域层完成资源管理业务逻辑，负责DTO转换。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-001~028: 资源管理功能</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Service
public class ResourceApplicationServiceImpl implements ResourceApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceApplicationServiceImpl.class);

    private final ResourceDomainService resourceDomainService;
    private final AuditLogService auditLogService;

    public ResourceApplicationServiceImpl(ResourceDomainService resourceDomainService,
                                          AuditLogService auditLogService) {
        this.resourceDomainService = resourceDomainService;
        this.auditLogService = auditLogService;
    }

    @Override
    public ResourceDTO createResource(CreateResourceRequest request, Long operatorId, String operatorName) {
        logger.info("创建资源，name: {}, operatorId: {}", request.getName(), operatorId);

        // 禁止通过资源API创建SUBGRAPH类型，应使用拓扑图API
        ResourceType resourceType = resourceDomainService.getResourceTypeById(request.getResourceTypeId())
                .orElseThrow(() -> new IllegalArgumentException("资源类型不存在: " + request.getResourceTypeId()));

        if (ResourceTypeConstants.isTopologyType(resourceType.getCode())) {
            throw new IllegalArgumentException("禁止通过资源API创建拓扑图类型，请使用 /api/v1/topologies/create 接口");
        }

        Resource resource = resourceDomainService.createResource(
                request.getName(),
                request.getDescription(),
                request.getResourceTypeId(),
                request.getAttributes(),
                operatorId,
                operatorName
        );

        return toDTO(resource);
    }

    @Override
    public PageResult<ResourceDTO> listResources(ListResourcesRequest request) {
        ResourceStatus status = null;
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            try {
                status = ResourceStatus.valueOf(request.getStatus());
            } catch (IllegalArgumentException e) {
                logger.warn("无效的资源状态: {}", request.getStatus());
            }
        }

        // 使用 listResourceNodes 方法，自动排除 SUBGRAPH 类型（拓扑图）
        // 拓扑图应通过 /api/v1/topologies/query 接口查询
        List<Resource> resources = resourceDomainService.listResourceNodes(
                request.getResourceTypeId(),
                status,
                request.getKeyword(),
                request.getPage(),
                request.getSize()
        );

        long total = resourceDomainService.countResourceNodes(
                request.getResourceTypeId(),
                status,
                request.getKeyword()
        );

        List<ResourceDTO> dtos = resources.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtos, request.getPage(), request.getSize(), total);
    }

    @Override
    public ResourceDTO getResourceById(Long resourceId) {
        return resourceDomainService.getResourceByIdWithType(resourceId)
                .map(this::toDTO)
                .orElse(null);
    }

    @Override
    public ResourceDTO updateResource(Long resourceId, UpdateResourceRequest request,
                                      Long operatorId, String operatorName) {
        logger.info("更新资源，resourceId: {}, operatorId: {}", resourceId, operatorId);

        Resource resource = resourceDomainService.updateResource(
                resourceId,
                request.getName(),
                request.getDescription(),
                request.getAttributes(),
                request.getVersion(),
                operatorId,
                operatorName
        );

        return toDTO(resource);
    }

    @Override
    public void deleteResource(Long resourceId, DeleteResourceRequest request,
                               Long operatorId, String operatorName) {
        logger.info("删除资源，resourceId: {}, operatorId: {}", resourceId, operatorId);

        resourceDomainService.deleteResource(resourceId, request.getConfirmName(), operatorId, operatorName);
    }

    @Override
    public ResourceDTO updateResourceStatus(Long resourceId, UpdateResourceStatusRequest request,
                                            Long operatorId, String operatorName) {
        logger.info("更新资源状态，resourceId: {}, newStatus: {}, operatorId: {}",
                resourceId, request.getStatus(), operatorId);

        ResourceStatus newStatus;
        try {
            newStatus = ResourceStatus.valueOf(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的资源状态: " + request.getStatus());
        }

        Resource resource = resourceDomainService.updateResourceStatus(
                resourceId,
                newStatus,
                request.getVersion(),
                operatorId,
                operatorName
        );

        return toDTO(resource);
    }

    @Override
    public PageResult<ResourceAuditLogDTO> getResourceAuditLogs(Long resourceId, int page, int size) {
        List<ResourceAuditLog> logs = resourceDomainService.getAuditLogs(resourceId, page, size);
        long total = auditLogService.countAuditLogs(resourceId);

        List<ResourceAuditLogDTO> dtos = logs.stream()
                .map(this::toAuditLogDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtos, page, size, total);
    }

    @Override
    public List<ResourceTypeDTO> getAllResourceTypes() {
        // 过滤掉 SUBGRAPH 类型，拓扑图类型不应显示在资源类型列表中
        return resourceDomainService.getAllResourceTypes().stream()
                .filter(type -> !ResourceTypeConstants.isTopologyType(type.getCode()))
                .map(this::toResourceTypeDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResourceTypeDTO getResourceTypeById(Long resourceTypeId) {
        return resourceDomainService.getResourceTypeById(resourceTypeId)
                .map(this::toResourceTypeDTO)
                .orElse(null);
    }

    @Override
    public boolean checkPermission(Long resourceId, Long userId, boolean isAdmin) {
        return resourceDomainService.checkOwnerPermission(resourceId, userId, isAdmin);
    }

    // ===== DTO 转换方法 =====

    private ResourceDTO toDTO(Resource resource) {
        if (resource == null) {
            return null;
        }

        ResourceDTO.ResourceDTOBuilder builder = ResourceDTO.builder()
                .id(resource.getId())
                .name(resource.getName())
                .description(resource.getDescription())
                .resourceTypeId(resource.getResourceTypeId())
                .attributes(resource.getAttributes())
                .version(resource.getVersion())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .createdBy(resource.getCreatedBy());

        if (resource.getStatus() != null) {
            builder.status(resource.getStatus().name())
                    .statusDisplay(resource.getStatus().getDescription());
        }

        if (resource.getResourceType() != null) {
            builder.resourceTypeName(resource.getResourceType().getName())
                    .resourceTypeCode(resource.getResourceType().getCode());
        }

        return builder.build();
    }

    private ResourceTypeDTO toResourceTypeDTO(ResourceType type) {
        if (type == null) {
            return null;
        }

        return ResourceTypeDTO.builder()
                .id(type.getId())
                .code(type.getCode())
                .name(type.getName())
                .description(type.getDescription())
                .icon(type.getIcon())
                .systemPreset(type.getIsSystem())
                .build();
    }

    private ResourceAuditLogDTO toAuditLogDTO(ResourceAuditLog log) {
        if (log == null) {
            return null;
        }

        ResourceAuditLogDTO.ResourceAuditLogDTOBuilder builder = ResourceAuditLogDTO.builder()
                .id(log.getId())
                .resourceId(log.getResourceId())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .operatorId(log.getOperatorId())
                .operatorName(log.getOperatorName())
                .operatedAt(log.getCreatedAt());

        if (log.getOperation() != null) {
            builder.operation(log.getOperation().name())
                    .operationDisplay(log.getOperation().getDescription());
        }

        return builder.build();
    }
}
