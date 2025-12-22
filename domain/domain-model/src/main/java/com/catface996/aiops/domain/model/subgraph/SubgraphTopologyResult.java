package com.catface996.aiops.domain.model.subgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * 子图拓扑结果聚合对象
 *
 * <p>v2.0 设计：包含完整的拓扑数据，支持嵌套子图边界</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求7: 子图详情视图</li>
 *   <li>需求9: 拓扑数据查询</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
public class SubgraphTopologyResult {

    /**
     * 拓扑节点列表
     */
    private List<TopologyNode> nodes;

    /**
     * 拓扑边（关系）列表
     */
    private List<TopologyEdge> edges;

    /**
     * 嵌套子图边界列表
     */
    private List<SubgraphBoundary> subgraphBoundaries;

    /**
     * 节点总数
     */
    private int nodeCount;

    /**
     * 边（关系）总数
     */
    private int edgeCount;

    /**
     * 最大嵌套深度
     */
    private int maxDepth;

    // ==================== Constructors ====================

    public SubgraphTopologyResult() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.subgraphBoundaries = new ArrayList<>();
    }

    public SubgraphTopologyResult(List<TopologyNode> nodes, List<TopologyEdge> edges, List<SubgraphBoundary> boundaries) {
        this.nodes = nodes != null ? new ArrayList<>(nodes) : new ArrayList<>();
        this.edges = edges != null ? new ArrayList<>(edges) : new ArrayList<>();
        this.subgraphBoundaries = boundaries != null ? new ArrayList<>(boundaries) : new ArrayList<>();
        this.nodeCount = this.nodes.size();
        this.edgeCount = this.edges.size();
        this.maxDepth = calculateMaxDepth();
    }

    // ==================== Business Methods ====================

    /**
     * 添加节点
     */
    public void addNode(TopologyNode node) {
        if (node != null) {
            this.nodes.add(node);
            this.nodeCount = this.nodes.size();
        }
    }

    /**
     * 添加边
     */
    public void addEdge(TopologyEdge edge) {
        if (edge != null) {
            this.edges.add(edge);
            this.edgeCount = this.edges.size();
        }
    }

    /**
     * 添加子图边界
     */
    public void addSubgraphBoundary(SubgraphBoundary boundary) {
        if (boundary != null) {
            this.subgraphBoundaries.add(boundary);
        }
    }

    /**
     * 计算最大嵌套深度
     */
    private int calculateMaxDepth() {
        return nodes.stream()
                .mapToInt(TopologyNode::getDepth)
                .max()
                .orElse(0);
    }

    // ==================== Getters and Setters ====================

    public List<TopologyNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<TopologyNode> nodes) {
        this.nodes = nodes != null ? new ArrayList<>(nodes) : new ArrayList<>();
        this.nodeCount = this.nodes.size();
        this.maxDepth = calculateMaxDepth();
    }

    public List<TopologyEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<TopologyEdge> edges) {
        this.edges = edges != null ? new ArrayList<>(edges) : new ArrayList<>();
        this.edgeCount = this.edges.size();
    }

    public List<SubgraphBoundary> getSubgraphBoundaries() {
        return subgraphBoundaries;
    }

    public void setSubgraphBoundaries(List<SubgraphBoundary> subgraphBoundaries) {
        this.subgraphBoundaries = subgraphBoundaries != null ? new ArrayList<>(subgraphBoundaries) : new ArrayList<>();
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    @Override
    public String toString() {
        return "SubgraphTopologyResult{" +
                "nodeCount=" + nodeCount +
                ", edgeCount=" + edgeCount +
                ", maxDepth=" + maxDepth +
                ", subgraphBoundaryCount=" + subgraphBoundaries.size() +
                '}';
    }
}
