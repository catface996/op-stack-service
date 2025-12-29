package com.catface996.aiops.domain.impl.service.agentbound;

import com.catface996.aiops.domain.model.agent.AgentHierarchyLevel;
import com.catface996.aiops.domain.model.agentbound.AgentBound;
import com.catface996.aiops.domain.model.agentbound.BoundEntityType;
import com.catface996.aiops.domain.service.agentbound.AgentBoundDomainService;
import com.catface996.aiops.repository.agentbound.AgentBoundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Agent 绑定关系领域服务实现
 *
 * <p>实现 Agent 与实体绑定关系的核心业务逻辑。</p>
 *
 * @author AI Assistant
 * @since 2025-12-29
 */
@Service
public class AgentBoundDomainServiceImpl implements AgentBoundDomainService {

    private static final Logger log = LoggerFactory.getLogger(AgentBoundDomainServiceImpl.class);

    private final AgentBoundRepository agentBoundRepository;

    public AgentBoundDomainServiceImpl(AgentBoundRepository agentBoundRepository) {
        this.agentBoundRepository = agentBoundRepository;
    }

    @Override
    @Transactional
    public AgentBound bindAgent(Long agentId, AgentHierarchyLevel hierarchyLevel,
                                 Long entityId, BoundEntityType entityType) {
        // 1. 参数验证
        if (agentId == null || entityId == null) {
            throw new IllegalArgumentException("agentId 和 entityId 不能为空");
        }
        if (hierarchyLevel == null || entityType == null) {
            throw new IllegalArgumentException("hierarchyLevel 和 entityType 不能为空");
        }

        // 2. 验证实体类型与层级匹配（BR-001, BR-002）
        if (!entityType.supportsHierarchyLevel(hierarchyLevel)) {
            throw new IllegalArgumentException(
                    String.format("实体类型 %s 不支持层级 %s", entityType, hierarchyLevel));
        }

        // 3. 处理 Supervisor 绑定（BR-003）：替换已有绑定
        if (isSupervisorLevel(hierarchyLevel)) {
            Optional<AgentBound> existingBinding = agentBoundRepository.findSupervisorBinding(
                    entityType, entityId, hierarchyLevel);
            if (existingBinding.isPresent()) {
                AgentBound oldBinding = existingBinding.get();
                // 如果是同一个 Agent，直接返回（幂等）
                if (oldBinding.getAgentId().equals(agentId)) {
                    log.info("绑定已存在，幂等返回: agentId={}, entityType={}, entityId={}",
                            agentId, entityType, entityId);
                    return oldBinding;
                }
                // 删除旧绑定
                agentBoundRepository.deleteBinding(
                        oldBinding.getAgentId(), entityId, entityType);
                log.info("替换 Supervisor 绑定: 旧 agentId={}, 新 agentId={}, entityType={}, entityId={}",
                        oldBinding.getAgentId(), agentId, entityType, entityId);
            }
        } else {
            // 4. 处理 Worker 绑定（BR-004, BR-005）：允许多个，但不允许重复
            if (agentBoundRepository.existsBinding(agentId, entityId, entityType)) {
                log.info("Worker 绑定已存在，幂等返回: agentId={}, entityType={}, entityId={}",
                        agentId, entityType, entityId);
                // 返回已存在的绑定
                List<AgentBound> existing = agentBoundRepository.findByEntity(
                        entityType, entityId, hierarchyLevel);
                return existing.stream()
                        .filter(b -> b.getAgentId().equals(agentId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("绑定查询不一致"));
            }
        }

        // 5. 创建新绑定
        AgentBound agentBound = AgentBound.create(agentId, hierarchyLevel, entityId, entityType);
        AgentBound saved = agentBoundRepository.save(agentBound);

        log.info("Agent 绑定成功: agentId={}, hierarchyLevel={}, entityType={}, entityId={}",
                agentId, hierarchyLevel, entityType, entityId);

        return saved;
    }

    @Override
    public List<AgentBound> findByEntity(BoundEntityType entityType, Long entityId,
                                          AgentHierarchyLevel hierarchyLevel) {
        return agentBoundRepository.findByEntity(entityType, entityId, hierarchyLevel);
    }

    @Override
    public List<AgentBound> findByAgentId(Long agentId, BoundEntityType entityType) {
        return agentBoundRepository.findByAgentId(agentId, entityType);
    }

    @Override
    public List<AgentBound> queryHierarchyByTopology(Long topologyId) {
        if (topologyId == null) {
            throw new IllegalArgumentException("topologyId 不能为空");
        }
        return agentBoundRepository.findHierarchyByTopologyId(topologyId);
    }

    @Override
    @Transactional
    public int unbind(Long agentId, Long entityId, BoundEntityType entityType) {
        if (agentId == null || entityId == null || entityType == null) {
            throw new IllegalArgumentException("agentId、entityId 和 entityType 不能为空");
        }
        int count = agentBoundRepository.deleteBinding(agentId, entityId, entityType);
        log.info("Agent 解绑: agentId={}, entityType={}, entityId={}, 删除记录数={}",
                agentId, entityType, entityId, count);
        return count;
    }

    @Override
    public boolean existsBinding(Long agentId, Long entityId, BoundEntityType entityType) {
        return agentBoundRepository.existsBinding(agentId, entityId, entityType);
    }

    @Override
    public Optional<AgentBound> findSupervisorBinding(BoundEntityType entityType, Long entityId,
                                                       AgentHierarchyLevel hierarchyLevel) {
        return agentBoundRepository.findSupervisorBinding(entityType, entityId, hierarchyLevel);
    }

    /**
     * 判断是否为 Supervisor 层级
     */
    private boolean isSupervisorLevel(AgentHierarchyLevel level) {
        return level == AgentHierarchyLevel.GLOBAL_SUPERVISOR
                || level == AgentHierarchyLevel.TEAM_SUPERVISOR;
    }
}
