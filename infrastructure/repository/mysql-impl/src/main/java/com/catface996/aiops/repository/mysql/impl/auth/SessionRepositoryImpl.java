package com.catface996.aiops.repository.mysql.impl.auth;

import com.catface996.aiops.common.enums.SessionErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.auth.DeviceInfo;
import com.catface996.aiops.domain.model.auth.Session;
import com.catface996.aiops.repository.auth.SessionRepository;
import com.catface996.aiops.repository.mysql.mapper.auth.SessionMapper;
import com.catface996.aiops.repository.mysql.po.auth.SessionPO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 会话仓储实现类
 *
 * <p>使用 MyBatis-Plus 实现会话数据访问（MySQL主存储）</p>
 * <p>采用Cache-Aside模式：MySQL作为主存储，Redis作为缓存层</p>
 * <p>负责领域对象与持久化对象之间的转换</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 1.1, 1.4, 1.5: 会话存储</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
@Repository
public class SessionRepositoryImpl implements SessionRepository {

    private static final Logger log = LoggerFactory.getLogger(SessionRepositoryImpl.class);

    private final SessionMapper sessionMapper;
    private final ObjectMapper objectMapper;

    public SessionRepositoryImpl(SessionMapper sessionMapper) {
        this.sessionMapper = sessionMapper;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    public Session save(Session session) {
        if (session == null) {
            throw new IllegalArgumentException("会话实体不能为null");
        }

        SessionPO po = toPO(session);

        if (sessionMapper.selectById(po.getId()) == null) {
            sessionMapper.insert(po);
            log.debug("Inserted new session: {}", po.getId());
        } else {
            sessionMapper.updateById(po);
            log.debug("Updated existing session: {}", po.getId());
        }

        SessionPO savedPO = sessionMapper.selectById(po.getId());
        return toEntity(savedPO);
    }

    @Override
    public Optional<Session> findById(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }

        SessionPO po = sessionMapper.selectById(sessionId);
        return Optional.ofNullable(toEntity(po));
    }

    @Override
    public Optional<Session> findByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为null");
        }

        SessionPO po = sessionMapper.selectByUserId(userId);
        return Optional.ofNullable(toEntity(po));
    }

    @Override
    public List<Session> findAllByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为null");
        }

        List<SessionPO> poList = sessionMapper.selectAllByUserId(userId);
        if (poList == null || poList.isEmpty()) {
            return Collections.emptyList();
        }

        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }

        sessionMapper.deleteById(sessionId);
        log.debug("Deleted session: {}", sessionId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为null");
        }

        int deleted = sessionMapper.deleteByUserId(userId);
        log.debug("Deleted {} sessions for user: {}", deleted, userId);
    }

    @Override
    public void batchDelete(List<String> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            throw new IllegalArgumentException("会话ID列表不能为空");
        }

        int deleted = sessionMapper.batchDeleteByIds(sessionIds);
        log.debug("Batch deleted {} sessions", deleted);
    }

    @Override
    public boolean existsById(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }

        return sessionMapper.selectById(sessionId) != null;
    }

    @Override
    public void updateExpiresAt(String sessionId, LocalDateTime expiresAt) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("过期时间不能为null");
        }

        SessionPO po = sessionMapper.selectById(sessionId);
        if (po == null) {
            throw new BusinessException(SessionErrorCode.SESSION_NOT_FOUND);
        }

        po.setExpiresAt(expiresAt);
        sessionMapper.updateById(po);
        log.debug("Updated expires_at for session: {}", sessionId);
    }

    @Override
    public int deleteExpiredSessions() {
        int deleted = sessionMapper.deleteExpiredSessions();
        log.info("Deleted {} expired sessions", deleted);
        return deleted;
    }

    @Override
    public int countByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为null");
        }

        return sessionMapper.countByUserId(userId);
    }

    /**
     * 将领域实体转换为持久化对象
     */
    private SessionPO toPO(Session entity) {
        if (entity == null) {
            return null;
        }

        SessionPO po = new SessionPO();
        po.setId(entity.getId());
        po.setUserId(entity.getUserId());
        po.setToken(entity.getToken());
        po.setExpiresAt(entity.getExpiresAt());
        po.setCreatedAt(entity.getCreatedAt());
        po.setLastActivityAt(entity.getLastActivityAt());
        po.setAbsoluteTimeout(entity.getAbsoluteTimeout());
        po.setIdleTimeout(entity.getIdleTimeout());
        po.setRememberMe(entity.isRememberMe());

        if (entity.getDeviceInfo() != null) {
            try {
                po.setDeviceInfo(objectMapper.writeValueAsString(entity.getDeviceInfo()));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize DeviceInfo to JSON", e);
                po.setDeviceInfo(null);
            }
        }

        return po;
    }

    /**
     * 将持久化对象转换为领域实体
     */
    private Session toEntity(SessionPO po) {
        if (po == null) {
            return null;
        }

        Session entity = new Session();
        entity.setId(po.getId());
        entity.setUserId(po.getUserId());
        entity.setToken(po.getToken());
        entity.setExpiresAt(po.getExpiresAt());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setLastActivityAt(po.getLastActivityAt());

        if (po.getAbsoluteTimeout() != null) {
            entity.setAbsoluteTimeout(po.getAbsoluteTimeout());
        }
        if (po.getIdleTimeout() != null) {
            entity.setIdleTimeout(po.getIdleTimeout());
        }
        if (po.getRememberMe() != null) {
            entity.setRememberMe(po.getRememberMe());
        }

        if (po.getDeviceInfo() != null && !po.getDeviceInfo().trim().isEmpty()) {
            try {
                entity.setDeviceInfo(objectMapper.readValue(po.getDeviceInfo(), DeviceInfo.class));
            } catch (JsonProcessingException e) {
                log.warn("Failed to deserialize DeviceInfo from JSON", e);
                entity.setDeviceInfo(null);
            }
        }

        return entity;
    }
}
