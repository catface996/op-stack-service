package com.catface996.aiops.application.impl.service.prompt;

import com.catface996.aiops.application.api.dto.prompt.TemplateUsageDTO;
import com.catface996.aiops.application.api.dto.prompt.request.CreateTemplateUsageRequest;
import com.catface996.aiops.application.api.service.prompt.TemplateUsageApplicationService;
import com.catface996.aiops.domain.model.prompt.TemplateUsage;
import com.catface996.aiops.domain.service.prompt.TemplateUsageDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 模板用途应用服务实现
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Service
public class TemplateUsageApplicationServiceImpl implements TemplateUsageApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateUsageApplicationServiceImpl.class);

    private final TemplateUsageDomainService templateUsageDomainService;

    public TemplateUsageApplicationServiceImpl(TemplateUsageDomainService templateUsageDomainService) {
        this.templateUsageDomainService = templateUsageDomainService;
    }

    @Override
    public TemplateUsageDTO createUsage(CreateTemplateUsageRequest request) {
        logger.info("创建用途，code: {}, name: {}", request.getCode(), request.getName());

        TemplateUsage usage = templateUsageDomainService.createUsage(
                request.getCode(),
                request.getName(),
                request.getDescription()
        );

        return toDTO(usage);
    }

    @Override
    public List<TemplateUsageDTO> listUsages() {
        return templateUsageDomainService.listUsages().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUsage(Long usageId) {
        logger.info("删除用途，usageId: {}", usageId);
        templateUsageDomainService.deleteUsage(usageId);
    }

    private TemplateUsageDTO toDTO(TemplateUsage usage) {
        if (usage == null) {
            return null;
        }

        return TemplateUsageDTO.builder()
                .id(usage.getId())
                .code(usage.getCode())
                .name(usage.getName())
                .description(usage.getDescription())
                .createdAt(usage.getCreatedAt())
                .updatedAt(usage.getUpdatedAt())
                .build();
    }
}
