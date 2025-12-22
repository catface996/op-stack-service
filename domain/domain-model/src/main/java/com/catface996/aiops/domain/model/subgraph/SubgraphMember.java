package com.catface996.aiops.domain.model.subgraph;

import java.time.LocalDateTime;

/**
 * 子图成员领域模型
 *
 * <p>v2.0 设计：子图作为资源类型，成员可以是任意资源（包括嵌套子图）</p>
 * <p>表示子图与成员资源之间的关联关系</p>
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
public class SubgraphMember {

    /**
     * 关联ID
     */
    private Long id;

    /**
     * 父子图资源ID（必须是 SUBGRAPH 类型的 resource）
     */
    private Long subgraphId;

    /**
     * 成员资源ID（可以是任意类型包括 SUBGRAPH，实现嵌套）
     */
    private Long memberId;

    /**
     * 添加时间
     */
    private LocalDateTime addedAt;

    /**
     * 添加者用户ID
     */
    private Long addedBy;

    // ==================== 派生属性（用于展示） ====================

    /**
     * 成员资源名称
     */
    private String memberName;

    /**
     * 成员资源类型代码（如 SERVER, APPLICATION, SUBGRAPH）
     */
    private String memberTypeCode;

    /**
     * 成员资源状态
     */
    private String memberStatus;

    /**
     * 成员是否为子图类型
     */
    private boolean isSubgraph;

    /**
     * 如果成员是子图，其包含的成员数量
     */
    private int nestedMemberCount;

    // ==================== Constructors ====================

    public SubgraphMember() {
    }

    public SubgraphMember(Long subgraphId, Long memberId, Long addedBy) {
        this.subgraphId = subgraphId;
        this.memberId = memberId;
        this.addedBy = addedBy;
        this.addedAt = LocalDateTime.now();
    }

    // ==================== Getters and Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubgraphId() {
        return subgraphId;
    }

    public void setSubgraphId(Long subgraphId) {
        this.subgraphId = subgraphId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
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

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberTypeCode() {
        return memberTypeCode;
    }

    public void setMemberTypeCode(String memberTypeCode) {
        this.memberTypeCode = memberTypeCode;
    }

    public String getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(String memberStatus) {
        this.memberStatus = memberStatus;
    }

    public boolean isSubgraph() {
        return isSubgraph;
    }

    public void setSubgraph(boolean subgraph) {
        isSubgraph = subgraph;
    }

    public int getNestedMemberCount() {
        return nestedMemberCount;
    }

    public void setNestedMemberCount(int nestedMemberCount) {
        this.nestedMemberCount = nestedMemberCount;
    }

    @Override
    public String toString() {
        return "SubgraphMember{" +
                "id=" + id +
                ", subgraphId=" + subgraphId +
                ", memberId=" + memberId +
                ", memberName='" + memberName + '\'' +
                ", memberTypeCode='" + memberTypeCode + '\'' +
                ", isSubgraph=" + isSubgraph +
                '}';
    }
}
