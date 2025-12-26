package com.catface996.aiops.domain.impl.service.prompt;

import com.catface996.aiops.common.enums.PromptTemplateErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.prompt.TemplateUsage;
import com.catface996.aiops.domain.service.prompt.TemplateUsageDomainService;
import com.catface996.aiops.repository.prompt.TemplateUsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 模板用途领域服务实现
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Service
public class TemplateUsageDomainServiceImpl implements TemplateUsageDomainService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateUsageDomainServiceImpl.class);

    private final TemplateUsageRepository templateUsageRepository;

    public TemplateUsageDomainServiceImpl(TemplateUsageRepository templateUsageRepository) {
        this.templateUsageRepository = templateUsageRepository;
    }

    @Override
    @Transactional
    public TemplateUsage createUsage(String code, String name, String description) {
        logger.info("创建用途，code: {}, name: {}", code, name);

        // 验证编码格式
        if (!TemplateUsage.isValidCode(code)) {
            throw new BusinessException(PromptTemplateErrorCode.INVALID_USAGE_CODE);
        }

        // 检查编码是否已存在
        if (templateUsageRepository.existsByCode(code)) {
            throw new BusinessException(PromptTemplateErrorCode.USAGE_CODE_EXISTS, code);
        }

        TemplateUsage usage = TemplateUsage.create(code, name, description);
        usage = templateUsageRepository.save(usage);

        logger.info("用途创建成功，id: {}", usage.getId());
        return usage;
    }

    @Override
    public List<TemplateUsage> listUsages() {
        return templateUsageRepository.findAll();
    }

    @Override
    public Optional<TemplateUsage> getUsageById(Long usageId) {
        return templateUsageRepository.findById(usageId);
    }

    @Override
    @Transactional
    public void deleteUsage(Long usageId) {
        logger.info("删除用途，usageId: {}", usageId);

        // 检查用途是否存在
        if (!templateUsageRepository.existsById(usageId)) {
            throw new BusinessException(PromptTemplateErrorCode.USAGE_NOT_FOUND, usageId);
        }

        // 检查是否有模板正在使用该用途
        long templateCount = templateUsageRepository.countTemplatesByUsageId(usageId);
        if (templateCount > 0) {
            throw new BusinessException(PromptTemplateErrorCode.USAGE_IN_USE);
        }

        templateUsageRepository.deleteById(usageId);

        logger.info("用途删除成功，id: {}", usageId);
    }

    @Override
    public boolean existsById(Long usageId) {
        return templateUsageRepository.existsById(usageId);
    }
}
