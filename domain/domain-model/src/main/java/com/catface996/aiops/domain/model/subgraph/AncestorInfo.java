package com.catface996.aiops.domain.model.subgraph;

/**
 * 祖先子图信息值对象
 *
 * <p>v2.0 设计：表示子图的祖先链信息，用于导航和循环检测</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求7: 子图详情视图（祖先导航）</li>
 *   <li>需求5: 添加成员（循环检测）</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
public class AncestorInfo {

    /**
     * 祖先子图ID
     */
    private Long subgraphId;

    /**
     * 祖先子图名称
     */
    private String subgraphName;

    /**
     * 距离查询子图的层数（1 = 直接父级）
     */
    private int depth;

    // ==================== Constructors ====================

    public AncestorInfo() {
    }

    public AncestorInfo(Long subgraphId, String subgraphName, int depth) {
        this.subgraphId = subgraphId;
        this.subgraphName = subgraphName;
        this.depth = depth;
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

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public String toString() {
        return "AncestorInfo{" +
                "subgraphId=" + subgraphId +
                ", subgraphName='" + subgraphName + '\'' +
                ", depth=" + depth +
                '}';
    }
}
