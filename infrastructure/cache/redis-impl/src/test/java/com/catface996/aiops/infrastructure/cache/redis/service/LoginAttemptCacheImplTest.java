package com.catface996.aiops.infrastructure.cache.redis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LoginAttemptCacheImpl 单元测试
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
@ExtendWith(MockitoExtension.class)
class LoginAttemptCacheImplTest {
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private ValueOperations<String, Object> valueOperations;
    
    private LoginAttemptCacheImpl loginAttemptCache;
    
    @BeforeEach
    void setUp() {
        loginAttemptCache = new LoginAttemptCacheImpl(redisTemplate);
    }
    
    @Test
    void testRecordFailure_FirstTime() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        
        // When
        int count = loginAttemptCache.recordFailure(identifier);
        
        // Then
        assertEquals(1, count);
        verify(valueOperations).increment("login:fail:test_user");
        verify(redisTemplate).expire("login:fail:test_user", 1800L, TimeUnit.SECONDS);
    }
    
    @Test
    void testRecordFailure_SecondTime() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(2L);
        
        // When
        int count = loginAttemptCache.recordFailure(identifier);
        
        // Then
        assertEquals(2, count);
        verify(valueOperations).increment("login:fail:test_user");
        verify(redisTemplate, never()).expire(anyString(), anyLong(), any(TimeUnit.class));
    }
    
    @Test
    void testRecordFailure_RedisConnectionFailure() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenThrow(new RedisConnectionFailureException("Redis连接失败"));
        
        // When
        int count = loginAttemptCache.recordFailure(identifier);
        
        // Then
        assertEquals(0, count); // 降级返回0
    }
    
    @Test
    void testRecordFailure_NullIdentifier() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> loginAttemptCache.recordFailure(null));
    }
    
    @Test
    void testRecordFailure_EmptyIdentifier() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> loginAttemptCache.recordFailure(""));
    }
    
    @Test
    void testGetFailureCount_Exists() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(3);
        
        // When
        int count = loginAttemptCache.getFailureCount(identifier);
        
        // Then
        assertEquals(3, count);
        verify(valueOperations).get("login:fail:test_user");
    }
    
    @Test
    void testGetFailureCount_NotExists() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        
        // When
        int count = loginAttemptCache.getFailureCount(identifier);
        
        // Then
        assertEquals(0, count);
    }
    
    @Test
    void testGetFailureCount_LongValue() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(5L);
        
        // When
        int count = loginAttemptCache.getFailureCount(identifier);
        
        // Then
        assertEquals(5, count);
    }
    
    @Test
    void testGetFailureCount_StringValue() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("4");
        
        // When
        int count = loginAttemptCache.getFailureCount(identifier);
        
        // Then
        assertEquals(4, count);
    }
    
    @Test
    void testGetFailureCount_RedisConnectionFailure() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenThrow(new RedisConnectionFailureException("Redis连接失败"));
        
        // When
        int count = loginAttemptCache.getFailureCount(identifier);
        
        // Then
        assertEquals(0, count); // 降级返回0
    }
    
    @Test
    void testResetFailureCount() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.delete(anyString())).thenReturn(true);
        
        // When
        loginAttemptCache.resetFailureCount(identifier);
        
        // Then
        verify(redisTemplate).delete("login:fail:test_user");
    }
    
    @Test
    void testResetFailureCount_RedisConnectionFailure() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.delete(anyString())).thenThrow(new RedisConnectionFailureException("Redis连接失败"));
        
        // When & Then - 不应该抛出异常
        assertDoesNotThrow(() -> loginAttemptCache.resetFailureCount(identifier));
    }
    
    @Test
    void testIsLocked_True() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(5);
        
        // When
        boolean locked = loginAttemptCache.isLocked(identifier);
        
        // Then
        assertTrue(locked);
    }
    
    @Test
    void testIsLocked_False() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(3);
        
        // When
        boolean locked = loginAttemptCache.isLocked(identifier);
        
        // Then
        assertFalse(locked);
    }
    
    @Test
    void testIsLocked_NotExists() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        
        // When
        boolean locked = loginAttemptCache.isLocked(identifier);
        
        // Then
        assertFalse(locked);
    }
    
    @Test
    void testGetRemainingLockTime_Locked() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(5);
        when(redisTemplate.getExpire(anyString(), any(TimeUnit.class))).thenReturn(600L);
        
        // When
        long remainingTime = loginAttemptCache.getRemainingLockTime(identifier);
        
        // Then
        assertEquals(600L, remainingTime);
    }
    
    @Test
    void testGetRemainingLockTime_NotLocked() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(3);
        
        // When
        long remainingTime = loginAttemptCache.getRemainingLockTime(identifier);
        
        // Then
        assertEquals(0L, remainingTime);
    }
    
    @Test
    void testGetRemainingLockTime_RedisConnectionFailure() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(5);
        when(redisTemplate.getExpire(anyString(), any(TimeUnit.class)))
            .thenThrow(new RedisConnectionFailureException("Redis连接失败"));
        
        // When
        long remainingTime = loginAttemptCache.getRemainingLockTime(identifier);
        
        // Then
        assertEquals(1800L, remainingTime); // 降级返回默认锁定时间
    }
    
    @Test
    void testUnlock() {
        // Given
        String identifier = "test_user";
        when(redisTemplate.delete(anyString())).thenReturn(true);
        
        // When
        loginAttemptCache.unlock(identifier);
        
        // Then
        verify(redisTemplate).delete("login:fail:test_user");
    }
}
