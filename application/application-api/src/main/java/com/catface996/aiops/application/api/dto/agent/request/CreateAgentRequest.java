package com.catface996.aiops.application.api.dto.agent.request;

import com.catface996.aiops.application.api.dto.agent.AgentConfigDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建 Agent 请求
 *
 * <p>创建新的 Agent，包含名称、角色、专业领域和可选配置。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建 Agent 请求")
public class CreateAgentRequest {

    @Schema(description = "Agent 名称（唯一）", example = "性能分析 Agent", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Agent 名称不能为空")
    @Size(max = 100, message = "Agent 名称长度不能超过 100 个字符")
    private String name;

    @Schema(description = "Agent 角色: GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, WORKER, SCOUTER",
            example = "WORKER", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Agent 角色不能为空")
    private String role;

    @Schema(description = "专业领域（可选）", example = "性能分析、资源监控")
    @Size(max = 200, message = "专业领域长度不能超过 200 个字符")
    private String specialty;

    @Schema(description = "AI 配置（可选，不提供则使用默认配置）")
    private AgentConfigDTO config;
}
