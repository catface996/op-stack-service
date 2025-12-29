package com.catface996.aiops.repository.mysql.mapper.node;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.node.NodeAgentRelationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Node-Agent 关联 Mapper 接口
 *
 * <p>SQL 定义在 mapper/node/NodeAgentRelationMapper.xml</p>
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
    List<Long> selectAgentIdsByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 根据 Agent ID 查询关联的节点 ID 列表（未删除）
     *
     * @param agentId Agent ID
     * @return Node ID 列表
     */
    List<Long> selectNodeIdsByAgentId(@Param("agentId") Long agentId);

    /**
     * 软删除指定记录
     *
     * @param id 关联记录 ID
     * @return 影响行数
     */
    int softDeleteById(@Param("id") Long id);

    /**
     * 根据节点 ID 软删除所有关联
     *
     * @param nodeId 资源节点 ID
     * @return 影响行数
     */
    int softDeleteByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 根据 Agent ID 软删除所有关联
     *
     * @param agentId Agent ID
     * @return 影响行数
     */
    int softDeleteByAgentId(@Param("agentId") Long agentId);

    /**
     * 根据节点ID和Agent ID查询关联记录
     *
     * @param nodeId  资源节点 ID
     * @param agentId Agent ID
     * @return 关联记录
     */
    NodeAgentRelationPO selectByNodeIdAndAgentId(@Param("nodeId") Long nodeId, @Param("agentId") Long agentId);

    /**
     * 统计节点的Agent绑定数量
     *
     * @param nodeId 资源节点 ID
     * @return 绑定数量
     */
    int countByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 物理删除已软删除的记录（用于解决唯一键冲突）
     *
     * @param nodeId  资源节点 ID
     * @param agentId Agent ID
     * @return 影响行数
     */
    int hardDeleteSoftDeleted(@Param("nodeId") Long nodeId, @Param("agentId") Long agentId);
}
