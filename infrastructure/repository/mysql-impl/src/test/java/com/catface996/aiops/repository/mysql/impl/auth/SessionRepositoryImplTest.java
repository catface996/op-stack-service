package com.catface996.aiops.repository.mysql.impl.auth;

import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.api.model.auth.DeviceInfo;
import com.catface996.aiops.domain.api.model.auth.Session;
import com.catface996.aiops.repository.mysql.mapper.auth.SessionMapper;
import com.catface996.aiops.repository.mysql.po.auth.SessionPO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * SessionRepositoryImpl 单元测试
 * 
 * <p>测试会话仓储实现的所有方法</p>
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
@ExtendWith(MockitoExtension.class)
class SessionRepositoryImplTest {
    
    @Mock
    private SessionMapper sessionMapper;
    
    @InjectMocks
    private SessionRepositoryImpl sessionRepository;
    
    private SessionPO testSessionPO;
    private Session testSession;
    
    @BeforeEach
    void setUp() {
        // 准备测试数据
        testSessionPO = new SessionPO();
        testSessionPO.setId("session-uuid-123");
        testSessionPO.setUserId(1L);
        testSessionPO.setToken("jwt-token-123");
        testSessionPO.setExpiresAt(LocalDateTime.now().plusHours(2));
        testSessionPO.setDeviceInfo("{\"ip\":\"192.168.1.1\",\"userAgent\":\"Mozilla/5.0\"}");
        testSessionPO.setCreatedAt(LocalDateTime.now());
        
        testSession = new Session();
        testSession.setId("session-uuid-123");
        testSession.setUserId(1L);
        testSession.setToken("jwt-token-123");
        testSession.setExpiresAt(LocalDateTime.now().plusHours(2));
        
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setIpAddress("192.168.1.1");
        deviceInfo.setUserAgent("Mozilla/5.0");
        testSession.setDeviceInfo(deviceInfo);
        testSession.setCreatedAt(LocalDateTime.now());
    }
    
    @Test
    void testFindById_Success() {
        // Given
        when(sessionMapper.selectById("session-uuid-123")).thenReturn(testSessionPO);
        
        // When
        Optional<Session> result = sessionRepository.findById("session-uuid-123");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("session-uuid-123", result.get().getId());
        assertEquals(1L, result.get().getUserId());
        verify(sessionMapper, times(1)).selectById("session-uuid-123");
    }
    
    @Test
    void testFindById_NotFound() {
        // Given
        when(sessionMapper.selectById("nonexistent")).thenReturn(null);
        
        // When
        Optional<Session> result = sessionRepository.findById("nonexistent");
        
        // Then
        assertFalse(result.isPresent());
        verify(sessionMapper, times(1)).selectById("nonexistent");
    }
    
    @Test
    void testFindById_EmptyId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> sessionRepository.findById(""));
        verify(sessionMapper, never()).selectById(anyString());
    }
    
    @Test
    void testFindByUserId_Success() {
        // Given
        when(sessionMapper.selectByUserId(1L)).thenReturn(testSessionPO);
        
        // When
        Optional<Session> result = sessionRepository.findByUserId(1L);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getUserId());
        verify(sessionMapper, times(1)).selectByUserId(1L);
    }
    
    @Test
    void testFindByUserId_NotFound() {
        // Given
        when(sessionMapper.selectByUserId(999L)).thenReturn(null);
        
        // When
        Optional<Session> result = sessionRepository.findByUserId(999L);
        
        // Then
        assertFalse(result.isPresent());
        verify(sessionMapper, times(1)).selectByUserId(999L);
    }
    
    @Test
    void testSave_Insert() {
        // Given
        Session newSession = new Session();
        newSession.setId("new-session-uuid");
        newSession.setUserId(2L);
        newSession.setToken("new-jwt-token");
        newSession.setExpiresAt(LocalDateTime.now().plusHours(2));
        newSession.setCreatedAt(LocalDateTime.now());
        
        when(sessionMapper.selectById("new-session-uuid")).thenReturn(null, testSessionPO);
        when(sessionMapper.insert(any(SessionPO.class))).thenReturn(1);
        
        // When
        Session result = sessionRepository.save(newSession);
        
        // Then
        assertNotNull(result);
        verify(sessionMapper, times(1)).insert(any(SessionPO.class));
        verify(sessionMapper, times(2)).selectById(anyString());
    }
    
    @Test
    void testSave_Update() {
        // Given
        when(sessionMapper.selectById("session-uuid-123")).thenReturn(testSessionPO);
        when(sessionMapper.updateById(any(SessionPO.class))).thenReturn(1);
        
        // When
        Session result = sessionRepository.save(testSession);
        
        // Then
        assertNotNull(result);
        verify(sessionMapper, times(1)).updateById(any(SessionPO.class));
        verify(sessionMapper, times(2)).selectById("session-uuid-123");
    }
    
    @Test
    void testDeleteById() {
        // When
        sessionRepository.deleteById("session-uuid-123");
        
        // Then
        verify(sessionMapper, times(1)).deleteById("session-uuid-123");
    }
    
    @Test
    void testDeleteByUserId() {
        // When
        sessionRepository.deleteByUserId(1L);
        
        // Then
        verify(sessionMapper, times(1)).deleteByUserId(1L);
    }
    
    @Test
    void testExistsById_True() {
        // Given
        when(sessionMapper.selectById("session-uuid-123")).thenReturn(testSessionPO);
        
        // When
        boolean result = sessionRepository.existsById("session-uuid-123");
        
        // Then
        assertTrue(result);
        verify(sessionMapper, times(1)).selectById("session-uuid-123");
    }
    
    @Test
    void testExistsById_False() {
        // Given
        when(sessionMapper.selectById("nonexistent")).thenReturn(null);
        
        // When
        boolean result = sessionRepository.existsById("nonexistent");
        
        // Then
        assertFalse(result);
        verify(sessionMapper, times(1)).selectById("nonexistent");
    }
    
    @Test
    void testUpdateExpiresAt_Success() {
        // Given
        LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(30);
        when(sessionMapper.selectById("session-uuid-123")).thenReturn(testSessionPO);
        when(sessionMapper.updateById(any(SessionPO.class))).thenReturn(1);
        
        // When
        sessionRepository.updateExpiresAt("session-uuid-123", newExpiresAt);
        
        // Then
        verify(sessionMapper, times(1)).selectById("session-uuid-123");
        verify(sessionMapper, times(1)).updateById(any(SessionPO.class));
    }
    
    @Test
    void testUpdateExpiresAt_SessionNotFound() {
        // Given
        LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(30);
        when(sessionMapper.selectById("nonexistent")).thenReturn(null);
        
        // When & Then
        assertThrows(BusinessException.class,
            () -> sessionRepository.updateExpiresAt("nonexistent", newExpiresAt));
        verify(sessionMapper, times(1)).selectById("nonexistent");
        verify(sessionMapper, never()).updateById(any(SessionPO.class));
    }
    
    @Test
    void testDeleteExpiredSessions() {
        // Given
        when(sessionMapper.deleteExpiredSessions()).thenReturn(5);
        
        // When
        int result = sessionRepository.deleteExpiredSessions();
        
        // Then
        assertEquals(5, result);
        verify(sessionMapper, times(1)).deleteExpiredSessions();
    }
}
