package com.catface996.aiops.domain.api.exception.auth;

import com.catface996.aiops.common.exception.BaseException;
import lombok.Getter;

import java.util.List;

/**
 * 密码无效异常
 * 当密码不符合强度要求时抛出此异常
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
@Getter
public class InvalidPasswordException extends BaseException {

    private static final String ERROR_CODE = "400002";

    /**
     * 密码验证错误详情列表
     */
    private final List<String> validationErrors;

    public InvalidPasswordException(String errorMessage, List<String> validationErrors) {
        super(ERROR_CODE, errorMessage);
        this.validationErrors = validationErrors;
    }

    public InvalidPasswordException(String errorMessage, List<String> validationErrors, Throwable cause) {
        super(ERROR_CODE, errorMessage, cause);
        this.validationErrors = validationErrors;
    }

    /**
     * 创建密码强度不足异常
     *
     * @param validationErrors 验证错误列表
     */
    public static InvalidPasswordException weakPassword(List<String> validationErrors) {
        return new InvalidPasswordException("密码不符合强度要求", validationErrors);
    }
}
