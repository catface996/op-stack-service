package com.catface996.aiops.domain.service.subgraph;

import com.catface996.aiops.domain.model.subgraph.PermissionRole;
import com.catface996.aiops.domain.model.subgraph.Subgraph;
import com.catface996.aiops.domain.model.subgraph.SubgraphPermission;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 子图领域服务接口
 *
 * <p>提供子图管理的核心业务逻辑，包括：</p>
 * <ul>
 *   <li>子图创建：名称唯一性检查、自动创建Owner权限、审计日志</li>
 *   <li>子图查询：分页、搜索、过滤、权限过滤</li>
 *   <li>子图更新：权限检查、乐观锁、审计日志</li>
 *   <li>子图删除：权限检查、空子图检查、级联删除、审计日志</li>
 *   <li>权限管理：添加/移除权限、防止移除最后一个Owner</li>
 *   <li>资源节点管理：批量添加/移除、权限检查、审计日志</li>
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
public interface SubgraphDomainService {

    // ==================== 子图生命周期 ====================

    /**
     * 创建子图
     *
     * <p>执行以下操作：</p>
     * <ol>
     *   <li>验证子图名称全局唯一</li>
     *   <li>保存子图到数据库</li>
     *   <li>自动为创建者分配Owner权限</li>
     *   <li>记录创建审计日志</li>
     * </ol>
     *
     * @param name 子图名称（全局唯一，1-255字符）
     * @param description 子图描述
     * @param tags 标签列表
     * @param metadata 元数据
     * @param creatorId 创建者用户ID
     * @param creatorName 创建者用户名
     * @return 创建的子图实体
     * @throws IllegalArgumentException 如果参数无效
     * @throws RuntimeException 如果子图名称已存在
     */
    Subgraph createSubgraph(String name, String description, List<String> tags,
                            Map<String, String> metadata, Long creatorId, String creatorName);

    /**
     * 更新子图
     *
     * <p>执行以下操作：</p>
     * <ol>
     *   <li>验证子图存在</li>
     *   <li>验证用户是Owner</li>
     *   <li>验证乐观锁版本</li>
     *   <li>如果修改名称，验证新名称唯一</li>
     *   <li>更新子图</li>
     *   <li>记录更新审计日志</li>
     * </ol>
     *
     * @param subgraphId 子图ID
     * @param name 新名称（可为null表示不修改）
     * @param description 新描述（可为null表示不修改）
     * @param tags 新标签列表（可为null表示不修改）
     * @param metadata 新元数据（可为null表示不修改）
     * @param version 当前版本号（乐观锁）
     * @param operatorId 操作者用户ID
     * @param operatorName 操作者用户名
     * @return 更新后的子图实体
     * @throws RuntimeException 如果子图不存在、权限不足或版本冲突
     */
    Subgraph updateSubgraph(Long subgraphId, String name, String description,
                            List<String> tags, Map<String, String> metadata,
                            Integer version, Long operatorId, String operatorName);

    /**
     * 删除子图
     *
     * <p>执行以下操作：</p>
     * <ol>
     *   <li>验证子图存在</li>
     *   <li>验证用户是Owner</li>
     *   <li>验证子图为空（不包含资源节点）</li>
     *   <li>物理删除子图（权限记录通过级联删除）</li>
     *   <li>记录删除审计日志</li>
     * </ol>
     *
     * @param subgraphId 子图ID
     * @param operatorId 操作者用户ID
     * @param operatorName 操作者用户名
     * @throws RuntimeException 如果子图不存在、权限不足或子图非空
     */
    void deleteSubgraph(Long subgraphId, Long operatorId, String operatorName);

    // ==================== 子图查询 ====================

    /**
     * 获取用户有权限访问的子图列表
     *
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 子图列表
     */
    List<Subgraph> listSubgraphs(Long userId, int page, int size);

    /**
     * 统计用户有权限访问的子图数量
     *
     * @param userId 用户ID
     * @return 子图数量
     */
    long countSubgraphs(Long userId);

    /**
     * 按关键词搜索子图
     *
     * @param keyword 搜索关键词
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 子图列表
     */
    List<Subgraph> searchSubgraphs(String keyword, Long userId, int page, int size);

    /**
     * 统计按关键词搜索的子图数量
     *
     * @param keyword 搜索关键词
     * @param userId 用户ID
     * @return 子图数量
     */
    long countSearchSubgraphs(String keyword, Long userId);

    /**
     * 按标签过滤子图
     *
     * @param tags 标签列表
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 子图列表
     */
    List<Subgraph> filterByTags(List<String> tags, Long userId, int page, int size);

    /**
     * 按所有者过滤子图
     *
     * @param ownerId 所有者用户ID
     * @param currentUserId 当前用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 子图列表
     */
    List<Subgraph> filterByOwner(Long ownerId, Long currentUserId, int page, int size);

