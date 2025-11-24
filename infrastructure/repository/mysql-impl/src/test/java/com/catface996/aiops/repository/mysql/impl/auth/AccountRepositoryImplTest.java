package com.catface996.aiops.repository.mysql.impl.auth;

import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.api.model.auth.Account;
import com.catface996.aiops.domain.api.model.auth.AccountRole;
import com.catface996.aiops.domain.api.model.auth.AccountStatus;
import com.catface996.aiops.repository.mysql.mapper.auth.AccountMapper;
import com.catface996.aiops.repository.mysql.po.auth.AccountPO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AccountRepositoryImpl 单元测试
 * 
 * <p>测试账号仓储实现的所有方法</p>
 * 
 * <p>验证方法：</p>
 * <ul>
 *   <li>【单元测试】验证 findByUsername 能正确查询用户</li>
 *   <li>【单元测试】验证 save 方法支持新增和更新</li>
 *   <li>【单元测试】验证 Optional 正确处理空结果</li>
 * </ul>
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
@ExtendWith(MockitoExtension.class)
class AccountRepositoryImplTest {
    
    @Mock
    private AccountMapper accountMapper;
    
    @InjectMocks
    private AccountRepositoryImpl accountRepository;
    
    private AccountPO testAccountPO;
    private Account testAccount;
    
    @BeforeEach
    void setUp() {
        // 准备测试数据
        testAccountPO = new AccountPO();
        testAccountPO.setId(1L);
        testAccountPO.setUsername("john_doe");
        testAccountPO.setEmail("john@example.com");
        testAccountPO.setPassword("$2a$10$encrypted_password");
        testAccountPO.setRole("ROLE_USER");
        testAccountPO.setStatus("ACTIVE");
        testAccountPO.setCreatedAt(LocalDateTime.now());
        testAccountPO.setUpdatedAt(LocalDateTime.now());
        
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setUsername("john_doe");
        testAccount.setEmail("john@example.com");
        testAccount.setPassword("$2a$10$encrypted_password");
        testAccount.setRole(AccountRole.ROLE_USER);
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setCreatedAt(LocalDateTime.now());
        testAccount.setUpdatedAt(LocalDateTime.now());
    }
    
    @Test
    void testFindById_Success() {
        // Given
        when(accountMapper.selectById(1L)).thenReturn(testAccountPO);
        
        // When
        Optional<Account> result = accountRepository.findById(1L);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("john_doe", result.get().getUsername());
        assertEquals("john@example.com", result.get().getEmail());
        verify(accountMapper, times(1)).selectById(1L);
    }
    
    @Test
    void testFindById_NotFound() {
        // Given
        when(accountMapper.selectById(999L)).thenReturn(null);
        
        // When
        Optional<Account> result = accountRepository.findById(999L);
        
        // Then
        assertFalse(result.isPresent());
        verify(accountMapper, times(1)).selectById(999L);
    }
    
