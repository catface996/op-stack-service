package com.catface996.aiops.infrastructure.cache.redis.service;

import com.catface996.aiops.infrastructure.cache.api.service.LoginAttemptCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录失败计数缓存实现（Redis）
 * 
 * <p>使用Redis存储登录失败计数，支持降级到MySQL。</p>
 * 
 * <p>Redis Key格式：login:fail:{identifier}</p>
 * <p>Redis TTL：30分钟（1800秒）</p>
 * 
 * <p>降级策略：</p>
 * <ul>
 *   <li>Redis不可用时，记录警告日志</li>
 *   <li>返回默认值（0次失败）</li>
 *   <li>不阻塞主流程</li>
 * </ul>
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
@Slf4j
@Service
public class LoginAttemptCacheImpl implements LoginAttemptCache {
    
    private static final String KEY_PREFIX = "login:fail:";
    private static final int LOCK_THRESHOLD = 5;
    private static final long LOCK_DURATION_MINUTES = 30;
    private static final long LOCK_DURATION_SECONDS = LOCK_DURATION_MINUTES * 60;
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public LoginAttemptCacheImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public int recordFailure(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("identifier不能为空");
        }
        
        String key = KEY_PREFIX + identifier;
        
        try {
            // 增加失败计数
            Long count = redisTemplate.opsForValue().increment(key);
            
            if (count == null) {
                log.warn("Redis increment返回null，identifier: {}", identifier);
                return 0;
            }
            
            // 如果是第一次失败，设置过期时间
            if (count == 1) {
                redisTemplate.expire(key, LOCK_DURATION_SECONDS, TimeUnit.SECONDS);
                log.debug("设置登录失败计数过期时间，identifier: {}, TTL: {}秒", identifier, LOCK_DURATION_SECONDS);
            }
            
            log.info("记录登录失败，identifier: {}, 当前失败次数: {}", identifier, count);
            return count.intValue();
            
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法记录登录失败，identifier: {}", identifier, e);
            // 降级：返回0，不阻塞主流程
            return 0;
        } catch (Exception e) {
            log.error("记录登录失败异常，identifier: {}", identifier, e);
            // 降级：返回0，不阻塞主流程
            return 0;
        }
    }
    
    @Override
    public int getFailureCount(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("identifier不能为空");
        }
        
        String key = KEY_PREFIX + identifier;
        
        try {
            Object value = redisTemplate.opsForValue().get(key);
            
            if (value == null) {
                return 0;
            }
            
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof Long) {
                return ((Long) value).intValue();
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            } else {
                log.warn("Redis返回的失败次数类型异常，identifier: {}, type: {}", identifier, value.getClass());
                return 0;
            }
            
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法获取登录失败次数，identifier: {}", identifier, e);
            // 降级：返回0，不阻塞主流程
            return 0;
        } catch (Exception e) {
            log.error("获取登录失败次数异常，identifier: {}", identifier, e);
            // 降级：返回0，不阻塞主流程
            return 0;
        }
    }
    
    @Override
    public void resetFailureCount(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("identifier不能为空");
        }
        
        String key = KEY_PREFIX + identifier;
        
        try {
            Boolean deleted = redisTemplate.delete(key);
            log.info("重置登录失败计数，identifier: {}, deleted: {}", identifier, deleted);
            
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法重置登录失败计数，identifier: {}", identifier, e);
            // 降级：不阻塞主流程
        } catch (Exception e) {
            log.error("重置登录失败计数异常，identifier: {}", identifier, e);
            // 降级：不阻塞主流程
        }
    }
    
    @Override
    public boolean isLocked(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("identifier不能为空");
        }
        
        int failureCount = getFailureCount(identifier);
        return failureCount >= LOCK_THRESHOLD;
    }
    
    @Override
    public long getRemainingLockTime(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("identifier不能为空");
        }
        
        if (!isLocked(identifier)) {
            return 0;
        }
        
        String key = KEY_PREFIX + identifier;
        
        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            
            if (ttl == null || ttl < 0) {
                return 0;
            }
            
            return ttl;
            
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法获取锁定剩余时间，identifier: {}", identifier, e);
            // 降级：返回默认锁定时间
            return LOCK_DURATION_SECONDS;
        } catch (Exception e) {
            log.error("获取锁定剩余时间异常，identifier: {}", identifier, e);
            // 降级：返回默认锁定时间
            return LOCK_DURATION_SECONDS;
        }
    }
    
    @Override
    public void unlock(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("identifier不能为空");
        }
        
        resetFailureCount(identifier);
        log.info("手动解锁账号，identifier: {}", identifier);
    }
}
