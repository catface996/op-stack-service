package com.catface996.aiops.domain.model.report;

/**
 * 报告类型枚举
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public enum ReportType {

    /**
     * 系统诊断报告
     */
    Diagnosis("系统诊断报告"),

    /**
     * 合规审计报告
     */
    Audit("合规审计报告"),

    /**
     * 性能分析报告
     */
    Performance("性能分析报告"),

    /**
     * 安全评估报告
     */
    Security("安全评估报告");

    private final String description;

    ReportType(String description) {
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
    public static ReportType fromName(String name) {
        if (name == null) {
            return null;
        }
        for (ReportType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
