package com.catface996.aiops.bootstrap.security;

import com.catface996.aiops.interface_.http.response.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 访问拒绝处理器
 *
 * <p>处理权限不足的情况，返回 403 Forbidden 错误</p>
 *
 * <p>触发场景：</p>
 * <ul>
 *   <li>已认证的用户访问需要更高权限的接口</li>
 *   <li>例如：普通用户访问管理员接口</li>
 * </ul>
 *
 * <p>返回统一的错误响应格式（JSON）：</p>
 * <pre>
 * {
 *   "code": 403001,
 *   "message": "权限不足，无法访问该资源",
 *   "data": null
 * }
 * </pre>
 *
 * @author AI Assistant
 * @since 2025-11-25
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * 处理访问拒绝
     *
     * @param request               HTTP 请求
     * @param response              HTTP 响应
     * @param accessDeniedException 访问拒绝异常
     * @throws IOException      IO 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.warn("[访问拒绝] 请求路径: {}, 原因: {}", request.getRequestURI(), accessDeniedException.getMessage());

        // 构造统一的错误响应
        Result<Void> apiResponse = Result.error(403001, "权限不足，无法访问该资源");

        // 设置响应头
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 将响应对象序列化为 JSON 并写入响应体
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.getWriter().flush();
    }
}
