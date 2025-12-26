package com.catface996.aiops.repository.mysql.impl.prompt;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.domain.model.prompt.PromptTemplate;
import com.catface996.aiops.repository.mysql.mapper.prompt.PromptTemplateMapper;
import com.catface996.aiops.repository.mysql.po.prompt.PromptTemplatePO;
import com.catface996.aiops.repository.prompt.PromptTemplateRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 提示词模板仓储实现
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Repository
public class PromptTemplateRepositoryImpl implements PromptTemplateRepository {

    private final PromptTemplateMapper promptTemplateMapper;

    public PromptTemplateRepositoryImpl(PromptTemplateMapper promptTemplateMapper) {
        this.promptTemplateMapper = promptTemplateMapper;
    }

    @Override
    public Optional<PromptTemplate> findById(Long id) {
        PromptTemplatePO po = promptTemplateMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<PromptTemplate> findByIdWithDetail(Long id) {
        PromptTemplatePO po = promptTemplateMapper.selectByIdWithDetail(id);
        return Optional.ofNullable(po).map(this::toDomainWithDetail);
    }

    @Override
    public Optional<PromptTemplate> findByName(String name) {
        PromptTemplatePO po = promptTemplateMapper.selectByName(name);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<PromptTemplate> findByCondition(Long usageId, String keyword, int page, int size) {
        Page<PromptTemplatePO> pageParam = new Page<>(page, size);
        return promptTemplateMapper.selectPageWithUsage(pageParam, keyword, usageId)
                .getRecords()
                .stream()
                .map(this::toDomainWithUsage)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCondition(Long usageId, String keyword) {
        return promptTemplateMapper.countByCondition(keyword, usageId);
    }

    @Override
    public PromptTemplate save(PromptTemplate template) {
        PromptTemplatePO po = toPO(template);
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        promptTemplateMapper.insert(po);
        template.setId(po.getId());
        return template;
    }

    @Override
    public boolean update(PromptTemplate template) {
        PromptTemplatePO po = toPO(template);
        po.setUpdatedAt(LocalDateTime.now());
        int rows = promptTemplateMapper.updateById(po);
        return rows > 0;
    }

    @Override
    public void deleteById(Long id) {
        // 软删除：使用 UpdateWrapper 直接更新 deleted 和 updated_at 字段
        LambdaUpdateWrapper<PromptTemplatePO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PromptTemplatePO::getId, id)
                .set(PromptTemplatePO::getDeleted, true)
                .set(PromptTemplatePO::getUpdatedAt, LocalDateTime.now());
        promptTemplateMapper.update(null, updateWrapper);
    }

    @Override
    public boolean existsById(Long id) {
        PromptTemplatePO po = promptTemplateMapper.selectById(id);
        return po != null && !Boolean.TRUE.equals(po.getDeleted());
    }

    @Override
    public boolean existsByName(String name) {
        return promptTemplateMapper.selectByName(name) != null;
    }

    @Override
    public boolean existsByNameExcludingId(String name, Long excludeId) {
        PromptTemplatePO po = promptTemplateMapper.selectByName(name);
        return po != null && !po.getId().equals(excludeId);
    }

    // ==================== 转换方法 ====================

    private PromptTemplate toDomain(PromptTemplatePO po) {
        if (po == null) {
            return null;
        }
        PromptTemplate template = new PromptTemplate();
        template.setId(po.getId());
        template.setName(po.getName());
        template.setUsageId(po.getUsageId());
        template.setDescription(po.getDescription());
        template.setCurrentVersion(po.getCurrentVersion());
        template.setVersion(po.getVersion());
        template.setDeleted(po.getDeleted());
        template.setCreatedBy(po.getCreatedBy());
        template.setCreatedAt(po.getCreatedAt());
        template.setUpdatedAt(po.getUpdatedAt());
        return template;
    }

    private PromptTemplate toDomainWithUsage(PromptTemplatePO po) {
        PromptTemplate template = toDomain(po);
        if (template != null && po.getUsageName() != null) {
            template.setUsageName(po.getUsageName());
        }
        return template;
    }

    private PromptTemplate toDomainWithDetail(PromptTemplatePO po) {
        PromptTemplate template = toDomainWithUsage(po);
        if (template != null && po.getContent() != null) {
            template.setContent(po.getContent());
        }
        return template;
    }

    private PromptTemplatePO toPO(PromptTemplate domain) {
        if (domain == null) {
            return null;
        }
        PromptTemplatePO po = new PromptTemplatePO();
        po.setId(domain.getId());
        po.setName(domain.getName());
        po.setUsageId(domain.getUsageId());
        po.setDescription(domain.getDescription());
        po.setCurrentVersion(domain.getCurrentVersion());
        po.setVersion(domain.getVersion());
        po.setDeleted(domain.getDeleted() != null ? domain.getDeleted() : false);
        po.setCreatedBy(domain.getCreatedBy());
        po.setCreatedAt(domain.getCreatedAt());
        po.setUpdatedAt(domain.getUpdatedAt());
        return po;
    }
}
