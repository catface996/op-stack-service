package com.catface996.aiops.application.api.dto.topology.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询拓扑图列表请求
 *
 * <p>支持按名称模糊查询和状态筛选。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-002: 系统必须提供独立的拓扑图列表查询接口</li>
 *   <li>US1: 查询所有拓扑图</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "查询拓扑图列表请求")
public class QueryTopologiesRequest {

    @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    @Schema(description = "名称模糊查询（可选）", example = "电商")
    private String name;

    @Schema(description = "状态筛选（可选，RUNNING/STOPPED/MAINTENANCE/OFFLINE）", example = "RUNNING")
    private String status;

    @Schema(description = "页码（从1开始）", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer size = 10;
}
