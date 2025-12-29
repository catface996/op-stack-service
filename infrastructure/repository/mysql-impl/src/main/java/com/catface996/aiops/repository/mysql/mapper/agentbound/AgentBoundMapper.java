package com.catface996.aiops.repository.mysql.mapper.agentbound;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.agentbound.AgentBoundPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Agent 绑定关系 Mapper 接口
 *
 * <p>SQL 定义在 mapper/agentbound/AgentBoundMapper.xml</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Mapper
public interface AgentBoundMapper extends BaseMapper<AgentBoundPO> {

    /**
     * 按实体查询绑定（带 Agent 名称）
     *
     * @param entityType     实体类型
     * @param entityId       实体 ID
     * @param hierarchyLevel 层级过滤（可选）
     * @return 绑定列表
     */
    List<AgentBoundPO> selectByEntity(@Param("entityType") String entityType,
                                       @Param("entityId") Long entityId,
                                       @Param("hierarchyLevel") String hierarchyLevel);

    /**
     * 按 Agent 查询绑定（带实体名称）
     *
     * @param agentId    Agent ID
     * @param entityType 实体类型过滤（可选）
     * @return 绑定列表
     */
    List<AgentBoundPO> selectByAgentId(@Param("agentId") Long agentId,
                                        @Param("entityType") String entityType);

    /**
     * 查询 Topology 的层级团队结构
     *
     * <p>返回 Topology 的 Global Supervisor 及其所有 Node 的 Team Supervisor 和 Workers</p>
     *
     * @param topologyId Topology ID
     * @return 绑定列表（包含所有层级）
     */
    List<AgentBoundPO> selectHierarchyByTopologyId(@Param("topologyId") Long topologyId);

    /**
     * 检查是否存在指定类型的绑定
     *
     * @param entityType     实体类型
     * @param entityId       实体 ID
     * @param hierarchyLevel 层级
     * @return 存在返回 1，否则返回 0
     */
    int existsByEntityAndHierarchy(@Param("entityType") String entityType,
                                    @Param("entityId") Long entityId,
                                    @Param("hierarchyLevel") String hierarchyLevel);

    /**
     * 查询指定实体的 Supervisor 绑定（用于替换逻辑）
     *
     * @param entityType     实体类型
     * @param entityId       实体 ID
     * @param hierarchyLevel 层级（GLOBAL_SUPERVISOR 或 TEAM_SUPERVISOR）
     * @return 绑定记录，不存在返回 null
     */
    AgentBoundPO selectSupervisorBinding(@Param("entityType") String entityType,
                                          @Param("entityId") Long entityId,
                                          @Param("hierarchyLevel") String hierarchyLevel);

    /**
     * 物理删除绑定
     *
     * @param agentId    Agent ID
     * @param entityId   实体 ID
     * @param entityType 实体类型
     * @return 删除的记录数
     */
    int hardDeleteBinding(@Param("agentId") Long agentId,
                           @Param("entityId") Long entityId,
                           @Param("entityType") String entityType);

    /**
     * 检查绑定是否已存在
     *
     * @param agentId    Agent ID
     * @param entityId   实体 ID
     * @param entityType 实体类型
     * @return 存在返回 1，否则返回 0
     */
    int existsBinding(@Param("agentId") Long agentId,
                       @Param("entityId") Long entityId,
                       @Param("entityType") String entityType);
}
