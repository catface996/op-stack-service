package com.catface996.aiops.interface_.http.request.relationship;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 图遍历请求
 *
 * <p>用于 POST /relationships/resource/traverse 接口</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>Feature 024: POST-Only API 重构</li>
 *   <li>原接口: GET /relationships/resource/{resourceId}/traverse</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@Schema(description = "图遍历请求")
public class TraverseRelationshipsRequest {

    @Schema(description = "资源ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

    @Schema(description = "最大深度", example = "10", minimum = "1", maximum = "100")
    @Min(value = 1, message = "最大深度最小为 1")
    @Max(value = 100, message = "最大深度最大为 100")
    private Integer maxDepth = 10;

    // ==================== 网关注入字段（可选） ====================

    @Schema(description = "租户ID（网关注入）", hidden = true)
    private Long tenantId;

    @Schema(description = "追踪ID（网关注入）", hidden = true)
    private String traceId;

    // ==================== Constructors ====================

    public TraverseRelationshipsRequest() {
    }

    public TraverseRelationshipsRequest(Long resourceId) {
        this.resourceId = resourceId;
    }

    // ==================== Getters and Setters ====================

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
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
