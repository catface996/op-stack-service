package com.catface996.aiops.application.api.dto.report.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取报告详情请求
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "获取报告详情请求")
public class GetReportRequest {

    @Schema(description = "报告ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "报告ID不能为空")
    private Long id;
}
