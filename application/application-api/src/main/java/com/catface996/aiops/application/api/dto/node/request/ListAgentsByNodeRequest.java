package com.catface996.aiops.application.api.dto.node.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查询节点关联的 Agent 列表请求
 *
 * <p>查询指定资源节点关联的所有 Agent</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-004: 系统必须提供查询接口，支持根据节点 ID 查询关联的 Agent 列表</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@Schema(description = "查询节点关联的 Agent 列表请求")
public class ListAgentsByNodeRequest {

    @NotNull(message = "节点ID不能为空")
    @Schema(description = "节点ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long nodeId;
}
