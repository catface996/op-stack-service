package com.catface996.aiops.application.api.dto.node.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询未绑定到指定节点的 Agent 列表请求
 *
 * <p>查询未与指定资源节点绑定的 Agent，支持分页和关键词过滤。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "查询未绑定到指定节点的 Agent 列表请求")
public class ListUnboundAgentsRequest {

    @NotNull(message = "节点ID不能为空")
    @Schema(description = "节点ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long nodeId;

    @Schema(description = "搜索关键词（可选，搜索 Agent 名称和专长）", example = "monitor")
    private String keyword;

    @Schema(description = "页码（从1开始）", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer size = 10;
}
