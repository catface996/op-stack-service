package com.catface996.aiops.application.api.service.subgraph;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphDTO;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphDetailDTO;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphResourceDTO;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphResourcesWithRelationsDTO;
import com.catface996.aiops.application.api.dto.subgraph.SubgraphTopologyDTO;
import com.catface996.aiops.application.api.dto.subgraph.request.*;

/**
 * 子图应用服务接口
 *
 * <p>提供子图管理的应用层服务，协调领域层和基础设施层。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>接收和验证请求参数</li>
 *   <li>调用领域服务执行业务逻辑</li>
 *   <li>转换领域对象为DTO返回</li>
 *   <li>处理事务边界</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求1-10: 子图CRUD、权限管理、资源节点管理、安全审计</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
public interface SubgraphApplicationService {

    // ==================== 子图生命周期 ====================

    /**
     * 创建子图
     *
     * @param request 创建请求
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 创建的子图DTO
     */
    SubgraphDTO createSubgraph(CreateSubgraphRequest request, Long operatorId, String operatorName);

    /**
     * 更新子图
     *
     * @param subgraphId 子图ID
     * @param request 更新请求
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     * @return 更新后的子图DTO
     */
    SubgraphDTO updateSubgraph(Long subgraphId, UpdateSubgraphRequest request,
                               Long operatorId, String operatorName);

    /**
     * 删除子图
     *
     * @param subgraphId 子图ID
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    void deleteSubgraph(Long subgraphId, Long operatorId, String operatorName);

    // ==================== 子图查询 ====================

    /**
     * 查询子图列表（分页）
     *
     * @param request 查询请求
     * @param userId 当前用户ID
     * @return 分页结果
     */
    PageResult<SubgraphDTO> listSubgraphs(ListSubgraphsRequest request, Long userId);

    /**
     * 获取子图详情
     *
     * @param subgraphId 子图ID
     * @param userId 当前用户ID
     * @return 子图详情DTO
     */
    SubgraphDetailDTO getSubgraphDetail(Long subgraphId, Long userId);

    // ==================== 权限管理 ====================

    /**
     * 添加或更新权限
     *
     * @param subgraphId 子图ID
     * @param request 权限请求
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    void addPermission(Long subgraphId, UpdatePermissionRequest request,
                       Long operatorId, String operatorName);

    /**
     * 移除权限
     *
     * @param subgraphId 子图ID
     * @param userId 被移除权限的用户ID
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    void removePermission(Long subgraphId, Long userId, Long operatorId, String operatorName);

    // ==================== 资源节点管理 ====================

    /**
     * 向子图添加资源
     *
     * @param subgraphId 子图ID
     * @param request 添加资源请求
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    void addResources(Long subgraphId, AddResourcesRequest request,
                      Long operatorId, String operatorName);

    /**
     * 从子图移除资源
     *
     * @param subgraphId 子图ID
     * @param request 移除资源请求
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    void removeResources(Long subgraphId, RemoveResourcesRequest request,
                         Long operatorId, String operatorName);

    /**
     * 查询子图资源列表（分页）
     *
     * @param subgraphId 子图ID
     * @param request 查询请求
     * @param userId 当前用户ID
     * @return 分页结果
     */
    PageResult<SubgraphResourceDTO> getSubgraphResources(Long subgraphId,
                                                          ListSubgraphResourcesRequest request,
                                                          Long userId);

    // ==================== 拓扑查询 ====================

    /**
     * 获取子图拓扑
     *
     * @param subgraphId 子图ID
     * @param userId 当前用户ID
     * @return 子图拓扑DTO
     */
    SubgraphTopologyDTO getSubgraphTopology(Long subgraphId, Long userId);

    /**
     * 获取子图所有资源及关系（不分页）
     *
     * <p>返回子图内所有资源节点的完整信息及节点之间的关系，用于前端拓扑图展示。</p>
     *
     * @param subgraphId 子图ID
     * @param userId 当前用户ID
     * @return 子图资源及关系DTO
     */
    SubgraphResourcesWithRelationsDTO getSubgraphResourcesWithRelations(Long subgraphId, Long userId);
}
