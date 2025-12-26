-- ==============================================
-- Resource 模型分离迁移脚本
-- Feature: 001-split-resource-model
-- 需求追溯: FR-001 ~ FR-014
-- Date: 2025-12-26
-- ==============================================

-- 0. 禁用外键检查（迁移期间需要）
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 创建 topology 表
CREATE TABLE topology (
    id BIGINT PRIMARY KEY COMMENT '主键ID（保留原 resource.id）',
    name VARCHAR(255) NOT NULL COMMENT '拓扑图名称',
    description TEXT COMMENT '拓扑图描述',
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING' COMMENT '状态：RUNNING, STOPPED, MAINTENANCE, OFFLINE',
    coordinator_agent_id BIGINT COMMENT '协调 Agent ID（预留字段）',
    attributes JSON COMMENT '扩展属性（JSON格式）',
    created_by BIGINT COMMENT '创建者ID',
    version INT DEFAULT 0 COMMENT '版本号（乐观锁）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC),
    INDEX idx_updated_at (updated_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拓扑图表';

-- 2. 迁移 SUBGRAPH 数据到 topology 表
INSERT INTO topology (id, name, description, status, attributes, created_by, version, created_at, updated_at)
SELECT r.id, r.name, r.description, r.status, r.attributes, r.created_by, r.version, r.created_at, r.updated_at
FROM resource r
JOIN resource_type rt ON r.resource_type_id = rt.id
WHERE rt.code = 'SUBGRAPH';

-- 3. 创建 node 表
CREATE TABLE node (
    id BIGINT PRIMARY KEY COMMENT '主键ID（保留原 resource.id）',
    name VARCHAR(255) NOT NULL COMMENT '节点名称',
    description TEXT COMMENT '节点描述',
    node_type_id BIGINT NOT NULL COMMENT '节点类型ID',
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING' COMMENT '状态：RUNNING, STOPPED, MAINTENANCE, OFFLINE',
    agent_team_id BIGINT COMMENT 'Agent Team ID（预留字段）',
    attributes JSON COMMENT '扩展属性（JSON格式）',
    created_by BIGINT COMMENT '创建者ID',
    version INT DEFAULT 0 COMMENT '版本号（乐观锁）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_type (node_type_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源节点表';

-- 4. 迁移非 SUBGRAPH 数据到 node 表
INSERT INTO node (id, name, description, node_type_id, status, attributes, created_by, version, created_at, updated_at)
SELECT r.id, r.name, r.description, r.resource_type_id, r.status, r.attributes, r.created_by, r.version, r.created_at, r.updated_at
FROM resource r
JOIN resource_type rt ON r.resource_type_id = rt.id
WHERE rt.code != 'SUBGRAPH';

-- 5. 重命名 resource_type 为 node_type
RENAME TABLE resource_type TO node_type;

-- 6. 删除 SUBGRAPH 类型记录
DELETE FROM node_type WHERE code = 'SUBGRAPH';

-- 7. 为 node 表添加外键约束
ALTER TABLE node ADD CONSTRAINT fk_node_type FOREIGN KEY (node_type_id) REFERENCES node_type(id);

-- 8. 添加唯一约束（同类型下名称唯一）
ALTER TABLE node ADD UNIQUE KEY uk_type_name (node_type_id, name);

-- 9. 创建 topology_2_node 关联表
CREATE TABLE topology_2_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    topology_id BIGINT NOT NULL COMMENT '拓扑图ID',
    node_id BIGINT NOT NULL COMMENT '节点ID',
    position_x INT COMMENT '节点在画布上的X坐标',
    position_y INT COMMENT '节点在画布上的Y坐标',
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    added_by BIGINT NOT NULL COMMENT '添加者用户ID',
    UNIQUE KEY uk_topology_node (topology_id, node_id),
    INDEX idx_topology_id (topology_id),
    INDEX idx_node_id (node_id),
    CONSTRAINT fk_t2n_topology FOREIGN KEY (topology_id) REFERENCES topology(id) ON DELETE CASCADE,
    CONSTRAINT fk_t2n_node FOREIGN KEY (node_id) REFERENCES node(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拓扑图-节点关联表';

-- 10. 从 subgraph_member 迁移数据到 topology_2_node
-- 注意：只迁移 node 类型的成员，不支持拓扑图嵌套
INSERT INTO topology_2_node (topology_id, node_id, added_at, added_by)
SELECT sm.subgraph_id, sm.member_id, sm.added_at, sm.added_by
FROM subgraph_member sm
JOIN node n ON sm.member_id = n.id;

-- 11. 创建 node_2_node 节点依赖关系表
CREATE TABLE node_2_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    source_id BIGINT NOT NULL COMMENT '源节点ID',
    target_id BIGINT NOT NULL COMMENT '目标节点ID',
    relationship_type VARCHAR(50) NOT NULL COMMENT '关系类型：DEPENDENCY, CALL, DEPLOYMENT, OWNERSHIP, ASSOCIATION',
    direction VARCHAR(20) NOT NULL COMMENT '关系方向：UNIDIRECTIONAL, BIDIRECTIONAL',
    strength VARCHAR(20) NOT NULL COMMENT '关系强度：STRONG, WEAK',
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '关系状态：NORMAL, ABNORMAL',
    description VARCHAR(500) COMMENT '关系描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_source_target_type (source_id, target_id, relationship_type),
    INDEX idx_source (source_id),
    INDEX idx_target (target_id),
    INDEX idx_type (relationship_type),
    INDEX idx_status (status),
    CONSTRAINT fk_n2n_source FOREIGN KEY (source_id) REFERENCES node(id) ON DELETE CASCADE,
    CONSTRAINT fk_n2n_target FOREIGN KEY (target_id) REFERENCES node(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节点依赖关系表';

-- 12. 从 resource_relationship 迁移数据到 node_2_node
INSERT INTO node_2_node (source_id, target_id, relationship_type, direction, strength, status, description, created_at, updated_at)
SELECT rr.source_resource_id, rr.target_resource_id, rr.relationship_type, rr.direction, rr.strength, rr.status, rr.description, rr.created_at, rr.updated_at
FROM resource_relationship rr
JOIN node n1 ON rr.source_resource_id = n1.id
JOIN node n2 ON rr.target_resource_id = n2.id;

-- 13. 删除旧表（先删除有外键依赖的表）
DROP TABLE IF EXISTS subgraph_member;
DROP TABLE IF EXISTS resource_relationship;
DROP TABLE IF EXISTS resource;

-- 14. 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;
