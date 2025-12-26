package com.catface996.aiops.domain.model.prompt;

import java.time.LocalDateTime;

/**
 * 提示词模板领域模型
 *
 * <p>提示词模板的主表，存储模板的基本信息和当前版本号</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public class PromptTemplate {

    /**
     * 内容最大长度（64KB）
     */
    public static final int MAX_CONTENT_LENGTH = 65535;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 用途ID
     */
    private Long usageId;

    /**
     * 用途名称（关联查询）
     */
    private String usageName;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 当前版本号
     */
    private Integer currentVersion;

    /**
     * 乐观锁版本（数据库行版本）
     */
    private Integer version;

    /**
     * 逻辑删除标记
     */
    private Boolean deleted;

    /**
     * 创建人ID
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

    /**
     * 当前版本内容（非持久化字段，用于返回）
     */
    private String content;

    // 构造函数

    public PromptTemplate() {
        this.currentVersion = 1;
        this.version = 0;
        this.deleted = false;
    }

    // 工厂方法

    /**
     * 创建新模板的工厂方法
     */
    public static PromptTemplate create(String name, Long usageId, String description, Long createdBy) {
        PromptTemplate template = new PromptTemplate();
        template.setName(name);
        template.setUsageId(usageId);
        template.setDescription(description);
        template.setCurrentVersion(1);
        template.setVersion(0);
        template.setDeleted(false);
        template.setCreatedBy(createdBy);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        return template;
    }

    // 业务方法

    /**
     * 递增版本号（创建新版本时调用）
     */
    public void incrementVersion() {
        this.currentVersion = (this.currentVersion == null) ? 1 : this.currentVersion + 1;
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
     * 更新模板基本信息
     */
    public void update(String name, Long usageId, String description) {
        if (name != null) {
            this.name = name;
        }
        if (usageId != null) {
            this.usageId = usageId;
        }
        if (description != null) {
            this.description = description;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 验证内容是否超过大小限制
     */
    public static boolean isContentTooLarge(String content) {
        return content != null && content.getBytes().length > MAX_CONTENT_LENGTH;
    }

    /**
     * 验证内容是否为空
     */
    public static boolean isContentEmpty(String content) {
        return content == null || content.trim().isEmpty();
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

    public Long getUsageId() {
        return usageId;
    }

    public void setUsageId(Long usageId) {
        this.usageId = usageId;
    }

    public String getUsageName() {
        return usageName;
    }

    public void setUsageName(String usageName) {
        this.usageName = usageName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(Integer currentVersion) {
        this.currentVersion = currentVersion;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "PromptTemplate{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", usageId=" + usageId +
                ", currentVersion=" + currentVersion +
                ", deleted=" + deleted +
                ", createdBy=" + createdBy +
                '}';
    }
}
