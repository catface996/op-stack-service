package com.catface996.aiops.interface_.http.request.resource;

import com.catface996.aiops.interface_.http.request.common.PageableRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 查询资源审计日志请求
 *
 * <p>用于 POST /resources/audit-logs/query 接口</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>Feature 024: POST-Only API 重构</li>
 *   <li>原接口: GET /resources/{id}/audit-logs</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@Schema(description = "查询资源审计日志请求")
public class QueryAuditLogsRequest extends PageableRequest {

    @Schema(description = "资源ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

    // ==================== Constructors ====================

    public QueryAuditLogsRequest() {
    }

    public QueryAuditLogsRequest(Long resourceId) {
        this.resourceId = resourceId;
    }

    // ==================== Getters and Setters ====================

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
}
