package com.catface996.aiops.domain.api.exception.auth;

import com.catface996.aiops.common.exception.BaseException;

/**
 * Token无效异常
 * 当JWT Token无效或格式错误时抛出此异常
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
public class InvalidTokenException extends BaseException {

    private static final String ERROR_CODE = "401003";

    public InvalidTokenException(String errorMessage) {
        super(ERROR_CODE, errorMessage);
    }

    public InvalidTokenException(String errorMessage, Throwable cause) {
        super(ERROR_CODE, errorMessage, cause);
    }

    /**
     * 创建默认的Token无效异常
     */
    public static InvalidTokenException invalid() {
        return new InvalidTokenException("Token无效");
    }
}
