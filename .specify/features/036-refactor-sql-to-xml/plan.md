# SQL 重构计划：从 Annotation + Lambda 迁移到 mapper.xml

## 1. 背景与目标

### 1.1 当前状况

| 实现方式 | 数量 | 说明 |
|---------|------|------|
| @Select 注解 | 61 处 | 分布在 13 个 Mapper 接口中 |
| @Update 注解 | 3 处 | NodeAgentRelationMapper |
| @Delete 注解 | 5 处 | Node2NodeMapper, Topology2NodeMapper, TopologyReportTemplateMapper |
| LambdaQueryWrapper | 8 处 | 分布在 6 个 RepositoryImpl 中 |
| LambdaUpdateWrapper | 5 处 | 分布在 5 个 RepositoryImpl 中 |
| mapper.xml 文件 | 0 个 | 完全未使用 |

### 1.2 重构目标

1. **统一 SQL 管理**：所有 SQL 语句迁移到 mapper.xml 文件
2. **移除注解 SQL**：删除所有 @Select/@Insert/@Update/@Delete 注解
3. **消除 Lambda**：将 LambdaQueryWrapper/LambdaUpdateWrapper 替换为 XML 中定义的 SQL
4. **保持 BaseMapper**：保留 MyBatis-Plus 的 BaseMapper 继承，利用其 CRUD 基础方法

---

## 2. 影响范围分析

### 2.1 需要创建的 mapper.xml 文件（13 个）

| 模块 | XML 文件 | 对应 Mapper | SQL 数量 |
|------|----------|-------------|----------|
| agent | AgentMapper.xml | AgentMapper.java | 11 |
| node | NodeMapper.xml | NodeMapper.java | 5 |
| node | NodeTypeMapper.xml | NodeTypeMapper.java | 2 |
| node | NodeAgentRelationMapper.xml | NodeAgentRelationMapper.java | 6 |
| node | Node2NodeMapper.xml | Node2NodeMapper.java | 12 |
| topology | TopologyMapper.xml | TopologyMapper.java | 3 |
| topology | Topology2NodeMapper.xml | Topology2NodeMapper.java | 6 |
| topology | TopologyReportTemplateMapper.xml | TopologyReportTemplateMapper.java | 6 |
| prompt | PromptTemplateMapper.xml | PromptTemplateMapper.java | 4 |
| prompt | PromptTemplateVersionMapper.xml | PromptTemplateVersionMapper.java | 5 |
| prompt | TemplateUsageMapper.xml | TemplateUsageMapper.java | 3 |
| report | ReportMapper.xml | ReportMapper.java | 2 |
| report | ReportTemplateMapper.xml | ReportTemplateMapper.java | 4 |

### 2.2 需要修改的 RepositoryImpl 文件（10 个）

| 文件 | Lambda 类型 | 使用次数 | 需新增 Mapper 方法 |
|------|-------------|----------|-------------------|
| NodeRepositoryImpl.java | LambdaQueryWrapper | 1 | findExistingIds |
| TemplateUsageRepositoryImpl.java | LambdaQueryWrapper | 1 | selectPage |
| NodeAgentRelationRepositoryImpl.java | LambdaQueryWrapper | 2 | selectByNodeIdAndAgentId, selectCountByNodeId |
| TopologyRepositoryImpl.java | LambdaQueryWrapper, LambdaUpdateWrapper | 2 | countByCondition, updateGlobalSupervisorAgentId |
| PromptTemplateRepositoryImpl.java | LambdaUpdateWrapper | 1 | softDeleteById |
| ReportRepositoryImpl.java | LambdaUpdateWrapper | 1 | softDeleteById |
| ReportTemplateRepositoryImpl.java | LambdaUpdateWrapper | 1 | softDeleteById |
| AgentRepositoryImpl.java | LambdaUpdateWrapper | 1 | softDeleteById |
| Topology2NodeRepositoryImpl.java | LambdaQueryWrapper | 3 | countByTopologyId, selectByTopologyIdAndNodeId, deleteByTopologyIdAndNodeIds |

---

## 3. 执行计划

### Phase 1: 基础设施准备

#### Task 1.1: 配置 MyBatis mapper.xml 扫描路径
- 修改 `application.yml` 添加 mapper-locations 配置
- 创建 resources/mapper 目录结构

```yaml
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
```

#### Task 1.2: 创建目录结构
```
infrastructure/repository/mysql-impl/src/main/resources/
└── mapper/
    ├── agent/
    ├── node/
    ├── topology/
    ├── prompt/
    └── report/
```

### Phase 2: 核心模块重构（优先级高）

#### Task 2.1: Agent 模块
1. 创建 `AgentMapper.xml`（11 个 SQL）
2. 修改 `AgentMapper.java`，移除 @Select 注解
3. 修改 `AgentRepositoryImpl.java`，移除 LambdaUpdateWrapper

#### Task 2.2: Node 模块
1. 创建 `NodeMapper.xml`（5 个 SQL）
2. 创建 `NodeTypeMapper.xml`（2 个 SQL）
3. 创建 `NodeAgentRelationMapper.xml`（6 个 SQL）
4. 创建 `Node2NodeMapper.xml`（12 个 SQL）
5. 修改对应 Mapper.java 和 RepositoryImpl.java

