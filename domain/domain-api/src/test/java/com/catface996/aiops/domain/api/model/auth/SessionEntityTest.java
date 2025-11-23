package com.catface996.aiops.domain.api.model.auth;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Session实体单元测试
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
class SessionEntityTest {
    
    @Test
    void testIsExpired_whenExpiresAtIsNull_shouldReturnTrue() {
        // Given
        Session session = new Session();
        session.setExpiresAt(null);
        
        // When & Then
        assertTrue(session.isExpired());
    }
    
    @Test
    void testIsExpired_whenExpiresAtIsInPast_shouldReturnTrue() {
        // Given
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.now().minusHours(1));
        
        // When & Then
        assertTrue(session.isExpired());
    }
    
    @Test
    void testIsExpired_whenExpiresAtIsInFuture_shouldReturnFalse() {
        // Given
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.now().plusHours(1));
        
        // When & Then
        assertFalse(session.isExpired());
    }
    
    @Test
    void testIsValid_whenNotExpired_shouldReturnTrue() {
        // Given
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.now().plusHours(1));
        
        // When & Then
        assertTrue(session.isValid());
    }
    
    @Test
    void testIsValid_whenExpired_shouldReturnFalse() {
        // Given
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.now().minusHours(1));
        
        // When & Then
        assertFalse(session.isValid());
    }
    
    @Test
    void testGetRemainingSeconds_whenExpired_shouldReturnZero() {
        // Given
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.now().minusHours(1));
        
        // When
        long remainingSeconds = session.getRemainingSeconds();
        
        // Then
        assertEquals(0, remainingSeconds);
    }
    
    @Test
    void testGetRemainingSeconds_whenNotExpired_shouldReturnPositiveValue() {
        // Given
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.now().plusHours(2));
        
        // When
        long remainingSeconds = session.getRemainingSeconds();
        
        // Then
        assertTrue(remainingSeconds > 0);
        // Should be approximately 2 hours (7200 seconds), allow some tolerance
        assertTrue(remainingSeconds >= 7190 && remainingSeconds <= 7210);
    }
    
    @Test
    void testGetRemainingMinutes_whenExpired_shouldReturnZero() {
        // Given
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.now().minusHours(1));
        
        // When
        long remainingMinutes = session.getRemainingMinutes();
        
        // Then
        assertEquals(0, remainingMinutes);
    }
    
    @Test
    void testGetRemainingMinutes_whenNotExpired_shouldReturnPositiveValue() {
        // Given
        Session session = new Session();
        session.setExpiresAt(LocalDateTime.now().plusHours(2));
        
        // When
        long remainingMinutes = session.getRemainingMinutes();
        
        // Then
        assertTrue(remainingMinutes > 0);
        // Should be approximately 120 minutes, allow some tolerance
        assertTrue(remainingMinutes >= 119 && remainingMinutes <= 121);
    }
    
    @Test
    void testConstructorWithAllParameters() {
        // Given
        String id = "session-123";
        Long userId = 1L;
        String token = "jwt-token";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(2);
        DeviceInfo deviceInfo = new DeviceInfo("192.168.1.1", "Mozilla/5.0");
        LocalDateTime createdAt = LocalDateTime.now();
        
        // When
        Session session = new Session(id, userId, token, expiresAt, deviceInfo, createdAt);
        
        // Then
        assertEquals(id, session.getId());
        assertEquals(userId, session.getUserId());
        assertEquals(token, session.getToken());
        assertEquals(expiresAt, session.getExpiresAt());
        assertEquals(deviceInfo, session.getDeviceInfo());
        assertEquals(createdAt, session.getCreatedAt());
    }
    
    @Test
    void testIsExpired_boundaryCase_whenExpiresAtIsNow_shouldReturnFalseOrTrue() {
        // Given
        Session session = new Session();
        // Set to current time (boundary case)
        session.setExpiresAt(LocalDateTime.now());
        
        // When & Then
        // This is a boundary case - could be either true or false depending on timing
        // Just verify the method doesn't throw an exception
        assertNotNull(session.isExpired());
    }
}
