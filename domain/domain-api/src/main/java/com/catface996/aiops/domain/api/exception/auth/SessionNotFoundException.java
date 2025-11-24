package com.catface996.aiops.domain.api.exception.auth;

import com.catface996.aiops.common.exception.BaseException;

/**
 * 会话未找到异常
 * 当指定的会话不存在时抛出此异常
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
public class SessionNotFoundException extends BaseException {

    private static final String ERROR_CODE = "404002";

    public SessionNotFoundException(String errorMessage) {
        super(ERROR_CODE, errorMessage);
    }

    public SessionNotFoundException(String errorMessage, Throwable cause) {
        super(ERROR_CODE, errorMessage, cause);
    }

    /**
     * 创建会话未找到异常
     *
     * @param sessionId 会话ID
     */
    public static SessionNotFoundException notFound(String sessionId) {
        return new SessionNotFoundException("会话不存在: " + sessionId);
    }
}
