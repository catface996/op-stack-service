# Research: MyBatis XML 配置与迁移最佳实践

## 1. MyBatis-Plus XML 配置

### Decision: 使用 classpath*:mapper/**/*.xml 配置

**Rationale**:
- MyBatis-Plus 默认不扫描 XML 文件，需要显式配置 mapper-locations
- 使用 `classpath*:` 前缀支持多模块项目
- 使用 `**/*.xml` 通配符支持子目录

**Alternatives considered**:
- `classpath:mapper/*.xml` - 不支持子目录，rejected
- 在 @MapperScan 中配置 - 不够灵活，rejected

**Configuration**:
```yaml
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
```

## 2. XML 文件结构规范

### Decision: 使用标准 MyBatis XML 结构

**Rationale**:
- 符合 MyBatis 3.0 DTD 规范
- IDE 支持良好（语法高亮、自动补全）
- 团队熟悉度高

**Template**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="全限定类名">
    <!-- SQL 片段 -->
    <sql id="Base_Column_List">...</sql>

    <!-- 查询 -->
    <select id="方法名" resultType="PO全限定类名">...</select>

    <!-- 更新 -->
    <update id="方法名">...</update>

    <!-- 删除 -->
    <delete id="方法名">...</delete>
</mapper>
```

## 3. 动态 SQL 迁移

### Decision: 使用 XML 原生动态标签替代 <script>

**Rationale**:
- 注解中的 `<script>` 标签在 XML 中不需要
- XML 原生支持 `<if>`, `<where>`, `<foreach>` 等标签
- 可读性更好

**Migration Pattern**:

| 注解写法 | XML 写法 |
|---------|---------|
| `@Select("<script>SELECT...`)` | `<select id="...">SELECT...` |
| `<if test='xx != null'>` | `<if test="xx != null">` (双引号) |
| `#{item}` 在 foreach 中 | 保持不变 |

**Example**:
```java
// Before (Annotation)
@Select("<script>" +
    "SELECT * FROM agent " +
    "<where>" +
    "deleted = 0 " +
    "<if test='role != null'>" +
    "AND role = #{role} " +
    "</if>" +
    "</where>" +
    "</script>")
```

```xml
<!-- After (XML) -->
<select id="selectByCondition" resultType="AgentPO">
    SELECT * FROM agent
    <where>
        deleted = 0
        <if test="role != null">
            AND role = #{role}
        </if>
    </where>
</select>
```

## 4. Lambda 替换策略

### Decision: 为每个 Lambda 使用场景创建对应的 XML SQL

**Rationale**:
- 保持 SQL 集中管理
- 便于 SQL 审查和优化
- 符合规范要求

**Lambda 类型与替换方案**:

| Lambda 类型 | 用途 | XML 替换 |
|------------|------|----------|
| LambdaQueryWrapper.eq() | 等值查询 | `<select>` with `WHERE field = #{value}` |
| LambdaQueryWrapper.like() | 模糊查询 | `<select>` with `WHERE field LIKE CONCAT('%', #{value}, '%')` |
| LambdaQueryWrapper.in() | IN 查询 | `<select>` with `<foreach>` |
| LambdaUpdateWrapper.set() | 更新字段 | `<update>` with `SET field = #{value}` |
| selectCount(wrapper) | 条件计数 | `<select>` 返回 COUNT(*) |

## 5. 分页查询处理

### Decision: 保留 MyBatis-Plus 分页插件，XML 中只写查询 SQL

**Rationale**:
- MyBatis-Plus 分页插件自动处理 LIMIT/OFFSET
- XML 中不需要写分页 SQL
- IPage 参数自动生效

**Pattern**:
```java
// Mapper 接口
IPage<AgentPO> selectPageByCondition(Page<AgentPO> page, @Param("role") String role);
```

```xml
<!-- XML - 不需要写 LIMIT -->
<select id="selectPageByCondition" resultType="AgentPO">
    SELECT * FROM agent
    WHERE deleted = 0
    <if test="role != null">
        AND role = #{role}
    </if>
    ORDER BY created_at DESC
</select>
```

## 6. ResultType vs ResultMap

### Decision: 简单查询用 resultType，复杂映射用 resultMap

**Rationale**:
- 当前项目 PO 字段与数据库列名一致（下划线转驼峰已配置）
- 简单查询使用 resultType 更简洁
- 多表 JOIN 返回非 PO 对象时使用 resultMap

**Guidelines**:
- 单表查询返回 PO → `resultType="PO全限定类名"`
- JOIN 查询返回 PO（有 @TableField(exist=false) 字段）→ `resultType` 仍可用
- 返回 Map 或自定义对象 → `resultMap`

## 7. 命名空间规范

### Decision: namespace 使用 Mapper 接口全限定类名

**Rationale**:
- MyBatis 要求 namespace 与 Mapper 接口完全匹配
- 便于 IDE 导航和错误定位

**Example**:
```xml
<mapper namespace="com.catface996.aiops.repository.mysql.mapper.agent.AgentMapper">
```

## 8. SQL 片段复用

### Decision: 使用 <sql> 定义公共列，使用 <include> 引用

**Rationale**:
- 减少重复代码
- 便于维护（字段变更只需修改一处）

**Pattern**:
```xml
<sql id="Base_Column_List">
    id, name, role, specialty, created_at, updated_at, deleted
</sql>

<select id="selectById" resultType="AgentPO">
    SELECT <include refid="Base_Column_List"/>
    FROM agent
    WHERE id = #{id}
</select>
```

## 9. 注意事项

### 9.1 参数类型
- 基本类型参数：直接使用 `#{paramName}`
- 对象参数：使用 `#{object.field}`
- Map 参数：使用 `#{key}`

### 9.2 特殊字符处理
- `<` 和 `>` 需要转义：`&lt;` 和 `&gt;`
- 或使用 CDATA：`<![CDATA[ condition > 0 ]]>`

### 9.3 空值处理
- MyBatis-Plus updateById 默认忽略 null 值
- XML update 中需要显式 SET null：`SET field = #{value}`（可以设置 null）
