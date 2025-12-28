package com.catface996.aiops.application.api.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent DTO
 *
 * <p>用于 Agent 列表和详情展示，配置采用扁平化设计。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent 信息")
public class AgentDTO {

    @Schema(description = "Agent ID", example = "1")
    private Long id;

    @Schema(description = "Agent 名称", example = "性能分析 Agent")
    private String name;

    @Schema(description = "Agent 角色", example = "WORKER")
    private String role;

    @Schema(description = "专业领域", example = "性能分析、资源监控")
    private String specialty;

    // ===== LLM 配置（扁平化） =====

    @Schema(description = "提示词模板 ID", example = "1")
    private Long promptTemplateId;

    @Schema(description = "提示词模板名称", example = "性能诊断模板")
    private String promptTemplateName;

    @Schema(description = "AI 模型标识", example = "gemini-2.0-flash")
    private String model;

    @Schema(description = "温度参数 (0.0-2.0)", example = "0.3")
    private Double temperature;

    @Schema(description = "Top P 参数 (0.0-1.0)", example = "0.9")
    private Double topP;

    @Schema(description = "最大输出 token 数", example = "4096")
    private Integer maxTokens;

    @Schema(description = "最长运行时间（秒）", example = "300")
    private Integer maxRuntime;

    // ===== 统计信息 =====

    @Schema(description = "警告数量", example = "5")
    private Integer warnings;

    @Schema(description = "严重问题数量", example = "2")
    private Integer critical;

    // ===== 审计字段 =====

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
