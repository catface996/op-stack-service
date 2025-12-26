package com.catface996.aiops.application.api.dto.prompt.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建模板用途请求
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建模板用途请求")
public class CreateTemplateUsageRequest {

    @Schema(description = "用途编码（大写字母和下划线）", example = "CUSTOM_USAGE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用途编码不能为空")
    @Size(max = 50, message = "用途编码最长50个字符")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "用途编码必须以大写字母开头，只能包含大写字母、数字和下划线")
    private String code;

    @Schema(description = "用途名称", example = "自定义用途", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用途名称不能为空")
    @Size(max = 100, message = "用途名称最长100个字符")
    private String name;

    @Schema(description = "用途描述", example = "用于自定义场景的提示词模板")
    @Size(max = 500, message = "用途描述最长500个字符")
    private String description;
}
