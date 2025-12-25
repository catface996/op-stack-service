package com.catface996.aiops.application.api.dto.relationship.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新关系请求
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新关系请求")
public class UpdateRelationshipRequest {

    @NotNull(message = "操作者ID不能为空")
    @Schema(description = "操作者ID", example = "1", required = true)
    private Long operatorId;

    @Schema(description = "关系ID（POST-Only API 使用）", example = "1")
    private Long relationshipId;

    @Schema(description = "关系类型: DEPENDENCY, CALL, DEPLOYMENT, OWNERSHIP, ASSOCIATION",
            example = "DEPENDENCY")
    private String relationshipType;

    @Schema(description = "关系强度: STRONG, WEAK", example = "STRONG")
    private String strength;

    @Schema(description = "关系状态: NORMAL, ABNORMAL", example = "NORMAL")
    private String status;

    @Size(max = 500, message = "描述不能超过500个字符")
    @Schema(description = "关系描述", example = "Web应用依赖MySQL数据库")
    private String description;
}
