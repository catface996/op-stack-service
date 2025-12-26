package com.catface996.aiops.domain.model.prompt;

import java.time.LocalDateTime;

/**
 * 模板版本领域模型
 *
 * <p>存储提示词模板的每个版本快照，包括内容和变更说明</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public class PromptTemplateVersion {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 模板ID
     */
    private Long templateId;

    /**
     * 版本号
     */
    private Integer versionNumber;

    /**
     * 模板内容
     */
    private String content;

    /**
     * 变更说明
     */
    private String changeNote;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    // 构造函数

    public PromptTemplateVersion() {
    }

    // 工厂方法

    /**
     * 创建新版本的工厂方法
     *
     * @param templateId    模板ID
     * @param versionNumber 版本号
     * @param content       模板内容
     * @param changeNote    变更说明
     * @param createdBy     创建人ID
     * @return 版本实例
     */
    public static PromptTemplateVersion create(Long templateId, Integer versionNumber,
                                                String content, String changeNote, Long createdBy) {
        PromptTemplateVersion version = new PromptTemplateVersion();
        version.setTemplateId(templateId);
        version.setVersionNumber(versionNumber);
        version.setContent(content);
        version.setChangeNote(changeNote);
        version.setCreatedBy(createdBy);
        version.setCreatedAt(LocalDateTime.now());
        return version;
    }

    /**
     * 创建初始版本的工厂方法（版本号为1）
     *
     * @param templateId 模板ID
     * @param content    模板内容
     * @param createdBy  创建人ID
     * @return 版本实例
     */
    public static PromptTemplateVersion createInitial(Long templateId, String content, Long createdBy) {
        return create(templateId, 1, content, "初始版本", createdBy);
    }

    /**
     * 创建回滚版本的工厂方法
     *
     * @param templateId        模板ID
     * @param newVersionNumber  新版本号
     * @param content           回滚的内容
     * @param rollbackFromVersion 回滚自的版本号
     * @param createdBy         创建人ID
     * @return 版本实例
     */
    public static PromptTemplateVersion createRollback(Long templateId, Integer newVersionNumber,
                                                        String content, Integer rollbackFromVersion,
                                                        Long createdBy) {
        String changeNote = "从版本 " + rollbackFromVersion + " 回滚";
        return create(templateId, newVersionNumber, content, changeNote, createdBy);
    }

    // 业务方法

    /**
     * 判断是否为初始版本
     */
    public boolean isInitialVersion() {
        return versionNumber != null && versionNumber == 1;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getChangeNote() {
        return changeNote;
    }

    public void setChangeNote(String changeNote) {
        this.changeNote = changeNote;
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

    @Override
    public String toString() {
        return "PromptTemplateVersion{" +
                "id=" + id +
                ", templateId=" + templateId +
                ", versionNumber=" + versionNumber +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                '}';
    }
}
