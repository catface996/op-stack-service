package com.catface996.aiops.repository.mysql.impl.report;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.domain.model.report.Report;
import com.catface996.aiops.domain.model.report.ReportStatus;
import com.catface996.aiops.domain.model.report.ReportType;
import com.catface996.aiops.repository.mysql.mapper.report.ReportMapper;
import com.catface996.aiops.repository.mysql.po.report.ReportPO;
import com.catface996.aiops.repository.report.ReportRepository;
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
 * 报告仓储实现
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Repository
public class ReportRepositoryImpl implements ReportRepository {

    private final ReportMapper reportMapper;
    private final ObjectMapper objectMapper;

    public ReportRepositoryImpl(ReportMapper reportMapper, ObjectMapper objectMapper) {
        this.reportMapper = reportMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Report> findById(Long id) {
        ReportPO po = reportMapper.selectById(id);
        if (po == null || Boolean.TRUE.equals(po.getDeleted())) {
            return Optional.empty();
        }
        return Optional.of(toDomain(po));
    }

    @Override
    public List<Report> findByCondition(ReportType type, ReportStatus status, String keyword,
                                         String sortBy, String sortOrder, int page, int size) {
        Page<ReportPO> pageParam = new Page<>(page, size);
        String typeStr = type != null ? type.name() : null;
        String statusStr = status != null ? status.name() : null;

        return reportMapper.selectPageByCondition(pageParam, typeStr, statusStr, keyword, sortBy, sortOrder)
                .getRecords()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCondition(ReportType type, ReportStatus status, String keyword) {
        String typeStr = type != null ? type.name() : null;
        String statusStr = status != null ? status.name() : null;
        return reportMapper.countByCondition(typeStr, statusStr, keyword);
    }

    @Override
    public Report save(Report report) {
        ReportPO po = toPO(report);
        po.setCreatedAt(LocalDateTime.now());
        po.setDeleted(false);
        reportMapper.insert(po);
        report.setId(po.getId());
        return report;
    }

    @Override
    public boolean deleteById(Long id) {
        // 软删除
        LambdaUpdateWrapper<ReportPO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ReportPO::getId, id)
                .eq(ReportPO::getDeleted, false)
                .set(ReportPO::getDeleted, true);
        int rows = reportMapper.update(null, updateWrapper);
        return rows > 0;
    }

    @Override
    public boolean existsById(Long id) {
        ReportPO po = reportMapper.selectById(id);
        return po != null && !Boolean.TRUE.equals(po.getDeleted());
    }

    // ==================== 转换方法 ====================

    private Report toDomain(ReportPO po) {
        if (po == null) {
            return null;
        }
        Report report = new Report();
        report.setId(po.getId());
        report.setTitle(po.getTitle());
        report.setType(ReportType.fromName(po.getType()));
        report.setStatus(ReportStatus.fromName(po.getStatus()));
        report.setAuthor(po.getAuthor());
        report.setSummary(po.getSummary());
        report.setContent(po.getContent());
        report.setTags(parseTagsJson(po.getTags()));
        report.setTopologyId(po.getTopologyId());
        report.setDeleted(po.getDeleted());
        report.setCreatedAt(po.getCreatedAt());
        return report;
    }

    private ReportPO toPO(Report domain) {
        if (domain == null) {
            return null;
        }
        ReportPO po = new ReportPO();
        po.setId(domain.getId());
        po.setTitle(domain.getTitle());
        po.setType(domain.getType() != null ? domain.getType().name() : null);
        po.setStatus(domain.getStatus() != null ? domain.getStatus().name() : null);
        po.setAuthor(domain.getAuthor());
        po.setSummary(domain.getSummary());
        po.setContent(domain.getContent());
        po.setTags(toTagsJson(domain.getTags()));
        po.setTopologyId(domain.getTopologyId());
        po.setDeleted(domain.getDeleted() != null ? domain.getDeleted() : false);
        po.setCreatedAt(domain.getCreatedAt());
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
