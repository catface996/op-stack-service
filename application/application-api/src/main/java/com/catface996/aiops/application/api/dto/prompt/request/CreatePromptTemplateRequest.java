package com.catface996.aiops.application.api.dto.prompt.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建提示词模板请求
 *
 * <p>用于创建新的提示词模板，包含名称、内容、用途等信息。</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建提示词模板请求")
public class CreatePromptTemplateRequest {

    @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    @Schema(description = "模板名称", example = "故障诊断提示词-v1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模板名称不能为空")
    @Size(max = 200, message = "模板名称最长200个字符")
    private String name;

    @Schema(description = "模板内容", example = "请分析以下故障信息...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模板内容不能为空")
    private String content;

    @Schema(description = "用途ID（可选）", example = "1")
    private Long usageId;

    @Schema(description = "模板描述", example = "用于K8s故障诊断的提示词模板")
    @Size(max = 1000, message = "描述最长1000个字符")
    private String description;
}
