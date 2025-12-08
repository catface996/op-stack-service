# 需求规格说明书

**项目名称**: 用户名密码登录  
**文档版本**: v1.0.0  
**创建日期**: 2025-01-23  
**最后更新**: 2025-01-23  
**文档状态**: 草稿

---

## 1. 引言

### 1.1 文档目的

本文档定义了AIOps平台用户名密码登录功能的详细需求规格，包括功能需求、非功能需求、约束条件和验收标准。本文档将作为设计、开发、测试的基准。

### 1.2 项目背景

AIOps平台当前缺少身份认证机制，无法区分不同用户，无法进行权限控制和操作审计。本项目旨在实现基于用户名或邮箱的本地账号登录功能，为平台提供基础的身份认证能力。

### 1.3 读者对象

- 产品经理
- 架构师
- 开发团队（前端、后端）
- 测试团队
- 项目干系人

### 1.4 参考资料

- 原始需求文档：`doc/1-intent/2-features/F01-1-用户名密码登录.md`
- 需求澄清文档：`.kiro/specs/username-password-login/requirement-clarification.md`
- EARS语法规范
- OWASP安全最佳实践

---

## 2. 术语表

| 术语 | 定义 | 备注 |
|-----|------|-----|
| System（系统） | 指AIOps平台系统 | |
| User（用户） | 使用系统的最终用户 | 包括普通用户和管理员 |
| Account（账号） | 用户在系统中的账号实体 | 包含用户名、邮箱、加密密码等 |
| Username（用户名） | 用户自定义的账号标识符 | 3-20个字符，字母数字下划线 |
| Email（邮箱） | 用户的电子邮件地址 | 可作为账号标识符 |
| Password（密码） | 用户的登录凭据 | 8-64个字符，需符合强度要求 |
| Session（会话） | 用户登录后的会话对象 | 维护用户登录状态 |
| JWT Token | JSON Web Token | 用于客户端会话管理 |
| BCrypt | 密码哈希函数 | 用于安全存储密码 |
| Login Attempt（登录尝试） | 用户尝试登录的行为记录 | 用于防暴力破解 |
| Account Lock（账号锁定） | 账号锁定状态 | 登录失败次数超过阈值时触发 |
| Audit Log（审计日志） | 系统操作日志 | 用于安全审计和问题追踪 |
| HTTPS | 超文本传输安全协议 | 网关层卸载，应用层HTTP |
| CSRF Token | 跨站请求伪造令牌 | 防止CSRF攻击 |
| SQL Injection | SQL注入攻击 | 代码注入攻击技术 |
| XSS | 跨站脚本攻击 | 代码注入攻击 |
| Redis | 内存数据库 | 用于存储登录失败计数 |
| MySQL | 关系型数据库 | 用于存储账号和审计日志 |

---

## 3. 用户角色

| 角色ID | 角色名称 | 职责描述 | 权限级别 |
|-------|---------|---------|---------|
| ROLE-001 | 普通用户 | 使用系统功能，访问个人资源 | 基础访问权限 |
| ROLE-002 | 系统管理员 | 管理系统和用户，查看审计日志，解锁账号 | 管理员权限 |
| ROLE-003 | 未注册用户 | 注册新账号 | 无（注册后获得ROLE-001） |

---

## 4. 功能需求

### 4.1 用户故事 1: 用户名密码登录

**需求ID**: REQ-FR-001  
**优先级**: [MUST]  
**相关角色**: ROLE-001, ROLE-002

**用户故事**:
> 作为用户，我希望使用用户名和密码登录系统，以便访问系统功能。

**验收标准**:
1. WHEN User 提交有效的 Username 和 Password THEN THE System SHALL 验证凭据、创建 Session、生成 JWT Token 并返回给 User
2. WHEN User 成功登录 THEN THE System SHALL 将 User 重定向到首页
3. WHEN User 提交空的 Username 或 Password THEN THE System SHALL 拒绝登录并显示错误消息"用户名和密码不能为空"
4. WHEN User 提交不存在的 Username THEN THE System SHALL 显示通用错误消息"用户名或密码错误"
5. WHEN User 提交错误的 Password THEN THE System SHALL 显示通用错误消息"用户名或密码错误"并记录失败的 Login Attempt

