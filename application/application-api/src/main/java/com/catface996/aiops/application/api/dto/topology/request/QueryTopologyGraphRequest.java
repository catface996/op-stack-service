package com.catface996.aiops.application.api.dto.topology.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询拓扑图数据请求
 *
 * <p>获取拓扑图的节点和边数据，用于图形渲染。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: resource 表拆分为 topology 表和 node 表</li>
 *   <li>FR-010: 拓扑图数据查询接口</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "查询拓扑图数据请求")
public class QueryTopologyGraphRequest {

    /**
     * 操作人ID
     */
    @NotNull(message = "操作人ID不能为空")
    @Min(value = 0, message = "操作人ID不能为负数")
    @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long operatorId;

    /**
     * 拓扑图ID
     */
    @NotNull(message = "拓扑图ID不能为空")
    @Min(value = 1, message = "拓扑图ID必须大于0")
    @Schema(description = "拓扑图ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long topologyId;

    /**
     * 展开深度（用于嵌套子图）
     *
     * <p>默认为1，最大为5。</p>
     */
    @Min(value = 1, message = "深度最小为1")
    @Max(value = 5, message = "深度最大为5")
    @Schema(description = "展开深度（用于嵌套子图）", example = "1", defaultValue = "1")
    @Builder.Default
    private Integer depth = 1;

    /**
     * 是否包含关系（边）
     */
    @Schema(description = "是否包含关系（边）", example = "true", defaultValue = "true")
    @Builder.Default
    private Boolean includeRelationships = true;
}
