package com.catface996.aiops.domain.model.report;

/**
 * 报告模板分类枚举
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public enum ReportTemplateCategory {

    /**
     * 事件报告模板
     */
    Incident("事件报告模板"),

    /**
     * 性能报告模板
     */
    Performance("性能报告模板"),

    /**
     * 安全报告模板
     */
    Security("安全报告模板"),

    /**
     * 审计报告模板
     */
    Audit("审计报告模板");

    private final String description;

    ReportTemplateCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据名称获取枚举值（忽略大小写）
     *
     * @param name 枚举名称
     * @return 枚举值，如果不存在返回 null
     */
    public static ReportTemplateCategory fromName(String name) {
        if (name == null) {
            return null;
        }
        for (ReportTemplateCategory category : values()) {
            if (category.name().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return null;
    }
}
