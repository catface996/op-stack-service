# Quickstart: SQL 重构指南

## 快速开始

### 1. 配置 MyBatis mapper-locations

在 `bootstrap/src/main/resources/application.yml` 中添加：

```yaml
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
```

### 2. 创建 XML 目录结构

```bash
mkdir -p infrastructure/repository/mysql-impl/src/main/resources/mapper/{agent,node,topology,prompt,report}
```

### 3. 迁移模板

#### 3.1 创建 XML 文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.catface996.aiops.repository.mysql.mapper.agent.AgentMapper">

    <sql id="Base_Column_List">
        id, name, role, specialty, prompt_template_id, model, temperature,
        top_p, max_tokens, max_runtime, warnings, critical,
        created_at, updated_at, deleted
    </sql>

    <!-- 从 @Select 迁移的查询 -->
    <select id="selectByName" resultType="com.catface996.aiops.repository.mysql.po.agent.AgentPO">
        SELECT <include refid="Base_Column_List"/>
        FROM agent
        WHERE name = #{name} AND deleted = 0
        LIMIT 1
    </select>

</mapper>
```

#### 3.2 修改 Mapper 接口

```java
// Before
@Select("SELECT * FROM agent WHERE name = #{name} AND deleted = 0 LIMIT 1")
AgentPO selectByName(@Param("name") String name);

// After
AgentPO selectByName(@Param("name") String name);
```

#### 3.3 替换 Lambda（在 RepositoryImpl 中）

```java
// Before
LambdaQueryWrapper<TopologyPO> wrapper = new LambdaQueryWrapper<>();
wrapper.like(TopologyPO::getName, name);
wrapper.eq(TopologyPO::getStatus, status);
return topologyMapper.selectCount(wrapper);

// After
return topologyMapper.countByCondition(name, status != null ? status.name() : null);
```

对应 XML：
```xml
<select id="countByCondition" resultType="long">
    SELECT COUNT(*) FROM topology
    <where>
        deleted = 0
        <if test="name != null and name != ''">
            AND name LIKE CONCAT('%', #{name}, '%')
        </if>
        <if test="status != null and status != ''">
            AND status = #{status}
        </if>
    </where>
</select>
```

## 验证步骤

### 1. 编译检查
```bash
mvn clean compile -pl infrastructure/repository/mysql-impl
```

### 2. 启动应用
```bash
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

### 3. 功能验证

测试关键 API 确保功能正常：
- Agent 列表查询
- Node 条件查询
- Topology 分页查询

## 常见问题

### Q1: XML 找不到

**症状**: `Invalid bound statement (not found)`

**解决**:
1. 检查 namespace 是否与 Mapper 接口全限定名一致
2. 检查 mapper-locations 配置是否正确
3. 确认 XML 文件位于正确目录

### Q2: 参数映射错误

**症状**: `Parameter 'xxx' not found`

**解决**:
- 单参数方法必须使用 `@Param` 注解
- 检查 XML 中的 `#{paramName}` 是否与 `@Param` 值一致

### Q3: 结果映射失败

**症状**: 返回对象字段为 null

**解决**:
1. 确认 `map-underscore-to-camel-case: true` 已配置
2. 或使用 AS 别名：`SELECT node_type_id AS nodeTypeId`

## 迁移清单

| 模块 | XML 文件 | SQL 数量 | 状态 |
|------|----------|----------|------|
| agent | AgentMapper.xml | 11 | [ ] |
| node | NodeMapper.xml | 5 | [ ] |
| node | NodeTypeMapper.xml | 2 | [ ] |
| node | NodeAgentRelationMapper.xml | 6 | [ ] |
| node | Node2NodeMapper.xml | 12 | [ ] |
| topology | TopologyMapper.xml | 3 | [ ] |
| topology | Topology2NodeMapper.xml | 6 | [ ] |
| topology | TopologyReportTemplateMapper.xml | 6 | [ ] |
| prompt | PromptTemplateMapper.xml | 4 | [ ] |
| prompt | PromptTemplateVersionMapper.xml | 5 | [ ] |
| prompt | TemplateUsageMapper.xml | 3 | [ ] |
| report | ReportMapper.xml | 2 | [ ] |
| report | ReportTemplateMapper.xml | 4 | [ ] |
