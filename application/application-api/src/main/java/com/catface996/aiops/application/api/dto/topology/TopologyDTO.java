package com.catface996.aiops.application.api.dto.topology;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 拓扑图DTO
 *
 * <p>用于拓扑图列表和详情展示，包含成员数量统计。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-002: 系统必须提供独立的拓扑图列表查询接口</li>
 *   <li>US1: 查询所有拓扑图</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "拓扑图信息")
public class TopologyDTO {

    @Schema(description = "拓扑图ID", example = "101")
    private Long id;

    @Schema(description = "拓扑图名称", example = "电商平台拓扑图")
    private String name;

    @Schema(description = "拓扑图描述", example = "展示电商平台核心组件的关系")
    private String description;

    @Schema(description = "状态", example = "RUNNING")
    private String status;

    @Schema(description = "状态显示名称", example = "运行中")
    private String statusDisplay;

    @Schema(description = "协调 Agent ID")
    private Long coordinatorAgentId;

    @Schema(description = "Global Supervisor Agent ID")
    private Long globalSupervisorAgentId;

    @Schema(description = "Global Supervisor Agent 名称")
    private String globalSupervisorAgentName;

    @Schema(description = "Global Supervisor Agent 角色")
    private String globalSupervisorAgentRole;

    @Schema(description = "扩展属性（JSON格式）", example = "{}")
    private String attributes;

    @Schema(description = "成员数量", example = "15")
    private Integer memberCount;

    @Schema(description = "版本号（乐观锁）", example = "1")
    private Integer version;

    @Schema(description = "创建者ID", example = "1")
    private Long createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
