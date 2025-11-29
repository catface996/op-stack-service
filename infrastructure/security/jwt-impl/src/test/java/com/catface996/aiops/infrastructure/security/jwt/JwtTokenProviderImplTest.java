package com.catface996.aiops.infrastructure.security.jwt;

import com.catface996.aiops.common.enums.SessionErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.auth.TokenClaims;
import com.catface996.aiops.domain.model.auth.TokenType;
import com.catface996.aiops.infrastructure.security.api.service.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JwtTokenProviderImpl 单元测试
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
@DisplayName("JWT Token Provider 实现测试")
class JwtTokenProviderImplTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "test-secret-key-for-jwt-token-provider-must-be-at-least-256-bits-long";
    private static final Long TEST_USER_ID = 12345L;
    private static final String TEST_USERNAME = "john_doe";
    private static final String TEST_ROLE = "ROLE_USER";
    private static final String TEST_SESSION_ID = "session-123-456-789";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProviderImpl(TEST_SECRET);
    }

    @Test
    @DisplayName("生成 Token - 默认过期时间（2小时）")
    void testGenerateToken_DefaultExpiration() {
        // When
        String token = jwtTokenProvider.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, false);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT 格式：header.payload.signature

        // 验证 Token 内容
        Map<String, Object> claims = jwtTokenProvider.validateAndParseToken(token);
        assertThat(claims.get("sub")).isEqualTo(TEST_USER_ID.toString());
        assertThat(claims.get("username")).isEqualTo(TEST_USERNAME);
        assertThat(claims.get("role")).isEqualTo(TEST_ROLE);

        // 验证过期时间约为 2 小时
        Date issuedAt = (Date) claims.get("iat");
        Date expiration = (Date) claims.get("exp");
        long expirationTime = expiration.getTime() - issuedAt.getTime();
        long twoHoursInMillis = 2 * 60 * 60 * 1000L;
        assertThat(expirationTime).isCloseTo(twoHoursInMillis, org.assertj.core.data.Offset.offset(1000L));
    }

    @Test
    @DisplayName("生成 Token - 记住我（30天）")
    void testGenerateToken_RememberMe() {
        // When
        String token = jwtTokenProvider.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, true);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // 验证过期时间约为 30 天
        Map<String, Object> claims = jwtTokenProvider.validateAndParseToken(token);
        Date issuedAt = (Date) claims.get("iat");
        Date expiration = (Date) claims.get("exp");
        long expirationTime = expiration.getTime() - issuedAt.getTime();
        long thirtyDaysInMillis = 30 * 24 * 60 * 60 * 1000L;
        assertThat(expirationTime).isCloseTo(thirtyDaysInMillis, org.assertj.core.data.Offset.offset(1000L));
    }

    @Test
    @DisplayName("生成 Token - 包含 sessionId")
    void testGenerateToken_WithSessionId() {
        // When
        String token = jwtTokenProvider.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, TEST_SESSION_ID, false);

        // Then
        assertThat(token).isNotNull();

        // 验证 sessionId
        String sessionId = jwtTokenProvider.getSessionIdFromToken(token);
        assertThat(sessionId).isEqualTo(TEST_SESSION_ID);
    }

    @Test
    @DisplayName("生成 Token - 带有唯一标识 (jti)")
    void testGenerateTokenWithJti() {
        // When
        String token = jwtTokenProvider.generateTokenWithJti(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, TEST_SESSION_ID, false);

        // Then
        assertThat(token).isNotNull();

        // 验证 jti 存在
        String tokenId = jwtTokenProvider.getTokenIdFromToken(token);
        assertThat(tokenId).isNotNull();
        assertThat(tokenId).isNotEmpty();

        // 验证 sessionId
        String sessionId = jwtTokenProvider.getSessionIdFromToken(token);
        assertThat(sessionId).isEqualTo(TEST_SESSION_ID);
    }

    @Test
    @DisplayName("生成两个 Token - jti 应该不同")
    void testGenerateTokenWithJti_UniqueJti() {
        // When
        String token1 = jwtTokenProvider.generateTokenWithJti(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, TEST_SESSION_ID, false);
        String token2 = jwtTokenProvider.generateTokenWithJti(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, TEST_SESSION_ID, false);

        // Then
        String jti1 = jwtTokenProvider.getTokenIdFromToken(token1);
        String jti2 = jwtTokenProvider.getTokenIdFromToken(token2);

        assertThat(jti1).isNotEqualTo(jti2);
    }

    @Test
    @DisplayName("验证有效的 Token")
    void testValidateAndParseToken_ValidToken() {
        // Given
        String token = jwtTokenProvider.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, false);

        // When
        Map<String, Object> claims = jwtTokenProvider.validateAndParseToken(token);

        // Then
        assertThat(claims).isNotNull();
        assertThat(claims.get("sub")).isEqualTo(TEST_USER_ID.toString());
        assertThat(claims.get("username")).isEqualTo(TEST_USERNAME);
        assertThat(claims.get("role")).isEqualTo(TEST_ROLE);
        assertThat(claims.get("iat")).isNotNull();
        assertThat(claims.get("exp")).isNotNull();
        assertThat((Date) claims.get("exp")).isAfter(new Date());
    }

    @Test
    @DisplayName("解析 Token 为 TokenClaims 值对象")
    void testParseTokenClaims() {
        // Given
        String token = jwtTokenProvider.generateTokenWithJti(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, TEST_SESSION_ID, false);

        // When
        TokenClaims claims = jwtTokenProvider.parseTokenClaims(token);

        // Then
        assertThat(claims).isNotNull();
        assertThat(claims.getUserId()).isEqualTo(TEST_USER_ID);
        assertThat(claims.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(claims.getRole()).isEqualTo(TEST_ROLE);
        assertThat(claims.getSessionId()).isEqualTo(TEST_SESSION_ID);
        assertThat(claims.getTokenId()).isNotNull();
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiresAt()).isNotNull();
        assertThat(claims.getTokenType()).isEqualTo(TokenType.ACCESS);
        assertThat(claims.isExpired()).isFalse();
    }

    @Test
    @DisplayName("验证过期的 Token - 抛出 ExpiredJwtException")
    void testValidateAndParseToken_ExpiredToken() {
        // Given - 创建一个已过期的 Token（过期时间为 1 秒前）
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() - 1000L); // 1秒前过期

        String expiredToken = Jwts.builder()
                .subject(TEST_USER_ID.toString())
                .claim("username", TEST_USERNAME)
                .claim("role", TEST_ROLE)
                .issuedAt(new Date(now.getTime() - 2000L))
                .expiration(expiredDate)
                .signWith(key)
                .compact();

        // When & Then
        assertThatThrownBy(() -> jwtTokenProvider.validateAndParseToken(expiredToken))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(SessionErrorCode.TOKEN_EXPIRED.getCode());
                });
    }

    @Test
    @DisplayName("验证格式错误的 Token - 抛出 BusinessException(TOKEN_INVALID)")
    void testValidateAndParseToken_MalformedToken() {
        // Given
        String malformedToken = "invalid.token.format";

        // When & Then
        assertThatThrownBy(() -> jwtTokenProvider.validateAndParseToken(malformedToken))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(SessionErrorCode.TOKEN_INVALID.getCode());
                });
    }

    @Test
    @DisplayName("验证签名错误的 Token - 抛出 BusinessException(TOKEN_INVALID)")
    void testValidateAndParseToken_InvalidSignature() {
        // Given - 使用不同的密钥生成 Token
        String differentSecret = "different-secret-key-for-jwt-token-provider-must-be-at-least-256-bits-long";
        SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));

        String tokenWithDifferentSignature = Jwts.builder()
                .subject(TEST_USER_ID.toString())
                .claim("username", TEST_USERNAME)
                .claim("role", TEST_ROLE)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(differentKey)
                .compact();

        // When & Then
        assertThatThrownBy(() -> jwtTokenProvider.validateAndParseToken(tokenWithDifferentSignature))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(SessionErrorCode.TOKEN_INVALID.getCode());
                });
    }

    @Test
    @DisplayName("验证空 Token - 抛出 BusinessException(TOKEN_INVALID)")
    void testValidateAndParseToken_NullToken() {
        // When & Then
        assertThatThrownBy(() -> jwtTokenProvider.validateAndParseToken(null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> {
                    BusinessException be = (BusinessException) e;
                    assertThat(be.getErrorCode()).isEqualTo(SessionErrorCode.TOKEN_INVALID.getCode());
                });
    }

    @Test
    @DisplayName("从 Token 中提取用户ID")
    void testGetUserIdFromToken() {
        // Given
        String token = jwtTokenProvider.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, false);

        // When
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        // Then
        assertThat(userId).isEqualTo(TEST_USER_ID);
    }

    @Test
    @DisplayName("从 Token 中提取用户名")
    void testGetUsernameFromToken() {
        // Given
        String token = jwtTokenProvider.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, false);

        // When
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertThat(username).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("从 Token 中提取用户角色")
    void testGetRoleFromToken() {
        // Given
        String token = jwtTokenProvider.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, false);

        // When
        String role = jwtTokenProvider.getRoleFromToken(token);

        // Then
        assertThat(role).isEqualTo(TEST_ROLE);
    }

    @Test
    @DisplayName("检查 Token 是否过期 - 未过期")
    void testIsTokenExpired_NotExpired() {
        // Given
        String token = jwtTokenProvider.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, false);

        // When
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("检查 Token 是否过期 - 已过期")
    void testIsTokenExpired_Expired() {
        // Given - 创建一个已过期的 Token
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() - 1000L);

        String expiredToken = Jwts.builder()
                .subject(TEST_USER_ID.toString())
                .claim("username", TEST_USERNAME)
                .claim("role", TEST_ROLE)
                .issuedAt(new Date(now.getTime() - 2000L))
                .expiration(expiredDate)
                .signWith(key)
                .compact();

        // When
        boolean isExpired = jwtTokenProvider.isTokenExpired(expiredToken);

        // Then
        assertThat(isExpired).isTrue();
    }

    @Test
    @DisplayName("获取 Token 的过期时间")
    void testGetExpirationDateFromToken() {
        // Given
        String token = jwtTokenProvider.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, false);

        // When
        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);

        // Then
        assertThat(expirationDate).isNotNull();
        assertThat(expirationDate).isAfter(new Date());

        // 验证过期时间约为 2 小时后
        long timeUntilExpiration = expirationDate.getTime() - System.currentTimeMillis();
        long twoHoursInMillis = 2 * 60 * 60 * 1000L;
        assertThat(timeUntilExpiration).isCloseTo(twoHoursInMillis, org.assertj.core.data.Offset.offset(2000L));
    }

    @Test
    @DisplayName("获取 Token 的剩余有效时间")
    void testGetRemainingTtl() {
        // Given
        String token = jwtTokenProvider.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, false);

        // When
        long remainingTtl = jwtTokenProvider.getRemainingTtl(token);

        // Then
        // 约为 2 小时 = 7200 秒
        long twoHoursInSeconds = 2 * 60 * 60;
        assertThat(remainingTtl).isCloseTo(twoHoursInSeconds, org.assertj.core.data.Offset.offset(5L));
    }

    @Test
    @DisplayName("获取过期 Token 的剩余有效时间 - 返回 0")
    void testGetRemainingTtl_ExpiredToken() {
        // Given - 创建一个已过期的 Token
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiredDate = new Date(now.getTime() - 1000L);

        String expiredToken = Jwts.builder()
                .subject(TEST_USER_ID.toString())
                .claim("username", TEST_USERNAME)
                .claim("role", TEST_ROLE)
                .issuedAt(new Date(now.getTime() - 2000L))
                .expiration(expiredDate)
                .signWith(key)
                .compact();

        // When
        long remainingTtl = jwtTokenProvider.getRemainingTtl(expiredToken);

        // Then
        assertThat(remainingTtl).isEqualTo(0);
    }

    @Test
    @DisplayName("生成的 Token 包含所有必要的用户信息")
    void testGeneratedTokenContainsAllUserInfo() {
        // Given
        Long userId = 99999L;
        String username = "test_user";
        String role = "ROLE_ADMIN";

        // When
        String token = jwtTokenProvider.generateToken(userId, username, role, false);
        Map<String, Object> claims = jwtTokenProvider.validateAndParseToken(token);

        // Then
        assertThat(claims.get("sub")).isEqualTo(userId.toString());
        assertThat(claims.get("username")).isEqualTo(username);
        assertThat(claims.get("role")).isEqualTo(role);
        assertThat(claims.get("iat")).isNotNull();
        assertThat(claims.get("exp")).isNotNull();
    }

    @Test
    @DisplayName("从没有 sessionId 的 Token 中提取 sessionId - 返回 null")
    void testGetSessionIdFromToken_NoSessionId() {
        // Given
        String token = jwtTokenProvider.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, false);

        // When
        String sessionId = jwtTokenProvider.getSessionIdFromToken(token);

        // Then
        assertThat(sessionId).isNull();
    }

    @Test
    @DisplayName("从没有 jti 的 Token 中提取 tokenId - 返回 null")
    void testGetTokenIdFromToken_NoJti() {
        // Given - 使用不带 jti 的方法生成 Token
        String token = jwtTokenProvider.generateToken(TEST_USER_ID, TEST_USERNAME, TEST_ROLE, TEST_SESSION_ID, false);

        // When
        String tokenId = jwtTokenProvider.getTokenIdFromToken(token);

        // Then
        assertThat(tokenId).isNull();
    }
}
