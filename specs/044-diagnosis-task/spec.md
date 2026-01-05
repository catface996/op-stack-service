# Feature Specification: 诊断任务持久化

**Feature Branch**: `044-diagnosis-task`
**Created**: 2026-01-01
**Status**: Draft
**Input**: User description: "诊断页面，执行诊断任务的时候，先在当前启动中创建一个诊断任务，这个诊断任务和拓扑图相关，另外，诊断任务中要保存诊断过程，诊断过程是由agent的诊断过程组成，每个agent的诊断过程也要记录下来，一个拓扑图的诊断任务与多个agent的诊断过程关联。调用executor获得流式响应，异步存储到redis中，待诊断任务结束后，异步读取redis中的所有stream，按照agent维度，整理成完整的文本，保存到数据库，保存到数据库也是按照agent维度。"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 创建诊断任务 (Priority: P1)

用户在诊断页面选择一个拓扑图并输入诊断问题后，系统创建一个新的诊断任务记录，关联到该拓扑图，并开始执行诊断流程。

**Why this priority**: 这是整个功能的入口点，没有诊断任务记录就无法追踪诊断过程和结果。

**Independent Test**: 可以通过创建诊断任务并验证数据库中存在对应记录来独立测试，即使后续流程未实现也能验证任务创建功能。

**Acceptance Scenarios**:

1. **Given** 用户已选择拓扑图（ID=43）并输入诊断问题"分析性能瓶颈", **When** 用户点击执行诊断, **Then** 系统创建诊断任务记录，状态为"运行中"，并返回任务ID
2. **Given** 诊断任务正在创建, **When** 创建成功, **Then** 诊断任务记录包含拓扑图ID、用户问题、创建时间、任务状态
3. **Given** 用户输入为空, **When** 用户尝试执行诊断, **Then** 系统提示"请输入诊断问题"

---

### User Story 2 - 实时记录Agent诊断过程 (Priority: P1)

当诊断任务执行时，系统实时接收executor返回的流式响应，并按Agent维度暂存诊断过程数据，便于后续整理。

**Why this priority**: 这是核心数据采集功能，没有实时记录就无法追踪每个Agent的诊断贡献。

**Independent Test**: 可以通过模拟executor流式响应，验证系统是否正确按Agent维度暂存数据。

**Acceptance Scenarios**:

1. **Given** 诊断任务正在执行，executor返回Agent A的诊断输出, **When** 系统接收到流式事件, **Then** 系统将该事件暂存并关联到Agent A
2. **Given** 多个Agent同时产生诊断输出, **When** 系统接收到多个流式事件, **Then** 系统按Agent ID正确分类暂存每个事件
3. **Given** executor返回错误事件, **When** 系统接收到错误, **Then** 系统记录错误信息并关联到对应的Agent

---

### User Story 3 - 诊断完成后持久化Agent诊断过程 (Priority: P1)

当诊断任务结束后，系统将暂存的流式数据按Agent维度整理成完整文本，并持久化保存到数据库。

**Why this priority**: 这是数据持久化的核心功能，确保诊断过程可被查询和回溯。

**Independent Test**: 可以通过预先填充暂存数据，然后触发持久化流程，验证数据库中是否正确保存按Agent维度整理的诊断过程。

**Acceptance Scenarios**:

1. **Given** 诊断任务已完成，暂存中有Agent A、B、C的诊断流式数据, **When** 系统执行持久化, **Then** 数据库中创建3条Agent诊断过程记录，分别对应Agent A、B、C
2. **Given** Agent A产生了多个流式事件（event1, event2, event3）, **When** 系统整理Agent A的诊断过程, **Then** 系统按时间顺序将事件内容拼接成完整文本
3. **Given** 诊断任务状态为"运行中", **When** 所有Agent诊断过程持久化完成, **Then** 诊断任务状态更新为"已完成"

---

### User Story 4 - 查询诊断任务历史 (Priority: P2)

用户可以查看某个拓扑图的历史诊断任务列表，并查看每个任务的详细诊断过程。

**Why this priority**: 这是用户回溯诊断历史的功能，依赖于前面的数据持久化功能。

**Independent Test**: 可以通过预先创建诊断任务记录，然后调用查询接口验证返回结果。

**Acceptance Scenarios**:

1. **Given** 拓扑图43有3个历史诊断任务, **When** 用户查询该拓扑图的诊断历史, **Then** 系统返回3个任务记录，按创建时间倒序排列
2. **Given** 用户选择查看某个诊断任务详情, **When** 系统加载任务详情, **Then** 显示所有参与该任务的Agent及其各自的诊断过程文本

