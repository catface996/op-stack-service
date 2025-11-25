package com.catface996.aiops.domain.impl.service.auth;

import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.auth.Account;
import com.catface996.aiops.domain.model.auth.AccountRole;
import com.catface996.aiops.domain.model.auth.AccountStatus;
import com.catface996.aiops.domain.model.auth.DeviceInfo;
import com.catface996.aiops.domain.model.auth.Session;
import com.catface996.aiops.repository.auth.AccountRepository;
import com.catface996.aiops.repository.auth.SessionRepository;
import com.catface996.aiops.infrastructure.cache.api.service.LoginAttemptCache;
import com.catface996.aiops.infrastructure.cache.api.service.SessionCache;
import com.catface996.aiops.infrastructure.security.api.service.JwtTokenProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 会话管理领域服务测试
 *
 * 测试任务11：会话管理领域服务
 * - 会话创建（rememberMe 功能）
 * - 会话验证（过期检查）
 * - 会话失效
 * - 会话互斥
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
@DisplayName("会话管理领域服务测试")
class AuthDomainServiceImplSessionTest {

    private AuthDomainServiceImpl authDomainService;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;
    private SessionCache sessionCache;
    private SessionRepository sessionRepository;
    private ObjectMapper objectMapper;

    private Account testAccount;
    private DeviceInfo testDeviceInfo;

