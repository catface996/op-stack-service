package com.catface996.aiops.domain.model.auth;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 会话聚合根
 *
 * <p>用于维护用户登录状态，是会话管理功能的核心实体。</p>
 *
 * <p>会话属性包括：</p>
 * <ul>
 *   <li>会话标识符（UUID）</li>
 *   <li>用户ID</li>
 *   <li>设备信息</li>
 *   <li>创建时间和最后活动时间</li>
 *   <li>过期时间（绝对超时）</li>
 *   <li>空闲超时时长</li>
 *   <li>记住我标志</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 1.1, 1.2, 1.3, 1.4: 会话生命周期管理</li>
 * </ul>
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
     * 过期时间（绝对超时）
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

    /**
     * 最后活动时间
     */
    private LocalDateTime lastActivityAt;

    /**
     * 绝对超时时长（秒）
     * 默认8小时 = 28800秒
     */
    private int absoluteTimeout = 28800;

    /**
     * 空闲超时时长（秒）
     * 默认30分钟 = 1800秒
     */
    private int idleTimeout = 1800;

    /**
     * 是否启用记住我功能
     */
    private boolean rememberMe;

    /**
     * 即将过期警告阈值（秒）
     * 默认5分钟 = 300秒
     */
    private static final int WARNING_THRESHOLD_SECONDS = 300;

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
        this.lastActivityAt = createdAt;
    }

    /**
     * 完整构造函数
     *
     * @param id 会话ID
     * @param userId 用户ID
     * @param token JWT令牌
     * @param expiresAt 过期时间
     * @param deviceInfo 设备信息
     * @param createdAt 创建时间
     * @param absoluteTimeout 绝对超时时长（秒）
     * @param idleTimeout 空闲超时时长（秒）
     * @param rememberMe 是否记住我
     */
    public Session(String id, Long userId, String token, LocalDateTime expiresAt,
                   DeviceInfo deviceInfo, LocalDateTime createdAt,
                   int absoluteTimeout, int idleTimeout, boolean rememberMe) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.deviceInfo = deviceInfo;
        this.createdAt = createdAt;
        this.lastActivityAt = createdAt;
        this.absoluteTimeout = absoluteTimeout;
        this.idleTimeout = idleTimeout;
        this.rememberMe = rememberMe;
    }

    // ==================== 业务方法 ====================

    /**
     * 判断会话是否已过期（绝对超时）
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
     * 判断会话是否空闲超时
     *
     * <p>如果启用了记住我功能，则不检查空闲超时。</p>
     *
     * @return true if session is idle timeout, false otherwise
     */
    @JsonIgnore
    public boolean isIdleTimeout() {
        if (rememberMe) {
            return false;
        }
        if (lastActivityAt == null) {
            return true;
        }
        LocalDateTime idleExpireTime = lastActivityAt.plusSeconds(idleTimeout);
        return LocalDateTime.now().isAfter(idleExpireTime);
    }

    /**
     * 判断会话是否有效
     *
     * <p>会话有效的条件：</p>
     * <ul>
     *   <li>会话未过期（绝对超时）</li>
     *   <li>会话未空闲超时</li>
     * </ul>
     *
     * @return true if session is valid, false otherwise
     */
    @JsonIgnore
    public boolean isValid() {
        return !isExpired() && !isIdleTimeout();
    }

    /**
     * 更新最后活动时间
     *
     * <p>每次验证会话时调用，用于重置空闲超时计时器。</p>
     */
    public void updateLastActivity() {
        this.lastActivityAt = LocalDateTime.now();
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
        return Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
    }

    /**
     * 获取会话剩余有效时间（整数秒）
     *
     * @return remaining seconds as int, or 0 if expired
     */
    @JsonIgnore
    public int getRemainingTime() {
        long remaining = getRemainingSeconds();
        return (int) Math.max(0, remaining);
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

    /**
     * 检查会话是否即将过期
     *
     * <p>剩余时间小于5分钟且大于0时返回true。</p>
     *
     * @return true if session is about to expire
     */
    @JsonIgnore
    public boolean isAboutToExpire() {
        int remaining = getRemainingTime();
        return remaining < WARNING_THRESHOLD_SECONDS && remaining > 0;
    }

    /**
     * 获取空闲剩余时间（秒）
     *
     * @return remaining idle seconds, or 0 if idle timeout
     */
    @JsonIgnore
    public long getIdleRemainingSeconds() {
        if (rememberMe || lastActivityAt == null) {
            return Long.MAX_VALUE;
        }
        LocalDateTime idleExpireTime = lastActivityAt.plusSeconds(idleTimeout);
        long remaining = Duration.between(LocalDateTime.now(), idleExpireTime).getSeconds();
        return Math.max(0, remaining);
    }

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

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public int getAbsoluteTimeout() {
        return absoluteTimeout;
    }

    public void setAbsoluteTimeout(int absoluteTimeout) {
        this.absoluteTimeout = absoluteTimeout;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    @Override
    public String toString() {
        return "Session{" +
                "id='" + id + '\'' +
                ", userId=" + userId +
                ", expiresAt=" + expiresAt +
                ", deviceInfo=" + deviceInfo +
                ", createdAt=" + createdAt +
                ", lastActivityAt=" + lastActivityAt +
                ", absoluteTimeout=" + absoluteTimeout +
                ", idleTimeout=" + idleTimeout +
                ", rememberMe=" + rememberMe +
                '}';
    }
}
