package com.catface996.aiops.domain.model.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * 令牌声明值对象
 *
 * <p>包含JWT令牌的声明信息，如会话ID、用户ID、过期时间等。</p>
 *
 * <p>设计原则：值对象应该是不可变的。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 2.1, 2.2, 2.3, 2.4: JWT令牌管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-28
 */
public class TokenClaims {

    /**
     * 会话标识符
     */
    private final String sessionId;

    /**
     * 用户ID
     */
    private final Long userId;

    /**
     * 用户名
     */
    private final String username;

    /**
     * 用户角色
     */
    private final String role;

    /**
     * 令牌ID（jti，用于黑名单管理）
     */
    private final String tokenId;

    /**
     * 颁发时间
     */
    private final Instant issuedAt;

    /**
     * 过期时间
     */
    private final Instant expiresAt;

    /**
     * 令牌类型
     */
    private final TokenType tokenType;

    /**
     * 完整构造函数
     */
    public TokenClaims(String sessionId, Long userId, String username, String role,
                       String tokenId, Instant issuedAt, Instant expiresAt, TokenType tokenType) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.tokenId = tokenId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.tokenType = tokenType;
    }

    /**
     * 简化构造函数（用于访问令牌）
     */
    public TokenClaims(String sessionId, Long userId, String username, String role,
                       Instant issuedAt, Instant expiresAt) {
        this(sessionId, userId, username, role, null, issuedAt, expiresAt, TokenType.ACCESS);
    }

    // ==================== 业务方法 ====================

    /**
     * 检查令牌是否已过期
     *
     * @return true if token is expired, false otherwise
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return true;
        }
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * 获取剩余TTL（秒）
     *
     * @return remaining TTL in seconds, or 0 if expired
     */
    public long getRemainingTtl() {
        if (expiresAt == null || isExpired()) {
            return 0;
        }
        return Duration.between(Instant.now(), expiresAt).getSeconds();
    }

    /**
     * 检查是否为访问令牌
     *
     * @return true if access token
     */
    public boolean isAccessToken() {
        return tokenType == TokenType.ACCESS;
    }

    /**
     * 检查是否为刷新令牌
     *
     * @return true if refresh token
     */
    public boolean isRefreshToken() {
        return tokenType == TokenType.REFRESH;
    }

    // ==================== Getters ====================

    public String getSessionId() {
        return sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getTokenId() {
        return tokenId;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenClaims that = (TokenClaims) o;
        return Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(tokenId, that.tokenId) &&
                tokenType == that.tokenType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, userId, tokenId, tokenType);
    }

    @Override
    public String toString() {
        return "TokenClaims{" +
                "sessionId='" + sessionId + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", tokenId='" + tokenId + '\'' +
                ", issuedAt=" + issuedAt +
                ", expiresAt=" + expiresAt +
                ", tokenType=" + tokenType +
                '}';
    }
}
