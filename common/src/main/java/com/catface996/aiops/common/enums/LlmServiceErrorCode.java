package com.catface996.aiops.common.enums;

/**
 * LLM 服务相关错误码
 *
 * <p>包括服务不存在、名称重复、状态冲突等。</p>
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
public enum LlmServiceErrorCode implements ErrorCode {

    // ==================== 资源不存在 (404) ====================

    /**
     * LLM 服务不存在
     */
    LLM_SERVICE_NOT_FOUND("LLM_NOT_FOUND_001", "LLM 服务不存在"),

    // ==================== 资源冲突 (409) ====================

    /**
     * LLM 服务名称已存在
     */
    LLM_SERVICE_NAME_DUPLICATE("LLM_CONFLICT_001", "服务名称已存在"),

    /**
     * LLM 服务被 Agent 引用
     */
    LLM_SERVICE_IN_USE("LLM_CONFLICT_002", "服务被 Agent 引用，如需删除请使用强制删除"),

    // ==================== 业务规则错误 (400) ====================

    /**
     * 无法禁用唯一默认服务
     */
    LLM_SERVICE_CANNOT_DISABLE("LLM_BIZ_001", "无法禁用唯一的默认服务"),

    /**
     * 只能将启用的服务设为默认
     */
    LLM_SERVICE_MUST_ENABLED("LLM_BIZ_002", "只能将启用的服务设为默认"),

    /**
     * 模型参数无效
     */
    LLM_SERVICE_INVALID_PARAMS("LLM_BIZ_003", "模型参数无效");

    private final String code;
    private final String message;

    LlmServiceErrorCode(String code, String message) {
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
