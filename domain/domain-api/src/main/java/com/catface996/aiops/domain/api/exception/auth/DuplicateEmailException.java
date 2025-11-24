package com.catface996.aiops.domain.api.exception.auth;

import com.catface996.aiops.common.exception.BaseException;

/**
 * 邮箱重复异常
 * 当注册时邮箱已存在时抛出此异常
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
public class DuplicateEmailException extends BaseException {

    private static final String ERROR_CODE = "409002";

    public DuplicateEmailException(String errorMessage) {
        super(ERROR_CODE, errorMessage);
    }

    public DuplicateEmailException(String errorMessage, Throwable cause) {
        super(ERROR_CODE, errorMessage, cause);
    }

    /**
     * 创建邮箱重复异常
     *
     * @param email 重复的邮箱
     */
    public static DuplicateEmailException duplicate(String email) {
        return new DuplicateEmailException("邮箱已存在: " + email);
    }
}
