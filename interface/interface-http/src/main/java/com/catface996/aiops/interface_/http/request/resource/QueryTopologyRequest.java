package com.catface996.aiops.interface_.http.request.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 查询资源拓扑请求
 *
 * <p>用于 POST /resources/topology/query 接口</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>Feature 024: POST-Only API 重构</li>
 *   <li>原接口: GET /resources/{id}/topology</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@Schema(description = "查询资源拓扑请求")
public class QueryTopologyRequest {

    @Schema(description = "资源ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

    @Schema(description = "是否展开嵌套子图", example = "false")
    private Boolean expandNested = false;

    @Schema(description = "最大展开深度（1-10）", example = "3", minimum = "1", maximum = "10")
    @Min(value = 1, message = "最大深度最小为 1")
    @Max(value = 10, message = "最大深度最大为 10")
    private Integer maxDepth = 3;

    // ==================== 网关注入字段（可选） ====================

    @Schema(description = "租户ID（网关注入）", hidden = true)
    private Long tenantId;

    @Schema(description = "追踪ID（网关注入）", hidden = true)
    private String traceId;

    // ==================== Constructors ====================

    public QueryTopologyRequest() {
    }

    public QueryTopologyRequest(Long resourceId) {
        this.resourceId = resourceId;
    }

    // ==================== Getters and Setters ====================

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Boolean getExpandNested() {
        return expandNested;
    }

    public void setExpandNested(Boolean expandNested) {
        this.expandNested = expandNested;
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
