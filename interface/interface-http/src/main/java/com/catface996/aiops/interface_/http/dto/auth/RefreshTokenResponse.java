package com.catface996.aiops.interface_.http.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 刷新令牌响应
 *
 * @author AI Assistant
 * @since 2025-01-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "刷新令牌响应")
public class RefreshTokenResponse {

    /**
     * 新的访问令牌
     */
    @Schema(description = "新的JWT访问令牌", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String token;

    /**
     * 会话ID
     */
    @Schema(description = "会话ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;

    /**
     * 令牌过期时间
     */
    @Schema(description = "令牌过期时间")
    private LocalDateTime expiresAt;

    /**
     * 操作消息
     */
    @Schema(description = "操作消息", example = "令牌刷新成功")
    private String message;

    /**
     * 创建响应
     */
    public static RefreshTokenResponse of(String token, String sessionId, LocalDateTime expiresAt) {
        return RefreshTokenResponse.builder()
                .token(token)
                .sessionId(sessionId)
                .expiresAt(expiresAt)
                .message("令牌刷新成功")
                .build();
    }
}