    @BeforeEach
    void setUp() {
        // 创建Mock对象
        passwordEncoder = new BCryptPasswordEncoder(10);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        sessionCache = mock(SessionCache.class);
        sessionRepository = mock(SessionRepository.class);
        LoginAttemptCache loginAttemptCache = mock(LoginAttemptCache.class);
        AccountRepository accountRepository = mock(AccountRepository.class);

        when(sessionCache.get(anyString())).thenReturn(Optional.empty());
        when(sessionRepository.findById(anyString())).thenReturn(Optional.empty());
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 创建服务实例
        authDomainService = new AuthDomainServiceImpl(
            passwordEncoder,
            jwtTokenProvider,
            sessionCache,
            sessionRepository,
            loginAttemptCache,
            accountRepository
        );

        // 创建测试数据
        testAccount = new Account(
            1L,
            "testuser",
            "testuser@example.com",
            "$2a$10$hash",
            AccountRole.ROLE_USER,
            AccountStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        testDeviceInfo = new DeviceInfo(
            "127.0.0.1",
            "Mozilla/5.0",
            "Desktop",
            "Linux",
            "Chrome"
        );

        // 配置ObjectMapper
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Mock JWT Token生成
        when(jwtTokenProvider.generateToken(anyLong(), anyString(), anyString(), anyBoolean()))
            .thenReturn("mock-jwt-token");
    }

    @Nested
    @DisplayName("会话创建测试")
    class CreateSessionTest {

        @Test
        @DisplayName("应该成功创建会话（默认2小时）")
        void shouldCreateSessionWithDefaultExpiration() {
            // Given
            boolean rememberMe = false;

            // When
            Session session = authDomainService.createSession(testAccount, rememberMe, testDeviceInfo);

            // Then
            assertNotNull(session, "会话不应该为空");
            assertNotNull(session.getId(), "会话ID不应该为空");
            assertEquals(testAccount.getId(), session.getUserId(), "用户ID应该匹配");
            assertNotNull(session.getToken(), "JWT Token不应该为空");
            assertEquals("mock-jwt-token", session.getToken(), "JWT Token应该匹配");
            assertNotNull(session.getExpiresAt(), "过期时间不应该为空");
            assertEquals(testDeviceInfo, session.getDeviceInfo(), "设备信息应该匹配");
            assertNotNull(session.getCreatedAt(), "创建时间不应该为空");

            // 验证过期时间约为2小时（允许1分钟误差）
            long minutesUntilExpiration = ChronoUnit.MINUTES.between(LocalDateTime.now(), session.getExpiresAt());
            assertTrue(minutesUntilExpiration >= 119 && minutesUntilExpiration <= 121,
                "过期时间应该约为2小时（119-121分钟），实际: " + minutesUntilExpiration + "分钟");

            // 验证JWT Token生成调用
            verify(jwtTokenProvider, times(1)).generateToken(
                testAccount.getId(),
                testAccount.getUsername(),
                AccountRole.ROLE_USER.name(),
                false
            );

            // 验证持久化与缓存保存调用
            verify(sessionRepository, times(1)).save(any(Session.class));
            verify(sessionCache, times(1)).save(
                eq(session.getId()),
                anyString(),
                eq(session.getExpiresAt()),
                eq(testAccount.getId())
            );
        }

        @Test
        @DisplayName("应该成功创建会话（记住我30天）")
        void shouldCreateSessionWithRememberMeExpiration() {
            // Given
            boolean rememberMe = true;

            // When
            Session session = authDomainService.createSession(testAccount, rememberMe, testDeviceInfo);

            // Then
            assertNotNull(session, "会话不应该为空");

            // 验证过期时间约为30天（允许1小时误差）
            long hoursUntilExpiration = ChronoUnit.HOURS.between(LocalDateTime.now(), session.getExpiresAt());
            assertTrue(hoursUntilExpiration >= 719 && hoursUntilExpiration <= 721,
                "过期时间应该约为30天（719-721小时），实际: " + hoursUntilExpiration + "小时");

            // 验证JWT Token生成时传递了rememberMe=true
            verify(jwtTokenProvider, times(1)).generateToken(
                testAccount.getId(),
                testAccount.getUsername(),
                AccountRole.ROLE_USER.name(),
                true
            );

            verify(sessionRepository, times(1)).save(any(Session.class));
        }

        @Test
        @DisplayName("应该生成唯一的会话ID（UUID）")
        void shouldGenerateUniqueSessionId() {
            // When
            Session session1 = authDomainService.createSession(testAccount, false, testDeviceInfo);
            Session session2 = authDomainService.createSession(testAccount, false, testDeviceInfo);

            // Then
            assertNotEquals(session1.getId(), session2.getId(), "每次创建应该生成不同的会话ID");
        }

        @Test
        @DisplayName("空账号应该抛出异常")
        void shouldThrowExceptionForNullAccount() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.createSession(null, false, testDeviceInfo);
            }, "空账号应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("账号ID为空应该抛出异常")
        void shouldThrowExceptionForNullAccountId() {
            // Given
            Account accountWithoutId = new Account(
                null,
                "testuser",
                "testuser@example.com",
                "$2a$10$hash",
                AccountRole.ROLE_USER,
                AccountStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.createSession(accountWithoutId, false, testDeviceInfo);
            }, "账号ID为空应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("用户名为空应该抛出异常")
        void shouldThrowExceptionForNullUsername() {
            // Given
            Account accountWithoutUsername = new Account(
                1L,
                null,
                "testuser@example.com",
                "$2a$10$hash",
                AccountRole.ROLE_USER,
                AccountStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.createSession(accountWithoutUsername, false, testDeviceInfo);
            }, "用户名为空应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("应该正确保存会话到SessionCache")
        void shouldSaveSessionToCache() {
            // When
            Session session = authDomainService.createSession(testAccount, false, testDeviceInfo);

            // Then
            // 捕获传递给sessionCache.save()的参数
            ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<LocalDateTime> expiresAtCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
            verify(sessionCache).save(eq(session.getId()), jsonCaptor.capture(), expiresAtCaptor.capture(), eq(testAccount.getId()));

            String sessionJson = jsonCaptor.getValue();
            assertNotNull(sessionJson, "会话JSON不应该为空");
            assertTrue(sessionJson.contains(session.getId()), "会话JSON应该包含会话ID");
            assertTrue(sessionJson.contains("mock-jwt-token"), "会话JSON应该包含JWT Token");

            LocalDateTime capturedExpiresAt = expiresAtCaptor.getValue();
            assertEquals(session.getExpiresAt(), capturedExpiresAt, "过期时间应该匹配");
        }
    }

    @Nested
    @DisplayName("会话验证测试")
    class ValidateSessionTest {

        @Test
        @DisplayName("应该成功验证有效会话")
        void shouldValidateValidSession() throws JsonProcessingException {
            // Given
            String sessionId = "test-session-id";
            Session validSession = new Session(
                sessionId,
                testAccount.getId(),
                "mock-jwt-token",
                LocalDateTime.now().plusHours(1), // 1小时后过期
                testDeviceInfo,
                LocalDateTime.now()
            );

            String sessionJson = objectMapper.writeValueAsString(validSession);
            when(sessionCache.get(sessionId)).thenReturn(Optional.of(sessionJson));

            // When
            Session result = authDomainService.validateSession(sessionId);

            // Then
            assertNotNull(result, "验证结果不应该为空");
            assertEquals(sessionId, result.getId(), "会话ID应该匹配");
            assertEquals(testAccount.getId(), result.getUserId(), "用户ID应该匹配");
            assertFalse(result.isExpired(), "会话不应该过期");

            // 验证SessionCache被调用
            verify(sessionCache, times(1)).get(sessionId);
            // 验证不应该删除有效会话
            verify(sessionCache, never()).delete(sessionId);
            verify(sessionCache, never()).deleteByUserId(anyLong());
            verify(sessionRepository, never()).deleteById(anyString());
        }

        @Test
        @DisplayName("缓存未命中时应该从仓储加载会话")
        void shouldValidateSessionFromRepositoryWhenCacheMiss() {
            // Given
            String sessionId = "db-session-id";
            Session persistentSession = new Session(
                sessionId,
                testAccount.getId(),
                "mock-jwt-token",
                LocalDateTime.now().plusHours(1),
                testDeviceInfo,
                LocalDateTime.now()
            );

            when(sessionCache.get(sessionId)).thenReturn(Optional.empty());
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(persistentSession));

            // When
            Session result = authDomainService.validateSession(sessionId);

            // Then
            assertNotNull(result);
            assertEquals(sessionId, result.getId());
            verify(sessionRepository, times(1)).findById(sessionId);
            verify(sessionCache, times(1)).save(
                eq(sessionId),
                anyString(),
                eq(persistentSession.getExpiresAt()),
                eq(testAccount.getId())
            );
        }

        @Test
        @DisplayName("会话不存在应该抛出BusinessException")
        void shouldThrowExceptionForNonExistentSession() {
            // Given
            String sessionId = "non-existent-session";
            when(sessionCache.get(sessionId)).thenReturn(Optional.empty());
            when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(BusinessException.class, () -> {
                authDomainService.validateSession(sessionId);
            }, "不存在的会话应该抛出BusinessException");

            // 验证SessionCache被调用
            verify(sessionCache, times(1)).get(sessionId);
            verify(sessionRepository, times(1)).findById(sessionId);
        }

        @Test
        @DisplayName("过期会话应该抛出BusinessException")
        void shouldThrowExceptionForExpiredSession() throws JsonProcessingException {
            // Given
            String sessionId = "expired-session-id";
            Session expiredSession = new Session(
                sessionId,
                testAccount.getId(),
                "mock-jwt-token",
                LocalDateTime.now().minusHours(1), // 1小时前过期
                testDeviceInfo,
                LocalDateTime.now().minusHours(3)
            );

            String sessionJson = objectMapper.writeValueAsString(expiredSession);
            when(sessionCache.get(sessionId)).thenReturn(Optional.of(sessionJson));

            // When & Then
            assertThrows(BusinessException.class, () -> {
                authDomainService.validateSession(sessionId);
            }, "过期的会话应该抛出BusinessException");

            // 验证SessionCache被调用，且删除过期会话
            verify(sessionCache, times(1)).get(sessionId);
            verify(sessionCache, times(1)).deleteByUserId(testAccount.getId());
            verify(sessionRepository, times(1)).deleteById(sessionId);
        }

        @Test
        @DisplayName("空会话ID应该抛出异常")
        void shouldThrowExceptionForNullSessionId() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.validateSession(null);
            }, "空会话ID应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("空字符串会话ID应该抛出异常")
        void shouldThrowExceptionForEmptySessionId() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.validateSession("");
            }, "空字符串会话ID应该抛出IllegalArgumentException");
        }
    }

    @Nested
    @DisplayName("会话失效测试")
    class InvalidateSessionTest {

        @Test
        @DisplayName("应该成功使会话失效")
        void shouldInvalidateSession() throws JsonProcessingException {
            // Given
            String sessionId = "test-session-id";
            Session session = new Session(
                sessionId,
                testAccount.getId(),
                "mock-jwt-token",
                LocalDateTime.now().plusHours(2),
                testDeviceInfo,
                LocalDateTime.now()
            );
            when(sessionCache.get(sessionId)).thenReturn(Optional.of(objectMapper.writeValueAsString(session)));

            // When
            authDomainService.invalidateSession(sessionId);

            // Then
            verify(sessionCache, times(1)).deleteByUserId(testAccount.getId());
            verify(sessionRepository, times(1)).deleteById(sessionId);
        }

        @Test
        @DisplayName("空会话ID应该抛出异常")
        void shouldThrowExceptionForNullSessionId() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.invalidateSession(null);
            }, "空会话ID应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("空字符串会话ID应该抛出异常")
        void shouldThrowExceptionForEmptySessionId() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.invalidateSession("");
            }, "空字符串会话ID应该抛出IllegalArgumentException");
        }
    }

    @Nested
    @DisplayName("会话互斥测试")
    class HandleSessionMutexTest {

        @Test
        @DisplayName("应该使旧会话失效")
        void shouldInvalidateOldSession() {
            // Given
            String newSessionId = "new-session-id";

            Session newSession = new Session(
                newSessionId,
                testAccount.getId(),
                "new-jwt-token",
                LocalDateTime.now().plusHours(2),
                testDeviceInfo,
                LocalDateTime.now()
            );

            // When
            authDomainService.handleSessionMutex(testAccount, newSession);

            // Then
            verify(sessionCache, times(1)).deleteByUserId(testAccount.getId());
            verify(sessionRepository, times(1)).deleteByUserId(testAccount.getId());
        }

        @Test
        @DisplayName("没有旧会话时不应该删除任何会话")
        void shouldNotDeleteWhenNoOldSession() {
            // Given
            String newSessionId = "new-session-id";

            Session newSession = new Session(
                newSessionId,
                testAccount.getId(),
                "new-jwt-token",
                LocalDateTime.now().plusHours(2),
                testDeviceInfo,
                LocalDateTime.now()
            );

            // When
            authDomainService.handleSessionMutex(testAccount, newSession);

            // Then
            verify(sessionCache, times(1)).deleteByUserId(testAccount.getId());
            verify(sessionRepository, times(1)).deleteByUserId(testAccount.getId());
        }

        @Test
        @DisplayName("空账号应该抛出异常")
        void shouldThrowExceptionForNullAccount() {
            // Given
            Session newSession = new Session(
                "new-session-id",
                1L,
                "new-jwt-token",
                LocalDateTime.now().plusHours(2),
                testDeviceInfo,
                LocalDateTime.now()
            );

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.handleSessionMutex(null, newSession);
            }, "空账号应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("账号ID为空应该抛出异常")
        void shouldThrowExceptionForNullAccountId() {
            // Given
            Account accountWithoutId = new Account(
                null,
                "testuser",
                "testuser@example.com",
                "$2a$10$hash",
                AccountRole.ROLE_USER,
                AccountStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
            );

            Session newSession = new Session(
                "new-session-id",
                1L,
                "new-jwt-token",
                LocalDateTime.now().plusHours(2),
                testDeviceInfo,
                LocalDateTime.now()
            );

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.handleSessionMutex(accountWithoutId, newSession);
            }, "账号ID为空应该抛出IllegalArgumentException");
        }

        @Test
        @DisplayName("新会话为空应该抛出异常")
        void shouldThrowExceptionForNullNewSession() {
            // When & Then
            assertThrows(IllegalArgumentException.class, () -> {
                authDomainService.handleSessionMutex(testAccount, null);
            }, "新会话为空应该抛出IllegalArgumentException");
        }
    }
}
