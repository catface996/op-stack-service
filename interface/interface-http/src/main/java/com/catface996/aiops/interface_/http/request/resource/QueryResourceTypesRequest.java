package com.catface996.aiops.interface_.http.request.resource;

import com.catface996.aiops.interface_.http.request.common.PageableRequest;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 查询资源类型列表请求
 *
 * <p>用于 POST /resource-types/query 接口</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>Feature 024: POST-Only API 重构</li>
 *   <li>原接口: GET /resource-types</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@Schema(description = "查询资源类型列表请求")
public class QueryResourceTypesRequest extends PageableRequest {

    // 资源类型查询目前无额外过滤参数
    // 可扩展添加如 name 过滤等

    // ==================== Constructors ====================

    public QueryResourceTypesRequest() {
    }
}
