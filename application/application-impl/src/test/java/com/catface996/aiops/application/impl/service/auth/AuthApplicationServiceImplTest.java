package com.catface996.aiops.application.impl.service.auth;

import com.catface996.aiops.application.api.dto.auth.LoginResult;
import com.catface996.aiops.application.api.dto.auth.RegisterResult;
import com.catface996.aiops.application.api.dto.auth.SessionValidationResult;
import com.catface996.aiops.application.api.dto.auth.request.LoginRequest;
import com.catface996.aiops.application.api.dto.auth.request.RegisterRequest;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.common.exception.ParameterException;
import com.catface996.aiops.domain.api.model.auth.Account;
import com.catface996.aiops.domain.api.model.auth.AccountLockInfo;
import com.catface996.aiops.domain.api.model.auth.AccountRole;
import com.catface996.aiops.domain.api.model.auth.AccountStatus;
import com.catface996.aiops.domain.api.model.auth.DeviceInfo;
import com.catface996.aiops.domain.api.model.auth.PasswordStrengthResult;
import com.catface996.aiops.domain.api.model.auth.Session;
import com.catface996.aiops.domain.api.repository.auth.AccountRepository;
import com.catface996.aiops.domain.api.repository.auth.SessionRepository;
import com.catface996.aiops.domain.api.service.auth.AuthDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuthApplicationServiceImpl 单元测试
 *
 * <p>测试用户注册、登录、登出、会话管理等应用层业务逻辑。</p>
 *
 * <p>验收标准：</p>
 * <ul>
 *   <li>验证注册时用户名重复抛出 BusinessException</li>
 *   <li>验证登录成功返回 JWT Token</li>
 *   <li>验证连续 5 次登录失败后账号被锁定</li>
 *   <li>验证登录成功后失败计数重置</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-24
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("认证应用服务测试")
class AuthApplicationServiceImplTest {

    @Mock
    private AuthDomainService authDomainService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private AuthApplicationServiceImpl authApplicationService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Account testAccount;
    private Session testSession;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        registerRequest = new RegisterRequest(
                "john_doe",
                "john@example.com",
                "SecureP@ss123"
        );

        loginRequest = LoginRequest.of("john_doe", "SecureP@ss123", false);

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setUsername("john_doe");
        testAccount.setEmail("john@example.com");
        testAccount.setPassword("$2a$10$encrypted_password");
        testAccount.setRole(AccountRole.ROLE_USER);
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setCreatedAt(LocalDateTime.now());
        testAccount.setUpdatedAt(LocalDateTime.now());

