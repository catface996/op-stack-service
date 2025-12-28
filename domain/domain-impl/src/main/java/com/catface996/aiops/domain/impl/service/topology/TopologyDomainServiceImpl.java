package com.catface996.aiops.domain.impl.service.topology;

import com.catface996.aiops.common.enums.ResourceErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.topology.Topology;
import com.catface996.aiops.domain.model.topology.TopologyStatus;
import com.catface996.aiops.domain.service.topology.TopologyDomainService;
import com.catface996.aiops.repository.topology2.Topology2NodeRepository;
import com.catface996.aiops.repository.topology2.TopologyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 拓扑图领域服务实现
 *
 * <p>实现拓扑图管理的核心业务逻辑，协调 Repository 和基础设施服务。</p>
 *
 * <p>核心职责：</p>
 * <ul>
 *   <li>拓扑图 CRUD 操作</li>
 *   <li>审计日志记录</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>FR-001~008: 资源分类体系设计</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-25
 */
@Service
public class TopologyDomainServiceImpl implements TopologyDomainService {

    private static final Logger logger = LoggerFactory.getLogger(TopologyDomainServiceImpl.class);

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    private final TopologyRepository topologyRepository;
    private final Topology2NodeRepository topology2NodeRepository;

    public TopologyDomainServiceImpl(
            TopologyRepository topologyRepository,
            Topology2NodeRepository topology2NodeRepository) {
        this.topologyRepository = topologyRepository;
        this.topology2NodeRepository = topology2NodeRepository;
    }

    @Override
    @Transactional
    public Topology createTopology(String name, String description, Long operatorId, String operatorName) {
        logger.info("创建拓扑图，name: {}, operatorId: {}", name, operatorId);

        // 1. 参数验证
        validateTopologyName(name);
        validateDescription(description);

        // 2. 创建拓扑图实体
        Topology topology = Topology.create(
                name.trim(),
                description,
                null, // coordinatorAgentId
                "{}", // attributes
                operatorId
        );

        // 3. 保存拓扑图
        Topology saved = topologyRepository.save(topology);
        logger.info("拓扑图创建成功，id: {}, name: {}", saved.getId(), saved.getName());

        return saved;
    }

    @Override
    public List<Topology> listTopologies(String name, TopologyStatus status, int page, int size) {
        return topologyRepository.findByCondition(name, status, page, size);
    }

    @Override
    public long countTopologies(String name, TopologyStatus status) {
        return topologyRepository.countByCondition(name, status);
    }

    @Override
    public Optional<Topology> getTopologyById(Long topologyId) {
        return topologyRepository.findById(topologyId);
    }

    @Override
    @Transactional
    public Topology updateTopology(Long topologyId, String name, String description,
                                   Integer version, Long operatorId, String operatorName) {
        logger.info("更新拓扑图，topologyId: {}, operatorId: {}", topologyId, operatorId);

        // 1. 验证拓扑图存在
        Topology topology = getTopologyById(topologyId)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.RESOURCE_NOT_FOUND, "拓扑图不存在"));

        // 2. 验证版本号（乐观锁）
        if (!topology.getVersion().equals(version)) {
            throw new BusinessException(ResourceErrorCode.VERSION_CONFLICT, "数据已被修改，请刷新后重试");
        }

        // 3. 更新字段
        if (name != null) {
            validateTopologyName(name);
            topology.setName(name.trim());
        }
        if (description != null) {
            validateDescription(description);
            topology.setDescription(description);
        }
        topology.setUpdatedAt(LocalDateTime.now());
        topology.incrementVersion();

        // 4. 保存更新
        topologyRepository.update(topology);
        logger.info("拓扑图更新成功，id: {}", topologyId);

        return topology;
    }

    @Override
    @Transactional
    public void deleteTopology(Long topologyId, Long operatorId, String operatorName) {
        logger.info("删除拓扑图，topologyId: {}, operatorId: {}", topologyId, operatorId);

        // 1. 验证拓扑图存在
        Topology topology = getTopologyById(topologyId)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.RESOURCE_NOT_FOUND, "拓扑图不存在"));

        // 2. 解除与所有成员的关联关系
        topology2NodeRepository.removeAllByTopologyId(topologyId);
        logger.info("已解除拓扑图 {} 的所有成员关联", topologyId);

        // 3. 删除拓扑图
        topologyRepository.deleteById(topologyId);
        logger.info("拓扑图删除成功，id: {}", topologyId);
    }

    @Override
    public int countMembers(Long topologyId) {
        return topology2NodeRepository.countByTopologyId(topologyId);
    }

    @Override
    public Long getSubgraphTypeId() {
        // 不再需要，拓扑图已有独立表
        // 保留方法以保持接口兼容性，返回固定值
        return 0L;
    }

    // ===== 私有方法 =====

    private void validateTopologyName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("拓扑图名称不能为空");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("拓扑图名称过长，最多 " + MAX_NAME_LENGTH + " 字符");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("拓扑图描述过长，最多 " + MAX_DESCRIPTION_LENGTH + " 字符");
        }
    }
}
