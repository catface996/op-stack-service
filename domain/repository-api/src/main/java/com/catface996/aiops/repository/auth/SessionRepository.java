package com.catface996.aiops.repository.auth;

import com.catface996.aiops.domain.api.model.auth.Session;

import java.util.Optional;

/**
 * 会话仓储接口
 *
 * <p>提供会话实体的数据访问操作，遵循DDD仓储模式。</p>
 * <p>会话数据优先存储在Redis缓存中，MySQL作为降级方案。</p>
 *
 * <p>存储策略：</p>
 * <ul>
 *   <li>主存储：Redis（高性能，支持TTL自动过期）</li>
 *   <li>降级存储：MySQL（Redis不可用时使用）</li>
 * </ul>
 *
 * <p>实现说明：</p>
 * <ul>
 *   <li>Redis Key格式：session:{sessionId}</li>
 *   <li>Redis TTL：2小时（默认）或30天（记住我）</li>
 *   <li>会话互斥：使用Redis Key session:user:{userId} 存储当前活跃会话ID</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-01-23
 */
public interface SessionRepository {

    /**
     * 根据会话ID查询会话
     *
     * <p>根据会话ID查询会话实体。</p>
     * <p>优先从Redis查询，Redis不可用时从MySQL查询。</p>
     *
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-007: 会话管理</li>
     *   <li>REQ-NFR-PERF-004: 会话验证 < 1秒</li>
     * </ul>
     *
     * @param sessionId 会话ID（UUID格式）
     * @return 会话实体（如果存在）
     * @throws IllegalArgumentException 如果sessionId为空或null
     */
    Optional<Session> findById(String sessionId);

    /**
     * 根据用户ID查询会话
     *
     * <p>根据用户ID查询当前活跃的会话。</p>
     * <p>用于会话互斥：同一用户只能有一个活跃会话。</p>
     *
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-009: 会话互斥</li>
     * </ul>
     *
     * @param userId 用户ID
     * @return 会话实体（如果存在）
     * @throws IllegalArgumentException 如果userId为null
     */
    Optional<Session> findByUserId(Long userId);

    /**
     * 保存会话
     *
     * <p>保存会话实体到存储。</p>
     * <p>优先保存到Redis，Redis不可用时保存到MySQL。</p>
     * <p>同时更新会话互斥映射：session:user:{userId} -> sessionId。</p>
     *
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-007: 会话管理</li>
     *   <li>REQ-FR-009: 会话互斥</li>
     * </ul>
     *
     * @param session 会话实体
     * @return 保存后的会话实体
     * @throws IllegalArgumentException 如果session为null
     */
    Session save(Session session);

    /**
     * 根据会话ID删除会话
     *
     * <p>根据会话ID删除会话。</p>
     * <p>同时删除Redis和MySQL中的会话数据。</p>
     *
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-010: 安全退出</li>
     * </ul>
     *
     * @param sessionId 会话ID
     * @throws IllegalArgumentException 如果sessionId为空或null
     */
    void deleteById(String sessionId);

    /**
     * 根据用户ID删除会话
     *
     * <p>根据用户ID删除该用户的所有会话。</p>
     * <p>用于会话互斥：删除旧会话。</p>
     *
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-009: 会话互斥</li>
     * </ul>
     *
     * @param userId 用户ID
     * @throws IllegalArgumentException 如果userId为null
     */
    void deleteByUserId(Long userId);

    /**
     * 检查会话是否存在
     *
     * <p>检查指定的会话ID是否存在。</p>
     * <p>优先从Redis检查，Redis不可用时从MySQL检查。</p>
     *
     * <p>需求追溯：</p>
     * <ul>
     *   <li>REQ-FR-007: 会话管理</li>
     * </ul>
     *
     * @param sessionId 会话ID
     * @return true if session exists, false otherwise
     * @throws IllegalArgumentException 如果sessionId为空或null
     */
    boolean existsById(String sessionId);

    /**
     * 更新会话过期时间
     *
     * <p>更新指定会话的过期时间。</p>
     * <p>用于会话续期功能（如果需要）。</p>
     *
     * @param sessionId 会话ID
     * @param expiresAt 新的过期时间
     * @throws IllegalArgumentException 如果sessionId为空或expiresAt为null
     * @throws com.catface996.aiops.domain.api.exception.auth.SessionNotFoundException 如果会话不存在
     */
    void updateExpiresAt(String sessionId, java.time.LocalDateTime expiresAt);

    /**
     * 删除所有过期会话
     *
     * <p>删除所有已过期的会话（仅MySQL）。</p>
     * <p>Redis会话通过TTL自动过期，无需手动清理。</p>
     * <p>此方法用于定期清理MySQL中的过期会话数据。</p>
     *
     * <p>建议：</p>
     * <ul>
     *   <li>使用定时任务（如Spring @Scheduled）定期执行</li>
     *   <li>执行频率：每小时或每天</li>
     * </ul>
     *
     * @return 删除的会话数量
     */
    int deleteExpiredSessions();
}
