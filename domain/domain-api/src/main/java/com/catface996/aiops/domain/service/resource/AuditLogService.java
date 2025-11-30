package com.catface996.aiops.domain.service.resource;

import com.catface996.aiops.domain.model.resource.OperationType;
import com.catface996.aiops.domain.model.resource.ResourceAuditLog;

import java.util.List;

/**
 * 审计日志服务接口
 *
 * <p>提供资源操作审计日志的记录和查询功能。</p>
 *
 * <p>支持的操作类型：</p>
 * <ul>
 *   <li>CREATE: 资源创建</li>
 *   <li>UPDATE: 资源更新</li>
 *   <li>DELETE: 资源删除</li>
 *   <li>STATUS_CHANGE: 状态变更</li>
 * </ul>
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
public interface AuditLogService {

    /**
     * 记录资源创建日志
     *
     * @param resourceId 资源ID
     * @param newValue 新值（JSON格式）
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    void logCreate(Long resourceId, String newValue, Long operatorId, String operatorName);

    /**
     * 记录资源更新日志
     *
     * @param resourceId 资源ID
     * @param oldValue 旧值（JSON格式）
     * @param newValue 新值（JSON格式）
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    void logUpdate(Long resourceId, String oldValue, String newValue, Long operatorId, String operatorName);

    /**
     * 记录资源删除日志
     *
     * @param resourceId 资源ID
     * @param oldValue 删除前的值（JSON格式）
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    void logDelete(Long resourceId, String oldValue, Long operatorId, String operatorName);

    /**
     * 记录资源状态变更日志
     *
     * @param resourceId 资源ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    void logStatusChange(Long resourceId, String oldStatus, String newStatus, Long operatorId, String operatorName);

    /**
     * 记录审计日志（通用方法）
     *
     * @param resourceId 资源ID
     * @param operation 操作类型
     * @param oldValue 旧值
     * @param newValue 新值
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    void log(Long resourceId, OperationType operation, String oldValue, String newValue,
             Long operatorId, String operatorName);

    /**
     * 分页查询资源的审计日志
     *
     * @param resourceId 资源ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 审计日志列表
     */
    List<ResourceAuditLog> getAuditLogs(Long resourceId, int page, int size);

    /**
     * 按操作类型查询审计日志
     *
     * @param resourceId 资源ID
     * @param operation 操作类型
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 审计日志列表
     */
    List<ResourceAuditLog> getAuditLogsByOperation(Long resourceId, OperationType operation, int page, int size);

    /**
     * 统计资源的审计日志数量
     *
     * @param resourceId 资源ID
     * @return 审计日志数量
     */
    long countAuditLogs(Long resourceId);
}