---

### 4.2 用户故事 2: 邮箱密码登录

**需求ID**: REQ-FR-002  
**优先级**: [MUST]  
**相关角色**: ROLE-001, ROLE-002

**用户故事**:
> 作为用户，我希望使用邮箱和密码登录系统，以便不需要记住额外的用户名。

**验收标准**:
1. WHEN User 提交有效的 Email 和 Password THEN THE System SHALL 验证凭据、创建 Session、生成 JWT Token 并返回给 User
2. WHEN User 在登录表单输入 Email 格式的字符串 THEN THE System SHALL 识别为 Email 并进行相应验证
3. WHEN User 提交不存在的 Email THEN THE System SHALL 显示通用错误消息"用户名或密码错误"
4. WHEN User 使用 Email 登录失败 THEN THE System SHALL 记录失败的 Login Attempt 并计入账号锁定计数

---

### 4.3 用户故事 3: 账号注册

**需求ID**: REQ-FR-003  
**优先级**: [MUST]  
**相关角色**: ROLE-003

**用户故事**:
> 作为新用户，我希望能够注册账号，以便使用系统功能。

**验收标准**:
1. WHEN User 提交注册表单包含 Username、Email 和 Password THEN THE System SHALL 验证数据有效性并创建 Account
2. WHEN User 提交的 Email 格式无效 THEN THE System SHALL 拒绝注册并显示错误消息"邮箱格式无效"
3. WHEN User 提交已存在的 Email THEN THE System SHALL 拒绝注册并显示错误消息"该邮箱已被使用"
4. WHEN User 提交已存在的 Username THEN THE System SHALL 拒绝注册并显示错误消息"该用户名已被使用"
5. WHEN User 注册成功 THEN THE System SHALL 创建包含 Username、Email 和加密 Password 的 Account 并分配 ROLE-001 权限

---

### 4.4 用户故事 4: 密码安全存储

**需求ID**: REQ-FR-004  
**优先级**: [MUST]  
**相关角色**: ROLE-001, ROLE-002

**用户故事**:
> 作为用户，我希望系统安全地存储我的密码，以便保护我的账号安全。

**验收标准**:
1. WHEN THE System 存储 User 的 Password THEN THE System SHALL 使用 BCrypt 算法加密 Password
2. WHEN THE System 验证 User 身份 THEN THE System SHALL 使用 BCrypt 比较提交的 Password 与加密后的 Password
3. WHEN THE System 存储 Password THEN THE System SHALL NOT 以明文形式存储 Password
4. WHEN User 的 Password 被加密 THEN THE System SHALL 使用盐值增强安全性
5. WHEN THE System 比较 Password THEN THE System SHALL 使用恒定时间比较以防止时序攻击

---

### 4.5 用户故事 5: 防暴力破解

**需求ID**: REQ-FR-005  
**优先级**: [MUST]  
**相关角色**: ROLE-001, ROLE-002

**用户故事**:
> 作为系统管理员，我希望系统能够防止暴力破解攻击，以便保护用户账号安全。

**验收标准**:
1. WHEN User 连续登录失败 5 次 THEN THE System SHALL 锁定该 Account 30 分钟
2. WHEN Account 被锁定 THEN THE System SHALL 拒绝该 Account 的所有登录尝试
3. WHEN Account 被锁定 THEN THE System SHALL 显示消息"账号已锁定，请在X分钟后重试"并显示剩余锁定时间
4. WHEN 锁定期限到期 THEN THE System SHALL 自动解锁该 Account
5. WHEN User 成功登录 THEN THE System SHALL 将失败登录尝试计数器重置为零
6. WHEN Redis 不可用 THEN THE System SHALL 使用 MySQL 存储登录失败计数作为降级方案

---

### 4.6 用户故事 6: 管理员手动解锁

**需求ID**: REQ-FR-006  
**优先级**: [MUST]  
**相关角色**: ROLE-002

