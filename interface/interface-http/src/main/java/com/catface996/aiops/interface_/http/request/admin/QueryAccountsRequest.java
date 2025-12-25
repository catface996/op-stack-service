package com.catface996.aiops.interface_.http.request.admin;

import com.catface996.aiops.interface_.http.request.common.PageableRequest;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 查询账号列表请求
 *
 * <p>用于 POST /admin/accounts/query 接口</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>Feature 024: POST-Only API 重构</li>
 *   <li>原接口: GET /admin/accounts</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@Schema(description = "查询账号列表请求")
public class QueryAccountsRequest extends PageableRequest {

    // 继承自 PageableRequest 的字段:
    // - page: 页码（从1开始）
    // - size: 每页大小
    // - tenantId: 网关注入的租户ID
    // - traceId: 网关注入的追踪ID

    // ==================== Constructors ====================

    public QueryAccountsRequest() {
        super();
    }

    public QueryAccountsRequest(Integer page, Integer size) {
        super();
        setPage(page);
        setSize(size);
    }
}
