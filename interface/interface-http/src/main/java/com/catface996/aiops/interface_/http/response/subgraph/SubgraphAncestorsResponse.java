package com.catface996.aiops.interface_.http.response.subgraph;

import com.catface996.aiops.application.dto.subgraph.SubgraphAncestorsDTO;

import java.util.List;

/**
 * 子图祖先响应
 *
 * <p>v2.0 设计：子图作为资源类型，成员可以是任意资源（包括嵌套子图）</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求7: 子图详情视图（祖先导航）</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
public class SubgraphAncestorsResponse {

    /**
     * 当前子图 ID
     */
    private Long subgraphId;

    /**
     * 祖先子图列表（按深度排序，depth=1 为直接父级）
     */
    private List<SubgraphAncestorsDTO.AncestorDTO> ancestors;

    /**
     * 嵌套深度（祖先链长度）
     */
    private int maxDepth;

    // ==================== Constructors ====================

    public SubgraphAncestorsResponse() {
    }

    // ==================== Static Factory ====================

    public static SubgraphAncestorsResponse from(SubgraphAncestorsDTO dto) {
        if (dto == null) {
            return null;
        }
        SubgraphAncestorsResponse response = new SubgraphAncestorsResponse();
        response.setSubgraphId(dto.getSubgraphId());
        response.setAncestors(dto.getAncestors());
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

    public List<SubgraphAncestorsDTO.AncestorDTO> getAncestors() {
        return ancestors;
    }

    public void setAncestors(List<SubgraphAncestorsDTO.AncestorDTO> ancestors) {
        this.ancestors = ancestors;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
}
