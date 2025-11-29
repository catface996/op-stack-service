package com.catface996.aiops.infrastructure.security.jwt;

import com.catface996.aiops.common.enums.SessionErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.auth.TokenClaims;
import com.catface996.aiops.domain.model.auth.TokenType;
import com.catface996.aiops.infrastructure.security.api.service.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT Token 提供者实现
 *
 * <p>负责 JWT Token 的生成、验证和解析功能。</p>
 * <p>支持不同的过期时间（2小时 vs 30天）。</p>
 * <p>处理 Token 过期、无效等异常情况。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 2.1, 2.2, 2.3, 2.4: JWT令牌管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
@Slf4j
@Component
public class JwtTokenProviderImpl implements JwtTokenProvider {

    /**
     * JWT 签名密钥
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
     * 构造函数
     *
     * @param jwtSecret JWT 签名密钥
     */
    public JwtTokenProviderImpl(@Value("${security.jwt.secret}") String jwtSecret) {
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

        if (sessionId != null && !sessionId.isEmpty()) {
            tokenBuilder.claim("sessionId", sessionId);
        }

        String token = tokenBuilder.signWith(secretKey).compact();

        log.debug("Generated JWT token for user: {}, sessionId: {}, rememberMe: {}, expiresAt: {}",
                username, sessionId, rememberMe, expirationDate);

        return token;
    }

    @Override
    public String generateTokenWithJti(Long userId, String username, String role, String sessionId, boolean rememberMe) {
        Date now = new Date();
        long expirationTime = rememberMe ? REMEMBER_ME_EXPIRATION_TIME : DEFAULT_EXPIRATION_TIME;
        Date expirationDate = new Date(now.getTime() + expirationTime);

        // 生成唯一的令牌ID
        String tokenId = UUID.randomUUID().toString();

        var tokenBuilder = Jwts.builder()
                .id(tokenId)  // jti claim
                .subject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expirationDate);

        if (sessionId != null && !sessionId.isEmpty()) {
            tokenBuilder.claim("sessionId", sessionId);
        }

        String token = tokenBuilder.signWith(secretKey).compact();

        log.debug("Generated JWT token with jti for user: {}, sessionId: {}, jti: {}, rememberMe: {}, expiresAt: {}",
                username, sessionId, tokenId, rememberMe, expirationDate);

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
    public String getTokenIdFromToken(String token) {
        try {
            Map<String, Object> claims = validateAndParseToken(token);
            Object jti = claims.get("jti");
            return jti != null ? jti.toString() : null;
        } catch (Exception e) {
            log.warn("Failed to extract jti from token: {}", e.getMessage());
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

            return buildClaimsMap(claims);

        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired, message: {}", e.getMessage(), e);
            throw new BusinessException(SessionErrorCode.TOKEN_EXPIRED, e);
        } catch (JwtException e) {
            log.warn("JWT token invalid, message: {}", e.getMessage(), e);
            throw new BusinessException(SessionErrorCode.TOKEN_INVALID, e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT token invalid argument, message: {}", e.getMessage(), e);
            throw new BusinessException(SessionErrorCode.TOKEN_INVALID, e);
        }
    }

    /**
     * 从 Claims 构建 claims map
     *
     * @param claims JWT claims
     * @return claims map
     */
    private Map<String, Object> buildClaimsMap(Claims claims) {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("sub", claims.getSubject());
        claimsMap.put("username", claims.get("username"));
        claimsMap.put("role", claims.get("role"));
        claimsMap.put("iat", claims.getIssuedAt());
        claimsMap.put("exp", claims.getExpiration());

        if (claims.getId() != null) {
            claimsMap.put("jti", claims.getId());
        }
        if (claims.get("sessionId") != null) {
            claimsMap.put("sessionId", claims.get("sessionId"));
        }

        return claimsMap;
    }

    @Override
    public TokenClaims parseTokenClaims(String token) {
        Map<String, Object> claims = validateAndParseToken(token);

        String sessionId = claims.get("sessionId") != null ? claims.get("sessionId").toString() : null;
        Long userId = Long.parseLong(claims.get("sub").toString());
        String username = (String) claims.get("username");
        String role = (String) claims.get("role");
        String tokenId = claims.get("jti") != null ? claims.get("jti").toString() : null;

        Date issuedAtDate = (Date) claims.get("iat");
        Date expiresAtDate = (Date) claims.get("exp");

        Instant issuedAt = issuedAtDate != null ? issuedAtDate.toInstant() : null;
        Instant expiresAt = expiresAtDate != null ? expiresAtDate.toInstant() : null;

        return new TokenClaims(sessionId, userId, username, role, tokenId, issuedAt, expiresAt, TokenType.ACCESS);
    }

    @Override
    public Long getUserIdFromToken(String token) {
        Map<String, Object> claims = validateAndParseToken(token);
        return Long.parseLong(claims.get("sub").toString());
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
        } catch (BusinessException e) {
            // TOKEN_EXPIRED 表示令牌已过期
            return SessionErrorCode.TOKEN_EXPIRED.getCode().equals(e.getErrorCode());
        }
    }

    @Override
    public Date getExpirationDateFromToken(String token) {
        Map<String, Object> claims = validateAndParseToken(token);
        return (Date) claims.get("exp");
    }

    @Override
    public long getRemainingTtl(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            long remainingMs = expiration.getTime() - System.currentTimeMillis();
            return remainingMs > 0 ? remainingMs / 1000 : 0;
        } catch (BusinessException e) {
            // TOKEN_EXPIRED 或 TOKEN_INVALID 都返回 0
            return 0;
        }
    }
}
