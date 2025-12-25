package com.catface996.aiops.application.service.subgraph;

import com.catface996.aiops.application.dto.subgraph.AddMembersCommand;
import com.catface996.aiops.application.dto.subgraph.SubgraphAncestorsDTO;
import com.catface996.aiops.application.dto.subgraph.SubgraphMemberDTO;
import com.catface996.aiops.application.dto.subgraph.SubgraphMembersWithRelationsDTO;
import com.catface996.aiops.application.dto.subgraph.TopologyGraphDTO;
import com.catface996.aiops.application.dto.subgraph.TopologyQueryCommand;

import java.util.List;

/**
 * 子图成员应用服务接口
 *
 * <p>v2.0 设计：子图作为资源类型，成员可以是任意资源（包括嵌套子图）</p>
 *
 * <p>提供子图成员管理的应用层操作：</p>
 * <ul>
 *   <li>成员添加：权限检查、循环检测、批量添加</li>
 *   <li>成员移除：权限检查、批量移除</li>
 *   <li>成员查询：分页列表、成员详情</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求5: 向子图添加成员资源</li>
 *   <li>需求6: 从子图移除成员资源</li>
 *   <li>需求8: 成员列表查询</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
public interface SubgraphMemberApplicationService {

    // ==================== 成员添加（US5）====================

    /**
     * 向子图添加成员
     *
     * <p>执行以下操作：</p>
     * <ol>
     *   <li>验证用户对子图的 Owner 权限</li>
     *   <li>调用领域服务添加成员（含循环检测）</li>
     * </ol>
     *
     * @param command 添加成员命令
     * @return 实际添加的成员数量
     * @throws IllegalArgumentException 如果子图不存在或不是 SUBGRAPH 类型
     * @throws IllegalStateException    如果添加会导致循环引用
     * @throws SecurityException        如果用户无权限
     */
    int addMembers(AddMembersCommand command);

    // ==================== 成员移除（US6）====================

    /**
     * 从子图移除成员
     *
     * @param subgraphId  子图资源 ID
     * @param memberIds   要移除的成员资源 ID 列表
     * @param operatorId  操作者用户 ID
     * @return 实际移除的成员数量
     */
    int removeMembers(Long subgraphId, List<Long> memberIds, Long operatorId);

    // ==================== 成员查询（US8）====================

    /**
     * 分页查询子图成员
     *
     * @param subgraphId 子图 ID
     * @param page       页码（从 1 开始）
     * @param size       每页大小
     * @return 成员 DTO 列表
     */
    List<SubgraphMemberDTO> listMembers(Long subgraphId, int page, int size);

    /**
     * 统计子图成员数量
     *
     * @param subgraphId 子图 ID
     * @return 成员数量
     */
    int countMembers(Long subgraphId);

    // ==================== 拓扑查询（US9）====================

    /**
     * 获取子图成员及其关系
     *
     * <p>返回子图的成员列表和成员之间的关系，支持嵌套子图展开。</p>
     *
     * @param command 拓扑查询命令
     * @return 成员和关系 DTO
     */
    SubgraphMembersWithRelationsDTO getMembersWithRelations(TopologyQueryCommand command);

    /**
     * 获取子图拓扑图数据
     *
     * <p>返回用于图形渲染的拓扑数据，包含节点、边和子图边界。</p>
     *
     * @param command 拓扑查询命令
     * @return 拓扑图 DTO
     */
    TopologyGraphDTO getSubgraphTopology(TopologyQueryCommand command);

    // ==================== 祖先查询（US7）====================

    /**
     * 获取子图的祖先链
     *
     * <p>返回子图的祖先列表，用于导航和面包屑显示。</p>
     *
     * @param subgraphId 子图 ID
     * @return 祖先 DTO
     */
    SubgraphAncestorsDTO getAncestors(Long subgraphId);

    // ==================== 子图空检查（Phase 8）====================

    /**
     * 检查子图是否为空
     *
     * @param subgraphId 子图 ID
     * @return true 如果子图为空
     */
    boolean isSubgraphEmpty(Long subgraphId);
}
