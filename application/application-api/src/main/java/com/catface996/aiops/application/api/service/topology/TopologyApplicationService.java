package com.catface996.aiops.application.api.service.topology;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.node.NodeDTO;
import com.catface996.aiops.application.api.dto.topology.TopologyDTO;
import com.catface996.aiops.application.api.dto.topology.TopologyGraphDTO;
import com.catface996.aiops.application.api.dto.topology.request.CreateTopologyRequest;
import com.catface996.aiops.application.api.dto.topology.request.QueryMembersRequest;
import com.catface996.aiops.application.api.dto.topology.request.QueryTopologiesRequest;
import com.catface996.aiops.application.api.dto.topology.request.QueryTopologyGraphRequest;
import com.catface996.aiops.application.api.dto.topology.request.UpdateTopologyRequest;

import java.util.List;

/**
 * 拓扑图应用服务接口
 *
 * <p>提供拓扑图管理的应用层接口，协调领域层完成业务逻辑。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>DTO 与领域模型的转换</li>
 *   <li>调用领域服务完成业务逻辑</li>
 *   <li>事务边界管理</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001~008: 资源分类体系设计</li>
 *   <li>US1: 查询所有拓扑图</li>
 *   <li>US3: 创建拓扑图</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-25
 */
public interface TopologyApplicationService {

    /**
     * 创建拓扑图
     *
     * @param request 创建请求
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 创建的拓扑图 DTO
     */
    TopologyDTO createTopology(CreateTopologyRequest request, Long operatorId, String operatorName);

    /**
     * 分页查询拓扑图列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<TopologyDTO> listTopologies(QueryTopologiesRequest request);

    /**
     * 获取拓扑图详情
     *
     * @param topologyId 拓扑图ID
     * @return 拓扑图 DTO，如不存在返回 null
     */
    TopologyDTO getTopologyById(Long topologyId);

    /**
     * 更新拓扑图
     *
     * @param topologyId 拓扑图ID
     * @param request 更新请求
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 更新后的拓扑图 DTO
     */
    TopologyDTO updateTopology(Long topologyId, UpdateTopologyRequest request,
                               Long operatorId, String operatorName);

    /**
     * 删除拓扑图
     *
     * @param topologyId 拓扑图ID
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    void deleteTopology(Long topologyId, Long operatorId, String operatorName);

    // ===== 成员管理方法 =====

    /**
     * 添加成员到拓扑图
     *
     * @param topologyId   拓扑图ID
     * @param nodeIds      节点ID列表
     * @param operatorId   操作人ID
     * @param operatorName 操作人姓名
     */
    void addMembers(Long topologyId, List<Long> nodeIds, Long operatorId, String operatorName);

    /**
     * 从拓扑图移除成员
     *
     * @param topologyId   拓扑图ID
     * @param nodeIds      节点ID列表
     * @param operatorId   操作人ID
     * @param operatorName 操作人姓名
     */
    void removeMembers(Long topologyId, List<Long> nodeIds, Long operatorId, String operatorName);

    /**
     * 查询拓扑图成员列表
     *
     * @param request 查询请求
     * @return 成员列表（分页）
     */
    PageResult<NodeDTO> queryMembers(QueryMembersRequest request);

    // ===== 拓扑图数据查询 =====

    /**
     * 获取拓扑图数据
     *
     * <p>获取拓扑图的节点和边数据，用于图形渲染。</p>
     *
     * @param request 查询请求
     * @return 拓扑图数据
     */
    TopologyGraphDTO getTopologyGraph(QueryTopologyGraphRequest request);

    // ===== Global Supervisor Agent 绑定方法 =====

    /**
     * 绑定 Global Supervisor Agent
     *
     * <p>将指定的 Agent 绑定到拓扑图作为 Global Supervisor。</p>
     *
     * @param topologyId 拓扑图ID
     * @param agentId    Agent ID（必须为 GLOBAL_SUPERVISOR 角色）
     * @param operatorId 操作人ID
     * @return 更新后的拓扑图 DTO
     */
    TopologyDTO bindGlobalSupervisorAgent(Long topologyId, Long agentId, Long operatorId);

    /**
     * 解绑 Global Supervisor Agent
     *
     * <p>解除拓扑图与 Global Supervisor Agent 的绑定关系。</p>
     *
     * @param topologyId 拓扑图ID
     * @param operatorId 操作人ID
     * @return 更新后的拓扑图 DTO
     */
    TopologyDTO unbindGlobalSupervisorAgent(Long topologyId, Long operatorId);
}
