package com.catface996.aiops.repository.mysql.impl.agentbound;

import com.catface996.aiops.domain.model.agent.AgentHierarchyLevel;
import com.catface996.aiops.domain.model.agentbound.AgentBound;
import com.catface996.aiops.domain.model.agentbound.BoundEntityType;
import com.catface996.aiops.repository.agentbound.AgentBoundRepository;
import com.catface996.aiops.repository.mysql.mapper.agentbound.AgentBoundMapper;
import com.catface996.aiops.repository.mysql.po.agentbound.AgentBoundPO;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Agent 绑定关系仓储实现
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Repository
public class AgentBoundRepositoryImpl implements AgentBoundRepository {

    private final AgentBoundMapper agentBoundMapper;

    public AgentBoundRepositoryImpl(AgentBoundMapper agentBoundMapper) {
        this.agentBoundMapper = agentBoundMapper;
    }

    @Override
    public AgentBound save(AgentBound agentBound) {
        AgentBoundPO po = toPO(agentBound);
        po.setCreatedAt(LocalDateTime.now());
        po.setDeleted(0);
        agentBoundMapper.insert(po);
        return toDomain(po);
    }

    @Override
    public Optional<AgentBound> findById(Long id) {
        AgentBoundPO po = agentBoundMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<AgentBound> findByEntity(BoundEntityType entityType, Long entityId, AgentHierarchyLevel hierarchyLevel) {
        if (entityType == null || entityId == null) {
            return Collections.emptyList();
        }
        String levelStr = hierarchyLevel != null ? hierarchyLevel.name() : null;
        List<AgentBoundPO> pos = agentBoundMapper.selectByEntity(entityType.name(), entityId, levelStr);
        return pos.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<AgentBound> findByAgentId(Long agentId, BoundEntityType entityType) {
        if (agentId == null) {
            return Collections.emptyList();
        }
        String typeStr = entityType != null ? entityType.name() : null;
        List<AgentBoundPO> pos = agentBoundMapper.selectByAgentId(agentId, typeStr);
        return pos.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<AgentBound> findHierarchyByTopologyId(Long topologyId) {
        if (topologyId == null) {
            return Collections.emptyList();
        }
        List<AgentBoundPO> pos = agentBoundMapper.selectHierarchyByTopologyId(topologyId);
        return pos.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<AgentBound> findSupervisorBinding(BoundEntityType entityType, Long entityId, AgentHierarchyLevel hierarchyLevel) {
        if (entityType == null || entityId == null || hierarchyLevel == null) {
            return Optional.empty();
        }
        AgentBoundPO po = agentBoundMapper.selectSupervisorBinding(
                entityType.name(), entityId, hierarchyLevel.name());
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public boolean existsByEntityAndHierarchy(BoundEntityType entityType, Long entityId, AgentHierarchyLevel hierarchyLevel) {
        if (entityType == null || entityId == null || hierarchyLevel == null) {
            return false;
        }
        int count = agentBoundMapper.existsByEntityAndHierarchy(
                entityType.name(), entityId, hierarchyLevel.name());
        return count > 0;
    }

    @Override
    public boolean existsBinding(Long agentId, Long entityId, BoundEntityType entityType) {
        if (agentId == null || entityId == null || entityType == null) {
            return false;
        }
        int count = agentBoundMapper.existsBinding(agentId, entityId, entityType.name());
        return count > 0;
    }

    @Override
    public int deleteBinding(Long agentId, Long entityId, BoundEntityType entityType) {
        if (agentId == null || entityId == null || entityType == null) {
            return 0;
        }
        return agentBoundMapper.hardDeleteBinding(agentId, entityId, entityType.name());
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            agentBoundMapper.deleteById(id);
        }
    }

    // ==================== PO <-> Domain 转换 ====================

    private AgentBoundPO toPO(AgentBound domain) {
        if (domain == null) {
            return null;
        }
        AgentBoundPO po = new AgentBoundPO();
        po.setId(domain.getId());
        po.setAgentId(domain.getAgentId());
        po.setHierarchyLevel(domain.getHierarchyLevel() != null ? domain.getHierarchyLevel().name() : null);
        po.setEntityId(domain.getEntityId());
        po.setEntityType(domain.getEntityType() != null ? domain.getEntityType().name() : null);
        po.setCreatedAt(domain.getCreatedAt());
        po.setDeleted(domain.getDeleted() != null && domain.getDeleted() ? 1 : 0);
        return po;
    }

    private AgentBound toDomain(AgentBoundPO po) {
        if (po == null) {
            return null;
        }
        AgentBound domain = AgentBound.builder()
                .id(po.getId())
                .agentId(po.getAgentId())
                .hierarchyLevel(AgentHierarchyLevel.fromName(po.getHierarchyLevel()))
                .entityId(po.getEntityId())
                .entityType(BoundEntityType.fromName(po.getEntityType()))
                .createdAt(po.getCreatedAt())
                .deleted(po.getDeleted() != null && po.getDeleted() == 1)
                .build();

        // 设置派生字段
        domain.setDerivedFields(po.getAgentName(), po.getAgentRole(), po.getEntityName());

        return domain;
    }
}
