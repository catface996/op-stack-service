package com.catface996.aiops.repository.mysql.mapper.topology;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.topology.Topology2NodePO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 拓扑图-节点关联 Mapper 接口
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Mapper
public interface Topology2NodeMapper extends BaseMapper<Topology2NodePO> {

    /**
     * 查询拓扑图的所有成员（带节点详情）
     *
     * @param topologyId 拓扑图ID
     * @return 成员列表
     */
    @Select("SELECT t2n.*, " +
            "n.name AS node_name, n.status AS node_status, " +
            "nt.code AS node_type_code, nt.name AS node_type_name " +
            "FROM topology_2_node t2n " +
            "JOIN node n ON t2n.node_id = n.id " +
            "JOIN node_type nt ON n.node_type_id = nt.id " +
            "WHERE t2n.topology_id = #{topologyId} " +
            "ORDER BY t2n.added_at DESC")
    List<Topology2NodePO> selectMembersByTopologyId(@Param("topologyId") Long topologyId);

    /**
     * 根据拓扑图ID和节点ID查询关联记录
     *
     * @param topologyId 拓扑图ID
     * @param nodeId     节点ID
     * @return 关联记录
     */
    @Select("SELECT * FROM topology_2_node WHERE topology_id = #{topologyId} AND node_id = #{nodeId}")
    Topology2NodePO selectByTopologyIdAndNodeId(@Param("topologyId") Long topologyId,
                                                 @Param("nodeId") Long nodeId);

    /**
     * 删除拓扑图的所有成员关联
     *
     * @param topologyId 拓扑图ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM topology_2_node WHERE topology_id = #{topologyId}")
    int deleteByTopologyId(@Param("topologyId") Long topologyId);

    /**
     * 删除节点相关的所有拓扑图关联
     *
     * @param nodeId 节点ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM topology_2_node WHERE node_id = #{nodeId}")
    int deleteByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 查询节点所属的拓扑图数量
     *
     * @param nodeId 节点ID
     * @return 拓扑图数量
     */
    @Select("SELECT COUNT(*) FROM topology_2_node WHERE node_id = #{nodeId}")
    int countByNodeId(@Param("nodeId") Long nodeId);
}
