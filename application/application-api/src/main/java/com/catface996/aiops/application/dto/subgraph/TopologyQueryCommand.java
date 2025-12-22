package com.catface996.aiops.application.dto.subgraph;

/**
 * 拓扑查询命令
 *
 * <p>v2.0 设计：子图作为资源类型，成员可以是任意资源（包括嵌套子图）</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求9: 拓扑数据查询</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
public class TopologyQueryCommand {

    /**
     * 子图资源 ID
     */
    private Long subgraphId;

    /**
     * 是否展开嵌套子图
     */
    private boolean expandNested;

    /**
     * 最大展开深度（默认 3，最大 10）
     */
    private int maxDepth = 3;

    // ==================== Constructors ====================

    public TopologyQueryCommand() {
    }

    public TopologyQueryCommand(Long subgraphId, boolean expandNested) {
        this.subgraphId = subgraphId;
        this.expandNested = expandNested;
    }

    public TopologyQueryCommand(Long subgraphId, boolean expandNested, int maxDepth) {
        this.subgraphId = subgraphId;
        this.expandNested = expandNested;
        this.maxDepth = maxDepth;
    }

    // ==================== Getters and Setters ====================

    public Long getSubgraphId() {
        return subgraphId;
    }

    public void setSubgraphId(Long subgraphId) {
        this.subgraphId = subgraphId;
    }

    public boolean isExpandNested() {
        return expandNested;
    }

    public void setExpandNested(boolean expandNested) {
        this.expandNested = expandNested;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        // 限制最大深度
        this.maxDepth = Math.min(Math.max(1, maxDepth), 10);
    }

    @Override
    public String toString() {
        return "TopologyQueryCommand{" +
                "subgraphId=" + subgraphId +
                ", expandNested=" + expandNested +
                ", maxDepth=" + maxDepth +
                '}';
    }
}
