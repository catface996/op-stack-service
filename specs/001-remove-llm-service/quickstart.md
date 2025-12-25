# Quickstart: 移除LLM服务管理功能

**Feature**: 001-remove-llm-service
**Date**: 2025-12-25

## Prerequisites

- Java 21
- Maven 3.8+
- 本地 MySQL 数据库（可选，用于验证迁移脚本）

## Implementation Steps

### Step 1: 验证无外部依赖

```bash
# 搜索 LLM 相关引用
grep -r "LlmService\|llm_service" --include="*.java" --include="*.xml" --include="*.yml" .

# 预期结果：仅返回 LLM 模块内部的 20 个文件
```

### Step 2: 删除 Interface 层代码

```bash
# 删除控制器
rm interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/controller/LlmServiceController.java

# 删除请求对象目录
rm -rf interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/request/llm/
```

### Step 3: 删除 Application 层代码

```bash
# 删除 DTO 目录
rm -rf application/application-api/src/main/java/com/catface996/aiops/application/api/dto/llm/

# 删除应用服务接口
rm application/application-api/src/main/java/com/catface996/aiops/application/api/service/llm/LlmServiceApplicationService.java

# 删除应用服务实现目录
rm -rf application/application-impl/src/main/java/com/catface996/aiops/application/impl/service/llm/
```

### Step 4: 删除 Domain 层代码

```bash
# 删除领域服务接口目录
rm -rf domain/domain-api/src/main/java/com/catface996/aiops/domain/service/llm/

# 删除领域服务实现目录
rm -rf domain/domain-impl/src/main/java/com/catface996/aiops/domain/impl/service/llm/

# 删除领域模型目录
rm -rf domain/domain-model/src/main/java/com/catface996/aiops/domain/model/llm/
```

### Step 5: 删除 Repository 层代码

```bash
# 删除仓储接口目录
rm -rf domain/repository-api/src/main/java/com/catface996/aiops/repository/llm/

# 删除仓储实现目录
rm -rf infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/impl/llm/

# 删除 Mapper 接口目录
rm -rf infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/mapper/llm/

# 删除 PO 目录
rm -rf infrastructure/repository/mysql-impl/src/main/java/com/catface996/aiops/repository/mysql/po/llm/

# 删除 XML Mapper 目录
rm -rf infrastructure/repository/mysql-impl/src/main/resources/mapper/llm/
```

### Step 6: 删除 Common 层代码

```bash
rm common/src/main/java/com/catface996/aiops/common/enums/LlmServiceErrorCode.java
```

### Step 7: 创建数据库迁移脚本

创建文件 `bootstrap/src/main/resources/db/migration/V9__Drop_llm_service_table.sql`:

```sql
-- 移除 LLM 服务配置表
-- Feature: 001-remove-llm-service
-- Date: 2025-12-25

DROP TABLE IF EXISTS llm_service_config;
```

### Step 8: 验证

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 启动应用
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

# 验证接口返回 404
curl -X POST http://localhost:8080/api/v1/llm-services/query
# 预期: 404 Not Found

# 验证 Swagger 文档
# 访问 http://localhost:8080/swagger-ui/index.html
# 确认不存在 "LLM 服务管理" 标签
```

## Verification Checklist

- [ ] `mvn clean compile` 成功
- [ ] `mvn test` 全部通过
- [ ] 应用正常启动
- [ ] LLM 服务接口返回 404
- [ ] Swagger 文档无 LLM 服务相关内容
- [ ] `grep -r "LlmService" --include="*.java" .` 返回空

## Rollback

如需回滚：

```bash
# 1. Git 回滚代码
git checkout HEAD~1 -- .

# 2. 数据库回滚（如果已执行迁移）
# 重新创建表（使用 V7 脚本内容）
```
