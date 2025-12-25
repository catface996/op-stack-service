package com.catface996.aiops.domain.model.subgraph;

/**
 * 拓扑边（关系）值对象
 *
 * <p>v2.0 设计：表示拓扑图中两个节点之间的关系</p>
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
public class TopologyEdge {

    /**
     * 源节点ID
     */
    private Long source;

    /**
     * 目标节点ID
     */
    private Long target;

    /**
     * 关系类型
     */
    private String type;

    /**
     * 关系方向（FORWARD, BACKWARD, BIDIRECTIONAL）
     */
    private String direction;

    /**
     * 关系强度（STRONG, MEDIUM, WEAK）
     */
    private String strength;

    /**
     * 关系状态（ACTIVE, INACTIVE）
     */
    private String status;

    // ==================== Constructors ====================

    public TopologyEdge() {
    }

    public TopologyEdge(Long source, Long target, String type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }

    // ==================== Getters and Setters ====================

    public Long getSource() {
        return source;
    }

    public void setSource(Long source) {
        this.source = source;
    }

    public Long getTarget() {
        return target;
    }

    public void setTarget(Long target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TopologyEdge{" +
                "source=" + source +
                ", target=" + target +
                ", type='" + type + '\'' +
                '}';
    }
}
