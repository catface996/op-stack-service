package com.catface996.aiops.infrastructure.cache.redis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SessionCacheImpl 单元测试
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
@ExtendWith(MockitoExtension.class)
class SessionCacheImplTest {
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private ValueOperations<String, Object> valueOperations;
    
    private SessionCacheImpl sessionCache;
    
    @BeforeEach
    void setUp() {
        sessionCache = new SessionCacheImpl(redisTemplate);
    }
    
    @Test
    void testSave_Success() {
        // Given
        String sessionId = "session-123";
        String sessionData = "{\"userId\":1,\"username\":\"test\"}";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(2);
        Long userId = 1L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // When
        sessionCache.save(sessionId, sessionData, expiresAt, userId);
        
        // Then
        verify(valueOperations).set(eq("session:session-123"), eq(sessionData), anyLong(), eq(TimeUnit.SECONDS));
        verify(valueOperations).set(eq("session:user:1"), eq(sessionId), anyLong(), eq(TimeUnit.SECONDS));
    }
    
    @Test
    void testSave_ExpiredSession() {
        // Given
        String sessionId = "session-123";
        String sessionData = "{\"userId\":1,\"username\":\"test\"}";
        LocalDateTime expiresAt = LocalDateTime.now().minusHours(1); // 已过期
        Long userId = 1L;
        
        // When
        sessionCache.save(sessionId, sessionData, expiresAt, userId);
        
        // Then
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any(TimeUnit.class));
    }
    
    @Test
    void testSave_NullSessionId() {
        // Given
        String sessionData = "{\"userId\":1,\"username\":\"test\"}";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(2);
        Long userId = 1L;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> sessionCache.save(null, sessionData, expiresAt, userId));
    }
    
    @Test
    void testSave_RedisConnectionFailure() {
        // Given
        String sessionId = "session-123";
        String sessionData = "{\"userId\":1,\"username\":\"test\"}";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(2);
        Long userId = 1L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RedisConnectionFailureException("Redis连接失败"))
            .when(valueOperations).set(anyString(), any(), anyLong(), any(TimeUnit.class));
        
        // When & Then - 不应该抛出异常
        assertDoesNotThrow(() -> sessionCache.save(sessionId, sessionData, expiresAt, userId));
    }
    
    @Test
    void testGet_Exists() {
        // Given
        String sessionId = "session-123";
        String sessionData = "{\"userId\":1,\"username\":\"test\"}";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(sessionData);
        
        // When
        Optional<String> result = sessionCache.get(sessionId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(sessionData, result.get());
        verify(valueOperations).get("session:session-123");
    }
    
    @Test
    void testGet_NotExists() {
        // Given
        String sessionId = "session-123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        
        // When
        Optional<String> result = sessionCache.get(sessionId);
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void testGet_RedisConnectionFailure() {
        // Given
        String sessionId = "session-123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenThrow(new RedisConnectionFailureException("Redis连接失败"));
        
        // When
        Optional<String> result = sessionCache.get(sessionId);
        
        // Then
        assertFalse(result.isPresent()); // 降级返回空
    }
    
    @Test
    void testGetSessionIdByUserId_Exists() {
        // Given
        Long userId = 1L;
        String sessionId = "session-123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(sessionId);
        
        // When
        Optional<String> result = sessionCache.getSessionIdByUserId(userId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(sessionId, result.get());
        verify(valueOperations).get("session:user:1");
    }
    
    @Test
    void testGetSessionIdByUserId_NotExists() {
        // Given
        Long userId = 1L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        
        // When
        Optional<String> result = sessionCache.getSessionIdByUserId(userId);
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void testDelete() {
        // Given
        String sessionId = "session-123";
        when(redisTemplate.delete(anyString())).thenReturn(true);
        
        // When
        sessionCache.delete(sessionId);
        
        // Then
        verify(redisTemplate).delete("session:session-123");
    }
    
    @Test
    void testDelete_RedisConnectionFailure() {
        // Given
        String sessionId = "session-123";
        when(redisTemplate.delete(anyString())).thenThrow(new RedisConnectionFailureException("Redis连接失败"));
        
        // When & Then - 不应该抛出异常
        assertDoesNotThrow(() -> sessionCache.delete(sessionId));
    }
    
    @Test
    void testDeleteByUserId() {
        // Given
        Long userId = 1L;
        String sessionId = "session-123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:user:1")).thenReturn(sessionId);
        when(redisTemplate.delete(anyString())).thenReturn(true);
        
        // When
        sessionCache.deleteByUserId(userId);
        
        // Then
        verify(redisTemplate).delete("session:session-123");
        verify(redisTemplate).delete("session:user:1");
    }
    
    @Test
    void testDeleteByUserId_NoActiveSession() {
        // Given
        Long userId = 1L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:user:1")).thenReturn(null);
        
        // When
        sessionCache.deleteByUserId(userId);
        
        // Then
        verify(redisTemplate, never()).delete("session:session-123");
        verify(redisTemplate, never()).delete("session:user:1");
    }
    
    @Test
    void testExists_True() {
        // Given
        String sessionId = "session-123";
        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        
        // When
        boolean exists = sessionCache.exists(sessionId);
        
        // Then
        assertTrue(exists);
        verify(redisTemplate).hasKey("session:session-123");
    }
    
    @Test
    void testExists_False() {
        // Given
        String sessionId = "session-123";
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        
        // When
        boolean exists = sessionCache.exists(sessionId);
        
        // Then
        assertFalse(exists);
    }
    
    @Test
    void testUpdateExpiration() {
        // Given
        String sessionId = "session-123";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(2);
        when(redisTemplate.expire(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        
        // When
        sessionCache.updateExpiration(sessionId, expiresAt);
        
        // Then
        verify(redisTemplate).expire(eq("session:session-123"), anyLong(), eq(TimeUnit.SECONDS));
    }
    
    @Test
    void testUpdateExpiration_ExpiredTime() {
        // Given
        String sessionId = "session-123";
        LocalDateTime expiresAt = LocalDateTime.now().minusHours(1); // 已过期
        when(redisTemplate.delete(anyString())).thenReturn(true);
        
        // When
        sessionCache.updateExpiration(sessionId, expiresAt);
        
        // Then
        verify(redisTemplate).delete("session:session-123");
        verify(redisTemplate, never()).expire(anyString(), anyLong(), any(TimeUnit.class));
    }
    
    @Test
    void testGetRemainingTime() {
        // Given
        String sessionId = "session-123";
        when(redisTemplate.getExpire(anyString(), any(TimeUnit.class))).thenReturn(3600L);
        
        // When
        long remainingTime = sessionCache.getRemainingTime(sessionId);
        
        // Then
        assertEquals(3600L, remainingTime);
        verify(redisTemplate).getExpire("session:session-123", TimeUnit.SECONDS);
    }
    
    @Test
    void testGetRemainingTime_Expired() {
        // Given
        String sessionId = "session-123";
        when(redisTemplate.getExpire(anyString(), any(TimeUnit.class))).thenReturn(-1L);
        
        // When
        long remainingTime = sessionCache.getRemainingTime(sessionId);
        
        // Then
        assertEquals(0L, remainingTime);
    }
    
    @Test
    void testGetRemainingTime_RedisConnectionFailure() {
        // Given
        String sessionId = "session-123";
        when(redisTemplate.getExpire(anyString(), any(TimeUnit.class)))
            .thenThrow(new RedisConnectionFailureException("Redis连接失败"));
        
        // When
        long remainingTime = sessionCache.getRemainingTime(sessionId);
        
        // Then
        assertEquals(0L, remainingTime); // 降级返回0
    }
}
