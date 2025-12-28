package com.catface996.aiops.application.api.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报告DTO
 *
 * <p>用于报告列表和详情展示。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "报告信息")
public class ReportDTO {

    @Schema(description = "报告ID", example = "1")
    private Long id;

    @Schema(description = "报告标题", example = "K8s 集群性能分析报告")
    private String title;

    @Schema(description = "报告类型", example = "Performance")
    private String type;

    @Schema(description = "报告状态", example = "Final")
    private String status;

    @Schema(description = "作者", example = "张三")
    private String author;

    @Schema(description = "报告摘要", example = "本报告分析了生产环境 K8s 集群的性能表现...")
    private String summary;

    @Schema(description = "报告内容(Markdown格式)")
    private String content;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "关联拓扑ID", example = "1")
    private Long topologyId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
