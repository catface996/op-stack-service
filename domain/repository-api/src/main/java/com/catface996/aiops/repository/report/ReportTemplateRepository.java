package com.catface996.aiops.repository.report;

import com.catface996.aiops.domain.model.report.ReportTemplate;
import com.catface996.aiops.domain.model.report.ReportTemplateCategory;

import java.util.List;
import java.util.Optional;

/**
 * 报告模板仓储接口
 *
 * <p>提供报告模板实体的数据访问操作。支持完整的 CRUD 操作。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
public interface ReportTemplateRepository {

    /**
     * 根据ID查询模板
     *
     * @param id 模板ID
     * @return 模板实体（如果存在且未删除）
     */
    Optional<ReportTemplate> findById(Long id);

    /**
     * 分页查询模板列表
     *
     * @param category 模板分类筛选（可选）
     * @param keyword  关键词搜索（可选，搜索 name, description, tags）
     * @param page     页码（从1开始）
     * @param size     每页大小
     * @return 模板列表
     */
    List<ReportTemplate> findByCondition(ReportTemplateCategory category, String keyword, int page, int size);

    /**
     * 按条件统计模板数量
     *
     * @param category 模板分类筛选（可选）
     * @param keyword  关键词搜索（可选）
     * @return 模板数量
     */
    long countByCondition(ReportTemplateCategory category, String keyword);

    /**
     * 保存模板
     *
     * @param template 模板实体
     * @return 保存后的模板实体（包含生成的ID）
     */
    ReportTemplate save(ReportTemplate template);

    /**
     * 更新模板（使用乐观锁）
     *
     * @param template 模板实体
     * @return 更新是否成功
     */
    boolean update(ReportTemplate template);

    /**
     * 删除模板（软删除）
     *
     * @param id 模板ID
     * @return 删除是否成功
     */
    boolean deleteById(Long id);

    /**
     * 检查模板是否存在
     *
     * @param id 模板ID
     * @return true if template exists and not deleted
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
     * @param name      模板名称
     * @param excludeId 排除的ID
     * @return true if name exists for other template
     */
    boolean existsByNameExcludeId(String name, Long excludeId);
}
