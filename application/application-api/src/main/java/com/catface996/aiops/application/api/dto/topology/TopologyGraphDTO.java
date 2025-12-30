package com.catface996.aiops.application.api.dto.topology;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 拓扑图数据 DTO
 *
 * <p>用于图形渲染的拓扑数据，包含节点和边。</p>
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
@Schema(description = "拓扑图数据")
public class TopologyGraphDTO {

    /**
     * 拓扑图基本信息
     */
    @Schema(description = "拓扑图基本信息")
    private TopologyInfo topology;

    /**
     * 节点列表
     */
    @Schema(description = "节点列表")
    @Builder.Default
    private List<GraphNodeDTO> nodes = new ArrayList<>();

    /**
     * 边列表（节点关系）
     */
    @Schema(description = "边列表（节点关系）")
    @Builder.Default
    private List<GraphEdgeDTO> edges = new ArrayList<>();

    // ==================== 嵌套类 ====================

    /**
     * 拓扑图基本信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "拓扑图基本信息")
    public static class TopologyInfo {

        @Schema(description = "拓扑图ID", example = "101")
        private Long id;

        @Schema(description = "拓扑图名称", example = "电商平台拓扑图")
        private String name;
    }

    /**
     * 图节点 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "图节点")
    public static class GraphNodeDTO {

        @Schema(description = "节点ID", example = "201")
        private Long id;

        @Schema(description = "节点名称", example = "订单服务")
        private String name;

        @Schema(description = "节点类型代码", example = "APPLICATION")
        private String nodeTypeCode;

        @Schema(description = "节点状态", example = "RUNNING")
        private String status;

        @Schema(description = "架构层级", example = "BUSINESS_APPLICATION")
        private String layer;

        @Schema(description = "节点在画布上的X坐标", example = "100")
        private Integer positionX;

        @Schema(description = "节点在画布上的Y坐标", example = "200")
        private Integer positionY;
    }

    /**
     * 图边 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "图边（节点关系）")
    public static class GraphEdgeDTO {

        @Schema(description = "源节点ID", example = "201")
        private Long sourceId;

        @Schema(description = "目标节点ID", example = "202")
        private Long targetId;

        @Schema(description = "关系类型", example = "DEPENDENCY")
        private String relationshipType;

        @Schema(description = "关系方向", example = "UNIDIRECTIONAL")
        private String direction;

        @Schema(description = "关系强度", example = "STRONG")
        private String strength;

        @Schema(description = "关系状态", example = "NORMAL")
        private String status;
    }
}
