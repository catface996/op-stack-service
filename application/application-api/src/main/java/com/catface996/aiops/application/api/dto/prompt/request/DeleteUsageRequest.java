package com.catface996.aiops.application.api.dto.prompt.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除模板用途请求
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "删除模板用途请求")
public class DeleteUsageRequest {

    @Schema(description = "用途ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用途ID不能为空")
    private Long id;
}
