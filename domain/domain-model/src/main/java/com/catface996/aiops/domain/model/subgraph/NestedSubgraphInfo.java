package com.catface996.aiops.domain.model.subgraph;

/**
 * 嵌套子图信息值对象
 *
 * <p>v2.0 设计：表示嵌套子图的元数据，用于展开/折叠状态管理</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求7: 子图详情视图（嵌套子图显示）</li>
 *   <li>需求9: 拓扑数据查询（嵌套展开）</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
public class NestedSubgraphInfo {

    /**
     * 嵌套子图ID
     */
    private Long subgraphId;

    /**
     * 嵌套子图名称
     */
    private String subgraphName;

    /**
     * 父子图ID（该嵌套子图被包含在哪个子图中）
     */
    private Long parentSubgraphId;

    /**
     * 嵌套深度（0 = 直接成员）
     */
    private int depth;

    /**
     * 该嵌套子图包含的成员数量
     */
    private int memberCount;

    /**
     * 是否在响应中已展开
     */
    private boolean expanded;

    // ==================== Constructors ====================

    public NestedSubgraphInfo() {
    }

    public NestedSubgraphInfo(Long subgraphId, String subgraphName, Long parentSubgraphId, int depth, int memberCount) {
        this.subgraphId = subgraphId;
        this.subgraphName = subgraphName;
        this.parentSubgraphId = parentSubgraphId;
        this.depth = depth;
        this.memberCount = memberCount;
        this.expanded = false;
    }

    // ==================== Getters and Setters ====================

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

    public Long getParentSubgraphId() {
        return parentSubgraphId;
    }

    public void setParentSubgraphId(Long parentSubgraphId) {
        this.parentSubgraphId = parentSubgraphId;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public String toString() {
        return "NestedSubgraphInfo{" +
                "subgraphId=" + subgraphId +
                ", subgraphName='" + subgraphName + '\'' +
                ", parentSubgraphId=" + parentSubgraphId +
                ", depth=" + depth +
                ", memberCount=" + memberCount +
                ", expanded=" + expanded +
                '}';
    }
}
