package com.catface996.aiops.infrastructure.cache.redis.service;

import com.catface996.aiops.infrastructure.cache.api.service.SessionCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 会话缓存实现（Redis）
 * 
 * <p>使用Redis存储会话信息，支持降级到MySQL。</p>
 * 
 * <p>Redis Key格式：</p>
 * <ul>
 *   <li>会话数据：session:{sessionId}</li>
 *   <li>会话互斥：session:user:{userId}</li>
 * </ul>
 * 
 * <p>降级策略：</p>
 * <ul>
 *   <li>Redis不可用时，记录警告日志</li>
 *   <li>返回空值（Optional.empty()）</li>
 *   <li>不阻塞主流程</li>
 * </ul>
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
@Slf4j
@Service
public class SessionCacheImpl implements SessionCache {
    
    private static final String SESSION_KEY_PREFIX = "session:";
    private static final String USER_SESSION_KEY_PREFIX = "session:user:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public SessionCacheImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public void save(String sessionId, String sessionData, LocalDateTime expiresAt, Long userId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("sessionId不能为空");
        }
        if (sessionData == null) {
            throw new IllegalArgumentException("sessionData不能为空");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId不能为空");
        }
        
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        String userSessionKey = USER_SESSION_KEY_PREFIX + userId;
        
        try {
            // 计算TTL（秒）
            long ttlSeconds = Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
            
            if (ttlSeconds <= 0) {
                log.warn("会话已过期，不保存到Redis，sessionId: {}, expiresAt: {}", sessionId, expiresAt);
                return;
            }
            
            // 保存会话数据
            redisTemplate.opsForValue().set(sessionKey, sessionData, ttlSeconds, TimeUnit.SECONDS);
            
            // 保存会话互斥映射
            redisTemplate.opsForValue().set(userSessionKey, sessionId, ttlSeconds, TimeUnit.SECONDS);
            
            log.info("保存会话到Redis，sessionId: {}, userId: {}, TTL: {}秒", sessionId, userId, ttlSeconds);
            
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法保存会话，sessionId: {}", sessionId, e);
            // 降级：不阻塞主流程，由调用方处理MySQL降级
        } catch (Exception e) {
            log.error("保存会话到Redis异常，sessionId: {}", sessionId, e);
            // 降级：不阻塞主流程，由调用方处理MySQL降级
        }
    }
    
    @Override
    public Optional<String> get(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("sessionId不能为空");
        }
        
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        
        try {
            Object value = redisTemplate.opsForValue().get(sessionKey);
            
            if (value == null) {
                log.debug("会话不存在于Redis，sessionId: {}", sessionId);
                return Optional.empty();
            }
            
            if (value instanceof String) {
                return Optional.of((String) value);
            } else {
                log.warn("Redis返回的会话数据类型异常，sessionId: {}, type: {}", sessionId, value.getClass());
                return Optional.empty();
            }
            
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法获取会话，sessionId: {}", sessionId, e);
            // 降级：返回空，由调用方处理MySQL降级
            return Optional.empty();
        } catch (Exception e) {
            log.error("从Redis获取会话异常，sessionId: {}", sessionId, e);
            // 降级：返回空，由调用方处理MySQL降级
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<String> getSessionIdByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId不能为空");
        }
        
        String userSessionKey = USER_SESSION_KEY_PREFIX + userId;
        
        try {
            Object value = redisTemplate.opsForValue().get(userSessionKey);
            
            if (value == null) {
                log.debug("用户没有活跃会话，userId: {}", userId);
                return Optional.empty();
            }
            
            if (value instanceof String) {
                return Optional.of((String) value);
            } else {
                log.warn("Redis返回的会话ID类型异常，userId: {}, type: {}", userId, value.getClass());
                return Optional.empty();
            }
            
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法获取用户会话ID，userId: {}", userId, e);
            // 降级：返回空，由调用方处理MySQL降级
            return Optional.empty();
        } catch (Exception e) {
            log.error("从Redis获取用户会话ID异常，userId: {}", userId, e);
            // 降级：返回空，由调用方处理MySQL降级
            return Optional.empty();
        }
    }
    
    @Override
    public void delete(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("sessionId不能为空");
        }
        
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        
        try {
            Boolean deleted = redisTemplate.delete(sessionKey);
            log.info("从Redis删除会话，sessionId: {}, deleted: {}", sessionId, deleted);
            
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法删除会话，sessionId: {}", sessionId, e);
            // 降级：不阻塞主流程
        } catch (Exception e) {
            log.error("从Redis删除会话异常，sessionId: {}", sessionId, e);
            // 降级：不阻塞主流程
        }
    }
    
    @Override
    public void deleteByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId不能为空");
        }
        
        try {
            // 先获取用户的会话ID
            Optional<String> sessionIdOpt = getSessionIdByUserId(userId);
            
            if (sessionIdOpt.isPresent()) {
                String sessionId = sessionIdOpt.get();
                
                // 删除会话数据
                delete(sessionId);
                
                // 删除会话互斥映射
                String userSessionKey = USER_SESSION_KEY_PREFIX + userId;
                Boolean deleted = redisTemplate.delete(userSessionKey);
                
                log.info("从Redis删除用户会话，userId: {}, sessionId: {}, deleted: {}", userId, sessionId, deleted);
            } else {
                log.debug("用户没有活跃会话，无需删除，userId: {}", userId);
            }
            
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法删除用户会话，userId: {}", userId, e);
            // 降级：不阻塞主流程
        } catch (Exception e) {
            log.error("从Redis删除用户会话异常，userId: {}", userId, e);
            // 降级：不阻塞主流程
        }
    }
    
    @Override
    public boolean exists(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("sessionId不能为空");
        }
        
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        
        try {
            Boolean exists = redisTemplate.hasKey(sessionKey);
            return exists != null && exists;
            
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法检查会话是否存在，sessionId: {}", sessionId, e);
            // 降级：返回false，由调用方处理MySQL降级
            return false;
        } catch (Exception e) {
            log.error("检查会话是否存在异常，sessionId: {}", sessionId, e);
            // 降级：返回false，由调用方处理MySQL降级
            return false;
        }
    }
    
    @Override
    public void updateExpiration(String sessionId, LocalDateTime expiresAt) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("sessionId不能为空");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt不能为空");
        }
        
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        
        try {
            // 计算新的TTL（秒）
            long ttlSeconds = Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
            
            if (ttlSeconds <= 0) {
                log.warn("新的过期时间已过期，删除会话，sessionId: {}, expiresAt: {}", sessionId, expiresAt);
                delete(sessionId);
                return;
            }
            
            // 更新过期时间
            Boolean updated = redisTemplate.expire(sessionKey, ttlSeconds, TimeUnit.SECONDS);
            
            log.info("更新会话过期时间，sessionId: {}, newTTL: {}秒, updated: {}", sessionId, ttlSeconds, updated);
            
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法更新会话过期时间，sessionId: {}", sessionId, e);
            // 降级：不阻塞主流程
        } catch (Exception e) {
            log.error("更新会话过期时间异常，sessionId: {}", sessionId, e);
            // 降级：不阻塞主流程
        }
    }
    
    @Override
    public long getRemainingTime(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("sessionId不能为空");
        }
        
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        
        try {
            Long ttl = redisTemplate.getExpire(sessionKey, TimeUnit.SECONDS);
            
            if (ttl == null || ttl < 0) {
                return 0;
            }
            
            return ttl;
            
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法获取会话剩余时间，sessionId: {}", sessionId, e);
            // 降级：返回0
            return 0;
        } catch (Exception e) {
            log.error("获取会话剩余时间异常，sessionId: {}", sessionId, e);
            // 降级：返回0
            return 0;
        }
    }
}
