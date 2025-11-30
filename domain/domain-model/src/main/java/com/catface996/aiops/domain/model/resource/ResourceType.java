package com.catface996.aiops.domain.model.resource;

import java.time.LocalDateTime;

/**
 * 资源类型实体
 *
 * 定义资源的分类类型，如服务器、应用、数据库等
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public class ResourceType {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 类型编码：SERVER, APPLICATION, DATABASE, API, MIDDLEWARE, REPORT
     */
    private String code;

    /**
     * 类型名称
     */
    private String name;

    /**
     * 类型描述
     */
    private String description;

    /**
     * 图标URL
     */
    private String icon;

    /**
     * 是否系统预置
     */
    private Boolean isSystem;

    /**
     * 属性定义Schema（JSON格式，为F02-1预留）
     */
    private String attributeSchema;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    private Long createdBy;

    // 构造函数
    public ResourceType() {
    }

    public ResourceType(Long id, String code, String name, String description,
                        String icon, Boolean isSystem, String attributeSchema,
                        LocalDateTime createdAt, LocalDateTime updatedAt, Long createdBy) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.isSystem = isSystem;
        this.attributeSchema = attributeSchema;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
    }

    // 业务方法

    /**
     * 判断是否为系统预置类型
     */
    public boolean isSystemType() {
        return Boolean.TRUE.equals(this.isSystem);
    }

    /**
     * 判断是否可以删除
     * 系统预置类型不可删除
     */
    public boolean canDelete() {
        return !isSystemType();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    public String getAttributeSchema() {
        return attributeSchema;
    }

    public void setAttributeSchema(String attributeSchema) {
        this.attributeSchema = attributeSchema;
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

    @Override
    public String toString() {
        return "ResourceType{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", icon='" + icon + '\'' +
                ", isSystem=" + isSystem +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy=" + createdBy +
                '}';
    }
}
