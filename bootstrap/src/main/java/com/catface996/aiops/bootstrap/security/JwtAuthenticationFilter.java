package com.catface996.aiops.bootstrap.security;

import com.catface996.aiops.common.enums.SessionErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.infrastructure.security.api.service.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JWT 认证过滤器
 *
 * <p>功能：</p>
 * <ul>
 *   <li>从 HTTP 请求头中提取 JWT Token</li>
 *   <li>验证 Token 的有效性</li>
 *   <li>将认证信息设置到 Spring Security 上下文</li>
 *   <li>处理 Token 过期、无效等异常情况</li>
 * </ul>
 *
 * <p>使用 OncePerRequestFilter 确保每个请求只执行一次</p>
 *
 * @author AI Assistant
 * @since 2025-11-25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 执行过滤逻辑
     *
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. 从请求头中提取 JWT Token
            String token = extractTokenFromRequest(request);

            // 2. 如果 Token 存在且 SecurityContext 中没有认证信息
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateToken(token, request);
            }

        } catch (BusinessException e) {
            // JwtTokenProviderImpl 将 jjwt 异常转换为 BusinessException
            if (SessionErrorCode.TOKEN_EXPIRED.getCode().equals(e.getErrorCode())) {
                log.warn("JWT Token 已过期: {}", e.getMessage());
            } else {
                log.warn("JWT Token 无效: {}", e.getMessage());
            }
            // 异常会被后续的 AuthenticationEntryPoint 处理
            request.setAttribute("exception", e);

        } catch (Exception e) {
            log.error("JWT 认证过程发生异常", e);
            request.setAttribute("exception", new BusinessException(SessionErrorCode.TOKEN_INVALID));
        }

        // 3. 继续执行过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从 HTTP 请求头中提取 JWT Token
     *
     * @param request HTTP 请求
     * @return JWT Token，如果不存在或格式错误则返回 null
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * 验证 Token 并设置认证信息到 SecurityContext
     *
     * @param token   JWT Token
     * @param request HTTP 请求
     */
    private void authenticateToken(String token, HttpServletRequest request) {
        // 验证并解析 Token
        Map<String, Object> claims = jwtTokenProvider.validateAndParseToken(token);

        // 提取用户信息
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String username = jwtTokenProvider.getUsernameFromToken(token);
        String role = jwtTokenProvider.getRoleFromToken(token);

        // 构造权限列表
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        // 创建认证对象
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);

        // 设置请求详情
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // 设置到 SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("成功认证用户: userId={}, username={}, role={}", userId, username, role);
    }
}
