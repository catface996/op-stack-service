# Feature Specification: 配置 LLM 服务

**Feature Branch**: `001-llm-service`
**Created**: 2025-12-05
**Status**: Draft
**Input**: User description: "F09-配置LLM服务.md - 系统管理员配置 LLM 服务（OpenAI、Claude等），以便 Agent 可以使用 LLM 进行智能分析"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 添加和配置 LLM 服务 (Priority: P1)

作为系统管理员，我希望能够添加和配置 LLM 服务（如 OpenAI、Claude），包括供应商类型、端点、模型参数等，以便在配置 Agent 时选择可用的 LLM 服务。

**Why this priority**: 这是整个功能的核心，没有 LLM 服务配置，Agent 无法进行任何智能分析操作。必须首先完成此功能才能使用其他相关功能。

**Independent Test**: 可以通过添加一个 LLM 服务配置并成功保存来独立测试，系统能够存储配置信息并在服务列表中显示。

**Acceptance Scenarios**:

1. **Given** 系统管理员已登录系统, **When** 管理员选择添加 LLM 服务并填写服务名称、供应商类型（OpenAI/Claude/本地模型）、端点地址, **Then** 系统保存配置信息，服务出现在服务列表中
2. **Given** 已添加 LLM 服务, **When** 管理员编辑服务的模型参数（Temperature、Max Tokens、Top-P 等）, **Then** 系统保存更新后的参数配置
3. **Given** 已添加 LLM 服务, **When** 管理员查看服务详情, **Then** 系统显示服务配置信息（供应商、模型、端点等）

---

### User Story 2 - 管理 LLM 服务生命周期 (Priority: P2)

作为系统管理员，我希望能够启用/禁用、编辑和删除 LLM 服务，以便灵活管理系统中的 LLM 服务。

**Why this priority**: 服务的生命周期管理是运维的基本需求，但在初始配置完成后才需要，因此优先级略低于配置。

**Independent Test**: 可以通过对已有服务执行启用/禁用/编辑/删除操作来独立测试。

**Acceptance Scenarios**:

1. **Given** 存在已启用的 LLM 服务, **When** 管理员禁用该服务, **Then** 服务状态变为"已禁用"，Agent 不再使用该服务
2. **Given** 存在已禁用的 LLM 服务, **When** 管理员启用该服务, **Then** 服务状态变为"已启用"，Agent 可以使用该服务
3. **Given** 存在 LLM 服务, **When** 管理员删除该服务, **Then** 系统要求确认后删除服务配置，服务从列表中移除

---

### User Story 3 - 设置默认服务和优先级 (Priority: P2)

作为系统管理员，我希望能够设置默认 LLM 服务并配置服务优先级，以便在配置 Agent 时能够选择合适的 LLM 服务。

**Why this priority**: 多服务管理提供了配置灵活性，但需要先有多个服务配置才有意义。

**Independent Test**: 可以通过配置多个服务、设置默认服务、调整优先级来独立测试。

**Acceptance Scenarios**:

1. **Given** 配置了多个 LLM 服务, **When** 管理员将某个服务设置为默认, **Then** 该服务标记为"默认"，在 Agent 配置时默认选中
2. **Given** 配置了多个服务, **When** 管理员调整服务优先级, **Then** 服务列表按优先级排序显示

---

### Edge Cases

- 当用户输入无效的模型参数（如 Temperature > 2）时，系统如何处理？（前端验证并提示错误，后端拒绝保存）
- 当删除正在被 Agent 使用的 LLM 服务配置时，系统如何处理？（提示警告，需要确认后才能删除）
- 当尝试禁用唯一启用的默认服务时，系统如何处理？（提示需要先设置其他服务为默认）
- 当服务名称重复时，系统如何处理？（拒绝保存，提示名称已存在）

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统必须支持添加 LLM 服务配置（支持 OpenAI、Claude、本地模型等类型）
- **FR-002**: 系统必须存储模型供应商和模型配置信息（不包含 API Key）
- **FR-003**: 系统必须支持配置 LLM 服务的基本信息（名称、描述、端点地址）
- **FR-004**: 系统必须支持配置模型参数（Temperature: 0-2, Max Tokens: 1-128000, Top-P: 0-1）
- **FR-005**: 系统必须支持启用/禁用 LLM 服务
- **FR-006**: 系统必须支持编辑已有 LLM 服务的配置
- **FR-007**: 系统必须支持删除 LLM 服务（需确认操作）
- **FR-008**: 系统必须支持设置默认 LLM 服务
- **FR-009**: 系统必须支持配置服务优先级（用于排序显示）
- **FR-010**: 系统必须只允许系统管理员配置 LLM 服务
- **FR-011**: 系统必须在 Agent 配置界面提供已配置的 LLM 服务列表供选择

### Key Entities

- **LLM 服务配置（LLMServiceConfig）**: 代表一个 LLM 服务的配置信息，包括服务名称、供应商类型、端点地址、模型参数、优先级、状态（启用/禁用）、是否为默认服务（注：不包含 API Key，由外部系统管理）
- **模型参数（ModelParameters）**: 代表 LLM 模型的配置参数，包括模型名称、Temperature、Max Tokens、Top-P、Frequency Penalty、Presence Penalty

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 管理员可以在 3 分钟内完成一个新 LLM 服务的完整配置（包括基本信息、模型参数）
- **SC-002**: 系统支持同时配置至少 10 个 LLM 服务
- **SC-003**: LLM 服务列表在 Agent 配置界面加载时间少于 1 秒
- **SC-004**: 配置变更立即生效，无需重启系统

## Clarifications

### Session 2025-12-05

- Q: API Key 应使用哪种加密标准存储？ → A: 本系统不存储 API Key，只配置模型供应商和模型信息。实际 LLM 调用由外部系统处理，该外部系统负责管理 API Key。
- Q: 本系统如何获取 LLM 调用统计数据？ → A: 本系统不需要统计功能，统计由外部调用系统负责。
- Q: 本系统如何向外部调用系统提供配置数据？ → A: 不需要自动集成。LLM 配置是为 Agent 配置可用的 LLM 列表，供人工配置使用，不涉及与外部系统的自动数据同步。

## Assumptions

- 本系统仅负责 LLM 服务配置管理，作为 Agent 配置的一部分
- LLM 配置信息供人工在配置 Agent 时选择使用，不涉及与外部系统的自动集成
- 实际 LLM 调用、API Key 管理、成本统计等由外部系统独立处理
- 系统管理员了解各 LLM 服务商的模型和参数规格
