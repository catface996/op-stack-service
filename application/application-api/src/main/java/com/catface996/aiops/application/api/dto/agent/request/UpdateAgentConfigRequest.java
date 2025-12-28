package com.catface996.aiops.application.api.dto.agent.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新 Agent 配置请求
 *
 * <p>单独更新 Agent 的 AI 配置。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新 Agent 配置请求")
public class UpdateAgentConfigRequest {

    @Schema(description = "Agent ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Agent ID 不能为空")
    private Long id;

    @Schema(description = "AI 模型标识（可选）", example = "gemini-2.0-flash")
    private String model;

    @Schema(description = "温度参数 (0.0-1.0)（可选）", example = "0.5")
    @DecimalMin(value = "0.0", message = "温度参数最小为 0.0")
    @DecimalMax(value = "1.0", message = "温度参数最大为 1.0")
    private Double temperature;

    @Schema(description = "系统指令（可选）", example = "你是一个专业的性能分析专家...")
    private String systemInstruction;

    @Schema(description = "默认上下文（可选）", example = "关注 CPU、内存、磁盘 IO 指标...")
    private String defaultContext;
}
