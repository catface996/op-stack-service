package com.catface996.aiops.domain.impl.service.subgraph;

import com.catface996.aiops.common.enums.SubgraphErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.relationship.Relationship;
import com.catface996.aiops.domain.model.subgraph.PermissionRole;
import com.catface996.aiops.domain.model.subgraph.Subgraph;
import com.catface996.aiops.domain.model.subgraph.SubgraphPermission;
import com.catface996.aiops.domain.model.subgraph.SubgraphTopology;
import com.catface996.aiops.domain.service.relationship.RelationshipDomainService;
import com.catface996.aiops.domain.service.subgraph.SubgraphDomainService;
import com.catface996.aiops.repository.subgraph.SubgraphRepository;
import com.catface996.aiops.repository.subgraph.SubgraphResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 子图领域服务实现类
 *
 * <p>实现子图管理的核心业务逻辑，协调多个Repository。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>子图CRUD操作</li>
 *   <li>权限管理</li>
 *   <li>资源节点管理</li>
 *   <li>审计日志记录</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求1-10: 子图CRUD、权限管理、资源节点管理、安全审计</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@Service
public class SubgraphDomainServiceImpl implements SubgraphDomainService {

    private static final Logger logger = LoggerFactory.getLogger(SubgraphDomainServiceImpl.class);

    private final SubgraphRepository subgraphRepository;
    private final SubgraphResourceRepository subgraphResourceRepository;
    private final RelationshipDomainService relationshipDomainService;

    public SubgraphDomainServiceImpl(SubgraphRepository subgraphRepository,
                                     SubgraphResourceRepository subgraphResourceRepository,
                                     RelationshipDomainService relationshipDomainService) {
        this.subgraphRepository = subgraphRepository;
        this.subgraphResourceRepository = subgraphResourceRepository;
        this.relationshipDomainService = relationshipDomainService;
    }

    // ==================== 子图生命周期 ====================

    @Override
    @Transactional
    public Subgraph createSubgraph(String name, String description, List<String> tags,
                                   Map<String, String> metadata, Long creatorId, String creatorName) {
        logger.info("创建子图，name: {}, creatorId: {}", name, creatorId);

        // 1. 参数验证
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("子图名称不能为空");
        }
        if (creatorId == null) {
            throw new IllegalArgumentException("创建者ID不能为空");
        }

