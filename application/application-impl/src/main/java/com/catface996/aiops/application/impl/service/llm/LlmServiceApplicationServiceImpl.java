package com.catface996.aiops.application.impl.service.llm;

import com.catface996.aiops.application.api.dto.llm.CreateLlmServiceCommand;
import com.catface996.aiops.application.api.dto.llm.LlmServiceDTO;
import com.catface996.aiops.application.api.dto.llm.ModelParametersDTO;
import com.catface996.aiops.application.api.dto.llm.UpdateLlmServiceCommand;
import com.catface996.aiops.application.api.service.llm.LlmServiceApplicationService;
import com.catface996.aiops.common.enums.LlmServiceErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.llm.LlmService;
import com.catface996.aiops.domain.model.llm.ModelParameters;
import com.catface996.aiops.domain.service.llm.LlmServiceDomainService;
import com.catface996.aiops.repository.llm.LlmServiceRepository;
import com.catface996.aiops.repository.llm.entity.LlmServiceEntity;
import com.catface996.aiops.domain.model.llm.ProviderType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * LLM 服务应用服务实现
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
@Service
public class LlmServiceApplicationServiceImpl implements LlmServiceApplicationService {

    private final LlmServiceDomainService llmServiceDomainService;
    private final LlmServiceRepository llmServiceRepository;
    private final ObjectMapper objectMapper;

    public LlmServiceApplicationServiceImpl(
            LlmServiceDomainService llmServiceDomainService,
            LlmServiceRepository llmServiceRepository,
            ObjectMapper objectMapper) {
        this.llmServiceDomainService = llmServiceDomainService;
        this.llmServiceRepository = llmServiceRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<LlmServiceDTO> list(boolean enabledOnly) {
        List<LlmServiceEntity> entities;
        if (enabledOnly) {
            entities = llmServiceRepository.findByEnabled(true);
        } else {
            entities = llmServiceRepository.findAll();
        }
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LlmServiceDTO getById(Long id) {
        LlmServiceEntity entity = llmServiceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(LlmServiceErrorCode.LLM_SERVICE_NOT_FOUND));
        return toDTO(entity);
    }

    @Override
    @Transactional
    public LlmServiceDTO create(CreateLlmServiceCommand command) {
        LlmService domain = toDomain(command);
        LlmService created = llmServiceDomainService.create(domain);
        return toDTO(created);
    }

    @Override
    @Transactional
    public LlmServiceDTO update(Long id, UpdateLlmServiceCommand command) {
        LlmService domain = toDomain(command);
        LlmService updated = llmServiceDomainService.update(id, domain);
        return toDTO(updated);
    }

    @Override
    @Transactional
    public void delete(Long id, boolean force) {
        llmServiceDomainService.delete(id, force);
    }

    @Override
    @Transactional
    public LlmServiceDTO updateStatus(Long id, boolean enabled) {
        LlmService updated = llmServiceDomainService.updateStatus(id, enabled);
        return toDTO(updated);
    }

    @Override
    @Transactional
    public LlmServiceDTO setDefault(Long id) {
        LlmService updated = llmServiceDomainService.setDefault(id);
        return toDTO(updated);
    }

    /**
     * CreateCommand 转领域对象
     */
    private LlmService toDomain(CreateLlmServiceCommand command) {
        return LlmService.builder()
                .name(command.getName())
                .description(command.getDescription())
                .providerType(command.getProviderType())
                .endpoint(command.getEndpoint())
                .modelParameters(toModelParameters(command.getModelParameters()))
                .priority(command.getPriority())
                .enabled(command.getEnabled())
                .isDefault(false)
                .build();
    }

    /**
     * UpdateCommand 转领域对象
     */
    private LlmService toDomain(UpdateLlmServiceCommand command) {
        return LlmService.builder()
                .name(command.getName())
                .description(command.getDescription())
                .providerType(command.getProviderType())
                .endpoint(command.getEndpoint())
                .modelParameters(command.getModelParameters() != null ? toModelParameters(command.getModelParameters()) : null)
                .priority(command.getPriority())
                .build();
    }

    /**
     * ModelParametersDTO 转值对象
     */
    private ModelParameters toModelParameters(ModelParametersDTO dto) {
        if (dto == null) {
            return null;
        }
        return ModelParameters.builder()
                .modelName(dto.getModelName())
                .temperature(dto.getTemperature())
                .maxTokens(dto.getMaxTokens())
                .topP(dto.getTopP())
                .frequencyPenalty(dto.getFrequencyPenalty())
                .presencePenalty(dto.getPresencePenalty())
                .build();
    }

    /**
     * 领域对象转 DTO
     */
    private LlmServiceDTO toDTO(LlmService domain) {
        return LlmServiceDTO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .providerType(domain.getProviderType())
                .endpoint(domain.getEndpoint())
                .modelParameters(toModelParametersDTO(domain.getModelParameters()))
                .priority(domain.getPriority())
                .enabled(domain.getEnabled())
                .isDefault(domain.getIsDefault())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * Entity 转 DTO
     */
    private LlmServiceDTO toDTO(LlmServiceEntity entity) {
        return LlmServiceDTO.builder()
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
     * ModelParameters 转 DTO
     */
    private ModelParametersDTO toModelParametersDTO(ModelParameters params) {
        if (params == null) {
            return null;
        }
        return ModelParametersDTO.builder()
                .modelName(params.getModelName())
                .temperature(params.getTemperature())
                .maxTokens(params.getMaxTokens())
                .topP(params.getTopP())
                .frequencyPenalty(params.getFrequencyPenalty())
                .presencePenalty(params.getPresencePenalty())
                .build();
    }

    /**
     * JSON 转 ModelParametersDTO
     */
    private ModelParametersDTO fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ModelParametersDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize ModelParameters", e);
        }
    }
}
