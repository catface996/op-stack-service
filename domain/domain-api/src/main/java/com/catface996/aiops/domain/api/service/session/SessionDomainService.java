package com.catface996.aiops.domain.api.service.session;

import com.catface996.aiops.domain.model.auth.DeviceInfo;
import com.catface996.aiops.domain.model.auth.Session;
import com.catface996.aiops.domain.model.auth.SessionValidationResult;

import java.util.List;

/**
 * 会话领域服务接口
 *
 * <p>提供会话管理的核心业务方法，包括：</p>
 * <ul>
 *   <li>会话创建（生成UUID标识符、清理超限会话、先写MySQL再写Redis）</li>
 *   <li>会话验证（先读Redis缓存、缓存未命中则读MySQL、检查超时、更新活动时间）</li>
 *   <li>会话销毁（先删MySQL再删Redis）</li>
 *   <li>会话数量限制和IP变化检测</li>
 *   <li>Redis故障时的降级处理</li>
 * </ul>
 *
 * <p>存储策略（Cache-Aside模式）：</p>
 * <ul>
 *   <li>主存储：MySQL（提供数据持久化和可靠性保证）</li>
 *   <li>缓存层：Redis（提供高性能读取能力）</li>
 *   <li>写操作：先写MySQL，再更新Redis</li>
 *   <li>读操作：先读Redis，未命中则读MySQL并回写Redis</li>
 *   <li>删除操作：先删MySQL，再删Redis</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 1.1, 1.2, 1.3, 1.4, 1.5: 会话生命周期管理</li>
 *   <li>REQ 3.1, 3.2, 3.3, 3.4, 3.5, 3.6: 安全和降级策略</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-28
 */
public interface SessionDomainService {

    /**
     * 创建会话
     *
     * <p>创建会话的流程：</p>
     * <ol>
     *   <li>生成UUID作为会话标识符</li>
     *   <li>检查并清理超限会话</li>
     *   <li>先写MySQL（主存储）</li>
     *   <li>再写Redis（缓存层，失败不阻塞主流程）</li>
     * </ol>
     *
     * @param userId 用户ID
     * @param deviceInfo 设备信息
     * @param absoluteTimeout 绝对超时时长（秒）
     * @param idleTimeout 空闲超时时长（秒）
     * @param rememberMe 是否记住我
     * @return 创建的会话实体
     * @throws IllegalArgumentException 如果userId为null或deviceInfo为null
     */
    Session createSession(Long userId, DeviceInfo deviceInfo,
                         int absoluteTimeout, int idleTimeout, boolean rememberMe);

    /**
     * 验证并刷新会话
     *
     * <p>验证会话的流程：</p>
     * <ol>
     *   <li>先读Redis缓存</li>
     *   <li>缓存未命中则读MySQL并回写Redis</li>
     *   <li>检查绝对超时</li>
     *   <li>检查空闲超时</li>
     *   <li>更新最后活动时间</li>
     * </ol>
     *
     * @param sessionId 会话标识符
     * @return 验证结果（包含会话实体和警告信息）
     * @throws com.catface996.aiops.common.exception.BusinessException 会话验证失败
     *         - SessionErrorCode.SESSION_EXPIRED: 会话绝对超时
     *         - SessionErrorCode.SESSION_IDLE_TIMEOUT: 会话空闲超时
     *         - SessionErrorCode.SESSION_NOT_FOUND: 会话不存在
     */
    SessionValidationResult validateAndRefreshSession(String sessionId);

    /**
     * 销毁会话
     *
     * <p>销毁会话的流程：</p>
     * <ol>
     *   <li>先删MySQL</li>
     *   <li>再删Redis（失败不阻塞主流程）</li>
     * </ol>
     *
     * @param sessionId 会话标识符
     * @throws IllegalArgumentException 如果sessionId为空或null
     */
    void destroySession(String sessionId);

    /**
     * 查询用户的所有会话
     *
     * @param userId 用户ID
     * @return 会话列表（按创建时间倒序）
     * @throws IllegalArgumentException 如果userId为null
     */
    List<Session> findUserSessions(Long userId);

    /**
     * 检查并清理超限会话
     *
     * <p>当用户的会话数量超过限制时，删除最旧的会话。</p>
     *
     * @param userId 用户ID
     * @param maxSessions 最大会话数（默认5个）
     * @return 被清理的会话数量
     * @throws IllegalArgumentException 如果userId为null或maxSessions <= 0
     */
    int enforceSessionLimit(Long userId, int maxSessions);

    /**
     * 检查IP地址变化
     *
     * <p>比较会话中记录的IP地址与当前请求的IP地址。</p>
     * <p>如果检测到IP变化，记录警告日志。</p>
     *
     * @param session 会话对象
     * @param currentIp 当前IP地址
     * @return true if IP changed, false otherwise
     * @throws IllegalArgumentException 如果session或currentIp为null
     */
    boolean checkIpChange(Session session, String currentIp);

    /**
     * 终止用户的其他会话
     *
     * <p>终止指定用户除当前会话外的所有会话。</p>
     *
     * @param currentSessionId 当前会话标识符（需要保留）
     * @param userId 用户ID
     * @return 终止的会话数量
     * @throws IllegalArgumentException 如果currentSessionId或userId为null
     */
    int terminateOtherSessions(String currentSessionId, Long userId);

    /**
     * 更新会话活动时间
     *
     * <p>更新会话的最后活动时间，用于重置空闲超时计时器。</p>
     *
     * @param sessionId 会话标识符
     * @throws IllegalArgumentException 如果sessionId为空或null
     */
    void updateLastActivity(String sessionId);
}
