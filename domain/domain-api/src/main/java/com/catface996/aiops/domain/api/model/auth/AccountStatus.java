package com.catface996.aiops.domain.api.model.auth;

/**
 * 账号状态枚举
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
public enum AccountStatus {
    /**
     * 活跃状态
     */
    ACTIVE("活跃状态"),
    
    /**
     * 锁定状态
     */
    LOCKED("锁定状态"),
    
    /**
     * 禁用状态
     */
    DISABLED("禁用状态");
    
    private final String description;
    
    AccountStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
