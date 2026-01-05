package com.catface996.aiops.application.api.dto.diagnosis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent诊断过程DTO
 *
 * <p>用于展示单个Agent在诊断任务中的诊断过程。</p>
 *
 * @author AI Assistant
 * @since 2026-01-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent诊断过程信息")
public class AgentDiagnosisProcessDTO {

    @Schema(description = "记录ID", example = "5001")
    private Long id;

    @Schema(description = "关联诊断任务ID", example = "1001")
    private Long taskId;

    @Schema(description = "关联AgentBound ID", example = "201")
    private Long agentBoundId;

    @Schema(description = "Agent名称", example = "数据库诊断专家")
    private String agentName;

    @Schema(description = "诊断内容（完整文本）")
    private String content;

    @Schema(description = "Agent开始诊断时间")
    private LocalDateTime startedAt;

    @Schema(description = "Agent结束诊断时间")
    private LocalDateTime endedAt;

    @Schema(description = "诊断时长（秒）", example = "30")
    private Long durationSeconds;

    @Schema(description = "内容长度（字符数）", example = "1500")
    private Integer contentLength;

    @Schema(description = "是否有输出", example = "true")
    private Boolean hasOutput;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
