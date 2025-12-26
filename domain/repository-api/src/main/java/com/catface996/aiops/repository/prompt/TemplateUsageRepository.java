package com.catface996.aiops.repository.prompt;

import com.catface996.aiops.domain.model.prompt.TemplateUsage;

import java.util.List;
import java.util.Optional;

/**
 * 模板用途仓储接口
 *
 * <p>提供模板用途实体的数据访问操作。</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public interface TemplateUsageRepository {

    /**
     * 根据ID查询用途
     *
     * @param id 用途ID
     * @return 用途实体（如果存在）
     */
    Optional<TemplateUsage> findById(Long id);

    /**
     * 根据编码查询用途
     *
     * @param code 用途编码
     * @return 用途实体（如果存在）
     */
    Optional<TemplateUsage> findByCode(String code);

    /**
     * 根据名称查询用途
     *
     * @param name 用途名称
     * @return 用途实体（如果存在）
     */
    Optional<TemplateUsage> findByName(String name);

    /**
     * 查询所有用途
     *
     * @return 用途列表
     */
    List<TemplateUsage> findAll();

    /**
     * 保存用途
     *
     * @param usage 用途实体
     * @return 保存后的用途实体
     */
    TemplateUsage save(TemplateUsage usage);

    /**
     * 删除用途（软删除）
     *
     * @param id 用途ID
     */
    void deleteById(Long id);

    /**
     * 检查用途是否存在
     *
     * @param id 用途ID
     * @return true if usage exists
     */
    boolean existsById(Long id);

    /**
     * 检查编码是否已存在
     *
     * @param code 用途编码
     * @return true if code exists
     */
    boolean existsByCode(String code);

    /**
     * 统计使用该用途的模板数量
     *
     * @param usageId 用途ID
     * @return 使用该用途的模板数量
     */
    long countTemplatesByUsageId(Long usageId);
}
