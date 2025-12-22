-- F08 子图管理 v2.0 - 子图成员表
-- 需求追溯: F08 需求规格 v2.0, 子图作为资源类型管理
-- 本迁移添加 SUBGRAPH 资源类型和 subgraph_member 表用于成员关联

-- ==============================================
-- 1. 添加 SUBGRAPH 资源类型
-- ==============================================
-- 子图作为资源类型之一，存储在 resource 表中
-- 支持嵌套子图（子图可以包含其他子图作为成员）
INSERT INTO resource_type (code, name, description, icon, is_system, created_by)
VALUES ('SUBGRAPH', '子图', '资源分组容器，支持嵌套', '/icons/folder-tree.svg', TRUE, 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    icon = VALUES(icon),
    updated_at = NOW();

-- ==============================================
-- 2. 创建 subgraph_member 表
-- ==============================================
-- 子图成员关联表，替代原 subgraph_resource 表
-- 支持将任意资源（包括子图）添加为成员
-- 成员可以是任何资源类型，包括 SUBGRAPH（实现嵌套子图）
CREATE TABLE IF NOT EXISTS subgraph_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID（主键）',
    subgraph_id BIGINT NOT NULL COMMENT '父子图资源ID（必须是 SUBGRAPH 类型的 resource）',
    member_id BIGINT NOT NULL COMMENT '成员资源ID（可以是任意类型包括 SUBGRAPH）',
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    added_by BIGINT NOT NULL COMMENT '添加者用户ID',

    -- 复合唯一索引：防止重复添加
    UNIQUE KEY uk_subgraph_member (subgraph_id, member_id) COMMENT '每个成员在子图中唯一',

    -- 单列索引：优化查询
    INDEX idx_subgraph_id (subgraph_id) COMMENT '按子图ID查询成员',
    INDEX idx_member_id (member_id) COMMENT '按成员ID查询所属子图（用于祖先查询和循环检测）',
    INDEX idx_added_at (added_at DESC) COMMENT '按添加时间排序',

    -- 外键约束：保证数据完整性
    CONSTRAINT fk_subgraph_member_subgraph
        FOREIGN KEY (subgraph_id) REFERENCES resource(id) ON DELETE CASCADE,
    CONSTRAINT fk_subgraph_member_member
        FOREIGN KEY (member_id) REFERENCES resource(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='子图成员关联表 v2.0';

-- ==============================================
-- 3. 注释说明
-- ==============================================
-- v2.0 设计变更：
-- 1. 子图不再使用独立的 subgraph 表，而是作为 resource 的一种类型
-- 2. 子图权限复用 resource_permission 表（如果存在）
-- 3. subgraph_member 替代 subgraph_resource，支持嵌套子图
-- 4. 循环检测在应用层实现，使用祖先链查询 (member_id 索引)
--
-- 约束说明：
-- - 最大嵌套深度：10 层（应用层校验）
-- - 每子图最大成员数：500（应用层校验）
-- - 禁止循环引用（应用层 DFS 检测）
-- - 删除子图前必须移除所有成员（应用层校验）
