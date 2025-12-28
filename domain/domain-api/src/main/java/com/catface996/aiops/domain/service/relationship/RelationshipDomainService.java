package com.catface996.aiops.domain.service.relationship;

import com.catface996.aiops.domain.model.relationship.*;

import java.util.List;
import java.util.Optional;

/**
 * 资源关系领域服务接口
 *
 * <p>提供资源关系管理的核心业务逻辑，包括：</p>
 * <ul>
 *   <li>关系创建：验证、双向处理、保存</li>
 *   <li>关系查询：分页、条件过滤、上下游依赖</li>
 *   <li>关系更新：权限检查、属性更新</li>
 *   <li>关系删除：权限检查、双向删除</li>
 *   <li>图遍历：循环依赖检测、广度优先遍历</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
public interface RelationshipDomainService {

    /**
     * 创建关系
     *
     * <p>执行以下操作：</p>
     * <ol>
     *   <li>验证源资源和目标资源存在</li>
     *   <li>验证用户对两个资源都有访问权限</li>
     *   <li>检查关系是否已存在</li>
     *   <li>保存关系</li>
     *   <li>如果是双向关系，同时创建反向关系</li>
     * </ol>
     *
     * @param sourceResourceId 源资源ID
     * @param targetResourceId 目标资源ID
     * @param type 关系类型
     * @param direction 关系方向
     * @param strength 关系强度
     * @param description 关系描述
     * @param operatorId 操作人ID
     * @return 创建的关系实体
     * @throws IllegalArgumentException 如果参数无效
     * @throws RuntimeException 如果资源不存在、权限不足或关系已存在
     */
    Relationship createRelationship(Long sourceResourceId, Long targetResourceId,
                                     RelationshipType type, RelationshipDirection direction,
                                     RelationshipStrength strength, String description,
                                     Long topologyId, Long operatorId);

    /**
     * 分页查询关系列表
     *
     * @param sourceResourceId 源资源ID（可选）
     * @param targetResourceId 目标资源ID（可选）
     * @param type 关系类型（可选）
     * @param status 关系状态（可选）
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 关系列表
     */
    List<Relationship> listRelationships(Long sourceResourceId, Long targetResourceId,
                                          RelationshipType type, RelationshipStatus status,
                                          int pageNum, int pageSize);

    /**
     * 统计关系数量
     *
     * @param sourceResourceId 源资源ID（可选）
     * @param targetResourceId 目标资源ID（可选）
     * @param type 关系类型（可选）
     * @param status 关系状态（可选）
     * @return 关系数量
     */
    long countRelationships(Long sourceResourceId, Long targetResourceId,
                            RelationshipType type, RelationshipStatus status);

    /**
     * 根据ID获取关系详情
     *
     * @param relationshipId 关系ID
     * @return 关系实体（如果存在）
     */
    Optional<Relationship> getRelationshipById(Long relationshipId);

    /**
     * 获取资源的上游依赖（指向该资源的关系）
     *
     * @param resourceId 资源ID
     * @return 上游依赖列表
     */
    List<Relationship> getUpstreamDependencies(Long resourceId);

    /**
     * 获取资源的下游依赖（从该资源发起的关系）
     *
     * @param resourceId 资源ID
     * @return 下游依赖列表
     */
    List<Relationship> getDownstreamDependencies(Long resourceId);

    /**
     * 更新关系
     *
     * <p>执行以下操作：</p>
     * <ol>
     *   <li>验证关系存在</li>
     *   <li>检查用户权限（源资源或目标资源的Owner）</li>
     *   <li>更新关系属性</li>
     * </ol>
     *
     * @param relationshipId 关系ID
     * @param type 新关系类型（可选，null表示不修改）
     * @param strength 新关系强度（可选）
     * @param status 新关系状态（可选）
     * @param description 新关系描述（可选）
     * @param operatorId 操作人ID
     * @return 更新后的关系实体
     * @throws RuntimeException 如果关系不存在或权限不足
     */
    Relationship updateRelationship(Long relationshipId, RelationshipType type,
                                     RelationshipStrength strength, RelationshipStatus status,
                                     String description, Long operatorId);

    /**
     * 删除关系
     *
     * <p>执行以下操作：</p>
     * <ol>
     *   <li>验证关系存在</li>
     *   <li>检查用户权限（源资源或目标资源的Owner）</li>
     *   <li>删除关系</li>
     *   <li>如果是双向关系，同时删除反向关系</li>
     * </ol>
     *
     * @param relationshipId 关系ID
     * @param operatorId 操作人ID
     * @throws RuntimeException 如果关系不存在或权限不足
     */
    void deleteRelationship(Long relationshipId, Long operatorId);

    /**
     * 删除资源的所有关系（级联删除）
     *
     * @param resourceId 资源ID
     */
    void deleteRelationshipsByResource(Long resourceId);

    /**
     * 检测循环依赖
     *
     * <p>使用DFS算法检测从指定资源开始是否存在循环依赖</p>
     *
     * @param resourceId 起始资源ID
     * @return 循环依赖检测结果
     */
    CycleDetectionResult detectCycle(Long resourceId);

    /**
     * 广度优先遍历
     *
     * <p>从指定资源开始，按层级遍历所有依赖资源</p>
     *
     * @param resourceId 起始资源ID
     * @param maxDepth 最大遍历深度
     * @return 遍历结果
     */
    TraverseResult traverse(Long resourceId, int maxDepth);
}
