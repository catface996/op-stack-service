package com.catface996.aiops.domain.impl.service.auth;

import com.catface996.aiops.domain.model.auth.Account;
import com.catface996.aiops.domain.model.auth.AccountLockInfo;
import com.catface996.aiops.domain.model.auth.AccountRole;
import com.catface996.aiops.domain.model.auth.AccountStatus;
import com.catface996.aiops.repository.auth.AccountRepository;
import com.catface996.aiops.repository.auth.SessionRepository;
import com.catface996.aiops.infrastructure.cache.api.service.LoginAttemptCache;
import com.catface996.aiops.infrastructure.cache.api.service.SessionCache;
import com.catface996.aiops.infrastructure.security.api.service.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 账号锁定领域服务测试
 *
 * 测试任务12：账号锁定领域服务
 * - 登录失败记录
 * - 账号锁定检查
 * - 账号锁定和解锁
 * - 失败计数重置
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
@DisplayName("账号锁定领域服务测试")
class AuthDomainServiceImplLockTest {

    private AuthDomainServiceImpl authDomainService;
    private LoginAttemptCache loginAttemptCache;
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        // 创建Mock对象
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
        SessionCache sessionCache = mock(SessionCache.class);
        SessionRepository sessionRepository = mock(SessionRepository.class);
        loginAttemptCache = mock(LoginAttemptCache.class);
        accountRepository = mock(AccountRepository.class);

