package com.catface996.aiops.repository.mysql.mapper.topology;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.repository.mysql.po.topology.TopologyPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 拓扑图 Mapper 接口
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Mapper
public interface TopologyMapper extends BaseMapper<TopologyPO> {

    /**
     * 分页查询拓扑图（带成员数量统计）
     *
     * @param page   分页参数
     * @param name   名称模糊查询（可选）
     * @param status 状态筛选（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT t.*, " +
            "(SELECT COUNT(*) FROM topology_2_node t2n WHERE t2n.topology_id = t.id) AS member_count " +
            "FROM topology t " +
            "<where>" +
            "<if test='name != null and name != \"\"'>" +
            "AND t.name LIKE CONCAT('%', #{name}, '%') " +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND t.status = #{status} " +
            "</if>" +
            "</where>" +
            "ORDER BY t.created_at DESC" +
            "</script>")
    IPage<TopologyPO> selectPageWithMemberCount(Page<TopologyPO> page,
                                                 @Param("name") String name,
                                                 @Param("status") String status);

    /**
     * 根据ID查询拓扑图（带成员数量统计）
     *
     * @param id 拓扑图ID
     * @return 拓扑图信息
     */
    @Select("SELECT t.*, " +
            "(SELECT COUNT(*) FROM topology_2_node t2n WHERE t2n.topology_id = t.id) AS member_count " +
            "FROM topology t " +
            "WHERE t.id = #{id}")
    TopologyPO selectByIdWithMemberCount(@Param("id") Long id);

    /**
     * 根据名称查询拓扑图
     *
     * @param name 拓扑图名称
     * @return 拓扑图信息
     */
    @Select("SELECT * FROM topology WHERE name = #{name}")
    TopologyPO selectByName(@Param("name") String name);
}
