# Quickstart: 移除认证功能

**Feature**: 001-remove-auth-features
**Date**: 2025-12-25

## 前提条件

- Java 21 已安装
- Maven 3.8+ 已安装
- MySQL 8.0 数据库可访问
- 外部认证系统已部署并运行

## 快速验证指南

### 1. 构建项目

```bash
mvn clean package -DskipTests
```

**预期结果**: 构建成功，无编译错误

### 2. 运行测试

```bash
mvn test
```

**预期结果**: 所有业务功能测试通过（认证相关测试已移除）

### 3. 启动应用

```bash
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

**预期结果**: 应用正常启动，无认证相关报错

### 4. 验证认证接口已移除

```bash
# 测试注册接口（应返回 404）
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@test.com","password":"test123"}'

# 测试登录接口（应返回 404）
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test123"}'
```

**预期结果**: 返回 404 Not Found

### 5. 验证业务接口正常

```bash
# 测试业务接口（应正常响应）
curl -X POST http://localhost:8080/api/v1/resources/list \
  -H "Content-Type: application/json" \
  -d '{"userId": 1}'
```

**预期结果**: 返回业务数据，不要求 JWT 令牌

### 6. 验证数据库表已删除

```sql
-- 连接到 MySQL 数据库后执行
SHOW TABLES LIKE 't_account';
SHOW TABLES LIKE 't_session';
```

**预期结果**: 无结果返回（表已删除）

---

## 关键变更摘要

| 变更类型 | 内容 |
|---------|------|
| 移除的接口 | `/api/v1/auth/*` |
| 移除的表 | `t_account`, `t_session` |
| 移除的安全配置 | Spring Security JWT 验证 |
| 用户身份传递 | 请求体中的 `userId` 字段 |

---

## 回滚指南

如需回滚此变更：

1. 切换到变更前的代码版本
2. 手动执行原始的建表迁移脚本（V1, V2）
3. 恢复用户数据（如果有备份）
4. 重新部署应用

**注意**: 本次变更为破坏性变更，数据直接删除，无自动回滚机制。

---

## 故障排除

### 问题: 启动时报 Bean 未找到错误

**原因**: 可能有遗漏的代码引用了已删除的认证组件

**解决**: 搜索并移除所有对 `AuthController`, `AuthApplicationService`, `AuthDomainService` 等的引用

### 问题: Flyway 迁移失败

**原因**: 迁移版本冲突或外键约束问题

**解决**:
1. 检查迁移脚本版本号是否正确递增
2. 确保先删除 `t_session` 再删除 `t_account`

### 问题: 业务接口返回 403

**原因**: Spring Security 配置未完全移除

**解决**: 检查是否还有残留的安全配置或过滤器
