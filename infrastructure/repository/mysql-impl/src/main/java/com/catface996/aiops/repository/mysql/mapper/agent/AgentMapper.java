package com.catface996.aiops.repository.mysql.mapper.agent;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.repository.mysql.po.agent.AgentPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * Agent Mapper 接口
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Mapper
public interface AgentMapper extends BaseMapper<AgentPO> {

    /**
     * 分页查询 Agent 列表
     *
     * @param page    分页参数
     * @param role    角色筛选（可选）
     * @param teamId  团队筛选（可选）
     * @param keyword 关键词搜索（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT DISTINCT a.* FROM agent a " +
            "<if test='teamId != null'>" +
            "INNER JOIN agent_2_team r ON a.id = r.agent_id AND r.deleted = 0 AND r.team_id = #{teamId} " +
            "</if>" +
            "<where>" +
            "a.deleted = 0 " +
            "<if test='role != null and role != \"\"'>" +
            "AND a.role = #{role} " +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (a.name LIKE CONCAT('%', #{keyword}, '%') OR a.specialty LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "</where>" +
            "ORDER BY a.created_at DESC" +
            "</script>")
    IPage<AgentPO> selectPageByCondition(Page<AgentPO> page,
                                          @Param("role") String role,
                                          @Param("teamId") Long teamId,
                                          @Param("keyword") String keyword);

    /**
     * 按条件统计 Agent 数量
     *
     * @param role    角色筛选（可选）
     * @param teamId  团队筛选（可选）
     * @param keyword 关键词搜索（可选）
     * @return Agent 数量
     */
    @Select("<script>" +
            "SELECT COUNT(DISTINCT a.id) FROM agent a " +
            "<if test='teamId != null'>" +
            "INNER JOIN agent_2_team r ON a.id = r.agent_id AND r.deleted = 0 AND r.team_id = #{teamId} " +
            "</if>" +
            "<where>" +
            "a.deleted = 0 " +
            "<if test='role != null and role != \"\"'>" +
            "AND a.role = #{role} " +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (a.name LIKE CONCAT('%', #{keyword}, '%') OR a.specialty LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "</where>" +
            "</script>")
    long countByCondition(@Param("role") String role,
                          @Param("teamId") Long teamId,
                          @Param("keyword") String keyword);

    /**
     * 根据名称查询 Agent
     *
     * @param name Agent 名称
     * @return Agent 持久化对象
     */
    @Select("SELECT * FROM agent WHERE name = #{name} AND deleted = 0 LIMIT 1")
    AgentPO selectByName(@Param("name") String name);

    /**
     * 检查名称是否已存在（排除指定ID）
     *
     * @param name      Agent 名称
     * @param excludeId 排除的 Agent ID
     * @return 存在返回 1，不存在返回 0
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM agent WHERE name = #{name} AND deleted = 0 " +
            "<if test='excludeId != null'>" +
            "AND id != #{excludeId} " +
            "</if>" +
            "</script>")
    int countByNameExcludeId(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * 检查是否存在 GLOBAL_SUPERVISOR
     *
     * @return 存在返回数量，不存在返回 0
     */
    @Select("SELECT COUNT(*) FROM agent WHERE role = 'GLOBAL_SUPERVISOR' AND deleted = 0")
    int countGlobalSupervisor();

    /**
     * 统计各角色的 Agent 数量
     *
     * @return 角色 -> 数量 的列表
     */
    @Select("SELECT role, COUNT(*) as count FROM agent WHERE deleted = 0 GROUP BY role")
    List<Map<String, Object>> countGroupByRole();

    /**
     * 统计总警告数和严重问题数
     *
     * @return 包含 totalWarnings 和 totalCritical 的 Map
     */
    @Select("SELECT COALESCE(SUM(warnings), 0) as totalWarnings, COALESCE(SUM(critical), 0) as totalCritical FROM agent WHERE deleted = 0")
    Map<String, Object> sumFindings();

    /**
     * 统计指定 Agent 的发现数
     *
     * @param agentId Agent ID
     * @return 包含 warnings 和 critical 的 Map
     */
    @Select("SELECT COALESCE(warnings, 0) as warnings, COALESCE(critical, 0) as critical FROM agent WHERE id = #{agentId} AND deleted = 0")
    Map<String, Object> sumFindingsById(@Param("agentId") Long agentId);
}
