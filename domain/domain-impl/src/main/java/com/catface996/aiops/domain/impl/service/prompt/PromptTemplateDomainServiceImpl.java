package com.catface996.aiops.domain.impl.service.prompt;

import com.catface996.aiops.common.enums.PromptTemplateErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.prompt.PromptTemplate;
import com.catface996.aiops.domain.model.prompt.PromptTemplateVersion;
import com.catface996.aiops.domain.service.prompt.PromptTemplateDomainService;
import com.catface996.aiops.repository.prompt.PromptTemplateRepository;
import com.catface996.aiops.repository.prompt.PromptTemplateVersionRepository;
import com.catface996.aiops.repository.prompt.TemplateUsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 提示词模板领域服务实现
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Service
public class PromptTemplateDomainServiceImpl implements PromptTemplateDomainService {

    private static final Logger logger = LoggerFactory.getLogger(PromptTemplateDomainServiceImpl.class);

    private final PromptTemplateRepository promptTemplateRepository;
    private final PromptTemplateVersionRepository promptTemplateVersionRepository;
    private final TemplateUsageRepository templateUsageRepository;

    public PromptTemplateDomainServiceImpl(PromptTemplateRepository promptTemplateRepository,
                                            PromptTemplateVersionRepository promptTemplateVersionRepository,
                                            TemplateUsageRepository templateUsageRepository) {
        this.promptTemplateRepository = promptTemplateRepository;
        this.promptTemplateVersionRepository = promptTemplateVersionRepository;
        this.templateUsageRepository = templateUsageRepository;
    }

    @Override
    @Transactional
    public PromptTemplate createTemplate(String name, String content, Long usageId,
                                          String description, Long operatorId) {
        logger.info("创建模板，name: {}, usageId: {}, operatorId: {}", name, usageId, operatorId);

        // 验证内容
        validateContent(content);

        // 检查名称是否已存在
        if (promptTemplateRepository.existsByName(name)) {
            throw new BusinessException(PromptTemplateErrorCode.TEMPLATE_NAME_EXISTS, name);
        }

        // 检查用途是否存在
        if (usageId != null && !templateUsageRepository.existsById(usageId)) {
            throw new BusinessException(PromptTemplateErrorCode.USAGE_NOT_FOUND, usageId);
        }

        // 创建模板
        PromptTemplate template = PromptTemplate.create(name, usageId, description, operatorId);
        template = promptTemplateRepository.save(template);

        // 创建初始版本
        PromptTemplateVersion version = PromptTemplateVersion.createInitial(
                template.getId(), content, operatorId);
        promptTemplateVersionRepository.save(version);

        // 设置内容返回
        template.setContent(content);

        logger.info("模板创建成功，id: {}, currentVersion: {}", template.getId(), template.getCurrentVersion());
        return template;
    }

    @Override
    public List<PromptTemplate> listTemplates(Long usageId, String keyword, int page, int size) {
        return promptTemplateRepository.findByCondition(usageId, keyword, page, size);
    }

    @Override
    public long countTemplates(Long usageId, String keyword) {
        return promptTemplateRepository.countByCondition(usageId, keyword);
    }

    @Override
    public Optional<PromptTemplate> getTemplateById(Long templateId) {
        return promptTemplateRepository.findByIdWithDetail(templateId);
    }

    @Override
    public List<PromptTemplateVersion> getVersionsByTemplateId(Long templateId) {
        // 检查模板是否存在
        if (!promptTemplateRepository.existsById(templateId)) {
            throw new BusinessException(PromptTemplateErrorCode.TEMPLATE_NOT_FOUND, templateId);
        }
        return promptTemplateVersionRepository.findByTemplateId(templateId);
    }

    @Override
    public Optional<PromptTemplateVersion> getVersion(Long templateId, Integer versionNumber) {
        // 检查模板是否存在
        if (!promptTemplateRepository.existsById(templateId)) {
            throw new BusinessException(PromptTemplateErrorCode.TEMPLATE_NOT_FOUND, templateId);
        }
        return promptTemplateVersionRepository.findByTemplateIdAndVersion(templateId, versionNumber);
    }

    @Override
    @Transactional
    public PromptTemplate updateTemplate(Long templateId, String content, String changeNote,
                                          Integer expectedVersion, Long operatorId) {
        logger.info("更新模板内容，templateId: {}, expectedVersion: {}, operatorId: {}",
                templateId, expectedVersion, operatorId);

        // 验证内容
        validateContent(content);

        // 获取模板
        PromptTemplate template = promptTemplateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(PromptTemplateErrorCode.TEMPLATE_NOT_FOUND, templateId));

        // 检查乐观锁版本
        if (expectedVersion != null && !expectedVersion.equals(template.getVersion())) {
            throw new BusinessException(PromptTemplateErrorCode.VERSION_CONFLICT);
        }

        // 获取当前版本内容，检查是否有变化
        PromptTemplateVersion currentVersion = promptTemplateVersionRepository
                .findByTemplateIdAndVersion(templateId, template.getCurrentVersion())
                .orElse(null);
        if (currentVersion != null && content.equals(currentVersion.getContent())) {
            throw new BusinessException(PromptTemplateErrorCode.TEMPLATE_CONTENT_UNCHANGED);
        }

