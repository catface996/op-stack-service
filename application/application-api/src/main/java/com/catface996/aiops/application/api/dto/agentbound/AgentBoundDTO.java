package com.catface996.aiops.application.api.dto.agentbound;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agent 绑定关系 DTO
 *
 * <p>用于返回单个绑定关系的详细信息。</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent 绑定关系信息")
public class AgentBoundDTO {

    @Schema(description = "绑定记录 ID", example = "1")
    private Long id;

    @Schema(description = "Agent ID", example = "100")
    private Long agentId;

    @Schema(description = "Agent 名称", example = "性能分析 Agent")
    private String agentName;

    @Schema(description = "Agent 角色", example = "WORKER")
    private String agentRole;

    @Schema(description = "Agent 层级", example = "TEAM_WORKER")
    private String hierarchyLevel;

    @Schema(description = "实体 ID", example = "10")
    private Long entityId;

    @Schema(description = "实体类型", example = "NODE")
    private String entityType;

    @Schema(description = "实体名称", example = "Web Server")
    private String entityName;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
