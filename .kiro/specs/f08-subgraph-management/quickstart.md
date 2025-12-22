# Quickstart Guide - Subgraph Management v2.0

**Feature**: F08 - 子图管理
**Date**: 2025-12-22

## Overview

本指南介绍如何快速开始子图管理功能的 v2.0 实现。

## Prerequisites

1. Java 21 (LTS)
2. Maven 3.8+
3. MySQL 8.0+
4. Redis 7.0+
5. 已实现的功能: F01 (认证), F03 (资源管理), F04 (拓扑关系)

## Quick Start Steps

### 1. 数据库迁移

创建新的 Flyway 迁移文件:

```sql
-- V7__Add_subgraph_resource_type.sql

-- 添加 SUBGRAPH 资源类型
INSERT INTO resource_type (code, name, description, icon, is_system, created_at, updated_at)
VALUES ('SUBGRAPH', '子图', '资源分组容器，支持嵌套', 'folder-tree', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 创建 subgraph_member 表
CREATE TABLE IF NOT EXISTS subgraph_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    subgraph_id BIGINT NOT NULL COMMENT '父子图资源ID',
    member_id BIGINT NOT NULL COMMENT '成员资源ID（可以是任意类型包括SUBGRAPH）',
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    added_by BIGINT NOT NULL COMMENT '添加者用户ID',

    UNIQUE KEY uk_subgraph_member (subgraph_id, member_id),
    INDEX idx_subgraph_id (subgraph_id),
    INDEX idx_member_id (member_id),

    CONSTRAINT fk_subgraph_member_subgraph
        FOREIGN KEY (subgraph_id) REFERENCES resource(id) ON DELETE CASCADE,
    CONSTRAINT fk_subgraph_member_member
        FOREIGN KEY (member_id) REFERENCES resource(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='子图成员关联表';
```

### 2. 创建子图（使用 Resource API）

```bash
# 创建子图
curl -X POST http://localhost:8080/api/v1/resources \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "resourceTypeCode": "SUBGRAPH",
    "name": "核心服务子图",
    "description": "包含核心业务服务的资源组",
    "tags": ["core", "production"],
    "metadata": {
      "team": "platform",
      "priority": "high"
    }
  }'
```

### 3. 添加成员到子图

```bash
# 添加资源作为成员
curl -X POST http://localhost:8080/api/v1/subgraphs/1/members \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "memberIds": [10, 11, 12]
  }'
```

### 4. 添加嵌套子图

```bash
# 创建另一个子图
curl -X POST http://localhost:8080/api/v1/resources \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "resourceTypeCode": "SUBGRAPH",
    "name": "数据库服务子图",
    "description": "数据库相关服务",
    "tags": ["database"]
  }'

# 将数据库子图添加到核心服务子图（嵌套）
curl -X POST http://localhost:8080/api/v1/subgraphs/1/members \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "memberIds": [2]
  }'
```

### 5. 查询子图成员

```bash
# 分页查询成员列表
curl "http://localhost:8080/api/v1/subgraphs/1/members?page=1&size=20" \
  -H "Authorization: Bearer {token}"

# 获取成员及关系（用于拓扑图）
curl "http://localhost:8080/api/v1/subgraphs/1/members-with-relations?expandNested=true&maxDepth=3" \
  -H "Authorization: Bearer {token}"
```

### 6. 获取拓扑数据

```bash
# 获取拓扑图数据
curl "http://localhost:8080/api/v1/subgraphs/1/topology?expandNested=false" \
  -H "Authorization: Bearer {token}"
```

## Key Implementation Files

### Domain Layer

```
domain/
├── domain-api/
│   └── src/main/java/com/catface996/aiops/domain/
│       ├── model/subgraph/
│       │   └── SubgraphMember.java          # 新实体
│       └── service/subgraph/
│           └── SubgraphMemberDomainService.java  # 新领域服务
├── domain-impl/
│   └── src/main/java/com/catface996/aiops/domain/
│       └── service/subgraph/
│           └── SubgraphMemberDomainServiceImpl.java
└── repository-api/
    └── src/main/java/com/catface996/aiops/repository/
        └── subgraph/
            └── SubgraphMemberRepository.java  # 新仓储接口
```

### Application Layer

