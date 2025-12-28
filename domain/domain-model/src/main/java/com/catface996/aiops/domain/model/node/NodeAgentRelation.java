package com.catface996.aiops.domain.model.node;

import java.time.LocalDateTime;

/**
 * Node-Agent 关联领域模型
 *
 * <p>表示 ResourceNode 与 Agent 之间的绑定关系</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: 系统必须支持将 Agent 与 ResourceNode 建立多对多的关联关系</li>
 *   <li>FR-006: 系统必须防止重复绑定</li>
 *   <li>FR-007: 系统必须对绑定记录支持软删除</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public class NodeAgentRelation {

    /**
     * 关联记录 ID
     */
    private Long id;

    /**
     * 资源节点 ID
     */
    private Long nodeId;

    /**
     * Agent ID
     */
    private Long agentId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 软删除标记
     */
    private Boolean deleted;

    // 构造函数

    public NodeAgentRelation() {
        this.deleted = false;
    }

    public NodeAgentRelation(Long id, Long nodeId, Long agentId, LocalDateTime createdAt, Boolean deleted) {
        this.id = id;
        this.nodeId = nodeId;
        this.agentId = agentId;
        this.createdAt = createdAt;
        this.deleted = deleted;
    }

    // 工厂方法

    /**
     * 创建新的 Node-Agent 关联关系
     *
     * @param nodeId  资源节点 ID
     * @param agentId Agent ID
     * @return 新创建的关联关系
     */
    public static NodeAgentRelation create(Long nodeId, Long agentId) {
        NodeAgentRelation relation = new NodeAgentRelation();
        relation.setNodeId(nodeId);
        relation.setAgentId(agentId);
        relation.setCreatedAt(LocalDateTime.now());
        relation.setDeleted(false);
        return relation;
    }

    // 业务方法

    /**
     * 标记为已删除（软删除）
     */
    public void markDeleted() {
        this.deleted = true;
    }

    /**
     * 检查是否已删除
     *
     * @return true 如果已删除
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "NodeAgentRelation{" +
                "id=" + id +
                ", nodeId=" + nodeId +
                ", agentId=" + agentId +
                ", createdAt=" + createdAt +
                ", deleted=" + deleted +
                '}';
    }
}
