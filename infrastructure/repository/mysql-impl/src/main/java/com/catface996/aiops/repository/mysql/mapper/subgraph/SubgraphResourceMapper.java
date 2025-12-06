package com.catface996.aiops.repository.mysql.mapper.subgraph;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.subgraph.SubgraphResourcePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 子图资源关联 Mapper 接口
 *
 * <p>提供子图资源关联数据的数据库访问操作</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求5: 向子图添加资源节点</li>
 *   <li>需求6: 从子图移除资源节点</li>
 *   <li>需求7.3: 拓扑图只显示子图内节点之间的关系</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
public interface SubgraphResourceMapper extends BaseMapper<SubgraphResourcePO> {

    /**
     * 批量插入资源关联
     *
     * @param list 资源关联列表
     * @return 插入的行数
     */
    int batchInsert(@Param("list") List<SubgraphResourcePO> list);

    /**
     * 根据子图ID查询所有资源关联
     *
     * @param subgraphId 子图ID
     * @return 资源关联列表
     */
    List<SubgraphResourcePO> selectBySubgraphId(@Param("subgraphId") Long subgraphId);

    /**
     * 查询子图中的所有资源节点ID
     *
     * @param subgraphId 子图ID
     * @return 资源节点ID列表
     */
    List<Long> selectResourceIdsBySubgraphId(@Param("subgraphId") Long subgraphId);

    /**
     * 统计子图中的资源节点数量
     *
     * @param subgraphId 子图ID
     * @return 资源节点数量
     */
    int countBySubgraphId(@Param("subgraphId") Long subgraphId);

    /**
     * 查询包含指定资源节点的所有子图ID
     *
     * @param resourceId 资源节点ID
     * @return 子图ID列表
     */
    List<Long> selectSubgraphIdsByResourceId(@Param("resourceId") Long resourceId);

    /**
     * 检查资源节点是否已在子图中
     *
     * @param subgraphId 子图ID
     * @param resourceId 资源节点ID
     * @return 存在返回1，不存在返回0
     */
    int existsInSubgraph(@Param("subgraphId") Long subgraphId, @Param("resourceId") Long resourceId);

    /**
     * 移除单个资源关联
     *
     * @param subgraphId 子图ID
     * @param resourceId 资源节点ID
     * @return 删除的行数
     */
    int deleteBySubgraphIdAndResourceId(@Param("subgraphId") Long subgraphId,
                                         @Param("resourceId") Long resourceId);

    /**
     * 批量移除资源关联
     *
     * @param subgraphId 子图ID
     * @param resourceIds 资源节点ID列表
     * @return 删除的行数
     */
    int batchDeleteBySubgraphIdAndResourceIds(@Param("subgraphId") Long subgraphId,
                                               @Param("resourceIds") List<Long> resourceIds);

    /**
     * 删除子图的所有资源关联
     *
     * @param subgraphId 子图ID
     * @return 删除的行数
     */
    int deleteAllBySubgraphId(@Param("subgraphId") Long subgraphId);

    /**
     * 删除资源节点在所有子图中的关联
     *
     * @param resourceId 资源节点ID
     * @return 删除的行数
     */
    int deleteAllByResourceId(@Param("resourceId") Long resourceId);

    /**
     * 分页查询子图中的资源关联
     *
     * @param subgraphId 子图ID
     * @param offset 偏移量
     * @param limit 每页大小
     * @return 资源关联列表
     */
    List<SubgraphResourcePO> selectBySubgraphIdPaged(@Param("subgraphId") Long subgraphId,
                                                      @Param("offset") int offset,
                                                      @Param("limit") int limit);
}
