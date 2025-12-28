package com.catface996.aiops.application.api.dto.report.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建报告模板请求
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建报告模板请求")
public class CreateReportTemplateRequest {

    @Schema(description = "模板名称", example = "安全审计报告模板", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称不能超过100个字符")
    private String name;

    @Schema(description = "模板描述", example = "用于生成安全审计报告的标准模板")
    @Size(max = 500, message = "模板描述不能超过500个字符")
    private String description;

    @Schema(description = "模板分类: Incident, Performance, Security, Audit", example = "Security", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模板分类不能为空")
    private String category;

    @Schema(description = "模板内容（含占位符的Markdown格式）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模板内容不能为空")
    private String content;

    @Schema(description = "标签列表")
    private List<String> tags;
}
