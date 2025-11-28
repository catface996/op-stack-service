package com.catface996.aiops.infrastructure.cache.api.service;

import com.catface996.aiops.domain.model.auth.Session;

import java.util.Set;

/**
 * 会话缓存服务接口
 *
 * <p>提供会话数据的Redis缓存操作，作为Cache-Aside模式的缓存层。</p>
 *
 * <p>Redis Key格式：</p>
 * <ul>
 *   <li>会话数据：session:{sessionId}</li>
 *   <li>用户会话列表：user:sessions:{userId} (Set类型)</li>
 *   <li>令牌黑名单：token:blacklist:{tokenId}</li>
 * </ul>
 *
 * <p>TTL策略：</p>
 * <ul>
 *   <li>会话数据TTL = 绝对超时时长</li>
 *   <li>黑名单TTL = 令牌剩余有效期</li>
 * </ul>
 *
 * <p>异常处理：</p>
 * <ul>
 *   <li>Redis连接失败时捕获异常，记录日志，不影响主流程</li>
 *   <li>调用方应处理返回null的情况，降级到MySQL查询</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F01-4: 会话管理功能</li>
 *   <li>REQ 1.2, 1.4, 2.4: 会话缓存和令牌黑名单</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-28
 */
public interface SessionCacheService {

    /**
     * 缓存会话
     *
     * <p>将会话数据缓存到Redis。</p>
     * <p>同时更新用户会话列表：user:sessions:{userId}。</p>
     *
     * @param session 会话实体
     * @param ttlSeconds 生存时间（秒）
     */
    void cacheSession(Session session, long ttlSeconds);

    /**
     * 获取缓存的会话
     *
     * <p>从Redis缓存中获取会话数据。</p>
     *
     * @param sessionId 会话标识符
     * @return 会话实体，不存在或出错时返回null
     */
    Session getCachedSession(String sessionId);

    /**
     * 删除缓存的会话
     *
     * <p>从Redis中删除会话缓存。</p>
     * <p>同时从用户会话列表中移除。</p>
     *
     * @param sessionId 会话标识符
     */
    void evictSession(String sessionId);

    /**
     * 删除用户的所有会话缓存
     *
     * <p>删除用户的所有会话缓存和用户会话列表。</p>
     *
     * @param userId 用户ID
     */
    void evictUserSessions(Long userId);

    /**
     * 缓存用户会话列表
     *
     * <p>将用户的会话ID集合存储到Redis Set中。</p>
     *
     * @param userId 用户ID
     * @param sessionIds 会话标识符集合
     */
    void cacheUserSessions(Long userId, Set<String> sessionIds);

    /**
     * 获取用户会话ID列表
     *
     * <p>从Redis获取用户的所有会话ID。</p>
     *
     * @param userId 用户ID
     * @return 会话标识符集合，不存在或出错时返回空集合
     */
    Set<String> getUserSessionIds(Long userId);

    /**
     * 添加会话ID到用户会话列表
     *
     * @param userId 用户ID
     * @param sessionId 会话标识符
     */
    void addUserSession(Long userId, String sessionId);

    /**
     * 从用户会话列表中移除会话ID
     *
     * @param userId 用户ID
     * @param sessionId 会话标识符
     */
    void removeUserSession(Long userId, String sessionId);

    /**
     * 将令牌加入黑名单
     *
     * <p>用于用户登出后使令牌失效。</p>
     *
     * @param tokenId 令牌ID（jti）
     * @param ttlSeconds 生存时间（秒），应为令牌剩余有效期
     */
    void addToBlacklist(String tokenId, long ttlSeconds);

    /**
     * 检查令牌是否在黑名单中
     *
     * @param tokenId 令牌ID（jti）
     * @return true if in blacklist, false otherwise
     */
    boolean isInBlacklist(String tokenId);

    /**
     * 检查会话缓存是否存在
     *
     * @param sessionId 会话标识符
     * @return true if exists in cache, false otherwise
     */
    boolean existsInCache(String sessionId);

    /**
     * 更新会话缓存的TTL
     *
     * @param sessionId 会话标识符
     * @param ttlSeconds 新的生存时间（秒）
     * @return true if updated successfully, false if session not found
     */
    boolean updateTtl(String sessionId, long ttlSeconds);

    /**
     * 检查Redis是否可用
     *
     * <p>用于健康检查和降级判断。</p>
     *
     * @return true if Redis is available, false otherwise
     */
    boolean isAvailable();
}
