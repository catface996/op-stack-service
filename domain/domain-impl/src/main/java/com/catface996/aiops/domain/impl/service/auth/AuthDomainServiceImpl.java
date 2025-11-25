package com.catface996.aiops.domain.impl.service.auth;

import com.catface996.aiops.common.enums.AuthErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.api.model.auth.Account;
import com.catface996.aiops.domain.api.model.auth.AccountLockInfo;
import com.catface996.aiops.domain.api.model.auth.AccountStatus;
import com.catface996.aiops.domain.api.model.auth.DeviceInfo;
import com.catface996.aiops.domain.api.model.auth.PasswordStrengthResult;
import com.catface996.aiops.domain.api.model.auth.Session;
import com.catface996.aiops.repository.auth.AccountRepository;
import com.catface996.aiops.repository.auth.SessionRepository;
import com.catface996.aiops.domain.api.service.auth.AuthDomainService;
import com.catface996.aiops.infrastructure.cache.api.service.LoginAttemptCache;
import com.catface996.aiops.infrastructure.cache.api.service.SessionCache;
import com.catface996.aiops.infrastructure.security.api.service.JwtTokenProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 认证领域服务实现类
 *
 * 实现密码管理、会话管理、账号锁定等核心业务逻辑
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
@Slf4j
@Service
public class AuthDomainServiceImpl implements AuthDomainService {

    // 密码长度常量
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 64;

    // 会话过期时间常量（小时）
    private static final int DEFAULT_SESSION_HOURS = 2;
    private static final int REMEMBER_ME_SESSION_DAYS = 30;

    // 账号锁定常量
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    // 密码字符类型正则表达式
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");

    // 弱密码检测正则表达式（连续字符至少4位才算弱密码）
    private static final Pattern CONSECUTIVE_CHARS_PATTERN = Pattern.compile(".*(0123|1234|2345|3456|4567|5678|6789|abcd|bcde|cdef|defg|efgh|fghi|ghij|hijk|ijkl|jklm|klmn|lmno|mnop|nopq|opqr|pqrs|qrst|rstu|stuv|tuvw|uvwx|vwxy|wxyz).*");
    private static final Pattern REPEATED_CHARS_PATTERN = Pattern.compile(".*(.)\\1{5,}.*");
    private static final Pattern KEYBOARD_SEQUENCE_PATTERN = Pattern.compile(".*(qwerty|asdfgh|zxcvbn).*");

    // 常见弱密码列表
    private static final String[] COMMON_WEAK_PASSWORDS = {
        "password", "admin", "123456", "qwerty", "letmein",
        "password123", "admin123", "12345678", "123456789"
    };

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SessionCache sessionCache;
    private final ObjectMapper objectMapper;
    private final SessionRepository sessionRepository;
    private final LoginAttemptCache loginAttemptCache;
    private final AccountRepository accountRepository;

