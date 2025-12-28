package com.catface996.aiops.repository.mysql.impl.agent;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.catface996.aiops.domain.model.agent.AgentStatus;
import com.catface996.aiops.domain.model.agent.AgentTeamRelation;
import com.catface996.aiops.repository.agent.AgentTeamRelationRepository;
import com.catface996.aiops.repository.mysql.mapper.agent.AgentTeamRelationMapper;
import com.catface996.aiops.repository.mysql.po.agent.AgentTeamRelationPO;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent-Team 关联仓储实现
 *
 * @author AI Assistant
 * @since 2025-12-28
 */
@Repository
public class AgentTeamRelationRepositoryImpl implements AgentTeamRelationRepository {

    private final AgentTeamRelationMapper relationMapper;

    public AgentTeamRelationRepositoryImpl(AgentTeamRelationMapper relationMapper) {
        this.relationMapper = relationMapper;
    }

    @Override
    public Optional<AgentTeamRelation> findById(Long id) {
        AgentTeamRelationPO po = relationMapper.selectById(id);
        if (po == null || po.getDeleted() != null && po.getDeleted() == 1) {
            return Optional.empty();
        }
        return Optional.of(toDomain(po));
    }

    @Override
    public Optional<AgentTeamRelation> findByAgentIdAndTeamId(Long agentId, Long teamId) {
        AgentTeamRelationPO po = relationMapper.selectByAgentIdAndTeamId(agentId, teamId);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(po));
    }

    @Override
    public List<AgentTeamRelation> findByAgentId(Long agentId) {
        return relationMapper.selectByAgentId(agentId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AgentTeamRelation> findByTeamId(Long teamId) {
        return relationMapper.selectByTeamId(teamId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AgentTeamRelation> findByTeamIdAndStatus(Long teamId, AgentStatus status) {
        String statusStr = status != null ? status.name() : null;
        return relationMapper.selectByTeamIdAndStatus(teamId, statusStr)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByAgentIdAndTeamId(Long agentId, Long teamId) {
        return relationMapper.countByAgentIdAndTeamId(agentId, teamId) > 0;
    }

    @Override
    public long countByTeamId(Long teamId) {
        return relationMapper.countByTeamId(teamId);
    }

    @Override
    public Map<AgentStatus, Long> countByStatus() {
        List<Map<String, Object>> results = relationMapper.countGroupByStatus();
        Map<AgentStatus, Long> countMap = new EnumMap<>(AgentStatus.class);
        for (Map<String, Object> result : results) {
            String statusStr = (String) result.get("status");
            Long count = ((Number) result.get("count")).longValue();
            AgentStatus status = AgentStatus.fromName(statusStr);
            if (status != null) {
                countMap.put(status, count);
            }
        }
        return countMap;
    }

    @Override
    public AgentTeamRelation save(AgentTeamRelation relation) {
        AgentTeamRelationPO po = toPO(relation);
        po.setCreatedAt(LocalDateTime.now());
        po.setDeleted(0);
        relationMapper.insert(po);
        relation.setId(po.getId());
        return relation;
    }

    @Override
    public AgentTeamRelation update(AgentTeamRelation relation) {
        AgentTeamRelationPO po = toPO(relation);
        relationMapper.updateById(po);
        return relation;
    }

    @Override
    public boolean deleteById(Long id) {
        LambdaUpdateWrapper<AgentTeamRelationPO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AgentTeamRelationPO::getId, id)
                .eq(AgentTeamRelationPO::getDeleted, 0)
                .set(AgentTeamRelationPO::getDeleted, 1);
        int rows = relationMapper.update(null, updateWrapper);
        return rows > 0;
    }

    @Override
    public boolean deleteByAgentIdAndTeamId(Long agentId, Long teamId) {
        int rows = relationMapper.softDeleteByAgentIdAndTeamId(agentId, teamId);
        return rows > 0;
    }

    @Override
    public int deleteByAgentId(Long agentId) {
        return relationMapper.softDeleteByAgentId(agentId);
    }

    @Override
    public boolean isAgentBusyInAnyTeam(Long agentId) {
        return relationMapper.countBusyByAgentId(agentId) > 0;
    }

    @Override
    public List<Long> findTeamIdsByAgentId(Long agentId) {
        return relationMapper.selectTeamIdsByAgentId(agentId);
    }

    // ==================== 转换方法 ====================

    private AgentTeamRelation toDomain(AgentTeamRelationPO po) {
        if (po == null) {
            return null;
        }
        AgentTeamRelation relation = new AgentTeamRelation();
        relation.setId(po.getId());
        relation.setAgentId(po.getAgentId());
        relation.setTeamId(po.getTeamId());
        relation.setStatus(AgentStatus.fromName(po.getStatus()));
        relation.setCurrentTask(po.getCurrentTask());
        relation.setCreatedAt(po.getCreatedAt());
        relation.setDeleted(po.getDeleted() != null && po.getDeleted() == 1);
        return relation;
    }

    private AgentTeamRelationPO toPO(AgentTeamRelation domain) {
        if (domain == null) {
            return null;
        }
        AgentTeamRelationPO po = new AgentTeamRelationPO();
        po.setId(domain.getId());
        po.setAgentId(domain.getAgentId());
        po.setTeamId(domain.getTeamId());
        po.setStatus(domain.getStatus() != null ? domain.getStatus().name() : AgentStatus.IDLE.name());
        po.setCurrentTask(domain.getCurrentTask());
        po.setCreatedAt(domain.getCreatedAt());
        po.setDeleted(Boolean.TRUE.equals(domain.getDeleted()) ? 1 : 0);
        return po;
    }
}
