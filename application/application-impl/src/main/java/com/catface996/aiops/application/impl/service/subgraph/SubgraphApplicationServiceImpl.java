package com.catface996.aiops.application.impl.service.subgraph;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.relationship.RelationshipDTO;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphDTO;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphDetailDTO;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphPermissionDTO;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphResourceDTO;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphResourcesWithRelationsDTO;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphTopologyDTO;
import com.catface996.aiops.application.api.dto.subgraph.request.*;
import com.catface996.aiops.application.api.service.subgraph.SubgraphApplicationService;
import com.catface996.aiops.domain.model.relationship.Relationship;
import com.catface996.aiops.domain.model.resource.Resource;
import com.catface996.aiops.domain.model.subgraph.PermissionRole;
import com.catface996.aiops.domain.model.subgraph.Subgraph;
import com.catface996.aiops.domain.model.subgraph.SubgraphPermission;
import com.catface996.aiops.domain.model.subgraph.SubgraphResource;
import com.catface996.aiops.domain.model.subgraph.SubgraphTopology;
import com.catface996.aiops.domain.service.subgraph.SubgraphDomainService;
import com.catface996.aiops.repository.resource.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 子图应用服务实现
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>任务14-19: 应用服务实现</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@Service
public class SubgraphApplicationServiceImpl implements SubgraphApplicationService {

    private final SubgraphDomainService subgraphDomainService;
    private final ResourceRepository resourceRepository;

    public SubgraphApplicationServiceImpl(SubgraphDomainService subgraphDomainService,
                                          ResourceRepository resourceRepository) {
        this.subgraphDomainService = subgraphDomainService;
        this.resourceRepository = resourceRepository;
    }

    // ==================== 子图生命周期 ====================

    @Override
    @Transactional
    public SubgraphDTO createSubgraph(CreateSubgraphRequest request, Long operatorId, String operatorName) {
        Subgraph subgraph = subgraphDomainService.createSubgraph(
                request.getName(),
                request.getDescription(),
                request.getTags(),
                request.getMetadata(),
                operatorId,
                operatorName
        );

        SubgraphDTO dto = toDTO(subgraph);
        dto.setResourceCount(0); // 新创建的子图没有资源
        return dto;
    }

    @Override
    @Transactional
    public SubgraphDTO updateSubgraph(Long subgraphId, UpdateSubgraphRequest request,
                                      Long operatorId, String operatorName) {
        Subgraph subgraph = subgraphDomainService.updateSubgraph(
                subgraphId,
                request.getName(),
                request.getDescription(),
                request.getTags(),
                request.getMetadata(),
                request.getVersion(),
                operatorId,
                operatorName
        );

        SubgraphDTO dto = toDTO(subgraph);
        dto.setResourceCount(subgraphDomainService.countResources(subgraphId));
        return dto;
    }

    @Override
    @Transactional
    public void deleteSubgraph(Long subgraphId, Long operatorId, String operatorName) {
        subgraphDomainService.deleteSubgraph(subgraphId, operatorId, operatorName);
    }

    // ==================== 子图查询 ====================

    @Override
    public PageResult<SubgraphDTO> listSubgraphs(ListSubgraphsRequest request, Long userId) {
        int page = request.getPage() != null ? request.getPage() : 1;
        int size = request.getSize() != null ? request.getSize() : 20;

        List<Subgraph> subgraphs;
        long total;

        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            // 关键词搜索
            subgraphs = subgraphDomainService.searchSubgraphs(request.getKeyword(), userId, page, size);
            total = subgraphDomainService.countSearchSubgraphs(request.getKeyword(), userId);
        } else if (request.getTags() != null && !request.getTags().isEmpty()) {
            // 标签过滤
            subgraphs = subgraphDomainService.filterByTags(request.getTags(), userId, page, size);
            total = subgraphs.size(); // 简化实现
        } else if (request.getOwnerId() != null) {
            // 按所有者过滤
            subgraphs = subgraphDomainService.filterByOwner(request.getOwnerId(), userId, page, size);
            total = subgraphs.size(); // 简化实现
        } else {
            // 默认列表
            subgraphs = subgraphDomainService.listSubgraphs(userId, page, size);
            total = subgraphDomainService.countSubgraphs(userId);
        }

        List<SubgraphDTO> dtos = subgraphs.stream()
                .map(subgraph -> {
                    SubgraphDTO dto = toDTO(subgraph);
                    dto.setResourceCount(subgraphDomainService.countResources(subgraph.getId()));
                    return dto;
                })
                .collect(Collectors.toList());

