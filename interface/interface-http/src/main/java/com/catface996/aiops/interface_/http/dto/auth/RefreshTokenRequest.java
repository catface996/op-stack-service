package com.catface996.aiops.interface_.http.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新令牌请求
 *
 * @author AI Assistant
 * @since 2025-01-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "刷新令牌请求")
public class RefreshTokenRequest {

    /**
     * 当前会话ID
     */
    @NotBlank(message = "会话ID不能为空")
    @Schema(description = "当前会话ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
    private String sessionId;
}
