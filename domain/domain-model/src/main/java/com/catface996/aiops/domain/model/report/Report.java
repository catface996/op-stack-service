package com.catface996.aiops.domain.model.report;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报告领域模型
 *
 * <p>报告创建后不可修改（immutable），不提供 update 方法。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public class Report {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 报告标题
     */
    private String title;

    /**
     * 报告类型
     */
    private ReportType type;

    /**
     * 报告状态
     */
    private ReportStatus status;

    /**
     * 作者
     */
    private String author;

    /**
     * 报告摘要
     */
    private String summary;

    /**
     * 报告内容（Markdown 格式）
     */
    private String content;

    /**
     * 标签数组
     */
    private List<String> tags;

    /**
     * 关联的拓扑图 ID
     */
    private Long topologyId;

    /**
     * 逻辑删除标记
     */
    private Boolean deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    // 构造函数

    public Report() {
        this.deleted = false;
    }

    // 工厂方法

    /**
     * 创建新报告的工厂方法
     */
    public static Report create(String title, ReportType type, ReportStatus status,
                                 String author, String summary, String content,
                                 List<String> tags, Long topologyId) {
        Report report = new Report();
        report.setTitle(title);
        report.setType(type);
        report.setStatus(status);
        report.setAuthor(author);
        report.setSummary(summary);
        report.setContent(content);
        report.setTags(tags);
        report.setTopologyId(topologyId);
        report.setDeleted(false);
        report.setCreatedAt(LocalDateTime.now());
        return report;
    }

    // 业务方法

    /**
     * 软删除
     */
    public void markDeleted() {
        this.deleted = true;
    }

    /**
     * 检查报告是否已删除
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ReportType getType() {
        return type;
    }

    public void setType(ReportType type) {
        this.type = type;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
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

    public Long getTopologyId() {
        return topologyId;
    }

    public void setTopologyId(Long topologyId) {
        this.topologyId = topologyId;
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

    @Override
    public String toString() {
        return "Report{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", author='" + author + '\'' +
                ", deleted=" + deleted +
                '}';
    }
}
