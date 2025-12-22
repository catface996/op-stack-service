package com.catface996.aiops.domain.model.subgraph;

/**
 * 拓扑节点值对象
 *
 * <p>v2.0 设计：用于拓扑图渲染，支持嵌套子图的展开/折叠</p>
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
public class TopologyNode {

    /**
     * 节点ID（资源ID）
     */
    private Long id;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 节点类型（如 SERVER, APPLICATION, SUBGRAPH）
     */
    private String type;

    /**
     * 节点状态
     */
    private String status;

    /**
     * 是否为子图类型
     */
    private boolean isSubgraph;

    /**
     * 是否已展开（仅对子图节点有效）
     */
    private boolean expanded;

    /**
     * 父子图ID（表示该节点属于哪个子图，null 表示根级别）
     */
    private Long parentSubgraphId;

    /**
     * 嵌套深度（0 表示直接成员）
     */
    private int depth;

    // ==================== Constructors ====================

    public TopologyNode() {
    }

    public TopologyNode(Long id, String name, String type, String status, boolean isSubgraph) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
        this.isSubgraph = isSubgraph;
        this.expanded = false;
        this.depth = 0;
    }

    // ==================== Getters and Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSubgraph() {
        return isSubgraph;
    }

    public void setSubgraph(boolean subgraph) {
        isSubgraph = subgraph;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
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

    @Override
    public String toString() {
        return "TopologyNode{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", isSubgraph=" + isSubgraph +
                ", expanded=" + expanded +
                '}';
    }
}
