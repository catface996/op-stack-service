package com.catface996.aiops.domain.service.prompt;

import com.catface996.aiops.domain.model.prompt.PromptTemplate;
import com.catface996.aiops.domain.model.prompt.PromptTemplateVersion;

import java.util.List;
import java.util.Optional;

/**
 * 提示词模板领域服务接口
 *
 * <p>提供提示词模板管理的核心业务逻辑。</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
public interface PromptTemplateDomainService {

    /**
     * 创建模板
     *
     * @param name        模板名称
     * @param content     模板内容
     * @param usageId     用途ID（可选）
     * @param description 模板描述
     * @param operatorId  操作人ID
     * @return 创建的模板
     */
    PromptTemplate createTemplate(String name, String content, Long usageId,
                                   String description, Long operatorId);

    /**
     * 分页查询模板列表
     *
     * @param usageId 用途ID（可选）
     * @param keyword 搜索关键词（可选）
     * @param page    页码（从1开始）
     * @param size    每页大小
     * @return 模板列表
     */
    List<PromptTemplate> listTemplates(Long usageId, String keyword, int page, int size);

    /**
     * 统计模板数量
     *
     * @param usageId 用途ID（可选）
     * @param keyword 搜索关键词（可选）
     * @return 模板数量
     */
    long countTemplates(Long usageId, String keyword);

    /**
     * 根据ID获取模板详情
     *
     * @param templateId 模板ID
     * @return 模板实体
     */
    Optional<PromptTemplate> getTemplateById(Long templateId);

    /**
     * 获取模板的版本列表
     *
     * @param templateId 模板ID
     * @return 版本列表（按版本号降序）
     */
    List<PromptTemplateVersion> getVersionsByTemplateId(Long templateId);

    /**
     * 获取指定版本详情
     *
     * @param templateId    模板ID
     * @param versionNumber 版本号
     * @return 版本实体
     */
    Optional<PromptTemplateVersion> getVersion(Long templateId, Integer versionNumber);

    /**
     * 更新模板（生成新版本）
     *
     * @param templateId      模板ID
     * @param content         新内容
     * @param changeNote      变更说明
     * @param expectedVersion 期望的当前版本号（乐观锁）
     * @param operatorId      操作人ID
     * @return 更新后的模板
     */
    PromptTemplate updateTemplate(Long templateId, String content, String changeNote,
                                   Integer expectedVersion, Long operatorId);

    /**
     * 更新模板基本信息（不生成新版本）
     *
     * @param templateId      模板ID
     * @param name            新名称（可选）
     * @param usageId         新用途ID（可选）
     * @param description     新描述（可选）
     * @param expectedVersion 期望的当前版本号（乐观锁）
     * @param operatorId      操作人ID
     * @return 更新后的模板
     */
    PromptTemplate updateTemplateInfo(Long templateId, String name, Long usageId,
                                       String description, Integer expectedVersion, Long operatorId);

    /**
     * 回滚到历史版本（创建新版本）
     *
     * @param templateId      模板ID
     * @param targetVersion   目标版本号
     * @param expectedVersion 期望的当前版本号（乐观锁）
     * @param operatorId      操作人ID
     * @return 更新后的模板
     */
    PromptTemplate rollbackTemplate(Long templateId, Integer targetVersion,
                                     Integer expectedVersion, Long operatorId);

    /**
     * 删除模板（软删除）
     *
     * @param templateId 模板ID
     * @param operatorId 操作人ID
     */
    void deleteTemplate(Long templateId, Long operatorId);

    /**
     * 检查模板是否存在
     *
     * @param templateId 模板ID
     * @return true if template exists
     */
    boolean existsById(Long templateId);
}
