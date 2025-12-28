package com.catface996.aiops.application.api.dto.agent.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 查询 Agent 统计信息请求
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "查询 Agent 统计信息请求")
public class AgentStatsRequest {

    @Schema(description = "Agent ID（可选，不传则查询整体统计）", example = "1")
    private Long agentId;

    @Schema(description = "统计开始时间（可选）")
    private LocalDateTime startTime;

    @Schema(description = "统计结束时间（可选）")
    private LocalDateTime endTime;
}
