package com.catface996.aiops.domain.model.auth;

import java.time.LocalDateTime;

/**
 * 账号锁定信息值对象
 * 
 * 用于返回账号锁定的详细信息
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
public class AccountLockInfo {
    
    /**
     * 是否被锁定
     */
    private final boolean locked;
    
    /**
     * 锁定原因
     */
    private final String reason;
    
    /**
     * 锁定时间
     */
    private final LocalDateTime lockedAt;
    
    /**
     * 解锁时间
     */
    private final LocalDateTime unlockAt;
    
    /**
     * 剩余锁定时间（分钟）
     */
    private final long remainingMinutes;
    
    /**
     * 失败尝试次数
     */
    private final int failedAttempts;
    
    // 构造函数
    public AccountLockInfo(boolean locked, String reason, LocalDateTime lockedAt, 
                           LocalDateTime unlockAt, long remainingMinutes, int failedAttempts) {
        this.locked = locked;
        this.reason = reason;
        this.lockedAt = lockedAt;
        this.unlockAt = unlockAt;
        this.remainingMinutes = remainingMinutes;
        this.failedAttempts = failedAttempts;
    }
    
    /**
     * 创建一个未锁定的账号锁定信息
     * 
     * @return unlocked account lock info
     */
    public static AccountLockInfo notLocked() {
        return new AccountLockInfo(false, null, null, null, 0, 0);
    }
    
    /**
     * 创建一个已锁定的账号锁定信息
     * 
     * @param reason lock reason
     * @param lockedAt lock time
     * @param unlockAt unlock time
     * @param failedAttempts failed attempts count
     * @return locked account lock info
     */
    public static AccountLockInfo locked(String reason, LocalDateTime lockedAt, 
                                         LocalDateTime unlockAt, int failedAttempts) {
        long remainingMinutes = calculateRemainingMinutes(unlockAt);
        return new AccountLockInfo(true, reason, lockedAt, unlockAt, remainingMinutes, failedAttempts);
    }
    
    /**
     * 计算剩余锁定时间（分钟）
     * 
     * @param unlockAt unlock time
     * @return remaining minutes
     */
    private static long calculateRemainingMinutes(LocalDateTime unlockAt) {
        if (unlockAt == null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(unlockAt)) {
            return 0;
        }
        return java.time.Duration.between(now, unlockAt).toMinutes();
    }
    
    /**
     * 获取锁定提示消息
     * 
     * @return lock message
     */
    public String getLockMessage() {
        if (!locked) {
            return null;
        }
        return String.format("账号已锁定，请在%d分钟后重试", remainingMinutes);
    }
    
    // Getters
    
    public boolean isLocked() {
        return locked;
    }
    
    public String getReason() {
        return reason;
    }
    
    public LocalDateTime getLockedAt() {
        return lockedAt;
    }
    
    public LocalDateTime getUnlockAt() {
        return unlockAt;
    }
    
    public long getRemainingMinutes() {
        return remainingMinutes;
    }
    
    public int getFailedAttempts() {
        return failedAttempts;
    }
    
    @Override
    public String toString() {
        return "AccountLockInfo{" +
                "locked=" + locked +
                ", reason='" + reason + '\'' +
                ", lockedAt=" + lockedAt +
                ", unlockAt=" + unlockAt +
                ", remainingMinutes=" + remainingMinutes +
                ", failedAttempts=" + failedAttempts +
                '}';
    }
}
