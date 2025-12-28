package com.catface996.aiops.domain.model.report;

/**
 * 报告状态枚举
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public enum ReportStatus {

    /**
     * 草稿
     */
    Draft("草稿"),

    /**
     * 已定稿
     */
    Final("已定稿"),

    /**
     * 已归档
     */
    Archived("已归档");

    private final String description;

    ReportStatus(String description) {
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
    public static ReportStatus fromName(String name) {
        if (name == null) {
            return null;
        }
        for (ReportStatus status : values()) {
            if (status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }
}
