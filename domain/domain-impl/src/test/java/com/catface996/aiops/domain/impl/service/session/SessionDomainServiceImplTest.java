package com.catface996.aiops.domain.impl.service.session;

import com.catface996.aiops.common.enums.SessionErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.api.service.session.SessionDomainService;
import com.catface996.aiops.domain.model.auth.DeviceInfo;
import com.catface996.aiops.domain.model.auth.Session;
import com.catface996.aiops.domain.model.auth.SessionValidationResult;
import com.catface996.aiops.infrastructure.cache.api.service.SessionCacheService;
import com.catface996.aiops.repository.auth.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * SessionDomainServiceImpl 单元测试
 *
 * @author AI Assistant
 * @since 2025-01-28
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("会话领域服务测试")
class SessionDomainServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionCacheService sessionCacheService;

    private SessionDomainService sessionDomainService;

    private static final Long TEST_USER_ID = 12345L;
    private static final String TEST_SESSION_ID = "test-session-123";
    private static final String TEST_IP = "192.168.1.100";
    private static final int ABSOLUTE_TIMEOUT = 28800; // 8 hours
    private static final int IDLE_TIMEOUT = 1800; // 30 minutes

    @BeforeEach
    void setUp() {
        sessionDomainService = new SessionDomainServiceImpl(sessionRepository, sessionCacheService);
    }

    // ==================== createSession 测试 ====================

    @Test
    @DisplayName("创建会话 - 成功")
    void testCreateSession_Success() {
        // Given
        DeviceInfo deviceInfo = createTestDeviceInfo();
        when(sessionRepository.findAllByUserId(TEST_USER_ID)).thenReturn(new ArrayList<>());
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Session session = sessionDomainService.createSession(
                TEST_USER_ID, deviceInfo, ABSOLUTE_TIMEOUT, IDLE_TIMEOUT, false);

        // Then
        assertThat(session).isNotNull();
        assertThat(session.getId()).isNotNull();
        assertThat(session.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(session.getDeviceInfo()).isEqualTo(deviceInfo);
        assertThat(session.getAbsoluteTimeout()).isEqualTo(ABSOLUTE_TIMEOUT);
        assertThat(session.getIdleTimeout()).isEqualTo(IDLE_TIMEOUT);
        assertThat(session.isRememberMe()).isFalse();

        // Verify interactions
        verify(sessionRepository).save(any(Session.class));
        verify(sessionCacheService).cacheSession(any(Session.class), eq((long) ABSOLUTE_TIMEOUT));
        verify(sessionCacheService).addUserSession(eq(TEST_USER_ID), anyString());
    }

    @Test
    @DisplayName("创建会话 - 记住我模式")
    void testCreateSession_RememberMe() {
        // Given
        DeviceInfo deviceInfo = createTestDeviceInfo();
        when(sessionRepository.findAllByUserId(TEST_USER_ID)).thenReturn(new ArrayList<>());
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Session session = sessionDomainService.createSession(
                TEST_USER_ID, deviceInfo, ABSOLUTE_TIMEOUT, IDLE_TIMEOUT, true);

        // Then
        assertThat(session).isNotNull();
        assertThat(session.isRememberMe()).isTrue();
    }

    @Test
    @DisplayName("创建会话 - Redis失败不阻塞主流程")
    void testCreateSession_RedisFails_StillSucceeds() {
        // Given
        DeviceInfo deviceInfo = createTestDeviceInfo();
        when(sessionRepository.findAllByUserId(TEST_USER_ID)).thenReturn(new ArrayList<>());
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new RuntimeException("Redis connection failed"))
                .when(sessionCacheService).cacheSession(any(Session.class), anyLong());

        // When
        Session session = sessionDomainService.createSession(
                TEST_USER_ID, deviceInfo, ABSOLUTE_TIMEOUT, IDLE_TIMEOUT, false);

        // Then
        assertThat(session).isNotNull();
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    @DisplayName("创建会话 - 用户ID为空抛出异常")
    void testCreateSession_NullUserId_ThrowsException() {
        // Given
        DeviceInfo deviceInfo = createTestDeviceInfo();

        // When & Then
        assertThatThrownBy(() -> sessionDomainService.createSession(
                null, deviceInfo, ABSOLUTE_TIMEOUT, IDLE_TIMEOUT, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("用户ID不能为空");
    }

    @Test
    @DisplayName("创建会话 - 设备信息为空抛出异常")
    void testCreateSession_NullDeviceInfo_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> sessionDomainService.createSession(
                TEST_USER_ID, null, ABSOLUTE_TIMEOUT, IDLE_TIMEOUT, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("设备信息不能为空");
    }

    // ==================== validateAndRefreshSession 测试 ====================

    @Test
    @DisplayName("验证会话 - 从缓存获取成功")
    void testValidateAndRefreshSession_FromCache_Success() {
        // Given
        Session session = createValidSession();
        when(sessionCacheService.getCachedSession(TEST_SESSION_ID)).thenReturn(session);

        // When
        SessionValidationResult result = sessionDomainService.validateAndRefreshSession(TEST_SESSION_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getSession()).isNotNull();
        assertThat(result.getSession().getId()).isEqualTo(TEST_SESSION_ID);

        verify(sessionCacheService).getCachedSession(TEST_SESSION_ID);
        verify(sessionRepository, never()).findById(anyString());
    }

    @Test
    @DisplayName("验证会话 - 缓存未命中从MySQL获取")
    void testValidateAndRefreshSession_CacheMiss_FromMySQL() {
        // Given
        Session session = createValidSession();
        when(sessionCacheService.getCachedSession(TEST_SESSION_ID)).thenReturn(null);
        when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));

        // When
        SessionValidationResult result = sessionDomainService.validateAndRefreshSession(TEST_SESSION_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();

        verify(sessionCacheService).getCachedSession(TEST_SESSION_ID);
        verify(sessionRepository).findById(TEST_SESSION_ID);
        // 验证回写缓存
        verify(sessionCacheService, atLeastOnce()).cacheSession(any(Session.class), anyLong());
    }

    @Test
    @DisplayName("验证会话 - 会话不存在抛出异常")
    void testValidateAndRefreshSession_SessionNotFound() {
        // Given
        when(sessionCacheService.getCachedSession(TEST_SESSION_ID)).thenReturn(null);
        when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> sessionDomainService.validateAndRefreshSession(TEST_SESSION_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(SessionErrorCode.SESSION_NOT_FOUND.getCode());
                });
    }

    @Test
    @DisplayName("验证会话 - 会话已过期抛出异常")
    void testValidateAndRefreshSession_SessionExpired() {
        // Given
        Session expiredSession = createExpiredSession();
        when(sessionCacheService.getCachedSession(TEST_SESSION_ID)).thenReturn(expiredSession);

        // When & Then
        assertThatThrownBy(() -> sessionDomainService.validateAndRefreshSession(TEST_SESSION_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(SessionErrorCode.SESSION_EXPIRED.getCode());
                });
    }

    @Test
    @DisplayName("验证会话 - 会话空闲超时抛出异常")
    void testValidateAndRefreshSession_IdleTimeout() {
        // Given
        Session idleSession = createIdleTimeoutSession();
        when(sessionCacheService.getCachedSession(TEST_SESSION_ID)).thenReturn(idleSession);

        // When & Then
        assertThatThrownBy(() -> sessionDomainService.validateAndRefreshSession(TEST_SESSION_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(SessionErrorCode.SESSION_IDLE_TIMEOUT.getCode());
                });
    }

    @Test
    @DisplayName("验证会话 - 会话ID为空抛出异常")
    void testValidateAndRefreshSession_NullSessionId() {
        // When & Then
        assertThatThrownBy(() -> sessionDomainService.validateAndRefreshSession(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("会话ID不能为空");
    }

    // ==================== destroySession 测试 ====================

    @Test
    @DisplayName("销毁会话 - 成功")
    void testDestroySession_Success() {
        // Given
        Session session = createValidSession();
        when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));
        doNothing().when(sessionRepository).deleteById(TEST_SESSION_ID);

        // When
        sessionDomainService.destroySession(TEST_SESSION_ID);

        // Then
        verify(sessionRepository).deleteById(TEST_SESSION_ID);
        verify(sessionCacheService).evictSession(TEST_SESSION_ID);
        verify(sessionCacheService).removeUserSession(TEST_USER_ID, TEST_SESSION_ID);
    }

    @Test
    @DisplayName("销毁会话 - Redis失败不阻塞主流程")
    void testDestroySession_RedisFails_StillSucceeds() {
        // Given
        Session session = createValidSession();
        when(sessionRepository.findById(TEST_SESSION_ID)).thenReturn(Optional.of(session));
        doNothing().when(sessionRepository).deleteById(TEST_SESSION_ID);
        doThrow(new RuntimeException("Redis error")).when(sessionCacheService).evictSession(TEST_SESSION_ID);

        // When - should not throw
        sessionDomainService.destroySession(TEST_SESSION_ID);

        // Then
        verify(sessionRepository).deleteById(TEST_SESSION_ID);
    }

    // ==================== findUserSessions 测试 ====================

    @Test
    @DisplayName("查询用户会话 - 成功")
    void testFindUserSessions_Success() {
        // Given
        List<Session> sessions = List.of(createValidSession(), createValidSession());
        when(sessionRepository.findAllByUserId(TEST_USER_ID)).thenReturn(sessions);

        // When
        List<Session> result = sessionDomainService.findUserSessions(TEST_USER_ID);

        // Then
        assertThat(result).hasSize(2);
        verify(sessionRepository).findAllByUserId(TEST_USER_ID);
    }

    // ==================== enforceSessionLimit 测试 ====================

    @Test
    @DisplayName("检查会话限制 - 未超限")
    void testEnforceSessionLimit_NotExceeded() {
        // Given
        List<Session> sessions = List.of(createValidSession());
        when(sessionRepository.findAllByUserId(TEST_USER_ID)).thenReturn(sessions);

        // When
        int cleanedCount = sessionDomainService.enforceSessionLimit(TEST_USER_ID, 5);

        // Then
        assertThat(cleanedCount).isZero();
    }

    @Test
    @DisplayName("检查会话限制 - 超限清理")
    void testEnforceSessionLimit_Exceeded_CleanOldest() {
        // Given
        List<Session> sessions = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Session session = new Session();
            session.setId("session-" + i);
            session.setUserId(TEST_USER_ID);
            session.setCreatedAt(LocalDateTime.now().minusHours(6 - i));
            sessions.add(session);
        }
        when(sessionRepository.findAllByUserId(TEST_USER_ID)).thenReturn(sessions);
        when(sessionRepository.findById(anyString())).thenReturn(Optional.of(sessions.get(0)));

        // When
        int cleanedCount = sessionDomainService.enforceSessionLimit(TEST_USER_ID, 5);

        // Then
        assertThat(cleanedCount).isEqualTo(2); // 6 - 5 + 1 = 2
    }

    // ==================== checkIpChange 测试 ====================

    @Test
    @DisplayName("检查IP变化 - 未变化")
    void testCheckIpChange_NotChanged() {
        // Given
        Session session = createValidSession();

        // When
        boolean changed = sessionDomainService.checkIpChange(session, TEST_IP);

        // Then
        assertThat(changed).isFalse();
    }

    @Test
    @DisplayName("检查IP变化 - 已变化")
    void testCheckIpChange_Changed() {
        // Given
        Session session = createValidSession();

        // When
        boolean changed = sessionDomainService.checkIpChange(session, "10.0.0.1");

        // Then
        assertThat(changed).isTrue();
    }

    // ==================== terminateOtherSessions 测试 ====================

    @Test
    @DisplayName("终止其他会话 - 成功")
    void testTerminateOtherSessions_Success() {
        // Given
        Session currentSession = createValidSession();
        Session otherSession = new Session();
        otherSession.setId("other-session-id");
        otherSession.setUserId(TEST_USER_ID);

        List<Session> sessions = List.of(currentSession, otherSession);
        when(sessionRepository.findAllByUserId(TEST_USER_ID)).thenReturn(sessions);
        when(sessionRepository.findById("other-session-id")).thenReturn(Optional.of(otherSession));

        // When
        int terminatedCount = sessionDomainService.terminateOtherSessions(TEST_SESSION_ID, TEST_USER_ID);

        // Then
        assertThat(terminatedCount).isEqualTo(1);
        verify(sessionRepository).deleteById("other-session-id");
    }

    // ==================== Helper Methods ====================

    private DeviceInfo createTestDeviceInfo() {
        return new DeviceInfo(
                TEST_IP,
                "Mozilla/5.0 Test Browser",
                "DESKTOP",
                "Windows 10",
                "Chrome"
        );
    }

    private Session createValidSession() {
        Session session = new Session();
        session.setId(TEST_SESSION_ID);
        session.setUserId(TEST_USER_ID);
        session.setCreatedAt(LocalDateTime.now());
        session.setLastActivityAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusHours(8));
        session.setDeviceInfo(createTestDeviceInfo());
        session.setAbsoluteTimeout(ABSOLUTE_TIMEOUT);
        session.setIdleTimeout(IDLE_TIMEOUT);
        session.setRememberMe(false);
        return session;
    }

    private Session createExpiredSession() {
        Session session = createValidSession();
        session.setExpiresAt(LocalDateTime.now().minusHours(1));
        return session;
    }

    private Session createIdleTimeoutSession() {
        Session session = createValidSession();
        session.setLastActivityAt(LocalDateTime.now().minusMinutes(35)); // 超过30分钟空闲
        return session;
    }
}
