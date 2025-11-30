package com.catface996.aiops.repository.resource;

import com.catface996.aiops.domain.model.resource.ResourceType;

import java.util.List;
import java.util.Optional;

/**
 * 资源类型仓储接口
 *
 * <p>提供资源类型实体的数据访问操作，遵循DDD仓储模式。</p>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public interface ResourceTypeRepository {

    /**
     * 根据ID查询资源类型
     *
     * @param id 资源类型ID
     * @return 资源类型实体（如果存在）
     * @throws IllegalArgumentException 如果id为null
     */
    Optional<ResourceType> findById(Long id);

    /**
     * 根据编码查询资源类型
     *
     * @param code 资源类型编码（如：SERVER, APPLICATION等）
     * @return 资源类型实体（如果存在）
     * @throws IllegalArgumentException 如果code为空或null
     */
    Optional<ResourceType> findByCode(String code);

    /**
     * 查询所有资源类型
     *
     * @return 资源类型列表
     */
    List<ResourceType> findAll();

    /**
     * 查询所有系统预置的资源类型
     *
     * @return 系统预置资源类型列表
     */
    List<ResourceType> findSystemTypes();

    /**
     * 保存资源类型
     *
     * @param resourceType 资源类型实体
     * @return 保存后的资源类型实体
     * @throws IllegalArgumentException 如果resourceType为null
     */
    ResourceType save(ResourceType resourceType);

    /**
     * 更新资源类型
     *
     * @param resourceType 资源类型实体
     * @return 更新后的资源类型实体
     * @throws IllegalArgumentException 如果resourceType为null
     */
    ResourceType update(ResourceType resourceType);

    /**
     * 删除资源类型
     *
     * @param id 资源类型ID
     * @throws IllegalArgumentException 如果id为null
     */
    void deleteById(Long id);

    /**
     * 检查资源类型是否存在
     *
     * @param id 资源类型ID
     * @return true if resource type exists
     */
    boolean existsById(Long id);

    /**
     * 检查资源类型编码是否已存在
     *
     * @param code 资源类型编码
     * @return true if code exists
     */
    boolean existsByCode(String code);

    /**
     * 统计资源类型数量
     *
     * @return 资源类型总数
     */
    long count();
}
