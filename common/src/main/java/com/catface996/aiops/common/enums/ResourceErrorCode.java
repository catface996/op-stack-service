package com.catface996.aiops.common.enums;

/**
 * 资源相关错误码
 *
 * <p>包括资源不存在、资源冲突、资源锁定等。</p>
 *
 * @author AI Assistant
 * @since 2025-11-24
 */
public enum ResourceErrorCode implements ErrorCode {

    // ==================== 资源不存在 (404) ====================

    /**
     * 账号不存在
     */
    ACCOUNT_NOT_FOUND("NOT_FOUND_001", "账号不存在"),

    /**
     * 资源不存在
     */
    RESOURCE_NOT_FOUND("NOT_FOUND_002", "资源不存在"),

    // ==================== 资源冲突 (409) ====================

    /**
     * 用户名已存在
     */
    USERNAME_CONFLICT("CONFLICT_001", "用户名已存在"),

    /**
     * 邮箱已存在
     */
    EMAIL_CONFLICT("CONFLICT_002", "邮箱已存在"),

    // ==================== 资源锁定 (423) ====================

    /**
     * 账号已锁定（支持参数化消息）
     *
     * <p>消息模板：账号已锁定，请在{0}分钟后重试</p>
     */
    ACCOUNT_LOCKED("LOCKED_001", "账号已锁定，请在{0}分钟后重试");

    private final String code;
    private final String message;

    ResourceErrorCode(String code, String message) {
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
