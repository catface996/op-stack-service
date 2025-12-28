package com.catface996.aiops.application.impl.service.reporttemplate;

import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.dto.report.ReportTemplateDTO;
import com.catface996.aiops.application.api.dto.report.request.CreateReportTemplateRequest;
import com.catface996.aiops.application.api.dto.report.request.DeleteReportTemplateRequest;
import com.catface996.aiops.application.api.dto.report.request.ListReportTemplatesRequest;
import com.catface996.aiops.application.api.dto.report.request.UpdateReportTemplateRequest;
import com.catface996.aiops.application.api.service.report.ReportTemplateApplicationService;
import com.catface996.aiops.common.enums.ReportErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.report.ReportTemplate;
import com.catface996.aiops.domain.model.report.ReportTemplateCategory;
import com.catface996.aiops.repository.report.ReportTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 报告模板应用服务实现
 *
 * <p>协调仓储层完成报告模板管理业务逻辑，负责 DTO 转换。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Service
public class ReportTemplateApplicationServiceImpl implements ReportTemplateApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ReportTemplateApplicationServiceImpl.class);

    private final ReportTemplateRepository reportTemplateRepository;

    public ReportTemplateApplicationServiceImpl(ReportTemplateRepository reportTemplateRepository) {
        this.reportTemplateRepository = reportTemplateRepository;
    }

    @Override
    public PageResult<ReportTemplateDTO> listTemplates(ListReportTemplatesRequest request) {
        logger.info("查询模板列表，category: {}, keyword: {}, page: {}, size: {}",
                request.getCategory(), request.getKeyword(), request.getPage(), request.getSize());

        // 解析枚举类型
        ReportTemplateCategory category = ReportTemplateCategory.fromName(request.getCategory());

        // 查询模板列表
        List<ReportTemplate> templates = reportTemplateRepository.findByCondition(
                category,
                request.getKeyword(),
                request.getPage(),
                request.getSize()
        );

        // 查询总数
        long total = reportTemplateRepository.countByCondition(category, request.getKeyword());

        // 转换为 DTO
        List<ReportTemplateDTO> dtos = templates.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtos, request.getPage(), request.getSize(), total);
    }

    @Override
    public ReportTemplateDTO getTemplateById(Long templateId) {
        logger.info("获取模板详情，templateId: {}", templateId);

        ReportTemplate template = reportTemplateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException(ReportErrorCode.TEMPLATE_NOT_FOUND, templateId));

        return toDTO(template);
    }

    @Override
    @Transactional
    public ReportTemplateDTO createTemplate(CreateReportTemplateRequest request) {
        logger.info("创建模板，name: {}, category: {}", request.getName(), request.getCategory());

        // 解析并验证分类
        ReportTemplateCategory category = ReportTemplateCategory.fromName(request.getCategory());
        if (category == null) {
            throw new BusinessException(ReportErrorCode.INVALID_TEMPLATE_CATEGORY, request.getCategory());
        }

        // 验证模板名称是否已存在
        if (reportTemplateRepository.existsByName(request.getName())) {
            throw new BusinessException(ReportErrorCode.TEMPLATE_NAME_EXISTS, request.getName());
        }

        // 验证模板内容不为空
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new BusinessException(ReportErrorCode.TEMPLATE_CONTENT_EMPTY);
        }

        // 创建模板
        ReportTemplate template = ReportTemplate.create(
                request.getName(),
                request.getDescription(),
                category,
                request.getContent(),
                request.getTags()
        );

        // 保存模板
        template = reportTemplateRepository.save(template);

        return toDTO(template);
    }

    @Override
    @Transactional
    public ReportTemplateDTO updateTemplate(UpdateReportTemplateRequest request) {
        logger.info("更新模板，id: {}, expectedVersion: {}", request.getId(), request.getExpectedVersion());

        // 查询现有模板
        ReportTemplate template = reportTemplateRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException(ReportErrorCode.TEMPLATE_NOT_FOUND, request.getId()));

        // 验证乐观锁版本
        if (!template.getVersion().equals(request.getExpectedVersion())) {
            throw new BusinessException(ReportErrorCode.VERSION_CONFLICT);
        }

        // 如果更新名称，验证名称是否已存在（排除自身）
        if (request.getName() != null && !request.getName().equals(template.getName())) {
            if (reportTemplateRepository.existsByNameExcludeId(request.getName(), request.getId())) {
                throw new BusinessException(ReportErrorCode.TEMPLATE_NAME_EXISTS, request.getName());
            }
        }

        // 解析分类（如果提供）
        ReportTemplateCategory category = null;
        if (request.getCategory() != null) {
            category = ReportTemplateCategory.fromName(request.getCategory());
            if (category == null) {
                throw new BusinessException(ReportErrorCode.INVALID_TEMPLATE_CATEGORY, request.getCategory());
            }
        }

        // 更新模板
        template.update(
                request.getName(),
                request.getDescription(),
                category,
                request.getContent(),
                request.getTags()
        );

        // 保存更新
        boolean updated = reportTemplateRepository.update(template);
        if (!updated) {
            throw new BusinessException(ReportErrorCode.VERSION_CONFLICT);
        }

        // 重新查询返回最新数据
        template = reportTemplateRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException(ReportErrorCode.TEMPLATE_NOT_FOUND, request.getId()));

        return toDTO(template);
    }

    @Override
    @Transactional
    public void deleteTemplate(DeleteReportTemplateRequest request) {
        logger.info("删除模板，id: {}", request.getId());

        // 验证模板是否存在
        if (!reportTemplateRepository.existsById(request.getId())) {
            throw new BusinessException(ReportErrorCode.TEMPLATE_NOT_FOUND, request.getId());
        }

        // 软删除
        reportTemplateRepository.deleteById(request.getId());
    }

    // ===== DTO 转换方法 =====

    private ReportTemplateDTO toDTO(ReportTemplate template) {
        if (template == null) {
            return null;
        }

        return ReportTemplateDTO.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .category(template.getCategory() != null ? template.getCategory().name() : null)
                .content(template.getContent())
                .tags(template.getTags())
                .version(template.getVersion())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
