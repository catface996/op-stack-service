package com.catface996.aiops.interface_.http.response.subgraph;

import com.catface996.aiops.application.dto.subgraph.SubgraphMemberDTO;

import java.util.List;

/**
 * 子图成员列表响应
 *
 * <p>v2.0 设计：子图作为资源类型，成员可以是任意资源（包括嵌套子图）</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求8: 成员列表查询</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
public class SubgraphMemberListResponse {

    /**
     * 成员列表
     */
    private List<SubgraphMemberDTO> content;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总元素数
     */
    private Long totalElements;

    /**
     * 总页数
     */
    private Integer totalPages;

    // ==================== Constructors ====================

    public SubgraphMemberListResponse() {
    }

    public SubgraphMemberListResponse(List<SubgraphMemberDTO> content, Integer page, Integer size, Long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    }

    // ==================== Static Factory ====================

    public static SubgraphMemberListResponse of(List<SubgraphMemberDTO> content, int page, int size, long total) {
        return new SubgraphMemberListResponse(content, page, size, total);
    }

    // ==================== Getters and Setters ====================

    public List<SubgraphMemberDTO> getContent() {
        return content;
    }

    public void setContent(List<SubgraphMemberDTO> content) {
        this.content = content;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}
