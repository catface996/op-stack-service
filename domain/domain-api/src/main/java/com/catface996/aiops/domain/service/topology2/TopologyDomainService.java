package com.catface996.aiops.domain.service.topology2;

import com.catface996.aiops.domain.model.topology.Topology;
import com.catface996.aiops.domain.model.topology.TopologyGraphData;
import com.catface996.aiops.domain.model.topology.TopologyStatus;

import java.util.List;
import java.util.Optional;

/**
 * 拓扑图领域服务接口（新版本）
 *
 * <p>提供拓扑图管理的核心业务逻辑，使用独立的 topology 表。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: 系统必须将 resource 表拆分为 topology 表和 node 表</li>
 *   <li>FR-002: topology 表字段定义</li>
 *   <li>FR-006: 拓扑图 API 保持接口契约不变</li>
 *   <li>FR-013: 支持 coordinator_agent_id 字段</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public interface TopologyDomainService {

    /**
     * 创建拓扑图
     *
     * @param name               拓扑图名称
     * @param description        拓扑图描述
     * @param coordinatorAgentId 协调 Agent ID（可选）
     * @param operatorId         操作人ID
     * @return 创建的拓扑图
     */
    Topology createTopology(String name, String description, Long coordinatorAgentId, Long operatorId);

    /**
     * 分页查询拓扑图列表
     *
     * @param name   名称模糊查询（可选）
     * @param status 状态筛选（可选）
     * @param page   页码（从1开始）
     * @param size   每页大小
     * @return 拓扑图列表
     */
    List<Topology> listTopologies(String name, TopologyStatus status, int page, int size);

    /**
     * 统计拓扑图数量
     *
     * @param name   名称模糊查询（可选）
     * @param status 状态筛选（可选）
     * @return 拓扑图数量
     */
    long countTopologies(String name, TopologyStatus status);

    /**
     * 根据ID获取拓扑图详情
     *
     * @param topologyId 拓扑图ID
     * @return 拓扑图实体
     */
    Optional<Topology> getTopologyById(Long topologyId);

    /**
     * 更新拓扑图
     *
     * @param topologyId         拓扑图ID
     * @param name               新名称（可选）
     * @param description        新描述（可选）
     * @param coordinatorAgentId 协调 Agent ID（可选）
     * @param version            当前版本号（乐观锁）
     * @param operatorId         操作人ID
     * @return 更新后的拓扑图
     */
    Topology updateTopology(Long topologyId, String name, String description,
                            Long coordinatorAgentId, Integer version, Long operatorId);

    /**
     * 删除拓扑图
     *
     * @param topologyId 拓扑图ID
     * @param operatorId 操作人ID
     */
    void deleteTopology(Long topologyId, Long operatorId);

    /**
     * 获取拓扑图的成员数量
     *
     * @param topologyId 拓扑图ID
     * @return 成员数量
     */
    int countMembers(Long topologyId);

    /**
     * 添加成员到拓扑图
     *
     * @param topologyId 拓扑图ID
     * @param nodeIds    节点ID列表
     * @param operatorId 操作人ID
     */
    void addMembers(Long topologyId, List<Long> nodeIds, Long operatorId);

    /**
     * 从拓扑图移除成员
     *
     * @param topologyId 拓扑图ID
     * @param nodeIds    节点ID列表
     * @param operatorId 操作人ID
     */
    void removeMembers(Long topologyId, List<Long> nodeIds, Long operatorId);

    /**
     * 检查拓扑图是否存在
     *
     * @param topologyId 拓扑图ID
     * @return true if topology exists
     */
    boolean existsById(Long topologyId);

    /**
     * 获取拓扑图数据
     *
     * <p>获取拓扑图的节点和边数据，用于图形渲染。</p>
     *
     * @param topologyId           拓扑图ID
     * @param includeRelationships 是否包含节点关系（边）
     * @return 拓扑图数据
     */
    TopologyGraphData getTopologyGraph(Long topologyId, boolean includeRelationships);
}
