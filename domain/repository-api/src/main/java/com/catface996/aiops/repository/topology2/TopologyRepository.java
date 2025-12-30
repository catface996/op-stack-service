package com.catface996.aiops.repository.topology2;

import com.catface996.aiops.domain.model.topology.Topology;
import com.catface996.aiops.domain.model.topology.TopologyStatus;

import java.util.List;
import java.util.Optional;

/**
 * 拓扑图仓储接口
 *
 * <p>提供拓扑图实体的数据访问操作。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: resource 表拆分为 topology 表和 node 表</li>
 *   <li>FR-006: 拓扑图 API 保持接口契约不变</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public interface TopologyRepository {

    /**
     * 根据ID查询拓扑图
     *
     * @param id 拓扑图ID
     * @return 拓扑图实体（如果存在）
     */
    Optional<Topology> findById(Long id);

    /**
     * 根据ID查询拓扑图，包含成员数量统计
     *
     * @param id 拓扑图ID
     * @return 拓扑图实体（如果存在）
     */
    Optional<Topology> findByIdWithMemberCount(Long id);

    /**
     * 根据名称查询拓扑图
     *
     * @param name 拓扑图名称
     * @return 拓扑图实体（如果存在）
     */
    Optional<Topology> findByName(String name);

    /**
     * 分页查询拓扑图列表
     *
     * @param name   名称模糊查询（可选）
     * @param status 状态筛选（可选）
     * @param page   页码（从1开始）
     * @param size   每页大小
     * @return 拓扑图列表
     */
    List<Topology> findByCondition(String name, TopologyStatus status, int page, int size);

    /**
     * 按条件统计拓扑图数量
     *
     * @param name   名称模糊查询（可选）
     * @param status 状态筛选（可选）
     * @return 拓扑图数量
     */
    long countByCondition(String name, TopologyStatus status);

    /**
     * 保存拓扑图
     *
     * @param topology 拓扑图实体
     * @return 保存后的拓扑图实体
     */
    Topology save(Topology topology);

    /**
     * 更新拓扑图（使用乐观锁）
     *
     * @param topology 拓扑图实体
     * @return 更新是否成功
     */
    boolean update(Topology topology);

    /**
     * 删除拓扑图
     *
     * @param id 拓扑图ID
     */
    void deleteById(Long id);

    /**
     * 检查拓扑图是否存在
     *
     * @param id 拓扑图ID
     * @return true if topology exists
     */
    boolean existsById(Long id);

    /**
     * 检查拓扑图名称是否已存在
     *
     * @param name 拓扑图名称
     * @return true if name exists
     */
    boolean existsByName(String name);
}
