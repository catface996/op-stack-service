package com.catface996.aiops.repository.mysql.mapper.node;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.node.Node2NodePO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 节点依赖关系 Mapper 接口
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Mapper
public interface Node2NodeMapper extends BaseMapper<Node2NodePO> {

    /**
     * 查询节点的出边关系（带节点名称）
     *
     * @param sourceId 源节点ID
     * @return 关系列表
     */
    @Select("SELECT n2n.*, n.name AS target_name " +
            "FROM node_2_node n2n " +
            "JOIN node n ON n2n.target_id = n.id " +
            "WHERE n2n.source_id = #{sourceId}")
    List<Node2NodePO> selectOutgoingBySourceId(@Param("sourceId") Long sourceId);

    /**
     * 查询节点的入边关系（带节点名称）
     *
     * @param targetId 目标节点ID
     * @return 关系列表
     */
    @Select("SELECT n2n.*, n.name AS source_name " +
            "FROM node_2_node n2n " +
            "JOIN node n ON n2n.source_id = n.id " +
            "WHERE n2n.target_id = #{targetId}")
    List<Node2NodePO> selectIncomingByTargetId(@Param("targetId") Long targetId);

    /**
     * 删除节点相关的所有关系
     *
     * @param nodeId 节点ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM node_2_node WHERE source_id = #{nodeId} OR target_id = #{nodeId}")
    int deleteByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 查询指定节点集合之间的关系
     *
     * @param nodeIds 节点ID列表
     * @return 关系列表
     */
    @Select("<script>" +
            "SELECT * FROM node_2_node " +
            "WHERE source_id IN " +
            "<foreach collection='nodeIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach> " +
            "AND target_id IN " +
            "<foreach collection='nodeIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<Node2NodePO> selectByNodeIds(@Param("nodeIds") List<Long> nodeIds);
}
