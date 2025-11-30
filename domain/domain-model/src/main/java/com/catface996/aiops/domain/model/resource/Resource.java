package com.catface996.aiops.domain.model.resource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 资源聚合根
 *
 * IT资源管理的核心实体，包含资源的完整信息和业务逻辑
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public class Resource {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 资源描述
     */
    private String description;

    /**
     * 资源类型ID
     */
    private Long resourceTypeId;

    /**
     * 资源类型（关联对象）
     */
    private ResourceType resourceType;

    /**
     * 资源状态
     */
    private ResourceStatus status;

    /**
     * 扩展属性（JSON格式）
     */
    private String attributes;

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

    /**
     * 创建者ID（第一个Owner）
     */
    private Long createdBy;

    /**
     * 资源标签列表
     */
    private List<ResourceTag> tags = new ArrayList<>();

    // 构造函数
    public Resource() {
        this.status = ResourceStatus.RUNNING;
        this.version = 0;
    }

    public Resource(Long id, String name, String description, Long resourceTypeId,
                    ResourceStatus status, String attributes, Integer version,
                    LocalDateTime createdAt, LocalDateTime updatedAt, Long createdBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.resourceTypeId = resourceTypeId;
        this.status = status;
        this.attributes = attributes;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
    }

    // 业务方法

    /**
     * 创建新资源的工厂方法
     */
    public static Resource create(String name, String description, Long resourceTypeId,
                                  String attributes, Long createdBy) {
        Resource resource = new Resource();
        resource.setName(name);
        resource.setDescription(description);
        resource.setResourceTypeId(resourceTypeId);
        resource.setAttributes(attributes);
        resource.setStatus(ResourceStatus.RUNNING);
        resource.setVersion(0);
        resource.setCreatedBy(createdBy);
        resource.setCreatedAt(LocalDateTime.now());
        resource.setUpdatedAt(LocalDateTime.now());
        return resource;
    }

    /**
     * 判断是否为资源所有者
     *
     * @param userId 用户ID
     * @return true if user is owner
     */
    public boolean isOwner(Long userId) {
        return userId != null && userId.equals(this.createdBy);
    }

    /**
     * 判断资源是否可以被修改
     */
    public boolean canModify() {
        return status != null && status.canModify();
    }

    /**
     * 判断资源是否可以被删除
     */
    public boolean canDelete() {
        return status != null && status.canDelete();
    }

    /**
     * 判断资源是否正在运行
     */
    public boolean isRunning() {
        return ResourceStatus.RUNNING.equals(this.status);
    }

    /**
     * 判断资源是否已停止
     */
    public boolean isStopped() {
        return ResourceStatus.STOPPED.equals(this.status);
    }

    /**
     * 判断资源是否在维护中
     */
    public boolean isInMaintenance() {
        return ResourceStatus.MAINTENANCE.equals(this.status);
    }

    /**
     * 判断资源是否已下线
     */
    public boolean isOffline() {
        return ResourceStatus.OFFLINE.equals(this.status);
    }

    /**
     * 更新资源状态
     *
     * @param newStatus 新状态
     */
    public void updateStatus(ResourceStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 启动资源
     */
    public void start() {
        this.status = ResourceStatus.RUNNING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 停止资源
     */
    public void stop() {
        this.status = ResourceStatus.STOPPED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 进入维护模式
     */
    public void enterMaintenance() {
        this.status = ResourceStatus.MAINTENANCE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 下线资源
     */
    public void offline() {
        this.status = ResourceStatus.OFFLINE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新资源基本信息
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

    /**
     * 添加标签
     */
    public void addTag(ResourceTag tag) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        this.tags.add(tag);
    }

    /**
     * 移除标签
     */
    public void removeTag(String tagName) {
        if (this.tags != null) {
            this.tags.removeIf(tag -> tag.getTagName().equals(tagName));
        }
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

    public Long getResourceTypeId() {
        return resourceTypeId;
    }

    public void setResourceTypeId(Long resourceTypeId) {
        this.resourceTypeId = resourceTypeId;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
        this.status = status;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
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

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public List<ResourceTag> getTags() {
        return tags;
    }

    public void setTags(List<ResourceTag> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", resourceTypeId=" + resourceTypeId +
                ", status=" + status +
                ", version=" + version +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                '}';
    }
}
