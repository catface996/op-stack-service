package com.catface996.aiops.repository.prompt;

import com.catface996.aiops.domain.model.prompt.PromptTemplateVersion;

import java.util.List;
import java.util.Optional;

/**
 * 模板版本仓储接口
 *
 * <p>提供模板版本实体的数据访问操作。</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public interface PromptTemplateVersionRepository {

    /**
     * 根据ID查询版本
     *
     * @param id 版本ID
     * @return 版本实体（如果存在）
     */
    Optional<PromptTemplateVersion> findById(Long id);

    /**
     * 根据模板ID和版本号查询版本
     *
     * @param templateId    模板ID
     * @param versionNumber 版本号
     * @return 版本实体（如果存在）
     */
    Optional<PromptTemplateVersion> findByTemplateIdAndVersion(Long templateId, Integer versionNumber);

    /**
     * 查询模板的所有版本（按版本号降序）
     *
     * @param templateId 模板ID
     * @return 版本列表
     */
    List<PromptTemplateVersion> findByTemplateId(Long templateId);

    /**
     * 查询模板的最新版本
     *
     * @param templateId 模板ID
     * @return 最新版本（如果存在）
     */
    Optional<PromptTemplateVersion> findLatestByTemplateId(Long templateId);

    /**
     * 保存版本
     *
     * @param version 版本实体
     * @return 保存后的版本实体
     */
    PromptTemplateVersion save(PromptTemplateVersion version);

    /**
     * 查询模板的版本数量
     *
     * @param templateId 模板ID
     * @return 版本数量
     */
    long countByTemplateId(Long templateId);

    /**
     * 删除模板的所有版本
     *
     * @param templateId 模板ID
     */
    void deleteByTemplateId(Long templateId);
}
