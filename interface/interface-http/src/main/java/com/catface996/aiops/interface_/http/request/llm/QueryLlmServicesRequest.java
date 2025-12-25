package com.catface996.aiops.interface_.http.request.llm;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 查询 LLM 服务列表请求
 *
 * <p>用于 POST /llm-services/query 接口</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>Feature 024: POST-Only API 重构</li>
 *   <li>原接口: GET /llm-services</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@Schema(description = "查询 LLM 服务列表请求")
public class QueryLlmServicesRequest {

    @Schema(description = "是否只返回启用的服务", example = "false")
    private Boolean enabledOnly = false;

    // ==================== 网关注入字段（可选） ====================

    @Schema(description = "租户ID（网关注入）", hidden = true)
    private Long tenantId;

    @Schema(description = "追踪ID（网关注入）", hidden = true)
    private String traceId;

    // ==================== Constructors ====================

    public QueryLlmServicesRequest() {
    }

    public QueryLlmServicesRequest(Boolean enabledOnly) {
        this.enabledOnly = enabledOnly;
    }

    // ==================== Getters and Setters ====================

    public Boolean getEnabledOnly() {
        return enabledOnly != null ? enabledOnly : false;
    }

    public void setEnabledOnly(Boolean enabledOnly) {
        this.enabledOnly = enabledOnly;
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
