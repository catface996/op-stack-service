package com.catface996.aiops.application.api.dto.resource.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除资源请求
 *
 * <p>删除操作需要输入资源名称确认，防止误删。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-017: 删除资源</li>
 *   <li>REQ-FR-019: 删除确认机制</li>
 *   <li>REQ-FR-020: 资源变更历史保留</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "删除资源请求")
public class DeleteResourceRequest {

    @Schema(description = "资源ID（POST-Only API 使用）", example = "1")
    private Long id;

    @Schema(description = "确认的资源名称（必须与资源名称完全匹配）",
            example = "web-server-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "确认名称不能为空")
    private String confirmName;
}
