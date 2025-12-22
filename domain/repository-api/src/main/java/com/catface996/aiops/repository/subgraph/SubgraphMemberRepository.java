package com.catface996.aiops.repository.subgraph;

import com.catface996.aiops.repository.subgraph.entity.SubgraphMemberEntity;

import java.util.List;
import java.util.Optional;

/**
 * 子图成员仓储接口
 *
 * <p>提供子图成员关联的数据访问操作，遵循 DDD 仓储模式。</p>
 * <p>v2.0 设计：子图作为资源类型，成员可以是任意资源（包括嵌套子图）</p>
 *
 * <p>实现说明：</p>
 * <ul>
 *   <li>使用 MyBatis-Plus 实现数据访问</li>
 *   <li>数据存储在 MySQL 数据库的 subgraph_member 表</li>
 *   <li>支持事务管理</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求5: 向子图添加成员资源</li>
 *   <li>需求6: 从子图移除成员资源</li>
 *   <li>需求8: 成员列表查询</li>
 *   <li>需求9: 拓扑数据查询</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
public interface SubgraphMemberRepository {

    // ==================== 基本 CRUD 操作 ====================

    /**
     * 保存子图成员关联
     *
     * @param member 成员关联实体
     * @return 保存后的实体（包含生成的 ID）
     */
    SubgraphMemberEntity save(SubgraphMemberEntity member);

    /**
     * 批量保存子图成员关联
     *
     * @param members 成员关联列表
     * @return 保存的行数
     */
    int batchSave(List<SubgraphMemberEntity> members);

    /**
     * 根据子图 ID 和成员 ID 查询关联
     *
     * @param subgraphId 子图 ID
     * @param memberId   成员 ID
     * @return 关联实体（如果存在）
     */
    Optional<SubgraphMemberEntity> findBySubgraphIdAndMemberId(Long subgraphId, Long memberId);

    /**
     * 删除单个成员关联
     *
     * @param subgraphId 子图 ID
     * @param memberId   成员 ID
     * @return 是否删除成功
     */
    boolean deleteBySubgraphIdAndMemberId(Long subgraphId, Long memberId);

    /**
     * 批量删除成员关联
     *
     * @param subgraphId 子图 ID
     * @param memberIds  成员 ID 列表
     * @return 删除的行数
     */
    int batchDelete(Long subgraphId, List<Long> memberIds);

    /**
     * 删除子图的所有成员关联
     *
     * @param subgraphId 子图 ID
     * @return 删除的行数
     */
    int deleteAllBySubgraphId(Long subgraphId);

    // ==================== 查询操作 ====================

    /**
     * 查询子图的所有成员（不分页，含成员详情）
     *
     * @param subgraphId 子图 ID
     * @return 成员列表（含派生属性）
     */
    List<SubgraphMemberEntity> findBySubgraphId(Long subgraphId);

    /**
     * 分页查询子图成员（含成员详情）
     *
     * @param subgraphId 子图 ID
     * @param offset     偏移量
     * @param limit      每页大小
     * @return 成员列表（含派生属性）
     */
    List<SubgraphMemberEntity> findBySubgraphIdPaged(Long subgraphId, int offset, int limit);

    /**
     * 查询子图的所有成员 ID
     *
     * @param subgraphId 子图 ID
     * @return 成员 ID 列表
     */
    List<Long> findMemberIdsBySubgraphId(Long subgraphId);

    /**
     * 统计子图的成员数量
     *
     * @param subgraphId 子图 ID
     * @return 成员数量
     */
    int countBySubgraphId(Long subgraphId);

    // ==================== 反向查询（用于祖先查询和循环检测）====================

    /**
     * 查询包含指定成员的所有子图 ID（直接父级）
     * <p>用于祖先链查询和循环检测</p>
     *
     * @param memberId 成员资源 ID
     * @return 父子图 ID 列表
     */
    List<Long> findSubgraphIdsByMemberId(Long memberId);

    /**
     * 查询包含指定成员的所有子图
     *
     * @param memberId 成员资源 ID
     * @return 父子图成员关联列表
     */
    List<SubgraphMemberEntity> findByMemberId(Long memberId);

    // ==================== 存在性检查 ====================

    /**
     * 检查成员是否已在子图中
     *
     * @param subgraphId 子图 ID
     * @param memberId   成员 ID
     * @return true 如果成员已在子图中
     */
    boolean existsBySubgraphIdAndMemberId(Long subgraphId, Long memberId);

    /**
     * 检查子图是否有任何成员
     *
     * @param subgraphId 子图 ID
     * @return true 如果子图有成员
     */
    boolean hasMembers(Long subgraphId);

    // ==================== 类型检查 ====================

    /**
     * 检查指定资源是否为子图类型
     *
     * @param resourceId 资源 ID
     * @return true 如果是 SUBGRAPH 类型
     */
    boolean isSubgraphType(Long resourceId);

    /**
     * 批量检查资源是否为子图类型
     *
     * @param resourceIds 资源 ID 列表
     * @return 子图类型的资源 ID 列表
     */
    List<Long> filterSubgraphTypeIds(List<Long> resourceIds);
}
