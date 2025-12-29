package com.catface996.aiops.repository.mysql.impl.topology;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.catface996.aiops.domain.model.topology.Topology;
import com.catface996.aiops.domain.model.topology.TopologyStatus;
import com.catface996.aiops.repository.mysql.mapper.topology.TopologyMapper;
import com.catface996.aiops.repository.mysql.po.topology.TopologyPO;
import com.catface996.aiops.repository.topology2.TopologyRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 拓扑图仓储实现
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Repository
public class TopologyRepositoryImpl implements TopologyRepository {

    private final TopologyMapper topologyMapper;

    public TopologyRepositoryImpl(TopologyMapper topologyMapper) {
        this.topologyMapper = topologyMapper;
    }

    @Override
    public Optional<Topology> findById(Long id) {
        TopologyPO po = topologyMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<Topology> findByIdWithMemberCount(Long id) {
        TopologyPO po = topologyMapper.selectByIdWithMemberCount(id);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public Optional<Topology> findByName(String name) {
        TopologyPO po = topologyMapper.selectByName(name);
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<Topology> findByCondition(String name, TopologyStatus status, int page, int size) {
        Page<TopologyPO> pageParam = new Page<>(page, size);
        String statusStr = status != null ? status.name() : null;
        return topologyMapper.selectPageWithMemberCount(pageParam, name, statusStr)
                .getRecords()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByCondition(String name, TopologyStatus status) {
        LambdaQueryWrapper<TopologyPO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(TopologyPO::getName, name);
        }
        if (status != null) {
            wrapper.eq(TopologyPO::getStatus, status.name());
        }
        return topologyMapper.selectCount(wrapper);
    }

    @Override
    public Topology save(Topology topology) {
        TopologyPO po = toPO(topology);
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        topologyMapper.insert(po);
        topology.setId(po.getId());
        return topology;
    }

    @Override
    public boolean update(Topology topology) {
        TopologyPO po = toPO(topology);
        po.setUpdatedAt(LocalDateTime.now());
        int rows = topologyMapper.updateById(po);
        return rows > 0;
    }

    @Override
    public void deleteById(Long id) {
        topologyMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return topologyMapper.selectById(id) != null;
    }

    @Override
    public boolean existsByName(String name) {
        return topologyMapper.selectByName(name) != null;
    }

    @Override
    public boolean updateGlobalSupervisorAgentId(Long topologyId, Long agentId) {
        LambdaUpdateWrapper<TopologyPO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TopologyPO::getId, topologyId)
                .set(TopologyPO::getGlobalSupervisorAgentId, agentId)
                .set(TopologyPO::getUpdatedAt, LocalDateTime.now());

        int rows = topologyMapper.update(null, updateWrapper);
        return rows > 0;
    }

    // ==================== 转换方法 ====================

    private Topology toDomain(TopologyPO po) {
        if (po == null) {
            return null;
        }
        Topology topology = new Topology();
        topology.setId(po.getId());
        topology.setName(po.getName());
        topology.setDescription(po.getDescription());
        topology.setStatus(TopologyStatus.valueOf(po.getStatus()));
        topology.setCoordinatorAgentId(po.getCoordinatorAgentId());
        topology.setGlobalSupervisorAgentId(po.getGlobalSupervisorAgentId());
        topology.setAttributes(po.getAttributes());
        topology.setCreatedBy(po.getCreatedBy());
        topology.setVersion(po.getVersion());
        topology.setCreatedAt(po.getCreatedAt());
        topology.setUpdatedAt(po.getUpdatedAt());
        return topology;
    }

    private TopologyPO toPO(Topology domain) {
        if (domain == null) {
            return null;
        }
        TopologyPO po = new TopologyPO();
        po.setId(domain.getId());
        po.setName(domain.getName());
        po.setDescription(domain.getDescription());
        po.setStatus(domain.getStatus() != null ? domain.getStatus().name() : TopologyStatus.RUNNING.name());
        po.setCoordinatorAgentId(domain.getCoordinatorAgentId());
        po.setGlobalSupervisorAgentId(domain.getGlobalSupervisorAgentId());
        po.setAttributes(domain.getAttributes());
        po.setCreatedBy(domain.getCreatedBy());
        po.setVersion(domain.getVersion());
        po.setCreatedAt(domain.getCreatedAt());
        po.setUpdatedAt(domain.getUpdatedAt());
        return po;
    }
}
