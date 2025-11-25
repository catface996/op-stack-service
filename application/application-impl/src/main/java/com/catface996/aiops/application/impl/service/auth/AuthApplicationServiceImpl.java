package com.catface996.aiops.application.impl.service.auth;

import com.catface996.aiops.application.api.dto.auth.LoginResult;
import com.catface996.aiops.application.api.dto.auth.RegisterResult;
import com.catface996.aiops.application.api.dto.auth.SessionValidationResult;
import com.catface996.aiops.application.api.dto.auth.UserInfo;
import com.catface996.aiops.application.api.dto.auth.request.ForceLogoutRequest;
import com.catface996.aiops.application.api.dto.auth.request.LoginRequest;
import com.catface996.aiops.application.api.dto.auth.request.RegisterRequest;
import com.catface996.aiops.application.api.service.auth.AuthApplicationService;
import com.catface996.aiops.common.enums.AuthErrorCode;
import com.catface996.aiops.common.enums.ParamErrorCode;
import com.catface996.aiops.common.enums.ResourceErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.common.exception.ParameterException;
import com.catface996.aiops.domain.model.auth.Account;
import com.catface996.aiops.domain.model.auth.AccountLockInfo;
import com.catface996.aiops.domain.model.auth.AccountRole;
import com.catface996.aiops.domain.model.auth.AccountStatus;
import com.catface996.aiops.domain.model.auth.DeviceInfo;
import com.catface996.aiops.domain.model.auth.PasswordStrengthResult;
import com.catface996.aiops.domain.model.auth.Session;
import com.catface996.aiops.domain.api.service.auth.AuthDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 认证应用服务实现
 *
 * <p>实现用户注册、登录、登出、会话管理等应用层业务逻辑。</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>协调领域服务和仓储完成业务流程</li>
 *   <li>处理事务边界</li>
 *   <li>记录审计日志</li>
 *   <li>转换领域模型与DTO</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-001: 用户名密码登录</li>
 *   <li>REQ-FR-002: 邮箱密码登录</li>
 *   <li>REQ-FR-003: 账号注册</li>
 *   <li>REQ-FR-005: 防暴力破解</li>
 *   <li>REQ-FR-012: 密码强度要求</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthApplicationServiceImpl implements AuthApplicationService {

    private final AuthDomainService authDomainService;

    /**
     * 用户注册
     *
     * <p>需求追溯：REQ-FR-003, REQ-FR-012</p>
     *
     * @param request 注册请求
     * @return 注册结果
     * @throws BusinessException 如果用户名或邮箱已存在
     * @throws ParameterException 如果密码不符合强度要求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterResult register(RegisterRequest request) {
        // 1. 验证账号唯一性
        validateAccountUniqueness(request);

        // 2. 验证密码强度
        validatePasswordStrength(request);

        // 3. 创建并加密账号
        Account account = createAccountWithEncryptedPassword(request);

        // 4. 持久化账号
        Account savedAccount = authDomainService.saveAccount(account);

        // 5. 记录审计日志
        logRegistrationSuccess(savedAccount);

        // 6. 返回结果
        return buildRegisterResult(savedAccount);
    }

    /**
     * 用户登录
     *
     * <p>需求追溯：REQ-FR-001, REQ-FR-002, REQ-FR-005</p>
     *
     * @param request 登录请求
     * @return 登录结果（包含JWT Token）
     * @throws BusinessException 如果账号不存在、被锁定或认证失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResult login(LoginRequest request) {
        // 1. 检查账号锁定
        checkAccountNotLocked(request.getIdentifier());

        // 2. 查找并验证账号
        Account account = findAndVerifyAccount(request);

        // 3. 创建会话
        Session session = createAndSaveSession(account, request.getRememberMe());

        // 4. 重置登录失败计数
        authDomainService.resetLoginFailureCount(request.getIdentifier());

        // 5. 记录审计日志
        logLoginSuccess(account, session, request.getRememberMe());

        // 6. 返回结果
        return buildLoginResult(account, session);
    }

    /**
     * 用户登出
     *
     * <p>需求追溯：REQ-FR-010</p>
     *
     * @param token JWT Token
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(String token) {
        // 1. 解析会话ID
        String sessionId = parseSessionId(token);

        // 2. 使会话失效
        authDomainService.invalidateSession(sessionId);

        // 3. 记录审计日志
        logLogoutSuccess(sessionId);
    }

    /**
     * 验证会话
     *
     * <p>需求追溯：REQ-FR-007</p>
     *
     * @param token JWT Token
     * @return 会话验证结果
     */
    @Override
    public SessionValidationResult validateSession(String token) {
        try {
            // 1. 解析会话ID
            String sessionId = parseSessionId(token);

            // 2. 验证会话
            Session session = authDomainService.validateSession(sessionId);

            // 3. 查询用户信息
            Account account = authDomainService.findAccountById(session.getUserId())
                    .orElseThrow(() -> new BusinessException(ResourceErrorCode.ACCOUNT_NOT_FOUND));

            // 4. 返回结果
            return buildSessionValidationResult(session, account);

        } catch (Exception e) {
            log.warn("[应用层] 会话验证失败: {}", e.getMessage());
            return SessionValidationResult.invalid("会话无效或已过期");
        }
    }

    /**
     * 强制登出其他设备
     *
     * <p>此方法已废弃，请使用 {@link #forceLogoutOthers(ForceLogoutRequest)}</p>
     *
     * @param token JWT Token
     * @param password 密码
     * @return 登录结果
     * @deprecated 使用 {@link #forceLogoutOthers(ForceLogoutRequest)} 代替
     */
    @Deprecated
    @Override
    public LoginResult forceLogoutOthers(String token, String password) {
        ForceLogoutRequest request = ForceLogoutRequest.of(token, password);
        return forceLogoutOthers(request);
    }

    /**
     * 强制登出其他设备
     *
     * <p>需求追溯：REQ-FR-009</p>
     *
     * @param request 强制登出请求
     * @return 登录结果（包含新的JWT Token）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResult forceLogoutOthers(ForceLogoutRequest request) {
        // 1. 解析 Token 并获取用户账号
        Account account = parseTokenAndGetAccount(request.getToken());

        // 2. 验证密码
        verifyPasswordForAccount(account, request.getPassword());

        // 3. 创建新会话（包含会话互斥逻辑）
        Session newSession = createAndSaveSession(account, false);

        // 4. 记录审计日志
        logForceLogoutOthers(account, newSession);

        // 5. 返回结果
        return buildLoginResult(account, newSession);
    }

    /**
     * 管理员解锁账号
     *
     * <p>需求追溯：REQ-FR-006</p>
     *
     * @param adminToken 管理员JWT Token
     * @param accountId 账号ID
     * @throws BusinessException 如果账号不存在
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlockAccount(String adminToken, Long accountId) {
        // 1. 验证管理员权限
        validateAdminPermission(adminToken);

        // 2. 解锁账号
        authDomainService.unlockAccount(accountId);

        // 3. 记录审计日志
        logAdminUnlockAccount(accountId, adminToken);
    }

    // ==================== 注册相关私有方法 ====================

    /**
     * 验证账号唯一性
     *
     * @param request 注册请求
     * @throws BusinessException 如果用户名或邮箱已存在
     */
    private void validateAccountUniqueness(RegisterRequest request) {
        if (authDomainService.existsByUsername(request.getUsername())) {
            log.warn("[应用层] 用户名已存在, username={}", request.getUsername());
            throw new BusinessException(ResourceErrorCode.USERNAME_CONFLICT);
        }

        if (authDomainService.existsByEmail(request.getEmail())) {
            log.warn("[应用层] 邮箱已存在, email={}", request.getEmail());
            throw new BusinessException(ResourceErrorCode.EMAIL_CONFLICT);
        }
    }

    /**
     * 验证密码强度
     *
     * @param request 注册请求
     * @throws ParameterException 如果密码不符合强度要求
     */
    private void validatePasswordStrength(RegisterRequest request) {
        PasswordStrengthResult result = authDomainService.validatePasswordStrength(
                request.getPassword(),
                request.getUsername(),
                request.getEmail()
        );

        if (!result.isValid()) {
            log.warn("[应用层] 密码强度不符合要求, username={}, errors={}",
                    request.getUsername(), result.getErrors());
            throw new ParameterException(ParamErrorCode.INVALID_PASSWORD, result.getErrors());
        }
    }

    /**
     * 创建账号并加密密码
     *
     * @param request 注册请求
     * @return 创建的账号实体
     */
    private Account createAccountWithEncryptedPassword(RegisterRequest request) {
        String encryptedPassword = authDomainService.encryptPassword(request.getPassword());

        Account account = new Account();
        account.setUsername(request.getUsername());
        account.setEmail(request.getEmail());
        account.setPassword(encryptedPassword);
        account.setRole(AccountRole.ROLE_USER);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());

        return account;
    }

    /**
     * 记录注册成功的审计日志
     *
     * @param account 保存后的账号实体
     */
    private void logRegistrationSuccess(Account account) {
        log.info("[审计日志] 用户注册成功 | accountId={} | username={} | email={} | role={} | timestamp={}",
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                account.getRole(),
                LocalDateTime.now());
    }

    /**
     * 构建注册结果
     *
     * @param account 保存后的账号实体
     * @return 注册结果 DTO
     */
    private RegisterResult buildRegisterResult(Account account) {
        return RegisterResult.builder()
                .accountId(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .role(account.getRole().name())
                .createdAt(account.getCreatedAt())
                .message("注册成功，请使用用户名或邮箱登录")
                .build();
    }

    // ==================== 登录相关私有方法 ====================

    /**
     * 检查账号是否被锁定
     *
     * @param identifier 用户标识符
     * @throws BusinessException 如果账号被锁定
     */
    private void checkAccountNotLocked(String identifier) {
        Optional<AccountLockInfo> lockInfo = authDomainService.checkAccountLock(identifier);

        if (lockInfo.isPresent()) {
            AccountLockInfo info = lockInfo.get();
            log.warn("[审计日志] 登录失败-账号已锁定 | identifier={} | remainingMinutes={} | timestamp={}",
                    identifier, info.getRemainingMinutes(), LocalDateTime.now());
            throw new BusinessException(ResourceErrorCode.ACCOUNT_LOCKED, (int) info.getRemainingMinutes());
        }
    }

    /**
     * 查找并验证账号
     *
     * @param request 登录请求
     * @return 验证通过的账号
     * @throws BusinessException 如果账号不存在或密码错误
     */
    private Account findAndVerifyAccount(LoginRequest request) {
        Account account = findAccountByIdentifier(request.getIdentifier());

        boolean passwordMatches = authDomainService.verifyPassword(
                request.getPassword(),
                account.getPassword()
        );

        if (!passwordMatches) {
            handleLoginFailure(request.getIdentifier(), account);
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        return account;
    }

    /**
     * 根据标识符查找账号
     *
     * <p>标识符可以是用户名或邮箱。</p>
     *
     * @param identifier 用户标识符
     * @return 账号实体
     * @throws BusinessException 如果账号不存在
     */
    private Account findAccountByIdentifier(String identifier) {
        return authDomainService.findAccountByUsername(identifier)
                .or(() -> authDomainService.findAccountByEmail(identifier))
                .orElseThrow(() -> {
                    log.warn("[应用层] 账号不存在, identifier={}", identifier);
                    // 为了安全，返回通用错误消息
                    return new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
                });
    }

    /**
     * 创建并保存会话
     *
     * @param account 账号实体
     * @param rememberMe 是否记住我
     * @return 创建的会话
     */
    private Session createAndSaveSession(Account account, Boolean rememberMe) {
        DeviceInfo deviceInfo = createDeviceInfo();
        Session session = authDomainService.createSession(account, rememberMe, deviceInfo);
        authDomainService.handleSessionMutex(account, session);
        return authDomainService.saveSession(session);
    }

    /**
     * 创建设备信息
     *
     * @return 设备信息
     */
    private DeviceInfo createDeviceInfo() {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceType("Unknown");  // TODO: 从request中提取设备信息
        deviceInfo.setOperatingSystem("Unknown");
        deviceInfo.setBrowser("Unknown");
        return deviceInfo;
    }

    /**
     * 处理登录失败
     *
     * <p>增加失败计数，如果达到5次则锁定账号30分钟。</p>
     *
     * @param identifier 用户标识符
     * @param account 账号实体
     */
    private void handleLoginFailure(String identifier, Account account) {
        int failureCount = authDomainService.recordLoginFailure(identifier);

        log.warn("[审计日志] 登录失败-密码错误 | accountId={} | username={} | identifier={} | failureCount={} | timestamp={}",
                account.getId(),
                account.getUsername(),
                identifier,
                failureCount,
                LocalDateTime.now());

        if (failureCount >= 5) {
            authDomainService.lockAccount(identifier, 30);
            log.warn("[审计日志] 账号锁定 | accountId={} | username={} | identifier={} | failureCount={} | lockDurationMinutes=30 | timestamp={}",
                    account.getId(),
                    account.getUsername(),
                    identifier,
                    failureCount,
                    LocalDateTime.now());
        }
    }

    /**
     * 记录登录成功的审计日志
     *
     * @param account 账号实体
     * @param session 会话
     * @param rememberMe 是否记住我
     */
    private void logLoginSuccess(Account account, Session session, Boolean rememberMe) {
        log.info("[审计日志] 用户登录成功 | accountId={} | username={} | sessionId={} | rememberMe={} | timestamp={}",
                account.getId(),
                account.getUsername(),
                session.getId(),
                rememberMe,
                LocalDateTime.now());
    }

    /**
     * 构建登录结果
     *
     * @param account 账号实体
     * @param session 会话
     * @return 登录结果 DTO
     */
    private LoginResult buildLoginResult(Account account, Session session) {
        UserInfo userInfo = convertToUserInfo(account);

        return LoginResult.builder()
                .token(session.getToken())
                .userInfo(userInfo)
                .sessionId(session.getId())
                .expiresAt(session.getExpiresAt())
                .deviceInfo(createDeviceInfo().toString())
                .message("登录成功")
                .build();
    }

    // ==================== 登出相关私有方法 ====================

    /**
     * 解析会话ID
     *
     * @param token JWT Token
     * @return 会话ID
     */
    private String parseSessionId(String token) {
        // TODO: 从JWT Token中解析sessionId
        // return jwtTokenProvider.getSessionIdFromToken(token);
        return "temp-session-id"; // 临时代码
    }

    /**
     * 记录登出成功的审计日志
     *
     * @param sessionId 会话ID
     */
    private void logLogoutSuccess(String sessionId) {
        log.info("[审计日志] 用户登出成功 | sessionId={} | timestamp={}",
                sessionId,
                LocalDateTime.now());
    }

    // ==================== 会话验证相关私有方法 ====================

    /**
     * 构建会话验证结果
     *
     * @param session 会话
     * @param account 账号实体
     * @return 会话验证结果
     */
    private SessionValidationResult buildSessionValidationResult(Session session, Account account) {
        UserInfo userInfo = convertToUserInfo(account);

        long remainingSeconds = java.time.Duration.between(
                LocalDateTime.now(),
                session.getExpiresAt()
        ).getSeconds();

        return SessionValidationResult.valid(
                userInfo,
                session.getId(),
                session.getExpiresAt(),
                remainingSeconds
        );
    }

    // ==================== 管理员相关私有方法 ====================

    /**
     * 验证管理员权限
     *
     * @param adminToken 管理员Token
     * @throws BusinessException 如果会话不存在、已过期、账号不存在或不是管理员
     */
    private void validateAdminPermission(String adminToken) {
        // 1. 解析 Token 获取会话ID
        String sessionId = parseSessionId(adminToken);

        // 2. 验证会话有效性
        Session session = authDomainService.validateSession(sessionId);

        // 3. 查询用户账号
        Account account = authDomainService.findAccountById(session.getUserId())
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.ACCOUNT_NOT_FOUND));

        // 4. 验证角色是否为 ADMIN
        if (account.getRole() != AccountRole.ROLE_ADMIN) {
            log.warn("[应用层] 非管理员尝试访问管理功能, accountId={}, username={}, role={}",
                    account.getId(), account.getUsername(), account.getRole());
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS, "权限不足，仅管理员可以执行此操作");
        }

        log.debug("[应用层] 管理员权限验证通过, accountId={}, username={}",
                account.getId(), account.getUsername());
    }

    /**
     * 记录管理员解锁账号的审计日志
     *
     * @param accountId 账号ID
     * @param adminToken 管理员Token
     */
    private void logAdminUnlockAccount(Long accountId, String adminToken) {
        log.info("[审计日志] 管理员解锁账号 | accountId={} | adminToken={} | timestamp={}",
                accountId,
                adminToken,
                LocalDateTime.now());
    }

    // ==================== 强制登出相关私有方法 ====================

    /**
     * 解析 Token 并获取用户账号
     *
     * @param token JWT Token
     * @return 用户账号
     * @throws BusinessException 如果会话不存在、已过期或账号不存在
     */
    private Account parseTokenAndGetAccount(String token) {
        // 1. 解析 Token 获取会话ID
        String sessionId = parseSessionId(token);

        // 2. 验证会话
        Session session = authDomainService.validateSession(sessionId);

        // 3. 查询用户账号
        return authDomainService.findAccountById(session.getUserId())
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.ACCOUNT_NOT_FOUND));
    }

    /**
     * 验证账号密码
     *
     * @param account 账号实体
     * @param password 待验证的密码
     * @throws BusinessException 如果密码验证失败
     */
    private void verifyPasswordForAccount(Account account, String password) {
        boolean passwordMatches = authDomainService.verifyPassword(password, account.getPassword());

        if (!passwordMatches) {
            log.warn("[应用层] 强制登出密码验证失败, accountId={}, username={}",
                    account.getId(), account.getUsername());
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS, "密码验证失败");
        }
    }

    /**
     * 记录强制登出其他设备的审计日志
     *
     * @param account 账号实体
     * @param session 新会话
     */
    private void logForceLogoutOthers(Account account, Session session) {
        log.info("[审计日志] 强制登出其他设备 | accountId={} | username={} | newSessionId={} | timestamp={}",
                account.getId(),
                account.getUsername(),
                session.getId(),
                LocalDateTime.now());
    }

    // ==================== 通用转换方法 ====================

    /**
     * 将账号实体转换为用户信息DTO
     *
     * @param account 账号实体
     * @return 用户信息DTO
     */
    private UserInfo convertToUserInfo(Account account) {
        return UserInfo.builder()
                .accountId(account.getId())
                .username(account.getUsername())
                .email(account.getEmail())
                .role(account.getRole().name())
                .status(account.getStatus().name())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
