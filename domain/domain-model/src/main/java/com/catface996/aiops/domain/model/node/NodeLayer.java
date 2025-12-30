package com.catface996.aiops.domain.model.node;

/**
 * 资源节点架构层级枚举
 *
 * <p>定义资源节点所属的架构层级，从上到下依次为：</p>
 * <ul>
 *   <li>BUSINESS_SCENARIO - 业务场景层</li>
 *   <li>BUSINESS_FLOW - 业务流程层</li>
 *   <li>BUSINESS_APPLICATION - 业务应用层</li>
 *   <li>MIDDLEWARE - 中间件层</li>
 *   <li>INFRASTRUCTURE - 基础设施层</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-30
 */
public enum NodeLayer {

    /**
     * 业务场景层
     */
    BUSINESS_SCENARIO("Business Scenario", "业务场景", 1),

    /**
     * 业务流程层
     */
    BUSINESS_FLOW("Business Flow", "业务流程", 2),

    /**
     * 业务应用层
     */
    BUSINESS_APPLICATION("Business Application", "业务应用", 3),

    /**
     * 中间件层
     */
    MIDDLEWARE("Middleware", "中间件", 4),

    /**
     * 基础设施层
     */
    INFRASTRUCTURE("Infrastructure", "基础设施", 5);

    private final String displayName;
    private final String displayNameCn;
    private final int order;

    NodeLayer(String displayName, String displayNameCn, int order) {
        this.displayName = displayName;
        this.displayNameCn = displayNameCn;
        this.order = order;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplayNameCn() {
        return displayNameCn;
    }

    public int getOrder() {
        return order;
    }

    /**
     * 根据名称获取枚举值
     *
     * @param name 枚举名称
     * @return 枚举值，不存在返回 null
     */
    public static NodeLayer fromName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return NodeLayer.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
