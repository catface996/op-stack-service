package com.catface996.aiops.interface_.http.request.session;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 验证会话请求
 *
 * <p>用于 POST /sessions/validate 接口</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>Feature 024: POST-Only API 重构</li>
 *   <li>原接口: GET /sessions/validate</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@Schema(description = "验证会话请求")
public class ValidateSessionRequest {

    @Schema(description = "JWT Token（包含 Bearer 前缀）", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String authorization;

    // ==================== 网关注入字段（可选） ====================

    @Schema(description = "租户ID（网关注入）", hidden = true)
    private Long tenantId;

    @Schema(description = "追踪ID（网关注入）", hidden = true)
    private String traceId;

    // ==================== Constructors ====================

    public ValidateSessionRequest() {
    }

    public ValidateSessionRequest(String authorization) {
        this.authorization = authorization;
    }

    // ==================== Getters and Setters ====================

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
