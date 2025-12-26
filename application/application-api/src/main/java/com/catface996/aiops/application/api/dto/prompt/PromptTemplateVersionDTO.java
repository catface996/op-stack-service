package com.catface996.aiops.application.api.dto.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 模板版本DTO
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板版本信息")
public class PromptTemplateVersionDTO {

    @Schema(description = "版本ID", example = "1")
    private Long id;

    @Schema(description = "模板ID", example = "1")
    private Long templateId;

    @Schema(description = "版本号", example = "1")
    private Integer versionNumber;

    @Schema(description = "模板内容")
    private String content;

    @Schema(description = "变更说明", example = "初始版本")
    private String changeNote;

    @Schema(description = "创建者ID", example = "1")
    private Long createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
