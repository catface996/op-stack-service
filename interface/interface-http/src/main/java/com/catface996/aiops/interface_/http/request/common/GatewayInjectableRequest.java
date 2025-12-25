package com.catface996.aiops.interface_.http.request.common;

/**
 * 网关可注入请求接口
 *
 * <p>定义网关可注入的标准字段，便于统一处理网关注入的参数</p>
 *
 * <p>网关注入字段：</p>
 * <ul>
 *   <li>tenantId - 租户ID，用于多租户隔离</li>
 *   <li>traceId - 追踪ID，用于分布式追踪</li>
 *   <li>userId - 用户ID，用于审计日志</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>Feature 024: POST-Only API 重构</li>
 *   <li>FR-009: 网关注入参数支持</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
public interface GatewayInjectableRequest {

    /**
     * 获取租户ID
     *
     * @return 租户ID
     */
    Long getTenantId();

    /**
     * 设置租户ID
     *
     * @param tenantId 租户ID
     */
    void setTenantId(Long tenantId);

    /**
     * 获取追踪ID
     *
     * @return 追踪ID
     */
    String getTraceId();

    /**
     * 设置追踪ID
     *
     * @param traceId 追踪ID
     */
    void setTraceId(String traceId);

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    String getUserId();

    /**
     * 设置用户ID
     *
     * @param userId 用户ID
     */
    void setUserId(String userId);
}
