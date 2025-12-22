package com.catface996.aiops.repository.mysql.impl.subgraph;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catface996.aiops.repository.mysql.mapper.subgraph.SubgraphMemberMapper;
import com.catface996.aiops.repository.mysql.po.subgraph.SubgraphMemberPO;
import com.catface996.aiops.repository.subgraph.SubgraphMemberRepository;
import com.catface996.aiops.repository.subgraph.entity.SubgraphMemberEntity;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 子图成员关联仓储实现类
 *
 * <p>使用 MyBatis-Plus 实现子图成员关联数据访问</p>
 * <p>v2.0 设计：子图作为资源类型，成员可以是任意资源（包括嵌套子图）</p>
 * <p>负责领域实体与持久化对象之间的转换</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能 v2.0</li>
 *   <li>需求5: 向子图添加成员资源</li>
 *   <li>需求6: 从子图移除成员资源</li>
 *   <li>需求8: 成员列表查询</li>
 *   <li>需求9: 拓扑数据查询</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-22
 */
@Repository
public class SubgraphMemberRepositoryImpl implements SubgraphMemberRepository {

    private final SubgraphMemberMapper memberMapper;

    public SubgraphMemberRepositoryImpl(SubgraphMemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    // ==================== 基本 CRUD 操作 ====================

    @Override
    public SubgraphMemberEntity save(SubgraphMemberEntity member) {
        if (member == null) {
            throw new IllegalArgumentException("成员关联实体不能为 null");
        }
        SubgraphMemberPO po = toPO(member);
        if (po.getAddedAt() == null) {
            po.setAddedAt(LocalDateTime.now());
        }
        memberMapper.insert(po);
        member.setId(po.getId());
        return member;
    }

    @Override
    public int batchSave(List<SubgraphMemberEntity> members) {
        if (members == null || members.isEmpty()) {
            return 0;
        }
        List<SubgraphMemberPO> poList = members.stream()
                .map(entity -> {
                    SubgraphMemberPO po = toPO(entity);
                    if (po.getAddedAt() == null) {
                        po.setAddedAt(LocalDateTime.now());
                    }
                    return po;
                })
                .collect(Collectors.toList());
        return memberMapper.batchInsert(poList);
    }

    @Override
    public Optional<SubgraphMemberEntity> findBySubgraphIdAndMemberId(Long subgraphId, Long memberId) {
        if (subgraphId == null || memberId == null) {
            return Optional.empty();
        }
        LambdaQueryWrapper<SubgraphMemberPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubgraphMemberPO::getSubgraphId, subgraphId)
               .eq(SubgraphMemberPO::getMemberId, memberId);
        SubgraphMemberPO po = memberMapper.selectOne(wrapper);
        return Optional.ofNullable(toEntity(po));
    }

    @Override
    public boolean deleteBySubgraphIdAndMemberId(Long subgraphId, Long memberId) {
        if (subgraphId == null || memberId == null) {
            return false;
        }
        return memberMapper.deleteBySubgraphIdAndMemberId(subgraphId, memberId) > 0;
    }

    @Override
    public int batchDelete(Long subgraphId, List<Long> memberIds) {
        if (subgraphId == null || memberIds == null || memberIds.isEmpty()) {
            return 0;
        }
        return memberMapper.batchDeleteBySubgraphIdAndMemberIds(subgraphId, memberIds);
    }

    @Override
    public int deleteAllBySubgraphId(Long subgraphId) {
        if (subgraphId == null) {
            return 0;
        }
        return memberMapper.deleteAllBySubgraphId(subgraphId);
    }

    // ==================== 查询操作 ====================