**用户故事**:
> 作为系统管理员，我希望能够手动解锁被锁定的账号，以便在特殊情况下快速恢复用户访问。

**验收标准**:
1. WHEN 管理员在管理界面选择被锁定的 Account 并点击解锁按钮 THEN THE System SHALL 立即解除该 Account 的锁定状态
2. WHEN Account 被手动解锁 THEN THE System SHALL 将失败登录尝试计数器重置为零
3. WHEN Account 被手动解锁 THEN THE System SHALL 记录解锁操作到 Audit Log
4. WHEN 管理员尝试解锁未锁定的 Account THEN THE System SHALL 显示提示消息"该账号未被锁定"

---

### 4.7 用户故事 7: 会话管理

**需求ID**: REQ-FR-007  
**优先级**: [MUST]  
**相关角色**: ROLE-001, ROLE-002

**用户故事**:
> 作为用户，我希望系统能够管理我的登录会话，以便在一定时间内保持登录状态。

**验收标准**:
1. WHEN User 成功登录 THEN THE System SHALL 创建默认过期时间为 2 小时的 Session
2. WHEN User 的 Session 过期 THEN THE System SHALL 要求 User 重新登录
3. WHEN User 访问受保护的资源 THEN THE System SHALL 验证 Session 是否有效
4. WHEN User 的 Session 无效或过期 THEN THE System SHALL 将 User 重定向到登录页面并显示提示消息"会话已过期，请重新登录"

---

### 4.8 用户故事 8: 记住我功能

**需求ID**: REQ-FR-008  
**优先级**: [SHOULD]  
**相关角色**: ROLE-001, ROLE-002

**用户故事**:
> 作为用户，我希望能够选择"记住我"选项，以便延长登录状态的有效期。

**验收标准**:
1. WHEN User 在登录时选择"记住我"选项 THEN THE System SHALL 将 Session 过期时间延长至 30 天
2. WHEN User 未选择"记住我"选项 THEN THE System SHALL 使用默认的 2 小时 Session 过期时间
3. WHEN 启用"记住我"的 User 关闭浏览器 THEN THE System SHALL 在浏览器重启后保持 Session

---

### 4.9 用户故事 9: 会话互斥

**需求ID**: REQ-FR-009  
**优先级**: [MUST]  
**相关角色**: ROLE-001, ROLE-002

**用户故事**:
> 作为系统管理员，我希望同一用户只能有一个活跃会话，以便防止账号共享和提高安全性。

**验收标准**:
1. WHEN User 在新设备登录 THEN THE System SHALL 使旧设备的 Session 失效
2. WHEN 旧设备的 Session 被使失效 THEN THE System SHALL 在旧设备访问时显示提示消息"您的账号已在其他设备登录"
3. WHEN User 在新设备登录 THEN THE System SHALL 记录新设备的登录信息到 Audit Log
4. WHEN 旧设备的 Session 被使失效 THEN THE System SHALL 记录会话失效事件到 Audit Log

---

### 4.10 用户故事 10: 安全退出

**需求ID**: REQ-FR-010  
**优先级**: [MUST]  
**相关角色**: ROLE-001, ROLE-002

**用户故事**:
> 作为用户，我希望能够安全地退出系统，以便保护我的账号安全。

**验收标准**:
1. WHEN User 点击退出按钮 THEN THE System SHALL 使 User 的 Session 失效
2. WHEN User 退出登录 THEN THE System SHALL 从客户端清除 JWT Token
3. WHEN User 退出登录 THEN THE System SHALL 将 User 重定向到登录页面
4. WHEN User 的 Session 被使失效 THEN THE System SHALL 拒绝使用该 Session 的后续请求

---

### 4.11 用户故事 11: 审计日志

**需求ID**: REQ-FR-011  
**优先级**: [MUST]  
**相关角色**: ROLE-002

**用户故事**:
> 作为系统管理员，我希望系统记录所有登录相关的操作，以便进行安全审计。