    /**
     * 根据ID获取子图详情
     *
     * @param subgraphId 子图ID
     * @return 子图实体（如果存在）
     */
    Optional<Subgraph> getSubgraphById(Long subgraphId);

    /**
     * 根据ID获取子图详情（带权限检查）
     *
     * @param subgraphId 子图ID
     * @param userId 用户ID
     * @return 子图实体（如果存在且有权限）
     * @throws RuntimeException 如果无权限访问
     */
    Optional<Subgraph> getSubgraphDetail(Long subgraphId, Long userId);

    // ==================== 权限管理 ====================

    /**
     * 添加权限
     *
     * @param subgraphId 子图ID
     * @param userId 被授权用户ID
     * @param role 权限角色
     * @param grantedBy 授权者用户ID
     * @param grantedByName 授权者用户名
     * @throws RuntimeException 如果无权限操作
     */
    void addPermission(Long subgraphId, Long userId, PermissionRole role,
                       Long grantedBy, String grantedByName);

    /**
     * 移除权限
     *
     * @param subgraphId 子图ID
     * @param userId 被移除权限的用户ID
     * @param removedBy 操作者用户ID
     * @param removedByName 操作者用户名
     * @throws RuntimeException 如果无权限操作或尝试移除最后一个Owner
     */
    void removePermission(Long subgraphId, Long userId, Long removedBy, String removedByName);

    /**
     * 检查用户是否有指定权限
     *
     * @param subgraphId 子图ID
     * @param userId 用户ID
     * @param role 权限角色
     * @return true 如果有该权限
     */
    boolean hasPermission(Long subgraphId, Long userId, PermissionRole role);

    /**
     * 检查用户是否有任何权限
     *
     * @param subgraphId 子图ID
     * @param userId 用户ID
     * @return true 如果有任何权限
     */
    boolean hasAnyPermission(Long subgraphId, Long userId);

    /**
     * 获取子图的所有权限记录
     *
     * @param subgraphId 子图ID
     * @return 权限列表
     */
    List<SubgraphPermission> getPermissions(Long subgraphId);

    // ==================== 资源节点管理 ====================

    /**
     * 向子图添加资源节点
     *
     * @param subgraphId 子图ID
     * @param resourceIds 资源节点ID列表
     * @param operatorId 操作者用户ID
     * @param operatorName 操作者用户名
     * @throws RuntimeException 如果无权限或资源不存在
     */
    void addResources(Long subgraphId, List<Long> resourceIds, Long operatorId, String operatorName);

    /**
     * 从子图移除资源节点
     *
     * @param subgraphId 子图ID
     * @param resourceIds 资源节点ID列表
     * @param operatorId 操作者用户ID
     * @param operatorName 操作者用户名
     * @throws RuntimeException 如果无权限
     */
    void removeResources(Long subgraphId, List<Long> resourceIds, Long operatorId, String operatorName);

    /**
     * 获取子图中的资源节点ID列表
     *
     * @param subgraphId 子图ID
     * @param userId 用户ID（用于权限检查）
     * @return 资源节点ID列表
     * @throws RuntimeException 如果无权限
     */
    List<Long> getResourceIds(Long subgraphId, Long userId);

    /**
     * 统计子图中的资源节点数量
     *
     * @param subgraphId 子图ID
     * @return 资源节点数量
     */
    int countResources(Long subgraphId);

    /**
     * 检查子图是否为空
     *
     * @param subgraphId 子图ID
     * @return true 如果子图不包含任何资源节点
     */
    boolean isSubgraphEmpty(Long subgraphId);

    /**
     * 分页查询子图中的资源关联记录
     *
     * @param subgraphId 子图ID
     * @param userId 用户ID（用于权限检查）
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 资源关联列表
     * @throws RuntimeException 如果无权限访问
     */
    List<com.catface996.aiops.domain.model.subgraph.SubgraphResource> getSubgraphResourcesPaged(
            Long subgraphId, Long userId, int page, int size);

    // ==================== 拓扑查询 ====================

    /**
     * 获取子图拓扑结构
     *
     * <p>执行以下操作：</p>
     * <ol>
     *   <li>验证子图存在</li>
     *   <li>验证用户有权限访问（Owner 或 Viewer）</li>
     *   <li>获取子图中的所有资源节点ID</li>
     *   <li>获取这些节点之间的关系</li>
     *   <li>过滤关系，仅保留子图内节点之间的关系</li>
     * </ol>
     *
     * @param subgraphId 子图ID
     * @param userId 用户ID（用于权限检查）
     * @return 子图拓扑结构
     * @throws RuntimeException 如果子图不存在或无权限访问
     */
    com.catface996.aiops.domain.model.subgraph.SubgraphTopology getSubgraphTopology(Long subgraphId, Long userId);
}
