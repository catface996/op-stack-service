package com.catface996.aiops.application.api.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent 模板 DTO
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent 配置模板")
public class AgentTemplateDTO {

    @Schema(description = "模板名称", example = "Standard Coordinator")
    private String name;

    @Schema(description = "模板描述", example = "标准协调者模板，适用于一般任务分配和协调")
    private String description;

    @Schema(description = "推荐角色", example = "TEAM_SUPERVISOR")
    private String recommendedRole;

    @Schema(description = "系统指令", example = "你是一个专业的团队协调者...")
    private String systemInstruction;

    @Schema(description = "推荐模型", example = "gemini-2.0-flash")
    private String recommendedModel;

    @Schema(description = "推荐温度", example = "0.3")
    private Double recommendedTemperature;
}
