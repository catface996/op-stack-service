package com.catface996.aiops.interface_.http.response.subgraph;

import com.catface996.aiops.application.dto.subgraph.SubgraphMemberDTO;
import com.catface996.aiops.application.dto.subgraph.SubgraphMembersWithRelationsDTO;

import java.util.List;

/**
 * 子图成员和关系响应
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
public class SubgraphMembersWithRelationsResponse {

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
    private List<SubgraphMemberDTO> members;

    /**
     * 成员间的关系列表
     */
    private List<SubgraphMembersWithRelationsDTO.RelationshipDTO> relationships;

    /**
     * 嵌套子图信息列表
     */
    private List<SubgraphMembersWithRelationsDTO.NestedSubgraphDTO> nestedSubgraphs;

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

    public SubgraphMembersWithRelationsResponse() {
    }

    // ==================== Static Factory ====================

    public static SubgraphMembersWithRelationsResponse from(SubgraphMembersWithRelationsDTO dto) {
        if (dto == null) {
            return null;
        }
        SubgraphMembersWithRelationsResponse response = new SubgraphMembersWithRelationsResponse();
        response.setSubgraphId(dto.getSubgraphId());
        response.setSubgraphName(dto.getSubgraphName());
        response.setMembers(dto.getMembers());
        response.setRelationships(dto.getRelationships());
        response.setNestedSubgraphs(dto.getNestedSubgraphs());
        response.setNodeCount(dto.getNodeCount());
        response.setEdgeCount(dto.getEdgeCount());
        response.setMaxDepth(dto.getMaxDepth());
        return response;
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

    public List<SubgraphMembersWithRelationsDTO.RelationshipDTO> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<SubgraphMembersWithRelationsDTO.RelationshipDTO> relationships) {
        this.relationships = relationships;
    }

    public List<SubgraphMembersWithRelationsDTO.NestedSubgraphDTO> getNestedSubgraphs() {
        return nestedSubgraphs;
    }

    public void setNestedSubgraphs(List<SubgraphMembersWithRelationsDTO.NestedSubgraphDTO> nestedSubgraphs) {
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
}
