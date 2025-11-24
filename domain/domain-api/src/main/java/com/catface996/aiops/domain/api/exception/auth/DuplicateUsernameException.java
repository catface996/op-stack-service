package com.catface996.aiops.domain.api.exception.auth;

import com.catface996.aiops.common.exception.BaseException;

/**
 * 用户名重复异常
 * 当注册时用户名已存在时抛出此异常
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
public class DuplicateUsernameException extends BaseException {

    private static final String ERROR_CODE = "409001";

    public DuplicateUsernameException(String errorMessage) {
        super(ERROR_CODE, errorMessage);
    }

    public DuplicateUsernameException(String errorMessage, Throwable cause) {
        super(ERROR_CODE, errorMessage, cause);
    }

    /**
     * 创建用户名重复异常
     *
     * @param username 重复的用户名
     */
    public static DuplicateUsernameException duplicate(String username) {
        return new DuplicateUsernameException("用户名已存在: " + username);
    }
}