    /**
     * 构造函数
     *
     * @param passwordEncoder Spring Security提供的BCryptPasswordEncoder
     * @param jwtTokenProvider JWT Token提供者
     * @param sessionCache 会话缓存
     * @param sessionRepository 会话仓储
     * @param loginAttemptCache 登录失败计数缓存
     * @param accountRepository 账号仓储
     */
    public AuthDomainServiceImpl(PasswordEncoder passwordEncoder,
                                  JwtTokenProvider jwtTokenProvider,
                                  SessionCache sessionCache,
                                  SessionRepository sessionRepository,
                                  LoginAttemptCache loginAttemptCache,
                                  AccountRepository accountRepository) {
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.sessionCache = sessionCache;
        this.sessionRepository = sessionRepository;
        this.loginAttemptCache = loginAttemptCache;
        this.accountRepository = accountRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        // 配置忽略未知属性
        this.objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // ==================== 密码管理 ====================

    @Override
    public String encryptPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("原始密码不能为空");
        }

        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("原始密码不能为空");
        }

        if (encodedPassword == null || encodedPassword.isEmpty()) {
            throw new IllegalArgumentException("加密密码不能为空");
        }

        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public PasswordStrengthResult validatePasswordStrength(String password, String username, String email) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }

        List<String> errors = new ArrayList<>();

        // 规则1：长度检查
        if (password.length() < MIN_PASSWORD_LENGTH) {
            errors.add("密码长度至少为" + MIN_PASSWORD_LENGTH + "个字符");
        }

        if (password.length() > MAX_PASSWORD_LENGTH) {
            errors.add("密码长度最多为" + MAX_PASSWORD_LENGTH + "个字符");
        }

        // 规则2：字符类型检查（至少包含3类）
        int typeCount = 0;
        if (UPPERCASE_PATTERN.matcher(password).matches()) {
            typeCount++;
        }
        if (LOWERCASE_PATTERN.matcher(password).matches()) {
            typeCount++;
        }
        if (DIGIT_PATTERN.matcher(password).matches()) {
            typeCount++;
        }
        if (SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            typeCount++;
        }

        if (typeCount < 3) {
            errors.add("密码必须包含大写字母、小写字母、数字、特殊字符中的至少3类");
        }

        // 规则3：不包含用户名或邮箱
        String lowerPassword = password.toLowerCase();
        String lowerUsername = username.toLowerCase();

        if (lowerPassword.contains(lowerUsername)) {
            errors.add("密码不能包含用户名");
        }

        // 提取邮箱前缀（@之前的部分）
        String emailPrefix = email.split("@")[0].toLowerCase();
        if (lowerPassword.contains(emailPrefix)) {
            errors.add("密码不能包含邮箱");
        }

        // 规则4：弱密码检查
        if (isWeakPassword(password)) {
            errors.add("密码过于简单，请使用更复杂的密码");
        }

        // 返回验证结果
        if (errors.isEmpty()) {
            return PasswordStrengthResult.valid();
        } else {
            return PasswordStrengthResult.invalid(errors);
        }
    }

    /**
     * 检查是否为弱密码
     *
     * @param password 待检查的密码
     * @return true if password is weak, false otherwise
     */
    private boolean isWeakPassword(String password) {
        String lowerPassword = password.toLowerCase();

        // 检查连续字符
        if (CONSECUTIVE_CHARS_PATTERN.matcher(lowerPassword).matches()) {
            return true;
        }

        // 检查重复字符
        if (REPEATED_CHARS_PATTERN.matcher(lowerPassword).matches()) {
            return true;
        }

        // 检查键盘序列
        if (KEYBOARD_SEQUENCE_PATTERN.matcher(lowerPassword).matches()) {
            return true;
        }

        // 检查常见弱密码
        for (String weakPassword : COMMON_WEAK_PASSWORDS) {
            if (lowerPassword.contains(weakPassword)) {
                return true;
            }
        }

        return false;
    }

    // ==================== 会话管理 ====================

    @Override
    public Session createSession(Account account, boolean rememberMe, DeviceInfo deviceInfo) {
        if (account == null) {
            throw new IllegalArgumentException("账号不能为空");
        }

        if (account.getId() == null) {
            throw new IllegalArgumentException("账号ID不能为空");
        }

        if (account.getUsername() == null || account.getUsername().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        // 生成会话ID（UUID）
        String sessionId = UUID.randomUUID().toString();

        // 计算过期时间
        LocalDateTime expiresAt;
        if (rememberMe) {
            // 记住我：30天
            expiresAt = LocalDateTime.now().plusDays(REMEMBER_ME_SESSION_DAYS);
        } else {
            // 默认：2小时
            expiresAt = LocalDateTime.now().plusHours(DEFAULT_SESSION_HOURS);
        }

        // 生成JWT Token
        String token = jwtTokenProvider.generateToken(
            account.getId(),
            account.getUsername(),
            account.getRole() != null ? account.getRole().name() : "USER",
            rememberMe
        );

        // 创建Session对象
        Session session = new Session(
            sessionId,
            account.getId(),
            token,
            expiresAt,
            deviceInfo,
            LocalDateTime.now()
        );

        // 保存到仓储（MySQL作为兜底）
        Session persistedSession = sessionRepository.save(session);

        // 同步缓存
        cacheSession(persistedSession);

        return persistedSession;
    }

    @Override
    public Session validateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }

        // 优先从缓存获取，缓存未命中则回源仓储
        Optional<Session> sessionOpt = findSession(sessionId, true);
        if (!sessionOpt.isPresent()) {
            throw new BusinessException(AuthErrorCode.SESSION_NOT_FOUND);
        }

        Session session = sessionOpt.get();

        // 检查会话是否过期
        if (session.isExpired()) {
            // 删除过期的会话（缓存 + 仓储）
            removeSessionData(sessionId, session.getUserId());
            throw new BusinessException(AuthErrorCode.SESSION_EXPIRED);
        }

        return session;
    }

    @Override
    public void invalidateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }

        // 尝试获取会话以定位用户ID
        Optional<Session> sessionOpt = findSession(sessionId, false);
        Long userId = sessionOpt.map(Session::getUserId).orElse(null);

        // 删除缓存与仓储记录
        removeSessionData(sessionId, userId);
    }

    @Override
    public void handleSessionMutex(Account account, Session newSession) {
        if (account == null) {
            throw new IllegalArgumentException("账号不能为空");
        }

        if (account.getId() == null) {
            throw new IllegalArgumentException("账号ID不能为空");
        }

        if (newSession == null) {
            throw new IllegalArgumentException("新会话不能为空");
        }

        // 删除缓存中的旧会话（如果存在）
        sessionCache.deleteByUserId(account.getId());

        // 删除仓储中的旧会话
        sessionRepository.deleteByUserId(account.getId());
    }

    // ==================== 账号锁定 ====================

    @Override
    public int recordLoginFailure(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("标识符不能为空");
        }

        log.info("记录登录失败，标识符：{}", identifier);

        // 使用LoginAttemptCache记录失败次数
        int failureCount = loginAttemptCache.recordFailure(identifier);

        log.info("登录失败记录完成，标识符：{}，失败次数：{}", identifier, failureCount);

        // 如果达到阈值，自动锁定账号
        if (failureCount >= MAX_LOGIN_ATTEMPTS) {
            log.warn("登录失败次数达到阈值，触发账号锁定，标识符：{}，失败次数：{}", identifier, failureCount);
            lockAccount(identifier, LOCK_DURATION_MINUTES);
        }

        return failureCount;
    }

    @Override
    public Optional<AccountLockInfo> checkAccountLock(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("标识符不能为空");
        }

        log.info("检查账号锁定状态，标识符：{}", identifier);

        // 检查是否被锁定
        boolean isLocked = loginAttemptCache.isLocked(identifier);

        if (!isLocked) {
            log.info("账号未锁定，标识符：{}", identifier);
            return Optional.of(AccountLockInfo.notLocked());
        }

        // 获取失败次数和剩余锁定时间
        int failedAttempts = loginAttemptCache.getFailureCount(identifier);
        long remainingSeconds = loginAttemptCache.getRemainingLockTime(identifier);

        // 如果剩余时间为0，说明已经自动解锁
        if (remainingSeconds <= 0) {
            log.info("账号锁定已过期，自动解锁，标识符：{}", identifier);
            return Optional.of(AccountLockInfo.notLocked());
        }

        // 计算锁定时间和解锁时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime unlockAt = now.plusSeconds(remainingSeconds);
        // 估算锁定时间（向前推算）
        LocalDateTime lockedAt = unlockAt.minusMinutes(LOCK_DURATION_MINUTES);

        AccountLockInfo lockInfo = AccountLockInfo.locked(
            "登录失败次数过多",
            lockedAt,
            unlockAt,
            failedAttempts
        );

        log.warn("账号已锁定，标识符：{}，失败次数：{}，剩余锁定时间：{}秒",
            identifier, failedAttempts, remainingSeconds);

        return Optional.of(lockInfo);
    }

    @Override
    public void lockAccount(String identifier, int lockDurationMinutes) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("标识符不能为空");
        }

        if (lockDurationMinutes <= 0) {
            throw new IllegalArgumentException("锁定时长必须大于0");
        }

        log.info("锁定账号，标识符：{}，锁定时长：{}分钟", identifier, lockDurationMinutes);

        // 注意：账号锁定主要通过Redis的登录失败计数来实现
        // 不需要更新Account实体的状态字段（避免数据库频繁更新）
        // 只有当失败次数>=5次时，才认为账号被锁定
        // 这里不执行额外操作，因为recordLoginFailure已经更新了计数

        log.info("账号锁定完成（通过Redis计数实现），标识符：{}", identifier);
    }

    @Override
    public void unlockAccount(Long accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("账号ID不能为空");
        }

        log.info("开始解锁账号，账号ID：{}", accountId);

        // 查找账号
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (!accountOpt.isPresent()) {
            log.error("解锁账号失败，账号不存在，账号ID：{}", accountId);
            throw new IllegalArgumentException("账号不存在");
        }

        Account account = accountOpt.get();

        // 重置用户名的失败计数
        if (account.getUsername() != null) {
            loginAttemptCache.unlock(account.getUsername());
            log.info("清除用户名失败计数，用户名：{}", account.getUsername());
        }

        // 重置邮箱的失败计数
        if (account.getEmail() != null) {
            loginAttemptCache.unlock(account.getEmail());
            log.info("清除邮箱失败计数，邮箱：{}", account.getEmail());
        }

        // 如果账号状态是LOCKED，更新为ACTIVE
        if (account.getStatus() == AccountStatus.LOCKED) {
            accountRepository.updateStatus(accountId, AccountStatus.ACTIVE);
            log.info("账号状态已更新，账号ID：{}，旧状态：{}，新状态：{}",
                accountId, AccountStatus.LOCKED, AccountStatus.ACTIVE);
        }

        log.info("账号解锁成功，账号ID：{}，用户名：{}", accountId, account.getUsername());
    }

    @Override
    public void resetLoginFailureCount(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("标识符不能为空");
        }

        log.info("重置登录失败计数，标识符：{}", identifier);

        // 使用LoginAttemptCache重置失败计数
        loginAttemptCache.resetFailureCount(identifier);

        log.info("登录失败计数重置成功，标识符：{}", identifier);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将Session写入缓存
     */
    private void cacheSession(Session session) {
        if (session == null) {
            return;
        }
        if (session.getId() == null || session.getUserId() == null || session.getExpiresAt() == null) {
            return;
        }

        String sessionData = serializeSession(session);
        sessionCache.save(session.getId(), sessionData, session.getExpiresAt(), session.getUserId());
    }

    /**
     * 查找会话
     *
     * @param sessionId 会话ID
     * @param refreshCacheFromRepository 是否在回源仓储后刷新缓存
     */
    private Optional<Session> findSession(String sessionId, boolean refreshCacheFromRepository) {
        Optional<Session> sessionFromCache = getSessionFromCache(sessionId);
        if (sessionFromCache.isPresent()) {
            return sessionFromCache;
        }

        Optional<Session> sessionFromRepository = sessionRepository.findById(sessionId);
        if (sessionFromRepository == null) {
            sessionFromRepository = Optional.empty();
        }

        if (refreshCacheFromRepository) {
            sessionFromRepository.ifPresent(this::cacheSession);
        }

        return sessionFromRepository;
    }

    /**
     * 从缓存中读取会话
     */
    private Optional<Session> getSessionFromCache(String sessionId) {
        Optional<String> sessionDataOpt = sessionCache.get(sessionId);
        if (sessionDataOpt == null || !sessionDataOpt.isPresent()) {
            return Optional.empty();
        }

        Session session = deserializeSession(sessionDataOpt.get());
        return Optional.of(session);
    }

    /**
     * 序列化会话
     */
    private String serializeSession(Session session) {
        try {
            return objectMapper.writeValueAsString(session);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("会话序列化失败", e);
        }
    }

    /**
     * 反序列化会话
     */
    private Session deserializeSession(String sessionData) {
        try {
            return objectMapper.readValue(sessionData, Session.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("会话反序列化失败", e);
        }
    }

    /**
     * 删除缓存和仓储中的会话数据
     */
    private void removeSessionData(String sessionId, Long userId) {
        if (userId != null) {
            sessionCache.deleteByUserId(userId);
        } else {
            sessionCache.delete(sessionId);
        }
        sessionRepository.deleteById(sessionId);
    }

    // ==================== 数据访问方法（Account） ====================

    @Override
    public Account saveAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("账号不能为空");
        }

        log.info("保存账号，用户名：{}", account.getUsername());
        Account savedAccount = accountRepository.save(account);
        log.info("账号保存成功，账号ID：{}，用户名：{}", savedAccount.getId(), savedAccount.getUsername());

        return savedAccount;
    }

    @Override
    public Optional<Account> findAccountById(Long accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("账号ID不能为空");
        }

        log.debug("根据ID查询账号，账号ID：{}", accountId);
        return accountRepository.findById(accountId);
    }

    @Override
    public Optional<Account> findAccountByUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        log.debug("根据用户名查询账号，用户名：{}", username);
        return accountRepository.findByUsername(username);
    }

    @Override
    public Optional<Account> findAccountByEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }

        log.debug("根据邮箱查询账号，邮箱：{}", email);
        return accountRepository.findByEmail(email);
    }

    @Override
    public Optional<Account> findAccountByUsernameOrEmail(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("标识符不能为空");
        }

        log.debug("根据用户名或邮箱查询账号，标识符：{}", identifier);

        // 先尝试用户名查询
        Optional<Account> accountOpt = accountRepository.findByUsername(identifier);
        if (accountOpt.isPresent()) {
            return accountOpt;
        }

        // 如果用户名没找到，尝试邮箱查询
        return accountRepository.findByEmail(identifier);
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        log.debug("检查用户名是否存在，用户名：{}", username);
        return accountRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }

        log.debug("检查邮箱是否存在，邮箱：{}", email);
        return accountRepository.existsByEmail(email);
    }

    // ==================== 数据访问方法（Session） ====================

    @Override
    public Session saveSession(Session session) {
        if (session == null) {
            throw new IllegalArgumentException("会话不能为空");
        }

        log.info("保存会话，会话ID：{}，用户ID：{}", session.getId(), session.getUserId());

        // 保存到数据库
        Session savedSession = sessionRepository.save(session);

        // 保存到缓存
        cacheSession(savedSession);

        log.info("会话保存成功，会话ID：{}", savedSession.getId());

        return savedSession;
    }
}
