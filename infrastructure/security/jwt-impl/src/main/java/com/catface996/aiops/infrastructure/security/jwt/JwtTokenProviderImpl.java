package com.catface996.aiops.infrastructure.security.jwt;

import com.catface996.aiops.infrastructure.security.api.service.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token 提供者实现
 * <p>
 * 负责 JWT Token 的生成、验证和解析功能
 * 支持不同的过期时间（2小时 vs 30天）
 * 处理 Token 过期、无效等异常情况
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
@Slf4j
@Component
public class JwtTokenProviderImpl implements JwtTokenProvider {

    /**
     * JWT 签名密钥（从配置文件读取）
     */
    private final SecretKey secretKey;

    /**
     * 默认过期时间：2小时（毫秒）
     */
    private static final long DEFAULT_EXPIRATION_TIME = 2 * 60 * 60 * 1000L;

    /**
     * "记住我"过期时间：30天（毫秒）
     */
    private static final long REMEMBER_ME_EXPIRATION_TIME = 30 * 24 * 60 * 60 * 1000L;

    /**
     * 构造函数，从配置文件读取签名密钥
     *
     * @param jwtSecret JWT 签名密钥（Base64编码或原始字符串）
     */
    public JwtTokenProviderImpl(@Value("${security.jwt.secret}") String jwtSecret) {
        // 使用 HMAC-SHA256 算法生成密钥
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        log.info("JwtTokenProvider initialized with secret key");
    }

    @Override
    public String generateToken(Long userId, String username, String role, boolean rememberMe) {
        return generateToken(userId, username, role, null, rememberMe);
    }

    @Override
    public String generateToken(Long userId, String username, String role, String sessionId, boolean rememberMe) {
        Date now = new Date();
        long expirationTime = rememberMe ? REMEMBER_ME_EXPIRATION_TIME : DEFAULT_EXPIRATION_TIME;
        Date expirationDate = new Date(now.getTime() + expirationTime);

        var tokenBuilder = Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expirationDate);

        // 如果提供了 sessionId，则添加到 Token 中
        if (sessionId != null && !sessionId.isEmpty()) {
            tokenBuilder.claim("sessionId", sessionId);
        }

        String token = tokenBuilder.signWith(secretKey).compact();

        log.debug("Generated JWT token for user: {}, sessionId: {}, rememberMe: {}, expiresAt: {}",
                username, sessionId, rememberMe, expirationDate);

        return token;
    }

    @Override
    public String getSessionIdFromToken(String token) {
        try {
            Map<String, Object> claims = validateAndParseToken(token);
            Object sessionId = claims.get("sessionId");
            return sessionId != null ? sessionId.toString() : null;
        } catch (Exception e) {
            log.warn("Failed to extract sessionId from token: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, Object> validateAndParseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            log.debug("Successfully validated JWT token for user: {}", claims.get("username"));

            // 将 Claims 转换为 Map
            Map<String, Object> claimsMap = new HashMap<>();
            claimsMap.put("sub", claims.getSubject());
            claimsMap.put("username", claims.get("username"));
            claimsMap.put("role", claims.get("role"));
            claimsMap.put("iat", claims.getIssuedAt());
            claimsMap.put("exp", claims.getExpiration());
            // 添加 sessionId（如果存在）
            if (claims.get("sessionId") != null) {
                claimsMap.put("sessionId", claims.get("sessionId"));
            }

            return claimsMap;

        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.error("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT token is invalid: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Long getUserIdFromToken(String token) {
        Map<String, Object> claims = validateAndParseToken(token);
        return Long.parseLong((String) claims.get("sub"));
    }

    @Override
    public String getUsernameFromToken(String token) {
        Map<String, Object> claims = validateAndParseToken(token);
        return (String) claims.get("username");
    }

    @Override
    public String getRoleFromToken(String token) {
        Map<String, Object> claims = validateAndParseToken(token);
        return (String) claims.get("role");
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            Map<String, Object> claims = validateAndParseToken(token);
            Date expiration = (Date) claims.get("exp");
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    @Override
    public Date getExpirationDateFromToken(String token) {
        Map<String, Object> claims = validateAndParseToken(token);
        return (Date) claims.get("exp");
    }
}
