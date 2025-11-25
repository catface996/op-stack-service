package com.catface996.aiops.repository.mysql.impl.auth;

import com.catface996.aiops.common.enums.AuthErrorCode;
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
import java.util.Optional;

/**
 * 会话仓储实现类
 * 
 * <p>使用 MyBatis-Plus 实现会话数据访问（降级方案）</p>
 * <p>主存储使用 Redis，MySQL 作为降级方案</p>
 * <p>负责领域对象与持久化对象之间的转换</p>
 * 
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-007: 会话管理</li>
 *   <li>REQ-FR-009: 会话互斥</li>
 *   <li>REQ-FR-010: 安全退出</li>
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
    public Session save(Session session) {
        if (session == null) {
            throw new IllegalArgumentException("会话实体不能为null");
        }
        
        SessionPO po = toPO(session);
        
        if (sessionMapper.selectById(po.getId()) == null) {
            // 插入新会话
            sessionMapper.insert(po);
        } else {
            // 更新现有会话
            sessionMapper.updateById(po);
        }
        
        // 重新查询以获取完整数据
        SessionPO savedPO = sessionMapper.selectById(po.getId());
        return toEntity(savedPO);
    }
    
    @Override
    public void deleteById(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("会话ID不能为空");
        }
        
        sessionMapper.deleteById(sessionId);
    }
    
    @Override
    public void deleteByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为null");
        }
        
        sessionMapper.deleteByUserId(userId);
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
            throw new BusinessException(AuthErrorCode.SESSION_NOT_FOUND);
        }
        
        po.setExpiresAt(expiresAt);
        sessionMapper.updateById(po);
    }
    
    @Override
    public int deleteExpiredSessions() {
        return sessionMapper.deleteExpiredSessions();
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
        
        // 将 DeviceInfo 对象序列化为 JSON 字符串
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
        
        // 将 JSON 字符串反序列化为 DeviceInfo 对象
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
