package com.catface996.aiops.common.enums;

/**
 * 参数验证相关错误码
 *
 * <p>所有参数验证相关的错误码，HTTP 状态码为 400 Bad Request。</p>
 *
 * @author AI Assistant
 * @since 2025-11-24
 */
public enum ParamErrorCode implements ErrorCode {

    /**
     * 密码不符合强度要求（支持详细的验证错误列表）
     */
    INVALID_PASSWORD("PARAM_001", "密码不符合强度要求"),

    /**
     * 参数验证失败
     */
    VALIDATION_FAILED("PARAM_002", "请求参数无效"),

    /**
     * 参数格式错误
     */
    INVALID_FORMAT("PARAM_003", "参数格式错误");

    private final String code;
    private final String message;

    ParamErrorCode(String code, String message) {
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
