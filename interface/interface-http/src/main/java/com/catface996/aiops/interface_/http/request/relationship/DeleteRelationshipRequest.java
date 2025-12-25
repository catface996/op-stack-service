package com.catface996.aiops.interface_.http.request.relationship;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 删除关系请求
 *
 * <p>用于 POST /relationships/delete 接口</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>Feature 024: POST-Only API 重构</li>
 *   <li>原接口: DELETE /relationships/{relationshipId}</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@Schema(description = "删除关系请求")
public class DeleteRelationshipRequest {

    @Schema(description = "操作者ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作者ID不能为空")
    private Long operatorId;

    @Schema(description = "关系ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "关系ID不能为空")
    private Long relationshipId;

    // ==================== 网关注入字段（可选） ====================

    @Schema(description = "租户ID（网关注入）", hidden = true)
    private Long tenantId;

    @Schema(description = "追踪ID（网关注入）", hidden = true)
    private String traceId;

    // ==================== Constructors ====================

    public DeleteRelationshipRequest() {
    }

    public DeleteRelationshipRequest(Long operatorId, Long relationshipId) {
        this.operatorId = operatorId;
        this.relationshipId = relationshipId;
    }

    // ==================== Getters and Setters ====================

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public Long getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(Long relationshipId) {
        this.relationshipId = relationshipId;
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
