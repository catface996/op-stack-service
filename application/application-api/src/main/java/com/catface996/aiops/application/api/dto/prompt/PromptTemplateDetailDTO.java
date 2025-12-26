package com.catface996.aiops.application.api.dto.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 模板详情DTO（包含版本历史）
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "提示词模板详情（包含版本历史）")
public class PromptTemplateDetailDTO {

    @Schema(description = "模板ID", example = "1")
    private Long id;

    @Schema(description = "模板名称", example = "故障诊断提示词-v1")
    private String name;

    @Schema(description = "用途ID", example = "1")
    private Long usageId;

    @Schema(description = "用途名称", example = "故障诊断")
    private String usageName;

    @Schema(description = "模板描述", example = "用于K8s故障诊断的提示词模板")
    private String description;

    @Schema(description = "当前版本号", example = "3")
    private Integer currentVersion;

    @Schema(description = "当前版本内容")
    private String content;

    @Schema(description = "乐观锁版本", example = "2")
    private Integer version;

    @Schema(description = "创建者ID", example = "1")
    private Long createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "版本历史列表")
    private List<PromptTemplateVersionDTO> versions;
}
