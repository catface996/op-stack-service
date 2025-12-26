package com.catface996.aiops.application.api.dto.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 节点类型DTO
 *
 * <p>用于节点类型列表和详情展示。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: resource 表拆分为 topology 表和 node 表</li>
 *   <li>FR-010: resource_type 表重命名为 node_type 表</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "节点类型信息")
public class NodeTypeDTO {

    @Schema(description = "类型ID", example = "1")
    private Long id;

    @Schema(description = "类型编码", example = "SERVER")
    private String code;

    @Schema(description = "类型名称", example = "服务器")
    private String name;

    @Schema(description = "类型描述", example = "物理或虚拟服务器")
    private String description;

    @Schema(description = "图标（可选）", example = "server-icon")
    private String icon;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
