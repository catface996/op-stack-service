package com.catface996.aiops.repository.mysql.mapper.node;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.repository.mysql.po.node.NodePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资源节点 Mapper 接口
 *
 * <p>SQL 定义在 mapper/node/NodeMapper.xml</p>
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
     * @param layer      架构层级筛选（可选）
     * @param topologyId 拓扑图ID筛选（可选）
     * @return 分页结果
     */
    IPage<NodePO> selectPageWithTypeInfo(Page<NodePO> page,
                                          @Param("keyword") String keyword,
                                          @Param("nodeTypeId") Long nodeTypeId,
                                          @Param("status") String status,
                                          @Param("layer") String layer,
                                          @Param("topologyId") Long topologyId);

    /**
     * 按条件统计节点数量
     *
     * @param keyword    关键词模糊查询（可选）
     * @param nodeTypeId 节点类型ID筛选（可选）
     * @param status     状态筛选（可选）
     * @param layer      架构层级筛选（可选）
     * @param topologyId 拓扑图ID筛选（可选）
     * @return 节点数量
     */
    long countByCondition(@Param("keyword") String keyword,
                          @Param("nodeTypeId") Long nodeTypeId,
                          @Param("status") String status,
                          @Param("layer") String layer,
                          @Param("topologyId") Long topologyId);

    /**
     * 根据ID查询节点（带类型信息）
     *
     * @param id 节点ID
     * @return 节点信息
     */
    NodePO selectByIdWithTypeInfo(@Param("id") Long id);

    /**
     * 根据类型ID和名称查询节点
     *
     * @param nodeTypeId 节点类型ID
     * @param name       节点名称
     * @return 节点信息
     */
    NodePO selectByTypeIdAndName(@Param("nodeTypeId") Long nodeTypeId, @Param("name") String name);

    /**
     * 根据名称查询节点
     *
     * @param name 节点名称
     * @return 节点信息
     */
    NodePO selectByName(@Param("name") String name);

    /**
     * 查询存在的节点ID列表
     *
     * @param nodeIds 待查询的节点ID列表
     * @return 存在的节点ID列表
     */
    List<Long> findExistingIds(@Param("nodeIds") List<Long> nodeIds);
}
