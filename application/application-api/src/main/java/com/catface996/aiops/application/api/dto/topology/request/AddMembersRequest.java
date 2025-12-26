package com.catface996.aiops.application.api.dto.topology.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 添加成员请求
 *
 * <p>用于向拓扑图中添加资源节点成员。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-008: 拓扑图成员管理接口</li>
 *   <li>US5: 拓扑图成员管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "添加成员请求")
public class AddMembersRequest {

    @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    @Schema(description = "拓扑图ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "拓扑图ID不能为空")
    private Long topologyId;

    @Schema(description = "要添加的节点ID列表", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "节点ID列表不能为空")
    private List<Long> nodeIds;
}
