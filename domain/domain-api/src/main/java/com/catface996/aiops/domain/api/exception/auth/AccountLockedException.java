package com.catface996.aiops.domain.api.exception.auth;

import com.catface996.aiops.common.exception.BaseException;
import lombok.Getter;

/**
 * 账号锁定异常
 * 当账号因登录失败次数过多被锁定时抛出此异常
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
@Getter
public class AccountLockedException extends BaseException {

    private static final String ERROR_CODE = "423001";

    /**
     * 剩余锁定时间（分钟）
     */
    private final int remainingMinutes;

    public AccountLockedException(String errorMessage, int remainingMinutes) {
        super(ERROR_CODE, errorMessage);
        this.remainingMinutes = remainingMinutes;
    }

    public AccountLockedException(String errorMessage, int remainingMinutes, Throwable cause) {
        super(ERROR_CODE, errorMessage, cause);
        this.remainingMinutes = remainingMinutes;
    }

    /**
     * 创建账号锁定异常
     *
     * @param remainingMinutes 剩余锁定时间（分钟）
     */
    public static AccountLockedException locked(int remainingMinutes) {
        String message = String.format("账号已锁定，请在%d分钟后重试", remainingMinutes);
        return new AccountLockedException(message, remainingMinutes);
    }
}
