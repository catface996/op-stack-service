# Quickstart: 移除废弃的API接口

本文档提供快速验证清理结果的步骤。

## 前置条件

- 已完成废弃代码移除
- 本地有 MySQL 和 Redis 服务运行

## 验证步骤

### 1. 编译验证

```bash
mvn clean package -DskipTests
```

**预期结果**: BUILD SUCCESS

### 2. 测试验证

```bash
mvn test
```

**预期结果**: 所有测试通过

### 3. 启动应用

```bash
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

**预期结果**: 应用正常启动，无错误日志

### 4. 验证废弃接口已移除

```bash
# 测试废弃接口（应返回 404）
curl -s -X POST http://localhost:8080/api/service/v1/resources/members/add \
  -H "Content-Type: application/json" \
  -d '{}' | grep -o '"code":[0-9]*'
# 预期: 404 或错误响应

# 测试替代接口（应正常工作）
curl -s -X POST http://localhost:8080/api/service/v1/topologies/members/query \
  -H "Content-Type: application/json" \
  -d '{"topologyId":1,"pageNum":1,"pageSize":10}'
# 预期: 正常响应
```

### 5. 验证 API 数量

```bash
curl -s http://localhost:8080/v3/api-docs | python3 -c "
import sys, json
d = json.load(sys.stdin)
count = sum(len(m) for m in d['paths'].values())
print(f'API 接口总数: {count}')
"
```

**预期结果**: API 接口总数: 45

### 6. 验证 Swagger 文档

访问 http://localhost:8080/swagger-ui/index.html

**预期结果**:
- 不显示任何带有"[已废弃]"标记的接口
- 资源管理分组只显示 CRUD 和审计日志接口

## 回滚方案

如需回滚，执行：

```bash
git checkout main
git branch -D 001-remove-deprecated-api
```
