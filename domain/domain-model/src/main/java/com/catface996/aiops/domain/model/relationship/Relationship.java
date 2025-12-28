package com.catface996.aiops.domain.model.relationship;

import java.time.LocalDateTime;

/**
 * 资源关系聚合根
 *
 * 表示两个IT资源之间的有向或双向关系，描述它们的交互或依赖关系
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
public class Relationship {

    /**
     * 关系ID
     */
    private Long id;

    /**
     * 源资源ID
     */
    private Long sourceResourceId;

    /**
     * 目标资源ID
     */
    private Long targetResourceId;

    /**
     * 关系类型
     */
    private RelationshipType relationshipType;

    /**
     * 关系方向
     */
    private RelationshipDirection direction;

    /**
     * 关系强度
     */
    private RelationshipStrength strength;

    /**
     * 关系状态
     */
    private RelationshipStatus status;

    /**
     * 关系描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    // 关联对象（用于展示，非持久化）
    /**
     * 源资源名称
     */
    private String sourceResourceName;

    /**
     * 目标资源名称
     */
    private String targetResourceName;

    // 构造函数
    public Relationship() {
        this.status = RelationshipStatus.NORMAL;
    }

    public Relationship(Long id, Long sourceResourceId, Long targetResourceId,
                        RelationshipType relationshipType, RelationshipDirection direction,
                        RelationshipStrength strength, RelationshipStatus status,
                        String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sourceResourceId = sourceResourceId;
        this.targetResourceId = targetResourceId;
        this.relationshipType = relationshipType;
        this.direction = direction;
        this.strength = strength;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 业务方法

    /**
     * 创建新关系的工厂方法
     */
    public static Relationship create(Long sourceResourceId, Long targetResourceId,
                                       RelationshipType type, RelationshipDirection direction,
                                       RelationshipStrength strength, String description) {
        Relationship relationship = new Relationship();
        relationship.setSourceResourceId(sourceResourceId);
        relationship.setTargetResourceId(targetResourceId);
        relationship.setRelationshipType(type);
        relationship.setDirection(direction);
        relationship.setStrength(strength);
        relationship.setDescription(description);
        relationship.setStatus(RelationshipStatus.NORMAL);
        relationship.setCreatedAt(LocalDateTime.now());
        relationship.setUpdatedAt(LocalDateTime.now());
        return relationship;
    }

    /**
     * 创建反向关系（用于双向关系）
     */
    public Relationship createReverseRelationship() {
        return Relationship.create(
            this.targetResourceId,
            this.sourceResourceId,
            this.relationshipType,
            this.direction,
            this.strength,
            this.description
        );
    }

    /**
     * 判断是否为双向关系
     */
    public boolean isBidirectional() {
        return direction != null && direction.isBidirectional();
    }

    /**
     * 判断用户是否有权限操作此关系
     * 用户必须是源节点或目标节点的Owner
     *
     * @param userId 用户ID
     * @param sourceCreatedBy 源节点创建者ID
     * @param targetCreatedBy 目标节点创建者ID
     * @return true if user is owner of source or target node
     */
    public boolean isOwner(Long userId, Long sourceCreatedBy, Long targetCreatedBy) {
        if (userId == null) {
            return false;
        }
        boolean isSourceOwner = sourceCreatedBy != null && sourceCreatedBy.equals(userId);
        boolean isTargetOwner = targetCreatedBy != null && targetCreatedBy.equals(userId);
        return isSourceOwner || isTargetOwner;
    }

    /**
     * 判断用户是否有权限查看此关系
     * 用户只需要对源节点或目标节点有访问权限即可
     *
     * @param userId 用户ID
     * @param sourceCreatedBy 源节点创建者ID
     * @param targetCreatedBy 目标节点创建者ID
     * @return true if user has access to source or target node
     */
    public boolean canView(Long userId, Long sourceCreatedBy, Long targetCreatedBy) {
        // 简化实现：只要用户是节点的创建者就可以查看
        return isOwner(userId, sourceCreatedBy, targetCreatedBy);
    }

    /**
     * 更新关系类型
     */
    public void updateType(RelationshipType newType) {
        if (newType != null) {
            this.relationshipType = newType;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 更新关系强度
     */
    public void updateStrength(RelationshipStrength newStrength) {
        if (newStrength != null) {
            this.strength = newStrength;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 更新关系状态
     */
    public void updateStatus(RelationshipStatus newStatus) {
        if (newStatus != null) {
            this.status = newStatus;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 更新关系描述
     */
    public void updateDescription(String newDescription) {
        this.description = newDescription;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新关系属性
     */
    public void update(RelationshipType type, RelationshipStrength strength,
                       RelationshipStatus status, String description) {
        if (type != null) {
            this.relationshipType = type;
        }
        if (strength != null) {
            this.strength = strength;
        }
        if (status != null) {
            this.status = status;
        }
        if (description != null) {
            this.description = description;
        }
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceResourceId() {
        return sourceResourceId;
    }

    public void setSourceResourceId(Long sourceResourceId) {
        this.sourceResourceId = sourceResourceId;
    }

    public Long getTargetResourceId() {
        return targetResourceId;
    }

    public void setTargetResourceId(Long targetResourceId) {
        this.targetResourceId = targetResourceId;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public RelationshipDirection getDirection() {
        return direction;
    }

    public void setDirection(RelationshipDirection direction) {
        this.direction = direction;
    }

    public RelationshipStrength getStrength() {
        return strength;
    }

    public void setStrength(RelationshipStrength strength) {
        this.strength = strength;
    }

    public RelationshipStatus getStatus() {
        return status;
    }

    public void setStatus(RelationshipStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSourceResourceName() {
        return sourceResourceName;
    }

    public void setSourceResourceName(String sourceResourceName) {
        this.sourceResourceName = sourceResourceName;
    }

    public String getTargetResourceName() {
        return targetResourceName;
    }

    public void setTargetResourceName(String targetResourceName) {
        this.targetResourceName = targetResourceName;
    }

    @Override
    public String toString() {
        return "Relationship{" +
                "id=" + id +
                ", sourceResourceId=" + sourceResourceId +
                ", targetResourceId=" + targetResourceId +
                ", relationshipType=" + relationshipType +
                ", direction=" + direction +
                ", strength=" + strength +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
