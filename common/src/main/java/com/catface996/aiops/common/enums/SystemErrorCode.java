package com.catface996.aiops.common.enums;

/**
 * 系统相关错误码
 *
 * <p>所有系统级别的错误码，HTTP 状态码为 500 Internal Server Error。</p>
 *
 * @author AI Assistant
 * @since 2025-11-24
 */
public enum SystemErrorCode implements ErrorCode {

    /**
     * 数据库错误
     */
    DATABASE_ERROR("SYS_001", "系统异常，请稍后重试"),

    /**
     * 未知错误
     */
    UNKNOWN_ERROR("SYS_002", "系统错误，请稍后重试");

    private final String code;
    private final String message;

    SystemErrorCode(String code, String message) {
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
