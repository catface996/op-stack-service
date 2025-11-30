# F03 资源管理功能 - 实施任务清单

**功能名称**: F03 - 创建和管理IT资源  
**文档版本**: v2.0  
**创建日期**: 2024-11-30  
**预计工作量**: 12人天（2周Sprint）  
**状态**: 规划中 📋

---

## 任务概览

**总任务数**: 15个核心任务  
**预计工期**: 2周（10个工作日）  
**推荐团队**: 2名后端开发

---

## 任务执行说明

### 任务标记说明

- `[ ]` - 未开始
- `[-]` - 进行中
- `[x]` - 已完成

### 验证方法优先级

1. 【运行时验证】- 最高优先级，启动应用实际测试功能
2. 【单元测试】- 次优先级，执行自动化测试用例
3. 【构建验证】- 第三优先级，执行构建命令验证编译
4. 【静态检查】- 最后手段，检查文件和代码存在

---

## 实施任务清单

- [x] 1. 创建数据库表结构和初始化数据 ✅
  - 创建resource_type、resource、resource_tag、resource_audit_log四张表
  - 创建所有必要的索引（idx_name, idx_type, idx_created_at等）
  - 插入6种预置资源类型数据
  - **验证方法**: 【运行时验证】启动应用，调用GET /api/v1/resource-types，验证返回6种类型
  - _需求: REQ-FR-001, REQ-FR-026_
  - **验证结果**: 2024-11-30 ✅
    - Flyway迁移V3成功执行
    - 4张表创建成功：resource_type, resource, resource_tag, resource_audit_log
    - 6种资源类型数据插入成功：SERVER, APPLICATION, DATABASE, API, MIDDLEWARE, REPORT
    - 数据库验证通过

- [x] 2. 实现领域模型和Repository接口 ✅
  - 实现Resource聚合根、ResourceType实体、ResourceStatus枚举
  - 定义ResourceRepository、ResourceTypeRepository、ResourceTagRepository、AuditLogRepository接口
  - 定义Repository层的Entity类
  - **验证方法**: 【构建验证】执行mvn clean compile，确认编译成功
  - _需求: REQ-FR-001~028_
  - **验证结果**: 2025-11-30 ✅
    - 领域模型已创建：
      - Resource.java（聚合根）
      - ResourceType.java（实体）
      - ResourceTag.java（实体）
      - ResourceAuditLog.java（实体）
      - ResourceStatus.java（枚举）
      - OperationType.java（枚举）
    - Repository接口已定义：
      - ResourceRepository.java
      - ResourceTypeRepository.java
      - ResourceTagRepository.java
      - ResourceAuditLogRepository.java
    - 构建验证通过：mvn clean compile BUILD SUCCESS

- [ ] 3. 实现Repository数据访问层
  - 使用MyBatis-Plus实现4个Repository
  - 支持CRUD操作和复杂查询（分页、过滤、排序）
  - 创建MyBatis Mapper接口和XML
  - **验证方法**: 【构建验证】执行mvn clean compile，确认编译成功
  - _需求: REQ-FR-001~020_

- [ ] 4. 实现AES-256加密服务
  - 实现EncryptionService接口和AesEncryptionServiceImpl
  - 支持encrypt()和decrypt()方法
  - 配置加密密钥和IV
  - **验证方法**: 【单元测试】测试加密解密功能，验证加密后可正确解密
  - _需求: REQ-FR-004, REQ-FR-014, REQ-NFR-008_

- [ ] 5. 实现权限验证切面
  - 实现@RequireOwnerPermission注解
  - 实现ResourcePermissionAspect切面
  - 实现权限检查逻辑
  - **验证方法**: 【运行时验证】非Owner用户尝试编辑资源，验证返回403错误
  - _需求: REQ-FR-011, REQ-FR-012, REQ-FR-016, REQ-NFR-009_

- [ ] 6. 实现Redis缓存服务
  - 实现ResourceCacheService接口和实现类
  - 支持资源详情缓存（5分钟TTL）
  - 支持资源列表缓存（前3页，5分钟TTL）
  - 支持缓存清除操作
  - **验证方法**: 【运行时验证】查询资源详情两次，第二次查询时间明显缩短
  - _需求: REQ-NFR-001, REQ-NFR-003_

- [ ] 7. 实现审计日志服务
  - 实现AuditLogService接口和实现类
  - 支持CREATE、UPDATE、DELETE、STATUS_CHANGE四种操作记录
  - 支持分页查询审计日志
  - **验证方法**: 【运行时验证】创建资源后，查询审计日志，验证CREATE操作被记录
  - _需求: REQ-FR-020, REQ-FR-025, REQ-FR-028, REQ-NFR-010_

- [ ] 8. 实现ResourceDomainService核心业务逻辑
  - 实现createResource()方法：验证、加密、保存、审计
  - 实现listResources()和getResourceById()方法：缓存、查询、分页
  - 实现updateResource()方法：权限检查、乐观锁、加密、审计
  - 实现deleteResource()方法：权限检查、关联检查、名称确认、物理删除
  - 实现updateResourceStatus()方法：状态更新、审计
  - 实现checkOwnerPermission()和getAuditLogs()方法
  - **验证方法**: 【运行时验证】调用创建资源API，验证资源创建成功且敏感信息已加密
  - _需求: REQ-FR-001~028_

