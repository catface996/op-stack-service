package com.catface996.aiops.repository.resource;

import com.catface996.aiops.domain.model.resource.OperationType;
import com.catface996.aiops.domain.model.resource.ResourceAuditLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 资源审计日志仓储接口
 *
 * <p>提供资源审计日志的数据访问操作，遵循DDD仓储模式。</p>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public interface ResourceAuditLogRepository {

    /**
     * 根据ID查询审计日志
     *
     * @param id 审计日志ID
     * @return 审计日志实体（如果存在）
     */
    Optional<ResourceAuditLog> findById(Long id);

    /**
     * 根据资源ID分页查询审计日志
     *
     * @param resourceId 资源ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 审计日志列表（按时间倒序排列）
     */
    List<ResourceAuditLog> findByResourceId(Long resourceId, int page, int size);

    /**
     * 统计资源的审计日志数量
     *
     * @param resourceId 资源ID
     * @return 审计日志数量
     */
    long countByResourceId(Long resourceId);

    /**
     * 根据操作类型查询审计日志
     *
     * @param resourceId 资源ID
     * @param operation 操作类型
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 审计日志列表
     */
    List<ResourceAuditLog> findByResourceIdAndOperation(Long resourceId,
                                                        OperationType operation,
                                                        int page, int size);

    /**
     * 根据时间范围查询审计日志
     *
     * @param resourceId 资源ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 审计日志列表
     */
    List<ResourceAuditLog> findByResourceIdAndTimeRange(Long resourceId,
                                                        LocalDateTime startTime,
                                                        LocalDateTime endTime,
                                                        int page, int size);

    /**
     * 根据操作人查询审计日志
     *
     * @param operatorId 操作人ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 审计日志列表
     */
    List<ResourceAuditLog> findByOperatorId(Long operatorId, int page, int size);

    /**
     * 保存审计日志
     *
     * @param auditLog 审计日志实体
     * @return 保存后的审计日志实体
     */
    ResourceAuditLog save(ResourceAuditLog auditLog);

    /**
     * 批量保存审计日志
     *
     * @param auditLogs 审计日志列表
     * @return 保存后的审计日志列表
     */
    List<ResourceAuditLog> saveAll(List<ResourceAuditLog> auditLogs);

    /**
     * 删除资源的所有审计日志
     * （通常在删除资源时级联删除）
     *
     * @param resourceId 资源ID
     */
    void deleteByResourceId(Long resourceId);

    /**
     * 清理指定时间之前的审计日志
     *
     * @param beforeTime 时间点
     * @return 删除的记录数
     */
    int deleteByCreatedAtBefore(LocalDateTime beforeTime);

    /**
     * 统计所有审计日志数量
     *
     * @return 审计日志总数
     */
    long count();
}
