package com.catface996.aiops.application.api.dto.diagnosis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 诊断任务DTO
 *
 * <p>用于诊断任务列表和详情展示。</p>
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "诊断任务信息")
public class DiagnosisTaskDTO {

    @Schema(description = "诊断任务ID", example = "1001")
    private Long id;

    @Schema(description = "关联拓扑图ID", example = "101")
    private Long topologyId;

    @Schema(description = "关联拓扑图名称", example = "电商系统架构图")
    private String topologyName;

    @Schema(description = "用户诊断问题", example = "系统响应变慢，请帮我分析原因")
    private String userQuestion;

    @Schema(description = "任务状态", example = "RUNNING")
    private String status;

    @Schema(description = "状态显示名称", example = "运行中")
    private String statusDisplay;

    @Schema(description = "错误信息", example = "executor连接失败")
    private String errorMessage;

    @Schema(description = "executor运行ID", example = "run_abc123")
    private String runId;

    @Schema(description = "参与诊断的Agent数量", example = "5")
    private Integer agentCount;

    @Schema(description = "完成时间")
    private LocalDateTime completedAt;

    @Schema(description = "创建人ID", example = "1")
    private Long createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "运行时长（秒）", example = "120")
    private Long durationSeconds;

    @Schema(description = "Agent诊断过程列表（详情查询时填充）")
    private List<AgentDiagnosisProcessDTO> agentProcesses;
}
