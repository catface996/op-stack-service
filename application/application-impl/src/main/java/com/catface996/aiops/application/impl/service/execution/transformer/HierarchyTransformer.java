package com.catface996.aiops.application.impl.service.execution.transformer;

import com.catface996.aiops.application.api.dto.agent.AgentDTO;
import com.catface996.aiops.application.api.dto.agentbound.HierarchyStructureDTO;
import com.catface996.aiops.application.api.dto.agentbound.HierarchyTeamDTO;
import com.catface996.aiops.application.impl.service.execution.client.dto.CreateHierarchyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 层级结构转换器
 *
 * <p>将 HierarchyStructureDTO 转换为 Executor API 所需的 CreateHierarchyRequest 格式。</p>
 *
 * <p>转换映射：</p>
 * <ul>
 *   <li>topologyName → hierarchy.name</li>
 *   <li>globalSupervisor.specialty → hierarchy.global_prompt</li>
 *   <li>teams[].nodeName → hierarchy.teams[].name</li>
 *   <li>teams[].supervisor.specialty → hierarchy.teams[].supervisor_prompt</li>
 *   <li>teams[].workers[] → hierarchy.teams[].workers[]</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Slf4j
@Component
public class HierarchyTransformer {

    /**
     * 将 HierarchyStructureDTO 转换为 CreateHierarchyRequest
     *
     * @param hierarchyStructure 层级结构 DTO
     * @return Executor API 创建层级请求
     */
    public CreateHierarchyRequest transform(HierarchyStructureDTO hierarchyStructure) {
        log.debug("Transforming HierarchyStructureDTO to CreateHierarchyRequest for topology: {}",
                hierarchyStructure.getTopologyName());

        // 构建 global_prompt
        String globalPrompt = buildGlobalPrompt(hierarchyStructure.getGlobalSupervisor());

        // 转换团队列表
        List<CreateHierarchyRequest.TeamConfig> teamConfigs = transformTeams(hierarchyStructure.getTeams());

        // 使用拓扑名称 + 时间戳确保唯一性（每次执行创建新的层级实例）
        String hierarchyName = hierarchyStructure.getTopologyName() + "_" + Instant.now().toEpochMilli();

        return CreateHierarchyRequest.builder()
                .name(hierarchyName)
                .globalPrompt(globalPrompt)
                .teams(teamConfigs)
                .build();
    }

    /**
     * 构建全局监督者提示词
     */
    private String buildGlobalPrompt(AgentDTO globalSupervisor) {
        if (globalSupervisor == null) {
            return "You are a global supervisor coordinating multiple teams.";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are ").append(globalSupervisor.getName()).append(". ");

        if (globalSupervisor.getSpecialty() != null && !globalSupervisor.getSpecialty().isEmpty()) {
            prompt.append(globalSupervisor.getSpecialty());
        } else {
            prompt.append("Coordinate and oversee the work of all team supervisors.");
        }

        return prompt.toString();
    }

    /**
     * 转换团队列表
     */
    private List<CreateHierarchyRequest.TeamConfig> transformTeams(List<HierarchyTeamDTO> teams) {
        if (teams == null || teams.isEmpty()) {
            return Collections.emptyList();
        }

        List<CreateHierarchyRequest.TeamConfig> teamConfigs = new ArrayList<>();

        for (HierarchyTeamDTO team : teams) {
            // 构建 supervisor_prompt
            String supervisorPrompt = buildSupervisorPrompt(team.getSupervisor(), team.getNodeName());

            // 转换工作者列表
            List<CreateHierarchyRequest.WorkerConfig> workers = transformWorkers(team.getWorkers());

            // 如果没有工作者，跳过这个团队
            if (workers.isEmpty()) {
                log.warn("Skipping team {} - no workers assigned", team.getNodeName());
                continue;
            }

            CreateHierarchyRequest.TeamConfig teamConfig = CreateHierarchyRequest.TeamConfig.builder()
                    .name(team.getNodeName())
                    .supervisorPrompt(supervisorPrompt)
                    .workers(workers)
                    .build();

            teamConfigs.add(teamConfig);
        }

        return teamConfigs;
    }

    /**
     * 构建团队监督者提示词
     */
    private String buildSupervisorPrompt(AgentDTO supervisor, String teamName) {
        if (supervisor == null) {
            return "You are the supervisor of " + teamName + ". Coordinate and oversee your team's work.";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are ").append(supervisor.getName());
        prompt.append(", the supervisor of ").append(teamName).append(". ");

        if (supervisor.getSpecialty() != null && !supervisor.getSpecialty().isEmpty()) {
            prompt.append(supervisor.getSpecialty());
        } else {
            prompt.append("Coordinate and oversee your team's work.");
        }

        return prompt.toString();
    }

    /**
     * 转换工作者 Agent 列表
     */
    private List<CreateHierarchyRequest.WorkerConfig> transformWorkers(List<AgentDTO> workers) {
        if (workers == null || workers.isEmpty()) {
            return Collections.emptyList();
        }

        List<CreateHierarchyRequest.WorkerConfig> workerConfigs = new ArrayList<>();
        for (AgentDTO worker : workers) {
            workerConfigs.add(transformWorker(worker));
        }
        return workerConfigs;
    }

    /**
     * 将 AgentDTO 转换为 WorkerConfig
     */
    private CreateHierarchyRequest.WorkerConfig transformWorker(AgentDTO agent) {
        String systemPrompt = buildWorkerSystemPrompt(agent);
        String model = agent.getModel() != null ? agent.getModel() : "gemini-2.0-flash";
        String role = agent.getRole() != null ? agent.getRole().toLowerCase() : "worker";

        return CreateHierarchyRequest.WorkerConfig.builder()
                .name(agent.getName())
                .role(role)
                .systemPrompt(systemPrompt)
                .model(model)
                .build();
    }

    /**
     * 构建工作者系统提示词
     */
    private String buildWorkerSystemPrompt(AgentDTO agent) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are ").append(agent.getName()).append(". ");

        if (agent.getSpecialty() != null && !agent.getSpecialty().isEmpty()) {
            prompt.append(agent.getSpecialty());
        } else {
            prompt.append("Complete assigned tasks efficiently.");
        }

        return prompt.toString();
    }
}
