package com.catface996.aiops.domain.api.exception.auth;

import com.catface996.aiops.common.exception.BaseException;

/**
 * 账号未找到异常
 * 当指定的账号不存在时抛出此异常
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
public class AccountNotFoundException extends BaseException {

    private static final String ERROR_CODE = "404001";

    public AccountNotFoundException(String errorMessage) {
        super(ERROR_CODE, errorMessage);
    }

    public AccountNotFoundException(String errorMessage, Throwable cause) {
        super(ERROR_CODE, errorMessage, cause);
    }

    /**
     * 创建账号未找到异常
     *
     * @param identifier 账号标识（用户名或邮箱）
     */
    public static AccountNotFoundException notFound(String identifier) {
        return new AccountNotFoundException("账号不存在: " + identifier);
    }

    /**
     * 创建账号未找到异常（通过ID）
     *
     * @param accountId 账号ID
     */
    public static AccountNotFoundException notFoundById(Long accountId) {
        return new AccountNotFoundException("账号不存在，ID: " + accountId);
    }
}
