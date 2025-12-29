package com.catface996.aiops.application.impl.service.agent;

import com.catface996.aiops.application.api.dto.agent.AgentDTO;
import com.catface996.aiops.application.api.dto.agent.AgentStatsDTO;
import com.catface996.aiops.application.api.dto.agent.request.*;
import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.service.agent.AgentApplicationService;
import com.catface996.aiops.domain.model.agent.Agent;
import com.catface996.aiops.domain.model.agent.AgentHierarchyLevel;
import com.catface996.aiops.domain.model.agent.AgentRole;
import com.catface996.aiops.common.enums.AgentErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.repository.agent.AgentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Agent 应用服务实现
 *
 * <p>协调仓储层完成 Agent 管理业务逻辑，负责 DTO 转换。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Service
public class AgentApplicationServiceImpl implements AgentApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(AgentApplicationServiceImpl.class);

    private final AgentRepository agentRepository;

    public AgentApplicationServiceImpl(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    @Override
    public PageResult<AgentDTO> listAgents(ListAgentsRequest request) {
        logger.info("查询 Agent 列表，role: {}, keyword: {}, page: {}, size: {}",
                request.getRole(), request.getKeyword(),
                request.getPage(), request.getSize());

        AgentRole role = AgentRole.fromName(request.getRole());

        List<Agent> agents = agentRepository.findByCondition(
                role,
                request.getKeyword(),
                request.getPage(),
                request.getSize()
        );

        long total = agentRepository.countByCondition(role, request.getKeyword());

        List<AgentDTO> dtos = agents.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PageResult.of(dtos, request.getPage(), request.getSize(), total);
    }

    @Override
    public AgentDTO getAgentById(Long agentId) {
        logger.info("获取 Agent 详情，agentId: {}", agentId);

        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new BusinessException(AgentErrorCode.AGENT_NOT_FOUND, agentId));

        return toDTO(agent);
    }

    @Override
    @Transactional
    public AgentDTO createAgent(CreateAgentRequest request) {
        logger.info("创建 Agent，name: {}, role: {}, hierarchyLevel: {}",
                request.getName(), request.getRole(), request.getHierarchyLevel());

        AgentRole role = AgentRole.fromName(request.getRole());
        if (role == null) {
            throw new BusinessException(AgentErrorCode.INVALID_AGENT_ROLE, request.getRole());
        }

        AgentHierarchyLevel hierarchyLevel = AgentHierarchyLevel.fromName(request.getHierarchyLevel());
        // 默认层级为 TEAM_WORKER
        if (hierarchyLevel == null) {
            hierarchyLevel = AgentHierarchyLevel.TEAM_WORKER;
        }

        // GLOBAL_SUPERVISOR 层级只能有一个
        if (hierarchyLevel == AgentHierarchyLevel.GLOBAL_SUPERVISOR && agentRepository.existsGlobalSupervisor()) {
            throw new BusinessException(AgentErrorCode.GLOBAL_SUPERVISOR_EXISTS);
        }

        if (agentRepository.existsByName(request.getName(), null)) {
            throw new BusinessException(AgentErrorCode.AGENT_NAME_EXISTS, request.getName());
        }

        // 使用工厂方法创建 Agent，传入 LLM 配置参数
        Agent agent = Agent.create(
                request.getName(),
                role,
                hierarchyLevel,
                request.getSpecialty(),
                request.getPromptTemplateId(),
                request.getModel()
        );

        // 设置其他 LLM 配置（如果提供）
        if (request.getTemperature() != null) {
            agent.setTemperature(request.getTemperature());
        }
        if (request.getTopP() != null) {
            agent.setTopP(request.getTopP());
        }
        if (request.getMaxTokens() != null) {
            agent.setMaxTokens(request.getMaxTokens());
        }
        if (request.getMaxRuntime() != null) {
            agent.setMaxRuntime(request.getMaxRuntime());
        }

        agent = agentRepository.save(agent);

        return toDTO(agent);
    }

    @Override
    @Transactional
    public AgentDTO updateAgent(UpdateAgentRequest request) {
        logger.info("更新 Agent，id: {}, name: {}, model: {}",
                request.getId(), request.getName(), request.getModel());

        Agent agent = agentRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException(AgentErrorCode.AGENT_NOT_FOUND, request.getId()));

        // 更新基本信息
        if (request.getName() != null && !request.getName().equals(agent.getName())) {
            if (agentRepository.existsByName(request.getName(), request.getId())) {
                throw new BusinessException(AgentErrorCode.AGENT_NAME_EXISTS, request.getName());
            }
            agent.setName(request.getName());
        }

        // 更新层级
        if (request.getHierarchyLevel() != null) {
            AgentHierarchyLevel newLevel = AgentHierarchyLevel.fromName(request.getHierarchyLevel());
            if (newLevel != null && newLevel != agent.getHierarchyLevel()) {
                // 如果要变成 GLOBAL_SUPERVISOR，检查是否已存在
                if (newLevel == AgentHierarchyLevel.GLOBAL_SUPERVISOR && agentRepository.existsGlobalSupervisor()) {
                    throw new BusinessException(AgentErrorCode.GLOBAL_SUPERVISOR_EXISTS);
                }
                agent.setHierarchyLevel(newLevel);
            }
        }

        if (request.getSpecialty() != null) {
            agent.setSpecialty(request.getSpecialty());
        }

        // 更新 LLM 配置（支持部分更新）
        agent.updateLlmConfig(
                request.getPromptTemplateId(),
                request.getModel(),
                request.getTemperature(),
                request.getTopP(),
                request.getMaxTokens(),
                request.getMaxRuntime()
        );

        agent = agentRepository.update(agent);

        return toDTO(agent);
    }

    @Override
    @Transactional
    public void deleteAgent(DeleteAgentRequest request) {
        logger.info("删除 Agent，id: {}", request.getId());

        Agent agent = agentRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException(AgentErrorCode.AGENT_NOT_FOUND, request.getId()));

        // GLOBAL_SUPERVISOR 层级不能删除
        if (agent.isGlobalSupervisor()) {
            throw new BusinessException(AgentErrorCode.CANNOT_DELETE_GLOBAL_SUPERVISOR);
        }

        // 软删除 Agent
        agentRepository.deleteById(request.getId());
    }

    @Override
    public AgentStatsDTO getAgentStats(AgentStatsRequest request) {
        logger.info("查询 Agent 统计信息，agentId: {}", request.getAgentId());

        if (request.getAgentId() != null) {
            // 单个 Agent 统计
            long[] findings = agentRepository.sumFindingsById(request.getAgentId());
            return AgentStatsDTO.builder()
                    .totalAgents(1L)
                    .totalWarnings(findings[0])
                    .totalCritical(findings[1])
                    .build();
        }

        // 整体统计
        Map<AgentRole, Long> byRole = agentRepository.countByRole();
        long[] findings = agentRepository.sumFindings();

        long totalAgents = byRole.values().stream().mapToLong(Long::longValue).sum();

        Map<String, Long> byRoleStr = byRole.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));

        return AgentStatsDTO.builder()
                .totalAgents(totalAgents)
                .byRole(byRoleStr)
                .totalWarnings(findings[0])
                .totalCritical(findings[1])
                .build();
    }

    // ===== DTO 转换方法 =====

    private AgentDTO toDTO(Agent agent) {
        if (agent == null) {
            return null;
        }

        return AgentDTO.builder()
                .id(agent.getId())
                .name(agent.getName())
                .role(agent.getRole() != null ? agent.getRole().name() : null)
                .hierarchyLevel(agent.getHierarchyLevel() != null ? agent.getHierarchyLevel().name() : null)
                .specialty(agent.getSpecialty())
                // LLM 配置（扁平化）
                .promptTemplateId(agent.getPromptTemplateId())
                .promptTemplateName(agent.getPromptTemplateName())
                .model(agent.getModel())
                .temperature(agent.getTemperature())
                .topP(agent.getTopP())
                .maxTokens(agent.getMaxTokens())
                .maxRuntime(agent.getMaxRuntime())
                // 统计信息
                .warnings(agent.getWarnings() != null ? agent.getWarnings() : 0)
                .critical(agent.getCritical() != null ? agent.getCritical() : 0)
                // 审计字段
                .createdAt(agent.getCreatedAt())
                .updatedAt(agent.getUpdatedAt())
                .build();
    }
}
