package com.catface996.aiops.application.api.dto.execution;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 执行事件 DTO
 *
 * <p>表示多智能体执行过程中的事件，通过 SSE 流式传输给前端。</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "执行事件")
public class ExecutionEventDTO {

    @Schema(description = "事件类型", example = "message",
            allowableValues = {"started", "thinking", "message", "tool_call", "tool_result", "error", "complete"})
    private String type;

    @Schema(description = "运行 ID（仅在 started 事件中返回）", example = "a1567309-4c03-43f8-bbae-9a2d75fd6d80")
    private String runId;

    @Schema(description = "产生事件的 Agent 名称", example = "Global Monitor")
    private String agentName;

    @Schema(description = "Agent 角色", example = "GLOBAL_SUPERVISOR",
            allowableValues = {"GLOBAL_SUPERVISOR", "TEAM_SUPERVISOR", "WORKER"})
    private String agentRole;

    @Schema(description = "事件内容/消息", example = "正在分析系统性能...")
    private String content;

    @Schema(description = "事件时间戳", example = "2025-12-29T10:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "附加元数据")
    private Map<String, Object> metadata;

    /**
     * 创建启动事件（包含 runId）
     */
    public static ExecutionEventDTO started(String runId) {
        return ExecutionEventDTO.builder()
                .type("started")
                .runId(runId)
                .content("Execution started")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建错误事件
     */
    public static ExecutionEventDTO error(String message) {
        return ExecutionEventDTO.builder()
                .type("error")
                .content(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建完成事件
     */
    public static ExecutionEventDTO complete(String message) {
        return ExecutionEventDTO.builder()
                .type("complete")
                .content(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 创建取消成功事件
     */
    public static ExecutionEventDTO cancelled(String runId) {
        return ExecutionEventDTO.builder()
                .type("cancelled")
                .runId(runId)
                .content("Execution cancelled")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
