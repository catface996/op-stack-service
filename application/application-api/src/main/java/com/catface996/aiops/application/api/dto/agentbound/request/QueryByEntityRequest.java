package com.catface996.aiops.application.api.dto.agentbound.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 按实体查询绑定请求
 *
 * <p>查询指定实体（Topology 或 Node）绑定的 Agent 列表。</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@Schema(description = "按实体查询绑定请求")
public class QueryByEntityRequest {

    @NotNull(message = "实体类型不能为空")
    @Schema(description = "实体类型：TOPOLOGY 或 NODE", example = "NODE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String entityType;

    @NotNull(message = "实体 ID 不能为空")
    @Schema(description = "实体 ID（Topology ID 或 Node ID）", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long entityId;

    @Schema(description = "层级过滤（可选）：GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, TEAM_WORKER", example = "TEAM_WORKER")
    private String hierarchyLevel;
}
