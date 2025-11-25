package com.catface996.aiops.repository.mysql.impl.auth;

import com.catface996.aiops.common.enums.ResourceErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.api.model.auth.Account;
import com.catface996.aiops.domain.api.model.auth.AccountRole;
import com.catface996.aiops.domain.api.model.auth.AccountStatus;
import com.catface996.aiops.repository.auth.AccountRepository;
import com.catface996.aiops.repository.mysql.mapper.auth.AccountMapper;
import com.catface996.aiops.repository.mysql.po.auth.AccountPO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 账号仓储实现类
 * 
 * <p>使用 MyBatis-Plus 实现账号数据访问</p>
 * <p>负责领域对象与持久化对象之间的转换</p>
 * 
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-001: 用户名密码登录</li>
 *   <li>REQ-FR-002: 邮箱密码登录</li>
 *   <li>REQ-FR-003: 账号注册</li>
 *   <li>REQ-FR-005: 防暴力破解</li>
 *   <li>REQ-FR-006: 管理员手动解锁</li>
 * </ul>
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
@Repository
public class AccountRepositoryImpl implements AccountRepository {
    
    private final AccountMapper accountMapper;
    
    public AccountRepositoryImpl(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }
    
    @Override
    public Optional<Account> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("账号ID不能为null");
        }
        
        AccountPO po = accountMapper.selectById(id);
        return Optional.ofNullable(toEntity(po));
    }
    
    @Override
    public Optional<Account> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        
        AccountPO po = accountMapper.selectByUsername(username);
        return Optional.ofNullable(toEntity(po));
    }
    
    @Override
    public Optional<Account> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        
        AccountPO po = accountMapper.selectByEmail(email);
        return Optional.ofNullable(toEntity(po));
    }
    
    @Override
    public Account save(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("账号实体不能为null");
        }
        
        AccountPO po = toPO(account);
        
        try {
            if (account.getId() == null) {
                // 插入新账号
                accountMapper.insert(po);
            } else {
                // 更新现有账号
                accountMapper.updateById(po);
            }
            
            // 重新查询以获取完整数据（包括自动填充的字段）
            AccountPO savedPO = accountMapper.selectById(po.getId());
            return toEntity(savedPO);
            
        } catch (DuplicateKeyException e) {
            // 处理唯一约束冲突
            String message = e.getMessage();
            if (message != null && message.contains("uk_username")) {
                throw new BusinessException(ResourceErrorCode.USERNAME_CONFLICT);
            } else if (message != null && message.contains("uk_email")) {
                throw new BusinessException(ResourceErrorCode.EMAIL_CONFLICT);
            } else {
                throw new BusinessException(ResourceErrorCode.USERNAME_CONFLICT, "账号信息重复");
            }
        }
    }
    
    @Override
    public void updateStatus(Long id, AccountStatus status) {
        if (id == null) {
            throw new IllegalArgumentException("账号ID不能为null");
        }
        if (status == null) {
            throw new IllegalArgumentException("账号状态不能为null");
        }
        
        // 检查账号是否存在
        AccountPO po = accountMapper.selectById(id);
        if (po == null) {
            throw new BusinessException(ResourceErrorCode.ACCOUNT_NOT_FOUND);
        }
        
        // 更新状态
        po.setStatus(status.name());
        accountMapper.updateById(po);
    }
    
    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("账号ID不能为null");
        }
        
        accountMapper.deleteById(id);
    }
    
    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        
        return accountMapper.selectByUsername(username) != null;
    }
    
    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        
        return accountMapper.selectByEmail(email) != null;
    }
    
    /**
     * 将领域实体转换为持久化对象
     */
    private AccountPO toPO(Account entity) {
        if (entity == null) {
            return null;
        }
        
        AccountPO po = new AccountPO();
        po.setId(entity.getId());
        po.setUsername(entity.getUsername());
        po.setEmail(entity.getEmail());
        po.setPassword(entity.getPassword());
        po.setRole(entity.getRole() != null ? entity.getRole().name() : null);
        po.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        po.setCreatedAt(entity.getCreatedAt());
        po.setUpdatedAt(entity.getUpdatedAt());
        
        return po;
    }
    
    /**
     * 将持久化对象转换为领域实体
     */
    private Account toEntity(AccountPO po) {
        if (po == null) {
            return null;
        }
        
        Account entity = new Account();
        entity.setId(po.getId());
        entity.setUsername(po.getUsername());
        entity.setEmail(po.getEmail());
        entity.setPassword(po.getPassword());
        entity.setRole(po.getRole() != null ? AccountRole.valueOf(po.getRole()) : null);
        entity.setStatus(po.getStatus() != null ? AccountStatus.valueOf(po.getStatus()) : null);
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        
        return entity;
    }
}