        return PageResult.of(dtos, page, size, total);
    }

    @Override
    public SubgraphDetailDTO getSubgraphDetail(Long subgraphId, Long userId) {
        Subgraph subgraph = subgraphDomainService.getSubgraphDetail(subgraphId, userId)
                .orElse(null);

        if (subgraph == null) {
            return null;
        }

        List<SubgraphPermission> permissions = subgraphDomainService.getPermissions(subgraphId);
        List<Long> resourceIds = subgraphDomainService.getResourceIds(subgraphId, userId);

        return toDetailDTO(subgraph, permissions, resourceIds);
    }

    // ==================== 权限管理 ====================

    @Override
    @Transactional
    public void addPermission(Long subgraphId, UpdatePermissionRequest request,
                              Long operatorId, String operatorName) {
        PermissionRole role = PermissionRole.valueOf(request.getRole());
        subgraphDomainService.addPermission(subgraphId, request.getUserId(), role, operatorId, operatorName);
    }

    @Override
    @Transactional
    public void removePermission(Long subgraphId, Long userId, Long operatorId, String operatorName) {
        subgraphDomainService.removePermission(subgraphId, userId, operatorId, operatorName);
    }

    // ==================== 资源节点管理 ====================

    @Override
    @Transactional
    public void addResources(Long subgraphId, AddResourcesRequest request,
                             Long operatorId, String operatorName) {
        subgraphDomainService.addResources(subgraphId, request.getResourceIds(), operatorId, operatorName);
    }

    @Override
    @Transactional
    public void removeResources(Long subgraphId, RemoveResourcesRequest request,
                                Long operatorId, String operatorName) {
        subgraphDomainService.removeResources(subgraphId, request.getResourceIds(), operatorId, operatorName);
    }

    @Override
    public PageResult<SubgraphResourceDTO> getSubgraphResources(Long subgraphId,
                                                                 ListSubgraphResourcesRequest request,
                                                                 Long userId) {
        int page = request.getPage() != null ? request.getPage() : 1;
        int size = request.getSize() != null ? request.getSize() : 20;

        // 获取分页的资源关联记录
        List<SubgraphResource> subgraphResources = subgraphDomainService.getSubgraphResourcesPaged(
                subgraphId, userId, page, size);

        // 获取总数
        long total = subgraphDomainService.countResources(subgraphId);

        // 批量查询资源详情
        List<Long> resourceIds = subgraphResources.stream()
                .map(SubgraphResource::getResourceId)
                .collect(Collectors.toList());

        Map<Long, Resource> resourceMap = resourceIds.stream()
                .map(resourceRepository::findByIdWithType)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toMap(Resource::getId, r -> r, (a, b) -> a));

        // 转换为DTO
        List<SubgraphResourceDTO> dtos = subgraphResources.stream()
                .map(sr -> toSubgraphResourceDTO(sr, resourceMap.get(sr.getResourceId())))
                .collect(Collectors.toList());

        return PageResult.of(dtos, page, size, total);
    }

    // ==================== 拓扑查询 ====================

    @Override
    public SubgraphTopologyDTO getSubgraphTopology(Long subgraphId, Long userId) {
        SubgraphTopology topology = subgraphDomainService.getSubgraphTopology(subgraphId, userId);
        return toTopologyDTO(topology);
    }

    @Override
    public SubgraphResourcesWithRelationsDTO getSubgraphResourcesWithRelations(Long subgraphId, Long userId) {
        // 获取子图拓扑（包含资源ID和关系）
        SubgraphTopology topology = subgraphDomainService.getSubgraphTopology(subgraphId, userId);
        if (topology == null) {
            return null;
        }

        // 获取所有资源关联记录
        List<SubgraphResource> subgraphResources = subgraphDomainService.getSubgraphResourcesPaged(
                subgraphId, userId, 1, Integer.MAX_VALUE);

        // 批量查询资源详情
        List<Long> resourceIds = subgraphResources.stream()
                .map(SubgraphResource::getResourceId)
                .collect(Collectors.toList());

        Map<Long, Resource> resourceMap = resourceIds.stream()
                .map(resourceRepository::findByIdWithType)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toMap(Resource::getId, r -> r, (a, b) -> a));

        // 转换资源为DTO
        List<SubgraphResourceDTO> resourceDTOs = subgraphResources.stream()
                .map(sr -> toSubgraphResourceDTO(sr, resourceMap.get(sr.getResourceId())))
                .collect(Collectors.toList());

        // 转换关系为DTO
        List<RelationshipDTO> relationshipDTOs = topology.getRelationships().stream()
                .map(this::toRelationshipDTO)
                .collect(Collectors.toList());

        return SubgraphResourcesWithRelationsDTO.builder()
                .subgraphId(topology.getSubgraphId())
                .subgraphName(topology.getSubgraphName())
                .resources(resourceDTOs)
                .relationships(relationshipDTOs)
                .nodeCount(resourceDTOs.size())
                .edgeCount(relationshipDTOs.size())
                .build();
    }

    // ==================== DTO 转换方法 ====================

    private SubgraphDTO toDTO(Subgraph subgraph) {
        if (subgraph == null) {
            return null;
        }
        return SubgraphDTO.builder()
                .id(subgraph.getId())
                .name(subgraph.getName())
                .description(subgraph.getDescription())
                .tags(subgraph.getTags())
                .metadata(subgraph.getMetadata())
                .createdBy(subgraph.getCreatedBy())
                .createdAt(subgraph.getCreatedAt())
                .updatedAt(subgraph.getUpdatedAt())
                .version(subgraph.getVersion())
                .build();
    }

    private SubgraphDetailDTO toDetailDTO(Subgraph subgraph, List<SubgraphPermission> permissions,
                                          List<Long> resourceIds) {
        if (subgraph == null) {
            return null;
        }

        List<SubgraphPermissionDTO> permissionDTOs = permissions.stream()
                .map(this::toPermissionDTO)
                .collect(Collectors.toList());

        return SubgraphDetailDTO.builder()
                .id(subgraph.getId())
                .name(subgraph.getName())
                .description(subgraph.getDescription())
                .tags(subgraph.getTags())
                .metadata(subgraph.getMetadata())
                .createdBy(subgraph.getCreatedBy())
                .createdAt(subgraph.getCreatedAt())
                .updatedAt(subgraph.getUpdatedAt())
                .version(subgraph.getVersion())
                .permissions(permissionDTOs)
                .resourceIds(resourceIds)
                .resourceCount(resourceIds.size())
                .build();
    }

    private SubgraphPermissionDTO toPermissionDTO(SubgraphPermission permission) {
        if (permission == null) {
            return null;
        }
        return SubgraphPermissionDTO.builder()
                .id(permission.getId())
                .subgraphId(permission.getSubgraphId())
                .userId(permission.getUserId())
                .role(permission.getRole().name())
                .grantedAt(permission.getGrantedAt())
                .grantedBy(permission.getGrantedBy())
                .build();
    }

    private SubgraphTopologyDTO toTopologyDTO(SubgraphTopology topology) {
        if (topology == null) {
            return null;
        }

        List<RelationshipDTO> relationshipDTOs = topology.getRelationships().stream()
                .map(this::toRelationshipDTO)
                .collect(Collectors.toList());

        return SubgraphTopologyDTO.builder()
                .subgraphId(topology.getSubgraphId())
                .subgraphName(topology.getSubgraphName())
                .resourceIds(topology.getResourceIds())
                .relationships(relationshipDTOs)
                .nodeCount(topology.getNodeCount())
                .edgeCount(topology.getEdgeCount())
                .build();
    }

    private RelationshipDTO toRelationshipDTO(Relationship relationship) {
        if (relationship == null) {
            return null;
        }
        return RelationshipDTO.builder()
                .id(relationship.getId())
                .sourceResourceId(relationship.getSourceResourceId())
                .targetResourceId(relationship.getTargetResourceId())
                .relationshipType(relationship.getRelationshipType() != null ?
                        relationship.getRelationshipType().name() : null)
                .direction(relationship.getDirection() != null ?
                        relationship.getDirection().name() : null)
                .strength(relationship.getStrength() != null ?
                        relationship.getStrength().name() : null)
                .status(relationship.getStatus() != null ?
                        relationship.getStatus().name() : null)
                .description(relationship.getDescription())
                .createdAt(relationship.getCreatedAt())
                .updatedAt(relationship.getUpdatedAt())
                .sourceResourceName(relationship.getSourceResourceName())
                .targetResourceName(relationship.getTargetResourceName())
                .build();
    }

    private SubgraphResourceDTO toSubgraphResourceDTO(SubgraphResource subgraphResource, Resource resource) {
        if (subgraphResource == null) {
            return null;
        }
        SubgraphResourceDTO.SubgraphResourceDTOBuilder builder = SubgraphResourceDTO.builder()
                .id(subgraphResource.getId())
                .resourceId(subgraphResource.getResourceId())
                .subgraphId(subgraphResource.getSubgraphId())
                .addedAt(subgraphResource.getAddedAt())
                .addedBy(subgraphResource.getAddedBy());

        if (resource != null) {
            builder.resourceName(resource.getName())
                    .resourceStatus(resource.getStatus() != null ? resource.getStatus().name() : null);
            if (resource.getResourceType() != null) {
                builder.resourceType(resource.getResourceType().getName());
            }
        }

        return builder.build();
    }
}
