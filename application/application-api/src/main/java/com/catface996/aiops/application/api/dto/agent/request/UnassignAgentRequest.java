package com.catface996.aiops.application.api.dto.agent.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 取消 Agent 团队分配请求
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "取消 Agent 团队分配请求")
public class UnassignAgentRequest {

    @Schema(description = "Agent ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Agent ID 不能为空")
    private Long agentId;

    @Schema(description = "Team ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Team ID 不能为空")
    private Long teamId;
}
