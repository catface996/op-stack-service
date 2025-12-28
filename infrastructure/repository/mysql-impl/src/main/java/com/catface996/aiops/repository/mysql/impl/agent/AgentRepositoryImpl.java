package com.catface996.aiops.repository.mysql.impl.agent;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.domain.model.agent.Agent;
import com.catface996.aiops.domain.model.agent.AgentRole;
import com.catface996.aiops.repository.agent.AgentRepository;
import com.catface996.aiops.repository.mysql.mapper.agent.AgentMapper;
import com.catface996.aiops.repository.mysql.po.agent.AgentPO;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent 仓储实现
 *
 * <p>负责 Agent 的持久化操作，包含领域对象与持久化对象之间的转换。</p>
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Repository
public class AgentRepositoryImpl implements AgentRepository {

    private final AgentMapper agentMapper;

    public AgentRepositoryImpl(AgentMapper agentMapper) {
        this.agentMapper = agentMapper;
    }

    @Override
    public Optional<Agent> findById(Long id) {
        AgentPO po = agentMapper.selectById(id);
        if (po == null || po.getDeleted() != null && po.getDeleted() == 1) {
            return Optional.empty();
        }
        return Optional.of(toDomain(po));
    }

    @Override
    public List<Agent> findByCondition(AgentRole role, String keyword, int page, int size) {
        Page<AgentPO> pageParam = new Page<>(page, size);
        String roleStr = role != null ? role.name() : null;

        return agentMapper.selectPageByCondition(pageParam, roleStr, keyword)
                .getRecords()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCondition(AgentRole role, String keyword) {
        String roleStr = role != null ? role.name() : null;
        return agentMapper.countByCondition(roleStr, keyword);
    }

    @Override
    public Optional<Agent> findByName(String name) {
        AgentPO po = agentMapper.selectByName(name);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(po));
    }

    @Override
    public boolean existsByName(String name, Long excludeId) {
        return agentMapper.countByNameExcludeId(name, excludeId) > 0;
    }

    @Override
    public boolean existsGlobalSupervisor() {
        return agentMapper.countGlobalSupervisor() > 0;
    }

    @Override
    public Agent save(Agent agent) {
        AgentPO po = toPO(agent);
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        po.setDeleted(0);
        agentMapper.insert(po);
        agent.setId(po.getId());
        return agent;
    }

    @Override
    public Agent update(Agent agent) {
        AgentPO po = toPO(agent);
        po.setUpdatedAt(LocalDateTime.now());
        agentMapper.updateById(po);
        return agent;
    }

    @Override
    public boolean deleteById(Long id) {
        LambdaUpdateWrapper<AgentPO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AgentPO::getId, id)
                .eq(AgentPO::getDeleted, 0)
                .set(AgentPO::getDeleted, 1)
                .set(AgentPO::getUpdatedAt, LocalDateTime.now());
        int rows = agentMapper.update(null, updateWrapper);
        return rows > 0;
    }

    @Override
    public boolean existsById(Long id) {
        AgentPO po = agentMapper.selectById(id);
        return po != null && (po.getDeleted() == null || po.getDeleted() == 0);
    }

    @Override
    public Map<AgentRole, Long> countByRole() {
        List<Map<String, Object>> results = agentMapper.countGroupByRole();
        Map<AgentRole, Long> countMap = new EnumMap<>(AgentRole.class);
        for (Map<String, Object> result : results) {
            String roleStr = (String) result.get("role");
            Long count = ((Number) result.get("count")).longValue();
            AgentRole role = AgentRole.fromName(roleStr);
            if (role != null) {
                countMap.put(role, count);
            }
        }
        return countMap;
    }

    @Override
    public long[] sumFindings() {
        Map<String, Object> result = agentMapper.sumFindings();
        long totalWarnings = result.get("totalWarnings") != null ?
                ((Number) result.get("totalWarnings")).longValue() : 0;
        long totalCritical = result.get("totalCritical") != null ?
                ((Number) result.get("totalCritical")).longValue() : 0;
        return new long[]{totalWarnings, totalCritical};
    }

    @Override
    public long[] sumFindingsById(Long agentId) {
        Map<String, Object> result = agentMapper.sumFindingsById(agentId);
        if (result == null) {
            return new long[]{0, 0};
        }
        long warnings = result.get("warnings") != null ?
                ((Number) result.get("warnings")).longValue() : 0;
        long critical = result.get("critical") != null ?
                ((Number) result.get("critical")).longValue() : 0;
        return new long[]{warnings, critical};
    }

    // ==================== 转换方法 ====================

    private Agent toDomain(AgentPO po) {
        if (po == null) {
            return null;
        }
        Agent agent = new Agent();
        agent.setId(po.getId());
        agent.setName(po.getName());
        agent.setRole(AgentRole.fromName(po.getRole()));
        agent.setSpecialty(po.getSpecialty());

        // LLM 配置（扁平化）
        agent.setPromptTemplateId(po.getPromptTemplateId());
        agent.setModel(po.getModel());
        agent.setTemperature(po.getTemperature());
        agent.setTopP(po.getTopP());
        agent.setMaxTokens(po.getMaxTokens());
        agent.setMaxRuntime(po.getMaxRuntime());

        // 统计信息
        agent.setWarnings(po.getWarnings());
        agent.setCritical(po.getCritical());

        // 审计字段
        agent.setCreatedAt(po.getCreatedAt());
        agent.setUpdatedAt(po.getUpdatedAt());
        agent.setDeleted(po.getDeleted() != null && po.getDeleted() == 1);

        return agent;
    }

    private AgentPO toPO(Agent domain) {
        if (domain == null) {
            return null;
        }
        AgentPO po = new AgentPO();
        po.setId(domain.getId());
        po.setName(domain.getName());
        po.setRole(domain.getRole() != null ? domain.getRole().name() : null);
        po.setSpecialty(domain.getSpecialty());

        // LLM 配置（扁平化）
        po.setPromptTemplateId(domain.getPromptTemplateId());
        po.setModel(domain.getModel());
        po.setTemperature(domain.getTemperature());
        po.setTopP(domain.getTopP());
        po.setMaxTokens(domain.getMaxTokens());
        po.setMaxRuntime(domain.getMaxRuntime());

        // 统计信息
        po.setWarnings(domain.getWarnings() != null ? domain.getWarnings() : 0);
        po.setCritical(domain.getCritical() != null ? domain.getCritical() : 0);

        // 审计字段
        po.setCreatedAt(domain.getCreatedAt());
        po.setUpdatedAt(domain.getUpdatedAt());
        po.setDeleted(Boolean.TRUE.equals(domain.getDeleted()) ? 1 : 0);

        return po;
    }
}