        // 2. 验证子图名称全局唯一
        if (subgraphRepository.existsByName(name.trim())) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_NAME_CONFLICT);
        }

        // 3. 创建子图实体
        Subgraph subgraph = Subgraph.create(name.trim(), description, tags, metadata, creatorId);

        // 4. 保存子图
        Subgraph savedSubgraph = subgraphRepository.save(subgraph);
        logger.info("子图创建成功，subgraphId: {}", savedSubgraph.getId());

        // 5. 自动为创建者分配Owner权限
        SubgraphPermission ownerPermission = SubgraphPermission.createOwner(
                savedSubgraph.getId(), creatorId, creatorId);
        subgraphRepository.savePermission(ownerPermission);
        logger.info("为创建者分配Owner权限，subgraphId: {}, userId: {}", savedSubgraph.getId(), creatorId);

        // 6. 记录创建审计日志（通过日志记录，简化实现）
        logger.info("[AUDIT] 子图创建 - subgraphId: {}, name: {}, operator: {} ({})",
                savedSubgraph.getId(), savedSubgraph.getName(), creatorName, creatorId);

        return savedSubgraph;
    }

    @Override
    @Transactional
    public Subgraph updateSubgraph(Long subgraphId, String name, String description,
                                   List<String> tags, Map<String, String> metadata,
                                   Integer version, Long operatorId, String operatorName) {
        logger.info("更新子图，subgraphId: {}, operatorId: {}", subgraphId, operatorId);

        // 1. 验证子图存在
        Subgraph subgraph = subgraphRepository.findById(subgraphId)
                .orElseThrow(() -> new BusinessException(SubgraphErrorCode.SUBGRAPH_NOT_FOUND));

        // 2. 验证用户是Owner
        if (!subgraphRepository.hasPermission(subgraphId, operatorId, PermissionRole.OWNER)) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_EDIT_DENIED);
        }

        // 3. 验证乐观锁版本
        if (version != null && !version.equals(subgraph.getVersion())) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_VERSION_CONFLICT);
        }

        // 4. 如果修改名称，验证新名称唯一
        if (name != null && !name.equals(subgraph.getName())) {
            if (subgraphRepository.existsByNameExcludeId(name.trim(), subgraphId)) {
                throw new BusinessException(SubgraphErrorCode.SUBGRAPH_NAME_CONFLICT);
            }
        }

        // 5. 更新子图
        subgraph.updateBasicInfo(name, description, tags);
        if (metadata != null) {
            subgraph.updateMetadata(metadata);
        }

        boolean updated = subgraphRepository.update(subgraph);
        if (!updated) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_VERSION_CONFLICT);
        }

        logger.info("子图更新成功，subgraphId: {}", subgraphId);

        // 6. 记录更新审计日志
        logger.info("[AUDIT] 子图更新 - subgraphId: {}, operator: {} ({})",
                subgraphId, operatorName, operatorId);

        return subgraph;
    }

    @Override
    @Transactional
    public void deleteSubgraph(Long subgraphId, Long operatorId, String operatorName) {
        logger.info("删除子图，subgraphId: {}, operatorId: {}", subgraphId, operatorId);

        // 1. 验证子图存在
        Subgraph subgraph = subgraphRepository.findById(subgraphId)
                .orElseThrow(() -> new BusinessException(SubgraphErrorCode.SUBGRAPH_NOT_FOUND));

        // 2. 验证用户是Owner
        if (!subgraphRepository.hasPermission(subgraphId, operatorId, PermissionRole.OWNER)) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_DELETE_DENIED);
        }

        // 3. 验证子图为空（不包含资源节点）
        if (!subgraphResourceRepository.isSubgraphEmpty(subgraphId)) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_NOT_EMPTY);
        }

        // 4. 物理删除子图（权限记录通过外键级联删除）
        subgraphRepository.delete(subgraphId);
        logger.info("子图删除成功，subgraphId: {}", subgraphId);

        // 5. 记录删除审计日志
        logger.info("[AUDIT] 子图删除 - subgraphId: {}, name: {}, operator: {} ({})",
                subgraphId, subgraph.getName(), operatorName, operatorId);
    }

    // ==================== 子图查询 ====================

    @Override
    public List<Subgraph> listSubgraphs(Long userId, int page, int size) {
        // 规范化参数
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 100) size = 100;

        return subgraphRepository.findByUserId(userId, page, size);
    }

    @Override
    public long countSubgraphs(Long userId) {
        return subgraphRepository.countByUserId(userId);
    }

    @Override
    public List<Subgraph> searchSubgraphs(String keyword, Long userId, int page, int size) {
        // 规范化参数
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 100) size = 100;

        return subgraphRepository.searchByKeyword(keyword, userId, page, size);
    }

    @Override
    public long countSearchSubgraphs(String keyword, Long userId) {
        return subgraphRepository.countByKeyword(keyword, userId);
    }

    @Override
    public List<Subgraph> filterByTags(List<String> tags, Long userId, int page, int size) {
        // 规范化参数
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 100) size = 100;

        return subgraphRepository.filterByTags(tags, userId, page, size);
    }

    @Override
    public List<Subgraph> filterByOwner(Long ownerId, Long currentUserId, int page, int size) {
        // 规范化参数
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 100) size = 100;

        return subgraphRepository.filterByOwner(ownerId, currentUserId, page, size);
    }

    @Override
    public Optional<Subgraph> getSubgraphById(Long subgraphId) {
        if (subgraphId == null) {
            return Optional.empty();
        }
        return subgraphRepository.findById(subgraphId);
    }

    @Override
    public Optional<Subgraph> getSubgraphDetail(Long subgraphId, Long userId) {
        if (subgraphId == null || userId == null) {
            return Optional.empty();
        }

        // 检查权限
        if (!subgraphRepository.hasAnyPermission(subgraphId, userId)) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_ACCESS_DENIED);
        }

        return subgraphRepository.findById(subgraphId);
    }

    // ==================== 权限管理 ====================

    @Override
    @Transactional
    public void addPermission(Long subgraphId, Long userId, PermissionRole role,
                              Long grantedBy, String grantedByName) {
        logger.info("添加权限，subgraphId: {}, userId: {}, role: {}", subgraphId, userId, role);

        // 1. 验证子图存在
        if (!subgraphRepository.existsById(subgraphId)) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_NOT_FOUND);
        }

        // 2. 验证操作者是Owner
        if (!subgraphRepository.hasPermission(subgraphId, grantedBy, PermissionRole.OWNER)) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_PERMISSION_DENIED);
        }

        // 3. 检查用户是否已有权限
        Optional<SubgraphPermission> existingPermission =
                subgraphRepository.findPermissionBySubgraphIdAndUserId(subgraphId, userId);
        if (existingPermission.isPresent()) {
            // 更新现有权限
            SubgraphPermission permission = existingPermission.get();
            permission.updateRole(role, grantedBy);
            subgraphRepository.savePermission(permission);
            logger.info("更新权限，subgraphId: {}, userId: {}, newRole: {}", subgraphId, userId, role);
        } else {
            // 创建新权限
            SubgraphPermission permission = SubgraphPermission.create(subgraphId, userId, role, grantedBy);
            subgraphRepository.savePermission(permission);
            logger.info("新增权限，subgraphId: {}, userId: {}, role: {}", subgraphId, userId, role);
        }

        // 4. 记录审计日志
        logger.info("[AUDIT] 权限添加 - subgraphId: {}, userId: {}, role: {}, operator: {} ({})",
                subgraphId, userId, role, grantedByName, grantedBy);
    }

    @Override
    @Transactional
    public void removePermission(Long subgraphId, Long userId, Long removedBy, String removedByName) {
        logger.info("移除权限，subgraphId: {}, userId: {}", subgraphId, userId);

        // 1. 验证子图存在
        if (!subgraphRepository.existsById(subgraphId)) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_NOT_FOUND);
        }

        // 2. 验证操作者是Owner
        if (!subgraphRepository.hasPermission(subgraphId, removedBy, PermissionRole.OWNER)) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_PERMISSION_DENIED);
        }

        // 3. 检查被移除用户的权限
        Optional<SubgraphPermission> permission =
                subgraphRepository.findPermissionBySubgraphIdAndUserId(subgraphId, userId);
        if (permission.isEmpty()) {
            return; // 用户本来就没有权限，直接返回
        }

        // 4. 如果移除的是Owner，检查是否是最后一个Owner
        if (permission.get().isOwner()) {
            int ownerCount = subgraphRepository.countOwnersBySubgraphId(subgraphId);
            if (ownerCount <= 1) {
                throw new BusinessException(SubgraphErrorCode.SUBGRAPH_LAST_OWNER);
            }
        }

        // 5. 删除权限
        subgraphRepository.deletePermission(subgraphId, userId);
        logger.info("权限移除成功，subgraphId: {}, userId: {}", subgraphId, userId);

        // 6. 记录审计日志
        logger.info("[AUDIT] 权限移除 - subgraphId: {}, userId: {}, operator: {} ({})",
                subgraphId, userId, removedByName, removedBy);
    }

    @Override
    public boolean hasPermission(Long subgraphId, Long userId, PermissionRole role) {
        if (subgraphId == null || userId == null || role == null) {
            return false;
        }
        return subgraphRepository.hasPermission(subgraphId, userId, role);
    }

    @Override
    public boolean hasAnyPermission(Long subgraphId, Long userId) {
        if (subgraphId == null || userId == null) {
            return false;
        }
        return subgraphRepository.hasAnyPermission(subgraphId, userId);
    }

    @Override
    public List<SubgraphPermission> getPermissions(Long subgraphId) {
        if (subgraphId == null) {
            return List.of();
        }
        return subgraphRepository.findPermissionsBySubgraphId(subgraphId);
    }

    // ==================== 资源节点管理 ====================

    @Override
    @Transactional
    public void addResources(Long subgraphId, List<Long> resourceIds,
                             Long operatorId, String operatorName) {
        logger.info("添加资源到子图，subgraphId: {}, resourceCount: {}", subgraphId, resourceIds.size());

        // 1. 验证子图存在
        if (!subgraphRepository.existsById(subgraphId)) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_NOT_FOUND);
        }

        // 2. 验证操作者是Owner
        if (!subgraphRepository.hasPermission(subgraphId, operatorId, PermissionRole.OWNER)) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_RESOURCE_DENIED);
        }

        // 3. 批量添加资源（跳过已存在的）
        if (resourceIds != null && !resourceIds.isEmpty()) {
            List<com.catface996.aiops.domain.model.subgraph.SubgraphResource> resources =
                    resourceIds.stream()
                            .filter(resourceId -> !subgraphResourceRepository.existsInSubgraph(subgraphId, resourceId))
                            .map(resourceId -> {
                                com.catface996.aiops.domain.model.subgraph.SubgraphResource sr =
                                        new com.catface996.aiops.domain.model.subgraph.SubgraphResource();
                                sr.setSubgraphId(subgraphId);
                                sr.setResourceId(resourceId);
                                sr.setAddedBy(operatorId);
                                sr.setAddedAt(java.time.LocalDateTime.now());
                                return sr;
                            })
                            .toList();

            if (!resources.isEmpty()) {
                subgraphResourceRepository.addResources(resources);
                logger.info("成功添加{}个资源到子图{}", resources.size(), subgraphId);
            }
        }

        // 4. 记录审计日志
        logger.info("[AUDIT] 资源添加 - subgraphId: {}, resourceIds: {}, operator: {} ({})",
                subgraphId, resourceIds, operatorName, operatorId);
    }

    @Override
    @Transactional
    public void removeResources(Long subgraphId, List<Long> resourceIds,
                                Long operatorId, String operatorName) {
        logger.info("从子图移除资源，subgraphId: {}, resourceCount: {}", subgraphId, resourceIds.size());

        // 1. 验证子图存在
        if (!subgraphRepository.existsById(subgraphId)) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_NOT_FOUND);
        }

        // 2. 验证操作者是Owner
        if (!subgraphRepository.hasPermission(subgraphId, operatorId, PermissionRole.OWNER)) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_RESOURCE_DENIED);
        }

        // 3. 批量移除资源
        if (resourceIds != null && !resourceIds.isEmpty()) {
            subgraphResourceRepository.removeResources(subgraphId, resourceIds);
            logger.info("成功从子图{}移除资源", subgraphId);
        }

        // 4. 记录审计日志
        logger.info("[AUDIT] 资源移除 - subgraphId: {}, resourceIds: {}, operator: {} ({})",
                subgraphId, resourceIds, operatorName, operatorId);
    }

    @Override
    public List<Long> getResourceIds(Long subgraphId, Long userId) {
        // 检查权限
        if (!subgraphRepository.hasAnyPermission(subgraphId, userId)) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_ACCESS_DENIED);
        }

        return subgraphResourceRepository.findResourceIdsBySubgraphId(subgraphId);
    }

    @Override
    public int countResources(Long subgraphId) {
        if (subgraphId == null) {
            return 0;
        }
        return subgraphResourceRepository.countBySubgraphId(subgraphId);
    }

    @Override
    public boolean isSubgraphEmpty(Long subgraphId) {
        if (subgraphId == null) {
            return true;
        }
        return subgraphResourceRepository.isSubgraphEmpty(subgraphId);
    }

    @Override
    public List<com.catface996.aiops.domain.model.subgraph.SubgraphResource> getSubgraphResourcesPaged(
            Long subgraphId, Long userId, int page, int size) {
        // 检查权限
        if (!subgraphRepository.hasAnyPermission(subgraphId, userId)) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_ACCESS_DENIED);
        }

        // 规范化参数
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 100) size = 100;

        return subgraphResourceRepository.findBySubgraphIdPaged(subgraphId, page, size);
    }

    // ==================== 拓扑查询 ====================

    @Override
    public SubgraphTopology getSubgraphTopology(Long subgraphId, Long userId) {
        logger.info("获取子图拓扑，subgraphId: {}, userId: {}", subgraphId, userId);

        // 1. 验证子图存在
        Subgraph subgraph = subgraphRepository.findById(subgraphId)
                .orElseThrow(() -> new BusinessException(SubgraphErrorCode.SUBGRAPH_NOT_FOUND));

        // 2. 验证用户有权限访问（Owner 或 Viewer）
        if (!subgraphRepository.hasAnyPermission(subgraphId, userId)) {
            throw new BusinessException(SubgraphErrorCode.SUBGRAPH_ACCESS_DENIED);
        }

        // 3. 获取子图中的所有资源节点ID
        List<Long> resourceIds = subgraphResourceRepository.findResourceIdsBySubgraphId(subgraphId);

        // 如果子图为空，返回空拓扑
        if (resourceIds.isEmpty()) {
            return new SubgraphTopology(subgraphId, subgraph.getName(), resourceIds, List.of());
        }

        // 4. 将资源ID列表转为Set以便快速查找
        Set<Long> resourceIdSet = new HashSet<>(resourceIds);

        // 5. 获取这些节点之间的关系
        List<Relationship> allRelationships = resourceIds.stream()
                .flatMap(resourceId -> {
                    List<Relationship> upstream = relationshipDomainService.getUpstreamDependencies(resourceId);
                    List<Relationship> downstream = relationshipDomainService.getDownstreamDependencies(resourceId);
                    return java.util.stream.Stream.concat(upstream.stream(), downstream.stream());
                })
                .distinct()
                .collect(Collectors.toList());

        // 6. 过滤关系，仅保留子图内节点之间的关系
        List<Relationship> filteredRelationships = allRelationships.stream()
                .filter(r -> resourceIdSet.contains(r.getSourceResourceId()) &&
                             resourceIdSet.contains(r.getTargetResourceId()))
                .distinct()
                .collect(Collectors.toList());

        logger.info("子图拓扑查询完成，subgraphId: {}, nodeCount: {}, edgeCount: {}",
                subgraphId, resourceIds.size(), filteredRelationships.size());

        return new SubgraphTopology(subgraphId, subgraph.getName(), resourceIds, filteredRelationships);
    }
}
