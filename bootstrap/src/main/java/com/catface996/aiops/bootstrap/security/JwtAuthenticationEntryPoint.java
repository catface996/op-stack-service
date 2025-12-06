package com.catface996.aiops.bootstrap.security;

import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.interface_.http.response.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 认证入口点
 *
 * <p>处理认证失败的情况，返回 401 Unauthorized 错误</p>
 *
 * <p>触发场景：</p>
 * <ul>
 *   <li>访问受保护接口时未提供 Token</li>
 *   <li>提供的 Token 无效或已过期</li>
 *   <li>Token 签名验证失败</li>
 * </ul>
 *
 * <p>返回统一的错误响应格式（JSON）：</p>
 * <pre>
 * {
 *   "code": 401001,
 *   "message": "Token无效",
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
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * 处理认证失败
     *
     * @param request       HTTP 请求
     * @param response      HTTP 响应
     * @param authException 认证异常
     * @throws IOException      IO 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        log.warn("[认证失败] 请求路径: {}, 原因: {}", request.getRequestURI(), authException.getMessage());

        // 检查请求属性中是否有更具体的异常信息（来自 JwtAuthenticationFilter）
        BusinessException businessException = (BusinessException) request.getAttribute("exception");

        Integer errorCode;
        String errorMessage;

        if (businessException != null) {
            // 使用过滤器中设置的具体异常信息
            errorCode = parseHttpErrorCode(businessException.getErrorCode());
            errorMessage = businessException.getErrorMessage();
        } else {
            // 使用默认的认证失败信息
            errorCode = 401001;
            errorMessage = "认证失败，请先登录";
        }

        // 构造统一的错误响应
        Result<Void> apiResponse = Result.error(errorCode, errorMessage);

        // 设置响应头
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 将响应对象序列化为 JSON 并写入响应体
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.getWriter().flush();
    }

    /**
     * 将字符串错误码转换为HTTP错误码
     *
     * <p>转换规则：AUTH_001 → 401001, AUTH_002 → 401002</p>
     *
     * @param errorCode 错误码（如 "AUTH_001"）
     * @return HTTP错误码（如 401001）
     */
    private Integer parseHttpErrorCode(String errorCode) {
        if (errorCode == null || errorCode.isEmpty()) {
            return 401000;
        }

        try {
            String[] parts = errorCode.split("_");
            if (parts.length == 2) {
                int sequence = Integer.parseInt(parts[1]);
                return 401000 + sequence;
            }
        } catch (Exception e) {
            log.warn("[认证入口点] 解析错误码失败: {}", errorCode, e);
        }

        return 401000;
    }
}
