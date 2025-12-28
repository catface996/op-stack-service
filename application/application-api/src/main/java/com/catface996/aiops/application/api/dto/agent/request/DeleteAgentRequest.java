package com.catface996.aiops.application.api.dto.agent.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除 Agent 请求
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "删除 Agent 请求")
public class DeleteAgentRequest {

    @Schema(description = "Agent ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Agent ID 不能为空")
    private Long id;
}
