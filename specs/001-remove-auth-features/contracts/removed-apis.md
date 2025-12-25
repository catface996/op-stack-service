# Removed APIs: 移除认证功能

**Feature**: 001-remove-auth-features
**Date**: 2025-12-25
**Type**: API 删除清单

## 概述

本文档记录需要移除的认证相关 API 端点。移除后，这些端点将不再可用。

---

## 待移除的 API 端点

### 1. 用户注册

**端点**: `POST /api/v1/auth/register`

**当前请求格式**:
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

**当前响应格式**:
```json
{
  "id": "number",
  "username": "string",
  "email": "string",
  "role": "string",
  "createdAt": "datetime"
}
```

**移除后行为**: 返回 404 Not Found

---

### 2. 用户登录

**端点**: `POST /api/v1/auth/login`

**当前请求格式**:
```json
{
  "username": "string",
  "password": "string",
  "rememberMe": "boolean"
}
```

**当前响应格式**:
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "expiresIn": "number",
  "tokenType": "Bearer"
}
```

**移除后行为**: 返回 404 Not Found

---

### 3. 用户登出

**端点**: `POST /api/v1/auth/logout`

**当前请求头**:
```
Authorization: Bearer <token>
```

**当前响应格式**:
```json
{
  "message": "Successfully logged out"
}
```

**移除后行为**: 返回 404 Not Found

---

### 4. 令牌刷新

**端点**: `POST /api/v1/auth/refresh`

**当前请求格式**:
```json
{
  "refreshToken": "string"
}
```

**当前响应格式**:
```json
{
  "accessToken": "string",
  "refreshToken": "string",
  "expiresIn": "number",
  "tokenType": "Bearer"
}
```

**移除后行为**: 返回 404 Not Found

---

## 待移除的会话相关 API

### 5. 会话管理端点

**控制器**: `SessionController.java`, `SessionCompatController.java`

需要检查并移除这些控制器中的所有端点，因为会话功能是认证系统的一部分。

---

## 安全配置变更

### 移除前的安全规则

```java
// SecurityConfig.java 中的配置
.requestMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll()
.requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
.anyRequest().authenticated()
```

### 移除后的安全规则

移除 Spring Security 配置后，系统将不再进行本地 JWT 验证。业务接口的访问控制将由外部认证系统/网关处理。

---

## 迁移说明

### 对调用方的影响

| 原接口 | 替代方案 |
|--------|---------|
| `/api/v1/auth/register` | 使用外部认证系统的注册接口 |
| `/api/v1/auth/login` | 使用外部认证系统的登录接口 |
| `/api/v1/auth/logout` | 使用外部认证系统的登出接口 |
| `/api/v1/auth/refresh` | 使用外部认证系统的令牌刷新接口 |

### 用户身份传递方式

移除本地认证后，业务接口通过请求体中的 `userId` 字段获取用户身份：

```json
{
  "userId": "number",
  // ... 其他业务字段
}
```

---

## 验证清单

- [ ] 确认 `/api/v1/auth/*` 端点返回 404
- [ ] 确认会话相关端点已移除
- [ ] 确认业务接口可以正常接收 userId 参数
- [ ] 确认系统不再进行本地 JWT 验证
