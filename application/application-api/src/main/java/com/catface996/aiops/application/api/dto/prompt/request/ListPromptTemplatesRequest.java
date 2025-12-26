package com.catface996.aiops.application.api.dto.prompt.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询提示词模板列表请求
 *
 * <p>支持分页查询，可按用途筛选和名称搜索。</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "查询提示词模板列表请求")
public class ListPromptTemplatesRequest {

    @Schema(description = "用途ID筛选（可选）", example = "1")
    private Long usageId;

    @Schema(description = "关键词搜索（模糊匹配名称和描述）", example = "故障诊断")
    private String keyword;

    @Schema(description = "页码（从1开始）", example = "1", defaultValue = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10", defaultValue = "10")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer size = 10;
}
