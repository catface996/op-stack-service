package com.catface996.aiops.application.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话验证结果 DTO
 *
 * <p>包含会话验证的结果，包括会话是否有效、用户信息和过期时间。</p>
 *
 * <p>验证流程：</p>
 * <ol>
 *   <li>解析 JWT Token 获取会话ID</li>
 *   <li>从 Redis 查询会话信息（优先）</li>
 *   <li>如果 Redis 未命中，从 MySQL 查询（降级）</li>
 *   <li>检查会话是否过期</li>
 *   <li>返回验证结果</li>
 * </ol>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>前端应用启动时验证会话是否有效</li>
 *   <li>后端接口调用前验证用户身份</li>
 *   <li>定时刷新会话状态</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "会话验证结果")
public class SessionValidationResult {

    /**
     * 会话是否有效
     *
     * <p>表示会话是否有效，用于快速判断。</p>
     *
     * <p>会话无效的原因：</p>
     * <ul>
     *   <li>会话已过期（超过有效期）</li>
     *   <li>会话不存在（已被删除或未创建）</li>
     *   <li>用户在其他设备登录，旧会话失效（会话互斥）</li>
     *   <li>用户主动登出</li>
     *   <li>JWT Token 格式错误或签名无效</li>
     * </ul>
     */
    @Schema(description = "会话是否有效", example = "true")
    private boolean valid;

    /**
     * 用户信息
     *
     * <p>如果会话有效，返回当前用户的基本信息；否则为 null。</p>
     *
     * <p>包含信息：</p>
     * <ul>
     *   <li>账号ID</li>
     *   <li>用户名</li>
     *   <li>邮箱</li>
     *   <li>角色</li>
     *   <li>账号状态</li>
     *   <li>创建时间</li>
     * </ul>
     */
    @Schema(description = "用户信息")
    private UserInfo userInfo;

    /**
     * 会话ID
     *
     * <p>会话的唯一标识符，如果会话有效则返回；否则为 null。</p>
     *
     * <p>UUID 格式示例："550e8400-e29b-41d4-a716-446655440000"</p>
     */
    @Schema(description = "会话ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;

    /**
     * 会话过期时间
     *
     * <p>会话的过期时间，如果会话有效则返回；否则为 null。</p>
     *
     * <p>ISO8601 格式示例："2025-01-24T12:30:00"</p>
     *
     * <p>前端可以使用此时间：</p>
     * <ul>
     *   <li>显示剩余有效时间</li>
     *   <li>在即将过期时提示用户</li>
     *   <li>决定是否需要刷新 Token</li>
     * </ul>
     */
    @Schema(description = "会话过期时间", example = "2025-01-24T12:30:00")
    private LocalDateTime expiresAt;

    /**
     * 剩余有效时间（秒）
     *
     * <p>会话剩余的有效时间，单位为秒；如果会话无效则为 0。</p>
     *
     * <p>计算方式：过期时间 - 当前时间</p>
     *
     * <p>用途：</p>
     * <ul>
     *   <li>前端显示倒计时</li>
     *   <li>判断是否需要续期</li>
     *   <li>提前提示用户会话即将过期</li>
     * </ul>
     */
    @Schema(description = "剩余有效时间（秒）", example = "7200")
    private long remainingSeconds;

    /**
     * 验证消息
     *
     * <p>验证结果的描述信息，用于前端提示用户。</p>
     *
     * <p>示例：</p>
     * <ul>
     *   <li>"会话有效"</li>
     *   <li>"会话已过期，请重新登录"</li>
     *   <li>"会话不存在"</li>
     *   <li>"您的账号已在其他设备登录"</li>
     * </ul>
     */
    @Schema(description = "验证消息", example = "会话有效")
    private String message;

    /**
     * 创建有效的会话验证结果
     *
     * @param userInfo 用户信息
     * @param sessionId 会话ID
     * @param expiresAt 过期时间
     * @param remainingSeconds 剩余有效时间（秒）
     * @return 会话验证结果
     */
    public static SessionValidationResult valid(UserInfo userInfo, String sessionId,
                                                  LocalDateTime expiresAt, long remainingSeconds) {
        return SessionValidationResult.builder()
                .valid(true)
                .userInfo(userInfo)
                .sessionId(sessionId)
                .expiresAt(expiresAt)
                .remainingSeconds(remainingSeconds)
                .message("会话有效")
                .build();
    }

    /**
     * 创建无效的会话验证结果
     *
     * @param message 错误消息
     * @return 会话验证结果
     */
    public static SessionValidationResult invalid(String message) {
        return SessionValidationResult.builder()
                .valid(false)
                .userInfo(null)
                .sessionId(null)
                .expiresAt(null)
                .remainingSeconds(0L)
                .message(message)
                .build();
    }
}
