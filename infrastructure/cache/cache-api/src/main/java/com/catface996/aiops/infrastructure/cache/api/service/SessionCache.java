package com.catface996.aiops.infrastructure.cache.api.service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 会话缓存接口
 * 
 * <p>用于会话管理功能，存储用户会话信息。</p>
 * 
 * <p>存储策略：</p>
 * <ul>
 *   <li>主存储：Redis（高性能，支持TTL自动过期）</li>
 *   <li>降级存储：MySQL（Redis不可用时使用）</li>
 * </ul>
 * 
 * <p>Redis Key格式：</p>
 * <ul>
 *   <li>会话数据：session:{sessionId}</li>
 *   <li>会话互斥：session:user:{userId}</li>
 * </ul>
 * 
 * <p>Redis TTL：</p>
 * <ul>
 *   <li>默认：2小时</li>
 *   <li>记住我：30天</li>
 * </ul>
 * 
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-007: 会话管理</li>
 *   <li>REQ-FR-008: 记住我功能</li>
 *   <li>REQ-FR-009: 会话互斥</li>
 *   <li>REQ-FR-010: 安全退出</li>
 * </ul>
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
public interface SessionCache {
    
    /**
     * 保存会话
     * 
     * <p>保存会话数据到缓存。</p>
     * <p>同时更新会话互斥映射：session:user:{userId} -> sessionId。</p>
     * 
     * @param sessionId 会话ID
     * @param sessionData 会话数据（JSON格式）
     * @param expiresAt 过期时间
     * @param userId 用户ID（用于会话互斥）
     * @throws IllegalArgumentException 如果参数为空或null
     */
    void save(String sessionId, String sessionData, LocalDateTime expiresAt, Long userId);
    
    /**
     * 根据会话ID获取会话
     * 
     * <p>从缓存中获取会话数据。</p>
     * 
     * @param sessionId 会话ID
     * @return 会话数据（JSON格式），如果不存在返回Optional.empty()
     * @throws IllegalArgumentException 如果sessionId为空或null
     */
    Optional<String> get(String sessionId);
    
    /**
     * 根据用户ID获取会话ID
     * 
     * <p>从会话互斥映射中获取用户当前活跃的会话ID。</p>
     * 
     * @param userId 用户ID
     * @return 会话ID，如果不存在返回Optional.empty()
     * @throws IllegalArgumentException 如果userId为null
     */
    Optional<String> getSessionIdByUserId(Long userId);
    
    /**
     * 根据会话ID删除会话
     * 
     * <p>从缓存中删除会话数据。</p>
     * 
     * @param sessionId 会话ID
     * @throws IllegalArgumentException 如果sessionId为空或null
     */
    void delete(String sessionId);
    
    /**
     * 根据用户ID删除会话
     * 
     * <p>删除用户的所有会话（包括会话数据和会话互斥映射）。</p>
     * 
     * @param userId 用户ID
     * @throws IllegalArgumentException 如果userId为null
     */
    void deleteByUserId(Long userId);
    
    /**
     * 检查会话是否存在
     * 
     * <p>检查指定的会话ID是否存在于缓存中。</p>
     * 
     * @param sessionId 会话ID
     * @return true if exists, false otherwise
     * @throws IllegalArgumentException 如果sessionId为空或null
     */
    boolean exists(String sessionId);
    
    /**
     * 更新会话过期时间
     * 
     * <p>更新指定会话的过期时间（TTL）。</p>
     * 
     * @param sessionId 会话ID
     * @param expiresAt 新的过期时间
     * @throws IllegalArgumentException 如果sessionId为空或expiresAt为null
     */
    void updateExpiration(String sessionId, LocalDateTime expiresAt);
    
    /**
     * 获取会话剩余有效时间（秒）
     * 
     * <p>获取指定会话的剩余有效时间。</p>
     * 
     * @param sessionId 会话ID
     * @return 剩余有效时间（秒），如果不存在或已过期返回0
     * @throws IllegalArgumentException 如果sessionId为空或null
     */
    long getRemainingTime(String sessionId);
}
