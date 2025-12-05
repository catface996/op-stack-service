package com.catface996.aiops.repository.mysql.impl.llm;

import com.catface996.aiops.repository.llm.LlmServiceRepository;
import com.catface996.aiops.repository.llm.entity.LlmServiceEntity;
import com.catface996.aiops.repository.mysql.mapper.llm.LlmServiceMapper;
import com.catface996.aiops.repository.mysql.po.llm.LlmServicePO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * LLM 服务仓储实现
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
@Repository
public class LlmServiceRepositoryImpl implements LlmServiceRepository {

    private final LlmServiceMapper llmServiceMapper;

    public LlmServiceRepositoryImpl(LlmServiceMapper llmServiceMapper) {
        this.llmServiceMapper = llmServiceMapper;
    }

    @Override
    public Optional<LlmServiceEntity> findById(Long id) {
        LlmServicePO po = llmServiceMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toEntity);
    }

    @Override
    public List<LlmServiceEntity> findAll() {
        return llmServiceMapper.findAllOrderByPriority().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<LlmServiceEntity> findByEnabled(boolean enabled) {
        return llmServiceMapper.findByEnabled(enabled).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public LlmServiceEntity save(LlmServiceEntity entity) {
        LlmServicePO po = toPO(entity);
        if (po.getId() == null) {
            llmServiceMapper.insert(po);
        } else {
            llmServiceMapper.updateById(po);
        }
        return toEntity(po);
    }

    @Override
    public void deleteById(Long id) {
        llmServiceMapper.deleteById(id);
    }

    @Override
    public Optional<LlmServiceEntity> findByName(String name) {
        LlmServicePO po = llmServiceMapper.findByName(name);
        return Optional.ofNullable(po).map(this::toEntity);
    }

    @Override
    public void clearAllDefault() {
        llmServiceMapper.clearAllDefault();
    }

    @Override
    public void setDefault(Long id) {
        llmServiceMapper.setDefault(id);
    }

    @Override
    public int countEnabledDefault() {
        return llmServiceMapper.countEnabledDefault();
    }

    /**
     * PO 转 Entity
     */
    private LlmServiceEntity toEntity(LlmServicePO po) {
        return LlmServiceEntity.builder()
                .id(po.getId())
                .name(po.getName())
                .description(po.getDescription())
                .providerType(po.getProviderType())
                .endpoint(po.getEndpoint())
                .modelParameters(po.getModelParameters())
                .priority(po.getPriority())
                .enabled(po.getEnabled())
                .isDefault(po.getIsDefault())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    /**
     * Entity 转 PO
     */
    private LlmServicePO toPO(LlmServiceEntity entity) {
        return LlmServicePO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .providerType(entity.getProviderType())
                .endpoint(entity.getEndpoint())
                .modelParameters(entity.getModelParameters())
                .priority(entity.getPriority())
                .enabled(entity.getEnabled())
                .isDefault(entity.getIsDefault())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
