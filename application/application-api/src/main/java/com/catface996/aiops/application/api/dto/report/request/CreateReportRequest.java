package com.catface996.aiops.application.api.dto.report.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建报告请求
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建报告请求")
public class CreateReportRequest {

    @Schema(description = "报告标题", example = "K8s 集群性能分析报告", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "报告标题不能为空")
    @Size(max = 200, message = "报告标题不能超过200个字符")
    private String title;

    @Schema(description = "报告类型: Diagnosis, Audit, Performance, Security", example = "Performance", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "报告类型不能为空")
    private String type;

    @Schema(description = "报告状态: Draft, Final, Archived", example = "Final", defaultValue = "Final")
    private String status = "Final";

    @Schema(description = "作者", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "作者不能为空")
    @Size(max = 100, message = "作者名称不能超过100个字符")
    private String author;

    @Schema(description = "报告摘要", example = "本报告分析了生产环境 K8s 集群的性能表现...")
    @Size(max = 500, message = "报告摘要不能超过500个字符")
    private String summary;

    @Schema(description = "报告内容(Markdown格式)")
    private String content;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "关联拓扑ID（可选）", example = "1")
    private Long topologyId;
}
