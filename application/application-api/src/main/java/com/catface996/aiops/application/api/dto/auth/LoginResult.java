package com.catface996.aiops.application.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户登录结果 DTO
 *
 * <p>包含用户登录成功后的 JWT Token、用户信息和会话信息。</p>
 *
 * <p>登录流程：</p>
 * <ol>
 *   <li>检查账号是否被锁定（连续5次失败锁定30分钟）</li>
 *   <li>根据标识符（用户名或邮箱）查找账号</li>
 *   <li>验证密码是否正确</li>
 *   <li>处理会话互斥（使旧会话失效）</li>
 *   <li>创建新会话并生成 JWT Token</li>
 *   <li>存储会话到 Redis</li>
 *   <li>重置登录失败计数</li>
 *   <li>返回登录结果</li>
 * </ol>
 *
 * <p>JWT Token 使用说明：</p>
 * <ul>
 *   <li>客户端需要将 Token 保存到 LocalStorage 或 Cookie</li>
 *   <li>后续请求需要在 HTTP Header 中携带 Token：Authorization: Bearer {token}</li>
 *   <li>Token 过期后需要重新登录获取新的 Token</li>
 *   <li>Token 包含用户ID、用户名、角色和过期时间等信息</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResult {

    /**
     * JWT Token
     *
     * <p>用于后续请求的身份认证令牌。</p>
     *
     * <p>Token 格式：</p>
     * <pre>
     * eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NSIsInVzZXJuYW1lIjoiam9obl9kb2UiLCJyb2xlIjoiUk9MRV9VU0VSIiwiaWF0IjoxNzA2MDAwMDAwLCJleHAiOjE3MDYwMDcyMDB9.signature
     * </pre>
     *
     * <p>使用方式：</p>
     * <pre>
     * Authorization: Bearer {token}
     * </pre>
     *
     * <p>Token 有效期：</p>
     * <ul>
     *   <li>默认（rememberMe=false）：2 小时</li>
     *   <li>记住我（rememberMe=true）：30 天</li>
     * </ul>
     */
    private String token;

    /**
     * 用户信息
     *
     * <p>当前登录用户的基本信息，包含账号ID、用户名、邮箱、角色和账号状态。</p>
     *
     * <p>用途：</p>
     * <ul>
     *   <li>前端展示用户信息</li>
     *   <li>前端根据角色显示不同的菜单</li>
     *   <li>前端根据状态提示用户</li>
     * </ul>
     */
    private UserInfo userInfo;

    /**
     * 会话ID
     *
     * <p>会话的唯一标识符，用于会话管理和审计。</p>
     *
     * <p>UUID 格式示例："550e8400-e29b-41d4-a716-446655440000"</p>
     */
    private String sessionId;

    /**
     * 会话过期时间
     *
     * <p>会话的过期时间，ISO8601 格式。</p>
     *
     * <p>过期时间计算：</p>
     * <ul>
     *   <li>默认（rememberMe=false）：当前时间 + 2 小时</li>
     *   <li>记住我（rememberMe=true）：当前时间 + 30 天</li>
     * </ul>
     *
     * <p>示例："2025-01-24T12:30:00"</p>
     */
    private LocalDateTime expiresAt;

    /**
     * 设备信息
     *
     * <p>用户登录设备的信息，用于审计和会话管理。</p>
     *
     * <p>包含信息：</p>
     * <ul>
     *   <li>设备类型：PC、Mobile、Tablet</li>
     *   <li>操作系统：Windows、macOS、Linux、Android、iOS</li>
     *   <li>浏览器：Chrome、Firefox、Safari、Edge</li>
     * </ul>
     *
     * <p>示例："Chrome 120.0 on Windows 11"</p>
     */
    private String deviceInfo;

    /**
     * 登录成功消息
     *
     * <p>友好的提示消息，告知用户登录成功。</p>
     *
     * <p>示例："登录成功"</p>
     */
    private String message;
}
