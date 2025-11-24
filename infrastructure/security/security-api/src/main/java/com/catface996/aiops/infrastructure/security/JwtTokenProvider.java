package com.catface996.aiops.infrastructure.security.api.service;

import java.util.Date;
import java.util.Map;

/**
 * JWT Token 提供者接口
 * <p>
 * 负责 JWT Token 的生成、验证和解析功能
 * 支持不同的过期时间（2小时 vs 30天）
 * 处理 Token 过期、无效等异常情况
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
public interface JwtTokenProvider {

    /**
     * 生成 JWT Token
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param role       用户角色
     * @param rememberMe 是否记住我
     * @return JWT Token 字符串
     */
    String generateToken(Long userId, String username, String role, boolean rememberMe);

    /**
     * 验证并解析 JWT Token
     *
     * @param token JWT Token 字符串
     * @return Claims Map，包含 Token 中的所有声明
     * @throws io.jsonwebtoken.ExpiredJwtException      Token 已过期
     * @throws io.jsonwebtoken.UnsupportedJwtException  不支持的 JWT
     * @throws io.jsonwebtoken.MalformedJwtException    JWT 格式错误
     * @throws io.jsonwebtoken.security.SignatureException 签名验证失败
     * @throws IllegalArgumentException                  Token 为空或无效
     */
    Map<String, Object> validateAndParseToken(String token);

    /**
     * 从 Token 中提取用户ID
     *
     * @param token JWT Token 字符串
     * @return 用户ID
     */
    Long getUserIdFromToken(String token);

    /**
     * 从 Token 中提取用户名
     *
     * @param token JWT Token 字符串
     * @return 用户名
     */
    String getUsernameFromToken(String token);

    /**
     * 从 Token 中提取用户角色
     *
     * @param token JWT Token 字符串
     * @return 用户角色
     */
    String getRoleFromToken(String token);

    /**
     * 检查 Token 是否过期
     *
     * @param token JWT Token 字符串
     * @return true 如果 Token 已过期，否则 false
     */
    boolean isTokenExpired(String token);

    /**
     * 获取 Token 的过期时间
     *
     * @param token JWT Token 字符串
     * @return 过期时间
     */
    Date getExpirationDateFromToken(String token);
}
