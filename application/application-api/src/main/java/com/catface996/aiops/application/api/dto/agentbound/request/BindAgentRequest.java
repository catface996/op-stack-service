package com.catface996.aiops.application.api.dto.agentbound.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 绑定 Agent 请求
 *
 * <p>将指定的 Agent 绑定到指定的实体（Topology 或 Node）。</p>
 *
 * <p>业务规则：</p>
 * <ul>
 *   <li>GLOBAL_SUPERVISOR 层级的 Agent 只能绑定到 TOPOLOGY</li>
 *   <li>TEAM_SUPERVISOR 和 TEAM_WORKER 层级的 Agent 只能绑定到 NODE</li>
 *   <li>Supervisor 绑定采用替换策略（同一实体只能有一个）</li>
 *   <li>Worker 绑定采用追加策略（同一实体可以有多个）</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@Schema(description = "绑定 Agent 请求")
public class BindAgentRequest {

    @NotNull(message = "Agent ID 不能为空")
    @Schema(description = "Agent ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long agentId;

    @NotNull(message = "实体 ID 不能为空")
    @Schema(description = "实体 ID（Topology ID 或 Node ID）", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long entityId;

    @NotNull(message = "实体类型不能为空")
    @Schema(description = "实体类型：TOPOLOGY 或 NODE", example = "NODE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String entityType;
}
