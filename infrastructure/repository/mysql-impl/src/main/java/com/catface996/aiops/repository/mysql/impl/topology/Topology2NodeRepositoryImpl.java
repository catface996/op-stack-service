package com.catface996.aiops.repository.mysql.impl.topology;

import com.catface996.aiops.repository.mysql.mapper.topology.Topology2NodeMapper;
import com.catface996.aiops.repository.mysql.po.topology.Topology2NodePO;
import com.catface996.aiops.repository.topology2.Topology2NodeRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拓扑图-节点关联仓储实现
 *
 * @author AI Assistant
 * @since 2025-12-26
 */
@Repository
public class Topology2NodeRepositoryImpl implements Topology2NodeRepository {

    private final Topology2NodeMapper topology2NodeMapper;

    public Topology2NodeRepositoryImpl(Topology2NodeMapper topology2NodeMapper) {
        this.topology2NodeMapper = topology2NodeMapper;
    }

    @Override
    public void addMember(Long topologyId, Long nodeId, Long addedBy) {
        Topology2NodePO po = new Topology2NodePO();
        po.setTopologyId(topologyId);
        po.setNodeId(nodeId);
        po.setAddedAt(LocalDateTime.now());
        po.setAddedBy(addedBy);
        topology2NodeMapper.insert(po);
    }

    @Override
    public void addMembers(Long topologyId, List<Long> nodeIds, Long addedBy) {
        for (Long nodeId : nodeIds) {
            if (!existsByTopologyIdAndNodeId(topologyId, nodeId)) {
                addMember(topologyId, nodeId, addedBy);
            }
        }
    }

    @Override
    public void removeMember(Long topologyId, Long nodeId) {
        topology2NodeMapper.deleteByTopologyIdAndNodeId(topologyId, nodeId);
    }

    @Override
    public void removeMembers(Long topologyId, List<Long> nodeIds) {
        for (Long nodeId : nodeIds) {
            removeMember(topologyId, nodeId);
        }
    }

    @Override
    public void removeAllByTopologyId(Long topologyId) {
        topology2NodeMapper.deleteByTopologyId(topologyId);
    }

    @Override
    public void removeAllByNodeId(Long nodeId) {
        topology2NodeMapper.deleteByNodeId(nodeId);
    }

    @Override
    public List<Long> findNodeIdsByTopologyId(Long topologyId) {
        return topology2NodeMapper.selectNodeIdsByTopologyId(topologyId);
    }

    @Override
    public boolean existsByTopologyIdAndNodeId(Long topologyId, Long nodeId) {
        return topology2NodeMapper.selectByTopologyIdAndNodeId(topologyId, nodeId) != null;
    }

    @Override
    public int countByTopologyId(Long topologyId) {
        return topology2NodeMapper.countByTopologyId(topologyId);
    }

    @Override
    public List<MemberInfo> findMembersByTopologyId(Long topologyId) {
        return topology2NodeMapper.selectMembersByTopologyId(topologyId)
                .stream()
                .map(po -> new MemberInfo(
                        po.getId(),
                        po.getTopologyId(),
                        po.getNodeId(),
                        po.getNodeName(),
                        po.getNodeTypeCode(),
                        po.getNodeTypeName(),
                        po.getNodeStatus(),
                        po.getNodeLayer(),
                        po.getPositionX(),
                        po.getPositionY(),
                        po.getAddedAt(),
                        po.getAddedBy()
                ))
                .collect(Collectors.toList());
    }
}
