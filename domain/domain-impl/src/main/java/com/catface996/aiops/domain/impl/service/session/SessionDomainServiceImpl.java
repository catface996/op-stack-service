package com.catface996.aiops.domain.impl.service.session;

import com.catface996.aiops.common.enums.SessionErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.api.service.session.SessionDomainService;
import com.catface996.aiops.domain.model.auth.DeviceInfo;
import com.catface996.aiops.domain.model.auth.Session;
import com.catface996.aiops.domain.model.auth.SessionValidationResult;
import com.catface996.aiops.infrastructure.cache.api.service.SessionCacheService;
import com.catface996.aiops.repository.auth.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 会话领域服务实现类
 *
 * <p>实现会话管理的核心业务逻辑，包括：</p>
 * <ul>
 *   <li>会话创建（生成UUID标识符、清理超限会话、先写MySQL再写Redis）</li>
 *   <li>会话验证（先读Redis缓存、缓存未命中则读MySQL、检查超时、更新活动时间）</li>
 *   <li>会话销毁（先删MySQL再删Redis）</li>
 *   <li>会话数量限制和IP变化检测</li>
 *   <li>Redis故障时的降级处理</li>
 * </ul>
 *
 * <p>存储策略（Cache-Aside模式）：</p>
 * <ul>
 *   <li>主存储：MySQL（提供数据持久化和可靠性保证）</li>
 *   <li>缓存层：Redis（提供高性能读取能力）</li>
 *   <li>写操作：先写MySQL，再更新Redis</li>
 *   <li>读操作：先读Redis，未命中则读MySQL并回写Redis</li>
 *   <li>删除操作：先删MySQL，再删Redis</li>
 * </ul>
 *
 * <p>异常处理策略：</p>
 * <ul>
 *   <li>Redis操作失败不影响主流程，仅记录警告日志</li>
 *   <li>MySQL操作失败抛出异常</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 1.1, 1.2, 1.3, 1.4, 1.5: 会话生命周期管理</li>
 *   <li>REQ 3.1, 3.2, 3.3, 3.4, 3.5, 3.6: 安全和降级策略</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-28
 */
@Slf4j
@Service
public class SessionDomainServiceImpl implements SessionDomainService {

    /**
     * 默认最大会话数
     */
    private static final int DEFAULT_MAX_SESSIONS = 5;

    private final SessionRepository sessionRepository;
    private final SessionCacheService sessionCacheService;

    public SessionDomainServiceImpl(SessionRepository sessionRepository,
                                    SessionCacheService sessionCacheService) {
        this.sessionRepository = sessionRepository;
        this.sessionCacheService = sessionCacheService;
    }

    @Override
    public Session createSession(Long userId, DeviceInfo deviceInfo,
                                 int absoluteTimeout, int idleTimeout, boolean rememberMe) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (deviceInfo == null) {
            throw new IllegalArgumentException("设备信息不能为空");
        }

        log.info("创建会话，用户ID：{}，设备IP：{}，绝对超时：{}秒，空闲超时：{}秒，记住我：{}",
                userId, deviceInfo.getIpAddress(), absoluteTimeout, idleTimeout, rememberMe);

        // 1. 检查并清理超限会话
        int cleanedCount = enforceSessionLimit(userId, DEFAULT_MAX_SESSIONS);
        if (cleanedCount > 0) {
            log.info("清理超限会话，用户ID：{}，清理数量：{}", userId, cleanedCount);
        }

        // 2. 生成UUID作为会话标识符
        String sessionId = UUID.randomUUID().toString();

        // 3. 计算过期时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(absoluteTimeout);

        // 4. 创建会话实体
        Session session = new Session(
                sessionId,
                userId,
                null,  // token由上层服务生成
                expiresAt,
                deviceInfo,
                now,
                absoluteTimeout,
                idleTimeout,
                rememberMe
        );

        // 5. 先写MySQL（主存储）
        Session savedSession = sessionRepository.save(session);
        log.debug("会话已保存到MySQL，会话ID：{}", sessionId);

        // 6. 再写Redis（缓存层，失败不阻塞主流程）
        try {
            sessionCacheService.cacheSession(savedSession, absoluteTimeout);
            sessionCacheService.addUserSession(userId, sessionId);
            log.debug("会话已缓存到Redis，会话ID：{}", sessionId);
        } catch (Exception e) {
            log.warn("缓存会话到Redis失败，会话ID：{}，错误：{}", sessionId, e.getMessage());
        }

