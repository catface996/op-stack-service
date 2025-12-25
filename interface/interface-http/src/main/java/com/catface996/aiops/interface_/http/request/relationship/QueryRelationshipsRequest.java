package com.catface996.aiops.interface_.http.request.relationship;

import com.catface996.aiops.interface_.http.request.common.PageableRequest;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 查询关系列表请求
 *
 * <p>用于 POST /relationships/query 接口</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>Feature 024: POST-Only API 重构</li>
 *   <li>原接口: GET /relationships</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@Schema(description = "查询关系列表请求")
public class QueryRelationshipsRequest extends PageableRequest {

    @Schema(description = "源资源ID", example = "1")
    private Long sourceResourceId;

    @Schema(description = "目标资源ID", example = "2")
    private Long targetResourceId;

    @Schema(description = "关系类型: DEPENDENCY, CALL, DEPLOYMENT, OWNERSHIP, ASSOCIATION", example = "DEPENDENCY")
    private String relationshipType;

    @Schema(description = "关系状态: NORMAL, ABNORMAL", example = "NORMAL")
    private String status;

    // ==================== Constructors ====================

    public QueryRelationshipsRequest() {
    }

    // ==================== Getters and Setters ====================

    public Long getSourceResourceId() {
        return sourceResourceId;
    }

    public void setSourceResourceId(Long sourceResourceId) {
        this.sourceResourceId = sourceResourceId;
    }

    public Long getTargetResourceId() {
        return targetResourceId;
    }

    public void setTargetResourceId(Long targetResourceId) {
        this.targetResourceId = targetResourceId;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
