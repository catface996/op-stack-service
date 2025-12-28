package com.catface996.aiops.application.api.dto.report.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 更新报告模板请求
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新报告模板请求")
public class UpdateReportTemplateRequest {

    @Schema(description = "模板ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模板ID不能为空")
    private Long id;

    @Schema(description = "模板名称", example = "安全审计报告模板v2")
    @Size(max = 100, message = "模板名称不能超过100个字符")
    private String name;

    @Schema(description = "模板描述", example = "用于生成安全审计报告的标准模板（更新版）")
    @Size(max = 500, message = "模板描述不能超过500个字符")
    private String description;

    @Schema(description = "模板分类: Incident, Performance, Security, Audit", example = "Security")
    private String category;

    @Schema(description = "模板内容（含占位符的Markdown格式）")
    private String content;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "期望的版本号（用于乐观锁）", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "版本号不能为空")
    private Integer expectedVersion;
}
