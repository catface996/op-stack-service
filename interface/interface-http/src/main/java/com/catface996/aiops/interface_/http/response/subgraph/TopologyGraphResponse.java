package com.catface996.aiops.interface_.http.response.subgraph;

import com.catface996.aiops.application.dto.subgraph.TopologyGraphDTO;

import java.util.List;

/**
 * 拓扑图响应
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
public class TopologyGraphResponse {

    /**
     * 节点列表
     */
    private List<TopologyGraphDTO.TopologyNodeDTO> nodes;

    /**
     * 边列表
     */
    private List<TopologyGraphDTO.TopologyEdgeDTO> edges;

    /**
     * 子图边界列表（用于渲染嵌套子图区域）
     */
    private List<TopologyGraphDTO.SubgraphBoundaryDTO> subgraphBoundaries;

    // ==================== Constructors ====================

    public TopologyGraphResponse() {
    }

    // ==================== Static Factory ====================

    public static TopologyGraphResponse from(TopologyGraphDTO dto) {
        if (dto == null) {
            return null;
        }
        TopologyGraphResponse response = new TopologyGraphResponse();
        response.setNodes(dto.getNodes());
        response.setEdges(dto.getEdges());
        response.setSubgraphBoundaries(dto.getSubgraphBoundaries());
        return response;
    }

    // ==================== Getters and Setters ====================

    public List<TopologyGraphDTO.TopologyNodeDTO> getNodes() {
        return nodes;
    }

    public void setNodes(List<TopologyGraphDTO.TopologyNodeDTO> nodes) {
        this.nodes = nodes;
    }

    public List<TopologyGraphDTO.TopologyEdgeDTO> getEdges() {
        return edges;
    }

    public void setEdges(List<TopologyGraphDTO.TopologyEdgeDTO> edges) {
        this.edges = edges;
    }

    public List<TopologyGraphDTO.SubgraphBoundaryDTO> getSubgraphBoundaries() {
        return subgraphBoundaries;
    }

    public void setSubgraphBoundaries(List<TopologyGraphDTO.SubgraphBoundaryDTO> subgraphBoundaries) {
        this.subgraphBoundaries = subgraphBoundaries;
    }
}
