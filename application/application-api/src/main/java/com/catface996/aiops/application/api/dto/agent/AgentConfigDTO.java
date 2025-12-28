package com.catface996.aiops.application.api.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 配置 DTO
 *
 * <p>Agent 的 AI 配置信息。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent AI 配置")
public class AgentConfigDTO {

    @Schema(description = "AI 模型标识", example = "gemini-2.0-flash")
    private String model;

    @Schema(description = "温度参数 (0.0-1.0)", example = "0.3")
    private Double temperature;

    @Schema(description = "系统指令", example = "你是一个专业的性能分析专家...")
    private String systemInstruction;

    @Schema(description = "默认上下文", example = "关注 CPU、内存、磁盘 IO 指标...")
    private String defaultContext;
}
