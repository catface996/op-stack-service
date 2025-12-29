package com.catface996.aiops.application.api.service.agentbound;

import com.catface996.aiops.application.api.dto.agentbound.AgentBoundDTO;
import com.catface996.aiops.application.api.dto.agentbound.HierarchyStructureDTO;

import java.util.List;

/**
 * Agent 绑定关系应用服务接口
 *
 * <p>提供 Agent 与实体绑定关系的应用层接口，协调领域层完成业务逻辑。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>DTO 与领域模型的转换</li>
 *   <li>调用领域服务完成业务逻辑</li>
 *   <li>Agent 存在性验证</li>
 *   <li>事务边界管理</li>
 * </ul>
 *
 * <p>API 端点映射：</p>
 * <ul>
 *   <li>POST /agent-bounds/bind → bindAgent()</li>
 *   <li>POST /agent-bounds/unbind → unbindAgent()</li>
 *   <li>POST /agent-bounds/query-by-entity → queryByEntity()</li>
 *   <li>POST /agent-bounds/query-by-agent → queryByAgent()</li>
 *   <li>POST /agent-bounds/query-hierarchy → queryHierarchy()</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
public interface AgentBoundApplicationService {

    /**
     * 绑定 Agent 到实体
     *
     * <p>验证 Agent 存在且层级匹配，然后执行绑定。</p>
     *
     * @param agentId    Agent ID
     * @param entityId   实体 ID
     * @param entityType 实体类型（TOPOLOGY 或 NODE）
     * @return 创建的绑定关系 DTO
     */
    AgentBoundDTO bindAgent(Long agentId, Long entityId, String entityType);

    /**
     * 解绑 Agent 与实体的关系
     *
     * @param agentId    Agent ID
     * @param entityId   实体 ID
     * @param entityType 实体类型
     */
    void unbindAgent(Long agentId, Long entityId, String entityType);

    /**
     * 按实体查询绑定
     *
     * @param entityType     实体类型
     * @param entityId       实体 ID
     * @param hierarchyLevel 层级过滤（可选）
     * @return 绑定列表 DTO
     */
    List<AgentBoundDTO> queryByEntity(String entityType, Long entityId, String hierarchyLevel);

    /**
     * 按 Agent 查询绑定
     *
     * @param agentId    Agent ID
     * @param entityType 实体类型过滤（可选）
     * @return 绑定列表 DTO
     */
    List<AgentBoundDTO> queryByAgent(Long agentId, String entityType);

    /**
     * 查询 Topology 的层级结构
     *
     * <p>返回完整的层级团队结构，包括：</p>
     * <ul>
     *   <li>Global Supervisor</li>
     *   <li>各 Node 的 Team Supervisor 和 Workers</li>
     * </ul>
     *
     * @param topologyId Topology ID
     * @return 层级结构 DTO
     */
    HierarchyStructureDTO queryHierarchy(Long topologyId);
}
