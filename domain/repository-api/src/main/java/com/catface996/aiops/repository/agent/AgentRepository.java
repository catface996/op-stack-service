package com.catface996.aiops.repository.agent;

import com.catface996.aiops.domain.model.agent.Agent;
import com.catface996.aiops.domain.model.agent.AgentHierarchyLevel;
import com.catface996.aiops.domain.model.agent.AgentRole;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Agent 仓储接口
 *
 * <p>提供 Agent 实体的数据访问操作。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public interface AgentRepository {

    /**
     * 根据ID查询 Agent
     *
     * @param id Agent ID
     * @return Agent 实体（如果存在且未删除）
     */
    Optional<Agent> findById(Long id);

    /**
     * 分页查询 Agent 列表
     *
     * @param role    角色筛选（可选）
     * @param keyword 关键词搜索（可选，搜索 name, specialty）
     * @param page    页码（从1开始）
     * @param size    每页大小
     * @return Agent 列表
     */
    List<Agent> findByCondition(AgentRole role, String keyword, int page, int size);

    /**
     * 按条件统计 Agent 数量
     *
     * @param role    角色筛选（可选）
     * @param keyword 关键词搜索（可选）
     * @return Agent 数量
     */
    long countByCondition(AgentRole role, String keyword);

    /**
     * 根据名称查询 Agent
     *
     * @param name Agent 名称
     * @return Agent 实体（如果存在且未删除）
     */
    Optional<Agent> findByName(String name);

    /**
     * 检查名称是否已存在
     *
     * @param name      Agent 名称
     * @param excludeId 排除的 Agent ID（用于更新时检查）
     * @return true 如果名称已存在
     */
    boolean existsByName(String name, Long excludeId);

    /**
     * 检查是否存在 GLOBAL_SUPERVISOR
     *
     * @return true 如果已存在 GLOBAL_SUPERVISOR
     */
    boolean existsGlobalSupervisor();

    /**
     * 保存 Agent
     *
     * @param agent Agent 实体
     * @return 保存后的 Agent 实体（包含生成的ID）
     */
    Agent save(Agent agent);

    /**
     * 更新 Agent
     *
     * @param agent Agent 实体
     * @return 更新后的 Agent 实体
     */
    Agent update(Agent agent);

    /**
     * 删除 Agent（软删除）
     *
     * @param id Agent ID
     * @return 删除是否成功
     */
    boolean deleteById(Long id);

    /**
     * 检查 Agent 是否存在
     *
     * @param id Agent ID
     * @return true 如果 Agent 存在且未删除
     */
    boolean existsById(Long id);

    /**
     * 统计各角色的 Agent 数量
     *
     * @return 角色 -> 数量 的映射
     */
    Map<AgentRole, Long> countByRole();

    /**
     * 统计总警告数和严重问题数
     *
     * @return [totalWarnings, totalCritical]
     */
    long[] sumFindings();

    /**
     * 统计指定 Agent 的发现数
     *
     * @param agentId Agent ID
     * @return [warnings, critical]
     */
    long[] sumFindingsById(Long agentId);

    /**
     * 查询未绑定到指定节点的 Agent 列表
     *
     * @param nodeId         节点ID
     * @param excludeAgentIds 要排除的 Agent ID 列表（已绑定的）
     * @param keyword        关键词搜索（可选，搜索 name, specialty）
     * @param page           页码（从1开始）
     * @param size           每页大小
     * @return Agent 列表
     */
    List<Agent> findUnboundByNodeId(Long nodeId, List<Long> excludeAgentIds, String keyword, int page, int size);

    /**
     * 统计未绑定到指定节点的 Agent 数量
     *
     * @param nodeId         节点ID
     * @param excludeAgentIds 要排除的 Agent ID 列表（已绑定的）
     * @param keyword        关键词搜索（可选）
     * @return Agent 数量
     */
    long countUnboundByNodeId(Long nodeId, List<Long> excludeAgentIds, String keyword);

    /**
     * 根据 ID 和角色查询 Agent
     *
     * @param id   Agent ID
     * @param role Agent 角色
     * @return Agent 实体（如果存在且角色匹配）
     */
    Optional<Agent> findByIdAndRole(Long id, AgentRole role);

    /**
     * 根据 ID 和层级查询 Agent
     *
     * @param id             Agent ID
     * @param hierarchyLevel Agent 层级
     * @return Agent 实体（如果存在且层级匹配）
     */
    Optional<Agent> findByIdAndHierarchyLevel(Long id, AgentHierarchyLevel hierarchyLevel);
}
