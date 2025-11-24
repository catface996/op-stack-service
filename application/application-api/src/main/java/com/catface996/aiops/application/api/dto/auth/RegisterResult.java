package com.catface996.aiops.application.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户注册结果 DTO
 *
 * <p>包含用户注册成功后的账号信息。</p>
 *
 * <p>注册流程：</p>
 * <ol>
 *   <li>验证用户名和邮箱唯一性</li>
 *   <li>验证密码强度要求</li>
 *   <li>使用 BCrypt 加密密码</li>
 *   <li>创建账号实体并持久化到数据库</li>
 *   <li>返回注册结果</li>
 * </ol>
 *
 * <p>注意事项：</p>
 * <ul>
 *   <li>注册成功后不会自动登录，用户需要使用用户名/邮箱和密码登录</li>
 *   <li>新注册的账号默认角色为 ROLE_USER（普通用户）</li>
 *   <li>新注册的账号默认状态为 ACTIVE（活跃状态）</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResult {

    /**
     * 账号ID
     *
     * <p>新创建账号的唯一标识符，后续可以使用此ID进行账号管理操作。</p>
     */
    private Long accountId;

    /**
     * 用户名
     *
     * <p>用户注册时提供的用户名，全局唯一。</p>
     *
     * <p>示例："john_doe"</p>
     */
    private String username;

    /**
     * 邮箱地址
     *
     * <p>用户注册时提供的邮箱地址，全局唯一。</p>
     *
     * <p>示例："john@example.com"</p>
     */
    private String email;

    /**
     * 角色
     *
     * <p>新注册用户的默认角色。</p>
     *
     * <p>默认值："ROLE_USER"（普通用户）</p>
     */
    private String role;

    /**
     * 账号创建时间
     *
     * <p>账号的注册时间，ISO8601 格式。</p>
     *
     * <p>示例："2025-01-24T10:30:00"</p>
     */
    private LocalDateTime createdAt;

    /**
     * 注册成功消息
     *
     * <p>友好的提示消息，告知用户注册成功并引导下一步操作。</p>
     *
     * <p>示例："注册成功，请使用用户名或邮箱登录"</p>
     */
    private String message;
}
