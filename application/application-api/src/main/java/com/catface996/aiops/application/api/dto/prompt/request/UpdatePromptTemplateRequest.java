package com.catface996.aiops.application.api.dto.prompt.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新提示词模板请求
 *
 * <p>更新模板内容会自动生成新版本。</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新提示词模板请求")
public class UpdatePromptTemplateRequest {

    @Schema(description = "模板ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模板ID不能为空")
    private Long id;

    @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    @Schema(description = "新模板内容", example = "请分析以下故障信息...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模板内容不能为空")
    private String content;

    @Schema(description = "变更说明", example = "修复格式问题")
    @Size(max = 500, message = "变更说明最长500个字符")
    private String changeNote;

    @Schema(description = "期望的乐观锁版本（用于并发控制）", example = "0")
    private Integer expectedVersion;
}
