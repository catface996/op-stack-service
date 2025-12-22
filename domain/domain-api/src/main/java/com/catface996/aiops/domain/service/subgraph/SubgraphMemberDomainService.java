package com.catface996.aiops.domain.service.subgraph;

import com.catface996.aiops.domain.model.subgraph.*;

import java.util.List;
import java.util.Set;

/**
 * 子图成员领域服务接口
 *
 * <p>v2.0 设计：子图作为资源类型，成员可以是任意资源（包括嵌套子图）</p>
 *
 * <p>提供子图成员管理的核心业务逻辑，包括：</p>
 * <ul>
 *   <li>成员添加：权限检查、循环检测、批量添加</li>
 *   <li>成员移除：权限检查、批量移除</li>
 *   <li>成员查询：分页列表、成员详情</li>
 *   <li>拓扑查询：嵌套展开、关系获取</li>
 *   <li>祖先查询：祖先链、导航</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求5: 向子图添加成员资源</li>
 *   <li>需求6: 从子图移除成员资源</li>
 *   <li>需求7: 子图详情视图</li>
 *   <li>需求8: 成员列表查询</li>
 *   <li>需求9: 拓扑数据查询</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
public interface SubgraphMemberDomainService {

    // ==================== 约束常量 ====================

    /**
     * 最大嵌套深度
     */
    int MAX_NESTING_DEPTH = 10;

    /**
     * 每子图最大成员数
     */
    int MAX_MEMBERS_PER_SUBGRAPH = 500;

    // ==================== 成员添加（US5）====================

    /**
     * 向子图添加成员
     *
     * <p>执行以下操作：</p>
     * <ol>
     *   <li>验证子图存在且为 SUBGRAPH 类型</li>
     *   <li>检查成员数量限制（最大 500）</li>
     *   <li>过滤已存在的成员</li>
     *   <li>对子图类型成员执行循环检测</li>
     *   <li>批量添加成员</li>
     * </ol>
     *
     * @param subgraphId 子图资源 ID
     * @param memberIds  要添加的成员资源 ID 列表
     * @param operatorId 操作者用户 ID
     * @return 实际添加的成员数量
     * @throws IllegalArgumentException 如果子图不存在或不是 SUBGRAPH 类型
     * @throws IllegalStateException    如果添加会导致循环引用
     * @throws IllegalStateException    如果超过成员数量限制
     */
    int addMembers(Long subgraphId, List<Long> memberIds, Long operatorId);

    // ==================== 循环检测（US5）====================

    /**
     * 检测添加成员是否会创建循环引用
     *
     * <p>使用 DFS 算法检测祖先链，如果候选成员是当前子图的祖先，则会形成循环</p>
     *
     * @param subgraphId        当前子图 ID
     * @param candidateMemberId 候选成员 ID（如果是子图类型）
     * @return true 如果会创建循环引用
     */
    boolean wouldCreateCycle(Long subgraphId, Long candidateMemberId);

    /**
     * 获取子图的所有祖先 ID（用于循环检测）
     *
     * @param subgraphId 子图 ID
     * @return 祖先子图 ID 集合
     */
    Set<Long> getAncestorSubgraphIds(Long subgraphId);

    // ==================== 成员移除（US6）====================

    /**
     * 从子图移除成员
     *
     * @param subgraphId 子图资源 ID
     * @param memberIds  要移除的成员资源 ID 列表
     * @param operatorId 操作者用户 ID
     * @return 实际移除的成员数量
     */
    int removeMembers(Long subgraphId, List<Long> memberIds, Long operatorId);

    // ==================== 成员查询（US8）====================

    /**
     * 分页查询子图成员（含成员详情）
     *
     * @param subgraphId 子图 ID
     * @param page       页码（从 1 开始）
     * @param size       每页大小
     * @return 成员列表（含派生属性）
     */
    List<SubgraphMember> getMembersBySubgraphIdPaged(Long subgraphId, int page, int size);

    /**
     * 统计子图成员数量
     *
     * @param subgraphId 子图 ID
     * @return 成员数量
     */
    int countMembers(Long subgraphId);

    /**
     * 获取子图的所有成员 ID
     *
     * @param subgraphId 子图 ID
     * @return 成员 ID 列表
     */
    List<Long> getMemberIds(Long subgraphId);

    // ==================== 拓扑查询（US9）====================

    /**
     * 获取子图成员及其之间的关系
     *
     * @param subgraphId   子图 ID
     * @param expandNested 是否展开嵌套子图
     * @param maxDepth     最大展开深度
     * @return 成员和关系数据
     */
    SubgraphMembersWithRelations getMembersWithRelations(Long subgraphId, boolean expandNested, int maxDepth);

    /**
     * 获取子图拓扑数据（用于图形渲染）
     *
     * @param subgraphId   子图 ID
     * @param expandNested 是否默认展开嵌套子图
     * @return 拓扑结果（含节点、边、边界）
     */
    SubgraphTopologyResult getSubgraphTopology(Long subgraphId, boolean expandNested);

    /**
     * 递归展开嵌套子图的成员
     *
     * @param subgraphId       根子图 ID
     * @param maxDepth         最大展开深度
     * @param parentSubgraphId 父子图 ID（用于追踪层级）
     * @param currentDepth     当前深度
     * @return 展开后的所有成员列表
     */
    List<SubgraphMember> expandNestedSubgraphs(Long subgraphId, int maxDepth, Long parentSubgraphId, int currentDepth);

    // ==================== 祖先查询（US7）====================

    /**
     * 获取子图的祖先链（用于导航）
     *
     * @param subgraphId 子图 ID
     * @return 祖先信息列表（按深度排序，depth=1 为直接父级）
     */
    List<AncestorInfo> getAncestors(Long subgraphId);

    // ==================== 子图删除校验（Phase 8）====================

    /**
     * 检查子图是否为空（不包含任何成员）
     *
     * @param subgraphId 子图 ID
     * @return true 如果子图为空
     */
    boolean isSubgraphEmpty(Long subgraphId);

    // ==================== 类型检查 ====================

    /**
     * 检查资源是否为子图类型
     *
     * @param resourceId 资源 ID
     * @return true 如果是 SUBGRAPH 类型
     */
    boolean isSubgraphType(Long resourceId);

    /**
     * 内部类：成员和关系数据
     */
    class SubgraphMembersWithRelations {
        private Long subgraphId;
        private String subgraphName;
        private List<SubgraphMember> members;
        private List<TopologyEdge> relationships;
        private List<NestedSubgraphInfo> nestedSubgraphs;
        private int nodeCount;
        private int edgeCount;
        private int maxDepth;

        public SubgraphMembersWithRelations() {
        }

        public Long getSubgraphId() {
            return subgraphId;
        }

        public void setSubgraphId(Long subgraphId) {
            this.subgraphId = subgraphId;
        }

        public String getSubgraphName() {
            return subgraphName;
        }

        public void setSubgraphName(String subgraphName) {
            this.subgraphName = subgraphName;
        }

        public List<SubgraphMember> getMembers() {
            return members;
        }

        public void setMembers(List<SubgraphMember> members) {
            this.members = members;
        }

        public List<TopologyEdge> getRelationships() {
            return relationships;
        }

        public void setRelationships(List<TopologyEdge> relationships) {
            this.relationships = relationships;
        }

        public List<NestedSubgraphInfo> getNestedSubgraphs() {
            return nestedSubgraphs;
        }

        public void setNestedSubgraphs(List<NestedSubgraphInfo> nestedSubgraphs) {
            this.nestedSubgraphs = nestedSubgraphs;
        }

        public int getNodeCount() {
            return nodeCount;
        }

        public void setNodeCount(int nodeCount) {
            this.nodeCount = nodeCount;
        }

        public int getEdgeCount() {
            return edgeCount;
        }

        public void setEdgeCount(int edgeCount) {
            this.edgeCount = edgeCount;
        }

        public int getMaxDepth() {
            return maxDepth;
        }

        public void setMaxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
        }
    }
}
