package com.catface996.aiops.repository.resource;

import com.catface996.aiops.domain.model.resource.Resource;
import com.catface996.aiops.domain.model.resource.ResourceStatus;

import java.util.List;
import java.util.Optional;

/**
 * 资源仓储接口
 *
 * <p>提供资源实体的数据访问操作，遵循DDD仓储模式。</p>
 * <p>仓储负责领域对象的持久化和重建，隔离领域层与基础设施层。</p>
 *
 * <p>实现说明：</p>
 * <ul>
 *   <li>使用MyBatis-Plus实现数据访问</li>
 *   <li>数据存储在MySQL数据库</li>
 *   <li>支持乐观锁（version字段）</li>
 *   <li>支持事务管理</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-30
 */
public interface ResourceRepository {

    /**
     * 根据ID查询资源
     *
     * @param id 资源ID
     * @return 资源实体（如果存在）
     * @throws IllegalArgumentException 如果id为null
     */
    Optional<Resource> findById(Long id);

    /**
     * 根据ID查询资源，包含关联的资源类型
     *
     * @param id 资源ID
     * @return 资源实体（如果存在），包含资源类型信息
     * @throws IllegalArgumentException 如果id为null
     */
    Optional<Resource> findByIdWithType(Long id);

    /**
     * 根据名称和类型ID查询资源
     *
     * @param name 资源名称
     * @param resourceTypeId 资源类型ID
     * @return 资源实体（如果存在）
     */
    Optional<Resource> findByNameAndTypeId(String name, Long resourceTypeId);

    /**
     * 分页查询资源列表
     *
     * @param resourceTypeId 资源类型ID（可选，为null则不过滤）
     * @param status 资源状态（可选，为null则不过滤）
     * @param keyword 搜索关键词（可选，搜索名称和描述）
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 资源列表
     */
    List<Resource> findByCondition(Long resourceTypeId, ResourceStatus status,
                                   String keyword, int page, int size);

    /**
     * 按条件统计资源数量
     *
     * @param resourceTypeId 资源类型ID（可选）
     * @param status 资源状态（可选）
     * @param keyword 搜索关键词（可选）
     * @return 资源数量
     */
    long countByCondition(Long resourceTypeId, ResourceStatus status, String keyword);

    /**
     * 保存资源
     *
     * @param resource 资源实体
     * @return 保存后的资源实体（包含生成的ID）
     * @throws IllegalArgumentException 如果resource为null
     */
    Resource save(Resource resource);

    /**
     * 更新资源（使用乐观锁）
     *
     * @param resource 资源实体
     * @return 更新是否成功（乐观锁冲突时返回false）
     * @throws IllegalArgumentException 如果resource为null或resource.id为null
     */
    boolean update(Resource resource);

    /**
     * 更新资源状态
     *
     * @param id 资源ID
     * @param status 新的资源状态
     * @param version 当前版本号（用于乐观锁）
     * @return 更新是否成功
     */
    boolean updateStatus(Long id, ResourceStatus status, Integer version);

    /**
     * 删除资源
     *
     * @param id 资源ID
     * @throws IllegalArgumentException 如果id为null
     */
    void deleteById(Long id);

    /**
     * 检查资源是否存在
     *
     * @param id 资源ID
     * @return true if resource exists
     */
    boolean existsById(Long id);

    /**
     * 检查同类型下资源名称是否已存在
     *
     * @param name 资源名称
     * @param resourceTypeId 资源类型ID
     * @return true if name exists in the same type
     */
    boolean existsByNameAndTypeId(String name, Long resourceTypeId);

    /**
     * 查询指定用户创建的资源
     *
     * @param createdBy 创建者ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 资源列表
     */
    List<Resource> findByCreatedBy(Long createdBy, int page, int size);

    /**
     * 统计所有资源数量
     *
     * @return 资源总数
     */
    long count();

    /**
     * 分页查询资源列表，排除指定资源类型
     *
     * <p>用于资源节点查询，排除 SUBGRAPH 类型。</p>
     *
     * @param resourceTypeId 资源类型ID（可选，为null则不过滤）
     * @param status 资源状态（可选，为null则不过滤）
     * @param keyword 搜索关键词（可选，搜索名称和描述）
     * @param excludeTypeId 要排除的资源类型ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 资源列表
     */
    List<Resource> findByConditionExcludeType(Long resourceTypeId, ResourceStatus status,
                                               String keyword, Long excludeTypeId, int page, int size);

    /**
     * 按条件统计资源数量，排除指定资源类型
     *
     * @param resourceTypeId 资源类型ID（可选）
     * @param status 资源状态（可选）
     * @param keyword 搜索关键词（可选）
     * @param excludeTypeId 要排除的资源类型ID
     * @return 资源数量
     */
    long countByConditionExcludeType(Long resourceTypeId, ResourceStatus status, String keyword, Long excludeTypeId);

    /**
     * 根据类型ID和条件查询资源（用于拓扑图查询）
     *
     * @param typeId 资源类型ID（必填）
     * @param status 资源状态（可选）
     * @param keyword 名称模糊查询（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 资源列表
     */
    List<Resource> findByTypeIdAndConditions(Long typeId, ResourceStatus status, String keyword, int page, int size);

    /**
     * 统计指定类型的资源数量
     *
     * @param typeId 资源类型ID（必填）
     * @param status 资源状态（可选）
     * @param keyword 名称模糊查询（可选）
     * @return 资源数量
     */
    long countByTypeIdAndConditions(Long typeId, ResourceStatus status, String keyword);

    /**
     * 插入资源
     *
     * @param resource 资源实体
     */
    void insert(Resource resource);

    /**
     * 根据ID更新资源
     *
     * @param resource 资源实体
     */
    void updateById(Resource resource);
}
