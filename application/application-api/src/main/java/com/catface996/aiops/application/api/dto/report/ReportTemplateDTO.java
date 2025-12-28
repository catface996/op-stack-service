package com.catface996.aiops.application.api.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报告模板DTO
 *
 * <p>用于模板列表和详情展示。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "报告模板信息")
public class ReportTemplateDTO {

    @Schema(description = "模板ID", example = "1")
    private Long id;

    @Schema(description = "模板名称", example = "安全审计报告模板")
    private String name;

    @Schema(description = "模板描述", example = "用于生成安全审计报告的标准模板")
    private String description;

    @Schema(description = "模板分类", example = "Security")
    private String category;

    @Schema(description = "模板内容（含占位符的Markdown格式）")
    private String content;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "乐观锁版本号", example = "0")
    private Integer version;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
