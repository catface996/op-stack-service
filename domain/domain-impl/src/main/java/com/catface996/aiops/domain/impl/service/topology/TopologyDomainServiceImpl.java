package com.catface996.aiops.domain.impl.service.topology;

import com.catface996.aiops.common.enums.ResourceErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.constant.ResourceTypeConstants;
import com.catface996.aiops.domain.model.resource.OperationType;
import com.catface996.aiops.domain.model.resource.Resource;
import com.catface996.aiops.domain.model.resource.ResourceStatus;
import com.catface996.aiops.domain.model.resource.ResourceType;
import com.catface996.aiops.domain.service.resource.AuditLogService;
import com.catface996.aiops.domain.service.subgraph.SubgraphMemberDomainService;
import com.catface996.aiops.domain.service.topology.TopologyDomainService;
import com.catface996.aiops.repository.resource.ResourceRepository;
import com.catface996.aiops.repository.resource.ResourceTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 拓扑图领域服务实现
 *
 * <p>实现拓扑图管理的核心业务逻辑，协调 Repository 和基础设施服务。</p>
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li>拓扑图 CRUD 操作</li>
 *   <li>自动识别和设置 SUBGRAPH 类型</li>
 *   <li>审计日志记录</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001~008: 资源分类体系设计</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-25
 */
@Service
public class TopologyDomainServiceImpl implements TopologyDomainService {

    private static final Logger logger = LoggerFactory.getLogger(TopologyDomainServiceImpl.class);

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    private final ResourceRepository resourceRepository;
    private final ResourceTypeRepository resourceTypeRepository;
    private final AuditLogService auditLogService;
    private final SubgraphMemberDomainService subgraphMemberDomainService;

    // 缓存 SUBGRAPH 类型ID，避免重复查询
    private Long subgraphTypeIdCache;

    public TopologyDomainServiceImpl(
            ResourceRepository resourceRepository,
            ResourceTypeRepository resourceTypeRepository,
            AuditLogService auditLogService,
            SubgraphMemberDomainService subgraphMemberDomainService) {
        this.resourceRepository = resourceRepository;
        this.resourceTypeRepository = resourceTypeRepository;
        this.auditLogService = auditLogService;
        this.subgraphMemberDomainService = subgraphMemberDomainService;
    }

    @Override
    @Transactional
    public Resource createTopology(String name, String description, Long operatorId, String operatorName) {
        logger.info("创建拓扑图，name: {}, operatorId: {}", name, operatorId);

        // 1. 参数验证
        validateTopologyName(name);
        validateDescription(description);

        // 2. 获取 SUBGRAPH 类型ID
        Long subgraphTypeId = getSubgraphTypeId();

        // 3. 创建资源实体
        Resource topology = new Resource();
        topology.setName(name.trim());
        topology.setDescription(description);
        topology.setResourceTypeId(subgraphTypeId);
        topology.setStatus(ResourceStatus.RUNNING);
        topology.setAttributes("{}");
        topology.setCreatedBy(operatorId);
        topology.setCreatedAt(LocalDateTime.now());
        topology.setUpdatedAt(LocalDateTime.now());
        topology.setVersion(1);

        // 4. 保存拓扑图
        resourceRepository.insert(topology);
        logger.info("拓扑图创建成功，id: {}, name: {}", topology.getId(), topology.getName());

        // 5. 记录审计日志
        auditLogService.log(
                topology.getId(),
                OperationType.CREATE,
                null,
                topology.toString(),
                operatorId,
                operatorName
        );

        return topology;
    }

    @Override
    public List<Resource> listTopologies(String name, ResourceStatus status, int page, int size) {
        Long subgraphTypeId = getSubgraphTypeId();
        return resourceRepository.findByTypeIdAndConditions(
                subgraphTypeId,
                status,
                name,
                page,
                size
        );
    }

    @Override
    public long countTopologies(String name, ResourceStatus status) {
        Long subgraphTypeId = getSubgraphTypeId();
        return resourceRepository.countByTypeIdAndConditions(subgraphTypeId, status, name);
    }

