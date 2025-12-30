package com.catface996.aiops.domain.model.topology;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 拓扑图数据领域模型
 *
 * <p>包含拓扑图的节点和边数据，用于图形渲染。</p>
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
public class TopologyGraphData {

    /**
     * 拓扑图ID
     */
    private Long topologyId;

    /**
     * 拓扑图名称
     */
    private String topologyName;

    /**
     * 节点列表
     */
    @Builder.Default
    private List<GraphNode> nodes = new ArrayList<>();

    /**
     * 边列表（节点关系）
     */
    @Builder.Default
    private List<GraphEdge> edges = new ArrayList<>();

    // ==================== 嵌套类 ====================

    /**
     * 图节点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphNode {

        /**
         * 节点ID
         */
        private Long id;

        /**
         * 节点名称
         */
        private String name;

        /**
         * 节点类型代码
         */
        private String nodeTypeCode;

        /**
         * 节点状态
         */
        private String status;

        /**
         * 架构层级
         */
        private String layer;

        /**
         * 节点在画布上的X坐标
         */
        private Integer positionX;

        /**
         * 节点在画布上的Y坐标
         */
        private Integer positionY;
    }

    /**
     * 图边（节点关系）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraphEdge {

        /**
         * 源节点ID
         */
        private Long sourceId;

        /**
         * 目标节点ID
         */
        private Long targetId;

        /**
         * 关系类型
         */
        private String relationshipType;

        /**
         * 关系方向
         */
        private String direction;

        /**
         * 关系强度
         */
        private String strength;

        /**
         * 关系状态
         */
        private String status;
    }
}
