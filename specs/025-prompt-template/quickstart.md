# Quickstart: 提示词模板管理

**Feature**: 025-prompt-template
**Date**: 2025-12-26

## Prerequisites

- Java 21 LTS
- Maven 3.8+
- MySQL 8.0+
- 项目已完成 `mvn clean package -DskipTests`

## Setup

### 1. 数据库迁移

启动应用时 Flyway 会自动执行迁移脚本 `V025__create_prompt_template_tables.sql`，创建以下表：

- `template_usage` - 模板用途表（预置 4 种用途）
- `prompt_template` - 提示词模板表
- `prompt_template_version` - 模板版本表

### 2. 启动应用

```bash
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

## API Quick Reference

所有接口使用 **POST** 方法，基础路径 `/api/v1`。

### 模板用途管理

| 操作 | 端点 | 描述 |
|------|------|------|
| 创建用途 | `/template-usages/create` | 自定义新的用途类型 |
| 查询用途列表 | `/template-usages/list` | 获取所有可用用途 |
| 删除用途 | `/template-usages/delete` | 删除未被使用的用途 |

### 提示词模板管理

| 操作 | 端点 | 描述 |
|------|------|------|
| 创建模板 | `/prompt-templates/create` | 创建新模板（版本 1） |
| 查询列表 | `/prompt-templates/list` | 分页查询，支持筛选 |
| 查看详情 | `/prompt-templates/detail` | 获取模板详情及版本历史 |
| 查看指定版本 | `/prompt-templates/version/detail` | 获取历史版本内容 |
| 更新模板 | `/prompt-templates/update` | 更新并生成新版本 |
| 回滚版本 | `/prompt-templates/rollback` | 回滚到历史版本 |
| 删除模板 | `/prompt-templates/delete` | 软删除模板 |

## Example Requests

### 1. 查询用途列表

```bash
curl -X POST http://localhost:8080/api/v1/template-usages/list \
  -H "Content-Type: application/json"
```

### 2. 创建提示词模板

```bash
curl -X POST http://localhost:8080/api/v1/prompt-templates/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "故障诊断通用模板",
    "usageId": 1,
    "content": "你是一个资深运维专家。请分析以下故障信息并给出诊断建议：\n\n{{fault_info}}\n\n请从以下几个方面进行分析：\n1. 可能的根因\n2. 影响范围\n3. 建议的处理步骤",
    "description": "用于通用故障诊断场景的提示词模板"
  }'
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "故障诊断通用模板",
    "usageId": 1,
    "usageName": "故障诊断",
    "currentVersion": 1,
    "content": "你是一个资深运维专家...",
    "createdAt": "2025-12-26T10:00:00"
  }
}
```

### 3. 查询模板列表

```bash
curl -X POST http://localhost:8080/api/v1/prompt-templates/list \
  -H "Content-Type: application/json" \
  -d '{
    "usageId": 1,
    "keyword": "故障",
    "pageNum": 1,
    "pageSize": 20
  }'
```

### 4. 更新模板（生成新版本）

```bash
curl -X POST http://localhost:8080/api/v1/prompt-templates/update \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": 1,
    "content": "你是一个资深运维专家。请分析以下故障信息...\n\n新增：4. 预防措施建议",
    "changeNote": "增加预防措施建议项"
  }'
```

### 5. 查看模板详情（含版本历史）

```bash
curl -X POST http://localhost:8080/api/v1/prompt-templates/detail \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": 1
  }'
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "template": {
      "id": 1,
      "name": "故障诊断通用模板",
      "currentVersion": 2
    },
    "versions": [
      {"versionNumber": 2, "changeNote": "增加预防措施建议项", "createdAt": "2025-12-26T11:00:00"},
      {"versionNumber": 1, "changeNote": null, "createdAt": "2025-12-26T10:00:00"}
    ]
  }
}
```

### 6. 回滚到历史版本

```bash
curl -X POST http://localhost:8080/api/v1/prompt-templates/rollback \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": 1,
    "targetVersionNumber": 1,
    "changeNote": "回滚到初始版本"
  }'
```

回滚后，`currentVersion` 变为 3（复制版本 1 的内容创建新版本）。

### 7. 删除模板

```bash
curl -X POST http://localhost:8080/api/v1/prompt-templates/delete \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": 1
  }'
```

## Error Codes

| 错误码 | 描述 |
|--------|------|
| `TEMPLATE_NAME_EXISTS` | 模板名称已存在 |
| `TEMPLATE_NOT_FOUND` | 模板不存在 |
| `TEMPLATE_CONTENT_EMPTY` | 模板内容不能为空 |
| `TEMPLATE_CONTENT_TOO_LARGE` | 模板内容超过 64KB 限制 |
| `TEMPLATE_CONTENT_UNCHANGED` | 内容无变化，无需更新 |
| `VERSION_CONFLICT` | 版本冲突，请刷新后重试 |
| `VERSION_NOT_FOUND` | 指定版本不存在 |
| `ALREADY_EARLIEST_VERSION` | 已是最早版本，无法回滚 |
| `USAGE_CODE_EXISTS` | 用途编码已存在 |
| `USAGE_IN_USE` | 用途正在被使用，无法删除 |

## Testing Checklist

- [ ] 创建模板成功，版本号为 1
- [ ] 名称重复时返回错误
- [ ] 列表查询支持分页和筛选
- [ ] 更新模板后版本号递增
- [ ] 内容无变化时拒绝更新
- [ ] 并发更新时返回版本冲突错误
- [ ] 回滚创建新版本而非修改指针
- [ ] 软删除后模板不可查询
- [ ] 自定义用途类型可创建和使用
