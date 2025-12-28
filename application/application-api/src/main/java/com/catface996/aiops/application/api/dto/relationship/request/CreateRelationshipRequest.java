package com.catface996.aiops.application.api.dto.relationship.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建关系请求
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建关系请求")
public class CreateRelationshipRequest {

    @NotNull(message = "操作者ID不能为空")
    @Schema(description = "操作者ID", example = "1", required = true)
    private Long operatorId;

    @NotNull(message = "源资源ID不能为空")
    @Schema(description = "源资源ID", example = "1", required = true)
    private Long sourceResourceId;

    @NotNull(message = "目标资源ID不能为空")
    @Schema(description = "目标资源ID", example = "2", required = true)
    private Long targetResourceId;

    @NotNull(message = "关系类型不能为空")
    @Schema(description = "关系类型: DEPENDENCY, CALL, DEPLOYMENT, OWNERSHIP, ASSOCIATION",
            example = "DEPENDENCY", required = true)
    private String relationshipType;

    @NotNull(message = "关系方向不能为空")
    @Schema(description = "关系方向: UNIDIRECTIONAL, BIDIRECTIONAL",
            example = "UNIDIRECTIONAL", required = true)
    private String direction;

    @NotNull(message = "关系强度不能为空")
    @Schema(description = "关系强度: STRONG, WEAK",
            example = "STRONG", required = true)
    private String strength;

    @NotNull(message = "拓扑ID不能为空")
    @Schema(description = "拓扑ID", example = "1", required = true)
    private Long topologyId;

    @Size(max = 500, message = "描述不能超过500个字符")
    @Schema(description = "关系描述", example = "Web应用依赖MySQL数据库")
    private String description;
}
