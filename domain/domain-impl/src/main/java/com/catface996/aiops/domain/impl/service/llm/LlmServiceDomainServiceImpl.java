package com.catface996.aiops.domain.impl.service.llm;

import com.catface996.aiops.common.enums.LlmServiceErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.llm.LlmService;
import com.catface996.aiops.domain.model.llm.ModelParameters;
import com.catface996.aiops.domain.model.llm.ProviderType;
import com.catface996.aiops.domain.service.llm.LlmServiceDomainService;
import com.catface996.aiops.repository.llm.LlmServiceRepository;
import com.catface996.aiops.repository.llm.entity.LlmServiceEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * LLM 服务领域服务实现
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
@Slf4j
@Service
public class LlmServiceDomainServiceImpl implements LlmServiceDomainService {

    private final LlmServiceRepository llmServiceRepository;
    private final ObjectMapper objectMapper;

    public LlmServiceDomainServiceImpl(LlmServiceRepository llmServiceRepository, ObjectMapper objectMapper) {
        this.llmServiceRepository = llmServiceRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public LlmService create(LlmService llmService) {
        // 验证名称唯一性
        if (existsByName(llmService.getName(), null)) {
            throw new BusinessException(LlmServiceErrorCode.LLM_SERVICE_NAME_DUPLICATE);
        }

        // 验证业务规则
        if (!llmService.isValid()) {
            throw new BusinessException(LlmServiceErrorCode.LLM_SERVICE_INVALID_PARAMS);
        }

        // 转换并保存
        LlmServiceEntity entity = toEntity(llmService);
        LlmServiceEntity saved = llmServiceRepository.save(entity);

        log.info("[LLM] 创建服务: id={}, name={}, provider={}", saved.getId(), saved.getName(), saved.getProviderType());
        return toDomain(saved);
    }

    @Override
    @Transactional
    public LlmService update(Long id, LlmService llmService) {
        // 查找服务
        LlmServiceEntity existing = llmServiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(LlmServiceErrorCode.LLM_SERVICE_NOT_FOUND));

        // 验证名称唯一性（排除自身）
        if (llmService.getName() != null && existsByName(llmService.getName(), id)) {
            throw new BusinessException(LlmServiceErrorCode.LLM_SERVICE_NAME_DUPLICATE);
        }

        // 更新字段
        if (llmService.getName() != null) {
            existing.setName(llmService.getName());
        }
        if (llmService.getDescription() != null) {
            existing.setDescription(llmService.getDescription());
        }
        if (llmService.getProviderType() != null) {
            existing.setProviderType(llmService.getProviderType().name());
        }
        if (llmService.getEndpoint() != null) {
            existing.setEndpoint(llmService.getEndpoint());
        }
        if (llmService.getModelParameters() != null) {
            existing.setModelParameters(toJson(llmService.getModelParameters()));
        }
        if (llmService.getPriority() != null) {
            existing.setPriority(llmService.getPriority());
        }

        LlmServiceEntity saved = llmServiceRepository.save(existing);

        log.info("[LLM] 更新服务: id={}", id);
        return toDomain(saved);
    }

    @Override
    public boolean existsByName(String name, Long excludeId) {
        Optional<LlmServiceEntity> existing = llmServiceRepository.findByName(name);
        if (existing.isEmpty()) {
            return false;
        }
        if (excludeId != null && existing.get().getId().equals(excludeId)) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public LlmService updateStatus(Long id, boolean enabled) {
        LlmServiceEntity existing = llmServiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(LlmServiceErrorCode.LLM_SERVICE_NOT_FOUND));

        // 如果要禁用，检查是否为唯一默认服务
        if (!enabled && Boolean.TRUE.equals(existing.getIsDefault())) {
            int enabledDefaultCount = llmServiceRepository.countEnabledDefault();
            if (enabledDefaultCount <= 1) {
                throw new BusinessException(LlmServiceErrorCode.LLM_SERVICE_CANNOT_DISABLE);
            }
        }

        existing.setEnabled(enabled);
        LlmServiceEntity saved = llmServiceRepository.save(existing);

        log.info("[LLM] 服务状态变更: id={}, enabled={}", id, enabled);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public void delete(Long id, boolean force) {
        LlmServiceEntity existing = llmServiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(LlmServiceErrorCode.LLM_SERVICE_NOT_FOUND));

        // TODO: 未来检查是否被 Agent 引用，如果有引用且 force=false 则抛出异常

        llmServiceRepository.deleteById(id);
        log.warn("[LLM] 删除服务: id={}, force={}", id, force);
    }

    @Override
    @Transactional
    public LlmService setDefault(Long id) {
        LlmServiceEntity existing = llmServiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(LlmServiceErrorCode.LLM_SERVICE_NOT_FOUND));

        // 检查服务是否启用
        if (!Boolean.TRUE.equals(existing.getEnabled())) {
            throw new BusinessException(LlmServiceErrorCode.LLM_SERVICE_MUST_ENABLED);
        }

        // 清除所有默认标记
        llmServiceRepository.clearAllDefault();

        // 设置新的默认
        llmServiceRepository.setDefault(id);

        // 重新查询返回
        LlmServiceEntity saved = llmServiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(LlmServiceErrorCode.LLM_SERVICE_NOT_FOUND));

        log.info("[LLM] 设置默认服务: id={}", id);
        return toDomain(saved);
    }

    /**
     * 领域对象转实体
     */
    private LlmServiceEntity toEntity(LlmService domain) {
        return LlmServiceEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .providerType(domain.getProviderType() != null ? domain.getProviderType().name() : null)
                .endpoint(domain.getEndpoint())
                .modelParameters(toJson(domain.getModelParameters()))
                .priority(domain.getPriority())
                .enabled(domain.getEnabled())
                .isDefault(domain.getIsDefault())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * 实体转领域对象
     */
    private LlmService toDomain(LlmServiceEntity entity) {
        return LlmService.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .providerType(entity.getProviderType() != null ? ProviderType.valueOf(entity.getProviderType()) : null)
                .endpoint(entity.getEndpoint())
                .modelParameters(fromJson(entity.getModelParameters()))
                .priority(entity.getPriority())
                .enabled(entity.getEnabled())
                .isDefault(entity.getIsDefault())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * ModelParameters 转 JSON
     */
    private String toJson(ModelParameters params) {
        if (params == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize ModelParameters", e);
        }
    }

    /**
     * JSON 转 ModelParameters
     */
    private ModelParameters fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ModelParameters.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize ModelParameters", e);
        }
    }
}