```
application/
├── application-api/
│   └── src/main/java/com/catface996/aiops/application/
│       ├── dto/subgraph/
│       │   ├── AddMembersCommand.java
│       │   ├── RemoveMembersCommand.java
│       │   └── SubgraphMemberDTO.java
│       └── service/subgraph/
│           └── SubgraphMemberApplicationService.java
└── application-impl/
    └── src/main/java/com/catface996/aiops/application/
        └── service/subgraph/
            └── SubgraphMemberApplicationServiceImpl.java
```

### Interface Layer

```
interface/
└── interface-http/
    └── src/main/java/com/catface996/aiops/http/
        ├── controller/
        │   └── SubgraphMemberController.java  # 新控制器
        ├── request/subgraph/
        │   ├── AddMembersRequest.java
        │   └── RemoveMembersRequest.java
        └── response/subgraph/
            ├── SubgraphMemberListResponse.java
            └── TopologyGraphResponse.java
```

### Infrastructure Layer

```
infrastructure/
└── repository/
    └── mysql-impl/
        └── src/main/
            ├── java/com/catface996/aiops/repository/
            │   └── subgraph/
            │       ├── SubgraphMemberRepositoryImpl.java
            │       ├── mapper/
            │       │   └── SubgraphMemberMapper.java
            │       └── po/
            │           └── SubgraphMemberPO.java
            └── resources/mapper/
                └── SubgraphMemberMapper.xml
```

## Cycle Detection Algorithm

添加成员时的循环检测实现:

```java
public boolean wouldCreateCycle(Long subgraphId, Long candidateMemberId) {
    // 如果候选成员不是子图类型，不会产生循环
    if (!isSubgraph(candidateMemberId)) {
        return false;
    }

    // 获取当前子图的所有祖先
    Set<Long> ancestors = getAllAncestors(subgraphId);

    // 如果候选成员是当前子图的祖先，则会形成循环
    if (ancestors.contains(candidateMemberId)) {
        return true;
    }

    // 如果候选成员就是当前子图本身
    if (candidateMemberId.equals(subgraphId)) {
        return true;
    }

    return false;
}

private Set<Long> getAllAncestors(Long subgraphId) {
    Set<Long> ancestors = new HashSet<>();
    Queue<Long> queue = new LinkedList<>();

    // 查找直接父级
    List<Long> parents = subgraphMemberRepository.findSubgraphIdsByMemberId(subgraphId);
    queue.addAll(parents);

    while (!queue.isEmpty()) {
        Long current = queue.poll();
        if (!ancestors.contains(current)) {
            ancestors.add(current);
            List<Long> grandparents = subgraphMemberRepository.findSubgraphIdsByMemberId(current);
            queue.addAll(grandparents);
        }
    }

    return ancestors;
}
```

## Testing

### Unit Test Example

```java
@Test
void shouldDetectCircularReference() {
    // Given: A -> B -> C (nested subgraphs)
    Long subgraphA = createSubgraph("A");
    Long subgraphB = createSubgraph("B");
    Long subgraphC = createSubgraph("C");

    addMember(subgraphA, subgraphB);  // A contains B
    addMember(subgraphB, subgraphC);  // B contains C

    // When: Try to add A to C (would create cycle: C -> A -> B -> C)
    // Then: Should throw CircularReferenceException
    assertThrows(CircularReferenceException.class,
        () -> addMember(subgraphC, subgraphA));
}
```

### Integration Test

```java
@SpringBootTest
@Testcontainers
class SubgraphMemberIntegrationTest {

    @Test
    void shouldAddAndQueryMembers() {
        // Create subgraph via Resource API
        Long subgraphId = createSubgraph("Test Subgraph");

        // Add members
        addMembers(subgraphId, List.of(resourceId1, resourceId2));

        // Query members
        var members = getMembersWithRelations(subgraphId);

        assertThat(members.getMembers()).hasSize(2);
    }
}
```

## Common Issues

### 1. Circular Reference Error

**Error**: `CIRCULAR_REFERENCE_DETECTED`

**Cause**: Attempting to add a subgraph that would create a cycle.

**Solution**: Check the ancestor chain before adding nested subgraphs.

### 2. Member Already Exists

**Error**: `MEMBER_ALREADY_EXISTS`

**Cause**: Trying to add a resource that is already a member.

**Solution**: Check membership before adding.

### 3. Subgraph Not Empty

**Error**: `SUBGRAPH_NOT_EMPTY`

**Cause**: Trying to delete a subgraph that still has members.

**Solution**: Remove all members first, then delete the subgraph.

---

**Document Version**: 1.0
**Last Updated**: 2025-12-22
