package com.catface996.aiops.application.dto.subgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * 子图成员和关系 DTO
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
public class SubgraphMembersWithRelationsDTO {

    /**
     * 子图资源 ID
     */
    private Long subgraphId;

    /**
     * 子图名称
     */
    private String subgraphName;

    /**
     * 成员列表
     */
    private List<SubgraphMemberDTO> members = new ArrayList<>();

    /**
     * 成员间的关系列表
     */
    private List<RelationshipDTO> relationships = new ArrayList<>();

    /**
     * 嵌套子图信息列表
     */
    private List<NestedSubgraphDTO> nestedSubgraphs = new ArrayList<>();

    /**
     * 节点数量
     */
    private int nodeCount;

    /**
     * 边数量
     */
    private int edgeCount;

    /**
     * 最大深度
     */
    private int maxDepth;

    // ==================== Constructors ====================

    public SubgraphMembersWithRelationsDTO() {
    }

    // ==================== Getters and Setters ====================

    public Long getSubgraphId() {
        return subgraphId;
    }

    public void setSubgraphId(Long subgraphId) {
        this.subgraphId = subgraphId;
    }

    public String getSubgraphName() {
        return subgraphName;
    }

    public void setSubgraphName(String subgraphName) {
        this.subgraphName = subgraphName;
    }

    public List<SubgraphMemberDTO> getMembers() {
        return members;
    }

    public void setMembers(List<SubgraphMemberDTO> members) {
        this.members = members;
    }

    public List<RelationshipDTO> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<RelationshipDTO> relationships) {
        this.relationships = relationships;
    }

    public List<NestedSubgraphDTO> getNestedSubgraphs() {
        return nestedSubgraphs;
    }

    public void setNestedSubgraphs(List<NestedSubgraphDTO> nestedSubgraphs) {
        this.nestedSubgraphs = nestedSubgraphs;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public void setEdgeCount(int edgeCount) {
        this.edgeCount = edgeCount;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    // ==================== 嵌套类 ====================

    /**
     * 关系 DTO
     */
    public static class RelationshipDTO {
        private Long sourceId;
        private Long targetId;
        private String relationshipType;
        private String direction;
        private String strength;
        private String status;

        public RelationshipDTO() {
        }

        public RelationshipDTO(Long sourceId, Long targetId, String relationshipType) {
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
     * 嵌套子图 DTO
     */
    public static class NestedSubgraphDTO {
        private Long subgraphId;
        private String name;
        private Long parentSubgraphId;
        private int depth;
        private int memberCount;
        private boolean expanded;

        public NestedSubgraphDTO() {
        }

        public NestedSubgraphDTO(Long subgraphId, String name, Long parentSubgraphId, int depth, int memberCount) {
            this.subgraphId = subgraphId;
            this.name = name;
            this.parentSubgraphId = parentSubgraphId;
            this.depth = depth;
            this.memberCount = memberCount;
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

        public Long getParentSubgraphId() {
            return parentSubgraphId;
        }

        public void setParentSubgraphId(Long parentSubgraphId) {
            this.parentSubgraphId = parentSubgraphId;
        }

        public int getDepth() {
            return depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }

        public int getMemberCount() {
            return memberCount;
        }

        public void setMemberCount(int memberCount) {
            this.memberCount = memberCount;
        }

        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }
    }
}
