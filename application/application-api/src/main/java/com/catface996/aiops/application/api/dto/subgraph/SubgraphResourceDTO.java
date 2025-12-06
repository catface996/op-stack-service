package com.catface996.aiops.application.api.dto.subgraph;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 子图资源DTO
 *
 * <p>用于返回子图中资源节点的详细信息。</p>
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
@Schema(description = "子图资源信息")
public class SubgraphResourceDTO {

    @Schema(description = "关联ID", example = "1")
    private Long id;

    @Schema(description = "资源节点ID", example = "123")
    private Long resourceId;

    @Schema(description = "子图ID", example = "5")
    private Long subgraphId;

    @Schema(description = "资源名称", example = "web-server-01")
    private String resourceName;

    @Schema(description = "资源类型名称", example = "SERVER")
    private String resourceType;

    @Schema(description = "资源状态", example = "RUNNING")
    private String resourceStatus;

    @Schema(description = "添加到子图的时间")
    private LocalDateTime addedAt;

    @Schema(description = "添加者ID", example = "1")
    private Long addedBy;
}
