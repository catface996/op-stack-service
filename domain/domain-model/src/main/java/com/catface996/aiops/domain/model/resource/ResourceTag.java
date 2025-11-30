package com.catface996.aiops.domain.model.resource;

import java.time.LocalDateTime;

/**
 * 资源标签实体
 *
 * 用于资源的标签管理
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public class ResourceTag {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 创建人ID
     */
    private Long createdBy;

    // 构造函数
    public ResourceTag() {
    }

    public ResourceTag(Long id, Long resourceId, String tagName,
                       LocalDateTime createdAt, Long createdBy) {
        this.id = id;
        this.resourceId = resourceId;
        this.tagName = tagName;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
    }

    /**
     * 创建新标签的工厂方法
     */
    public static ResourceTag create(Long resourceId, String tagName, Long createdBy) {
        ResourceTag tag = new ResourceTag();
        tag.setResourceId(resourceId);
        tag.setTagName(tagName);
        tag.setCreatedBy(createdBy);
        tag.setCreatedAt(LocalDateTime.now());
        return tag;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "ResourceTag{" +
                "id=" + id +
                ", resourceId=" + resourceId +
                ", tagName='" + tagName + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy=" + createdBy +
                '}';
    }
}
