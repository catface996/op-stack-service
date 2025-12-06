package com.catface996.aiops.repository.subgraph;

import com.catface996.aiops.domain.model.subgraph.SubgraphResource;

import java.util.List;

/**
 * 子图资源关联仓储接口
 *
 * <p>提供子图与资源节点关联关系的数据访问操作，遵循DDD仓储模式。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求5: 向子图添加资源节点</li>
 *   <li>需求6: 从子图移除资源节点</li>
 *   <li>需求7.3: 拓扑图只显示子图内节点之间的关系</li>
 *   <li>需求澄清2: 资源节点与子图的多对多关系</li>
 *   <li>需求10.2: 资源节点从系统中删除时自动移除子图关联</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
public interface SubgraphResourceRepository {

    /**
     * 添加资源关联
     *
     * @param subgraphResource 子图资源关联实体
     * @return 保存后的关联实体（包含生成的ID）
     * @throws IllegalArgumentException 如果subgraphResource为null
     */
    SubgraphResource addResource(SubgraphResource subgraphResource);

    /**
     * 批量添加资源关联
     *
     * @param subgraphResources 子图资源关联实体列表
     */
    void addResources(List<SubgraphResource> subgraphResources);

    /**
     * 移除资源关联
     *
     * @param subgraphId 子图ID
     * @param resourceId 资源节点ID
     */
    void removeResource(Long subgraphId, Long resourceId);

    /**
     * 批量移除资源关联
     *
     * @param subgraphId 子图ID
     * @param resourceIds 资源节点ID列表
     */
    void removeResources(Long subgraphId, List<Long> resourceIds);

    /**
     * 查询子图中的所有资源节点ID
     *
     * @param subgraphId 子图ID
     * @return 资源节点ID列表
     */
    List<Long> findResourceIdsBySubgraphId(Long subgraphId);

    /**
     * 查询子图中的所有资源关联记录
     *
     * @param subgraphId 子图ID
     * @return 资源关联列表
     */
    List<SubgraphResource> findBySubgraphId(Long subgraphId);

    /**
     * 统计子图中的资源节点数量
     *
     * @param subgraphId 子图ID
     * @return 资源节点数量
     */
    int countBySubgraphId(Long subgraphId);

    /**
     * 查询包含指定资源节点的所有子图ID
     *
     * @param resourceId 资源节点ID
     * @return 子图ID列表
     */
    List<Long> findSubgraphIdsByResourceId(Long resourceId);

    /**
     * 检查资源节点是否已在子图中
     *
     * @param subgraphId 子图ID
     * @param resourceId 资源节点ID
     * @return true 如果资源已在子图中
     */
    boolean existsInSubgraph(Long subgraphId, Long resourceId);

    /**
     * 删除子图的所有资源关联
     * <p>用于子图删除前清理资源关联</p>
     *
     * @param subgraphId 子图ID
     */
    void deleteAllBySubgraphId(Long subgraphId);

    /**
     * 删除资源节点在所有子图中的关联
     * <p>用于资源节点删除时级联删除</p>
     *
     * @param resourceId 资源节点ID
     */
    void deleteAllByResourceId(Long resourceId);

    /**
     * 检查子图是否为空（不包含任何资源节点）
     *
     * @param subgraphId 子图ID
     * @return true 如果子图为空
     */
    boolean isSubgraphEmpty(Long subgraphId);

    /**
     * 分页查询子图中的资源关联记录
     *
     * @param subgraphId 子图ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 资源关联列表
     */
    List<SubgraphResource> findBySubgraphIdPaged(Long subgraphId, int page, int size);
}
