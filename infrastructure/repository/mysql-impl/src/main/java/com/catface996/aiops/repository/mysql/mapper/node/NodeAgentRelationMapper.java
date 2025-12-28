package com.catface996.aiops.repository.mysql.mapper.node;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.node.NodeAgentRelationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Node-Agent 关联 Mapper 接口
 *
 * <p>提供 node_2_agent 表的数据库操作</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Mapper
public interface NodeAgentRelationMapper extends BaseMapper<NodeAgentRelationPO> {

    /**
     * 根据节点 ID 查询关联的 Agent ID 列表（未删除）
     *
     * @param nodeId 资源节点 ID
     * @return Agent ID 列表
     */
    @Select("SELECT agent_id FROM node_2_agent WHERE node_id = #{nodeId} AND deleted = 0")
    List<Long> selectAgentIdsByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 根据 Agent ID 查询关联的节点 ID 列表（未删除）
     *
     * @param agentId Agent ID
     * @return Node ID 列表
     */
    @Select("SELECT node_id FROM node_2_agent WHERE agent_id = #{agentId} AND deleted = 0")
    List<Long> selectNodeIdsByAgentId(@Param("agentId") Long agentId);

    /**
     * 软删除指定记录
     *
     * @param id 关联记录 ID
     * @return 影响行数
     */
    @Update("UPDATE node_2_agent SET deleted = 1 WHERE id = #{id}")
    int softDeleteById(@Param("id") Long id);

    /**
     * 根据节点 ID 软删除所有关联
     *
     * @param nodeId 资源节点 ID
     * @return 影响行数
     */
    @Update("UPDATE node_2_agent SET deleted = 1 WHERE node_id = #{nodeId} AND deleted = 0")
    int softDeleteByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 根据 Agent ID 软删除所有关联
     *
     * @param agentId Agent ID
     * @return 影响行数
     */
    @Update("UPDATE node_2_agent SET deleted = 1 WHERE agent_id = #{agentId} AND deleted = 0")
    int softDeleteByAgentId(@Param("agentId") Long agentId);
}
