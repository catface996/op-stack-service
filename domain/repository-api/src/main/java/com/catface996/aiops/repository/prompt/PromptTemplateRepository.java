package com.catface996.aiops.repository.prompt;

import com.catface996.aiops.domain.model.prompt.PromptTemplate;

import java.util.List;
import java.util.Optional;

/**
 * 提示词模板仓储接口
 *
 * <p>提供提示词模板实体的数据访问操作。</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public interface PromptTemplateRepository {

    /**
     * 根据ID查询模板
     *
     * @param id 模板ID
     * @return 模板实体（如果存在）
     */
    Optional<PromptTemplate> findById(Long id);

    /**
     * 根据ID查询模板，包含用途信息和当前版本内容
     *
     * @param id 模板ID
     * @return 模板实体（如果存在）
     */
    Optional<PromptTemplate> findByIdWithDetail(Long id);

    /**
     * 根据名称查询模板
     *
     * @param name 模板名称
     * @return 模板实体（如果存在）
     */
    Optional<PromptTemplate> findByName(String name);

    /**
     * 分页查询模板列表
     *
     * @param usageId 用途ID筛选（可选）
     * @param keyword 关键词模糊查询（可选，搜索名称和描述）
     * @param page    页码（从1开始）
     * @param size    每页大小
     * @return 模板列表
     */
    List<PromptTemplate> findByCondition(Long usageId, String keyword, int page, int size);

    /**
     * 按条件统计模板数量
     *
     * @param usageId 用途ID筛选（可选）
     * @param keyword 关键词模糊查询（可选）
     * @return 模板数量
     */
    long countByCondition(Long usageId, String keyword);

    /**
     * 保存模板
     *
     * @param template 模板实体
     * @return 保存后的模板实体
     */
    PromptTemplate save(PromptTemplate template);

    /**
     * 更新模板（使用乐观锁）
     *
     * @param template 模板实体
     * @return 更新是否成功
     */
    boolean update(PromptTemplate template);

    /**
     * 删除模板（软删除）
     *
     * @param id 模板ID
     */
    void deleteById(Long id);

    /**
     * 检查模板是否存在
     *
     * @param id 模板ID
     * @return true if template exists
     */
    boolean existsById(Long id);

    /**
     * 检查模板名称是否已存在
     *
     * @param name 模板名称
     * @return true if name exists
     */
    boolean existsByName(String name);

    /**
     * 检查模板名称是否已存在（排除指定ID）
     *
     * @param name       模板名称
     * @param excludeId  排除的模板ID
     * @return true if name exists for another template
     */
    boolean existsByNameExcludingId(String name, Long excludeId);
}
