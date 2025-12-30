package com.catface996.aiops.domain.service.node;

import com.catface996.aiops.domain.model.node.Node;
import com.catface996.aiops.domain.model.node.NodeLayer;
import com.catface996.aiops.domain.model.node.NodeStatus;
import com.catface996.aiops.domain.model.node.NodeType;

import java.util.List;
import java.util.Optional;

/**
 * 资源节点领域服务接口
 *
 * <p>提供资源节点管理的核心业务逻辑，使用独立的 node 表。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: 系统必须将 resource 表拆分为 topology 表和 node 表</li>
 *   <li>FR-003: node 表字段定义</li>
 *   <li>FR-005: 节点 API 保持接口契约不变</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public interface NodeDomainService {

    /**
     * 创建节点
     *
     * @param name        节点名称
     * @param description 节点描述
     * @param nodeTypeId  节点类型ID
     * @param layer       架构层级（可选）
     * @param attributes  扩展属性（JSON格式）
     * @param operatorId  操作人ID
     * @return 创建的节点
     */
    Node createNode(String name, String description, Long nodeTypeId, NodeLayer layer,
                    String attributes, Long operatorId);

    /**
     * 分页查询节点列表
     *
     * @param nodeTypeId 节点类型ID（可选）
     * @param status     状态筛选（可选）
     * @param layer      架构层级筛选（可选）
     * @param keyword    搜索关键词（可选）
     * @param topologyId 拓扑图ID（可选）
     * @param page       页码（从1开始）
     * @param size       每页大小
     * @return 节点列表
     */
    List<Node> listNodes(Long nodeTypeId, NodeStatus status, NodeLayer layer, String keyword,
                         Long topologyId, int page, int size);

    /**
     * 统计节点数量
     *
     * @param nodeTypeId 节点类型ID（可选）
     * @param status     状态筛选（可选）
     * @param layer      架构层级筛选（可选）
     * @param keyword    搜索关键词（可选）
     * @param topologyId 拓扑图ID（可选）
     * @return 节点数量
     */
    long countNodes(Long nodeTypeId, NodeStatus status, NodeLayer layer, String keyword, Long topologyId);

    /**
     * 根据ID获取节点详情
     *
     * @param nodeId 节点ID
     * @return 节点实体
     */
    Optional<Node> getNodeById(Long nodeId);

    /**
     * 更新节点
     *
     * @param nodeId      节点ID
     * @param name        新名称（可选）
     * @param description 新描述（可选）
     * @param attributes  扩展属性（可选）
     * @param version     当前版本号（乐观锁）
     * @param operatorId  操作人ID
     * @return 更新后的节点
     */
    Node updateNode(Long nodeId, String name, String description,
                    String attributes, Integer version, Long operatorId);

    /**
     * 删除节点
     *
     * @param nodeId     节点ID
     * @param operatorId 操作人ID
     */
    void deleteNode(Long nodeId, Long operatorId);

    /**
     * 获取所有节点类型
     *
     * @return 节点类型列表
     */
    List<NodeType> listNodeTypes();

    /**
     * 检查节点是否存在
     *
     * @param nodeId 节点ID
     * @return true if node exists
     */
    boolean existsById(Long nodeId);

    /**
     * 批量检查节点是否存在
     *
     * @param nodeIds 节点ID列表
     * @return 存在的节点ID列表
     */
    List<Long> findExistingIds(List<Long> nodeIds);
}
