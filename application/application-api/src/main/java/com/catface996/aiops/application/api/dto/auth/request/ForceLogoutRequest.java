package com.catface996.aiops.application.api.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 强制登出其他设备请求
 *
 * <p>用于用户主动使其他设备上的会话失效，然后在当前设备重新登录。
 * 此功能常用于以下场景：</p>
 * <ul>
 *   <li>用户发现账号在其他设备上登录，怀疑账号被盗用</li>
 *   <li>用户希望清除所有旧会话，重新开始</li>
 *   <li>用户在新设备上登录，但旧设备会话未过期</li>
 * </ul>
 *
 * <p>安全机制：</p>
 * <ul>
 *   <li>需要验证密码，防止他人滥用此功能</li>
 *   <li>需要提供当前有效的 JWT Token，证明当前会话有效</li>
 *   <li>操作会记录到审计日志，便于追踪</li>
 * </ul>
 *
 * <p>执行流程：</p>
 * <ol>
 *   <li>解析 JWT Token 获取用户ID</li>
 *   <li>验证密码是否正确（安全验证）</li>
 *   <li>查询该用户的所有活跃会话</li>
 *   <li>删除所有旧会话（包括当前会话）</li>
 *   <li>创建新会话并生成新的 JWT Token</li>
 *   <li>记录审计日志</li>
 * </ol>
 *
 * @author AI Assistant
 * @since 2025-01-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForceLogoutRequest {

    /**
     * JWT Token
     *
     * <p>当前会话的 JWT Token，用于识别用户身份。</p>
     *
     * <p>格式要求：</p>
     * <ul>
     *   <li>必须包含 "Bearer " 前缀</li>
     *   <li>示例："Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."</li>
     * </ul>
     *
     * <p>验证规则：</p>
     * <ul>
     *   <li>不能为空</li>
     *   <li>Token 必须有效且未过期</li>
     *   <li>Token 必须对应一个存在的会话</li>
     * </ul>
     */
    @NotBlank(message = "Token 不能为空")
    private String token;

    /**
     * 密码
     *
     * <p>用户密码，用于安全验证，确保是用户本人执行此操作。</p>
     *
     * <p>验证规则：</p>
     * <ul>
     *   <li>不能为空</li>
     *   <li>必须与账号密码匹配</li>
     * </ul>
     *
     * <p>安全说明：</p>
     * <ul>
     *   <li>密码通过 HTTPS 传输（网关层卸载）</li>
     *   <li>密码不会以明文形式记录到日志</li>
     *   <li>如果密码错误，会记录登录失败次数</li>
     *   <li>连续5次密码错误会锁定账号30分钟</li>
     * </ul>
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 创建强制登出请求
     *
     * @param token JWT Token（包含 Bearer 前缀）
     * @param password 用户密码
     * @return 强制登出请求对象
     */
    public static ForceLogoutRequest of(String token, String password) {
        return new ForceLogoutRequest(token, password);
    }
}
