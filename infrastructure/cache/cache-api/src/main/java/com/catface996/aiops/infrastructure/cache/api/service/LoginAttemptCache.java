package com.catface996.aiops.infrastructure.cache.api.service;

import java.util.Optional;

/**
 * 登录失败计数缓存接口
 * 
 * <p>用于防暴力破解功能，记录用户登录失败次数。</p>
 * 
 * <p>存储策略：</p>
 * <ul>
 *   <li>主存储：Redis（高性能，支持TTL自动过期）</li>
 *   <li>降级存储：MySQL（Redis不可用时使用）</li>
 * </ul>
 * 
 * <p>Redis Key格式：login:fail:{identifier}</p>
 * <p>Redis TTL：30分钟（锁定时长）</p>
 * 
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-FR-005: 防暴力破解</li>
 *   <li>REQ-FR-006: 管理员手动解锁</li>
 * </ul>
 * 
 * @author AI Assistant
 * @since 2025-01-23
 */
public interface LoginAttemptCache {
    
    /**
     * 记录登录失败
     * 
     * <p>增加指定标识符的登录失败计数。</p>
     * <p>如果是第一次失败，设置TTL为30分钟。</p>
     * 
     * @param identifier 用户标识符（用户名或邮箱）
     * @return 当前失败次数
     * @throws IllegalArgumentException 如果identifier为空或null
     */
    int recordFailure(String identifier);
    
    /**
     * 获取登录失败次数
     * 
     * <p>获取指定标识符的登录失败次数。</p>
     * 
     * @param identifier 用户标识符（用户名或邮箱）
     * @return 失败次数，如果不存在返回0
     * @throws IllegalArgumentException 如果identifier为空或null
     */
    int getFailureCount(String identifier);
    
    /**
     * 重置登录失败计数
     * 
     * <p>清除指定标识符的登录失败计数。</p>
     * <p>用于登录成功后重置计数。</p>
     * 
     * @param identifier 用户标识符（用户名或邮箱）
     * @throws IllegalArgumentException 如果identifier为空或null
     */
    void resetFailureCount(String identifier);
    
    /**
     * 检查账号是否被锁定
     * 
     * <p>检查指定标识符的登录失败次数是否达到锁定阈值（5次）。</p>
     * 
     * @param identifier 用户标识符（用户名或邮箱）
     * @return true if locked (failure count >= 5), false otherwise
     * @throws IllegalArgumentException 如果identifier为空或null
     */
    boolean isLocked(String identifier);
    
    /**
     * 获取锁定剩余时间（秒）
     * 
     * <p>获取指定标识符的锁定剩余时间。</p>
     * <p>如果未锁定或已过期，返回0。</p>
     * 
     * @param identifier 用户标识符（用户名或邮箱）
     * @return 剩余锁定时间（秒），如果未锁定返回0
     * @throws IllegalArgumentException 如果identifier为空或null
     */
    long getRemainingLockTime(String identifier);
    
    /**
     * 手动解锁账号
     * 
     * <p>清除指定标识符的登录失败计数，解除锁定。</p>
     * <p>用于管理员手动解锁功能。</p>
     * 
     * @param identifier 用户标识符（用户名或邮箱）
     * @throws IllegalArgumentException 如果identifier为空或null
     */
    void unlock(String identifier);
}
