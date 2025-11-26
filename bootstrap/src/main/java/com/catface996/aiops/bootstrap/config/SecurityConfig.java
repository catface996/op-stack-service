package com.catface996.aiops.bootstrap.config;

import com.catface996.aiops.bootstrap.security.JwtAccessDeniedHandler;
import com.catface996.aiops.bootstrap.security.JwtAuthenticationEntryPoint;
import com.catface996.aiops.bootstrap.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置类
 *
 * <p>核心配置：</p>
 * <ul>
 *   <li>无状态会话管理（使用 JWT）</li>
 *   <li>JWT 认证过滤器</li>
 *   <li>公开接口：注册、登录、健康检查</li>
 *   <li>受保护接口：需要认证</li>
 *   <li>管理员接口：需要 ROLE_ADMIN 权限</li>
 *   <li>异常处理：统一返回 JSON 格式错误响应</li>
 * </ul>
 *
 * <p>URL 访问规则：</p>
 * <pre>
 * /actuator/health               - 公开（健康检查）
 * /actuator/prometheus            - 公开（Prometheus 监控）
 * /api/v1/auth/register           - 公开（用户注册）
 * /api/v1/auth/login              - 公开（用户登录）
 * /api/v1/admin/**                - 需要 ROLE_ADMIN 权限
 * 其他所有接口                     - 需要认证
 * </pre>
 *
 * @author AI Assistant
 * @since 2025-11-25
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 启用方法级权限控制（支持 @PreAuthorize 等注解）
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * 配置 HTTP 安全
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（使用 JWT，不需要 CSRF 保护）
            .csrf(AbstractHttpConfigurer::disable)

            // 配置会话管理：无状态
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 公开接口：健康检查和监控端点
                .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()

                // 公开接口：Swagger/OpenAPI 文档
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()

                // 公开接口：认证相关（注册、登录）
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll()

                // 管理员接口：需要 ROLE_ADMIN 权限
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // 其他所有接口都需要认证
                .anyRequest().authenticated()
            )

            // 添加 JWT 认证过滤器（在 UsernamePasswordAuthenticationFilter 之前执行）
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // 配置异常处理
            .exceptionHandling(exception -> exception
                // 认证失败处理器（401）
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                // 访问拒绝处理器（403）
                .accessDeniedHandler(jwtAccessDeniedHandler)
            );

        return http.build();
    }

    /**
     * 配置密码加密器
     *
     * <p>使用 BCrypt 算法，Work Factor = 10</p>
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
