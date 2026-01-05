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
 * <p>事件格式示例：</p>
 * <pre>
 * {
 *   "type": "llm.stream",
 *   "runId": "abc-123",
 *   "agentId": "100",
 *   "agentName": "Agent A",
 *   "agentType": "worker",
 *   "teamName": "Team 1",
 *   "content": "Hello...",
 *   "timestamp": "2025-12-31T10:00:00",
 *   "metadata": {}
 * }
 * </pre>
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

    @Schema(description = "事件类型（格式: category.action）", example = "llm.stream",
            allowableValues = {"started", "lifecycle.started", "lifecycle.completed", "lifecycle.failed",
                    "llm.stream", "llm.reasoning", "llm.tool_call", "llm.tool_result",
                    "dispatch.team", "dispatch.worker", "system.error", "error", "complete", "cancelled"})
    private String type;

    @Schema(description = "运行 ID", example = "a1567309-4c03-43f8-bbae-9a2d75fd6d80")
    private String runId;

    @Schema(description = "诊断任务 ID", example = "1")
    private Long taskId;

    @Schema(description = "Agent ID（绑定关系 ID，用于追溯）", example = "100")
    private String agentId;

    @Schema(description = "产生事件的 Agent 名称", example = "Global Monitor")
    private String agentName;

    @Schema(description = "Agent 类型", example = "worker",
            allowableValues = {"global_supervisor", "team_supervisor", "worker"})
    private String agentType;

    @Schema(description = "所属团队名称（global_supervisor 时为 null）", example = "分析组")
    private String teamName;

    @Schema(description = "事件内容/消息", example = "正在分析系统性能...")
    private String content;

    @Schema(description = "事件时间戳", example = "2025-12-29T10:00:00")
    private LocalDateTime timestamp;

    @Schema(description = "附加元数据")
    private Map<String, Object> metadata;

    // ==================== 兼容旧字段（已废弃，保留向后兼容）====================

    @Schema(description = "Agent 角色（已废弃，使用 agentType）", hidden = true)
    private String agentRole;

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
