package com.catface996.aiops.application.api.dto.topology.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取拓扑图详情请求
 *
 * <p>用于查询单个拓扑图的详细信息。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-002: 系统必须提供独立的拓扑图列表查询接口</li>
 *   <li>US1: 查询所有拓扑图（包含详情查询）</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "获取拓扑图详情请求")
public class GetTopologyRequest {

    @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    @Schema(description = "拓扑图ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "拓扑图ID不能为空")
    private Long id;
}
