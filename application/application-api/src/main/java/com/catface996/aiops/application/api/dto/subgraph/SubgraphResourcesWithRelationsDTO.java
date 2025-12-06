package com.catface996.aiops.application.api.dto.subgraph;

import com.catface996.aiops.application.api.dto.relationship.RelationshipDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 子图资源及关系DTO
 *
 * <p>返回子图中所有资源节点的完整信息及节点之间的关系，不分页。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>前端拓扑图展示需要完整的节点信息和关系数据</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "子图资源及关系信息")
public class SubgraphResourcesWithRelationsDTO {

    @Schema(description = "子图ID", example = "1")
    private Long subgraphId;

    @Schema(description = "子图名称", example = "production-network")
    private String subgraphName;

    @Schema(description = "资源节点列表（完整信息）")
    private List<SubgraphResourceDTO> resources;

    @Schema(description = "节点之间的关系列表")
    private List<RelationshipDTO> relationships;

    @Schema(description = "节点总数", example = "15")
    private int nodeCount;

    @Schema(description = "边（关系）总数", example = "20")
    private int edgeCount;
}
