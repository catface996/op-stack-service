package com.catface996.aiops.application.api.dto.agent.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询 Agent 列表请求
 *
 * <p>支持分页查询，可按角色筛选和关键词搜索。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "查询 Agent 列表请求")
public class ListAgentsRequest {

    @Schema(description = "角色筛选（可选）: GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, WORKER, SCOUTER", example = "WORKER")
    private String role;

    @Schema(description = "关键词搜索（模糊匹配名称、专业领域）", example = "性能")
    private String keyword;

    @Schema(description = "页码（从1开始）", example = "1", defaultValue = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10", defaultValue = "10")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer size = 10;
}
