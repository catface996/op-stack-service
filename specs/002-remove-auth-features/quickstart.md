# Quickstart: 移除认证相关功能

本文档提供快速验证清理结果的步骤。

## 前置条件

- 已完成认证相关代码移除
- 本地有 MySQL 和 Redis 服务运行

## 验证步骤

### 1. 编译验证

```bash
mvn clean compile
```

**预期结果**: BUILD SUCCESS

### 2. 打包验证

```bash
mvn clean package -DskipTests
```

**预期结果**: BUILD SUCCESS

### 3. 启动应用

```bash
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

**预期结果**: 应用正常启动，无错误日志

### 4. 验证 Swagger 文档

访问 http://localhost:8080/swagger-ui/index.html

**预期结果**:
- 不显示"用户与认证"功能模块描述
- 仅显示资源管理、拓扑图、提示词模板等功能描述
- 认证方式说明已更新

### 5. 验证文件删除

```bash
# 检查认证相关文件是否已删除
ls interface/interface-http/src/main/java/com/catface996/aiops/interface_/http/dto/auth/ 2>/dev/null && echo "ERROR: auth dto dir exists" || echo "OK: auth dto dir removed"

ls common/src/main/java/com/catface996/aiops/common/enums/AuthErrorCode.java 2>/dev/null && echo "ERROR: AuthErrorCode exists" || echo "OK: AuthErrorCode removed"

ls common/src/main/java/com/catface996/aiops/common/enums/SessionErrorCode.java 2>/dev/null && echo "ERROR: SessionErrorCode exists" || echo "OK: SessionErrorCode removed"
```

**预期结果**: 所有检查输出 "OK: ... removed"

### 6. 验证注释更新

```bash
# 检查是否还有 AuthErrorCode 引用
grep -r "AuthErrorCode" --include="*.java" . | grep -v ".kiro" | grep -v "specs"
```

**预期结果**: 无输出（不存在 AuthErrorCode 引用）

## 回滚方案

如需回滚，执行：

```bash
git checkout main
git branch -D 002-remove-auth-features
```
