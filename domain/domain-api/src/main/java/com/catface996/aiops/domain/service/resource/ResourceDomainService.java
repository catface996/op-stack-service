package com.catface996.aiops.domain.service.resource;

import com.catface996.aiops.domain.model.resource.Resource;
import com.catface996.aiops.domain.model.resource.ResourceAuditLog;
import com.catface996.aiops.domain.model.resource.ResourceStatus;
import com.catface996.aiops.domain.model.resource.ResourceType;

import java.util.List;
import java.util.Optional;

/**
 * 资源领域服务接口
 *
 * <p>提供资源管理的核心业务逻辑，包括：</p>
 * <ul>
 *   <li>资源创建：验证、加密、保存、审计</li>
 *   <li>资源查询：缓存、分页、条件过滤</li>
 *   <li>资源更新：权限检查、乐观锁、加密、审计</li>
 *   <li>资源删除：权限检查、关联检查、审计</li>
 *   <li>状态管理：状态变更、审计记录</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-001: 创建资源</li>
 *   <li>REQ-FR-002: 资源列表展示</li>
 *   <li>REQ-FR-003: 资源详情查看</li>
 *   <li>REQ-FR-004: 敏感配置加密存储</li>
 *   <li>REQ-FR-010~016: 资源编辑</li>
 *   <li>REQ-FR-017~022: 资源删除</li>
 *   <li>REQ-FR-023~028: 状态管理与审计</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public interface ResourceDomainService {

    /**
     * 创建资源
     *
     * <p>执行以下操作：</p>
     * <ol>
     *   <li>验证资源名称在同类型下唯一</li>
     *   <li>验证资源类型存在</li>
     *   <li>加密敏感配置信息</li>
     *   <li>保存资源到数据库</li>
     *   <li>记录创建审计日志</li>
     * </ol>
     *
     * @param name 资源名称
     * @param description 资源描述
     * @param resourceTypeId 资源类型ID
     * @param attributes 扩展属性（JSON格式，敏感字段将被加密）
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 创建的资源实体
     * @throws IllegalArgumentException 如果参数无效
     * @throws RuntimeException 如果资源名称已存在或类型不存在
     */
    Resource createResource(String name, String description, Long resourceTypeId,
                            String attributes, Long operatorId, String operatorName);

    /**
     * 分页查询资源列表
     *
     * <p>支持缓存，前3页数据会被缓存5分钟。</p>
     *
     * @param resourceTypeId 资源类型ID（可选）
     * @param status 资源状态（可选）
     * @param keyword 搜索关键词（可选）
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 资源列表
     */
    List<Resource> listResources(Long resourceTypeId, ResourceStatus status,
                                 String keyword, int page, int size);

    /**
     * 统计资源数量
     *
     * @param resourceTypeId 资源类型ID（可选）
     * @param status 资源状态（可选）
     * @param keyword 搜索关键词（可选）
     * @return 资源数量
     */
    long countResources(Long resourceTypeId, ResourceStatus status, String keyword);

    /**
     * 根据ID获取资源详情
     *
     * <p>支持缓存，详情数据会被缓存5分钟。</p>
     * <p>敏感配置会被解密后返回。</p>
     *
     * @param resourceId 资源ID
     * @return 资源实体（如果存在）
     */
    Optional<Resource> getResourceById(Long resourceId);

    /**
     * 根据ID获取资源详情（包含资源类型）
     *
     * @param resourceId 资源ID
     * @return 资源实体（包含类型信息）
     */
    Optional<Resource> getResourceByIdWithType(Long resourceId);

    /**
     * 更新资源
     *
     * <p>执行以下操作：</p>
     * <ol>
     *   <li>验证资源存在</li>
     *   <li>检查用户权限（Owner或Admin）</li>
     *   <li>验证乐观锁版本</li>
     *   <li>加密敏感配置信息</li>
     *   <li>更新资源</li>
     *   <li>清除缓存</li>
     *   <li>记录更新审计日志</li>
     * </ol>
     *
     * @param resourceId 资源ID
     * @param name 新资源名称（可选，null表示不修改）
     * @param description 新描述（可选）
     * @param attributes 新扩展属性（可选）
     * @param version 当前版本号（乐观锁）
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 更新后的资源实体
     * @throws RuntimeException 如果资源不存在、权限不足或版本冲突
     */
    Resource updateResource(Long resourceId, String name, String description,
                            String attributes, Integer version,
                            Long operatorId, String operatorName);

    /**
     * 删除资源
     *
     * <p>执行以下操作：</p>
     * <ol>
     *   <li>验证资源存在</li>
     *   <li>检查用户权限（Owner或Admin）</li>
     *   <li>验证资源名称确认</li>
     *   <li>检查关联资源（如有关联则拒绝删除）</li>
     *   <li>删除资源标签</li>
     *   <li>物理删除资源</li>
     *   <li>清除缓存</li>
     *   <li>记录删除审计日志</li>
     * </ol>
     *
     * @param resourceId 资源ID
     * @param confirmName 确认的资源名称
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @throws RuntimeException 如果资源不存在、权限不足、名称不匹配或存在关联
     */
    void deleteResource(Long resourceId, String confirmName, Long operatorId, String operatorName);

    /**
     * 更新资源状态
     *
     * <p>执行以下操作：</p>
     * <ol>
     *   <li>验证资源存在</li>
     *   <li>验证状态转换是否有效</li>
     *   <li>更新状态（使用乐观锁）</li>
     *   <li>清除缓存</li>
     *   <li>记录状态变更审计日志</li>
     * </ol>
     *
     * @param resourceId 资源ID
     * @param newStatus 新状态
     * @param version 当前版本号（乐观锁）
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 更新后的资源实体
     * @throws RuntimeException 如果资源不存在或版本冲突
     */
    Resource updateResourceStatus(Long resourceId, ResourceStatus newStatus, Integer version,
                                  Long operatorId, String operatorName);

    /**
     * 检查用户是否有资源操作权限
     *
     * <p>Owner 或 Admin 角色有权限。</p>
     *
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @param isAdmin 是否为管理员
     * @return true 如果有权限
     */
    boolean checkOwnerPermission(Long resourceId, Long userId, boolean isAdmin);

    /**
     * 获取资源的审计日志
     *
     * @param resourceId 资源ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 审计日志列表
     */
    List<ResourceAuditLog> getAuditLogs(Long resourceId, int page, int size);

    /**
     * 获取所有资源类型
     *
     * <p>支持缓存，资源类型会被缓存30分钟。</p>
     *
     * @return 资源类型列表
     */
    List<ResourceType> getAllResourceTypes();

    /**
     * 根据ID获取资源类型
     *
     * @param resourceTypeId 资源类型ID
     * @return 资源类型（如果存在）
     */
    Optional<ResourceType> getResourceTypeById(Long resourceTypeId);
}
