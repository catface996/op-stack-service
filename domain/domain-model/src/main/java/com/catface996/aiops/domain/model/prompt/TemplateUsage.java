package com.catface996.aiops.domain.model.prompt;

import java.time.LocalDateTime;

/**
 * 模板用途领域模型
 *
 * <p>定义提示词模板的用途类型，如故障诊断、知识问答等</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public class TemplateUsage {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用途编码（如 FAULT_DIAGNOSIS）
     */
    private String code;

    /**
     * 用途名称（如 故障诊断）
     */
    private String name;

    /**
     * 用途描述
     */
    private String description;

    /**
     * 逻辑删除标记
     */
    private Boolean deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // 构造函数

    public TemplateUsage() {
        this.deleted = false;
    }

    // 工厂方法

    /**
     * 创建新用途的工厂方法
     */
    public static TemplateUsage create(String code, String name, String description) {
        TemplateUsage usage = new TemplateUsage();
        usage.setCode(code);
        usage.setName(name);
        usage.setDescription(description);
        usage.setDeleted(false);
        usage.setCreatedAt(LocalDateTime.now());
        usage.setUpdatedAt(LocalDateTime.now());
        return usage;
    }

    // 业务方法

    /**
     * 检查用途编码格式是否有效（大写字母和下划线）
     */
    public static boolean isValidCode(String code) {
        return code != null && code.matches("^[A-Z][A-Z0-9_]*$");
    }

    /**
     * 软删除
     */
    public void markDeleted() {
        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
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

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
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
        return "TemplateUsage{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", deleted=" + deleted +
                '}';
    }
}
