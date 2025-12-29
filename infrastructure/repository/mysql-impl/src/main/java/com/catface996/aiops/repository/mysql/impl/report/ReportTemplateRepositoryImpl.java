package com.catface996.aiops.repository.mysql.impl.report;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.domain.model.report.ReportTemplate;
import com.catface996.aiops.domain.model.report.ReportTemplateCategory;
import com.catface996.aiops.repository.mysql.mapper.report.ReportTemplateMapper;
import com.catface996.aiops.repository.mysql.po.report.ReportTemplatePO;
import com.catface996.aiops.repository.report.ReportTemplateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 报告模板仓储实现
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Repository
public class ReportTemplateRepositoryImpl implements ReportTemplateRepository {

    private final ReportTemplateMapper reportTemplateMapper;
    private final ObjectMapper objectMapper;

    public ReportTemplateRepositoryImpl(ReportTemplateMapper reportTemplateMapper, ObjectMapper objectMapper) {
        this.reportTemplateMapper = reportTemplateMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<ReportTemplate> findById(Long id) {
        ReportTemplatePO po = reportTemplateMapper.selectById(id);
        if (po == null || Boolean.TRUE.equals(po.getDeleted())) {
            return Optional.empty();
        }
        return Optional.of(toDomain(po));
    }

    @Override
    public List<ReportTemplate> findByCondition(ReportTemplateCategory category, String keyword, int page, int size) {
        Page<ReportTemplatePO> pageParam = new Page<>(page, size);
        String categoryStr = category != null ? category.name() : null;

        return reportTemplateMapper.selectPageByCondition(pageParam, categoryStr, keyword)
                .getRecords()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCondition(ReportTemplateCategory category, String keyword) {
        String categoryStr = category != null ? category.name() : null;
        return reportTemplateMapper.countByCondition(categoryStr, keyword);
    }

    @Override
    public ReportTemplate save(ReportTemplate template) {
        ReportTemplatePO po = toPO(template);
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        po.setDeleted(false);
        po.setVersion(0);
        reportTemplateMapper.insert(po);
        template.setId(po.getId());
        return template;
    }

    @Override
    public boolean update(ReportTemplate template) {
        ReportTemplatePO po = toPO(template);
        po.setUpdatedAt(LocalDateTime.now());
        int rows = reportTemplateMapper.updateById(po);
        return rows > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        int rows = reportTemplateMapper.softDeleteById(id, LocalDateTime.now());
        return rows > 0;
    }

    @Override
    public boolean existsById(Long id) {
        ReportTemplatePO po = reportTemplateMapper.selectById(id);
        return po != null && !Boolean.TRUE.equals(po.getDeleted());
    }

    @Override
    public boolean existsByName(String name) {
        ReportTemplatePO po = reportTemplateMapper.selectByName(name);
        return po != null;
    }

    @Override
    public boolean existsByNameExcludeId(String name, Long excludeId) {
        ReportTemplatePO po = reportTemplateMapper.selectByName(name);
        return po != null && !po.getId().equals(excludeId);
    }

    @Override
    public List<Long> findExistingIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return reportTemplateMapper.selectExistingIds(ids);
    }

    // ==================== 转换方法 ====================

    private ReportTemplate toDomain(ReportTemplatePO po) {
        if (po == null) {
            return null;
        }
        ReportTemplate template = new ReportTemplate();
        template.setId(po.getId());
        template.setName(po.getName());
        template.setDescription(po.getDescription());
        template.setCategory(ReportTemplateCategory.fromName(po.getCategory()));
        template.setContent(po.getContent());
        template.setTags(parseTagsJson(po.getTags()));
        template.setVersion(po.getVersion());
        template.setDeleted(po.getDeleted());
        template.setCreatedAt(po.getCreatedAt());
        template.setUpdatedAt(po.getUpdatedAt());
        return template;
    }

    private ReportTemplatePO toPO(ReportTemplate domain) {
        if (domain == null) {
            return null;
        }
        ReportTemplatePO po = new ReportTemplatePO();
        po.setId(domain.getId());
        po.setName(domain.getName());
        po.setDescription(domain.getDescription());
        po.setCategory(domain.getCategory() != null ? domain.getCategory().name() : null);
        po.setContent(domain.getContent());
        po.setTags(toTagsJson(domain.getTags()));
        po.setVersion(domain.getVersion());
        po.setDeleted(domain.getDeleted() != null ? domain.getDeleted() : false);
        po.setCreatedAt(domain.getCreatedAt());
        po.setUpdatedAt(domain.getUpdatedAt());
        return po;
    }

    private List<String> parseTagsJson(String tagsJson) {
        if (tagsJson == null || tagsJson.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private String toTagsJson(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
