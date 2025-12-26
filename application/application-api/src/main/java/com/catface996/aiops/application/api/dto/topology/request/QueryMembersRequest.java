package com.catface996.aiops.application.api.dto.topology.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询成员列表请求
 *
 * <p>用于分页查询拓扑图中的资源节点成员。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-008: 拓扑图成员管理接口</li>
 *   <li>US5: 拓扑图成员管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "查询成员列表请求")
public class QueryMembersRequest {

    @Schema(description = "拓扑图ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "拓扑图ID不能为空")
    private Long topologyId;

    @Schema(description = "节点类型ID（可选，筛选指定类型的节点）", example = "1")
    private Long nodeTypeId;

    @Schema(description = "搜索关键词（可选，搜索名称和描述）", example = "web")
    private String keyword;

    @Schema(description = "页码（从1开始）", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer size = 10;
}