---

### User Story 5 - 诊断任务异常处理 (Priority: P2)

当诊断任务执行过程中发生错误（如executor连接失败、超时等），系统正确记录错误状态并通知用户。

**Why this priority**: 错误处理是系统健壮性的保障，但不是核心功能流程。

**Independent Test**: 可以通过模拟executor错误响应，验证系统是否正确更新任务状态并记录错误信息。

**Acceptance Scenarios**:

1. **Given** 诊断任务正在执行, **When** executor服务连接失败, **Then** 诊断任务状态更新为"失败"，错误信息记录到任务中
2. **Given** 诊断任务执行超时（超过配置的最大时间）, **When** 达到超时时间, **Then** 系统终止任务，状态更新为"超时"
3. **Given** 诊断任务失败, **When** 用户查看任务详情, **Then** 显示失败原因和已完成的部分诊断过程

---

### Edge Cases

- 诊断任务创建后executor服务不可用时，任务状态应为"失败"，而非永远停留在"运行中"
- 同一拓扑图同时发起多个诊断任务时，每个任务应独立处理，互不干扰
- 暂存数据在持久化前系统崩溃时，应有机制防止数据丢失或能够重试
- Agent无诊断输出（空结果）时，仍应创建Agent诊断过程记录，内容标记为"无输出"
- 诊断过程文本过长时，应有合理的截断或分段存储策略

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统必须在用户触发诊断时创建诊断任务记录，包含拓扑图ID、用户问题、创建时间、任务状态
- **FR-002**: 系统必须为诊断任务生成唯一标识符，用于追踪任务全生命周期
- **FR-003**: 诊断任务状态必须包含：运行中、已完成、失败、超时
- **FR-004**: 系统必须实时接收executor的流式响应，并按 `agent_bound_id` 分类暂存
- **FR-005**: 系统必须在诊断任务结束后，异步将暂存数据按Agent维度整理并持久化
- **FR-006**: 每个Agent诊断过程记录必须包含：Agent绑定ID、Agent名称、诊断内容、开始时间、结束时间
- **FR-007**: 系统必须支持按拓扑图ID查询历史诊断任务列表
- **FR-008**: 系统必须支持查询单个诊断任务的详情，包括所有Agent的诊断过程
- **FR-009**: 系统必须在executor服务错误或超时时更新任务状态为失败/超时
- **FR-010**: 诊断任务超时时间默认为10分钟，可配置

### Key Entities

- **诊断任务（DiagnosisTask）**:
  - 代表一次诊断执行的记录
  - 关联到一个拓扑图
  - 包含用户问题、状态、创建时间、完成时间、错误信息
  - 一个诊断任务关联多个Agent诊断过程

- **Agent诊断过程（AgentDiagnosisProcess）**:
  - 代表单个Agent在一次诊断任务中的执行过程
  - 关联到一个诊断任务和一个Agent绑定关系
  - 包含Agent名称、诊断内容（整理后的完整文本）、开始时间、结束时间
  - 一个诊断任务可有多个Agent诊断过程

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 用户触发诊断后，诊断任务记录在1秒内创建完成
- **SC-002**: 流式事件暂存延迟不超过100毫秒，确保实时性
- **SC-003**: 诊断任务完成后，Agent诊断过程在5秒内完成持久化
- **SC-004**: 用户可以查询到过去30天内的所有诊断任务历史
- **SC-005**: 诊断任务详情页能在2秒内加载完成，包括所有Agent诊断过程
- **SC-006**: 系统支持单个诊断任务关联至少20个Agent的诊断过程
- **SC-007**: 诊断任务失败时，用户能在界面上看到明确的错误原因

## Clarifications

### Session 2026-01-05

- Q: executor流式响应中如何标识每个Agent？ → A: 使用 `agent_bound_id`（Agent绑定ID）标识
- Q: 流式数据暂存是否必须使用Redis？ → A: 必须使用Redis暂存
- Q: Redis暂存数据的过期时间（TTL）应设为多少？ → A: 24小时

## Assumptions

- 暂存机制必须使用Redis，用于流式数据的临时存储和系统崩溃恢复，TTL设为24小时
- Agent绑定ID（agent_bound.id）作为追溯Agent信息的唯一标识
- 诊断过程文本无硬性长度限制，但建议单个Agent诊断过程不超过100KB
- 用户有权限查看其创建的诊断任务及其所属拓扑图的所有历史任务
- 诊断任务历史默认保留30天，可根据需求调整
