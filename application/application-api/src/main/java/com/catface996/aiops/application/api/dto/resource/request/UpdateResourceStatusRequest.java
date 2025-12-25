package com.catface996.aiops.application.api.dto.resource.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新资源状态请求
 *
 * <p>支持的状态值：RUNNING, STOPPED, MAINTENANCE, OFFLINE</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-023: 更新资源状态</li>
 *   <li>REQ-FR-024: 状态快速切换</li>
 *   <li>REQ-FR-025: 状态变更历史记录</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新资源状态请求")
public class UpdateResourceStatusRequest {

    @Schema(description = "资源ID（POST-Only API 使用）", example = "1")
    private Long id;

    @Schema(description = "新状态（RUNNING/STOPPED/MAINTENANCE/OFFLINE）",
            example = "STOPPED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "状态不能为空")
    private String status;

    @Schema(description = "当前版本号（乐观锁）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "版本号不能为空")
    private Integer version;
}
