package com.catface996.aiops.domain.model.node;

import java.time.LocalDateTime;

/**
 * 资源节点领域模型
 *
 * <p>资源节点是实际的 IT 资源，如服务器、应用、数据库等</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: resource 表拆分为 topology 表和 node 表</li>
 *   <li>FR-003: node 表字段定义</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public class Node {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 节点描述
     */
    private String description;

    /**
     * 节点类型ID
     */
    private Long nodeTypeId;

    /**
     * 节点类型（关联对象）
     */
    private NodeType nodeType;

    /**
     * 节点状态
     */
    private NodeStatus status;

    /**
     * 架构层级
     */
    private NodeLayer layer;

    /**
     * 扩展属性（JSON格式）
     */
    private String attributes;

    /**
     * 创建者ID
     */
    private Long createdBy;

    /**
     * 版本号（乐观锁）
     */
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // 构造函数

    public Node() {
        this.status = NodeStatus.RUNNING;
        this.version = 0;
    }

    public Node(Long id, String name, String description, Long nodeTypeId,
                NodeStatus status, String attributes, Long createdBy,
                Integer version, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.nodeTypeId = nodeTypeId;
        this.status = status;
        this.attributes = attributes;
        this.createdBy = createdBy;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 工厂方法

    /**
     * 创建新节点的工厂方法
     */
    public static Node create(String name, String description, Long nodeTypeId, NodeLayer layer,
                              String attributes, Long createdBy) {
        Node node = new Node();
        node.setName(name);
        node.setDescription(description);
        node.setNodeTypeId(nodeTypeId);
        node.setLayer(layer);
        node.setAttributes(attributes);
        node.setStatus(NodeStatus.RUNNING);
        node.setVersion(0);
        node.setCreatedBy(createdBy);
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());
        return node;
    }

    // 业务方法

    /**
     * 判断是否为节点所有者
     */
    public boolean isOwner(Long userId) {
        return userId != null && userId.equals(this.createdBy);
    }

    /**
     * 判断节点是否可以被修改
     */
    public boolean canModify() {
        return status != null && status.canModify();
    }

    /**
     * 判断节点是否可以被删除
     */
    public boolean canDelete() {
        return status != null && status.canDelete();
    }

    /**
     * 判断节点是否正在运行
     */
    public boolean isRunning() {
        return NodeStatus.RUNNING.equals(this.status);
    }

    /**
     * 更新节点状态
     */
    public void updateStatus(NodeStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新节点基本信息
     */
    public void update(String name, String description, String attributes) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (attributes != null) {
            this.attributes = attributes;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 增加版本号（用于乐观锁）
     */
    public void incrementVersion() {
        this.version = (this.version == null) ? 1 : this.version + 1;
    }

    // Getters and Setters

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getNodeTypeId() {
        return nodeTypeId;
    }

    public void setNodeTypeId(Long nodeTypeId) {
        this.nodeTypeId = nodeTypeId;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public NodeLayer getLayer() {
        return layer;
    }

    public void setLayer(NodeLayer layer) {
        this.layer = layer;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", nodeTypeId=" + nodeTypeId +
                ", status=" + status +
                ", layer=" + layer +
                ", version=" + version +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                '}';
    }
}
