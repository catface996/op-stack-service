package com.catface996.aiops.interface_.http.request.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 分页请求基类
 *
 * <p>提供标准分页参数，所有需要分页的请求 DTO 应继承此类</p>
 *
 * <p>网关注入支持：</p>
 * <ul>
 *   <li>tenantId - 租户ID</li>
 *   <li>traceId - 追踪ID</li>
 *   <li>userId - 用户ID</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>Feature 024: POST-Only API 重构</li>
 *   <li>FR-009: 网关注入参数支持</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@Schema(description = "分页请求基类")
public abstract class PageableRequest {

    @Schema(description = "页码（从 1 开始）", example = "1", minimum = "1")
    @Min(value = 1, message = "页码最小为 1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "20", minimum = "1", maximum = "100")
    @Min(value = 1, message = "每页大小最小为 1")
    @Max(value = 100, message = "每页大小最大为 100")
    private Integer size = 20;

    // ==================== 网关注入字段（可选） ====================

    @Schema(description = "租户ID（网关注入）", hidden = true)
    private Long tenantId;

    @Schema(description = "追踪ID（网关注入）", hidden = true)
    private String traceId;

    @Schema(description = "用户ID（网关注入）", hidden = true)
    private String userId;

    // ==================== Getters and Setters ====================

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
