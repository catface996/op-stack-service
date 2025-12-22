package com.catface996.aiops.application.dto.subgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * 拓扑图 DTO
 *
 * <p>v2.0 设计：子图作为资源类型，成员可以是任意资源（包括嵌套子图）</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求9: 拓扑数据查询</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
public class TopologyGraphDTO {

    /**
     * 节点列表
     */
    private List<TopologyNodeDTO> nodes = new ArrayList<>();

    /**
     * 边列表
     */
    private List<TopologyEdgeDTO> edges = new ArrayList<>();

    /**
     * 子图边界列表（用于渲染嵌套子图区域）
     */
    private List<SubgraphBoundaryDTO> subgraphBoundaries = new ArrayList<>();

    // ==================== Constructors ====================

    public TopologyGraphDTO() {
    }

    // ==================== Getters and Setters ====================

    public List<TopologyNodeDTO> getNodes() {
        return nodes;
    }

    public void setNodes(List<TopologyNodeDTO> nodes) {
        this.nodes = nodes;
    }

    public List<TopologyEdgeDTO> getEdges() {
        return edges;
    }

    public void setEdges(List<TopologyEdgeDTO> edges) {
        this.edges = edges;
    }

    public List<SubgraphBoundaryDTO> getSubgraphBoundaries() {
        return subgraphBoundaries;
    }

    public void setSubgraphBoundaries(List<SubgraphBoundaryDTO> subgraphBoundaries) {
        this.subgraphBoundaries = subgraphBoundaries;
    }

    public void addNode(TopologyNodeDTO node) {
        this.nodes.add(node);
    }

    public void addEdge(TopologyEdgeDTO edge) {
        this.edges.add(edge);
    }

    public void addSubgraphBoundary(SubgraphBoundaryDTO boundary) {
        this.subgraphBoundaries.add(boundary);
    }

    // ==================== 嵌套类 ====================

    /**
     * 拓扑节点 DTO
     */
    public static class TopologyNodeDTO {
        private Long id;
        private String name;
        private String typeCode;
        private String status;
        private boolean isSubgraph;
        private boolean expanded;
        private Long parentSubgraphId;

        public TopologyNodeDTO() {
        }

        public TopologyNodeDTO(Long id, String name, String typeCode, String status, boolean isSubgraph) {
            this.id = id;
            this.name = name;
            this.typeCode = typeCode;
            this.status = status;
            this.isSubgraph = isSubgraph;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTypeCode() {
            return typeCode;
        }

        public void setTypeCode(String typeCode) {
            this.typeCode = typeCode;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isSubgraph() {
            return isSubgraph;
        }

        public void setSubgraph(boolean subgraph) {
            isSubgraph = subgraph;
        }

        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }

        public Long getParentSubgraphId() {
            return parentSubgraphId;
        }

        public void setParentSubgraphId(Long parentSubgraphId) {
            this.parentSubgraphId = parentSubgraphId;
        }
    }

    /**
     * 拓扑边 DTO
     */
    public static class TopologyEdgeDTO {
        private Long sourceId;
        private Long targetId;
        private String relationshipType;
        private String direction;
        private String strength;
        private String status;

        public TopologyEdgeDTO() {
        }

        public TopologyEdgeDTO(Long sourceId, Long targetId, String relationshipType) {
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.relationshipType = relationshipType;
        }

        public Long getSourceId() {
            return sourceId;
        }

        public void setSourceId(Long sourceId) {
            this.sourceId = sourceId;
        }

        public Long getTargetId() {
            return targetId;
        }

        public void setTargetId(Long targetId) {
            this.targetId = targetId;
        }

        public String getRelationshipType() {
            return relationshipType;
        }

        public void setRelationshipType(String relationshipType) {
            this.relationshipType = relationshipType;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public String getStrength() {
            return strength;
        }

        public void setStrength(String strength) {
            this.strength = strength;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * 子图边界 DTO
     */
    public static class SubgraphBoundaryDTO {
        private Long subgraphId;
        private String name;
        private List<Long> memberIds;

        public SubgraphBoundaryDTO() {
        }

        public SubgraphBoundaryDTO(Long subgraphId, String name, List<Long> memberIds) {
            this.subgraphId = subgraphId;
            this.name = name;
            this.memberIds = memberIds;
        }

        public Long getSubgraphId() {
            return subgraphId;
        }

        public void setSubgraphId(Long subgraphId) {
            this.subgraphId = subgraphId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Long> getMemberIds() {
            return memberIds;
        }

        public void setMemberIds(List<Long> memberIds) {
            this.memberIds = memberIds;
        }
    }
}
