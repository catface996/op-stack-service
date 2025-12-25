package com.catface996.aiops.domain.model.subgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * 子图边界值对象
 *
 * <p>v2.0 设计：表示嵌套子图的边界，用于拓扑图中绘制子图边界框</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求7: 子图详情视图（嵌套子图显示）</li>
 *   <li>需求9: 拓扑数据查询</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
public class SubgraphBoundary {

    /**
     * 子图ID
     */
    private Long subgraphId;

    /**
     * 子图名称
     */
    private String subgraphName;

    /**
     * 子图包含的成员ID列表
     */
    private List<Long> memberIds;

    // ==================== Constructors ====================

    public SubgraphBoundary() {
        this.memberIds = new ArrayList<>();
    }

    public SubgraphBoundary(Long subgraphId, String subgraphName, List<Long> memberIds) {
        this.subgraphId = subgraphId;
        this.subgraphName = subgraphName;
        this.memberIds = memberIds != null ? new ArrayList<>(memberIds) : new ArrayList<>();
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

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds != null ? new ArrayList<>(memberIds) : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "SubgraphBoundary{" +
                "subgraphId=" + subgraphId +
                ", subgraphName='" + subgraphName + '\'' +
                ", memberCount=" + memberIds.size() +
                '}';
    }
}
