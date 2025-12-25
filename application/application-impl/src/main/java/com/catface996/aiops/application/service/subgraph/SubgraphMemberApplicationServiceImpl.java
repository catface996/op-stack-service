package com.catface996.aiops.application.service.subgraph;

import com.catface996.aiops.application.dto.subgraph.AddMembersCommand;
import com.catface996.aiops.application.dto.subgraph.SubgraphAncestorsDTO;
import com.catface996.aiops.application.dto.subgraph.SubgraphMemberDTO;
import com.catface996.aiops.application.dto.subgraph.SubgraphMembersWithRelationsDTO;
import com.catface996.aiops.application.dto.subgraph.TopologyGraphDTO;
import com.catface996.aiops.application.dto.subgraph.TopologyQueryCommand;
import com.catface996.aiops.domain.model.subgraph.*;
import com.catface996.aiops.domain.service.subgraph.SubgraphMemberDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 子图成员应用服务实现类
 *
 * <p>v2.0 设计：子图作为资源类型，成员可以是任意资源（包括嵌套子图）</p>
 *
 * <p>职责：</p>
 * <ul>
 *   <li>协调领域服务完成业务操作</li>
 *   <li>DTO 与领域模型之间的转换</li>
 *   <li>事务边界管理</li>
 * </ul>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求5: 向子图添加成员资源</li>
 *   <li>需求6: 从子图移除成员资源</li>
 *   <li>需求8: 成员列表查询</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@Service
