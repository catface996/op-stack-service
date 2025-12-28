package com.catface996.aiops.repository.mysql.config;

/**
 * 用户上下文
 *
 * <p>使用 ThreadLocal 存储当前用户 ID，供审计字段自动填充使用</p>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 在请求过滤器或拦截器中设置
 * UserContext.setCurrentUserId(userId);
 *
 * // 业务操作完成后清理
 * UserContext.clear();
 * }</pre>
 * </p>
 */
public class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    /**
     * 设置当前用户 ID
     *
     * @param userId 用户 ID
     */
    public static void setCurrentUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    /**
     * 获取当前用户 ID
     *
     * @return 当前用户 ID，如果未设置则返回 null
     */
    public static Long getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    /**
     * 清除当前用户 ID
     *
     * <p>应在请求处理完成后调用，防止内存泄漏</p>
     */
    public static void clear() {
        CURRENT_USER_ID.remove();
    }
}