**验收标准**:
1. WHEN User 尝试登录 THEN THE System SHALL 记录包含时间戳、用户名、IP地址和结果的 Audit Log
2. WHEN User 成功登录 THEN THE System SHALL 记录成功登录的 Audit Log
3. WHEN User 登录失败 THEN THE System SHALL 记录包含失败原因的登录失败 Audit Log
4. WHEN Account 被锁定 THEN THE System SHALL 记录账号锁定的 Audit Log
5. WHEN User 退出登录 THEN THE System SHALL 记录退出登录的 Audit Log
6. WHEN 管理员手动解锁 Account THEN THE System SHALL 记录解锁操作的 Audit Log

---

### 4.12 用户故事 12: 密码强度要求

**需求ID**: REQ-FR-012  
**优先级**: [MUST]  
**相关角色**: ROLE-003

**用户故事**:
> 作为新用户，我希望在创建账号时设置符合安全要求的密码，以便保护我的账号安全。

**验收标准**:
1. WHEN User 创建 Password THEN THE System SHALL 要求 Password 长度至少为 8 个字符
2. WHEN User 创建 Password THEN THE System SHALL 要求 Password 包含大写字母、小写字母、数字、特殊字符中的至少 3 类
3. WHEN User 创建 Password THEN THE System SHALL 验证 Password 不包含 Username 或 Email 的任何部分
4. WHEN User 创建 Password THEN THE System SHALL 验证 Password 不是常见弱密码（如password123、admin123等）
5. WHEN User 提交不符合要求的 Password THEN THE System SHALL 拒绝该 Password 并显示具体的验证错误消息

---

## 5. 非功能需求

### 5.1 性能需求

**REQ-NFR-PERF-001**: THE System SHALL 在 2 秒内响应 95% 的登录请求

**REQ-NFR-PERF-002**: THE System SHALL 支持 1000 个并发用户同时登录

**REQ-NFR-PERF-003**: THE System SHALL 在 500 毫秒内完成单次 BCrypt 密码验证

**REQ-NFR-PERF-004**: THE System SHALL 在 1 秒内完成 Session 验证

---

### 5.2 安全需求

**REQ-NFR-SEC-001**: WHEN THE System 接收登录请求 THEN THE System SHALL 验证并清理所有输入以防止 SQL Injection

**REQ-NFR-SEC-002**: WHEN THE System 处理登录请求 THEN THE System SHALL 包含 CSRF Token 以防止跨站请求伪造

**REQ-NFR-SEC-003**: WHEN THE System 渲染登录响应 THEN THE System SHALL 转义输出以防止 XSS 攻击

**REQ-NFR-SEC-004**: WHEN THE System 处理登录请求 THEN THE System SHALL 通过网关层强制使用 HTTPS 传输

**REQ-NFR-SEC-005**: WHEN THE System 存储会话数据 THEN THE System SHALL 为 Cookie 使用 secure 和 httpOnly 标志

**REQ-NFR-SEC-006**: THE System SHALL 使用 BCrypt work factor 至少为 10 加密密码

---

### 5.3 可用性需求

**REQ-NFR-AVAIL-001**: THE System SHALL 达到 99.9% 的月度可用性

**REQ-NFR-AVAIL-002**: WHEN Redis 不可用 THEN THE System SHALL 自动切换到 MySQL 降级方案并继续提供服务

---

### 5.4 可维护性需求

**REQ-NFR-MAINT-001**: THE System SHALL 记录所有错误信息到日志系统

**REQ-NFR-MAINT-002**: THE System SHALL 为所有关键操作提供详细的日志记录

**REQ-NFR-MAINT-003**: THE System SHALL 使用结构化日志格式便于日志分析

---

### 5.5 兼容性需求

**REQ-NFR-COMPAT-001**: THE System SHALL 支持 Chrome、Firefox、Safari 最新两个版本

**REQ-NFR-COMPAT-002**: THE System SHALL 支持 JavaScript ES6+ 和 Fetch API

**REQ-NFR-COMPAT-003**: THE System SHALL 支持 Cookie 和 LocalStorage

---

## 6. 约束条件

### 6.1 技术约束

