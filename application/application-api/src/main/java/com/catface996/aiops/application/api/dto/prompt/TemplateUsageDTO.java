package com.catface996.aiops.application.api.dto.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 模板用途DTO
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板用途信息")
public class TemplateUsageDTO {

    @Schema(description = "用途ID", example = "1")
    private Long id;

    @Schema(description = "用途编码", example = "FAULT_DIAGNOSIS")
    private String code;

    @Schema(description = "用途名称", example = "故障诊断")
    private String name;

    @Schema(description = "用途描述", example = "用于故障分析和诊断场景的提示词模板")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
