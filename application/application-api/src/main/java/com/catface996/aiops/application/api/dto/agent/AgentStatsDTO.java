package com.catface996.aiops.application.api.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Agent 统计信息 DTO
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent 统计信息")
public class AgentStatsDTO {

    @Schema(description = "Agent 总数", example = "25")
    private Long totalAgents;

    @Schema(description = "按角色分布", example = "{\"WORKER\": 20, \"TEAM_SUPERVISOR\": 4, \"GLOBAL_SUPERVISOR\": 1}")
    private Map<String, Long> byRole;

    @Schema(description = "总警告数", example = "42")
    private Long totalWarnings;

    @Schema(description = "总严重问题数", example = "8")
    private Long totalCritical;
}
