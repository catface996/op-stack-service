package com.catface996.aiops.common.enums;

/**
 * 报告相关错误码
 *
 * <p>包括报告不存在、模板不存在等错误。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public enum ReportErrorCode implements ErrorCode {

    // ==================== 报告不存在 (404) ====================

    /**
     * 报告不存在
     */
    REPORT_NOT_FOUND("RPT_NOT_FOUND_001", "报告不存在: {0}"),

    /**
     * 报告模板不存在
     */
    TEMPLATE_NOT_FOUND("RPT_NOT_FOUND_002", "报告模板不存在: {0}"),

    /**
     * 关联的拓扑不存在
     */
    TOPOLOGY_NOT_FOUND("RPT_NOT_FOUND_003", "关联的拓扑不存在: {0}"),

    // ==================== 冲突错误 (409) ====================

    /**
     * 模板名称已存在
     */
    TEMPLATE_NAME_EXISTS("RPT_CONFLICT_001", "模板名称已存在: {0}"),

    /**
     * 版本冲突（乐观锁）
     */
    VERSION_CONFLICT("RPT_CONFLICT_002", "版本冲突，请刷新后重试"),

    // ==================== 参数错误 (400) ====================

    /**
     * 无效的报告类型
     */
    INVALID_REPORT_TYPE("RPT_PARAM_001", "无效的报告类型: {0}"),

    /**
     * 无效的报告状态
     */
    INVALID_REPORT_STATUS("RPT_PARAM_002", "无效的报告状态: {0}"),

    /**
     * 无效的模板分类
     */
    INVALID_TEMPLATE_CATEGORY("RPT_PARAM_003", "无效的模板分类: {0}"),

    /**
     * 模板内容为空
     */
    TEMPLATE_CONTENT_EMPTY("RPT_PARAM_004", "模板内容不能为空");

    private final String code;
    private final String message;

    ReportErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
