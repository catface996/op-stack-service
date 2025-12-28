package com.catface996.aiops.domain.model.report;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报告模板领域模型
 *
 * <p>支持完整的 CRUD 操作，使用乐观锁进行并发控制。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public class ReportTemplate {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板分类
     */
    private ReportTemplateCategory category;

    /**
     * 模板内容（含占位符的 Markdown）
     */
    private String content;

    /**
     * 标签数组
     */
    private List<String> tags;

    /**
     * 乐观锁版本号
     */
    private Integer version;

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

    public ReportTemplate() {
        this.version = 0;
        this.deleted = false;
    }

    // 工厂方法

    /**
     * 创建新模板的工厂方法
     */
    public static ReportTemplate create(String name, String description,
                                         ReportTemplateCategory category,
                                         String content, List<String> tags) {
        ReportTemplate template = new ReportTemplate();
        template.setName(name);
        template.setDescription(description);
        template.setCategory(category);
        template.setContent(content);
        template.setTags(tags);
        template.setVersion(0);
        template.setDeleted(false);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        return template;
    }

    // 业务方法

    /**
     * 更新模板信息
     */
    public void update(String name, String description,
                       ReportTemplateCategory category,
                       String content, List<String> tags) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (category != null) {
            this.category = category;
        }
        if (content != null) {
            this.content = content;
        }
        if (tags != null) {
            this.tags = tags;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 软删除
     */
    public void markDeleted() {
        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查模板是否已删除
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

    public ReportTemplateCategory getCategory() {
        return category;
    }

    public void setCategory(ReportTemplateCategory category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
        return "ReportTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", version=" + version +
                ", deleted=" + deleted +
                '}';
    }
}
