package com.catface996.aiops.application.api.dto.agent.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建 Agent 请求
 *
 * <p>创建新的 Agent，包含名称、角色、专业领域和 LLM 配置。</p>
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

    // ===== LLM 配置（扁平化） =====

    @Schema(description = "提示词模板 ID（可选）", example = "1")
    private Long promptTemplateId;

    @Schema(description = "AI 模型标识（可选，默认 gemini-2.0-flash）", example = "gemini-2.0-flash")
    @Size(max = 100, message = "模型标识长度不能超过 100 个字符")
    private String model;

    @Schema(description = "温度参数 (0.0-2.0)，控制输出随机性（可选，默认 0.3）", example = "0.3")
    @DecimalMin(value = "0.0", message = "温度参数不能小于 0.0")
    @DecimalMax(value = "2.0", message = "温度参数不能大于 2.0")
    private Double temperature;

    @Schema(description = "Top P 参数 (0.0-1.0)，核采样阈值（可选，默认 0.9）", example = "0.9")
    @DecimalMin(value = "0.0", message = "Top P 参数不能小于 0.0")
    @DecimalMax(value = "1.0", message = "Top P 参数不能大于 1.0")
    private Double topP;

    @Schema(description = "最大输出 token 数（可选，默认 4096）", example = "4096")
    @Min(value = 1, message = "最大 token 数不能小于 1")
    private Integer maxTokens;

    @Schema(description = "最长运行时间（秒）（可选，默认 300）", example = "300")
    @Min(value = 1, message = "最长运行时间不能小于 1 秒")
    private Integer maxRuntime;
}
