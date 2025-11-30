package com.catface996.aiops.infrastructure.cache.api.service;

import java.util.List;
import java.util.Optional;

/**
 * 资源缓存服务接口
 *
 * <p>提供资源数据的Redis缓存功能，加速资源查询。</p>
 *
 * <p>缓存策略：</p>
 * <ul>
 *   <li>资源详情缓存：5分钟TTL</li>
 *   <li>资源列表缓存：前3页，5分钟TTL</li>
 *   <li>支持缓存清除操作</li>
 * </ul>
 *
 * <p>降级策略：</p>
 * <ul>
 *   <li>Redis不可用时返回空</li>
 *   <li>不阻塞主流程</li>
 *   <li>由调用方决定是否从数据库读取</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>REQ-NFR-001: 查询性能要求</li>
 *   <li>REQ-NFR-003: 缓存策略要求</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public interface ResourceCacheService {

    /**
     * 缓存资源详情
     *
     * @param resourceId 资源ID
     * @param resourceJson 资源JSON数据
     */
    void cacheResource(Long resourceId, String resourceJson);

    /**
     * 获取缓存的资源详情
     *
     * @param resourceId 资源ID
     * @return 资源JSON数据（如果存在）
     */
    Optional<String> getResource(Long resourceId);

    /**
     * 删除资源缓存
     *
     * @param resourceId 资源ID
     */
    void evictResource(Long resourceId);

    /**
     * 缓存资源列表
     *
     * @param cacheKey 缓存键（包含查询条件）
     * @param resourceListJson 资源列表JSON数据
     */
    void cacheResourceList(String cacheKey, String resourceListJson);

    /**
     * 获取缓存的资源列表
     *
     * @param cacheKey 缓存键
     * @return 资源列表JSON数据（如果存在）
     */
    Optional<String> getResourceList(String cacheKey);

    /**
     * 清除所有资源列表缓存
     *
     * <p>当资源发生变化时调用，确保列表数据一致性。</p>
     */
    void evictAllResourceLists();

    /**
     * 缓存资源类型列表
     *
     * @param resourceTypesJson 资源类型列表JSON数据
     */
    void cacheResourceTypes(String resourceTypesJson);

    /**
     * 获取缓存的资源类型列表
     *
     * @return 资源类型列表JSON数据（如果存在）
     */
    Optional<String> getResourceTypes();

    /**
     * 清除资源类型缓存
     */
    void evictResourceTypes();

    /**
     * 生成资源列表缓存键
     *
     * @param resourceTypeId 资源类型ID（可选）
     * @param status 资源状态（可选）
     * @param keyword 搜索关键词（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 缓存键
     */
    String generateListCacheKey(Long resourceTypeId, String status, String keyword, int page, int size);

    /**
     * 检查页码是否在缓存范围内
     *
     * <p>只缓存前3页数据以控制内存使用。</p>
     *
     * @param page 页码
     * @return true如果应该缓存
     */
    boolean shouldCachePage(int page);
}
