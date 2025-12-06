package com.catface996.aiops.application.api.dto.subgraph.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 子图资源列表查询请求
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>BUG-007: 缺少查询子图资源列表的API端点</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "子图资源列表查询请求")
public class ListSubgraphResourcesRequest {

    @Min(value = 1, message = "页码最小为1")
    @Schema(description = "页码（从1开始）", example = "1", defaultValue = "1")
    private Integer page = 1;

    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    @Schema(description = "每页大小", example = "20", defaultValue = "20")
    private Integer size = 20;
}
