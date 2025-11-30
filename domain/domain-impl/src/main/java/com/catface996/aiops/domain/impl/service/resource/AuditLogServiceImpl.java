package com.catface996.aiops.domain.impl.service.resource;

import com.catface996.aiops.domain.model.resource.OperationType;
import com.catface996.aiops.domain.model.resource.ResourceAuditLog;
import com.catface996.aiops.domain.service.resource.AuditLogService;
import com.catface996.aiops.repository.resource.ResourceAuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审计日志服务实现
 *
 * <p>记录资源的操作历史，支持变更追溯。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-020: 查看资源变更历史</li>
 *   <li>REQ-FR-025: 记录资源状态变更历史</li>
 *   <li>REQ-FR-028: 审计日志功能</li>
 *   <li>REQ-NFR-010: 审计追溯要求</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final ResourceAuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(ResourceAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void logCreate(Long resourceId, String newValue, Long operatorId, String operatorName) {
        log(resourceId, OperationType.CREATE, null, newValue, operatorId, operatorName);
    }

    @Override
    public void logUpdate(Long resourceId, String oldValue, String newValue, Long operatorId, String operatorName) {
        log(resourceId, OperationType.UPDATE, oldValue, newValue, operatorId, operatorName);
    }

    @Override
    public void logDelete(Long resourceId, String oldValue, Long operatorId, String operatorName) {
        log(resourceId, OperationType.DELETE, oldValue, null, operatorId, operatorName);
    }

    @Override
    public void logStatusChange(Long resourceId, String oldStatus, String newStatus, Long operatorId, String operatorName) {
        String oldValue = "{\"status\":\"" + oldStatus + "\"}";
        String newValue = "{\"status\":\"" + newStatus + "\"}";
        log(resourceId, OperationType.STATUS_CHANGE, oldValue, newValue, operatorId, operatorName);
    }

    @Override
    public void log(Long resourceId, OperationType operation, String oldValue, String newValue,
                    Long operatorId, String operatorName) {
        if (resourceId == null) {
            logger.warn("审计日志记录失败：资源ID为空");
            return;
        }
        if (operation == null) {
            logger.warn("审计日志记录失败：操作类型为空，resourceId: {}", resourceId);
            return;
        }

        try {
            ResourceAuditLog auditLog = ResourceAuditLog.create(
                    resourceId, operation, oldValue, newValue, operatorId, operatorName);

            auditLogRepository.save(auditLog);

            logger.info("记录审计日志成功，resourceId: {}, operation: {}, operatorId: {}",
                    resourceId, operation, operatorId);
        } catch (Exception e) {
            // 审计日志记录失败不应影响主流程
            logger.error("记录审计日志失败，resourceId: {}, operation: {}", resourceId, operation, e);
        }
    }

    @Override
    public List<ResourceAuditLog> getAuditLogs(Long resourceId, int page, int size) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为空");
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 20;
        }

        return auditLogRepository.findByResourceId(resourceId, page, size);
    }

    @Override
    public List<ResourceAuditLog> getAuditLogsByOperation(Long resourceId, OperationType operation, int page, int size) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为空");
        }
        if (operation == null) {
            throw new IllegalArgumentException("操作类型不能为空");
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 20;
        }

        return auditLogRepository.findByResourceIdAndOperation(resourceId, operation, page, size);
    }

    @Override
    public long countAuditLogs(Long resourceId) {
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为空");
        }
        return auditLogRepository.countByResourceId(resourceId);
    }
}
