package com.catface996.aiops.application.api.dto.node.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除节点请求
 *
 * <p>用于删除资源节点。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-004: 系统必须提供独立的节点管理接口</li>
 *   <li>US4: 节点管理（删除属于节点管理范畴）</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "删除节点请求")
public class DeleteNodeRequest {

    @Schema(description = "操作人ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    @Schema(description = "节点ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "节点ID不能为空")
    private Long id;
}