    @Test
    void testFindById_NullId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> accountRepository.findById(null));
        verify(accountMapper, never()).selectById(anyLong());
    }
    
    @Test
    void testFindByUsername_Success() {
        // Given
        when(accountMapper.selectByUsername("john_doe")).thenReturn(testAccountPO);
        
        // When
        Optional<Account> result = accountRepository.findByUsername("john_doe");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("john_doe", result.get().getUsername());
        assertEquals("john@example.com", result.get().getEmail());
        verify(accountMapper, times(1)).selectByUsername("john_doe");
    }
    
    @Test
    void testFindByUsername_NotFound() {
        // Given
        when(accountMapper.selectByUsername("nonexistent")).thenReturn(null);
        
        // When
        Optional<Account> result = accountRepository.findByUsername("nonexistent");
        
        // Then
        assertFalse(result.isPresent());
        verify(accountMapper, times(1)).selectByUsername("nonexistent");
    }
    
    @Test
    void testFindByUsername_EmptyUsername() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> accountRepository.findByUsername(""));
        verify(accountMapper, never()).selectByUsername(anyString());
    }
    
    @Test
    void testFindByEmail_Success() {
        // Given
        when(accountMapper.selectByEmail("john@example.com")).thenReturn(testAccountPO);
        
        // When
        Optional<Account> result = accountRepository.findByEmail("john@example.com");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("john_doe", result.get().getUsername());
        assertEquals("john@example.com", result.get().getEmail());
        verify(accountMapper, times(1)).selectByEmail("john@example.com");
    }
    
    @Test
    void testFindByEmail_NotFound() {
        // Given
        when(accountMapper.selectByEmail("nonexistent@example.com")).thenReturn(null);
        
        // When
        Optional<Account> result = accountRepository.findByEmail("nonexistent@example.com");
        
        // Then
        assertFalse(result.isPresent());
        verify(accountMapper, times(1)).selectByEmail("nonexistent@example.com");
    }
    
    @Test
    void testSave_Insert() {
        // Given
        Account newAccount = new Account();
        newAccount.setUsername("jane_doe");
        newAccount.setEmail("jane@example.com");
        newAccount.setPassword("$2a$10$encrypted_password");
        newAccount.setRole(AccountRole.ROLE_USER);
        newAccount.setStatus(AccountStatus.ACTIVE);
        
        when(accountMapper.insert(any(AccountPO.class))).thenAnswer(invocation -> {
            AccountPO po = invocation.getArgument(0);
            po.setId(2L);
            return 1;
        });
        when(accountMapper.selectById(2L)).thenReturn(testAccountPO);
        
        // When
        Account result = accountRepository.save(newAccount);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        verify(accountMapper, times(1)).insert(any(AccountPO.class));
        verify(accountMapper, times(1)).selectById(anyLong());
    }
    
    @Test
    void testSave_Update() {
        // Given
        testAccount.setEmail("newemail@example.com");
        
        when(accountMapper.updateById(any(AccountPO.class))).thenReturn(1);
        when(accountMapper.selectById(1L)).thenReturn(testAccountPO);
        
        // When
        Account result = accountRepository.save(testAccount);
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(accountMapper, times(1)).updateById(any(AccountPO.class));
        verify(accountMapper, times(1)).selectById(1L);
    }
    
    @Test
    void testSave_DuplicateUsername() {
        // Given
        Account newAccount = new Account();
        newAccount.setUsername("john_doe");
        newAccount.setEmail("different@example.com");
        newAccount.setPassword("$2a$10$encrypted_password");
        newAccount.setRole(AccountRole.ROLE_USER);
        newAccount.setStatus(AccountStatus.ACTIVE);
        
        when(accountMapper.insert(any(AccountPO.class)))
            .thenThrow(new DuplicateKeyException("Duplicate entry 'john_doe' for key 'uk_username'"));
        
        // When & Then
        assertThrows(BusinessException.class, () -> accountRepository.save(newAccount));
        verify(accountMapper, times(1)).insert(any(AccountPO.class));
    }
    
    @Test
    void testSave_DuplicateEmail() {
        // Given
        Account newAccount = new Account();
        newAccount.setUsername("different_user");
        newAccount.setEmail("john@example.com");
        newAccount.setPassword("$2a$10$encrypted_password");
        newAccount.setRole(AccountRole.ROLE_USER);
        newAccount.setStatus(AccountStatus.ACTIVE);
        
        when(accountMapper.insert(any(AccountPO.class)))
            .thenThrow(new DuplicateKeyException("Duplicate entry 'john@example.com' for key 'uk_email'"));
        
        // When & Then
        assertThrows(BusinessException.class, () -> accountRepository.save(newAccount));
        verify(accountMapper, times(1)).insert(any(AccountPO.class));
    }
    
    @Test
    void testUpdateStatus_Success() {
        // Given
        when(accountMapper.selectById(1L)).thenReturn(testAccountPO);
        when(accountMapper.updateById(any(AccountPO.class))).thenReturn(1);
        
        // When
        accountRepository.updateStatus(1L, AccountStatus.LOCKED);
        
        // Then
        verify(accountMapper, times(1)).selectById(1L);
        verify(accountMapper, times(1)).updateById(any(AccountPO.class));
    }
    
    @Test
    void testUpdateStatus_AccountNotFound() {
        // Given
        when(accountMapper.selectById(999L)).thenReturn(null);
        
        // When & Then
        assertThrows(BusinessException.class,
            () -> accountRepository.updateStatus(999L, AccountStatus.LOCKED));
        verify(accountMapper, times(1)).selectById(999L);
        verify(accountMapper, never()).updateById(any(AccountPO.class));
    }
    
    @Test
    void testDeleteById() {
        // When
        accountRepository.deleteById(1L);
        
        // Then
        verify(accountMapper, times(1)).deleteById(1L);
    }
    
    @Test
    void testExistsByUsername_True() {
        // Given
        when(accountMapper.selectByUsername("john_doe")).thenReturn(testAccountPO);
        
        // When
        boolean result = accountRepository.existsByUsername("john_doe");
        
        // Then
        assertTrue(result);
        verify(accountMapper, times(1)).selectByUsername("john_doe");
    }
    
    @Test
    void testExistsByUsername_False() {
        // Given
        when(accountMapper.selectByUsername("nonexistent")).thenReturn(null);
        
        // When
        boolean result = accountRepository.existsByUsername("nonexistent");
        
        // Then
        assertFalse(result);
        verify(accountMapper, times(1)).selectByUsername("nonexistent");
    }
    
    @Test
    void testExistsByEmail_True() {
        // Given
        when(accountMapper.selectByEmail("john@example.com")).thenReturn(testAccountPO);
        
        // When
        boolean result = accountRepository.existsByEmail("john@example.com");
        
        // Then
        assertTrue(result);
        verify(accountMapper, times(1)).selectByEmail("john@example.com");
    }
    
    @Test
    void testExistsByEmail_False() {
        // Given
        when(accountMapper.selectByEmail("nonexistent@example.com")).thenReturn(null);
        
        // When
        boolean result = accountRepository.existsByEmail("nonexistent@example.com");
        
        // Then
        assertFalse(result);
        verify(accountMapper, times(1)).selectByEmail("nonexistent@example.com");
    }
}
