package com.catface996.aiops.application.impl.service.agent;

import com.catface996.aiops.application.api.dto.agent.AgentDTO;
import com.catface996.aiops.application.api.dto.agent.AgentStatsDTO;
import com.catface996.aiops.application.api.dto.agent.request.*;
import com.catface996.aiops.application.api.dto.common.PageResult;
import com.catface996.aiops.application.api.service.agent.AgentApplicationService;
import com.catface996.aiops.domain.model.agent.Agent;
import com.catface996.aiops.domain.model.agent.AgentRole;
import com.catface996.aiops.domain.model.agent.AgentStatus;
import com.catface996.aiops.domain.model.agent.AgentTeamRelation;
import com.catface996.aiops.common.enums.AgentErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.repository.agent.AgentRepository;
import com.catface996.aiops.repository.agent.AgentTeamRelationRepository;
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
    private final AgentTeamRelationRepository relationRepository;

    public AgentApplicationServiceImpl(AgentRepository agentRepository,
                                       AgentTeamRelationRepository relationRepository) {
        this.agentRepository = agentRepository;
        this.relationRepository = relationRepository;
    }

    @Override
    public PageResult<AgentDTO> listAgents(ListAgentsRequest request) {
        logger.info("查询 Agent 列表，role: {}, teamId: {}, keyword: {}, page: {}, size: {}",
                request.getRole(), request.getTeamId(), request.getKeyword(),
                request.getPage(), request.getSize());

        AgentRole role = AgentRole.fromName(request.getRole());

        List<Agent> agents = agentRepository.findByCondition(
                role,
                request.getTeamId(),
                request.getKeyword(),
                request.getPage(),
                request.getSize()
        );

        long total = agentRepository.countByCondition(role, request.getTeamId(), request.getKeyword());

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
        logger.info("创建 Agent，name: {}, role: {}", request.getName(), request.getRole());

        AgentRole role = AgentRole.fromName(request.getRole());
        if (role == null) {
            throw new BusinessException(AgentErrorCode.INVALID_AGENT_ROLE, request.getRole());
        }

        if (role == AgentRole.GLOBAL_SUPERVISOR && agentRepository.existsGlobalSupervisor()) {
            throw new BusinessException(AgentErrorCode.GLOBAL_SUPERVISOR_EXISTS);
        }

        if (agentRepository.existsByName(request.getName(), null)) {
            throw new BusinessException(AgentErrorCode.AGENT_NAME_EXISTS, request.getName());
        }

        // 使用工厂方法创建 Agent，传入 LLM 配置参数
        Agent agent = Agent.create(
                request.getName(),
                role,
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

        if (relationRepository.isAgentBusyInAnyTeam(request.getId())) {
            throw new BusinessException(AgentErrorCode.AGENT_IS_BUSY, "WORKING/THINKING");
        }

        // 更新基本信息
        if (request.getName() != null && !request.getName().equals(agent.getName())) {
            if (agentRepository.existsByName(request.getName(), request.getId())) {
                throw new BusinessException(AgentErrorCode.AGENT_NAME_EXISTS, request.getName());
            }
            agent.setName(request.getName());
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

        // GLOBAL_SUPERVISOR 不能删除
        if (agent.getRole() == AgentRole.GLOBAL_SUPERVISOR) {
            throw new BusinessException(AgentErrorCode.CANNOT_DELETE_GLOBAL_SUPERVISOR);
        }

        // TEAM_SUPERVISOR 有成员时不能删除
        if (agent.getRole() == AgentRole.TEAM_SUPERVISOR) {
            List<Long> teamIds = relationRepository.findTeamIdsByAgentId(request.getId());
            for (Long teamId : teamIds) {
                if (relationRepository.countByTeamId(teamId) > 1) {
                    throw new BusinessException(AgentErrorCode.CANNOT_DELETE_SUPERVISOR_WITH_MEMBERS, request.getId());
                }
            }
        }

        // 检查是否正在工作中
        if (relationRepository.isAgentBusyInAnyTeam(request.getId())) {
            throw new BusinessException(AgentErrorCode.AGENT_IS_BUSY, "WORKING/THINKING");
        }

        // 删除所有团队关联
        relationRepository.deleteByAgentId(request.getId());

        // 软删除 Agent
        agentRepository.deleteById(request.getId());
    }

    @Override
    @Transactional
    public void assignAgent(AssignAgentRequest request) {
        logger.info("分配 Agent 到团队，agentId: {}, teamId: {}", request.getAgentId(), request.getTeamId());

        // 验证 Agent 存在
        if (!agentRepository.existsById(request.getAgentId())) {
            throw new BusinessException(AgentErrorCode.AGENT_NOT_FOUND, request.getAgentId());
        }

        // 验证是否已分配
        if (relationRepository.existsByAgentIdAndTeamId(request.getAgentId(), request.getTeamId())) {
            throw new BusinessException(AgentErrorCode.AGENT_ALREADY_ASSIGNED, request.getAgentId(), request.getTeamId());
        }

        // 创建关联
        AgentTeamRelation relation = AgentTeamRelation.create(request.getAgentId(), request.getTeamId());
        relationRepository.save(relation);
    }

    @Override
    @Transactional
    public void unassignAgent(UnassignAgentRequest request) {
        logger.info("取消 Agent 团队分配，agentId: {}, teamId: {}", request.getAgentId(), request.getTeamId());

        // 验证 Agent 存在
        Agent agent = agentRepository.findById(request.getAgentId())
                .orElseThrow(() -> new BusinessException(AgentErrorCode.AGENT_NOT_FOUND, request.getAgentId()));

        // TEAM_SUPERVISOR 不能取消分配
        if (agent.getRole() == AgentRole.TEAM_SUPERVISOR) {
            throw new BusinessException(AgentErrorCode.SUPERVISOR_CANNOT_UNASSIGN);
        }

        // 验证关联存在
        if (!relationRepository.existsByAgentIdAndTeamId(request.getAgentId(), request.getTeamId())) {
            throw new BusinessException(AgentErrorCode.AGENT_TEAM_RELATION_NOT_FOUND, request.getAgentId(), request.getTeamId());
        }

        // 删除关联
        relationRepository.deleteByAgentIdAndTeamId(request.getAgentId(), request.getTeamId());
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
        Map<AgentStatus, Long> byStatus = relationRepository.countByStatus();
        long[] findings = agentRepository.sumFindings();

        long totalAgents = byRole.values().stream().mapToLong(Long::longValue).sum();

        Map<String, Long> byRoleStr = byRole.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
        Map<String, Long> byStatusStr = byStatus.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));

        return AgentStatsDTO.builder()
                .totalAgents(totalAgents)
                .byRole(byRoleStr)
                .byStatus(byStatusStr)
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
                // 关联信息
                .teamIds(agent.getTeamIds())
                // 审计字段
                .createdAt(agent.getCreatedAt())
                .updatedAt(agent.getUpdatedAt())
                .build();
    }
}
