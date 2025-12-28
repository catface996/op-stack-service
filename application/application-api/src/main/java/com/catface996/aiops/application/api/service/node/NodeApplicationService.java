package com.catface996.aiops.application.api.service.node;

import com.catface996.aiops.application.api.dto.agent.AgentDTO;
import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.node.NodeAgentRelationDTO;
import com.catface996.aiops.application.api.dto.node.NodeDTO;
import com.catface996.aiops.application.api.dto.node.NodeTypeDTO;
import com.catface996.aiops.application.api.dto.node.request.CreateNodeRequest;
import com.catface996.aiops.application.api.dto.node.request.ListUnboundAgentsRequest;
import com.catface996.aiops.application.api.dto.node.request.QueryNodesRequest;
import com.catface996.aiops.application.api.dto.node.request.UpdateNodeRequest;

import java.util.List;

/**
 * 资源节点应用服务接口
 *
 * <p>提供资源节点管理的应用层接口，协调领域层完成业务逻辑。</p>
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
 *   <li>FR-001: resource 表拆分为 topology 表和 node 表</li>
 *   <li>FR-005: 节点 API 保持接口契约不变</li>
 *   <li>US2: 查询所有节点</li>
 *   <li>US4: 节点管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public interface NodeApplicationService {

    /**
     * 创建节点
     *
     * @param request      创建请求
     * @param operatorId   操作人ID
     * @param operatorName 操作人姓名
     * @return 创建的节点 DTO
     */
    NodeDTO createNode(CreateNodeRequest request, Long operatorId, String operatorName);

    /**
     * 分页查询节点列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<NodeDTO> listNodes(QueryNodesRequest request);

    /**
     * 获取节点详情
     *
     * @param nodeId 节点ID
     * @return 节点 DTO，如不存在返回 null
     */
    NodeDTO getNodeById(Long nodeId);

    /**
     * 更新节点
     *
     * @param nodeId       节点ID
     * @param request      更新请求
     * @param operatorId   操作人ID
     * @param operatorName 操作人姓名
     * @return 更新后的节点 DTO
     */
    NodeDTO updateNode(Long nodeId, UpdateNodeRequest request, Long operatorId, String operatorName);

    /**
     * 删除节点
     *
     * @param nodeId       节点ID
     * @param operatorId   操作人ID
     * @param operatorName 操作人姓名
     */
    void deleteNode(Long nodeId, Long operatorId, String operatorName);

    /**
     * 获取所有节点类型
     *
     * @return 节点类型列表
     */
    List<NodeTypeDTO> listNodeTypes();

    // ==================== Node-Agent 绑定相关方法 ====================

    /**
     * 绑定 Agent 到节点
     *
     * <p>需求追溯：FR-002, US1</p>
     *
     * @param nodeId  节点ID
     * @param agentId Agent ID
     * @return 绑定关系 DTO
     */
    NodeAgentRelationDTO bindAgent(Long nodeId, Long agentId);

    /**
     * 解绑 Agent 与节点的关系
     *
     * <p>需求追溯：FR-003, US4</p>
     *
     * @param nodeId  节点ID
     * @param agentId Agent ID
     */
    void unbindAgent(Long nodeId, Long agentId);

    /**
     * 查询节点关联的 Agent 列表
     *
     * <p>需求追溯：FR-004, US2</p>
     *
     * @param nodeId 节点ID
     * @return Agent DTO 列表
     */
    List<AgentDTO> listAgentsByNode(Long nodeId);

    /**
     * 查询 Agent 关联的节点列表
     *
     * <p>需求追溯：FR-005, US3</p>
     *
     * @param agentId Agent ID
     * @return 节点 DTO 列表
     */
    List<NodeDTO> listNodesByAgent(Long agentId);

    /**
     * 查询未绑定到指定节点的 Agent 列表
     *
     * <p>查询未与指定资源节点绑定的 Agent，支持分页和关键词过滤。</p>
     *
     * @param request 查询请求（包含 nodeId、keyword、page、size）
     * @return Agent DTO 分页结果
     */
    PageResult<AgentDTO> listUnboundAgents(ListUnboundAgentsRequest request);
}
