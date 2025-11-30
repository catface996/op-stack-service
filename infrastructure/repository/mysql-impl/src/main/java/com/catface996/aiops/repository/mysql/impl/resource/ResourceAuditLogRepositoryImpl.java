package com.catface996.aiops.repository.mysql.impl.resource;

import com.catface996.aiops.domain.model.resource.OperationType;
import com.catface996.aiops.domain.model.resource.ResourceAuditLog;
import com.catface996.aiops.repository.resource.ResourceAuditLogRepository;
import com.catface996.aiops.repository.mysql.mapper.resource.ResourceAuditLogMapper;
import com.catface996.aiops.repository.mysql.po.resource.ResourceAuditLogPO;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 资源审计日志仓储实现类
 *
 * <p>使用 MyBatis-Plus 实现资源审计日志数据访问</p>
 * <p>负责领域对象与持久化对象之间的转换</p>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Repository
public class ResourceAuditLogRepositoryImpl implements ResourceAuditLogRepository {

    private final ResourceAuditLogMapper resourceAuditLogMapper;

    public ResourceAuditLogRepositoryImpl(ResourceAuditLogMapper resourceAuditLogMapper) {
        this.resourceAuditLogMapper = resourceAuditLogMapper;
    }

    @Override
    public Optional<ResourceAuditLog> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("审计日志ID不能为null");
        }
        ResourceAuditLogPO po = resourceAuditLogMapper.selectById(id);
        return Optional.ofNullable(toEntity(po));
    }

    @Override
    public List<ResourceAuditLog> findByResourceId(Long resourceId, int page, int size) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为null");
        }
        int offset = (page - 1) * size;
        List<ResourceAuditLogPO> poList = resourceAuditLogMapper.selectByResourceId(resourceId, offset, size);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long countByResourceId(Long resourceId) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为null");
        }
        return resourceAuditLogMapper.countByResourceId(resourceId);
    }

    @Override
    public List<ResourceAuditLog> findByResourceIdAndOperation(Long resourceId,
                                                               OperationType operation,
                                                               int page, int size) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为null");
        }
        if (operation == null) {
            throw new IllegalArgumentException("操作类型不能为null");
        }
        int offset = (page - 1) * size;
        List<ResourceAuditLogPO> poList = resourceAuditLogMapper.selectByResourceIdAndOperation(
                resourceId, operation.name(), offset, size);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResourceAuditLog> findByResourceIdAndTimeRange(Long resourceId,
                                                                LocalDateTime startTime,
                                                                LocalDateTime endTime,
                                                                int page, int size) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为null");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("时间范围不能为null");
        }
        int offset = (page - 1) * size;
        List<ResourceAuditLogPO> poList = resourceAuditLogMapper.selectByResourceIdAndTimeRange(
                resourceId, startTime, endTime, offset, size);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResourceAuditLog> findByOperatorId(Long operatorId, int page, int size) {
        if (operatorId == null) {
            throw new IllegalArgumentException("操作人ID不能为null");
        }
        int offset = (page - 1) * size;
        List<ResourceAuditLogPO> poList = resourceAuditLogMapper.selectByOperatorId(operatorId, offset, size);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ResourceAuditLog save(ResourceAuditLog auditLog) {
        if (auditLog == null) {
            throw new IllegalArgumentException("审计日志实体不能为null");
        }
        ResourceAuditLogPO po = toPO(auditLog);
        resourceAuditLogMapper.insert(po);
        ResourceAuditLogPO savedPO = resourceAuditLogMapper.selectById(po.getId());
        return toEntity(savedPO);
    }

    @Override
    public List<ResourceAuditLog> saveAll(List<ResourceAuditLog> auditLogs) {
        if (auditLogs == null || auditLogs.isEmpty()) {
            return List.of();
        }
        return auditLogs.stream()
                .map(this::save)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByResourceId(Long resourceId) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为null");
        }
        resourceAuditLogMapper.deleteByResourceId(resourceId);
    }

    @Override
    public int deleteByCreatedAtBefore(LocalDateTime beforeTime) {
        if (beforeTime == null) {
            throw new IllegalArgumentException("时间点不能为null");
        }
        return resourceAuditLogMapper.deleteByCreatedAtBefore(beforeTime);
    }

    @Override
    public long count() {
        return resourceAuditLogMapper.selectCount(null);
    }

    /**
     * 将领域实体转换为持久化对象
     */
    private ResourceAuditLogPO toPO(ResourceAuditLog entity) {
        if (entity == null) {
            return null;
        }
        ResourceAuditLogPO po = new ResourceAuditLogPO();
        po.setId(entity.getId());
        po.setResourceId(entity.getResourceId());
        po.setOperation(entity.getOperation() != null ? entity.getOperation().name() : null);
        po.setOperatorId(entity.getOperatorId());
        po.setOperatorName(entity.getOperatorName());
        po.setOldValue(entity.getOldValue());
        po.setNewValue(entity.getNewValue());
        // remark 在领域模型中不需要，PO中保留用于数据库兼容
        po.setCreatedAt(entity.getCreatedAt());
        return po;
    }

    /**
     * 将持久化对象转换为领域实体
     */
    private ResourceAuditLog toEntity(ResourceAuditLogPO po) {
        if (po == null) {
            return null;
        }
        ResourceAuditLog entity = new ResourceAuditLog();
        entity.setId(po.getId());
        entity.setResourceId(po.getResourceId());
        entity.setOperation(po.getOperation() != null ? OperationType.valueOf(po.getOperation()) : null);
        entity.setOperatorId(po.getOperatorId());
        entity.setOperatorName(po.getOperatorName());
        entity.setOldValue(po.getOldValue());
        entity.setNewValue(po.getNewValue());
        // remark 在领域模型中不需要
        entity.setCreatedAt(po.getCreatedAt());
        return entity;
    }
}
