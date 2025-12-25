package com.catface996.aiops.interface_.http.request.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 终止会话请求
 *
 * <p>用于 POST /sessions/terminate 接口</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>Feature 024: POST-Only API 重构</li>
 *   <li>原接口: DELETE /sessions/{sessionId}</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@Schema(description = "终止会话请求")
public class TerminateSessionRequest {

    @Schema(description = "会话ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    @Schema(description = "JWT Token（包含 Bearer 前缀）", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String authorization;

    // ==================== 网关注入字段（可选） ====================

    @Schema(description = "租户ID（网关注入）", hidden = true)
    private Long tenantId;

    @Schema(description = "追踪ID（网关注入）", hidden = true)
    private String traceId;

    // ==================== Constructors ====================

    public TerminateSessionRequest() {
    }

    public TerminateSessionRequest(String sessionId) {
        this.sessionId = sessionId;
    }

    // ==================== Getters and Setters ====================

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
