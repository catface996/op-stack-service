package com.catface996.aiops.application.api.dto.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Node-Agent 关联 DTO
 *
 * <p>用于返回 Node-Agent 绑定关系信息</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Data
@Schema(description = "Node-Agent 关联信息")
public class NodeAgentRelationDTO {

    @Schema(description = "关联记录 ID", example = "1")
    private Long id;

    @Schema(description = "资源节点 ID", example = "1")
    private Long nodeId;

    @Schema(description = "Agent ID", example = "1")
    private Long agentId;

    @Schema(description = "创建时间", example = "2025-12-28T10:30:00")
    private LocalDateTime createdAt;
}
