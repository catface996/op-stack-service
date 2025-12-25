package com.catface996.aiops.interface_.http.request.subgraph;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 移除成员请求
 *
 * <p>v2.0 设计：子图作为资源类型，成员可以是任意资源（包括嵌套子图）</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求6: 从子图移除成员资源</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
public class RemoveMembersRequest {

    /**
     * 资源 ID（POST-Only API 使用，从路径参数迁移到请求体）
     */
    private Long resourceId;

    /**
     * 要移除的成员资源 ID 列表
     */
    @NotEmpty(message = "成员 ID 列表不能为空")
    @Size(min = 1, max = 100, message = "每次最多移除 100 个成员")
    private List<Long> memberIds;

    // ==================== Constructors ====================

    public RemoveMembersRequest() {
    }

    public RemoveMembersRequest(List<Long> memberIds) {
        this.memberIds = memberIds;
    }

    // ==================== Getters and Setters ====================

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }

    @Override
    public String toString() {
        return "RemoveMembersRequest{" +
                "memberIds=" + memberIds +
                '}';
    }
}