        // 递增版本号
        template.incrementVersion();

        // 创建新版本
        PromptTemplateVersion newVersion = PromptTemplateVersion.create(
                templateId, template.getCurrentVersion(), content, changeNote, operatorId);
        promptTemplateVersionRepository.save(newVersion);

        // 更新模板
        if (!promptTemplateRepository.update(template)) {
            throw new BusinessException(PromptTemplateErrorCode.VERSION_CONFLICT);
        }

        // 设置内容返回
        template.setContent(content);

        logger.info("模板更新成功，id: {}, newVersion: {}", template.getId(), template.getCurrentVersion());
        return template;
    }

    @Override
    @Transactional
    public PromptTemplate updateTemplateInfo(Long templateId, String name, Long usageId,
                                              String description, Integer expectedVersion, Long operatorId) {
        logger.info("更新模板基本信息，templateId: {}, operatorId: {}", templateId, operatorId);

        // 获取模板
        PromptTemplate template = promptTemplateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(PromptTemplateErrorCode.TEMPLATE_NOT_FOUND, templateId));

        // 检查乐观锁版本
        if (expectedVersion != null && !expectedVersion.equals(template.getVersion())) {
            throw new BusinessException(PromptTemplateErrorCode.VERSION_CONFLICT);
        }

        // 如果修改名称，检查新名称是否已存在
        if (name != null && !name.equals(template.getName())
                && promptTemplateRepository.existsByNameExcludingId(name, templateId)) {
            throw new BusinessException(PromptTemplateErrorCode.TEMPLATE_NAME_EXISTS, name);
        }

        // 检查用途是否存在
        if (usageId != null && !templateUsageRepository.existsById(usageId)) {
            throw new BusinessException(PromptTemplateErrorCode.USAGE_NOT_FOUND, usageId);
        }

        // 更新基本信息
        template.update(name, usageId, description);

        if (!promptTemplateRepository.update(template)) {
            throw new BusinessException(PromptTemplateErrorCode.VERSION_CONFLICT);
        }

        return promptTemplateRepository.findByIdWithDetail(templateId).orElse(template);
    }

    @Override
    @Transactional
    public PromptTemplate rollbackTemplate(Long templateId, Integer targetVersion,
                                            Integer expectedVersion, Long operatorId) {
        logger.info("回滚模板，templateId: {}, targetVersion: {}, operatorId: {}",
                templateId, targetVersion, operatorId);

        // 获取模板
        PromptTemplate template = promptTemplateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(PromptTemplateErrorCode.TEMPLATE_NOT_FOUND, templateId));

        // 检查乐观锁版本
        if (expectedVersion != null && !expectedVersion.equals(template.getVersion())) {
            throw new BusinessException(PromptTemplateErrorCode.VERSION_CONFLICT);
        }

        // 检查是否已是最早版本
        if (template.getCurrentVersion() == 1) {
            throw new BusinessException(PromptTemplateErrorCode.ALREADY_EARLIEST_VERSION);
        }

        // 获取目标版本
        PromptTemplateVersion targetVersionEntity = promptTemplateVersionRepository
                .findByTemplateIdAndVersion(templateId, targetVersion)
                .orElseThrow(() -> new BusinessException(
                        PromptTemplateErrorCode.VERSION_NOT_FOUND, templateId, targetVersion));

        // 递增版本号
        template.incrementVersion();

        // 创建回滚版本（内容来自目标版本）
        PromptTemplateVersion rollbackVersion = PromptTemplateVersion.createRollback(
                templateId, template.getCurrentVersion(), targetVersionEntity.getContent(),
                targetVersion, operatorId);
        promptTemplateVersionRepository.save(rollbackVersion);

        // 更新模板
        if (!promptTemplateRepository.update(template)) {
            throw new BusinessException(PromptTemplateErrorCode.VERSION_CONFLICT);
        }

        // 设置内容返回
        template.setContent(targetVersionEntity.getContent());

        logger.info("模板回滚成功，id: {}, newVersion: {}", template.getId(), template.getCurrentVersion());
        return template;
    }

    @Override
    @Transactional
    public void deleteTemplate(Long templateId, Long operatorId) {
        logger.info("删除模板，templateId: {}, operatorId: {}", templateId, operatorId);

        PromptTemplate template = promptTemplateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(PromptTemplateErrorCode.TEMPLATE_NOT_FOUND, templateId));

        // 软删除模板
        promptTemplateRepository.deleteById(templateId);

        logger.info("模板删除成功，id: {}", templateId);
    }

    @Override
    public boolean existsById(Long templateId) {
        return promptTemplateRepository.existsById(templateId);
    }

    // ===== 私有方法 =====

    private void validateContent(String content) {
        if (PromptTemplate.isContentEmpty(content)) {
            throw new BusinessException(PromptTemplateErrorCode.TEMPLATE_CONTENT_EMPTY);
        }
        if (PromptTemplate.isContentTooLarge(content)) {
            throw new BusinessException(PromptTemplateErrorCode.TEMPLATE_CONTENT_TOO_LARGE);
        }
    }
}
