package com.catface996.aiops.interface_.http.request.subgraph;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 添加成员请求
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
public class AddMembersRequest {

    /**
     * 要添加的成员资源 ID 列表（可以包含子图 ID 实现嵌套）
     */
    @NotEmpty(message = "成员 ID 列表不能为空")
    @Size(min = 1, max = 100, message = "每次最多添加 100 个成员")
    private List<Long> memberIds;

    // ==================== Constructors ====================

    public AddMembersRequest() {
    }

    public AddMembersRequest(List<Long> memberIds) {
        this.memberIds = memberIds;
    }

    // ==================== Getters and Setters ====================

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }

    @Override
    public String toString() {
        return "AddMembersRequest{" +
                "memberIds=" + memberIds +
                '}';
    }
}
