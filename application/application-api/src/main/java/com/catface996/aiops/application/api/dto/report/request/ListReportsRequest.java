package com.catface996.aiops.application.api.dto.report.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询报告列表请求
 *
 * <p>支持分页查询，可按类型、状态筛选和关键词搜索。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "查询报告列表请求")
public class ListReportsRequest {

    @Schema(description = "报告类型筛选（可选）: Diagnosis, Audit, Performance, Security", example = "Performance")
    private String type;

    @Schema(description = "报告状态筛选（可选）: Draft, Final, Archived", example = "Final")
    private String status;

    @Schema(description = "关键词搜索（模糊匹配标题、摘要和标签）", example = "性能")
    private String keyword;

    @Schema(description = "排序字段（可选）: title, type, status, created_at", example = "created_at")
    private String sortBy;

    @Schema(description = "排序方向（可选）: asc, desc", example = "desc", defaultValue = "desc")
    private String sortOrder = "desc";

    @Schema(description = "页码（从1开始）", example = "1", defaultValue = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10", defaultValue = "10")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer size = 10;
}
