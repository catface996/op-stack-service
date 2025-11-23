package com.catface996.aiops.domain.api.model.auth;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Account实体单元测试
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
class AccountEntityTest {
    
    @Test
    void testIsActive_whenStatusIsActive_shouldReturnTrue() {
        // Given
        Account account = new Account();
        account.setStatus(AccountStatus.ACTIVE);
        
        // When & Then
        assertTrue(account.isActive());
    }
    
    @Test
    void testIsActive_whenStatusIsLocked_shouldReturnFalse() {
        // Given
        Account account = new Account();
        account.setStatus(AccountStatus.LOCKED);
        
        // When & Then
        assertFalse(account.isActive());
    }
    
    @Test
    void testIsLocked_whenStatusIsLocked_shouldReturnTrue() {
        // Given
        Account account = new Account();
        account.setStatus(AccountStatus.LOCKED);
        
        // When & Then
        assertTrue(account.isLocked());
    }
    
    @Test
    void testIsLocked_whenStatusIsActive_shouldReturnFalse() {
        // Given
        Account account = new Account();
        account.setStatus(AccountStatus.ACTIVE);
        
        // When & Then
        assertFalse(account.isLocked());
    }
    
    @Test
    void testCanLogin_whenStatusIsActive_shouldReturnTrue() {
        // Given
        Account account = new Account();
        account.setStatus(AccountStatus.ACTIVE);
        
        // When & Then
        assertTrue(account.canLogin());
    }
    
    @Test
    void testCanLogin_whenStatusIsLocked_shouldReturnFalse() {
        // Given
        Account account = new Account();
        account.setStatus(AccountStatus.LOCKED);
        
        // When & Then
        assertFalse(account.canLogin());
    }
    
    @Test
    void testCanLogin_whenStatusIsDisabled_shouldReturnFalse() {
        // Given
        Account account = new Account();
        account.setStatus(AccountStatus.DISABLED);
        
        // When & Then
        assertFalse(account.canLogin());
    }
    
    @Test
    void testIsDisabled_whenStatusIsDisabled_shouldReturnTrue() {
        // Given
        Account account = new Account();
        account.setStatus(AccountStatus.DISABLED);
        
        // When & Then
        assertTrue(account.isDisabled());
    }
    
    @Test
    void testIsAdmin_whenRoleIsAdmin_shouldReturnTrue() {
        // Given
        Account account = new Account();
        account.setRole(AccountRole.ROLE_ADMIN);
        
        // When & Then
        assertTrue(account.isAdmin());
    }
    
    @Test
    void testIsAdmin_whenRoleIsUser_shouldReturnFalse() {
        // Given
        Account account = new Account();
        account.setRole(AccountRole.ROLE_USER);
        
        // When & Then
        assertFalse(account.isAdmin());
    }
    
    @Test
    void testLock_shouldChangeStatusToLocked() {
        // Given
        Account account = new Account();
        account.setStatus(AccountStatus.ACTIVE);
        LocalDateTime beforeLock = LocalDateTime.now();
        
        // When
        account.lock();
        
        // Then
        assertEquals(AccountStatus.LOCKED, account.getStatus());
        assertNotNull(account.getUpdatedAt());
        assertTrue(account.getUpdatedAt().isAfter(beforeLock) || 
                   account.getUpdatedAt().isEqual(beforeLock));
    }
    
    @Test
    void testUnlock_shouldChangeStatusToActive() {
        // Given
        Account account = new Account();
        account.setStatus(AccountStatus.LOCKED);
        LocalDateTime beforeUnlock = LocalDateTime.now();
        
        // When
        account.unlock();
        
        // Then
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertNotNull(account.getUpdatedAt());
        assertTrue(account.getUpdatedAt().isAfter(beforeUnlock) || 
                   account.getUpdatedAt().isEqual(beforeUnlock));
    }
    
    @Test
    void testDisable_shouldChangeStatusToDisabled() {
        // Given
        Account account = new Account();
        account.setStatus(AccountStatus.ACTIVE);
        LocalDateTime beforeDisable = LocalDateTime.now();
        
        // When
        account.disable();
        
        // Then
        assertEquals(AccountStatus.DISABLED, account.getStatus());
        assertNotNull(account.getUpdatedAt());
        assertTrue(account.getUpdatedAt().isAfter(beforeDisable) || 
                   account.getUpdatedAt().isEqual(beforeDisable));
    }
    
    @Test
    void testConstructorWithAllParameters() {
        // Given
        Long id = 1L;
        String username = "john_doe";
        String email = "john@example.com";
        String password = "encrypted_password";
        AccountRole role = AccountRole.ROLE_USER;
        AccountStatus status = AccountStatus.ACTIVE;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();
        
        // When
        Account account = new Account(id, username, email, password, role, status, createdAt, updatedAt);
        
        // Then
        assertEquals(id, account.getId());
        assertEquals(username, account.getUsername());
        assertEquals(email, account.getEmail());
        assertEquals(password, account.getPassword());
        assertEquals(role, account.getRole());
        assertEquals(status, account.getStatus());
        assertEquals(createdAt, account.getCreatedAt());
        assertEquals(updatedAt, account.getUpdatedAt());
    }
}
