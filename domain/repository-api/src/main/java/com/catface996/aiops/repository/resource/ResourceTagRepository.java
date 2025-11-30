package com.catface996.aiops.repository.resource;

import com.catface996.aiops.domain.model.resource.ResourceTag;

import java.util.List;
import java.util.Optional;

/**
 * 资源标签仓储接口
 *
 * <p>提供资源标签实体的数据访问操作，遵循DDD仓储模式。</p>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public interface ResourceTagRepository {

    /**
     * 根据ID查询标签
     *
     * @param id 标签ID
     * @return 标签实体（如果存在）
     */
    Optional<ResourceTag> findById(Long id);

    /**
     * 根据资源ID查询所有标签
     *
     * @param resourceId 资源ID
     * @return 标签列表
     */
    List<ResourceTag> findByResourceId(Long resourceId);

    /**
     * 根据标签名称查询资源ID列表
     *
     * @param tagName 标签名称
     * @return 资源ID列表
     */
    List<Long> findResourceIdsByTagName(String tagName);

    /**
     * 保存标签
     *
     * @param tag 标签实体
     * @return 保存后的标签实体
     */
    ResourceTag save(ResourceTag tag);

    /**
     * 批量保存标签
     *
     * @param tags 标签列表
     * @return 保存后的标签列表
     */
    List<ResourceTag> saveAll(List<ResourceTag> tags);

    /**
     * 删除标签
     *
     * @param id 标签ID
     */
    void deleteById(Long id);

    /**
     * 删除资源的所有标签
     *
     * @param resourceId 资源ID
     */
    void deleteByResourceId(Long resourceId);

    /**
     * 删除资源的指定标签
     *
     * @param resourceId 资源ID
     * @param tagName 标签名称
     */
    void deleteByResourceIdAndTagName(Long resourceId, String tagName);

    /**
     * 检查资源是否已有指定标签
     *
     * @param resourceId 资源ID
     * @param tagName 标签名称
     * @return true if tag exists
     */
    boolean existsByResourceIdAndTagName(Long resourceId, String tagName);

    /**
     * 获取热门标签（按使用次数排序）
     *
     * @param limit 返回数量限制
     * @return 标签名称列表
     */
    List<String> findPopularTags(int limit);
}
