# Requirements Document

## Introduction

本文档定义了 AIOps Service 的 LLM 服务配置功能需求。该功能允许系统管理员配置和管理多种 LLM 服务（如 OpenAI、Claude），为 Agent 提供智能分析能力，同时提供成本控制、连接测试、调用统计等管理功能。

## Glossary

- **LLM Service**: 大语言模型服务，指 OpenAI、Anthropic Claude 等提供自然语言处理能力的云端服务或本地部署模型
- **System**: AIOps Service 系统
- **Administrator**: 系统管理员，负责配置和管理 LLM 服务的用户
- **API Key**: 用于访问 LLM 服务的认证密钥
- **Token**: LLM 服务计费的基本单位，通常表示文本的最小处理单元
- **Temperature**: 控制 LLM 输出随机性的参数，范围 0-2，值越高输出越随机
- **Top-P**: 核采样参数，控制输出的多样性
- **Frequency Penalty**: 频率惩罚参数，降低重复内容的概率
- **Presence Penalty**: 存在惩罚参数，鼓励模型谈论新话题
- **Cost Limit**: 成本限制，用于控制 LLM 服务调用的费用上限
- **Default Service**: 默认服务，系统优先使用的 LLM 服务
- **Fallback Service**: 备用服务，当默认服务不可用时自动切换的服务

## Requirements

### Requirement 1

**User Story:** 作为系统管理员，我希望添加和配置 LLM 服务，以便系统可以使用 LLM 进行智能分析

#### Acceptance Criteria

1. WHEN Administrator 选择添加 LLM 服务 THEN THE System SHALL 支持至少 OpenAI 和 Claude 两种 LLM 服务类型
2. WHEN Administrator 填写 LLM 服务的基本信息 THEN THE System SHALL 要求提供服务名称和服务描述
3. WHEN Administrator 配置 LLM 服务优先级 THEN THE System SHALL 允许设置优先级数值（1-100）
4. WHEN Administrator 提供 API Key THEN THE System SHALL 使用 AES-256 加密算法存储 API Key
5. WHEN Administrator 配置 API 端点 THEN THE System SHALL 支持自定义 API 端点 URL

### Requirement 2

**User Story:** 作为系统管理员，我希望配置 LLM 模型参数，以便控制 LLM 的输出质量和行为

#### Acceptance Criteria

1. WHEN Administrator 选择 OpenAI 服务 THEN THE System SHALL 提供 GPT-4 和 GPT-3.5-turbo 模型选项
2. WHEN Administrator 选择 Claude 服务 THEN THE System SHALL 提供 Claude-3-opus、Claude-3-sonnet 和 Claude-3-haiku 模型选项
3. WHEN Administrator 设置 Temperature 参数 THEN THE System SHALL 限制取值范围在 0.0 到 2.0 之间
4. WHEN Administrator 设置最大 Token 数 THEN THE System SHALL 限制取值范围在 1 到 128000 之间
5. WHEN Administrator 设置 Top-P 参数 THEN THE System SHALL 限制取值范围在 0.0 到 1.0 之间
6. WHEN Administrator 设置 Frequency Penalty THEN THE System SHALL 限制取值范围在 -2.0 到 2.0 之间
7. WHEN Administrator 设置 Presence Penalty THEN THE System SHALL 限制取值范围在 -2.0 到 2.0 之间

### Requirement 3

**User Story:** 作为系统管理员，我希望测试 LLM 服务连接，以便验证配置是否正确

#### Acceptance Criteria

1. WHEN Administrator 点击测试连接按钮 THEN THE System SHALL 发送测试请求到配置的 LLM 服务
2. WHEN 测试请求成功 THEN THE System SHALL 显示成功消息和响应时间
3. WHEN 测试请求失败且原因是 API Key 无效 THEN THE System SHALL 显示 "API Key 验证失败" 错误信息
4. WHEN 测试请求失败且原因是网络超时 THEN THE System SHALL 显示 "网络连接超时" 错误信息
5. WHEN 测试请求失败且原因是 API 端点不可达 THEN THE System SHALL 显示 "API 端点无法访问" 错误信息

### Requirement 4

**User Story:** 作为系统管理员，我希望设置成本限制，以便控制 LLM 服务的使用成本

#### Acceptance Criteria

