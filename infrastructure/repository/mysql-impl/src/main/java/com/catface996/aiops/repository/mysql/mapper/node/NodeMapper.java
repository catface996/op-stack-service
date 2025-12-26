package com.catface996.aiops.repository.mysql.mapper.node;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.repository.mysql.po.node.NodePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 资源节点 Mapper 接口
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Mapper
public interface NodeMapper extends BaseMapper<NodePO> {

    /**
     * 分页查询节点（带类型信息）
     *
     * @param page       分页参数
     * @param keyword    关键词模糊查询（可选，搜索名称和描述）
     * @param nodeTypeId 节点类型ID筛选（可选）
     * @param status     状态筛选（可选）
     * @param topologyId 拓扑图ID筛选（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT DISTINCT n.*, nt.code AS node_type_code, nt.name AS node_type_name " +
            "FROM node n " +
            "JOIN node_type nt ON n.node_type_id = nt.id " +
            "<if test='topologyId != null'>" +
            "JOIN topology_2_node t2n ON n.id = t2n.node_id " +
            "</if>" +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (n.name LIKE CONCAT('%', #{keyword}, '%') OR n.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='nodeTypeId != null'>" +
            "AND n.node_type_id = #{nodeTypeId} " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND n.status = #{status} " +
            "</if>" +
            "<if test='topologyId != null'>" +
            "AND t2n.topology_id = #{topologyId} " +
            "</if>" +
            "</where>" +
            "ORDER BY n.created_at DESC" +
            "</script>")
    IPage<NodePO> selectPageWithTypeInfo(Page<NodePO> page,
                                          @Param("keyword") String keyword,
                                          @Param("nodeTypeId") Long nodeTypeId,
                                          @Param("status") String status,
                                          @Param("topologyId") Long topologyId);

    /**
     * 按条件统计节点数量
     *
     * @param keyword    关键词模糊查询（可选）
     * @param nodeTypeId 节点类型ID筛选（可选）
     * @param status     状态筛选（可选）
     * @param topologyId 拓扑图ID筛选（可选）
     * @return 节点数量
     */
    @Select("<script>" +
            "SELECT COUNT(DISTINCT n.id) " +
            "FROM node n " +
            "<if test='topologyId != null'>" +
            "JOIN topology_2_node t2n ON n.id = t2n.node_id " +
            "</if>" +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (n.name LIKE CONCAT('%', #{keyword}, '%') OR n.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='nodeTypeId != null'>" +
            "AND n.node_type_id = #{nodeTypeId} " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND n.status = #{status} " +
            "</if>" +
            "<if test='topologyId != null'>" +
            "AND t2n.topology_id = #{topologyId} " +
            "</if>" +
            "</where>" +
            "</script>")
    long countByCondition(@Param("keyword") String keyword,
                          @Param("nodeTypeId") Long nodeTypeId,
                          @Param("status") String status,
                          @Param("topologyId") Long topologyId);

    /**
     * 根据ID查询节点（带类型信息）
     *
     * @param id 节点ID
     * @return 节点信息
     */
    @Select("SELECT n.*, nt.code AS node_type_code, nt.name AS node_type_name " +
            "FROM node n " +
            "JOIN node_type nt ON n.node_type_id = nt.id " +
            "WHERE n.id = #{id}")
    NodePO selectByIdWithTypeInfo(@Param("id") Long id);

    /**
     * 根据类型ID和名称查询节点
     *
     * @param nodeTypeId 节点类型ID
     * @param name       节点名称
     * @return 节点信息
     */
    @Select("SELECT * FROM node WHERE node_type_id = #{nodeTypeId} AND name = #{name}")
    NodePO selectByTypeIdAndName(@Param("nodeTypeId") Long nodeTypeId, @Param("name") String name);

    /**
     * 根据名称查询节点
     *
     * @param name 节点名称
     * @return 节点信息
     */
    @Select("SELECT * FROM node WHERE name = #{name}")
    NodePO selectByName(@Param("name") String name);
}