- 必须使用 Spring Security 框架
- 必须使用 BCrypt 加密算法
- 必须使用 JWT Token 进行会话管理
- 必须使用 Redis 存储登录失败计数（MySQL 作为降级方案）
- 必须使用 MySQL 存储用户账号和审计日志
- 网关层卸载 HTTPS，应用层使用 HTTP

### 6.2 业务约束

- 用户名长度：3-20 个字符，只能包含字母、数字、下划线
- 邮箱长度：最大 100 个字符
- 密码长度：8-64 个字符
- 登录失败锁定：5 次失败锁定 30 分钟（不可配置）
- 会话有效期：默认 2 小时，"记住我" 30 天（不可配置）
- 同一用户只能有一个活跃会话

### 6.3 时间约束

- 本功能为 MVP 必须功能，需要在第一个 Sprint 完成
- 预计开发时间：2 周
- 预计测试时间：3 天

---

## 7. 假设与依赖

### 7.1 假设

- 用户具备基本的互联网使用能力
- 用户设备支持 JavaScript 和 Cookie
- Redis 可用性达到 99.9%（失败后使用 MySQL 兜底）
- 数据库支持事务
- 系统部署在 HTTPS 环境（网关层卸载）

### 7.2 外部依赖

- Redis 缓存服务（基础设施团队提供）
- MySQL 数据库（基础设施团队提供）
- HTTPS 网关（运维团队提供）

### 7.3 内部依赖

- 用户数据模型（后端团队设计）
- 审计日志模块（后端团队开发）
- 前端登录页面（前端团队开发）

---

## 8. 功能边界

### 8.1 包含的功能

- ✅ 用户名/邮箱注册和登录
- ✅ 密码 BCrypt 加密存储
- ✅ 防暴力破解（5次失败锁定30分钟）
- ✅ 管理员手动解锁
- ✅ 会话管理（默认2小时，"记住我"30天）
- ✅ 会话互斥（同一用户只能有一个活跃会话）
- ✅ 安全退出
- ✅ 审计日志记录
- ✅ Web 安全防护（SQL注入、XSS、CSRF）
- ✅ 密码强度要求

### 8.2 不包含的功能（后续版本）

- ❌ 第三方 OAuth 登录（如 Google、GitHub）
- ❌ LDAP/AD 认证
- ❌ 多因素认证（MFA）
- ❌ 密码找回功能
- ❌ 手机号注册和登录
- ❌ 邮箱验证码确认
- ❌ 审计日志归档功能
- ❌ 密码有效期和历史密码检查

---

## 9. 需求追溯矩阵

| 需求ID | 原始需求来源 | 优先级 | 设计文档 | 测试用例 |
|-------|------------|--------|---------|---------|
| REQ-FR-001 | F01-1 AC1 | MUST | TBD | TBD |
| REQ-FR-002 | F01-1 AC1 | MUST | TBD | TBD |
| REQ-FR-003 | F01-1 核心功能 | MUST | TBD | TBD |
| REQ-FR-004 | F01-1 AC3 | MUST | TBD | TBD |
| REQ-FR-005 | F01-1 AC2 | MUST | TBD | TBD |
| REQ-FR-006 | 需求澄清 | MUST | TBD | TBD |
| REQ-FR-007 | F01-4 会话管理 | MUST | TBD | TBD |
| REQ-FR-008 | F01-4 会话管理 | SHOULD | TBD | TBD |
| REQ-FR-009 | 需求澄清 | MUST | TBD | TBD |
| REQ-FR-010 | F01-4 会话管理 | MUST | TBD | TBD |
| REQ-FR-011 | F01-1 AC3 | MUST | TBD | TBD |
| REQ-FR-012 | F01-1 核心功能 | MUST | TBD | TBD |

---

## 10. 版本历史

| 版本 | 日期 | 修改内容 | 作者 |
|-----|------|---------|------|
| v1.0.0 | 2025-01-23 | 初始版本，完成需求澄清和文档编写 | AI Assistant |

---

**文档审核**：
- 需求分析师：AI Assistant
- 产品负责人：[待审核]
- 技术负责人：[待审核]