#### Task 2.3: Topology 模块
1. 创建 `TopologyMapper.xml`（3 个 SQL）
2. 创建 `Topology2NodeMapper.xml`（6 个 SQL）
3. 创建 `TopologyReportTemplateMapper.xml`（6 个 SQL）
4. 修改对应 Mapper.java 和 RepositoryImpl.java

### Phase 3: 辅助模块重构

#### Task 3.1: Prompt 模块
1. 创建 `PromptTemplateMapper.xml`（4 个 SQL）
2. 创建 `PromptTemplateVersionMapper.xml`（5 个 SQL）
3. 创建 `TemplateUsageMapper.xml`（3 个 SQL）
4. 修改对应 Mapper.java 和 RepositoryImpl.java

#### Task 3.2: Report 模块
1. 创建 `ReportMapper.xml`（2 个 SQL）
2. 创建 `ReportTemplateMapper.xml`（4 个 SQL）
3. 修改对应 Mapper.java 和 RepositoryImpl.java

### Phase 4: 验证与清理

#### Task 4.1: 编译验证
- 执行 `mvn clean compile` 确保无编译错误

#### Task 4.2: 单元测试
- 运行所有现有测试确保功能不变

#### Task 4.3: 集成测试
- 启动应用验证 API 功能正常

#### Task 4.4: 代码清理
- 移除未使用的 import
- 检查并移除废弃代码

---

## 4. XML 模板示例

### 4.1 基础结构

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.catface996.aiops.repository.mysql.mapper.agent.AgentMapper">

    <!-- 公共列定义 -->
    <sql id="Base_Column_List">
        id, name, role, specialty, prompt_template_id, model, temperature,
        top_p, max_tokens, max_runtime, warnings, critical,
        created_at, updated_at, deleted
    </sql>

    <!-- 查询方法 -->
    <select id="selectByName" resultType="com.catface996.aiops.repository.mysql.po.agent.AgentPO">
        SELECT <include refid="Base_Column_List"/>
        FROM agent
        WHERE name = #{name} AND deleted = 0
        LIMIT 1
    </select>

    <!-- 动态查询 -->
    <select id="selectPageByCondition" resultType="com.catface996.aiops.repository.mysql.po.agent.AgentPO">
        SELECT <include refid="Base_Column_List"/>
        FROM agent
        <where>
            deleted = 0
            <if test="role != null and role != ''">
                AND role = #{role}
            </if>
            <if test="keyword != null and keyword != ''">
                AND (name LIKE CONCAT('%', #{keyword}, '%')
                     OR specialty LIKE CONCAT('%', #{keyword}, '%'))
            </if>
        </where>
        ORDER BY created_at DESC
    </select>

    <!-- 更新方法 -->
    <update id="softDeleteById">
        UPDATE agent
        SET deleted = 1, updated_at = #{updatedAt}
        WHERE id = #{id} AND deleted = 0
    </update>

</mapper>
```

### 4.2 Mapper 接口风格（重构后）

```java
@Mapper
public interface AgentMapper extends BaseMapper<AgentPO> {

    /**
     * 根据名称查询 Agent
     */
    AgentPO selectByName(@Param("name") String name);

    /**
     * 分页查询 Agent 列表
     */
    IPage<AgentPO> selectPageByCondition(Page<AgentPO> page,
                                          @Param("role") String role,
                                          @Param("keyword") String keyword);

    /**
     * 软删除
     */
    int softDeleteById(@Param("id") Long id, @Param("updatedAt") LocalDateTime updatedAt);
}
```

---

## 5. 宪法修订建议

重构完成后，建议在 `constitution.md` 中添加以下规则：

```markdown
### SQL 实现规范

- **禁止使用 @Select/@Insert/@Update/@Delete 注解编写 SQL**
- **禁止在 RepositoryImpl 中使用 LambdaQueryWrapper/LambdaUpdateWrapper**
- **所有 SQL 必须在 mapper.xml 文件中定义**
- **可以继承 MyBatis-Plus BaseMapper 使用其基础 CRUD 方法**
```

---

## 6. 风险与注意事项

### 6.1 风险点
| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| SQL 迁移出错 | 功能异常 | 逐个迁移，每次迁移后验证 |
| XML namespace 错误 | 启动失败 | 使用全限定类名，仔细检查 |
| resultType 不匹配 | 查询结果映射失败 | 使用 PO 全路径类名 |
| 动态 SQL 语法差异 | 查询条件错误 | 注意 `<script>` 标签内外语法差异 |

### 6.2 回滚方案
- 每个 Phase 完成后创建 Git tag
- 保留原有代码直到新代码验证通过
- 遇到无法解决的问题可回退到上一个 tag

---

## 7. 工作量估算

| Phase | 任务数 | 文件数 | 预计复杂度 |
|-------|--------|--------|-----------|
| Phase 1 | 2 | 2 | 低 |
| Phase 2 | 3 | 15 | 高 |
| Phase 3 | 2 | 9 | 中 |
| Phase 4 | 4 | - | 低 |
| **合计** | **11** | **26+** | - |

---

## 8. 验收标准

- [ ] 所有 Mapper 接口中无 @Select/@Insert/@Update/@Delete 注解
- [ ] 所有 RepositoryImpl 中无 LambdaQueryWrapper/LambdaUpdateWrapper
- [ ] 所有 SQL 在 mapper.xml 中有对应定义
- [ ] `mvn clean compile` 编译通过
- [ ] 所有单元测试通过
- [ ] API 功能验证通过
- [ ] 宪法规则已更新
