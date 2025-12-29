package com.catface996.aiops.repository.mysql.mapper.topology;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.topology.Topology2NodePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 拓扑图-节点关联 Mapper 接口
 *
 * <p>SQL 定义在 mapper/topology/Topology2NodeMapper.xml</p>
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
    List<Topology2NodePO> selectMembersByTopologyId(@Param("topologyId") Long topologyId);

    /**
     * 根据拓扑图ID和节点ID查询关联记录
     *
     * @param topologyId 拓扑图ID
     * @param nodeId     节点ID
     * @return 关联记录
     */
    Topology2NodePO selectByTopologyIdAndNodeId(@Param("topologyId") Long topologyId,
                                                 @Param("nodeId") Long nodeId);

    /**
     * 删除拓扑图的所有成员关联
     *
     * @param topologyId 拓扑图ID
     * @return 删除的记录数
     */
    int deleteByTopologyId(@Param("topologyId") Long topologyId);

    /**
     * 删除节点相关的所有拓扑图关联
     *
     * @param nodeId 节点ID
     * @return 删除的记录数
     */
    int deleteByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 查询节点所属的拓扑图数量
     *
     * @param nodeId 节点ID
     * @return 拓扑图数量
     */
    int countByNodeId(@Param("nodeId") Long nodeId);

    /**
     * 统计拓扑图的成员数量
     *
     * @param topologyId 拓扑图ID
     * @return 成员数量
     */
    int countByTopologyId(@Param("topologyId") Long topologyId);

    /**
     * 查询拓扑图的所有节点ID
     *
     * @param topologyId 拓扑图ID
     * @return 节点ID列表
     */
    List<Long> selectNodeIdsByTopologyId(@Param("topologyId") Long topologyId);

    /**
     * 删除指定拓扑图和节点的关联
     *
     * @param topologyId 拓扑图ID
     * @param nodeId     节点ID
     * @return 删除的记录数
     */
    int deleteByTopologyIdAndNodeId(@Param("topologyId") Long topologyId, @Param("nodeId") Long nodeId);
}
