package com.catface996.aiops.domain.api.exception.auth;

import com.catface996.aiops.common.exception.BaseException;

/**
 * 认证异常
 * 当用户认证失败时抛出此异常（如用户名或密码错误）
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
public class AuthenticationException extends BaseException {

    private static final String ERROR_CODE = "401001";

    public AuthenticationException(String errorMessage) {
        super(ERROR_CODE, errorMessage);
    }

    public AuthenticationException(String errorMessage, Throwable cause) {
        super(ERROR_CODE, errorMessage, cause);
    }

    /**
     * 创建默认的认证失败异常
     */
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("用户名或密码错误");
    }
}