    @Override
    public Optional<Resource> getTopologyById(Long topologyId) {
        Long subgraphTypeId = getSubgraphTypeId();
        return resourceRepository.findById(topologyId)
                .filter(resource -> subgraphTypeId.equals(resource.getResourceTypeId()));
    }

    @Override
    @Transactional
    public Resource updateTopology(Long topologyId, String name, String description,
                                   Integer version, Long operatorId, String operatorName) {
        logger.info("更新拓扑图，topologyId: {}, operatorId: {}", topologyId, operatorId);

        // 1. 验证拓扑图存在
        Resource topology = getTopologyById(topologyId)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.RESOURCE_NOT_FOUND, "拓扑图不存在"));

        // 2. 验证版本号（乐观锁）
        if (!topology.getVersion().equals(version)) {
            throw new BusinessException(ResourceErrorCode.VERSION_CONFLICT, "数据已被修改，请刷新后重试");
        }

        // 3. 记录旧值
        String oldValue = topology.toString();

        // 4. 更新字段
        if (name != null) {
            validateTopologyName(name);
            topology.setName(name.trim());
        }
        if (description != null) {
            validateDescription(description);
            topology.setDescription(description);
        }
        topology.setUpdatedAt(LocalDateTime.now());
        topology.setVersion(topology.getVersion() + 1);

        // 5. 保存更新
        resourceRepository.updateById(topology);
        logger.info("拓扑图更新成功，id: {}", topologyId);

        // 6. 记录审计日志
        auditLogService.log(
                topologyId,
                OperationType.UPDATE,
                oldValue,
                topology.toString(),
                operatorId,
                operatorName
        );

        return topology;
    }

    @Override
    @Transactional
    public void deleteTopology(Long topologyId, Long operatorId, String operatorName) {
        logger.info("删除拓扑图，topologyId: {}, operatorId: {}", topologyId, operatorId);

        // 1. 验证拓扑图存在
        Resource topology = getTopologyById(topologyId)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.RESOURCE_NOT_FOUND, "拓扑图不存在"));

        // 2. 记录旧值
        String oldValue = topology.toString();

        // 3. 解除与所有成员的关联关系 - 获取所有成员ID并批量移除
        List<Long> memberIds = subgraphMemberDomainService.getMemberIds(topologyId);
        if (!memberIds.isEmpty()) {
            subgraphMemberDomainService.removeMembers(topologyId, memberIds, operatorId);
            logger.info("已解除拓扑图 {} 与 {} 个成员的关联", topologyId, memberIds.size());
        }

        // 4. 删除拓扑图
        resourceRepository.deleteById(topologyId);
        logger.info("拓扑图删除成功，id: {}", topologyId);

        // 5. 记录审计日志
        auditLogService.log(
                topologyId,
                OperationType.DELETE,
                oldValue,
                null,
                operatorId,
                operatorName
        );
    }

    @Override
    public int countMembers(Long topologyId) {
        return subgraphMemberDomainService.countMembers(topologyId);
    }

    @Override
    public Long getSubgraphTypeId() {
        if (subgraphTypeIdCache != null) {
            return subgraphTypeIdCache;
        }

        Optional<ResourceType> subgraphType = resourceTypeRepository.findByCode(ResourceTypeConstants.SUBGRAPH_CODE);
        if (subgraphType.isEmpty()) {
            throw new BusinessException(ResourceErrorCode.RESOURCE_TYPE_NOT_FOUND,
                    "系统配置错误：SUBGRAPH 资源类型不存在");
        }

        subgraphTypeIdCache = subgraphType.get().getId();
        return subgraphTypeIdCache;
    }

    // ===== 私有方法 =====

    private void validateTopologyName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("拓扑图名称不能为空");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("拓扑图名称过长，最多 " + MAX_NAME_LENGTH + " 字符");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("拓扑图描述过长，最多 " + MAX_DESCRIPTION_LENGTH + " 字符");
        }
    }
}