        testSession = new Session();
        testSession.setId(UUID.randomUUID().toString());
        testSession.setUserId(1L);
        testSession.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token");
        testSession.setExpiresAt(LocalDateTime.now().plusHours(2));
        testSession.setCreatedAt(LocalDateTime.now());
    }

    // ==================== 用户注册测试 ====================

    @Test
    @DisplayName("注册成功 - 所有验证通过")
    void testRegisterSuccess() {
        // Given
        when(accountRepository.existsByUsername(anyString())).thenReturn(false);
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);
        when(authDomainService.validatePasswordStrength(anyString(), anyString(), anyString()))
                .thenReturn(new PasswordStrengthResult(true, Collections.emptyList()));
        when(authDomainService.encryptPassword(anyString())).thenReturn("$2a$10$encrypted_password");
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        RegisterResult result = authApplicationService.register(registerRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccountId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("john_doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getRole()).isEqualTo("ROLE_USER");
        assertThat(result.getMessage()).contains("注册成功");

        // 验证方法调用
        verify(accountRepository).existsByUsername("john_doe");
        verify(accountRepository).existsByEmail("john@example.com");
        verify(authDomainService).validatePasswordStrength("SecureP@ss123", "john_doe", "john@example.com");
        verify(authDomainService).encryptPassword("SecureP@ss123");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("注册失败 - 用户名已存在")
    void testRegisterFailure_DuplicateUsername() {
        // Given
        when(accountRepository.existsByUsername(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authApplicationService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名已存在");

        // 验证方法调用
        verify(accountRepository).existsByUsername("john_doe");
        verify(accountRepository, never()).existsByEmail(anyString());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("注册失败 - 邮箱已存在")
    void testRegisterFailure_DuplicateEmail() {
        // Given
        when(accountRepository.existsByUsername(anyString())).thenReturn(false);
        when(accountRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authApplicationService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("邮箱已存在");

        // 验证方法调用
        verify(accountRepository).existsByUsername("john_doe");
        verify(accountRepository).existsByEmail("john@example.com");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("注册失败 - 密码强度不符合要求")
    void testRegisterFailure_WeakPassword() {
        // Given
        when(accountRepository.existsByUsername(anyString())).thenReturn(false);
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);
        when(authDomainService.validatePasswordStrength(anyString(), anyString(), anyString()))
                .thenReturn(new PasswordStrengthResult(false,
                        Collections.singletonList("密码长度至少为8个字符")));

        // When & Then
        assertThatThrownBy(() -> authApplicationService.register(registerRequest))
                .isInstanceOf(ParameterException.class)
                .hasMessageContaining("密码不符合强度要求");

        // 验证方法调用
        verify(authDomainService).validatePasswordStrength("SecureP@ss123", "john_doe", "john@example.com");
        verify(authDomainService, never()).encryptPassword(anyString());
        verify(accountRepository, never()).save(any(Account.class));
    }

    // ==================== 用户登录测试 ====================

    @Test
    @DisplayName("登录成功 - 返回JWT Token")
    void testLoginSuccess_ReturnsJwtToken() {
        // Given
        when(authDomainService.checkAccountLock(anyString())).thenReturn(Optional.empty());
        when(accountRepository.findByUsername(anyString())).thenReturn(Optional.of(testAccount));
        when(authDomainService.verifyPassword(anyString(), anyString())).thenReturn(true);
        when(authDomainService.createSession(any(Account.class), anyBoolean(), any(DeviceInfo.class)))
                .thenReturn(testSession);
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        // When
        LoginResult result = authApplicationService.login(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getToken()).startsWith("eyJ");
        assertThat(result.getUserInfo()).isNotNull();
        assertThat(result.getUserInfo().getAccountId()).isEqualTo(1L);
        assertThat(result.getUserInfo().getUsername()).isEqualTo("john_doe");
        assertThat(result.getSessionId()).isNotNull();
        assertThat(result.getMessage()).contains("登录成功");

        // 验证方法调用
        verify(authDomainService).checkAccountLock("john_doe");
        verify(accountRepository).findByUsername("john_doe");
        verify(authDomainService).verifyPassword("SecureP@ss123", "$2a$10$encrypted_password");
        verify(authDomainService).createSession(eq(testAccount), eq(false), any(DeviceInfo.class));
        verify(authDomainService).handleSessionMutex(eq(testAccount), eq(testSession));
        verify(sessionRepository).save(testSession);
        verify(authDomainService).resetLoginFailureCount("john_doe");
    }

    @Test
    @DisplayName("登录失败 - 账号已锁定")
    void testLoginFailure_AccountLocked() {
        // Given
        AccountLockInfo lockInfo = AccountLockInfo.locked(
                "连续登录失败5次",
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(30),
                5
        );
        when(authDomainService.checkAccountLock(anyString())).thenReturn(Optional.of(lockInfo));

        // When & Then
        assertThatThrownBy(() -> authApplicationService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("账号已锁定");

        // 验证方法调用
        verify(authDomainService).checkAccountLock("john_doe");
        verify(accountRepository, never()).findByUsername(anyString());
        verify(authDomainService, never()).verifyPassword(anyString(), anyString());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void testLoginFailure_WrongPassword() {
        // Given
        when(authDomainService.checkAccountLock(anyString())).thenReturn(Optional.empty());
        when(accountRepository.findByUsername(anyString())).thenReturn(Optional.of(testAccount));
        when(authDomainService.verifyPassword(anyString(), anyString())).thenReturn(false);
        when(authDomainService.recordLoginFailure(anyString())).thenReturn(1);

        // When & Then
        assertThatThrownBy(() -> authApplicationService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名或密码错误");

        // 验证方法调用
        verify(authDomainService).checkAccountLock("john_doe");
        verify(accountRepository).findByUsername("john_doe");
        verify(authDomainService).verifyPassword("SecureP@ss123", "$2a$10$encrypted_password");
        verify(authDomainService).recordLoginFailure("john_doe");
        verify(authDomainService, never()).createSession(any(), anyBoolean(), any());
    }

    @Test
    @DisplayName("登录失败 - 连续5次失败后账号被锁定")
    void testLoginFailure_LockedAfter5Failures() {
        // Given
        when(authDomainService.checkAccountLock(anyString())).thenReturn(Optional.empty());
        when(accountRepository.findByUsername(anyString())).thenReturn(Optional.of(testAccount));
        when(authDomainService.verifyPassword(anyString(), anyString())).thenReturn(false);
        when(authDomainService.recordLoginFailure(anyString())).thenReturn(5);

        // When & Then
        assertThatThrownBy(() -> authApplicationService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名或密码错误");

        // 验证方法调用
        verify(authDomainService).recordLoginFailure("john_doe");
        verify(authDomainService).lockAccount("john_doe", 30);
    }

    @Test
    @DisplayName("登录成功 - 失败计数重置")
    void testLoginSuccess_FailureCountReset() {
        // Given
        when(authDomainService.checkAccountLock(anyString())).thenReturn(Optional.empty());
        when(accountRepository.findByUsername(anyString())).thenReturn(Optional.of(testAccount));
        when(authDomainService.verifyPassword(anyString(), anyString())).thenReturn(true);
        when(authDomainService.createSession(any(Account.class), anyBoolean(), any(DeviceInfo.class)))
                .thenReturn(testSession);
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        // When
        LoginResult result = authApplicationService.login(loginRequest);

        // Then
        assertThat(result).isNotNull();

        // 验证失败计数被重置
        verify(authDomainService).resetLoginFailureCount("john_doe");
    }

    @Test
    @DisplayName("登录成功 - 使用邮箱登录")
    void testLoginSuccess_WithEmail() {
        // Given
        LoginRequest emailLoginRequest = LoginRequest.of("john@example.com", "SecureP@ss123", false);
        when(authDomainService.checkAccountLock(anyString())).thenReturn(Optional.empty());
        when(accountRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));
        when(authDomainService.verifyPassword(anyString(), anyString())).thenReturn(true);
        when(authDomainService.createSession(any(Account.class), anyBoolean(), any(DeviceInfo.class)))
                .thenReturn(testSession);
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        // When
        LoginResult result = authApplicationService.login(emailLoginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isNotNull();

        // 验证方法调用
        verify(accountRepository).findByUsername("john@example.com");
        verify(accountRepository).findByEmail("john@example.com");
    }

    @Test
    @DisplayName("登录成功 - 记住我功能")
    void testLoginSuccess_WithRememberMe() {
        // Given
        LoginRequest rememberMeRequest = LoginRequest.of("john_doe", "SecureP@ss123", true);
        when(authDomainService.checkAccountLock(anyString())).thenReturn(Optional.empty());
        when(accountRepository.findByUsername(anyString())).thenReturn(Optional.of(testAccount));
        when(authDomainService.verifyPassword(anyString(), anyString())).thenReturn(true);
        when(authDomainService.createSession(any(Account.class), anyBoolean(), any(DeviceInfo.class)))
                .thenReturn(testSession);
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        // When
        LoginResult result = authApplicationService.login(rememberMeRequest);

        // Then
        assertThat(result).isNotNull();

        // 验证创建会话时传入了 rememberMe=true
        verify(authDomainService).createSession(eq(testAccount), eq(true), any(DeviceInfo.class));
    }

    // ==================== 用户登出测试 ====================

    @Test
    @DisplayName("登出成功")
    void testLogoutSuccess() {
        // Given
        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";

        // When
        authApplicationService.logout(token);

        // Then
        // 验证方法调用
        verify(authDomainService).invalidateSession(anyString());
    }

    // ==================== 会话验证测试 ====================

    @Test
    @DisplayName("会话验证成功")
    void testValidateSessionSuccess() {
        // Given
        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
        when(authDomainService.validateSession(anyString())).thenReturn(testSession);
        when(accountRepository.findById(any(Long.class))).thenReturn(Optional.of(testAccount));

        // When
        SessionValidationResult result = authApplicationService.validateSession(token);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getUserInfo()).isNotNull();
        assertThat(result.getUserInfo().getAccountId()).isEqualTo(1L);
        assertThat(result.getSessionId()).isNotNull();
    }

    @Test
    @DisplayName("会话验证失败 - 会话不存在")
    void testValidateSessionFailure_SessionNotFound() {
        // Given
        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
        when(authDomainService.validateSession(anyString()))
                .thenThrow(new RuntimeException("Session not found"));

        // When
        SessionValidationResult result = authApplicationService.validateSession(token);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("会话无效或已过期");
    }

    // ==================== 强制登出其他设备测试 ====================

    @Test
    @DisplayName("强制登出其他设备成功")
    void testForceLogoutOthersSuccess() {
        // Given
        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
        String password = "SecureP@ss123";

        // Mock parseTokenAndGetAccount 流程
        when(authDomainService.validateSession(anyString())).thenReturn(testSession);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Mock 密码验证
        when(authDomainService.verifyPassword(eq(password), eq(testAccount.getPassword())))
                .thenReturn(true);

        // Mock 创建新会话
        Session newSession = new Session();
        newSession.setId(UUID.randomUUID().toString());
        newSession.setUserId(testAccount.getId());
        newSession.setToken("new_jwt_token");
        newSession.setCreatedAt(LocalDateTime.now());
        newSession.setExpiresAt(LocalDateTime.now().plusHours(2));

        when(authDomainService.createSession(eq(testAccount), eq(false), any(DeviceInfo.class)))
                .thenReturn(newSession);
        when(sessionRepository.save(any(Session.class))).thenReturn(newSession);

        // When
        com.catface996.aiops.application.api.dto.auth.request.ForceLogoutRequest request =
                com.catface996.aiops.application.api.dto.auth.request.ForceLogoutRequest.of(token, password);
        LoginResult result = authApplicationService.forceLogoutOthers(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("new_jwt_token");
        assertThat(result.getSessionId()).isEqualTo(newSession.getId());
        assertThat(result.getUserInfo()).isNotNull();
        assertThat(result.getUserInfo().getUsername()).isEqualTo("john_doe");

        // 验证方法调用
        verify(authDomainService).validateSession(anyString());
        verify(authDomainService).verifyPassword(eq(password), eq(testAccount.getPassword()));
        verify(authDomainService).createSession(eq(testAccount), eq(false), any(DeviceInfo.class));
        verify(authDomainService).handleSessionMutex(eq(testAccount), eq(newSession));
        verify(sessionRepository).save(eq(newSession));
    }

    @Test
    @DisplayName("强制登出其他设备 - 密码错误")
    void testForceLogoutOthersWithInvalidPassword() {
        // Given
        String token = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
        String wrongPassword = "WrongPassword123";

        // Mock parseTokenAndGetAccount 流程
        when(authDomainService.validateSession(anyString())).thenReturn(testSession);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        // Mock 密码验证失败
        when(authDomainService.verifyPassword(eq(wrongPassword), eq(testAccount.getPassword())))
                .thenReturn(false);

        // When & Then
        com.catface996.aiops.application.api.dto.auth.request.ForceLogoutRequest request =
                com.catface996.aiops.application.api.dto.auth.request.ForceLogoutRequest.of(token, wrongPassword);

        assertThatThrownBy(() -> authApplicationService.forceLogoutOthers(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("密码验证失败");

        // 验证不会创建新会话
        verify(authDomainService, never()).createSession(any(), anyBoolean(), any());
        verify(authDomainService, never()).handleSessionMutex(any(), any());
        verify(sessionRepository, never()).save(any());
    }

    // ==================== 管理员解锁账号测试 ====================

    @Test
    @DisplayName("管理员解锁账号成功")
    void testUnlockAccountSuccess() {
        // Given
        String adminToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.admin.token";
        Long accountId = 2L;

        // Mock 管理员账号
        Account adminAccount = new Account();
        adminAccount.setId(1L);
        adminAccount.setUsername("admin");
        adminAccount.setRole(AccountRole.ROLE_ADMIN);

        // Mock validateAdminPermission 流程
        when(authDomainService.validateSession(anyString())).thenReturn(testSession);
        when(accountRepository.findById(testSession.getUserId())).thenReturn(Optional.of(adminAccount));

        // When
        authApplicationService.unlockAccount(adminToken, accountId);

        // Then
        // 验证方法调用
        verify(authDomainService).validateSession(anyString());
        verify(authDomainService).unlockAccount(accountId);
    }

    @Test
    @DisplayName("非管理员无法解锁账号")
    void testUnlockAccountWithNonAdminUser() {
        // Given
        String userToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.user.token";
        Long accountId = 2L;

        // Mock 普通用户账号
        Account normalUser = new Account();
        normalUser.setId(1L);
        normalUser.setUsername("john_doe");
        normalUser.setRole(AccountRole.ROLE_USER);  // 普通用户

        // Mock validateAdminPermission 流程
        when(authDomainService.validateSession(anyString())).thenReturn(testSession);
        when(accountRepository.findById(testSession.getUserId())).thenReturn(Optional.of(normalUser));

        // When & Then
        assertThatThrownBy(() -> authApplicationService.unlockAccount(userToken, accountId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("权限不足");

        // 验证不会调用解锁方法
        verify(authDomainService, never()).unlockAccount(any());
    }

    @Test
    @DisplayName("无效Token无法解锁账号")
    void testUnlockAccountWithInvalidToken() {
        // Given
        String invalidToken = "Bearer invalid.token";
        Long accountId = 2L;

        // Mock 会话验证失败
        when(authDomainService.validateSession(anyString()))
                .thenThrow(new RuntimeException("Session not found"));

        // When & Then
        assertThatThrownBy(() -> authApplicationService.unlockAccount(invalidToken, accountId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Session not found");

        // 验证不会调用解锁方法
        verify(authDomainService, never()).unlockAccount(any());
    }

    @Test
    @DisplayName("解锁不存在的账号")
    void testUnlockAccountNotFound() {
        // Given
        String adminToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.admin.token";
        Long nonExistentAccountId = 999L;

        // Mock 管理员账号
        Account adminAccount = new Account();
        adminAccount.setId(1L);
        adminAccount.setUsername("admin");
        adminAccount.setRole(AccountRole.ROLE_ADMIN);

        // Mock validateAdminPermission 流程
        when(authDomainService.validateSession(anyString())).thenReturn(testSession);
        when(accountRepository.findById(testSession.getUserId())).thenReturn(Optional.of(adminAccount));

        // Mock Domain 层抛出账号不存在异常（使用 doThrow 语法用于 void 方法）
        doThrow(new BusinessException("NOT_FOUND_001", "账号不存在"))
                .when(authDomainService).unlockAccount(nonExistentAccountId);

        // When & Then
        assertThatThrownBy(() -> authApplicationService.unlockAccount(adminToken, nonExistentAccountId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("账号不存在");

        // 验证调用了解锁方法（但失败了）
        verify(authDomainService).unlockAccount(nonExistentAccountId);
    }
}
