package com.catface996.aiops.repository.mysql.impl.prompt;

import com.catface996.aiops.domain.model.prompt.TemplateUsage;
import com.catface996.aiops.repository.mysql.mapper.prompt.TemplateUsageMapper;
import com.catface996.aiops.repository.mysql.po.prompt.TemplateUsagePO;
import com.catface996.aiops.repository.prompt.TemplateUsageRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 模板用途仓储实现
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Repository
public class TemplateUsageRepositoryImpl implements TemplateUsageRepository {

    private final TemplateUsageMapper templateUsageMapper;

    public TemplateUsageRepositoryImpl(TemplateUsageMapper templateUsageMapper) {
        this.templateUsageMapper = templateUsageMapper;
    }

    @Override
    public Optional<TemplateUsage> findById(Long id) {
        TemplateUsagePO po = templateUsageMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<TemplateUsage> findByCode(String code) {
        TemplateUsagePO po = templateUsageMapper.selectByCode(code);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<TemplateUsage> findByName(String name) {
        TemplateUsagePO po = templateUsageMapper.selectByName(name);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<TemplateUsage> findAll() {
        return templateUsageMapper.selectAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public TemplateUsage save(TemplateUsage usage) {
        TemplateUsagePO po = toPO(usage);
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        templateUsageMapper.insert(po);
        usage.setId(po.getId());
        return usage;
    }

    @Override
    public void deleteById(Long id) {
        templateUsageMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        TemplateUsagePO po = templateUsageMapper.selectById(id);
        return po != null && !Boolean.TRUE.equals(po.getDeleted());
    }

    @Override
    public boolean existsByCode(String code) {
        return templateUsageMapper.selectByCode(code) != null;
    }

    @Override
    public long countTemplatesByUsageId(Long usageId) {
        return templateUsageMapper.countTemplatesByUsageId(usageId);
    }

    // ==================== 转换方法 ====================

    private TemplateUsage toDomain(TemplateUsagePO po) {
        if (po == null) {
            return null;
        }
        TemplateUsage usage = new TemplateUsage();
        usage.setId(po.getId());
        usage.setCode(po.getCode());
        usage.setName(po.getName());
        usage.setDescription(po.getDescription());
        usage.setDeleted(po.getDeleted());
        usage.setCreatedAt(po.getCreatedAt());
        usage.setUpdatedAt(po.getUpdatedAt());
        return usage;
    }

    private TemplateUsagePO toPO(TemplateUsage domain) {
        if (domain == null) {
            return null;
        }
        TemplateUsagePO po = new TemplateUsagePO();
        po.setId(domain.getId());
        po.setCode(domain.getCode());
        po.setName(domain.getName());
        po.setDescription(domain.getDescription());
        po.setDeleted(domain.getDeleted() != null ? domain.getDeleted() : false);
        po.setCreatedAt(domain.getCreatedAt());
        po.setUpdatedAt(domain.getUpdatedAt());
        return po;
    }
}
