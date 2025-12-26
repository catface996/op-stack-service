package com.catface996.aiops.application.api.dto.node.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取节点详情请求
 *
 * <p>用于获取单个节点的详细信息。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-003: 系统必须提供独立的节点详情查询接口</li>
 *   <li>US2: 查询节点详情</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "获取节点详情请求")
public class GetNodeRequest {

    @Schema(description = "节点ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "节点ID不能为空")
    private Long id;
}
