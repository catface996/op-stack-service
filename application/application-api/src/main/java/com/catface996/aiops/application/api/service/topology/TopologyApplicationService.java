package com.catface996.aiops.application.api.service.topology;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.topology.TopologyDTO;
import com.catface996.aiops.application.api.dto.topology.request.CreateTopologyRequest;
import com.catface996.aiops.application.api.dto.topology.request.QueryTopologiesRequest;
import com.catface996.aiops.application.api.dto.topology.request.UpdateTopologyRequest;

/**
 * 拓扑图应用服务接口
 *
 * <p>提供拓扑图管理的应用层接口，协调领域层完成业务逻辑。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>DTO 与领域模型的转换</li>
 *   <li>调用领域服务完成业务逻辑</li>
 *   <li>事务边界管理</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001~008: 资源分类体系设计</li>
 *   <li>US1: 查询所有拓扑图</li>
 *   <li>US3: 创建拓扑图</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-25
 */
public interface TopologyApplicationService {

    /**
     * 创建拓扑图
     *
     * @param request 创建请求
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 创建的拓扑图 DTO
     */
    TopologyDTO createTopology(CreateTopologyRequest request, Long operatorId, String operatorName);

    /**
     * 分页查询拓扑图列表
     *
     * @param request 查询请求
     * @return 分页结果
     */
    PageResult<TopologyDTO> listTopologies(QueryTopologiesRequest request);

    /**
     * 获取拓扑图详情
     *
     * @param topologyId 拓扑图ID
     * @return 拓扑图 DTO，如不存在返回 null
     */
    TopologyDTO getTopologyById(Long topologyId);

    /**
     * 更新拓扑图
     *
     * @param topologyId 拓扑图ID
     * @param request 更新请求
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 更新后的拓扑图 DTO
     */
    TopologyDTO updateTopology(Long topologyId, UpdateTopologyRequest request,
                               Long operatorId, String operatorName);

    /**
     * 删除拓扑图
     *
     * @param topologyId 拓扑图ID
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    void deleteTopology(Long topologyId, Long operatorId, String operatorName);
}