1. WHEN Administrator 设置每日成本限制 THEN THE System SHALL 存储每日成本限制金额（单位：美元）
2. WHEN Administrator 设置每月成本限制 THEN THE System SHALL 存储每月成本限制金额（单位：美元）
3. WHEN Administrator 设置单次调用成本限制 THEN THE System SHALL 存储单次调用成本限制金额（单位：美元）
4. WHEN Administrator 设置成本告警阈值 THEN THE System SHALL 存储告警阈值百分比（0-100）
5. WHEN LLM 调用累计成本达到告警阈值 THEN THE System SHALL 发送告警通知给 Administrator
6. WHEN LLM 调用累计成本达到成本限制 THEN THE System SHALL 拒绝新的 LLM 调用请求

### Requirement 5

**User Story:** 作为系统管理员，我希望查看 LLM 服务的调用统计，以便了解使用情况和成本

#### Acceptance Criteria

1. WHEN Administrator 查看调用统计 THEN THE System SHALL 显示总调用次数
2. WHEN Administrator 查看调用统计 THEN THE System SHALL 显示总成本（基于 Token 计费）
3. WHEN Administrator 查看调用统计 THEN THE System SHALL 显示平均响应延迟（毫秒）
4. WHEN Administrator 查看调用统计 THEN THE System SHALL 显示成功率百分比
5. WHEN Administrator 查看调用统计 THEN THE System SHALL 显示错误次数和错误类型分布
6. WHEN Administrator 选择时间范围为"按天" THEN THE System SHALL 显示最近 30 天的统计数据
7. WHEN Administrator 选择时间范围为"按周" THEN THE System SHALL 显示最近 12 周的统计数据
8. WHEN Administrator 选择时间范围为"按月" THEN THE System SHALL 显示最近 12 个月的统计数据

### Requirement 6

**User Story:** 作为系统管理员，我希望管理多个 LLM 服务，以便实现服务切换和高可用

#### Acceptance Criteria

1. WHEN Administrator 启用 LLM 服务 THEN THE System SHALL 将服务状态设置为"已启用"并允许调用
2. WHEN Administrator 禁用 LLM 服务 THEN THE System SHALL 将服务状态设置为"已禁用"并拒绝新的调用
3. WHEN Administrator 编辑 LLM 服务配置 THEN THE System SHALL 保存修改后的配置并保持服务状态不变
4. WHEN Administrator 删除 LLM 服务 THEN THE System SHALL 删除服务配置和相关统计数据
5. WHEN Administrator 设置默认服务 THEN THE System SHALL 标记该服务为 Default Service
6. WHEN 系统调用 LLM 且 Default Service 可用 THEN THE System SHALL 使用 Default Service
7. WHEN 系统调用 LLM 且 Default Service 不可用 THEN THE System SHALL 按优先级顺序自动切换到 Fallback Service

### Requirement 7

**User Story:** 作为系统管理员，我希望 LLM 服务配置具有超时和重试机制，以便提高系统稳定性

#### Acceptance Criteria

1. WHEN Administrator 配置超时时间 THEN THE System SHALL 存储超时时间（单位：秒，范围 1-300）
2. WHEN LLM 调用超过超时时间 THEN THE System SHALL 取消请求并返回超时错误
3. WHEN Administrator 配置重试次数 THEN THE System SHALL 存储重试次数（范围 0-5）
4. WHEN LLM 调用失败且重试次数大于 0 THEN THE System SHALL 自动重试指定次数
5. WHEN LLM 调用重试仍然失败 THEN THE System SHALL 记录错误日志并返回失败响应

### Requirement 8

**User Story:** 作为系统，我需要安全地存储和传输 LLM 服务的敏感信息，以便保护用户数据安全

#### Acceptance Criteria

1. WHEN System 存储 API Key THEN THE System SHALL 使用 AES-256 加密算法加密后存储
2. WHEN System 调用 LLM 服务 THEN THE System SHALL 使用 HTTPS 协议传输数据
3. WHEN System 记录 LLM 调用日志 THEN THE System SHALL 不记录 API Key 和敏感请求内容
4. WHEN Administrator 查看 LLM 服务配置 THEN THE System SHALL 显示 API Key 的掩码形式（如 sk-***abc）

### Requirement 9

**User Story:** 作为系统，我需要记录 LLM 服务的调用历史，以便进行审计和问题排查

#### Acceptance Criteria

1. WHEN System 调用 LLM 服务 THEN THE System SHALL 记录调用时间、服务名称、模型名称、请求 Token 数、响应 Token 数、成本和响应时间
2. WHEN LLM 调用成功 THEN THE System SHALL 记录调用状态为"成功"
3. WHEN LLM 调用失败 THEN THE System SHALL 记录调用状态为"失败"和错误信息
4. WHEN Administrator 查询调用历史 THEN THE System SHALL 支持按时间范围、服务名称、状态筛选
5. WHEN 调用历史记录超过 90 天 THEN THE System SHALL 自动归档或删除旧记录
