# Requirements Checklist: Report Management

## Report APIs (P0/P1)

- [ ] **FR-001**: POST /api/service/v1/reports/list - 分页查询报告列表
- [ ] **FR-002**: 支持按 type 筛选报告
- [ ] **FR-003**: 支持按 status 筛选报告
- [ ] **FR-004**: 支持按 keyword 搜索报告（title, summary, tags）
- [ ] **FR-005**: 支持 sort_by 和 sort_order 排序
- [ ] **FR-006**: POST /api/service/v1/reports/get - 获取报告详情
- [ ] **FR-007**: POST /api/service/v1/reports/create - 创建报告
- [ ] **FR-008**: POST /api/service/v1/reports/delete - 删除报告
- [ ] **FR-009**: 报告创建后不可修改（无更新 API）

## Report Template APIs (P2)

- [ ] **FR-010**: POST /api/service/v1/report-templates/list - 分页查询模板列表
- [ ] **FR-011**: 支持按 category 筛选模板
- [ ] **FR-012**: 支持按 keyword 搜索模板
- [ ] **FR-013**: POST /api/service/v1/report-templates/get - 获取模板详情
- [ ] **FR-014**: POST /api/service/v1/report-templates/create - 创建模板
- [ ] **FR-015**: POST /api/service/v1/report-templates/update - 更新模板
- [ ] **FR-016**: POST /api/service/v1/report-templates/delete - 删除模板

## Data Validation

- [ ] **FR-017**: Report type 枚举校验: Diagnosis, Audit, Performance, Security
- [ ] **FR-018**: Report status 枚举校验: Draft, Final, Archived
- [ ] **FR-019**: Template category 枚举校验: Incident, Performance, Security, Audit
- [ ] **FR-020**: title 最大 200 字符
- [ ] **FR-021**: summary 最大 500 字符
- [ ] **FR-022**: template name 最大 100 字符
- [ ] **FR-023**: template description 最大 500 字符

## Association Validation

- [ ] **FR-024**: 创建报告时校验 topology_id 存在性（如提供）

## Future (P3)

- [ ] **FR-025**: POST /api/service/v1/reports/generate - 自动生成报告（异步）

## Success Criteria

- [ ] **SC-001**: P0 API（list, get）正常工作
- [ ] **SC-002**: P1 API（create, delete）正常工作
- [ ] **SC-003**: P2 API（模板 CRUD）正常工作
- [ ] **SC-004**: 响应格式符合 Result<T> 和 PageResult<T>
- [ ] **SC-005**: 分页接口符合 pagination protocol
- [ ] **SC-006**: 所有 API 遵循 POST-Only 设计
- [ ] **SC-007**: SpringDoc OpenAPI 文档正确生成
