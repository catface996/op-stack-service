package com.catface996.aiops.repository.node;

import com.catface996.aiops.domain.model.node.Node;
import com.catface996.aiops.domain.model.node.NodeStatus;

import java.util.List;
import java.util.Optional;

/**
 * 资源节点仓储接口
 *
 * <p>提供资源节点实体的数据访问操作。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: resource 表拆分为 topology 表和 node 表</li>
 *   <li>FR-007: 资源节点 API 路径变更为 /api/v1/nodes/*</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public interface NodeRepository {

    /**
     * 根据ID查询节点
     *
     * @param id 节点ID
     * @return 节点实体（如果存在）
     */
    Optional<Node> findById(Long id);

    /**
     * 根据ID查询节点，包含类型信息
     *
     * @param id 节点ID
     * @return 节点实体（如果存在）
     */
    Optional<Node> findByIdWithType(Long id);

    /**
     * 根据类型ID和名称查询节点
     *
     * @param nodeTypeId 节点类型ID
     * @param name       节点名称
     * @return 节点实体（如果存在）
     */
    Optional<Node> findByTypeIdAndName(Long nodeTypeId, String name);

    /**
     * 根据名称查询节点
     *
     * @param name 节点名称
     * @return 节点实体（如果存在）
     */
    Optional<Node> findByName(String name);

    /**
     * 分页查询节点列表
     *
     * @param nodeTypeId 节点类型ID筛选（可选）
     * @param status     状态筛选（可选）
     * @param keyword    关键词模糊查询（可选，搜索名称和描述）
     * @param topologyId 拓扑图ID筛选（可选，只查询属于指定拓扑图的节点）
     * @param page       页码（从1开始）
     * @param size       每页大小
     * @return 节点列表
     */
    List<Node> findByCondition(Long nodeTypeId, NodeStatus status, String keyword, Long topologyId, int page, int size);

    /**
     * 按条件统计节点数量
     *
     * @param nodeTypeId 节点类型ID筛选（可选）
     * @param status     状态筛选（可选）
     * @param keyword    关键词模糊查询（可选，搜索名称和描述）
     * @param topologyId 拓扑图ID筛选（可选）
     * @return 节点数量
     */
    long countByCondition(Long nodeTypeId, NodeStatus status, String keyword, Long topologyId);

    /**
     * 保存节点
     *
     * @param node 节点实体
     * @return 保存后的节点实体
     */
    Node save(Node node);

    /**
     * 更新节点（使用乐观锁）
     *
     * @param node 节点实体
     * @return 更新是否成功
     */
    boolean update(Node node);

    /**
     * 删除节点
     *
     * @param id 节点ID
     */
    void deleteById(Long id);

    /**
     * 检查节点是否存在
     *
     * @param id 节点ID
     * @return true if node exists
     */
    boolean existsById(Long id);

    /**
     * 检查节点名称是否已存在
     *
     * @param name 节点名称
     * @return true if name exists
     */
    boolean existsByName(String name);

    /**
     * 检查同类型下节点名称是否已存在
     *
     * @param name       节点名称
     * @param nodeTypeId 节点类型ID
     * @return true if name exists in the same type
     */
    boolean existsByNameAndTypeId(String name, Long nodeTypeId);

    /**
     * 批量检查节点是否存在
     *
     * @param ids 节点ID列表
     * @return 存在的节点ID列表
     */
    List<Long> findExistingIds(List<Long> ids);
}