- [ ] 9. 实现ResourceApplicationService应用服务
  - 定义完整的Command、Query、DTO类
  - 实现所有应用服务方法，包含事务控制
  - 实现DTO转换逻辑
  - **验证方法**: 【构建验证】执行mvn clean compile，确认编译成功
  - _需求: REQ-FR-001~028_

- [ ] 10. 实现ResourceController REST API
  - 实现POST /api/v1/resources - 创建资源
  - 实现GET /api/v1/resources - 查询资源列表
  - 实现GET /api/v1/resources/{id} - 查询资源详情
  - 实现PUT /api/v1/resources/{id} - 更新资源
  - 实现DELETE /api/v1/resources/{id} - 删除资源
  - 实现PATCH /api/v1/resources/{id}/status - 更新资源状态
  - 实现GET /api/v1/resources/{id}/audit-logs - 查询审计日志
  - 实现GET /api/v1/resource-types - 查询资源类型列表
  - **验证方法**: 【运行时验证】启动应用，逐一调用8个API端点，验证功能正常
  - _需求: REQ-FR-001~028_

- [ ] 11. 实现全局异常处理
  - 定义业务异常类（ResourceNotFoundException等）
  - 实现GlobalExceptionHandler处理各种异常
  - 返回统一的错误响应格式
  - **验证方法**: 【运行时验证】触发业务异常，验证返回统一错误格式
  - _需求: 所有功能性需求_

- [ ] 12. 完善配置和监控
  - 配置多环境（local, dev, test, prod）
  - 配置数据库连接池、Redis连接
  - 配置加密密钥和日志
  - 实现性能监控切面和健康检查
  - **验证方法**: 【运行时验证】使用不同环境配置启动应用，验证配置生效
  - _需求: REQ-NFR-011, REQ-NFR-014_

- [ ] 13. 性能测试和优化
  - 准备10000条测试数据
  - 测试列表查询性能（<1秒）
  - 测试搜索性能（<500ms）
  - 测试详情页加载（<500ms）
  - 测试创建资源（<200ms）
  - 测试并发场景（100并发用户）
  - **验证方法**: 【运行时验证】使用JMeter或类似工具进行性能测试
  - _需求: REQ-NFR-001~007_

- [ ] 14. 完整功能验证
  - 执行完整的资源生命周期测试（创建→查询→更新→删除）
  - 测试并发场景，验证乐观锁正常工作
  - 测试权限验证，验证非Owner无法编辑/删除
  - 验证所有审计日志正确记录
  - 验证缓存机制提升查询性能
  - **验证方法**: 【运行时验证】执行完整的功能测试用例
  - _需求: REQ-FR-001~028, REQ-NFR-001~014_

- [ ] 15. 编写单元测试和集成测试
  - 编写Domain Service单元测试（覆盖率≥70%）
  - 编写Application Service单元测试
  - 编写Repository层单元测试
  - 编写完整链路集成测试
  - **验证方法**: 【单元测试】执行mvn test，验证所有测试通过
  - _需求: REQ-NFR-013_

---

## 任务依赖关系

```
任务1（数据库+初始化）
  ↓
任务2（领域模型+接口）
  ↓
任务3（Repository实现）
  ↓
任务4-7（基础设施：加密、权限、缓存、审计）
  ↓
任务8（Domain Service）
  ↓
任务9（Application Service）
  ↓
任务10（Controller）
  ↓
任务11（异常处理）
  ↓
任务12（配置监控）
  ↓
任务13（性能测试）
  ↓
任务14（功能验证）
  ↓
任务15（单元测试）
```

---

## 工作量分配建议

### 后端开发1（约48小时）
- 任务1-3：数据库、领域模型、Repository（12小时）
- 任务8：Domain Service实现（16小时）
- 任务9：Application Service实现（6小时）
- 任务14：功能验证（6小时）
- 任务15：单元测试（8小时）

### 后端开发2（约48小时）
- 任务4-7：基础设施实现（12小时）
- 任务10：Controller实现（10小时）
- 任务11：异常处理（4小时）
- 任务12：配置监控（6小时）
- 任务13：性能测试（8小时）
- 协助任务14-15（8小时）

---

## 风险提示

| 任务 | 风险 | 缓解措施 |
|------|------|---------|
| 任务3 | 列表查询性能 | 使用索引+缓存，任务13性能测试验证 |
| 任务4 | 加密性能影响 | 只加密必要字段，任务13性能测试验证 |
| 任务8 | 业务逻辑复杂 | 充分运行时验证，代码审查 |
| 任务13 | 性能不达标 | 提前进行性能测试，预留优化时间 |

---

## 完成标准

### 功能完成标准
- ✅ 所有REST API端点正常工作
- ✅ 所有功能性需求实现
- ✅ 所有非功能性需求满足

### 质量完成标准
- ✅ 运行时验证全部通过
- ✅ 性能测试达标
- ✅ 代码审查通过
- ✅ 无严重Bug

### 交付完成标准
- ✅ 代码提交到代码库
- ✅ 数据库脚本提交
- ✅ 配置文件完整
- ✅ 可运行的应用程序
- ✅ API文档完整

---

**任务清单创建日期**: 2024-11-30  
**预计开始日期**: 待定  
**预计完成日期**: 开始后2周  
**状态**: ✅ 任务列表已创建，等待验证
