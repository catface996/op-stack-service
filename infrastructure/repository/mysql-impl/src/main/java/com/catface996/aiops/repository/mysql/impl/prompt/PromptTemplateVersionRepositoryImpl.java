package com.catface996.aiops.repository.mysql.impl.prompt;

import com.catface996.aiops.domain.model.prompt.PromptTemplateVersion;
import com.catface996.aiops.repository.mysql.mapper.prompt.PromptTemplateVersionMapper;
import com.catface996.aiops.repository.mysql.po.prompt.PromptTemplateVersionPO;
import com.catface996.aiops.repository.prompt.PromptTemplateVersionRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 模板版本仓储实现
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Repository
public class PromptTemplateVersionRepositoryImpl implements PromptTemplateVersionRepository {

    private final PromptTemplateVersionMapper promptTemplateVersionMapper;

    public PromptTemplateVersionRepositoryImpl(PromptTemplateVersionMapper promptTemplateVersionMapper) {
        this.promptTemplateVersionMapper = promptTemplateVersionMapper;
    }

    @Override
    public Optional<PromptTemplateVersion> findById(Long id) {
        PromptTemplateVersionPO po = promptTemplateVersionMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<PromptTemplateVersion> findByTemplateIdAndVersion(Long templateId, Integer versionNumber) {
        PromptTemplateVersionPO po = promptTemplateVersionMapper.selectByTemplateIdAndVersion(templateId, versionNumber);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<PromptTemplateVersion> findByTemplateId(Long templateId) {
        return promptTemplateVersionMapper.selectByTemplateId(templateId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PromptTemplateVersion> findLatestByTemplateId(Long templateId) {
        PromptTemplateVersionPO po = promptTemplateVersionMapper.selectLatestByTemplateId(templateId);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public PromptTemplateVersion save(PromptTemplateVersion version) {
        PromptTemplateVersionPO po = toPO(version);
        po.setCreatedAt(LocalDateTime.now());
        promptTemplateVersionMapper.insert(po);
        version.setId(po.getId());
        return version;
    }

    @Override
    public long countByTemplateId(Long templateId) {
        return promptTemplateVersionMapper.countByTemplateId(templateId);
    }

    @Override
    public void deleteByTemplateId(Long templateId) {
        promptTemplateVersionMapper.deleteByTemplateId(templateId);
    }

    // ==================== 转换方法 ====================

    private PromptTemplateVersion toDomain(PromptTemplateVersionPO po) {
        if (po == null) {
            return null;
        }
        PromptTemplateVersion version = new PromptTemplateVersion();
        version.setId(po.getId());
        version.setTemplateId(po.getTemplateId());
        version.setVersionNumber(po.getVersionNumber());
        version.setContent(po.getContent());
        version.setChangeNote(po.getChangeNote());
        version.setCreatedBy(po.getCreatedBy());
        version.setCreatedAt(po.getCreatedAt());
        return version;
    }

    private PromptTemplateVersionPO toPO(PromptTemplateVersion domain) {
        if (domain == null) {
            return null;
        }
        PromptTemplateVersionPO po = new PromptTemplateVersionPO();
        po.setId(domain.getId());
        po.setTemplateId(domain.getTemplateId());
        po.setVersionNumber(domain.getVersionNumber());
        po.setContent(domain.getContent());
        po.setChangeNote(domain.getChangeNote());
        po.setCreatedBy(domain.getCreatedBy());
        po.setCreatedAt(domain.getCreatedAt());
        return po;
    }
}
