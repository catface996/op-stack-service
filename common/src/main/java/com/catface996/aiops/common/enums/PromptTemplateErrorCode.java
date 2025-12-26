package com.catface996.aiops.common.enums;

/**
 * 提示词模板相关错误码
 *
 * <p>包括模板不存在、名称冲突、版本控制等错误。</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public enum PromptTemplateErrorCode implements ErrorCode {

    // ==================== 模板不存在 (404) ====================

    /**
     * 模板不存在
     */
    TEMPLATE_NOT_FOUND("PT_NOT_FOUND_001", "模板不存在: {0}"),

    /**
     * 版本不存在
     */
    VERSION_NOT_FOUND("PT_NOT_FOUND_002", "版本不存在: 模板{0}的版本{1}"),

    /**
     * 用途不存在
     */
    USAGE_NOT_FOUND("PT_NOT_FOUND_003", "用途不存在: {0}"),

    // ==================== 冲突错误 (409) ====================

    /**
     * 模板名称已存在
     */
    TEMPLATE_NAME_EXISTS("PT_CONFLICT_001", "模板名称已存在: {0}"),

    /**
     * 版本冲突（乐观锁）
     */
    VERSION_CONFLICT("PT_CONFLICT_002", "版本冲突，请刷新后重试"),

    /**
     * 用途编码已存在
     */
    USAGE_CODE_EXISTS("PT_CONFLICT_003", "用途编码已存在: {0}"),

    // ==================== 参数错误 (400) ====================

    /**
     * 模板内容为空
     */
    TEMPLATE_CONTENT_EMPTY("PT_PARAM_001", "模板内容不能为空"),

    /**
     * 模板内容超过大小限制
     */
    TEMPLATE_CONTENT_TOO_LARGE("PT_PARAM_002", "模板内容超过64KB限制"),

    /**
     * 内容无变化
     */
    TEMPLATE_CONTENT_UNCHANGED("PT_PARAM_003", "内容无变化，无需更新"),

    /**
     * 已是最早版本
     */
    ALREADY_EARLIEST_VERSION("PT_PARAM_004", "已是最早版本，无法回滚"),

    /**
     * 无效的用途编码格式
     */
    INVALID_USAGE_CODE("PT_PARAM_005", "无效的用途编码格式，需为大写字母和下划线"),

    // ==================== 业务规则 (400) ====================

    /**
     * 用途正在被使用
     */
    USAGE_IN_USE("PT_RULE_001", "用途正在被使用，无法删除");

    private final String code;
    private final String message;

    PromptTemplateErrorCode(String code, String message) {
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
