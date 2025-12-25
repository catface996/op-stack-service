package com.catface996.aiops.domain.constant;

/**
 * 资源类型常量
 *
 * <p>定义资源分类体系中的常量值，用于区分拓扑图和资源节点。</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001: 系统必须支持将资源分为拓扑图和资源节点两大类</li>
 *   <li>FR-007: 系统必须支持现有数据的自动识别</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-25
 */
public final class ResourceTypeConstants {

    private ResourceTypeConstants() {
        // 防止实例化
    }

    /**
     * 拓扑图（子图）类型编码
     *
     * <p>当 resource_type.code = 'SUBGRAPH' 时，该资源被识别为拓扑图。</p>
     * <p>其他所有类型编码对应的资源被识别为资源节点。</p>
     */
    public static final String SUBGRAPH_CODE = "SUBGRAPH";

    /**
     * 资源节点类型编码列表（非拓扑图）
     */
    public static final String SERVER_CODE = "SERVER";
    public static final String APPLICATION_CODE = "APPLICATION";
    public static final String DATABASE_CODE = "DATABASE";
    public static final String API_CODE = "API";
    public static final String MIDDLEWARE_CODE = "MIDDLEWARE";
    public static final String REPORT_CODE = "REPORT";

    /**
     * 判断给定的类型编码是否为拓扑图类型
     *
     * @param code 资源类型编码
     * @return true 如果是拓扑图类型
     */
    public static boolean isTopologyType(String code) {
        return SUBGRAPH_CODE.equals(code);
    }

    /**
     * 判断给定的类型编码是否为资源节点类型（非拓扑图）
     *
     * @param code 资源类型编码
     * @return true 如果是资源节点类型
     */
    public static boolean isResourceNodeType(String code) {
        return code != null && !SUBGRAPH_CODE.equals(code);
    }
}