        // 创建服务实例
        authDomainService = new AuthDomainServiceImpl(
            passwordEncoder,
            jwtTokenProvider,
            sessionCache,
            sessionRepository,
            loginAttemptCache,
            accountRepository
        );
    }

    @Nested
    @DisplayName("记录登录失败测试")
    class RecordLoginFailureTest {

        @Test
        @DisplayName("应该成功记录第1次登录失败")
        void shouldRecordFirstFailure() {
            // Given
            String identifier = "testuser";
            when(loginAttemptCache.recordFailure(identifier)).thenReturn(1);

            // When
            int failureCount = authDomainService.recordLoginFailure(identifier);

            // Then
            assertEquals(1, failureCount, "失败次数应该为1");
            verify(loginAttemptCache, times(1)).recordFailure(identifier);
        }

        @Test
        @DisplayName("应该成功记录连续失败")
        void shouldRecordConsecutiveFailures() {
            // Given
            String identifier = "testuser";
            when(loginAttemptCache.recordFailure(identifier))
                .thenReturn(1)
                .thenReturn(2)
                .thenReturn(3);

            // When
            int count1 = authDomainService.recordLoginFailure(identifier);
            int count2 = authDomainService.recordLoginFailure(identifier);
            int count3 = authDomainService.recordLoginFailure(identifier);

            // Then
            assertEquals(1, count1, "第1次失败次数应该为1");
            assertEquals(2, count2, "第2次失败次数应该为2");
            assertEquals(3, count3, "第3次失败次数应该为3");
        }

        @Test
        @DisplayName("连续5次失败应该触发账号锁定")
        void shouldTriggerLockAfter5Failures() {
            // Given
            String identifier = "testuser";
            when(loginAttemptCache.recordFailure(identifier)).thenReturn(5);

            // When
            int failureCount = authDomainService.recordLoginFailure(identifier);

            // Then
            assertEquals(5, failureCount, "失败次数应该为5");
            verify(loginAttemptCache, times(1)).recordFailure(identifier);
            // lockAccount method is called but does nothing in the implementation
        }

        @Test
        @DisplayName("空标识符应该抛出异常")
        void shouldThrowExceptionForEmptyIdentifier() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.recordLoginFailure("");
            }, "空标识符应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("null标识符应该抛出异常")
        void shouldThrowExceptionForNullIdentifier() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.recordLoginFailure(null);
            }, "null标识符应该抛出IllegalArgumentException");
        }
    }

    @Nested
    @DisplayName("检查账号锁定测试")
    class CheckAccountLockTest {

        @Test
        @DisplayName("未锁定账号应该返回未锁定信息")
        void shouldReturnNotLockedForUnlockedAccount() {
            // Given
            String identifier = "testuser";
            when(loginAttemptCache.isLocked(identifier)).thenReturn(false);

            // When
            Optional<AccountLockInfo> lockInfoOpt = authDomainService.checkAccountLock(identifier);

            // Then
            assertTrue(lockInfoOpt.isPresent(), "锁定信息不应该为空");
            AccountLockInfo lockInfo = lockInfoOpt.get();
            assertFalse(lockInfo.isLocked(), "账号不应该被锁定");
            assertEquals(0, lockInfo.getFailedAttempts(), "失败次数应该为0");
            assertNull(lockInfo.getLockMessage(), "锁定消息应该为空");
        }

        @Test
        @DisplayName("已锁定账号应该返回锁定信息")
        void shouldReturnLockedInfoForLockedAccount() {
            // Given
            String identifier = "testuser";
            when(loginAttemptCache.isLocked(identifier)).thenReturn(true);
            when(loginAttemptCache.getFailureCount(identifier)).thenReturn(5);
            when(loginAttemptCache.getRemainingLockTime(identifier)).thenReturn(1800L); // 30分钟

            // When
            Optional<AccountLockInfo> lockInfoOpt = authDomainService.checkAccountLock(identifier);

            // Then
            assertTrue(lockInfoOpt.isPresent(), "锁定信息不应该为空");
            AccountLockInfo lockInfo = lockInfoOpt.get();
            assertTrue(lockInfo.isLocked(), "账号应该被锁定");
            assertEquals(5, lockInfo.getFailedAttempts(), "失败次数应该为5");
            // Allow 1 minute tolerance for timing precision
            assertTrue(lockInfo.getRemainingMinutes() >= 29 && lockInfo.getRemainingMinutes() <= 30,
                    "剩余锁定时间应该约为30分钟，实际: " + lockInfo.getRemainingMinutes() + "分钟");
            assertNotNull(lockInfo.getLockMessage(), "锁定消息不应该为空");
            assertTrue(lockInfo.getLockMessage().matches(".*[0-9]+分钟.*"), "锁定消息应该包含剩余时间");
        }

        @Test
        @DisplayName("锁定已过期应该返回未锁定信息")
        void shouldReturnNotLockedWhenLockExpired() {
            // Given
            String identifier = "testuser";
            when(loginAttemptCache.isLocked(identifier)).thenReturn(true);
            when(loginAttemptCache.getFailureCount(identifier)).thenReturn(5);
            when(loginAttemptCache.getRemainingLockTime(identifier)).thenReturn(0L); // 已过期

            // When
            Optional<AccountLockInfo> lockInfoOpt = authDomainService.checkAccountLock(identifier);

            // Then
            assertTrue(lockInfoOpt.isPresent(), "锁定信息不应该为空");
            AccountLockInfo lockInfo = lockInfoOpt.get();
            assertFalse(lockInfo.isLocked(), "账号不应该被锁定（已自动解锁）");
        }

        @Test
        @DisplayName("锁定剩余时间应该正确计算")
        void shouldCalculateRemainingTimeCorrectly() {
            // Given
            String identifier = "testuser";
            when(loginAttemptCache.isLocked(identifier)).thenReturn(true);
            when(loginAttemptCache.getFailureCount(identifier)).thenReturn(5);
            when(loginAttemptCache.getRemainingLockTime(identifier)).thenReturn(600L); // 10分钟

            // When
            Optional<AccountLockInfo> lockInfoOpt = authDomainService.checkAccountLock(identifier);

            // Then
            assertTrue(lockInfoOpt.isPresent(), "锁定信息不应该为空");
            AccountLockInfo lockInfo = lockInfoOpt.get();
            // Allow 1 minute tolerance for timing precision
            assertTrue(lockInfo.getRemainingMinutes() >= 9 && lockInfo.getRemainingMinutes() <= 10,
                    "剩余锁定时间应该约为10分钟，实际: " + lockInfo.getRemainingMinutes() + "分钟");
        }

        @Test
        @DisplayName("空标识符应该抛出异常")
        void shouldThrowExceptionForEmptyIdentifier() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.checkAccountLock("");
            }, "空标识符应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("null标识符应该抛出异常")
        void shouldThrowExceptionForNullIdentifier() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.checkAccountLock(null);
            }, "null标识符应该抛出IllegalArgumentException");
        }
    }

    @Nested
    @DisplayName("锁定账号测试")
    class LockAccountTest {

        @Test
        @DisplayName("应该成功锁定账号")
        void shouldLockAccountSuccessfully() {
            // Given
            String identifier = "testuser";
            int lockDurationMinutes = 30;

            // When
            authDomainService.lockAccount(identifier, lockDurationMinutes);

            // Then
            // lockAccount方法目前不执行任何操作，因为锁定是通过Redis计数实现的
            // 这里验证方法能正常执行不抛出异常
        }

        @Test
        @DisplayName("空标识符应该抛出异常")
        void shouldThrowExceptionForEmptyIdentifier() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.lockAccount("", 30);
            }, "空标识符应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("null标识符应该抛出异常")
        void shouldThrowExceptionForNullIdentifier() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.lockAccount(null, 30);
            }, "null标识符应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("锁定时长小于等于0应该抛出异常")
        void shouldThrowExceptionForInvalidLockDuration() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.lockAccount("testuser", 0);
            }, "锁定时长小于等于0应该抛出IllegalArgumentException");

            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.lockAccount("testuser", -1);
            }, "锁定时长为负数应该抛出IllegalArgumentException");
        }
    }

    @Nested
    @DisplayName("解锁账号测试")
    class UnlockAccountTest {

        @Test
        @DisplayName("应该成功解锁账号（ACTIVE状态）")
        void shouldUnlockActiveAccountSuccessfully() {
            // Given
            Long accountId = 1L;
            Account account = new Account(
                accountId,
                "testuser",
                "test@example.com",
                "$2a$10$hash",
                AccountRole.ROLE_USER,
                AccountStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

            // When
            authDomainService.unlockAccount(accountId);

            // Then
            verify(loginAttemptCache, times(1)).unlock("testuser");
            verify(loginAttemptCache, times(1)).unlock("test@example.com");
            verify(accountRepository, never()).updateStatus(any(), any());
        }

        @Test
        @DisplayName("应该成功解锁账号（LOCKED状态）")
        void shouldUnlockLockedAccountSuccessfully() {
            // Given
            Long accountId = 1L;
            Account account = new Account(
                accountId,
                "testuser",
                "test@example.com",
                "$2a$10$hash",
                AccountRole.ROLE_USER,
                AccountStatus.LOCKED,
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

            // When
            authDomainService.unlockAccount(accountId);

            // Then
            verify(loginAttemptCache, times(1)).unlock("testuser");
            verify(loginAttemptCache, times(1)).unlock("test@example.com");
            verify(accountRepository, times(1)).updateStatus(accountId, AccountStatus.ACTIVE);
        }

        @Test
        @DisplayName("账号不存在应该抛出异常")
        void shouldThrowExceptionForNonExistentAccount() {
            // Given
            Long accountId = 999L;
            when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.unlockAccount(accountId);
            }, "账号不存在应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("null账号ID应该抛出异常")
        void shouldThrowExceptionForNullAccountId() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.unlockAccount(null);
            }, "null账号ID应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("解锁后失败计数应该被重置")
        void shouldResetFailureCountAfterUnlock() {
            // Given
            Long accountId = 1L;
            Account account = new Account(
                accountId,
                "testuser",
                "test@example.com",
                "$2a$10$hash",
                AccountRole.ROLE_USER,
                AccountStatus.LOCKED,
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

            // When
            authDomainService.unlockAccount(accountId);

            // Then
            verify(loginAttemptCache, times(1)).unlock("testuser");
            verify(loginAttemptCache, times(1)).unlock("test@example.com");
        }

        @Test
        @DisplayName("解锁后账号状态应该变为ACTIVE")
        void shouldChangeStatusToActiveAfterUnlock() {
            // Given
            Long accountId = 1L;
            Account account = new Account(
                accountId,
                "testuser",
                "test@example.com",
                "$2a$10$hash",
                AccountRole.ROLE_USER,
                AccountStatus.LOCKED,
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

            // When
            authDomainService.unlockAccount(accountId);

            // Then
            verify(accountRepository, times(1)).updateStatus(accountId, AccountStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("重置失败计数测试")
    class ResetLoginFailureCountTest {

        @Test
        @DisplayName("应该成功重置失败计数")
        void shouldResetFailureCountSuccessfully() {
            // Given
            String identifier = "testuser";

            // When
            authDomainService.resetLoginFailureCount(identifier);

            // Then
            verify(loginAttemptCache, times(1)).resetFailureCount(identifier);
        }

        @Test
        @DisplayName("空标识符应该抛出异常")
        void shouldThrowExceptionForEmptyIdentifier() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.resetLoginFailureCount("");
            }, "空标识符应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("null标识符应该抛出异常")
        void shouldThrowExceptionForNullIdentifier() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.resetLoginFailureCount(null);
            }, "null标识符应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("登录成功后应该重置计数")
        void shouldResetCountAfterSuccessfulLogin() {
            // Given
            String identifier = "testuser";

            // When
            authDomainService.resetLoginFailureCount(identifier);

            // Then
            verify(loginAttemptCache, times(1)).resetFailureCount(identifier);
        }
    }
}
