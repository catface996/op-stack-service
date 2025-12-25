package com.catface996.aiops.application.dto.subgraph;

import java.time.LocalDateTime;

/**
 * 子图成员 DTO
 *
 * <p>v2.0 设计：子图作为资源类型，成员可以是任意资源（包括嵌套子图）</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求5: 向子图添加成员资源</li>
 *   <li>需求6: 从子图移除成员资源</li>
 *   <li>需求8: 成员列表查询</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
public class SubgraphMemberDTO {

    /**
     * 关联 ID
     */
    private Long id;

    /**
     * 成员资源 ID
     */
    private Long memberId;

    /**
     * 父子图 ID
     */
    private Long subgraphId;

    /**
     * 成员资源名称
     */
    private String memberName;

    /**
     * 成员资源类型（如 SERVER, APPLICATION, SUBGRAPH）
     */
    private String memberType;

    /**
     * 成员资源状态
     */
    private String memberStatus;

    /**
     * 该成员是否为子图
     */
    private Boolean isSubgraph;

    /**
     * 如果是子图，其包含的成员数量
     */
    private Integer nestedMemberCount;

    /**
     * 添加时间
     */
    private LocalDateTime addedAt;

    /**
     * 添加者用户 ID
     */
    private Long addedBy;

    // ==================== Constructors ====================

    public SubgraphMemberDTO() {
    }

    // ==================== Getters and Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getSubgraphId() {
        return subgraphId;
    }

    public void setSubgraphId(Long subgraphId) {
        this.subgraphId = subgraphId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public String getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(String memberStatus) {
        this.memberStatus = memberStatus;
    }

    public Boolean getIsSubgraph() {
        return isSubgraph;
    }

    public void setIsSubgraph(Boolean isSubgraph) {
        this.isSubgraph = isSubgraph;
    }

    public Integer getNestedMemberCount() {
        return nestedMemberCount;
    }

    public void setNestedMemberCount(Integer nestedMemberCount) {
        this.nestedMemberCount = nestedMemberCount;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public Long getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(Long addedBy) {
        this.addedBy = addedBy;
    }

    @Override
    public String toString() {
        return "SubgraphMemberDTO{" +
                "id=" + id +
                ", memberId=" + memberId +
                ", memberName='" + memberName + '\'' +
                ", memberType='" + memberType + '\'' +
                ", isSubgraph=" + isSubgraph +
                '}';
    }
}