    @Override
    public List<SubgraphMemberEntity> findBySubgraphId(Long subgraphId) {
        if (subgraphId == null) {
            return Collections.emptyList();
        }
        List<SubgraphMemberPO> poList = memberMapper.selectBySubgraphIdWithDetails(subgraphId);
        return poList.stream()
                .map(this::toEntityWithDetails)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubgraphMemberEntity> findBySubgraphIdPaged(Long subgraphId, int offset, int limit) {
        if (subgraphId == null) {
            return Collections.emptyList();
        }
        List<SubgraphMemberPO> poList = memberMapper.selectBySubgraphIdPagedWithDetails(subgraphId, offset, limit);
        return poList.stream()
                .map(this::toEntityWithDetails)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findMemberIdsBySubgraphId(Long subgraphId) {
        if (subgraphId == null) {
            return Collections.emptyList();
        }
        return memberMapper.selectMemberIdsBySubgraphId(subgraphId);
    }

    @Override
    public int countBySubgraphId(Long subgraphId) {
        if (subgraphId == null) {
            return 0;
        }
        return memberMapper.countBySubgraphId(subgraphId);
    }

    // ==================== 反向查询（用于祖先查询和循环检测）====================

    @Override
    public List<Long> findSubgraphIdsByMemberId(Long memberId) {
        if (memberId == null) {
            return Collections.emptyList();
        }
        return memberMapper.selectSubgraphIdsByMemberId(memberId);
    }

    @Override
    public List<SubgraphMemberEntity> findByMemberId(Long memberId) {
        if (memberId == null) {
            return Collections.emptyList();
        }
        List<SubgraphMemberPO> poList = memberMapper.selectByMemberId(memberId);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    // ==================== 存在性检查 ====================

    @Override
    public boolean existsBySubgraphIdAndMemberId(Long subgraphId, Long memberId) {
        if (subgraphId == null || memberId == null) {
            return false;
        }
        return memberMapper.existsBySubgraphIdAndMemberId(subgraphId, memberId) > 0;
    }

    @Override
    public boolean hasMembers(Long subgraphId) {
        if (subgraphId == null) {
            return false;
        }
        return memberMapper.hasMembers(subgraphId) > 0;
    }

    // ==================== 类型检查 ====================

    @Override
    public boolean isSubgraphType(Long resourceId) {
        if (resourceId == null) {
            return false;
        }
        return memberMapper.isSubgraphType(resourceId) > 0;
    }

    @Override
    public List<Long> filterSubgraphTypeIds(List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return Collections.emptyList();
        }
        return memberMapper.filterSubgraphTypeIds(resourceIds);
    }

    // ==================== 对象转换方法 ====================

    /**
     * 将领域实体转换为持久化对象
     */
    private SubgraphMemberPO toPO(SubgraphMemberEntity entity) {
        if (entity == null) {
            return null;
        }
        SubgraphMemberPO po = new SubgraphMemberPO();
        po.setId(entity.getId());
        po.setSubgraphId(entity.getSubgraphId());
        po.setMemberId(entity.getMemberId());
        po.setAddedAt(entity.getAddedAt());
        po.setAddedBy(entity.getAddedBy());
        return po;
    }

    /**
     * 将持久化对象转换为领域实体（基础字段）
     */
    private SubgraphMemberEntity toEntity(SubgraphMemberPO po) {
        if (po == null) {
            return null;
        }
        SubgraphMemberEntity entity = new SubgraphMemberEntity();
        entity.setId(po.getId());
        entity.setSubgraphId(po.getSubgraphId());
        entity.setMemberId(po.getMemberId());
        entity.setAddedAt(po.getAddedAt());
        entity.setAddedBy(po.getAddedBy());
        return entity;
    }

    /**
     * 将持久化对象转换为领域实体（含成员详情）
     */
    private SubgraphMemberEntity toEntityWithDetails(SubgraphMemberPO po) {
        if (po == null) {
            return null;
        }
        SubgraphMemberEntity entity = toEntity(po);
        entity.setMemberName(po.getMemberName());
        entity.setMemberTypeCode(po.getMemberTypeCode());
        entity.setMemberStatus(po.getMemberStatus());
        entity.setIsSubgraph(po.getIsSubgraph());
        entity.setNestedMemberCount(po.getNestedMemberCount());
        return entity;
    }
}
