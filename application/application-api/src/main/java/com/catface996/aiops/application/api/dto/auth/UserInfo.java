package com.catface996.aiops.application.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息 DTO
 *
 * <p>包含用户的基本信息，用于在登录、会话验证等场景中返回给前端。</p>
 *
 * <p>此 DTO 不包含敏感信息（如密码），可以安全地返回给客户端。</p>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>登录成功后返回用户信息</li>
 *   <li>会话验证时返回当前用户信息</li>
 *   <li>管理员查询用户详情</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    /**
     * 账号ID
     *
     * <p>账号的唯一标识符，用于后续的业务操作。</p>
     */
    private Long accountId;

    /**
     * 用户名
     *
     * <p>用户的登录用户名，全局唯一。</p>
     *
     * <p>示例："john_doe"</p>
     */
    private String username;

    /**
     * 邮箱地址
     *
     * <p>用户的邮箱地址，全局唯一。</p>
     *
     * <p>示例："john@example.com"</p>
     */
    private String email;

    /**
     * 角色
     *
     * <p>用户的角色，用于权限控制。</p>
     *
     * <p>可选值：</p>
     * <ul>
     *   <li>"ROLE_USER" - 普通用户</li>
     *   <li>"ROLE_ADMIN" - 系统管理员</li>
     * </ul>
     */
    private String role;

    /**
     * 账号状态
     *
     * <p>账号的当前状态，影响登录和功能访问权限。</p>
     *
     * <p>可选值：</p>
     * <ul>
     *   <li>"ACTIVE" - 活跃状态，可以正常登录和使用</li>
     *   <li>"LOCKED" - 锁定状态，登录失败次数过多导致（30分钟后自动解锁）</li>
     *   <li>"DISABLED" - 禁用状态，管理员手动禁用（需要管理员手动启用）</li>
     * </ul>
     */
    private String status;

    /**
     * 账号创建时间
     *
     * <p>账号的注册时间，ISO8601 格式。</p>
     *
     * <p>示例："2025-01-24T10:30:00"</p>
     */
    private LocalDateTime createdAt;
}
