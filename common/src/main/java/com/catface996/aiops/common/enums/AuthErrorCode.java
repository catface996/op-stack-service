package com.catface996.aiops.common.enums;

/**
 * 认证相关错误码
 *
 * <p>所有认证相关的错误码，HTTP 状态码为 401 Unauthorized。</p>
 *
 * @author AI Assistant
 * @since 2025-11-24
 */
public enum AuthErrorCode implements ErrorCode {

    /**
     * 用户名或密码错误
     */
    INVALID_CREDENTIALS("AUTH_001", "用户名或密码错误"),

    /**
     * Token无效
     */
    TOKEN_INVALID("AUTH_002", "Token无效"),

    /**
     * 会话已过期
     */
    SESSION_EXPIRED("AUTH_003", "会话已过期"),

    /**
     * 会话不存在
     */
    SESSION_NOT_FOUND("AUTH_004", "会话不存在");

    private final String code;
    private final String message;

    AuthErrorCode(String code, String message) {
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
