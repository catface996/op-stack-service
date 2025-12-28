package com.catface996.aiops.application.api.service.agent;

import com.catface996.aiops.application.api.dto.agent.AgentDTO;
import com.catface996.aiops.application.api.dto.agent.AgentStatsDTO;
import com.catface996.aiops.application.api.dto.agent.AgentTemplateDTO;
import com.catface996.aiops.application.api.dto.agent.request.*;
import com.catface996.aiops.application.api.dto.common.PageResult;

import java.util.List;

/**
 * Agent 应用服务接口
 *
 * <p>提供 Agent 管理的应用层接口，协调领域层完成业务逻辑。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>DTO 与领域模型的转换</li>
 *   <li>调用仓储层完成数据操作</li>
 *   <li>事务边界管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public interface AgentApplicationService {

    /**
     * 分页查询 Agent 列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<AgentDTO> listAgents(ListAgentsRequest request);

    /**
     * 获取 Agent 详情
     *
     * @param agentId Agent ID
     * @return Agent 详情 DTO
     */
    AgentDTO getAgentById(Long agentId);

    /**
     * 创建 Agent
     *
     * @param request 创建请求
     * @return 创建的 Agent DTO
     */
    AgentDTO createAgent(CreateAgentRequest request);

    /**
     * 更新 Agent 信息
     *
     * @param request 更新请求
     * @return 更新后的 Agent DTO
     */
    AgentDTO updateAgent(UpdateAgentRequest request);

    /**
     * 更新 Agent 配置
     *
     * @param request 配置更新请求
     * @return 更新后的 Agent DTO
     */
    AgentDTO updateAgentConfig(UpdateAgentConfigRequest request);

    /**
     * 删除 Agent（软删除）
     *
     * @param request 删除请求
     */
    void deleteAgent(DeleteAgentRequest request);

    /**
     * 分配 Agent 到团队
     *
     * @param request 分配请求
     */
    void assignAgent(AssignAgentRequest request);

    /**
     * 取消 Agent 团队分配
     *
     * @param request 取消分配请求
     */
    void unassignAgent(UnassignAgentRequest request);

    /**
     * 获取 Agent 模板列表
     *
     * @return 模板列表
     */
    List<AgentTemplateDTO> listAgentTemplates();

    /**
     * 获取 Agent 统计信息
     *
     * @param request 统计请求
     * @return 统计信息
     */
    AgentStatsDTO getAgentStats(AgentStatsRequest request);
}
