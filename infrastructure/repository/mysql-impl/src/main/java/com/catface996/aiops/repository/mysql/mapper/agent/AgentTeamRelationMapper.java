package com.catface996.aiops.repository.mysql.mapper.agent;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.agent.AgentTeamRelationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * Agent-Team 关联 Mapper 接口
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Mapper
public interface AgentTeamRelationMapper extends BaseMapper<AgentTeamRelationPO> {

    /**
     * 根据 Agent ID 和 Team ID 查询关联
     *
     * @param agentId Agent ID
     * @param teamId  Team ID
     * @return 关联持久化对象
     */
    @Select("SELECT * FROM agent_2_team WHERE agent_id = #{agentId} AND team_id = #{teamId} AND deleted = 0 LIMIT 1")
    AgentTeamRelationPO selectByAgentIdAndTeamId(@Param("agentId") Long agentId, @Param("teamId") Long teamId);

    /**
     * 根据 Agent ID 查询所有关联
     *
     * @param agentId Agent ID
     * @return 关联列表
     */
    @Select("SELECT * FROM agent_2_team WHERE agent_id = #{agentId} AND deleted = 0")
    List<AgentTeamRelationPO> selectByAgentId(@Param("agentId") Long agentId);

    /**
     * 根据 Team ID 查询所有关联
     *
     * @param teamId Team ID
     * @return 关联列表
     */
    @Select("SELECT * FROM agent_2_team WHERE team_id = #{teamId} AND deleted = 0")
    List<AgentTeamRelationPO> selectByTeamId(@Param("teamId") Long teamId);

    /**
     * 根据 Team ID 和状态查询关联
     *
     * @param teamId Team ID
     * @param status Agent 状态
     * @return 关联列表
     */
    @Select("SELECT * FROM agent_2_team WHERE team_id = #{teamId} AND status = #{status} AND deleted = 0")
    List<AgentTeamRelationPO> selectByTeamIdAndStatus(@Param("teamId") Long teamId, @Param("status") String status);

    /**
     * 检查关联是否已存在
     *
     * @param agentId Agent ID
     * @param teamId  Team ID
     * @return 存在返回 1，不存在返回 0
     */
    @Select("SELECT COUNT(*) FROM agent_2_team WHERE agent_id = #{agentId} AND team_id = #{teamId} AND deleted = 0")
    int countByAgentIdAndTeamId(@Param("agentId") Long agentId, @Param("teamId") Long teamId);

    /**
     * 统计 Team 中的 Agent 数量
     *
     * @param teamId Team ID
     * @return Agent 数量
     */
    @Select("SELECT COUNT(*) FROM agent_2_team WHERE team_id = #{teamId} AND deleted = 0")
    long countByTeamId(@Param("teamId") Long teamId);

    /**
     * 统计各状态的关联数量
     *
     * @return 状态 -> 数量 的列表
     */
    @Select("SELECT status, COUNT(*) as count FROM agent_2_team WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> countGroupByStatus();

    /**
     * 根据 Agent ID 和 Team ID 软删除关联
     *
     * @param agentId Agent ID
     * @param teamId  Team ID
     * @return 删除的行数
     */
    @Update("UPDATE agent_2_team SET deleted = 1 WHERE agent_id = #{agentId} AND team_id = #{teamId} AND deleted = 0")
    int softDeleteByAgentIdAndTeamId(@Param("agentId") Long agentId, @Param("teamId") Long teamId);

    /**
     * 根据 Agent ID 软删除所有关联
     *
     * @param agentId Agent ID
     * @return 删除的行数
     */
    @Update("UPDATE agent_2_team SET deleted = 1 WHERE agent_id = #{agentId} AND deleted = 0")
    int softDeleteByAgentId(@Param("agentId") Long agentId);

    /**
     * 检查 Agent 在任意 Team 中是否处于工作状态
     *
     * @param agentId Agent ID
     * @return 工作中返回 1+，否则返回 0
     */
    @Select("SELECT COUNT(*) FROM agent_2_team WHERE agent_id = #{agentId} AND status IN ('WORKING', 'THINKING') AND deleted = 0")
    int countBusyByAgentId(@Param("agentId") Long agentId);

    /**
     * 获取 Agent 关联的 Team ID 列表
     *
     * @param agentId Agent ID
     * @return Team ID 列表
     */
    @Select("SELECT team_id FROM agent_2_team WHERE agent_id = #{agentId} AND deleted = 0")
    List<Long> selectTeamIdsByAgentId(@Param("agentId") Long agentId);
}
