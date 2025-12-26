package com.catface996.aiops.domain.model.node;

import java.time.LocalDateTime;

/**
 * 节点类型领域模型
 *
 * <p>节点类型定义了资源节点的分类</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public class NodeType {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 类型编码（唯一）
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
     * 图标 URL
     */
    private String icon;

    /**
     * 是否系统预置
     */
    private Boolean isSystem;

    /**
     * 属性定义 Schema（JSON格式）
     */
    private String attributeSchema;

    /**
     * 创建人 ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // 构造函数

    public NodeType() {
        this.isSystem = false;
    }

    public NodeType(Long id, String code, String name, String description, String icon,
                    Boolean isSystem, String attributeSchema, Long createdBy,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.isSystem = isSystem;
        this.attributeSchema = attributeSchema;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
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
        return "NodeType{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isSystem=" + isSystem +
                '}';
    }
}
