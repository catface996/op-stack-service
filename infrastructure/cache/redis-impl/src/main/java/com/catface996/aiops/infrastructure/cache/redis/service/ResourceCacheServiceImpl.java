package com.catface996.aiops.infrastructure.cache.redis.service;

import com.catface996.aiops.infrastructure.cache.api.service.ResourceCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 资源缓存服务实现（Redis）
 *
 * <p>使用Redis缓存资源数据，提高查询性能。</p>
 *
 * <p>Redis Key格式：</p>
 * <ul>
 *   <li>资源详情：resource:{resourceId}</li>
 *   <li>资源列表：resource:list:{hash}</li>
 *   <li>资源类型：resource:types</li>
 * </ul>
 *
 * <p>TTL策略：</p>
 * <ul>
 *   <li>资源详情：5分钟</li>
 *   <li>资源列表：5分钟（只缓存前3页）</li>
 *   <li>资源类型：30分钟（变化不频繁）</li>
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
@Slf4j
@Service
public class ResourceCacheServiceImpl implements ResourceCacheService {

    private static final String RESOURCE_KEY_PREFIX = "resource:";
    private static final String RESOURCE_LIST_KEY_PREFIX = "resource:list:";
    private static final String RESOURCE_TYPES_KEY = "resource:types";

    private static final long RESOURCE_TTL_MINUTES = 5;
    private static final long RESOURCE_LIST_TTL_MINUTES = 5;
    private static final long RESOURCE_TYPES_TTL_MINUTES = 30;
    private static final int MAX_CACHED_PAGES = 3;

    private final RedisTemplate<String, Object> redisTemplate;

    public ResourceCacheServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void cacheResource(Long resourceId, String resourceJson) {
        if (resourceId == null || resourceJson == null) {
            log.warn("缓存资源参数无效，resourceId: {}", resourceId);
            return;
        }

        String key = RESOURCE_KEY_PREFIX + resourceId;

        try {
            redisTemplate.opsForValue().set(key, resourceJson, RESOURCE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("缓存资源成功，resourceId: {}, TTL: {}分钟", resourceId, RESOURCE_TTL_MINUTES);
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法缓存资源，resourceId: {}", resourceId);
        } catch (Exception e) {
            log.error("缓存资源异常，resourceId: {}", resourceId, e);
        }
    }

    @Override
    public Optional<String> getResource(Long resourceId) {
        if (resourceId == null) {
            return Optional.empty();
        }

        String key = RESOURCE_KEY_PREFIX + resourceId;

        try {
            Object value = redisTemplate.opsForValue().get(key);

            if (value instanceof String) {
                log.debug("从缓存获取资源成功，resourceId: {}", resourceId);
                return Optional.of((String) value);
            }

            return Optional.empty();
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法获取缓存资源，resourceId: {}", resourceId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("获取缓存资源异常，resourceId: {}", resourceId, e);
            return Optional.empty();
        }
    }

    @Override
    public void evictResource(Long resourceId) {
        if (resourceId == null) {
            return;
        }

        String key = RESOURCE_KEY_PREFIX + resourceId;

        try {
            Boolean deleted = redisTemplate.delete(key);
            log.debug("删除资源缓存，resourceId: {}, deleted: {}", resourceId, deleted);

            // 同时清除所有列表缓存（因为资源变化会影响列表）
            evictAllResourceLists();
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法删除资源缓存，resourceId: {}", resourceId);
        } catch (Exception e) {
            log.error("删除资源缓存异常，resourceId: {}", resourceId, e);
        }
    }

    @Override
    public void cacheResourceList(String cacheKey, String resourceListJson) {
        if (cacheKey == null || resourceListJson == null) {
            return;
        }

        String key = RESOURCE_LIST_KEY_PREFIX + cacheKey;

        try {
            redisTemplate.opsForValue().set(key, resourceListJson, RESOURCE_LIST_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("缓存资源列表成功，cacheKey: {}, TTL: {}分钟", cacheKey, RESOURCE_LIST_TTL_MINUTES);
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法缓存资源列表，cacheKey: {}", cacheKey);
        } catch (Exception e) {
            log.error("缓存资源列表异常，cacheKey: {}", cacheKey, e);
        }
    }

    @Override
    public Optional<String> getResourceList(String cacheKey) {
        if (cacheKey == null) {
            return Optional.empty();
        }

        String key = RESOURCE_LIST_KEY_PREFIX + cacheKey;

        try {
            Object value = redisTemplate.opsForValue().get(key);

            if (value instanceof String) {
                log.debug("从缓存获取资源列表成功，cacheKey: {}", cacheKey);
                return Optional.of((String) value);
            }

            return Optional.empty();
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法获取缓存资源列表，cacheKey: {}", cacheKey);
            return Optional.empty();
        } catch (Exception e) {
            log.error("获取缓存资源列表异常，cacheKey: {}", cacheKey, e);
            return Optional.empty();
        }
    }

    @Override
    public void evictAllResourceLists() {
        try {
            Set<String> keys = redisTemplate.keys(RESOURCE_LIST_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                log.debug("清除资源列表缓存，删除数量: {}", deleted);
            }
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法清除资源列表缓存");
        } catch (Exception e) {
            log.error("清除资源列表缓存异常", e);
        }
    }

    @Override
    public void cacheResourceTypes(String resourceTypesJson) {
        if (resourceTypesJson == null) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(RESOURCE_TYPES_KEY, resourceTypesJson,
                    RESOURCE_TYPES_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("缓存资源类型列表成功，TTL: {}分钟", RESOURCE_TYPES_TTL_MINUTES);
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法缓存资源类型列表");
        } catch (Exception e) {
            log.error("缓存资源类型列表异常", e);
        }
    }

    @Override
    public Optional<String> getResourceTypes() {
        try {
            Object value = redisTemplate.opsForValue().get(RESOURCE_TYPES_KEY);

            if (value instanceof String) {
                log.debug("从缓存获取资源类型列表成功");
                return Optional.of((String) value);
            }

            return Optional.empty();
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法获取缓存资源类型列表");
            return Optional.empty();
        } catch (Exception e) {
            log.error("获取缓存资源类型列表异常", e);
            return Optional.empty();
        }
    }

    @Override
    public void evictResourceTypes() {
        try {
            Boolean deleted = redisTemplate.delete(RESOURCE_TYPES_KEY);
            log.debug("清除资源类型缓存，deleted: {}", deleted);
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，无法清除资源类型缓存");
        } catch (Exception e) {
            log.error("清除资源类型缓存异常", e);
        }
    }

    @Override
    public String generateListCacheKey(Long resourceTypeId, String status, String keyword, int page, int size) {
        StringBuilder sb = new StringBuilder();
        sb.append("type:").append(resourceTypeId != null ? resourceTypeId : "all");
        sb.append(":status:").append(status != null ? status : "all");
        sb.append(":kw:").append(keyword != null ? keyword.hashCode() : "none");
        sb.append(":p:").append(page);
        sb.append(":s:").append(size);
        return sb.toString();
    }

    @Override
    public boolean shouldCachePage(int page) {
        return page > 0 && page <= MAX_CACHED_PAGES;
    }
}
