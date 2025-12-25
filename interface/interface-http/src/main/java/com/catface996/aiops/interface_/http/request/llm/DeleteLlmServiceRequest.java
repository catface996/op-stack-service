package com.catface996.aiops.interface_.http.request.llm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 删除 LLM 服务请求
 *
 * <p>用于 POST /llm-services/delete 接口</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>Feature 024: POST-Only API 重构</li>
 *   <li>原接口: DELETE /llm-services/{id}</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@Schema(description = "删除 LLM 服务请求")
public class DeleteLlmServiceRequest {

    @Schema(description = "服务ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "服务ID不能为空")
    private Long id;

    @Schema(description = "是否强制删除", example = "false")
    private Boolean force = false;

    // ==================== 网关注入字段（可选） ====================

    @Schema(description = "租户ID（网关注入）", hidden = true)
    private Long tenantId;

    @Schema(description = "追踪ID（网关注入）", hidden = true)
    private String traceId;

    // ==================== Constructors ====================

    public DeleteLlmServiceRequest() {
    }

    public DeleteLlmServiceRequest(Long id) {
        this.id = id;
    }

    public DeleteLlmServiceRequest(Long id, Boolean force) {
        this.id = id;
        this.force = force;
    }

    // ==================== Getters and Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getForce() {
        return force != null ? force : false;
    }

    public void setForce(Boolean force) {
        this.force = force;
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
