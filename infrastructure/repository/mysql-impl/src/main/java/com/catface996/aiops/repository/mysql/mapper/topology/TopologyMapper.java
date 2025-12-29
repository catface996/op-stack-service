package com.catface996.aiops.repository.mysql.mapper.topology;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.repository.mysql.po.topology.TopologyPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 拓扑图 Mapper 接口
 *
 * <p>SQL 定义在 mapper/topology/TopologyMapper.xml</p>
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
    IPage<TopologyPO> selectPageWithMemberCount(Page<TopologyPO> page,
                                                 @Param("name") String name,
                                                 @Param("status") String status);

    /**
     * 根据ID查询拓扑图（带成员数量统计）
     *
     * @param id 拓扑图ID
     * @return 拓扑图信息
     */
    TopologyPO selectByIdWithMemberCount(@Param("id") Long id);

    /**
     * 根据名称查询拓扑图
     *
     * @param name 拓扑图名称
     * @return 拓扑图信息
     */
    TopologyPO selectByName(@Param("name") String name);

    /**
     * 根据条件统计拓扑图数量
     *
     * @param name   名称模糊查询（可选）
     * @param status 状态筛选（可选）
     * @return 数量
     */
    long countByCondition(@Param("name") String name, @Param("status") String status);

    /**
     * 更新全局监督Agent ID
     *
     * @param topologyId 拓扑图ID
     * @param agentId    Agent ID
     * @param updatedAt  更新时间
     * @return 影响行数
     */
    int updateGlobalSupervisorAgentId(@Param("topologyId") Long topologyId,
                                       @Param("agentId") Long agentId,
                                       @Param("updatedAt") java.time.LocalDateTime updatedAt);
}
