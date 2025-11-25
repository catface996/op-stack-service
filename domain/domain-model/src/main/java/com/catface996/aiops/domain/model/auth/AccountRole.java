package com.catface996.aiops.domain.model.auth;

/**
 * 账号角色枚举
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
public enum AccountRole {
    /**
     * 普通用户
     */
    ROLE_USER("普通用户"),
    
    /**
     * 系统管理员
     */
    ROLE_ADMIN("系统管理员");
    
    private final String description;
    
    AccountRole(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
