package com.catface996.aiops.interface_.http.request.subgraph;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "添加成员请求")
public class AddMembersRequest {

    @Schema(description = "操作者ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作者ID不能为空")
    private Long operatorId;

    /**
     * 资源 ID（POST-Only API 使用，从路径参数迁移到请求体）
     */
    @Schema(description = "资源ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;

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

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

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
        return "AddMembersRequest{" +
                "memberIds=" + memberIds +
                '}';
    }
}
