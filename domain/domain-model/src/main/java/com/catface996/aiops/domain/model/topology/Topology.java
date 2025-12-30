package com.catface996.aiops.domain.model.topology;

import java.time.LocalDateTime;

/**
 * 拓扑图领域模型
 *
 * <p>拓扑图是业务场景的容器，用于组织和展示节点之间的关系</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: resource 表拆分为 topology 表和 node 表</li>
 *   <li>FR-002: topology 表字段定义</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public class Topology {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 拓扑图名称
     */
    private String name;

    /**
     * 拓扑图描述
     */
    private String description;

    /**
     * 拓扑图状态
     */
    private TopologyStatus status;

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

    public Topology() {
        this.status = TopologyStatus.RUNNING;
        this.version = 0;
    }

    public Topology(Long id, String name, String description, TopologyStatus status,
                    String attributes, Long createdBy,
                    Integer version, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.attributes = attributes;
        this.createdBy = createdBy;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 工厂方法

    /**
     * 创建新拓扑图的工厂方法
     */
    public static Topology create(String name, String description, String attributes, Long createdBy) {
        Topology topology = new Topology();
        topology.setName(name);
        topology.setDescription(description);
        topology.setAttributes(attributes);
        topology.setStatus(TopologyStatus.RUNNING);
        topology.setVersion(0);
        topology.setCreatedBy(createdBy);
        topology.setCreatedAt(LocalDateTime.now());
        topology.setUpdatedAt(LocalDateTime.now());
        return topology;
    }

    // 业务方法

    /**
     * 判断是否为拓扑图所有者
     */
    public boolean isOwner(Long userId) {
        return userId != null && userId.equals(this.createdBy);
    }

    /**
     * 判断拓扑图是否可以被修改
     */
    public boolean canModify() {
        return status != null && status.canModify();
    }

    /**
     * 判断拓扑图是否可以被删除
     */
    public boolean canDelete() {
        return status != null && status.canDelete();
    }

    /**
     * 判断拓扑图是否正在运行
     */
    public boolean isRunning() {
        return TopologyStatus.RUNNING.equals(this.status);
    }

    /**
     * 更新拓扑图状态
     */
    public void updateStatus(TopologyStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新拓扑图基本信息
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

    public TopologyStatus getStatus() {
        return status;
    }

    public void setStatus(TopologyStatus status) {
        this.status = status;
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
        return "Topology{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", version=" + version +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                '}';
    }
}
