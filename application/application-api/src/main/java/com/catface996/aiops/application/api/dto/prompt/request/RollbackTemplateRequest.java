package com.catface996.aiops.application.api.dto.prompt.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回滚模板版本请求
 *
 * <p>回滚到历史版本会创建一个新版本，内容与目标版本相同。</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "回滚模板版本请求")
public class RollbackTemplateRequest {

    @Schema(description = "模板ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "模板ID不能为空")
    private Long id;

    @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    @Schema(description = "目标版本号", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "目标版本号不能为空")
    @Min(value = 1, message = "版本号最小为1")
    private Integer targetVersion;

    @Schema(description = "期望的乐观锁版本（用于并发控制）", example = "2")
    private Integer expectedVersion;
}
