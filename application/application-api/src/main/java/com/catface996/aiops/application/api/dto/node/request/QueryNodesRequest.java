package com.catface996.aiops.application.api.dto.node.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询节点列表请求
 *
 * <p>支持按节点类型、状态和关键词过滤。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-003: 系统必须提供独立的节点列表查询接口</li>
 *   <li>US2: 查询所有节点</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "查询节点列表请求")
public class QueryNodesRequest {

    @Schema(description = "节点类型ID（可选，不传则查询所有类型）", example = "1")
    private Long nodeTypeId;

    @Schema(description = "节点状态（可选，RUNNING/STOPPED/MAINTENANCE/OFFLINE）", example = "RUNNING")
    private String status;

    @Schema(description = "搜索关键词（可选，搜索名称和描述）", example = "web")
    private String keyword;

    @Schema(description = "拓扑图ID（可选，只查询属于指定拓扑图的节点）", example = "1")
    private Long topologyId;

    @Schema(description = "页码（从1开始）", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer size = 10;
}
