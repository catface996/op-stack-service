package com.catface996.aiops.domain.api.exception.auth;

import com.catface996.aiops.common.exception.BaseException;

/**
 * 会话过期异常
 * 当用户会话已过期时抛出此异常
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
public class SessionExpiredException extends BaseException {

    private static final String ERROR_CODE = "401002";

    public SessionExpiredException(String errorMessage) {
        super(ERROR_CODE, errorMessage);
    }

    public SessionExpiredException(String errorMessage, Throwable cause) {
        super(ERROR_CODE, errorMessage, cause);
    }

    /**
     * 创建默认的会话过期异常
     */
    public static SessionExpiredException expired() {
        return new SessionExpiredException("会话已过期，请重新登录");
    }
}
