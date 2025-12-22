package com.catface996.aiops.application.dto.subgraph;

import java.util.List;

/**
 * 添加成员命令 DTO
 *
 * <p>v2.0 设计：子图作为资源类型，成员可以是任意资源（包括嵌套子图）</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求5: 向子图添加成员资源</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
public class AddMembersCommand {

    /**
     * 子图资源 ID
     */
    private Long subgraphId;

    /**
     * 要添加的成员资源 ID 列表（可以包含子图 ID 实现嵌套）
     */
    private List<Long> memberIds;

    /**
     * 操作者用户 ID
     */
    private Long operatorId;

    /**
     * 操作者用户名
     */
    private String operatorName;

    // ==================== Constructors ====================

    public AddMembersCommand() {
    }

    public AddMembersCommand(Long subgraphId, List<Long> memberIds, Long operatorId) {
        this.subgraphId = subgraphId;
        this.memberIds = memberIds;
        this.operatorId = operatorId;
    }

    // ==================== Getters and Setters ====================

    public Long getSubgraphId() {
        return subgraphId;
    }

    public void setSubgraphId(Long subgraphId) {
        this.subgraphId = subgraphId;
    }

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    @Override
    public String toString() {
        return "AddMembersCommand{" +
                "subgraphId=" + subgraphId +
                ", memberIds=" + memberIds +
                ", operatorId=" + operatorId +
                '}';
    }
}
