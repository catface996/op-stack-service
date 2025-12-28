package com.catface996.aiops.repository.agent;

import com.catface996.aiops.domain.model.agent.AgentStatus;
import com.catface996.aiops.domain.model.agent.AgentTeamRelation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Agent-Team 关联仓储接口
 *
 * <p>提供 Agent 与 Team 关联关系的数据访问操作。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public interface AgentTeamRelationRepository {

    /**
     * 根据ID查询关联
     *
     * @param id 关联ID
     * @return 关联实体（如果存在且未删除）
     */
    Optional<AgentTeamRelation> findById(Long id);

    /**
     * 根据 Agent ID 和 Team ID 查询关联
     *
     * @param agentId Agent ID
     * @param teamId  Team ID
     * @return 关联实体（如果存在且未删除）
     */
    Optional<AgentTeamRelation> findByAgentIdAndTeamId(Long agentId, Long teamId);

    /**
     * 根据 Agent ID 查询所有关联
     *
     * @param agentId Agent ID
     * @return 关联列表
     */
    List<AgentTeamRelation> findByAgentId(Long agentId);

    /**
     * 根据 Team ID 查询所有关联
     *
     * @param teamId Team ID
     * @return 关联列表
     */
    List<AgentTeamRelation> findByTeamId(Long teamId);

    /**
     * 根据 Team ID 和状态查询关联
     *
     * @param teamId Team ID
     * @param status Agent 状态
     * @return 关联列表
     */
    List<AgentTeamRelation> findByTeamIdAndStatus(Long teamId, AgentStatus status);

    /**
     * 检查关联是否已存在
     *
     * @param agentId Agent ID
     * @param teamId  Team ID
     * @return true 如果关联已存在
     */
    boolean existsByAgentIdAndTeamId(Long agentId, Long teamId);

    /**
     * 统计 Team 中的 Agent 数量
     *
     * @param teamId Team ID
     * @return Agent 数量
     */
    long countByTeamId(Long teamId);

    /**
     * 统计各状态的关联数量
     *
     * @return 状态 -> 数量 的映射
     */
    Map<AgentStatus, Long> countByStatus();

    /**
     * 保存关联
     *
     * @param relation 关联实体
     * @return 保存后的关联实体（包含生成的ID）
     */
    AgentTeamRelation save(AgentTeamRelation relation);

    /**
     * 更新关联
     *
     * @param relation 关联实体
     * @return 更新后的关联实体
     */
    AgentTeamRelation update(AgentTeamRelation relation);

    /**
     * 删除关联（软删除）
     *
     * @param id 关联ID
     * @return 删除是否成功
     */
    boolean deleteById(Long id);

    /**
     * 根据 Agent ID 和 Team ID 删除关联（软删除）
     *
     * @param agentId Agent ID
     * @param teamId  Team ID
     * @return 删除是否成功
     */
    boolean deleteByAgentIdAndTeamId(Long agentId, Long teamId);

    /**
     * 删除 Agent 的所有关联（软删除）
     *
     * @param agentId Agent ID
     * @return 删除的关联数量
     */
    int deleteByAgentId(Long agentId);

    /**
     * 检查 Agent 在任意 Team 中是否处于工作状态
     *
     * @param agentId Agent ID
     * @return true 如果 Agent 正在工作
     */
    boolean isAgentBusyInAnyTeam(Long agentId);

    /**
     * 获取 Agent 关联的 Team ID 列表
     *
     * @param agentId Agent ID
     * @return Team ID 列表
     */
    List<Long> findTeamIdsByAgentId(Long agentId);
}
