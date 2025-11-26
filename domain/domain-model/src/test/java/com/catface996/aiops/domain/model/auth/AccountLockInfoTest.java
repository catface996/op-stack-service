package com.catface996.aiops.domain.model.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AccountLockInfo 单元测试
 *
 * 测试账号锁定信息值对象的所有方法
 *
 * @author AI Assistant
 * @since 2025-01-26
 */
@DisplayName("账号锁定信息测试")
class AccountLockInfoTest {

    @Test
    @DisplayName("应该创建未锁定的账号锁定信息")
    void should_CreateNotLockedInfo_When_UsingNotLockedFactory() {
        // When
        AccountLockInfo lockInfo = AccountLockInfo.notLocked();

        // Then
        assertFalse(lockInfo.isLocked(), "账号不应该被锁定");
        assertNull(lockInfo.getReason(), "锁定原因应该为空");
        assertNull(lockInfo.getLockedAt(), "锁定时间应该为空");
        assertNull(lockInfo.getUnlockAt(), "解锁时间应该为空");
        assertEquals(0, lockInfo.getRemainingMinutes(), "剩余锁定时间应该为0");
        assertEquals(0, lockInfo.getFailedAttempts(), "失败尝试次数应该为0");
        assertNull(lockInfo.getLockMessage(), "未锁定时锁定消息应该为空");
    }

    @Test
    @DisplayName("应该创建已锁定的账号锁定信息")
    void should_CreateLockedInfo_When_UsingLockedFactory() {
        // Given
        String reason = "连续登录失败5次";
        LocalDateTime lockedAt = LocalDateTime.now();
        LocalDateTime unlockAt = LocalDateTime.now().plusMinutes(30);
        int failedAttempts = 5;

        // When
        AccountLockInfo lockInfo = AccountLockInfo.locked(reason, lockedAt, unlockAt, failedAttempts);

        // Then
        assertTrue(lockInfo.isLocked(), "账号应该被锁定");
        assertEquals(reason, lockInfo.getReason(), "锁定原因应该匹配");
        assertEquals(lockedAt, lockInfo.getLockedAt(), "锁定时间应该匹配");
        assertEquals(unlockAt, lockInfo.getUnlockAt(), "解锁时间应该匹配");
        assertTrue(lockInfo.getRemainingMinutes() > 0, "剩余锁定时间应该大于0");
        assertEquals(failedAttempts, lockInfo.getFailedAttempts(), "失败尝试次数应该匹配");
        assertNotNull(lockInfo.getLockMessage(), "锁定消息不应该为空");
        assertTrue(lockInfo.getLockMessage().contains("账号已锁定"), "锁定消息应该包含锁定提示");
    }

    @Test
    @DisplayName("应该使用构造函数创建锁定信息")
    void should_CreateLockInfo_When_UsingConstructor() {
        // Given
        boolean locked = true;
        String reason = "测试锁定";
        LocalDateTime lockedAt = LocalDateTime.now();
        LocalDateTime unlockAt = LocalDateTime.now().plusMinutes(15);
        long remainingMinutes = 15;
        int failedAttempts = 3;

        // When
        AccountLockInfo lockInfo = new AccountLockInfo(locked, reason, lockedAt, unlockAt, remainingMinutes, failedAttempts);

        // Then
        assertTrue(lockInfo.isLocked());
        assertEquals(reason, lockInfo.getReason());
        assertEquals(lockedAt, lockInfo.getLockedAt());
        assertEquals(unlockAt, lockInfo.getUnlockAt());
        assertEquals(remainingMinutes, lockInfo.getRemainingMinutes());
        assertEquals(failedAttempts, lockInfo.getFailedAttempts());
    }

    @Test
    @DisplayName("应该返回0当解锁时间为null")
    void should_ReturnZero_When_UnlockAtIsNull() {
        // When
        AccountLockInfo lockInfo = AccountLockInfo.locked("测试", LocalDateTime.now(), null, 5);

        // Then
        assertEquals(0, lockInfo.getRemainingMinutes(), "解锁时间为null时剩余分钟应该为0");
    }

    @Test
    @DisplayName("应该返回0当解锁时间已过")
    void should_ReturnZero_When_UnlockAtInPast() {
        // Given
        LocalDateTime lockedAt = LocalDateTime.now().minusHours(1);
        LocalDateTime unlockAt = LocalDateTime.now().minusMinutes(30); // 30分钟前已解锁

        // When
        AccountLockInfo lockInfo = AccountLockInfo.locked("测试", lockedAt, unlockAt, 5);

        // Then
        assertEquals(0, lockInfo.getRemainingMinutes(), "解锁时间已过时剩余分钟应该为0");
    }

    @Test
    @DisplayName("应该正确计算剩余锁定时间")
    void should_CalculateCorrectRemainingMinutes_When_UnlockAtInFuture() {
        // Given
        LocalDateTime lockedAt = LocalDateTime.now();
        LocalDateTime unlockAt = LocalDateTime.now().plusMinutes(30);

        // When
        AccountLockInfo lockInfo = AccountLockInfo.locked("测试", lockedAt, unlockAt, 5);

        // Then
        // 允许1分钟的误差
        assertTrue(lockInfo.getRemainingMinutes() >= 29 && lockInfo.getRemainingMinutes() <= 30,
                "剩余锁定时间应该约为30分钟，实际: " + lockInfo.getRemainingMinutes());
    }

    @Test
    @DisplayName("应该正确生成锁定消息")
    void should_GenerateCorrectLockMessage_When_Locked() {
        // Given
        LocalDateTime lockedAt = LocalDateTime.now();
        LocalDateTime unlockAt = LocalDateTime.now().plusMinutes(15);

        // When
        AccountLockInfo lockInfo = AccountLockInfo.locked("测试", lockedAt, unlockAt, 5);
        String message = lockInfo.getLockMessage();

        // Then
        assertNotNull(message);
        assertTrue(message.contains("账号已锁定"));
        assertTrue(message.contains("分钟后重试"));
    }

    @Test
    @DisplayName("应该正确生成toString")
    void should_GenerateCorrectString_When_CallingToString() {
        // Given
        AccountLockInfo lockInfo = AccountLockInfo.locked("测试锁定原因", LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), 5);

        // When
        String toString = lockInfo.toString();

        // Then
        assertTrue(toString.contains("locked=true"));
        assertTrue(toString.contains("测试锁定原因"));
        assertTrue(toString.contains("failedAttempts=5"));
    }
}
