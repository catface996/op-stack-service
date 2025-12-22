package com.catface996.aiops.application.dto.subgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * 子图祖先 DTO
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
public class SubgraphAncestorsDTO {

    /**
     * 当前子图 ID
     */
    private Long subgraphId;

    /**
     * 祖先子图列表（按深度排序，depth=1 为直接父级）
     */
    private List<AncestorDTO> ancestors = new ArrayList<>();

    /**
     * 嵌套深度（祖先链长度）
     */
    private int maxDepth;

    // ==================== Constructors ====================

    public SubgraphAncestorsDTO() {
    }

    public SubgraphAncestorsDTO(Long subgraphId, List<AncestorDTO> ancestors) {
        this.subgraphId = subgraphId;
        this.ancestors = ancestors;
        this.maxDepth = ancestors != null && !ancestors.isEmpty()
                ? ancestors.stream().mapToInt(AncestorDTO::getDepth).max().orElse(0)
                : 0;
    }

    // ==================== Getters and Setters ====================

    public Long getSubgraphId() {
        return subgraphId;
    }

    public void setSubgraphId(Long subgraphId) {
        this.subgraphId = subgraphId;
    }

    public List<AncestorDTO> getAncestors() {
        return ancestors;
    }

    public void setAncestors(List<AncestorDTO> ancestors) {
        this.ancestors = ancestors;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    // ==================== 嵌套类 ====================

    /**
     * 祖先 DTO
     */
    public static class AncestorDTO {
        private Long subgraphId;
        private String name;
        private int depth;

        public AncestorDTO() {
        }

        public AncestorDTO(Long subgraphId, String name, int depth) {
            this.subgraphId = subgraphId;
            this.name = name;
            this.depth = depth;
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

        public int getDepth() {
            return depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }
    }
}
