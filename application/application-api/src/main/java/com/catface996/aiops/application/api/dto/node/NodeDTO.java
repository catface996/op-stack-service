package com.catface996.aiops.application.api.dto.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 资源节点DTO
 *
 * <p>用于节点列表和详情展示。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: resource 表拆分为 topology 表和 node 表</li>
 *   <li>FR-005: 节点 API 保持接口契约不变</li>
 *   <li>US2: 查询所有节点</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "资源节点信息")
public class NodeDTO {

    @Schema(description = "节点ID", example = "1")
    private Long id;

    @Schema(description = "节点名称", example = "web-server-01")
    private String name;

    @Schema(description = "节点描述", example = "生产环境Web服务器")
    private String description;

    @Schema(description = "节点类型ID", example = "1")
    private Long nodeTypeId;

    @Schema(description = "节点类型名称", example = "服务器")
    private String nodeTypeName;

    @Schema(description = "节点类型编码", example = "SERVER")
    private String nodeTypeCode;

    @Schema(description = "节点状态", example = "RUNNING")
    private String status;

    @Schema(description = "状态显示名称", example = "运行中")
    private String statusDisplay;

    @Schema(description = "架构层级", example = "BUSINESS_APPLICATION")
    private String layer;

    @Schema(description = "扩展属性（JSON格式）", example = "{\"ip\": \"192.168.1.100\", \"port\": 8080}")
    private String attributes;

    @Schema(description = "版本号（乐观锁）", example = "1")
    private Integer version;

    @Schema(description = "创建者ID", example = "1")
    private Long createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
