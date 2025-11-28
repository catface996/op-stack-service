package com.catface996.aiops.repository.auth;

import com.catface996.aiops.domain.model.auth.Session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 会话仓储接口
 *
 * <p>提供会话实体的数据访问操作，遵循DDD仓储模式。</p>
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
 * <p>实现说明：</p>
 * <ul>
 *   <li>MySQL表：t_session</li>
 *   <li>Redis Key格式：session:{sessionId}</li>
 *   <li>用户会话列表：user:sessions:{userId} (Set类型)</li>
 *   <li>Redis TTL：等于会话绝对超时时长</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 1.1, 1.4, 1.5: 会话存储</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
public interface SessionRepository {

    /**
     * 保存会话
     *
     * <p>保存会话实体到MySQL（主存储），然后更新Redis缓存。</p>
     * <p>Redis缓存失败不影响主流程，仅记录日志。</p>
     *
     * @param session 会话实体
     * @return 保存后的会话实体
     * @throws IllegalArgumentException 如果session为null
     */
    Session save(Session session);

    /**
     * 根据会话ID查询会话
     *
     * <p>先读Redis缓存，未命中则读MySQL并回写Redis。</p>
     *
     * @param sessionId 会话ID（UUID格式）
     * @return 会话实体（如果存在）
     * @throws IllegalArgumentException 如果sessionId为空或null
     */
    Optional<Session> findById(String sessionId);

    /**
     * 根据用户ID查询单个会话（兼容旧接口）
     *
     * <p>查询用户最近的一个会话。</p>
     *
     * @param userId 用户ID
     * @return 会话实体（如果存在）
     * @throws IllegalArgumentException 如果userId为null
     */
    Optional<Session> findByUserId(Long userId);

    /**
     * 根据用户ID查询所有会话
     *
     * <p>查询指定用户的所有活跃会话，支持多设备会话管理。</p>
     *
     * @param userId 用户ID
     * @return 会话列表
     * @throws IllegalArgumentException 如果userId为null
     */
    List<Session> findAllByUserId(Long userId);

    /**
     * 根据会话ID删除会话
     *
     * <p>先删MySQL，再删Redis。</p>
     * <p>Redis删除失败不影响主流程，仅记录日志。</p>
     *
     * @param sessionId 会话ID
     * @throws IllegalArgumentException 如果sessionId为空或null
     */
    void deleteById(String sessionId);

    /**
     * 根据用户ID删除该用户的所有会话
     *
     * @param userId 用户ID
     * @throws IllegalArgumentException 如果userId为null
     */
    void deleteByUserId(Long userId);

    /**
     * 批量删除会话
     *
     * <p>根据会话ID列表批量删除会话。</p>
     * <p>用于清理超限会话或批量登出。</p>
     *
     * @param sessionIds 会话ID列表
     * @throws IllegalArgumentException 如果sessionIds为null或空
     */
    void batchDelete(List<String> sessionIds);

    /**
     * 检查会话是否存在
     *
     * <p>先检查Redis缓存，未命中则检查MySQL。</p>
     *
     * @param sessionId 会话ID
     * @return true if session exists, false otherwise
     * @throws IllegalArgumentException 如果sessionId为空或null
     */
    boolean existsById(String sessionId);

    /**
     * 更新会话过期时间
     *
     * <p>更新MySQL中的过期时间，并更新Redis缓存的TTL。</p>
     *
     * @param sessionId 会话ID
     * @param expiresAt 新的过期时间
     * @throws IllegalArgumentException 如果sessionId为空或expiresAt为null
     */
    void updateExpiresAt(String sessionId, LocalDateTime expiresAt);

    /**
     * 删除所有过期会话
     *
     * <p>删除MySQL中所有已过期的会话。</p>
     * <p>Redis会话通过TTL自动过期，无需手动清理。</p>
     * <p>此方法由定时任务调用，每小时执行一次。</p>
     *
     * @return 删除的会话数量
     */
    int deleteExpiredSessions();

    /**
     * 统计用户的会话数量
     *
     * <p>统计指定用户当前活跃的会话数量。</p>
     * <p>用于会话数量限制检查。</p>
     *
     * @param userId 用户ID
     * @return 会话数量
     * @throws IllegalArgumentException 如果userId为null
     */
    int countByUserId(Long userId);
}