public class SubgraphMemberApplicationServiceImpl implements SubgraphMemberApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(SubgraphMemberApplicationServiceImpl.class);

    private final SubgraphMemberDomainService memberDomainService;

    public SubgraphMemberApplicationServiceImpl(SubgraphMemberDomainService memberDomainService) {
        this.memberDomainService = memberDomainService;
    }

    // ==================== 成员添加（US5）====================

    @Override
    @Transactional
    public int addMembers(AddMembersCommand command) {
        logger.info("添加成员到子图，command: {}", command);

        // 参数验证
        if (command == null) {
            throw new IllegalArgumentException("命令不能为空");
        }
        if (command.getSubgraphId() == null) {
            throw new IllegalArgumentException("子图 ID 不能为空");
        }
        if (command.getMemberIds() == null || command.getMemberIds().isEmpty()) {
            return 0;
        }
        if (command.getOperatorId() == null) {
            throw new IllegalArgumentException("操作者 ID 不能为空");
        }

        // TODO: 权限检查 - 验证用户是子图的 Owner
        // 由于子图现在是资源类型，需要通过 Resource API 检查权限
        // 暂时跳过，后续集成 ResourcePermissionService

        // 调用领域服务
        int addedCount = memberDomainService.addMembers(
                command.getSubgraphId(),
                command.getMemberIds(),
                command.getOperatorId()
        );

        logger.info("成功添加 {} 个成员到子图 {}", addedCount, command.getSubgraphId());
        return addedCount;
    }

    // ==================== 成员移除（US6）====================

    @Override
    @Transactional
    public int removeMembers(Long subgraphId, List<Long> memberIds, Long operatorId) {
        logger.info("从子图移除成员，subgraphId: {}, memberCount: {}, operatorId: {}",
                subgraphId, memberIds != null ? memberIds.size() : 0, operatorId);

        if (subgraphId == null || memberIds == null || memberIds.isEmpty()) {
            return 0;
        }

        // TODO: 权限检查

        int removedCount = memberDomainService.removeMembers(subgraphId, memberIds, operatorId);
        logger.info("成功从子图 {} 移除 {} 个成员", subgraphId, removedCount);
        return removedCount;
    }

    // ==================== 成员查询（US8）====================

    @Override
    public List<SubgraphMemberDTO> listMembers(Long subgraphId, int page, int size) {
        if (subgraphId == null) {
            return Collections.emptyList();
        }

        List<SubgraphMember> members = memberDomainService.getMembersBySubgraphIdPaged(subgraphId, page, size);
        return members.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public int countMembers(Long subgraphId) {
        if (subgraphId == null) {
            return 0;
        }
        return memberDomainService.countMembers(subgraphId);
    }

    // ==================== 拓扑查询（US9）====================

    @Override
    public SubgraphMembersWithRelationsDTO getMembersWithRelations(TopologyQueryCommand command) {
        logger.info("获取子图成员及关系，command: {}", command);

        if (command == null || command.getSubgraphId() == null) {
            throw new IllegalArgumentException("查询命令不能为空");
        }

        SubgraphMemberDomainService.SubgraphMembersWithRelations result =
                memberDomainService.getMembersWithRelations(
                        command.getSubgraphId(),
                        command.isExpandNested(),
                        command.getMaxDepth()
                );

        return toMembersWithRelationsDTO(result);
    }

    @Override
    public TopologyGraphDTO getSubgraphTopology(TopologyQueryCommand command) {
        logger.info("获取子图拓扑图，command: {}", command);

        if (command == null || command.getSubgraphId() == null) {
            throw new IllegalArgumentException("查询命令不能为空");
        }

        SubgraphTopologyResult result = memberDomainService.getSubgraphTopology(
                command.getSubgraphId(),
                command.isExpandNested()
        );

        return toTopologyGraphDTO(result);
    }

    // ==================== 祖先查询（US7）====================

    @Override
    public SubgraphAncestorsDTO getAncestors(Long subgraphId) {
        logger.info("获取子图祖先链，subgraphId: {}", subgraphId);

        if (subgraphId == null) {
            throw new IllegalArgumentException("子图 ID 不能为空");
        }

        List<AncestorInfo> ancestors = memberDomainService.getAncestors(subgraphId);
        return toAncestorsDTO(subgraphId, ancestors);
    }

    // ==================== 子图空检查（Phase 8）====================

    @Override
    public boolean isSubgraphEmpty(Long subgraphId) {
        return memberDomainService.isSubgraphEmpty(subgraphId);
    }

    // ==================== 对象转换 ====================

    /**
     * 领域模型转 DTO
     */
    private SubgraphMemberDTO toDTO(SubgraphMember member) {
        if (member == null) {
            return null;
        }
        SubgraphMemberDTO dto = new SubgraphMemberDTO();
        dto.setId(member.getId());
        dto.setMemberId(member.getMemberId());
        dto.setSubgraphId(member.getSubgraphId());
        dto.setMemberName(member.getMemberName());
        dto.setMemberType(member.getMemberTypeCode());
        dto.setMemberStatus(member.getMemberStatus());
        dto.setIsSubgraph(member.isSubgraph());
        dto.setNestedMemberCount(member.getNestedMemberCount());
        dto.setAddedAt(member.getAddedAt());
        dto.setAddedBy(member.getAddedBy());
        return dto;
    }

    /**
     * 领域模型转成员和关系 DTO
     */
    private SubgraphMembersWithRelationsDTO toMembersWithRelationsDTO(
            SubgraphMemberDomainService.SubgraphMembersWithRelations result) {
        if (result == null) {
            return null;
        }
        SubgraphMembersWithRelationsDTO dto = new SubgraphMembersWithRelationsDTO();
        dto.setSubgraphId(result.getSubgraphId());
        dto.setSubgraphName(result.getSubgraphName());
        dto.setNodeCount(result.getNodeCount());
        dto.setEdgeCount(result.getEdgeCount());
        dto.setMaxDepth(result.getMaxDepth());

        // 转换成员列表
        if (result.getMembers() != null) {
            dto.setMembers(result.getMembers().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList()));
        }

        // 转换关系列表
        if (result.getRelationships() != null) {
            dto.setRelationships(result.getRelationships().stream()
                    .map(this::toRelationshipDTO)
                    .collect(Collectors.toList()));
        }

        // 转换嵌套子图列表
        if (result.getNestedSubgraphs() != null) {
            dto.setNestedSubgraphs(result.getNestedSubgraphs().stream()
                    .map(this::toNestedSubgraphDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * 领域模型转拓扑图 DTO
     */
    private TopologyGraphDTO toTopologyGraphDTO(SubgraphTopologyResult result) {
        if (result == null) {
            return null;
        }
        TopologyGraphDTO dto = new TopologyGraphDTO();

        // 转换节点列表
        if (result.getNodes() != null) {
            dto.setNodes(result.getNodes().stream()
                    .map(this::toTopologyNodeDTO)
                    .collect(Collectors.toList()));
        }

        // 转换边列表
        if (result.getEdges() != null) {
            dto.setEdges(result.getEdges().stream()
                    .map(this::toTopologyEdgeDTO)
                    .collect(Collectors.toList()));
        }

        // 转换子图边界列表
        if (result.getSubgraphBoundaries() != null) {
            dto.setSubgraphBoundaries(result.getSubgraphBoundaries().stream()
                    .map(this::toSubgraphBoundaryDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * 边转关系 DTO
     */
    private SubgraphMembersWithRelationsDTO.RelationshipDTO toRelationshipDTO(TopologyEdge edge) {
        if (edge == null) {
            return null;
        }
        SubgraphMembersWithRelationsDTO.RelationshipDTO dto = new SubgraphMembersWithRelationsDTO.RelationshipDTO();
        dto.setSourceId(edge.getSource());
        dto.setTargetId(edge.getTarget());
        dto.setRelationshipType(edge.getType());
        dto.setDirection(edge.getDirection());
        dto.setStrength(edge.getStrength());
        dto.setStatus(edge.getStatus());
        return dto;
    }

    /**
     * 嵌套子图信息转 DTO
     */
    private SubgraphMembersWithRelationsDTO.NestedSubgraphDTO toNestedSubgraphDTO(NestedSubgraphInfo info) {
        if (info == null) {
            return null;
        }
        SubgraphMembersWithRelationsDTO.NestedSubgraphDTO dto = new SubgraphMembersWithRelationsDTO.NestedSubgraphDTO();
        dto.setSubgraphId(info.getSubgraphId());
        dto.setName(info.getSubgraphName());
        dto.setParentSubgraphId(info.getParentSubgraphId());
        dto.setDepth(info.getDepth());
        dto.setMemberCount(info.getMemberCount());
        dto.setExpanded(info.isExpanded());
        return dto;
    }

    /**
     * 拓扑节点转 DTO
     */
    private TopologyGraphDTO.TopologyNodeDTO toTopologyNodeDTO(TopologyNode node) {
        if (node == null) {
            return null;
        }
        TopologyGraphDTO.TopologyNodeDTO dto = new TopologyGraphDTO.TopologyNodeDTO();
        dto.setId(node.getId());
        dto.setName(node.getName());
        dto.setTypeCode(node.getType());
        dto.setStatus(node.getStatus());
        dto.setSubgraph(node.isSubgraph());
        dto.setExpanded(node.isExpanded());
        dto.setParentSubgraphId(node.getParentSubgraphId());
        return dto;
    }

    /**
     * 拓扑边转 DTO
     */
    private TopologyGraphDTO.TopologyEdgeDTO toTopologyEdgeDTO(TopologyEdge edge) {
        if (edge == null) {
            return null;
        }
        TopologyGraphDTO.TopologyEdgeDTO dto = new TopologyGraphDTO.TopologyEdgeDTO();
        dto.setSourceId(edge.getSource());
        dto.setTargetId(edge.getTarget());
        dto.setRelationshipType(edge.getType());
        dto.setDirection(edge.getDirection());
        dto.setStrength(edge.getStrength());
        dto.setStatus(edge.getStatus());
        return dto;
    }

    /**
     * 子图边界转 DTO
     */
    private TopologyGraphDTO.SubgraphBoundaryDTO toSubgraphBoundaryDTO(SubgraphBoundary boundary) {
        if (boundary == null) {
            return null;
        }
        return new TopologyGraphDTO.SubgraphBoundaryDTO(
                boundary.getSubgraphId(),
                boundary.getSubgraphName(),
                boundary.getMemberIds()
        );
    }

    /**
     * 祖先列表转 DTO
     */
    private SubgraphAncestorsDTO toAncestorsDTO(Long subgraphId, List<AncestorInfo> ancestors) {
        SubgraphAncestorsDTO dto = new SubgraphAncestorsDTO();
        dto.setSubgraphId(subgraphId);

        if (ancestors != null && !ancestors.isEmpty()) {
            List<SubgraphAncestorsDTO.AncestorDTO> ancestorDTOs = ancestors.stream()
                    .map(info -> new SubgraphAncestorsDTO.AncestorDTO(
                            info.getSubgraphId(),
                            info.getSubgraphName(),
                            info.getDepth()))
                    .collect(Collectors.toList());
            dto.setAncestors(ancestorDTOs);
            dto.setMaxDepth(ancestors.stream().mapToInt(AncestorInfo::getDepth).max().orElse(0));
        }

        return dto;
    }
}
