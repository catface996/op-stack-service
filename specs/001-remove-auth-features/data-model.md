# Data Model: 移除认证功能

**Feature**: 001-remove-auth-features
**Date**: 2025-12-25
**Type**: 删除型变更（无新增实体）

## 待移除的实体

### t_account（用户账号表）

**当前状态**: 存在
**目标状态**: 删除

```sql
-- 当前表结构
CREATE TABLE t_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '账号ID',
    username VARCHAR(20) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    password VARCHAR(60) NOT NULL COMMENT '加密后的密码(BCrypt)',
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER' COMMENT '角色: ROLE_USER, ROLE_ADMIN',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '账号状态: ACTIVE, LOCKED, DISABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);
```

**字段说明**:
| 字段 | 类型 | 说明 | 删除影响 |
|------|------|------|---------|
| id | BIGINT | 主键 | t_session 外键依赖 |
| username | VARCHAR(20) | 用户名，唯一 | 无 |
| email | VARCHAR(100) | 邮箱，唯一 | 无 |
| password | VARCHAR(60) | BCrypt 加密密码 | 无 |
| role | VARCHAR(20) | 用户角色 | 无 |
| status | VARCHAR(20) | 账号状态 | 无 |
| created_at | DATETIME | 创建时间 | 无 |
| updated_at | DATETIME | 更新时间 | 无 |

**依赖关系**:
- 被 `t_session.user_id` 外键引用（CASCADE DELETE）

---

### t_session（会话表）

**当前状态**: 存在
**目标状态**: 删除

```sql
-- 当前表结构
CREATE TABLE t_session (
    id VARCHAR(36) PRIMARY KEY COMMENT '会话ID (UUID)',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    token TEXT NOT NULL COMMENT 'JWT Token',
    expires_at DATETIME NOT NULL COMMENT '过期时间',
    device_info TEXT COMMENT '设备信息 (JSON格式)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_session_user_id FOREIGN KEY (user_id) REFERENCES t_account(id) ON DELETE CASCADE
);
```

**字段说明**:
| 字段 | 类型 | 说明 | 删除影响 |
|------|------|------|---------|
| id | VARCHAR(36) | UUID 主键 | 无 |
| user_id | BIGINT | 用户ID，外键 | 依赖 t_account |
| token | TEXT | JWT 令牌 | 无 |
| expires_at | DATETIME | 过期时间 | 无 |
| device_info | TEXT | 设备信息 JSON | 无 |
| created_at | DATETIME | 创建时间 | 无 |

**依赖关系**:
- 外键依赖 `t_account.id`

---

## 删除迁移脚本

### V10__Drop_auth_tables.sql

```sql
-- Drop authentication tables
-- This migration removes the account and session tables as authentication
-- has been moved to an external system.

-- Step 1: Drop t_session first (has foreign key to t_account)
DROP TABLE IF EXISTS t_session;

-- Step 2: Drop t_account
DROP TABLE IF EXISTS t_account;
```

**执行顺序说明**:
1. 必须先删除 `t_session`，因为它有外键引用 `t_account`
2. 然后删除 `t_account`

---

## 对应的 Java 实体类

### AccountPO.java（待删除）

**路径**: `infrastructure/repository/mysql-impl/src/main/java/.../po/auth/AccountPO.java`

```java
@TableName("t_account")
public class AccountPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String email;
    private String password;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### SessionPO.java（待删除）

**路径**: `infrastructure/repository/mysql-impl/src/main/java/.../po/auth/SessionPO.java`

```java
@TableName("t_session")
public class SessionPO {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private Long userId;
    private String token;
    private LocalDateTime expiresAt;
    private String deviceInfo;
    private LocalDateTime createdAt;
}
```

---

## 数据处理策略

**决策**: 直接删除，无需备份

**原因**:
- 认证功能已迁移到外部系统
- 用户数据已不再需要或已在外部系统重建
- 用户确认数据可直接删除

---

## 验证清单

- [ ] 确认没有其他表引用 t_account 或 t_session
- [ ] 确认没有业务代码依赖这些表的数据
- [ ] 执行迁移脚本后验证表已删除
- [ ] 验证系统正常启动
