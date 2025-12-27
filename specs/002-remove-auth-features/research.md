# Research: 移除认证相关功能

**Feature**: 002-remove-auth-features
**Date**: 2025-12-27

## 研究摘要

本文档记录了移除认证相关功能前的依赖关系分析和清理策略研究。

## 1. 待删除文件分析

### 1.1 RefreshTokenResponse.java

**路径**: `interface/interface-http/src/main/java/.../dto/auth/RefreshTokenResponse.java`

**Decision**: 安全删除

**Rationale**:
- 该文件是令牌刷新响应 DTO，仅用于认证功能
- 认证功能已迁移到 op-stack-auth 服务
- 当前项目中无代码依赖此类

**使用情况**:
- 无实际代码引用
- 目录 `dto/auth/` 中仅有此文件

### 1.2 AuthErrorCode.java

**路径**: `common/src/main/java/.../enums/AuthErrorCode.java`

**Decision**: 安全删除

**Rationale**:
- 该枚举定义认证相关错误码（AUTH_001 ~ AUTH_004）
- 仅在 BusinessException、BaseException、ErrorCode 的**注释**中作为示例引用
- 无实际代码调用此枚举

**使用情况**:
- BusinessException.java: 注释示例（L30, L33）
- BaseException.java: 注释示例（L22, L25）
- ErrorCode.java: 注释示例（L18, L25, L42, L43）

**需要清理**:
- 更新上述文件中的示例注释，改用 ResourceErrorCode 等现有枚举

### 1.3 SessionErrorCode.java

**路径**: `common/src/main/java/.../enums/SessionErrorCode.java`

**Decision**: 安全删除

**Rationale**:
- 该枚举定义会话相关错误码（AUTH_101 ~ AUTH_203, AUTHZ_001）
- 无实际代码引用此枚举
- 会话管理功能已迁移到 op-stack-auth 服务

**使用情况**:
- 无任何代码引用

## 2. 待删除目录分析

### 2.1 空目录列表（仅含 .gitkeep）

| 目录路径 | 状态 | 操作 |
|---------|------|------|
| `interface/.../controller/auth/` | 仅含 .gitkeep | 删除整个目录 |
| `interface/.../dto/auth/` | 含 RefreshTokenResponse.java | 删除整个目录 |
| `application/application-api/.../service/auth/` | 仅含 .gitkeep | 删除整个目录 |
| `application/application-api/.../command/auth/` | 仅含 .gitkeep | 删除整个目录 |
| `application/application-impl/.../service/auth/` | 仅含 .gitkeep | 删除整个目录 |

## 3. OpenApiConfig 更新分析

### 3.1 当前描述内容

```java
"## 功能模块\n\n" +
"### 用户与认证\n" +
"- **认证管理**: 用户注册、登录、登出、令牌刷新\n" +
"- **会话管理**: 会话验证、会话互斥、多设备会话管理、强制登出\n" +
"- **管理员功能**: 账号解锁、账号查询、系统配置管理\n\n" +
"### 资源管理\n" +
// ...
"## 认证方式\n\n" +
"除注册和登录接口外，所有接口需要在请求头中携带 JWT Token：\n"
```

### 3.2 更新策略

**Decision**: 移除"用户与认证"章节，更新认证方式说明

**Rationale**:
- 用户与认证功能已迁移到 op-stack-auth 服务
- 当前服务的认证由网关统一处理，无需在服务文档中详细说明
- 保留资源管理相关功能描述

**更新内容**:
1. 删除"用户与认证"整个章节
2. 更新"认证方式"说明为"本服务接口通过网关统一认证，无需单独处理 Token"
3. 保留资源管理、拓扑图、提示词模板等功能描述

## 4. 清理策略

### 4.1 清理顺序

1. **第一步**: 更新注释文件（BusinessException、BaseException、ErrorCode）
   - 将 AuthErrorCode 示例替换为 ResourceErrorCode 等现有枚举

2. **第二步**: 删除认证相关文件
   - 删除 RefreshTokenResponse.java
   - 删除 AuthErrorCode.java
   - 删除 SessionErrorCode.java

3. **第三步**: 删除空目录
   - 删除 controller/auth/
   - 删除 dto/auth/
   - 删除 application 层的 auth 目录

4. **第四步**: 更新 OpenApiConfig.java
   - 移除"用户与认证"功能描述
   - 更新认证方式说明

5. **第五步**: 验证
   - 执行编译验证
   - 启动应用验证 Swagger 文档

### 4.2 潜在风险

| 风险 | 可能性 | 缓解措施 |
|------|--------|---------|
| 编译错误 | 低 | 先更新注释再删除文件 |
| 遗漏引用 | 低 | 使用 grep 全面搜索确认 |
| Swagger 异常 | 低 | 启动应用验证 |

## 5. 结论

本次清理任务风险可控，主要工作是代码删除和注释更新。关键要点：

1. RefreshTokenResponse、AuthErrorCode、SessionErrorCode 可安全删除
2. 需要同步更新 3 个异常相关文件中的示例注释
3. OpenApiConfig 需要更新功能描述和认证说明
4. 清理顺序：先更新注释 → 删除文件 → 删除目录 → 更新配置 → 验证
