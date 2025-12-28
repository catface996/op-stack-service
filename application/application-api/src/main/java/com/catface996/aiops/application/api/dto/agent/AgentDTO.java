package com.catface996.aiops.application.api.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent DTO
 *
 * <p>用于 Agent 列表和详情展示。</p>
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

    @Schema(description = "警告数量", example = "5")
    private Integer warnings;

    @Schema(description = "严重问题数量", example = "2")
    private Integer critical;

    @Schema(description = "AI 配置")
    private AgentConfigDTO config;

    @Schema(description = "所属团队 ID 列表")
    private List<Long> teamIds;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
