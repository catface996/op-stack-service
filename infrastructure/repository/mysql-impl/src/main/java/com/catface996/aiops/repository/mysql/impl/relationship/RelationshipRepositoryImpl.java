package com.catface996.aiops.repository.mysql.impl.relationship;

import com.catface996.aiops.domain.model.relationship.*;
import com.catface996.aiops.repository.relationship.RelationshipRepository;
import com.catface996.aiops.repository.mysql.mapper.relationship.RelationshipMapper;
import com.catface996.aiops.repository.mysql.po.relationship.RelationshipPO;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 资源关系仓储实现类
 *
 * <p>使用 MyBatis-Plus 实现资源关系数据访问</p>
 * <p>负责领域对象与持久化对象之间的转换</p>
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
@Repository
public class RelationshipRepositoryImpl implements RelationshipRepository {

    private final RelationshipMapper relationshipMapper;

    public RelationshipRepositoryImpl(RelationshipMapper relationshipMapper) {
        this.relationshipMapper = relationshipMapper;
    }

    @Override
    public Relationship save(Relationship relationship) {
        if (relationship == null) {
            throw new IllegalArgumentException("关系实体不能为null");
        }
        RelationshipPO po = toPO(relationship);
        relationshipMapper.insert(po);
        RelationshipPO savedPO = relationshipMapper.selectById(po.getId());
        return toEntity(savedPO);
    }

    @Override
    public Relationship update(Relationship relationship) {
        if (relationship == null) {
            throw new IllegalArgumentException("关系实体不能为null");
        }
        if (relationship.getId() == null) {
            throw new IllegalArgumentException("关系ID不能为null");
        }
        RelationshipPO po = toPO(relationship);
        relationshipMapper.updateById(po);
        RelationshipPO updatedPO = relationshipMapper.selectById(po.getId());
        return toEntity(updatedPO);
    }

    @Override
    public Optional<Relationship> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("关系ID不能为null");
        }
        RelationshipPO po = relationshipMapper.selectById(id);
        return Optional.ofNullable(toEntity(po));
    }

    @Override
    public List<Relationship> findBySourceResourceId(Long sourceResourceId) {
        if (sourceResourceId == null) {
            throw new IllegalArgumentException("源资源ID不能为null");
        }
        List<RelationshipPO> poList = relationshipMapper.selectBySourceResourceId(sourceResourceId);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Relationship> findByTargetResourceId(Long targetResourceId) {
        if (targetResourceId == null) {
            throw new IllegalArgumentException("目标资源ID不能为null");
        }
        List<RelationshipPO> poList = relationshipMapper.selectByTargetResourceId(targetResourceId);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Relationship> findByConditions(Long sourceResourceId, Long targetResourceId,
                                                RelationshipType type, RelationshipStatus status,
                                                int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        String typeStr = type != null ? type.name() : null;
        String statusStr = status != null ? status.name() : null;
        List<RelationshipPO> poList = relationshipMapper.selectByConditions(
                sourceResourceId, targetResourceId, typeStr, statusStr, offset, pageSize);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long countByConditions(Long sourceResourceId, Long targetResourceId,
                                   RelationshipType type, RelationshipStatus status) {
        String typeStr = type != null ? type.name() : null;
        String statusStr = status != null ? status.name() : null;
        return relationshipMapper.countByConditions(sourceResourceId, targetResourceId, typeStr, statusStr);
    }

    @Override
    public boolean existsBySourceAndTargetAndType(Long sourceResourceId, Long targetResourceId,
                                                   RelationshipType type) {
        if (sourceResourceId == null || targetResourceId == null || type == null) {
            return false;
        }
        return relationshipMapper.existsBySourceAndTargetAndType(
                sourceResourceId, targetResourceId, type.name()) > 0;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("关系ID不能为null");
        }
        relationshipMapper.deleteById(id);
    }

    @Override
    public void deleteBySourceAndTargetAndType(Long sourceResourceId, Long targetResourceId,
                                                RelationshipType type) {
        if (sourceResourceId == null || targetResourceId == null || type == null) {
            return;
        }
        relationshipMapper.deleteBySourceAndTargetAndType(
                sourceResourceId, targetResourceId, type.name());
    }

    @Override
    public void deleteByResourceId(Long resourceId) {
        if (resourceId == null) {
            return;
        }
        relationshipMapper.deleteByResourceId(resourceId);
    }

    @Override
    public long count() {
        return relationshipMapper.countByConditions(null, null, null, null);
    }

    @Override
    public List<Relationship> findBySourceResourceIds(List<Long> sourceResourceIds) {
        if (sourceResourceIds == null || sourceResourceIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<RelationshipPO> poList = relationshipMapper.selectBySourceResourceIds(sourceResourceIds);
        return poList.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * 将领域实体转换为持久化对象
     */
    private RelationshipPO toPO(Relationship entity) {
        if (entity == null) {
            return null;
        }
        RelationshipPO po = new RelationshipPO();
        po.setId(entity.getId());
        po.setSourceResourceId(entity.getSourceResourceId());
        po.setTargetResourceId(entity.getTargetResourceId());
        po.setRelationshipType(entity.getRelationshipType() != null ? entity.getRelationshipType().name() : null);
        po.setDirection(entity.getDirection() != null ? entity.getDirection().name() : null);
        po.setStrength(entity.getStrength() != null ? entity.getStrength().name() : null);
        po.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        po.setDescription(entity.getDescription());
        po.setCreatedAt(entity.getCreatedAt());
        po.setUpdatedAt(entity.getUpdatedAt());
        return po;
    }

    /**
     * 将持久化对象转换为领域实体
     */
    private Relationship toEntity(RelationshipPO po) {
        if (po == null) {
            return null;
        }
        Relationship entity = new Relationship();
        entity.setId(po.getId());
        entity.setSourceResourceId(po.getSourceResourceId());
        entity.setTargetResourceId(po.getTargetResourceId());
        entity.setRelationshipType(po.getRelationshipType() != null ?
                RelationshipType.valueOf(po.getRelationshipType()) : null);
        entity.setDirection(po.getDirection() != null ?
                RelationshipDirection.valueOf(po.getDirection()) : null);
        entity.setStrength(po.getStrength() != null ?
                RelationshipStrength.valueOf(po.getStrength()) : null);
        entity.setStatus(po.getStatus() != null ?
                RelationshipStatus.valueOf(po.getStatus()) : null);
        entity.setDescription(po.getDescription());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }
}
