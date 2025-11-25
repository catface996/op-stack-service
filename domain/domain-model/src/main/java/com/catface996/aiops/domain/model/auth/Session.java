package com.catface996.aiops.domain.model.auth;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

/**
 * 会话实体
 *
 * 用于维护用户登录状态
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
                isGetterVisibility = JsonAutoDetect.Visibility.NONE,
                fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Session {
    
    /**
     * 会话ID（UUID）
     */
    private String id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * JWT Token
     */
    private String token;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
    
    /**
     * 设备信息
     */
    private DeviceInfo deviceInfo;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    // 构造函数
    public Session() {
    }
    
    public Session(String id, Long userId, String token, LocalDateTime expiresAt, 
                   DeviceInfo deviceInfo, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.deviceInfo = deviceInfo;
        this.createdAt = createdAt;
    }
    
    // 业务方法
    
    /**
     * 判断会话是否已过期
     *
     * @return true if session is expired, false otherwise
     */
    @JsonIgnore
    public boolean isExpired() {
        if (expiresAt == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 判断会话是否有效
     *
     * 会话有效的条件：
     * 1. 会话未过期
     *
     * @return true if session is valid, false otherwise
     */
    @JsonIgnore
    public boolean isValid() {
        return !isExpired();
    }

    /**
     * 获取会话剩余有效时间（秒）
     *
     * @return remaining seconds, or 0 if expired
     */
    @JsonIgnore
    public long getRemainingSeconds() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
    }

    /**
     * 获取会话剩余有效时间（分钟）
     *
     * @return remaining minutes, or 0 if expired
     */
    @JsonIgnore
    public long getRemainingMinutes() {
        return getRemainingSeconds() / 60;
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "Session{" +
                "id='" + id + '\'' +
                ", userId=" + userId +
                ", expiresAt=" + expiresAt +
                ", deviceInfo=" + deviceInfo +
                ", createdAt=" + createdAt +
                '}';
    }
}
