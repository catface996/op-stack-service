package com.catface996.aiops.domain.model.auth;

import java.time.LocalDateTime;

/**
 * 账号实体
 * 
 * 领域实体，包含账号的核心业务逻辑
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
public class Account {
    
    /**
     * 账号ID
     */
    private Long id;
    
    /**
     * 用户名（3-20个字符，字母数字下划线）
     */
    private String username;
    
    /**
     * 邮箱（最大100字符）
     */
    private String email;
    
    /**
     * 加密后的密码（BCrypt加密，60字符）
     */
    private String password;
    
    /**
     * 角色
     */
    private AccountRole role;
    
    /**
     * 账号状态
     */
    private AccountStatus status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    // 构造函数
    public Account() {
    }
    
    public Account(Long id, String username, String email, String password, 
                   AccountRole role, AccountStatus status, 
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // 业务方法
    
    /**
     * 判断账号是否处于活跃状态
     * 
     * @return true if account is active, false otherwise
     */
    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(this.status);
    }
    
    /**
     * 判断账号是否被锁定
     * 
     * @return true if account is locked, false otherwise
     */
    public boolean isLocked() {
        return AccountStatus.LOCKED.equals(this.status);
    }
    
    /**
     * 判断账号是否可以登录
     * 
     * 账号可以登录的条件：
     * 1. 账号状态为活跃（ACTIVE）
     * 2. 账号未被锁定
     * 
     * @return true if account can login, false otherwise
     */
    public boolean canLogin() {
        return isActive() && !isLocked();
    }
    
    /**
     * 判断账号是否被禁用
     * 
     * @return true if account is disabled, false otherwise
     */
    public boolean isDisabled() {
        return AccountStatus.DISABLED.equals(this.status);
    }
    
    /**
     * 判断账号是否为管理员
     * 
     * @return true if account is admin, false otherwise
     */
    public boolean isAdmin() {
        return AccountRole.ROLE_ADMIN.equals(this.role);
    }
    
    /**
     * 锁定账号
     */
    public void lock() {
        this.status = AccountStatus.LOCKED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 解锁账号
     */
    public void unlock() {
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 禁用账号
     */
    public void disable() {
        this.status = AccountStatus.DISABLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public AccountRole getRole() {
        return role;
    }
    
    public void setRole(AccountRole role) {
        this.role = role;
    }
    
    public AccountStatus getStatus() {
        return status;
    }
    
    public void setStatus(AccountStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
