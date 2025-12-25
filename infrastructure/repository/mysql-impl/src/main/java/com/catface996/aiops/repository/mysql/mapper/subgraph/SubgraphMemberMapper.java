package com.catface996.aiops.repository.mysql.mapper.subgraph;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.subgraph.SubgraphMemberPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 子图成员关联 Mapper 接口
 *
 * <p>提供子图成员关联数据的数据库访问操作</p>
 * <p>v2.0 设计：子图作为资源类型，成员可以是任意资源（包括嵌套子图）</p>
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
@Mapper
public interface SubgraphMemberMapper extends BaseMapper<SubgraphMemberPO> {

    // ==================== 批量操作 ====================

    /**
     * 批量插入成员关联
     *
     * @param list 成员关联列表
     * @return 插入的行数
     */
    int batchInsert(@Param("list") List<SubgraphMemberPO> list);

    /**
     * 批量删除成员关联
     *
     * @param subgraphId 子图 ID
     * @param memberIds  成员 ID 列表
     * @return 删除的行数
     */
    int batchDeleteBySubgraphIdAndMemberIds(@Param("subgraphId") Long subgraphId,
                                             @Param("memberIds") List<Long> memberIds);

    // ==================== 基础查询 ====================

    /**
     * 根据子图 ID 查询所有成员（含成员详情 JOIN 查询）
     *
     * @param subgraphId 子图 ID
     * @return 成员关联列表（含派生属性）
     */
    List<SubgraphMemberPO> selectBySubgraphIdWithDetails(@Param("subgraphId") Long subgraphId);

    /**
     * 分页查询子图成员（含成员详情 JOIN 查询）
     *
     * @param subgraphId 子图 ID
     * @param offset     偏移量
     * @param limit      每页大小
     * @return 成员关联列表（含派生属性）
     */
    List<SubgraphMemberPO> selectBySubgraphIdPagedWithDetails(@Param("subgraphId") Long subgraphId,
                                                               @Param("offset") int offset,
                                                               @Param("limit") int limit);

    /**
     * 查询子图的所有成员 ID
     *
     * @param subgraphId 子图 ID
     * @return 成员 ID 列表
     */
    List<Long> selectMemberIdsBySubgraphId(@Param("subgraphId") Long subgraphId);

    /**
     * 统计子图的成员数量
     *
     * @param subgraphId 子图 ID
     * @return 成员数量
     */
    int countBySubgraphId(@Param("subgraphId") Long subgraphId);

    // ==================== 反向查询（用于祖先查询和循环检测）====================

    /**
     * 查询包含指定成员的所有子图 ID（直接父级）
     * <p>用于祖先链查询和循环检测</p>
     *
     * @param memberId 成员资源 ID
     * @return 父子图 ID 列表
     */
    List<Long> selectSubgraphIdsByMemberId(@Param("memberId") Long memberId);

    /**
     * 查询包含指定成员的所有子图关联
     *
     * @param memberId 成员资源 ID
     * @return 父子图成员关联列表
     */
    List<SubgraphMemberPO> selectByMemberId(@Param("memberId") Long memberId);

    // ==================== 存在性检查 ====================

    /**
     * 检查成员是否已在子图中
     *
     * @param subgraphId 子图 ID
     * @param memberId   成员 ID
     * @return 存在返回 1，不存在返回 0
     */
    int existsBySubgraphIdAndMemberId(@Param("subgraphId") Long subgraphId,
                                       @Param("memberId") Long memberId);

    /**
     * 检查子图是否有任何成员
     *
     * @param subgraphId 子图 ID
     * @return 有成员返回 1，无成员返回 0
     */
    int hasMembers(@Param("subgraphId") Long subgraphId);

    // ==================== 类型检查 ====================

    /**
     * 检查指定资源是否为子图类型
     *
     * @param resourceId 资源 ID
     * @return 是子图类型返回 1，否则返回 0
     */
    int isSubgraphType(@Param("resourceId") Long resourceId);

    /**
     * 批量筛选子图类型的资源 ID
     *
     * @param resourceIds 资源 ID 列表
     * @return 子图类型的资源 ID 列表
     */
    List<Long> filterSubgraphTypeIds(@Param("resourceIds") List<Long> resourceIds);

    // ==================== 删除操作 ====================

    /**
     * 删除单个成员关联
     *
     * @param subgraphId 子图 ID
     * @param memberId   成员 ID
     * @return 删除的行数
     */
    int deleteBySubgraphIdAndMemberId(@Param("subgraphId") Long subgraphId,
                                       @Param("memberId") Long memberId);

    /**
     * 删除子图的所有成员关联
     *
     * @param subgraphId 子图 ID
     * @return 删除的行数
     */
    int deleteAllBySubgraphId(@Param("subgraphId") Long subgraphId);
}
