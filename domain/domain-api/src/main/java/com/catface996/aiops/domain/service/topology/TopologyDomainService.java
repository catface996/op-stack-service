package com.catface996.aiops.domain.service.topology;

import com.catface996.aiops.domain.model.resource.Resource;
import com.catface996.aiops.domain.model.resource.ResourceStatus;

import java.util.List;
import java.util.Optional;

/**
 * 拓扑图领域服务接口
 *
 * <p>提供拓扑图管理的核心业务逻辑，包括：</p>
 * <ul>
 *   <li>拓扑图创建：自动设置 SUBGRAPH 类型</li>
 *   <li>拓扑图查询：只查询 SUBGRAPH 类型资源</li>
 *   <li>拓扑图更新：权限检查、乐观锁、审计</li>
 *   <li>拓扑图删除：解除成员关联、审计</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: 系统必须支持将资源分为拓扑图和资源节点两大类</li>
 *   <li>FR-002: 系统必须提供独立的拓扑图列表查询接口</li>
 *   <li>FR-004: 系统必须提供独立的拓扑图创建接口</li>
 *   <li>FR-008: 拓扑图删除时，必须解除与成员资源的关联关系</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-25
 */
public interface TopologyDomainService {

    /**
     * 创建拓扑图
     *
     * <p>执行以下操作：</p>
     * <ol>
     *   <li>验证拓扑图名称非空且长度合规</li>
     *   <li>自动设置资源类型为 SUBGRAPH</li>
     *   <li>保存拓扑图到数据库</li>
     *   <li>记录创建审计日志</li>
     * </ol>
     *
     * @param name 拓扑图名称（必填，最长100字符）
     * @param description 拓扑图描述（可选，最长500字符）
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 创建的拓扑图资源实体
     * @throws IllegalArgumentException 如果名称为空或过长
     */
    Resource createTopology(String name, String description, Long operatorId, String operatorName);

    /**
     * 分页查询拓扑图列表
     *
     * <p>只查询 resource_type.code = 'SUBGRAPH' 的资源。</p>
     *
     * @param name 名称模糊查询（可选）
     * @param status 状态筛选（可选）
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 拓扑图列表
     */
    List<Resource> listTopologies(String name, ResourceStatus status, int page, int size);

    /**
     * 统计拓扑图数量
     *
     * @param name 名称模糊查询（可选）
     * @param status 状态筛选（可选）
     * @return 拓扑图数量
     */
    long countTopologies(String name, ResourceStatus status);

    /**
     * 根据ID获取拓扑图详情
     *
     * @param topologyId 拓扑图ID
     * @return 拓扑图实体（如果存在且为 SUBGRAPH 类型）
     */
    Optional<Resource> getTopologyById(Long topologyId);

    /**
     * 更新拓扑图
     *
     * @param topologyId 拓扑图ID
     * @param name 新名称（可选，null表示不修改）
     * @param description 新描述（可选）
     * @param version 当前版本号（乐观锁）
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 更新后的拓扑图实体
     * @throws RuntimeException 如果拓扑图不存在或版本冲突
     */
    Resource updateTopology(Long topologyId, String name, String description,
                            Integer version, Long operatorId, String operatorName);

    /**
     * 删除拓扑图
     *
     * <p>执行以下操作：</p>
     * <ol>
     *   <li>验证拓扑图存在且为 SUBGRAPH 类型</li>
     *   <li>解除与所有成员资源的关联关系</li>
     *   <li>删除拓扑图</li>
     *   <li>记录删除审计日志</li>
     * </ol>
     *
     * @param topologyId 拓扑图ID
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @throws RuntimeException 如果拓扑图不存在
     */
    void deleteTopology(Long topologyId, Long operatorId, String operatorName);

    /**
     * 获取拓扑图的成员数量
     *
     * @param topologyId 拓扑图ID
     * @return 成员数量
     */
    int countMembers(Long topologyId);

    /**
     * 获取 SUBGRAPH 类型的资源类型ID
     *
     * @return SUBGRAPH 类型ID
     */
    Long getSubgraphTypeId();
}
