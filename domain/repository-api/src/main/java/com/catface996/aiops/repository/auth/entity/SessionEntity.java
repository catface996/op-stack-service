package com.catface996.aiops.repository.auth.entity;

import java.time.LocalDateTime;

/**
 * 会话实体（用于数据库持久化）
 *
 * <p>与领域模型Session的映射关系由仓储实现层处理。</p>
 *
 * <p>数据库表：t_session</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 1.1, 1.4, 1.5: 会话存储</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-28
 */
public class SessionEntity {

    /**
     * 会话ID（UUID格式，主键）
     */
    private String id;

    /**
     * 用户ID（外键，关联t_account表）
     */
    private Long userId;

    /**
     * JWT令牌
     */
    private String token;

    /**
     * 设备信息（JSON格式存储）
     */
    private String deviceInfo;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后活动时间
     */
    private LocalDateTime lastActivityAt;

    /**
     * 过期时间（绝对超时）
     */
    private LocalDateTime expiresAt;

    /**
     * 绝对超时时长（秒）
     */
    private Integer absoluteTimeout;

    /**
     * 空闲超时时长（秒）
     */
    private Integer idleTimeout;

    /**
     * 是否启用记住我功能
     */
    private Boolean rememberMe;

    // ==================== Getters and Setters ====================

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

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Integer getAbsoluteTimeout() {
        return absoluteTimeout;
    }

    public void setAbsoluteTimeout(Integer absoluteTimeout) {
        this.absoluteTimeout = absoluteTimeout;
    }

    public Integer getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    @Override
    public String toString() {
        return "SessionEntity{" +
                "id='" + id + '\'' +
                ", userId=" + userId +
                ", createdAt=" + createdAt +
                ", lastActivityAt=" + lastActivityAt +
                ", expiresAt=" + expiresAt +
                ", absoluteTimeout=" + absoluteTimeout +
                ", idleTimeout=" + idleTimeout +
                ", rememberMe=" + rememberMe +
                '}';
    }
}
