package com.catface996.aiops.application.impl.service.prompt;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.prompt.PromptTemplateDTO;
import com.catface996.aiops.application.api.dto.prompt.PromptTemplateDetailDTO;
import com.catface996.aiops.application.api.dto.prompt.PromptTemplateVersionDTO;
import com.catface996.aiops.application.api.dto.prompt.request.CreatePromptTemplateRequest;
import com.catface996.aiops.application.api.dto.prompt.request.DeleteTemplateRequest;
import com.catface996.aiops.application.api.dto.prompt.request.ListPromptTemplatesRequest;
import com.catface996.aiops.application.api.dto.prompt.request.RollbackTemplateRequest;
import com.catface996.aiops.application.api.dto.prompt.request.UpdatePromptTemplateRequest;
import com.catface996.aiops.application.api.service.prompt.PromptTemplateApplicationService;
import com.catface996.aiops.common.enums.PromptTemplateErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.prompt.PromptTemplate;
import com.catface996.aiops.domain.model.prompt.PromptTemplateVersion;
import com.catface996.aiops.domain.service.prompt.PromptTemplateDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 提示词模板应用服务实现
 *
 * <p>协调领域层完成提示词模板管理业务逻辑，负责 DTO 转换。</p>
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Service
public class PromptTemplateApplicationServiceImpl implements PromptTemplateApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(PromptTemplateApplicationServiceImpl.class);

    private final PromptTemplateDomainService promptTemplateDomainService;

    public PromptTemplateApplicationServiceImpl(PromptTemplateDomainService promptTemplateDomainService) {
        this.promptTemplateDomainService = promptTemplateDomainService;
    }

    @Override
    public PromptTemplateDTO createPromptTemplate(CreatePromptTemplateRequest request) {
        logger.info("创建提示词模板，name: {}, operatorId: {}", request.getName(), request.getOperatorId());

        PromptTemplate template = promptTemplateDomainService.createTemplate(
                request.getName(),
                request.getContent(),
                request.getUsageId(),
                request.getDescription(),
                request.getOperatorId()
        );

        return toDTO(template);
    }

    @Override
    public PageResult<PromptTemplateDTO> listPromptTemplates(ListPromptTemplatesRequest request) {
        logger.info("查询提示词模板列表，usageId: {}, keyword: {}, page: {}, size: {}",
                request.getUsageId(), request.getKeyword(), request.getPage(), request.getSize());

        List<PromptTemplate> templates = promptTemplateDomainService.listTemplates(
                request.getUsageId(),
                request.getKeyword(),
                request.getPage(),
                request.getSize()
        );

        long total = promptTemplateDomainService.countTemplates(
                request.getUsageId(),
                request.getKeyword()
        );

        List<PromptTemplateDTO> dtos = templates.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtos, request.getPage(), request.getSize(), total);
    }

    @Override
    public PromptTemplateDetailDTO getTemplateDetail(Long templateId) {
        logger.info("获取模板详情，templateId: {}", templateId);

        PromptTemplate template = promptTemplateDomainService.getTemplateById(templateId)
                .orElseThrow(() -> new BusinessException(PromptTemplateErrorCode.TEMPLATE_NOT_FOUND, templateId));

        List<PromptTemplateVersion> versions = promptTemplateDomainService.getVersionsByTemplateId(templateId);

        return toDetailDTO(template, versions);
    }

    @Override
    public PromptTemplateVersionDTO getVersionDetail(Long templateId, Integer versionNumber) {
        logger.info("获取版本详情，templateId: {}, versionNumber: {}", templateId, versionNumber);

        PromptTemplateVersion version = promptTemplateDomainService.getVersion(templateId, versionNumber)
                .orElseThrow(() -> new BusinessException(
                        PromptTemplateErrorCode.VERSION_NOT_FOUND, templateId, versionNumber));

        return toVersionDTO(version);
    }

    @Override
    public PromptTemplateDTO updatePromptTemplate(UpdatePromptTemplateRequest request) {
        logger.info("更新提示词模板，id: {}, operatorId: {}", request.getId(), request.getOperatorId());

        PromptTemplate template = promptTemplateDomainService.updateTemplate(
                request.getId(),
                request.getContent(),
                request.getChangeNote(),
                request.getExpectedVersion(),
                request.getOperatorId()
        );

        return toDTO(template);
    }

    @Override
    public PromptTemplateDTO rollbackPromptTemplate(RollbackTemplateRequest request) {
        logger.info("回滚模板，id: {}, targetVersion: {}, operatorId: {}",
                request.getId(), request.getTargetVersion(), request.getOperatorId());

        PromptTemplate template = promptTemplateDomainService.rollbackTemplate(
                request.getId(),
                request.getTargetVersion(),
                request.getExpectedVersion(),
                request.getOperatorId()
        );

        return toDTO(template);
    }

    @Override
    public void deletePromptTemplate(DeleteTemplateRequest request) {
        logger.info("删除模板，id: {}, operatorId: {}", request.getId(), request.getOperatorId());

        promptTemplateDomainService.deleteTemplate(request.getId(), request.getOperatorId());
    }

    // ===== DTO 转换方法 =====

    private PromptTemplateDTO toDTO(PromptTemplate template) {
        if (template == null) {
            return null;
        }

        return PromptTemplateDTO.builder()
                .id(template.getId())
                .name(template.getName())
                .usageId(template.getUsageId())
                .usageName(template.getUsageName())
                .description(template.getDescription())
                .currentVersion(template.getCurrentVersion())
                .content(template.getContent())
                .version(template.getVersion())
                .createdBy(template.getCreatedBy())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    private PromptTemplateDetailDTO toDetailDTO(PromptTemplate template, List<PromptTemplateVersion> versions) {
        if (template == null) {
            return null;
        }

        List<PromptTemplateVersionDTO> versionDTOs = versions.stream()
                .map(this::toVersionDTO)
                .collect(Collectors.toList());

        return PromptTemplateDetailDTO.builder()
                .id(template.getId())
                .name(template.getName())
                .usageId(template.getUsageId())
                .usageName(template.getUsageName())
                .description(template.getDescription())
                .currentVersion(template.getCurrentVersion())
                .content(template.getContent())
                .version(template.getVersion())
                .createdBy(template.getCreatedBy())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .versions(versionDTOs)
                .build();
    }

    private PromptTemplateVersionDTO toVersionDTO(PromptTemplateVersion version) {
        if (version == null) {
            return null;
        }

        return PromptTemplateVersionDTO.builder()
                .id(version.getId())
                .templateId(version.getTemplateId())
                .versionNumber(version.getVersionNumber())
                .content(version.getContent())
                .changeNote(version.getChangeNote())
                .createdBy(version.getCreatedBy())
                .createdAt(version.getCreatedAt())
                .build();
    }
}
