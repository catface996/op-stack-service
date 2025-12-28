package com.catface996.aiops.application.api.dto.agent.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新 Agent 信息请求
 *
 * <p>更新 Agent 的基本信息（名称、专业领域）。角色创建后不可变。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新 Agent 信息请求")
public class UpdateAgentRequest {

    @Schema(description = "Agent ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Agent ID 不能为空")
    private Long id;

    @Schema(description = "Agent 名称（可选，不传则不更新）", example = "新的 Agent 名称")
    @Size(max = 100, message = "Agent 名称长度不能超过 100 个字符")
    private String name;

    @Schema(description = "专业领域（可选，不传则不更新）", example = "性能分析、资源监控")
    @Size(max = 200, message = "专业领域长度不能超过 200 个字符")
    private String specialty;
}