        log.info("会话创建成功，会话ID：{}，用户ID：{}，过期时间：{}", sessionId, userId, expiresAt);
        return savedSession;
    }

    @Override
    public SessionValidationResult validateAndRefreshSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }

        log.debug("验证会话，会话ID：{}", sessionId);

        // 1. 先读Redis缓存
        Session session = null;
        boolean fromCache = false;
        try {
            session = sessionCacheService.getCachedSession(sessionId);
            if (session != null) {
                fromCache = true;
                log.debug("从Redis缓存获取会话，会话ID：{}", sessionId);
            }
        } catch (Exception e) {
            log.warn("从Redis获取会话失败，降级到MySQL，会话ID：{}，错误：{}", sessionId, e.getMessage());
        }

        // 2. 缓存未命中则读MySQL
        if (session == null) {
            Optional<Session> sessionOpt = sessionRepository.findById(sessionId);
            if (sessionOpt.isEmpty()) {
                log.warn("会话不存在，会话ID：{}", sessionId);
                throw new BusinessException(SessionErrorCode.SESSION_NOT_FOUND);
            }
            session = sessionOpt.get();
            log.debug("从MySQL获取会话，会话ID：{}", sessionId);

            // 3. 回写Redis缓存
            try {
                long ttlSeconds = session.getRemainingSeconds();
                if (ttlSeconds > 0) {
                    sessionCacheService.cacheSession(session, ttlSeconds);
                    log.debug("会话已回写到Redis缓存，会话ID：{}，TTL：{}秒", sessionId, ttlSeconds);
                }
            } catch (Exception e) {
                log.warn("回写Redis缓存失败，会话ID：{}，错误：{}", sessionId, e.getMessage());
            }
        }

        // 4. 检查绝对超时
        if (session.isExpired()) {
            log.warn("会话已过期（绝对超时），会话ID：{}，过期时间：{}", sessionId, session.getExpiresAt());
            // 删除过期会话
            destroySession(sessionId);
            throw new BusinessException(SessionErrorCode.SESSION_EXPIRED);
        }

        // 5. 检查空闲超时
        if (session.isIdleTimeout()) {
            log.warn("会话空闲超时，会话ID：{}，最后活动时间：{}", sessionId, session.getLastActivityAt());
            // 删除过期会话
            destroySession(sessionId);
            throw new BusinessException(SessionErrorCode.SESSION_IDLE_TIMEOUT);
        }

        // 6. 更新最后活动时间
        session.updateLastActivity();

        // 7. 更新存储
        try {
            sessionRepository.save(session);
            log.debug("最后活动时间已更新到MySQL，会话ID：{}", sessionId);
        } catch (Exception e) {
            log.warn("更新MySQL最后活动时间失败，会话ID：{}，错误：{}", sessionId, e.getMessage());
        }

        try {
            long ttlSeconds = session.getRemainingSeconds();
            sessionCacheService.cacheSession(session, ttlSeconds);
            log.debug("最后活动时间已更新到Redis，会话ID：{}", sessionId);
        } catch (Exception e) {
            log.warn("更新Redis会话失败，会话ID：{}，错误：{}", sessionId, e.getMessage());
        }

        log.info("会话验证成功，会话ID：{}，用户ID：{}，剩余时间：{}秒",
                sessionId, session.getUserId(), session.getRemainingSeconds());

        return SessionValidationResult.success(session);
    }

    @Override
    public void destroySession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }

        log.info("销毁会话，会话ID：{}", sessionId);

        // 获取会话信息（用于删除用户会话列表）
        Long userId = null;
        try {
            Optional<Session> sessionOpt = sessionRepository.findById(sessionId);
            if (sessionOpt.isPresent()) {
                userId = sessionOpt.get().getUserId();
            }
        } catch (Exception e) {
            log.warn("获取会话信息失败，会话ID：{}，错误：{}", sessionId, e.getMessage());
        }

        // 1. 先删MySQL
        try {
            sessionRepository.deleteById(sessionId);
            log.debug("会话已从MySQL删除，会话ID：{}", sessionId);
        } catch (Exception e) {
            log.error("从MySQL删除会话失败，会话ID：{}，错误：{}", sessionId, e.getMessage());
            // MySQL删除失败应该抛出异常
            throw e;
        }

        // 2. 再删Redis（失败不阻塞主流程）
        try {
            sessionCacheService.evictSession(sessionId);
            if (userId != null) {
                sessionCacheService.removeUserSession(userId, sessionId);
            }
            log.debug("会话已从Redis删除，会话ID：{}", sessionId);
        } catch (Exception e) {
            log.warn("从Redis删除会话失败，会话ID：{}，错误：{}", sessionId, e.getMessage());
        }

        log.info("会话销毁成功，会话ID：{}", sessionId);
    }

    @Override
    public List<Session> findUserSessions(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        log.debug("查询用户所有会话，用户ID：{}", userId);

        List<Session> sessions = sessionRepository.findAllByUserId(userId);

        // 按创建时间倒序排列
        sessions = sessions.stream()
                .sorted(Comparator.comparing(Session::getCreatedAt).reversed())
                .collect(Collectors.toList());

        log.debug("查询到用户会话，用户ID：{}，会话数量：{}", userId, sessions.size());
        return sessions;
    }

    @Override
    public int enforceSessionLimit(Long userId, int maxSessions) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (maxSessions <= 0) {
            throw new IllegalArgumentException("最大会话数必须大于0");
        }

        log.debug("检查会话数量限制，用户ID：{}，最大会话数：{}", userId, maxSessions);

        // 获取用户所有会话
        List<Session> sessions = sessionRepository.findAllByUserId(userId);
        int currentCount = sessions.size();

        // 计算需要清理的会话数量（减1是为新会话预留位置）
        int needToClean = currentCount - maxSessions + 1;
        if (needToClean <= 0) {
            log.debug("会话数量未超限，用户ID：{}，当前数量：{}，最大数量：{}",
                    userId, currentCount, maxSessions);
            return 0;
        }

        // 按创建时间排序，清理最旧的会话
        List<String> sessionsToClean = sessions.stream()
                .sorted(Comparator.comparing(Session::getCreatedAt))
                .limit(needToClean)
                .map(Session::getId)
                .collect(Collectors.toList());

        log.info("清理超限会话，用户ID：{}，当前数量：{}，需清理：{}",
                userId, currentCount, sessionsToClean.size());

        // 批量删除会话
        for (String sessionId : sessionsToClean) {
            try {
                destroySession(sessionId);
            } catch (Exception e) {
                log.warn("清理会话失败，会话ID：{}，错误：{}", sessionId, e.getMessage());
            }
        }

        return sessionsToClean.size();
    }

    @Override
    public boolean checkIpChange(Session session, String currentIp) {
        if (session == null) {
            throw new IllegalArgumentException("会话不能为空");
        }
        if (currentIp == null || currentIp.isEmpty()) {
            throw new IllegalArgumentException("当前IP不能为空");
        }

        DeviceInfo deviceInfo = session.getDeviceInfo();
        if (deviceInfo == null || deviceInfo.getIpAddress() == null) {
            log.warn("会话设备信息为空，无法检测IP变化，会话ID：{}", session.getId());
            return false;
        }

        String originalIp = deviceInfo.getIpAddress();
        boolean ipChanged = !originalIp.equals(currentIp);

        if (ipChanged) {
            log.warn("检测到IP地址变化，会话ID：{}，原IP：{}，当前IP：{}",
                    session.getId(), originalIp, currentIp);
        }

        return ipChanged;
    }

    @Override
    public int terminateOtherSessions(String currentSessionId, Long userId) {
        if (currentSessionId == null || currentSessionId.isEmpty()) {
            throw new IllegalArgumentException("当前会话ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        log.info("终止其他会话，当前会话ID：{}，用户ID：{}", currentSessionId, userId);

        // 获取用户所有会话
        List<Session> sessions = sessionRepository.findAllByUserId(userId);

        // 过滤出需要终止的会话（排除当前会话）
        List<String> sessionsToTerminate = sessions.stream()
                .map(Session::getId)
                .filter(id -> !id.equals(currentSessionId))
                .collect(Collectors.toList());

        log.info("需要终止的会话数量：{}", sessionsToTerminate.size());

        // 终止会话
        int terminatedCount = 0;
        for (String sessionId : sessionsToTerminate) {
            try {
                destroySession(sessionId);
                terminatedCount++;
            } catch (Exception e) {
                log.warn("终止会话失败，会话ID：{}，错误：{}", sessionId, e.getMessage());
            }
        }

        log.info("其他会话终止完成，终止数量：{}", terminatedCount);
        return terminatedCount;
    }

    @Override
    public void updateLastActivity(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }

        log.debug("更新会话活动时间，会话ID：{}", sessionId);

        // 从MySQL获取会话
        Optional<Session> sessionOpt = sessionRepository.findById(sessionId);
        if (sessionOpt.isEmpty()) {
            log.warn("会话不存在，无法更新活动时间，会话ID：{}", sessionId);
            return;
        }

        Session session = sessionOpt.get();
        session.updateLastActivity();

        // 更新MySQL
        try {
            sessionRepository.save(session);
            log.debug("活动时间已更新到MySQL，会话ID：{}", sessionId);
        } catch (Exception e) {
            log.warn("更新MySQL活动时间失败，会话ID：{}，错误：{}", sessionId, e.getMessage());
        }

        // 更新Redis
        try {
            long ttlSeconds = session.getRemainingSeconds();
            sessionCacheService.cacheSession(session, ttlSeconds);
            log.debug("活动时间已更新到Redis，会话ID：{}", sessionId);
        } catch (Exception e) {
            log.warn("更新Redis活动时间失败，会话ID：{}，错误：{}", sessionId, e.getMessage());
        }
    }
}
