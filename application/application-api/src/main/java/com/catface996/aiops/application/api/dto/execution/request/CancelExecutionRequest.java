package com.catface996.aiops.application.api.dto.execution.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 取消执行请求
 *
 * <p>用于取消正在执行的多智能体运行。</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "取消执行请求")
public class CancelExecutionRequest {

    @NotBlank(message = "runId 不能为空")
    @Schema(description = "运行 ID", example = "a1567309-4c03-43f8-bbae-9a2d75fd6d80", required = true)
    private String runId;
}
