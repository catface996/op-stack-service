---
inclusion: manual
---

# MyBatis-Plus 最佳实践指南

## 为什么选择 MyBatis-Plus

MyBatis-Plus（简称 MP）是 MyBatis 的增强工具，在 MyBatis 的基础上只做增强不做改变。

### 核心优势

- **零侵入**：只做增强不做改变，引入不影响现有工程
- **损耗小**：启动即会自动注入基本 CURD，性能基本无损耗
- **强大的 CRUD 操作**：内置通用 Mapper、通用 Service，仅通过少量配置即可实现单表大部分 CRUD 操作
- **支持 Lambda 形式调用**：通过 Lambda 表达式，方便编写各类查询条件，无需担心字段写错
- **支持主键自动生成**：支持多达 4 种主键策略，可自由配置
- **支持 ActiveRecord 模式**：支持 ActiveRecord 形式调用，实体类只需继承 Model 类即可进行强大的 CRUD 操作
- **支持自定义全局通用操作**：支持全局通用方法注入
- **内置代码生成器**：采用代码或者 Maven 插件可快速生成 Mapper、Model、Service、Controller 层代码
- **内置分页插件**：基于 MyBatis 物理分页，开发者无需关心具体操作
- **内置性能分析插件**：可输出 SQL 语句以及其执行时间
- **内置全局拦截插件**：提供全表 delete、update 操作智能分析阻断
- **内置 SQL 注入剥离器**：支持 SQL 注入剥离，有效预防 SQL 注入攻击

### 适用场景

- 单表 CRUD 操作频繁的项目
- 需要快速开发的项目
- 微服务架构下的持久层开发
- 需要统一数据访问规范的团队项目

---

## 依赖管理

### Maven 依赖配置

#### 父 POM 依赖管理（pom.xml）

```xml
<properties>
    <!-- MyBatis-Plus 版本 -->
    <mybatis-plus.version>3.5.7</mybatis-plus.version>
    <!-- Druid 版本 -->
    <druid.version>1.2.20</druid.version>
</properties>

<dependencyManagement>
    <dependencies>
        <!-- MyBatis-Plus Spring Boot 3 Starter -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- Druid 数据库连接池 -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>${druid.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### 子模块依赖配置（infrastructure/repository/mysql-impl/pom.xml）

```xml
<dependencies>
    <!-- MyBatis-Plus Spring Boot 3 Starter -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    </dependency>

    <!-- Druid 数据库连接池 -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
    </dependency>

    <!-- MySQL 驱动 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 版本选择建议

- **JDK**：21（LTS 版本，Spring Boot 3.4.1 完全支持）
- **Spring Boot**：3.4.1（最新稳定版本）
- **Spring Cloud**：2025.0.0（与 Spring Boot 3.4.1 兼容）
- **MyBatis-Plus**：3.5.7（支持 Spring Boot 3，必须使用 `mybatis-plus-spring-boot3-starter`）
- **Druid**：1.2.20（阿里巴巴数据库连接池，支持 Spring Boot 3）
- **MySQL Connector/J**：由 Spring Boot BOM 管理（Spring Boot 3.4.1 默认使用 8.x 版本）

### 版本兼容性说明

1. **Spring Boot 3.x + MyBatis-Plus 3.5.7**：
   - ✅ **必须使用**：`mybatis-plus-spring-boot3-starter`（Spring Boot 3 专用）
   - ❌ **不能使用**：`mybatis-plus-boot-starter`（仅支持 Spring Boot 2）
   - **原因**：Spring Boot 3 对 Jakarta EE 的支持与 Spring Boot 2 不同，必须使用专用启动器

2. **MySQL 驱动类名**：
   - ✅ **正确**：`com.mysql.cj.jdbc.Driver`（MySQL Connector/J 8.x）
   - ❌ **错误**：`com.mysql.jdbc.Driver`（已废弃）
   - **原因**：MySQL Connector/J 8.x 使用新的驱动类名

3. **JDK 版本要求**：
   - Spring Boot 3.4.1 最低要求 JDK 17
   - 推荐使用 JDK 21（LTS 版本）
   - MyBatis-Plus 3.5.7 支持 JDK 8+，完全兼容 JDK 21

4. **依赖版本管理原则**：
   - 所有版本号在父 POM 的 `<properties>` 中定义
   - 在父 POM 的 `<dependencyManagement>` 中声明依赖
   - 子模块不指定版本号，从父 POM 继承
   - Spring Boot 和 Spring Cloud 通过 BOM 管理依赖版本

---

## 基础配置

### application.yml 配置示例

```yaml
spring:
  datasource:
    # 数据源配置
    url: jdbc:mysql://localhost:3306/mydb?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
    
    # HikariCP 连接池配置（可选）
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-timeout: 30000

mybatis-plus:
  # Mapper XML 文件位置
  mapper-locations: classpath*:/mapper/**/*.xml
  
  # 实体类包路径
  type-aliases-package: com.example.project.domain.entity
  
  # 全局配置
  global-config:
    # 数据库相关配置
    db-config:
      # 主键类型（AUTO: 数据库自增, ASSIGN_ID: 雪花算法, ASSIGN_UUID: UUID）
      id-type: ASSIGN_ID
      
      # 表名前缀
      table-prefix: t_
      
      # 逻辑删除配置
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      
    # 是否打印 Banner
    banner: false
  
  # MyBatis 原生配置
  configuration:
    # 驼峰命名转换
    map-underscore-to-camel-case: true
    
    # 缓存配置
    cache-enabled: false
    
    # 日志实现
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```


### 配置类

```java
package com.example.project.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.project.infrastructure.repository.mapper")
public class MybatisPlusConfig {
    
    /**
     * MyBatis-Plus 拦截器配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 1. 分页插件（必须放在第一位）
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(500L); // 单页最大数量限制
        paginationInnerInterceptor.setOverflow(false); // 溢出总页数后是否进行处理
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        
        // 2. 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        // 3. 防止全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        
        return interceptor;
    }
    
    /**
     * 元数据填充处理器（自动填充创建时间、更新时间等）
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new CustomMetaObjectHandler();
    }
}
```

### 元数据自动填充处理器

```java
package com.example.project.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

public class CustomMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        // 创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 创建人（可从上下文获取）
        this.strictInsertFill(metaObject, "createBy", String.class, getCurrentUser());
        // 更新人
        this.strictInsertFill(metaObject, "updateBy", String.class, getCurrentUser());
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // 更新人
        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUser());
    }
    
    private String getCurrentUser() {
        // 从 Spring Security 或其他上下文获取当前用户
        // 这里简化处理
        return "system";
    }
}
```

---

## 实体类设计规范

### 基础实体类

```java
package com.example.project.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 包含所有表的公共字段
 */
@Data
public abstract class BaseEntity implements Serializable {
    
    /**
     * 主键 ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 创建人（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;
    
    /**
     * 更新人（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
    
    /**
     * 逻辑删除标记（0: 未删除, 1: 已删除）
     */
    @TableLogic
    private Integer deleted;
    
    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}
```


### 业务实体类示例

```java
package com.example.project.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class UserEntity extends BaseEntity {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码（加密存储）
     */
    private String password;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 状态（0: 禁用, 1: 启用）
     */
    private Integer status;
}
```

### 实体类注解说明

#### @TableName
指定数据库表名，如果表名与实体类名不一致时使用。

```java
@TableName("t_user")  // 表名为 t_user
public class UserEntity extends BaseEntity { }
```

#### @TableId
标识主键字段，可指定主键生成策略。

```java
@TableId(type = IdType.ASSIGN_ID)  // 雪花算法
private Long id;

@TableId(type = IdType.AUTO)  // 数据库自增
private Long id;

@TableId(type = IdType.INPUT)  // 手动输入
private Long id;
```

#### @TableField
指定字段属性，常用于字段名映射、自动填充、排除字段等。

```java
// 字段名映射
@TableField("user_name")
private String username;

// 自动填充
@TableField(fill = FieldFill.INSERT)
private LocalDateTime createTime;

// 不映射到数据库
@TableField(exist = false)
private String tempField;

// 查询时不返回该字段
@TableField(select = false)
private String password;
```

#### @TableLogic
标识逻辑删除字段。

```java
@TableLogic
private Integer deleted;  // 0: 未删除, 1: 已删除
```

#### @Version
标识乐观锁版本号字段。

```java
@Version
private Integer version;
```

### 实体类设计原则

1. **继承基础实体类**：公共字段统一管理，避免重复定义
2. **使用 Lombok**：减少样板代码，提高可读性
3. **明确字段含义**：添加清晰的注释说明
4. **合理使用注解**：只在必要时使用注解，避免过度配置
5. **字段命名规范**：遵循驼峰命名，与数据库字段对应
6. **避免使用基本类型**：使用包装类型（Integer、Long），避免 null 值问题

---

## Mapper 层开发规范

### 基础 Mapper 接口

```java
package com.example.project.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.project.domain.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    // 继承 BaseMapper 后，自动拥有基础 CRUD 方法
    // 可以在此添加自定义方法
}
```

### BaseMapper 提供的方法

```java
// 插入
int insert(T entity);

// 根据 ID 删除
int deleteById(Serializable id);

// 根据条件删除
int delete(Wrapper<T> wrapper);

// 根据 ID 批量删除
int deleteBatchIds(Collection<? extends Serializable> idList);

// 根据 ID 更新
int updateById(T entity);

// 根据条件更新
int update(T entity, Wrapper<T> updateWrapper);

// 根据 ID 查询
T selectById(Serializable id);

// 根据 ID 批量查询
List<T> selectBatchIds(Collection<? extends Serializable> idList);

// 根据条件查询一条记录
T selectOne(Wrapper<T> queryWrapper);

// 根据条件查询记录数
Long selectCount(Wrapper<T> queryWrapper);

// 根据条件查询列表
List<T> selectList(Wrapper<T> queryWrapper);

// 根据条件分页查询
IPage<T> selectPage(IPage<T> page, Wrapper<T> queryWrapper);
```

### 自定义 SQL 方法

**重要规范**：除了插入、更新和根据主键查询外，其他所有数据操作都应在 Mapper XML 中定义。

```java
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    
    // ✅ 推荐：在 XML 中定义 SQL
    /**
     * 根据用户名查询用户
     */
    UserEntity selectByUsername(@Param("username") String username);
    
    /**
     * 根据状态查询用户列表
     */
    List<UserEntity> selectByStatus(@Param("status") Integer status);
    
    /**
     * 根据条件查询用户数量
     */
    Long countByCondition(@Param("username") String username, 
                          @Param("status") Integer status,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);
    
    /**
     * 复杂查询：用户及其角色信息
     */
    List<UserEntity> selectUserWithRoles(@Param("userId") Long userId);
    
    /**
     * 批量更新用户状态
     */
    int updateStatusBatch(@Param("userIds") List<Long> userIds, 
                          @Param("status") Integer status);
    
    /**
     * 根据条件删除（逻辑删除）
     */
    int deleteByCondition(@Param("status") Integer status, 
                          @Param("createTime") LocalDateTime createTime);
}
```

**不推荐的做法**：

```java
// ❌ 不推荐：在 Service 中使用 Wrapper 构造复杂查询
public List<UserEntity> searchUsers(String username, Integer status) {
    LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.like(StringUtils.isNotBlank(username), UserEntity::getUsername, username)
           .eq(status != null, UserEntity::getStatus, status)
           .orderByDesc(UserEntity::getCreateTime);
    return list(wrapper);
}

// ✅ 推荐：在 Mapper XML 中定义
public List<UserEntity> searchUsers(String username, Integer status) {
    return baseMapper.selectByCondition(username, status);
}
```

### Mapper XML 文件

**所有条件查询、复杂查询都应在 XML 中定义，便于统一管理和性能优化。**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.project.infrastructure.repository.mapper.UserMapper">
    
    <!-- 结果映射 -->
    <resultMap id="BaseResultMap" type="com.example.project.domain.entity.UserEntity">
        <id column="id" property="id"/>
        <result column="username" property="username"/>
        <result column="password" property="password"/>
        <result column="nickname" property="nickname"/>
        <result column="email" property="email"/>
        <result column="phone" property="phone"/>
        <result column="status" property="status"/>
        <result column="create_time" property="createTime"/>
        <result column="update_time" property="updateTime"/>
        <result column="create_by" property="createBy"/>
        <result column="update_by" property="updateBy"/>
        <result column="deleted" property="deleted"/>
        <result column="version" property="version"/>
    </resultMap>
    
    <!-- 根据用户名查询 -->
    <select id="selectByUsername" resultMap="BaseResultMap">
        SELECT * FROM t_user
        WHERE username = #{username}
        AND deleted = 0
    </select>
    
    <!-- 根据状态查询 -->
    <select id="selectByStatus" resultMap="BaseResultMap">
        SELECT * FROM t_user
        WHERE status = #{status}
        AND deleted = 0
        ORDER BY create_time DESC
    </select>
    
    <!-- 根据条件查询数量 -->
    <select id="countByCondition" resultType="java.lang.Long">
        SELECT COUNT(1) FROM t_user
        WHERE deleted = 0
        <if test="username != null and username != ''">
            AND username LIKE CONCAT('%', #{username}, '%')
        </if>
        <if test="status != null">
            AND status = #{status}
        </if>
        <if test="startTime != null">
            AND create_time &gt;= #{startTime}
        </if>
        <if test="endTime != null">
            AND create_time &lt;= #{endTime}
        </if>
    </select>
    
    <!-- 复杂查询：用户及其角色信息 -->
    <resultMap id="UserWithRolesMap" type="com.example.project.domain.entity.UserEntity">
        <id column="id" property="id"/>
        <result column="username" property="username"/>
        <!-- 其他字段映射 -->
    </resultMap>
    
    <select id="selectUserWithRoles" resultMap="UserWithRolesMap">
        SELECT u.*, r.role_name
        FROM t_user u
        LEFT JOIN t_user_role ur ON u.id = ur.user_id
        LEFT JOIN t_role r ON ur.role_id = r.id
        WHERE u.id = #{userId}
        AND u.deleted = 0
    </select>
    
    <!-- 批量更新用户状态 -->
    <update id="updateStatusBatch">
        UPDATE t_user
        SET status = #{status},
            update_time = NOW()
        WHERE id IN
        <foreach collection="userIds" item="id" open="(" separator="," close=")">
            #{id}
        </foreach>
        AND deleted = 0
    </update>
    
    <!-- 根据条件删除（逻辑删除） -->
    <update id="deleteByCondition">
        UPDATE t_user
        SET deleted = 1,
            update_time = NOW()
        WHERE status = #{status}
        AND create_time &lt; #{createTime}
        AND deleted = 0
    </update>
    
</mapper>
```

**XML 文件组织规范**：

1. **文件位置**：`infrastructure/repository/src/main/resources/mapper/`
2. **命名规范**：与 Mapper 接口同名，如 `UserMapper.xml`
3. **SQL 顺序**：查询 → 插入 → 更新 → 删除
4. **注释规范**：每个 SQL 语句都应添加清晰的注释
5. **格式规范**：保持良好的缩进和换行，提高可读性


---

## Service 层开发规范

### 基础 Service 接口

```java
package com.example.project.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.project.domain.entity.UserEntity;

/**
 * 用户服务接口
 */
public interface UserService extends IService<UserEntity> {
    // 继承 IService 后，自动拥有基础 CRUD 方法
    // 可以在此添加自定义业务方法
    
    /**
     * 根据用户名查询用户
     */
    UserEntity getByUsername(String username);
    
    /**
     * 注册新用户
     */
    boolean register(UserEntity user);
}
```

### Service 实现类

**遵循数据操作规范：条件查询使用 Mapper XML，插入更新使用 MyBatis-Plus API。**

```java
package com.example.project.application.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.project.application.service.UserService;
import com.example.project.domain.entity.UserEntity;
import com.example.project.infrastructure.repository.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
    
    @Override
    public UserEntity getByUsername(String username) {
        // ✅ 推荐：使用 Mapper XML 中定义的方法
        return baseMapper.selectByUsername(username);
        
        // ❌ 不推荐：使用 Wrapper 构造查询
        // LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(UserEntity::getUsername, username);
        // return this.getOne(wrapper);
    }
    
    @Override
    public List<UserEntity> listByStatus(Integer status) {
        // ✅ 推荐：使用 Mapper XML 中定义的方法
        return baseMapper.selectByStatus(status);
    }
    
    @Override
    public Long countUsers(String username, Integer status, 
                          LocalDateTime startTime, LocalDateTime endTime) {
        // ✅ 推荐：使用 Mapper XML 中定义的方法
        return baseMapper.countByCondition(username, status, startTime, endTime);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(UserEntity user) {
        // 检查用户名是否已存在
        UserEntity existUser = getByUsername(user.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 密码加密（实际项目中应使用 BCrypt 等加密算法）
        // user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // ✅ 插入操作：使用 MyBatis-Plus API
        return this.save(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserStatus(Long userId, Integer status) {
        // ✅ 根据主键更新：使用 MyBatis-Plus API
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setStatus(status);
        return this.updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateStatus(List<Long> userIds, Integer status) {
        // ✅ 批量条件更新：使用 Mapper XML 中定义的方法
        return baseMapper.updateStatusBatch(userIds, status);
        
        // ❌ 不推荐：使用 UpdateWrapper
        // LambdaUpdateWrapper<UserEntity> wrapper = new LambdaUpdateWrapper<>();
        // wrapper.in(UserEntity::getId, userIds)
        //        .set(UserEntity::getStatus, status);
        // return this.update(wrapper) ? userIds.size() : 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanInactiveUsers(Integer status, LocalDateTime createTime) {
        // ✅ 条件删除：使用 Mapper XML 中定义的方法
        return baseMapper.deleteByCondition(status, createTime);
        
        // ❌ 不推荐：使用 Wrapper
        // LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(UserEntity::getStatus, status)
        //        .lt(UserEntity::getCreateTime, createTime);
        // return this.remove(wrapper) ? 1 : 0;
    }
}
```

**Service 层数据操作规范总结**：

| 操作类型 | 推荐做法 | 示例 |
|---------|---------|------|
| 插入单条 | MyBatis-Plus API | `save(entity)` |
| 批量插入 | MyBatis-Plus API | `saveBatch(list)` |
| 根据主键更新 | MyBatis-Plus API | `updateById(entity)` |
| 批量主键更新 | MyBatis-Plus API | `updateBatchById(list)` |
| 根据主键查询 | MyBatis-Plus API | `getById(id)` |
| 批量主键查询 | MyBatis-Plus API | `listByIds(ids)` |
| 条件查询 | Mapper XML | `baseMapper.selectByXxx()` |
| 条件更新 | Mapper XML | `baseMapper.updateByXxx()` |
| 条件删除 | Mapper XML | `baseMapper.deleteByXxx()` |
| 复杂查询 | Mapper XML | `baseMapper.selectXxxWithYyy()` |
| 聚合查询 | Mapper XML | `baseMapper.countByXxx()` |

### IService 提供的常用方法

```java
// 保存（插入）
boolean save(T entity);
boolean saveBatch(Collection<T> entityList);

// 保存或更新（根据 ID 判断）
boolean saveOrUpdate(T entity);
boolean saveOrUpdateBatch(Collection<T> entityList);

// 根据 ID 删除
boolean removeById(Serializable id);
boolean removeByIds(Collection<? extends Serializable> idList);

// 根据条件删除
boolean remove(Wrapper<T> queryWrapper);

// 根据 ID 更新
boolean updateById(T entity);
boolean updateBatchById(Collection<T> entityList);

// 根据条件更新
boolean update(Wrapper<T> updateWrapper);
boolean update(T entity, Wrapper<T> updateWrapper);

// 根据 ID 查询
T getById(Serializable id);
List<T> listByIds(Collection<? extends Serializable> idList);

// 查询一条记录
T getOne(Wrapper<T> queryWrapper);

// 查询列表
List<T> list();
List<T> list(Wrapper<T> queryWrapper);

// 查询总数
long count();
long count(Wrapper<T> queryWrapper);

// 分页查询
IPage<T> page(IPage<T> page);
IPage<T> page(IPage<T> page, Wrapper<T> queryWrapper);
```

---

## 数据操作规范（核心最佳实践）

### 规范说明

为了便于统一管理、代码审查和性能分析，项目中的数据操作应严格遵循以下规范：

**允许使用 MyBatis-Plus 提供的 API**：
- ✅ 插入操作：`save()`、`saveBatch()`、`saveOrUpdate()` 等
- ✅ 更新操作：`updateById()`、`updateBatchById()` 等
- ✅ 根据主键查询：`getById()`、`listByIds()` 等

**必须在 Mapper XML 中实现**：
- ❌ 所有条件查询（不使用 Wrapper）
- ❌ 所有条件更新（不使用 UpdateWrapper）
- ❌ 所有条件删除（不使用 QueryWrapper）
- ❌ 所有复杂查询（多表关联、子查询、聚合等）

### 规范理由

1. **统一管理**
   - 所有 SQL 语句集中在 XML 文件中
   - 便于查找、维护和优化
   - 避免 SQL 分散在各个 Service 中

2. **代码审查**
   - DBA 和技术负责人可以快速审查所有 SQL
   - 及时发现性能问题和安全隐患
   - 确保 SQL 编写规范统一

3. **性能分析**
   - 便于使用工具分析 SQL 性能
   - 便于添加索引和优化查询
   - 便于监控慢查询

4. **可维护性**
   - SQL 语句清晰可见，易于理解
   - 避免动态 SQL 难以追踪
   - 便于后续重构和优化

5. **团队协作**
   - 统一的开发规范
   - 降低代码审查成本
   - 提高代码质量

### 正确示例

#### 示例 1：条件查询

```java
// ❌ 错误：在 Service 中使用 Wrapper
public List<UserEntity> searchUsers(String username, Integer status) {
    LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.like(StringUtils.isNotBlank(username), UserEntity::getUsername, username)
           .eq(status != null, UserEntity::getStatus, status);
    return list(wrapper);
}

// ✅ 正确：在 Mapper XML 中定义
// Mapper 接口
List<UserEntity> selectByCondition(@Param("username") String username, 
                                   @Param("status") Integer status);

// Service 实现
public List<UserEntity> searchUsers(String username, Integer status) {
    return baseMapper.selectByCondition(username, status);
}

// XML 文件
<select id="selectByCondition" resultMap="BaseResultMap">
    SELECT * FROM t_user
    WHERE deleted = 0
    <if test="username != null and username != ''">
        AND username LIKE CONCAT('%', #{username}, '%')
    </if>
    <if test="status != null">
        AND status = #{status}
    </if>
    ORDER BY create_time DESC
</select>
```

#### 示例 2：条件更新

```java
// ❌ 错误：在 Service 中使用 UpdateWrapper
public boolean updateUserStatus(List<Long> userIds, Integer status) {
    LambdaUpdateWrapper<UserEntity> wrapper = new LambdaUpdateWrapper<>();
    wrapper.in(UserEntity::getId, userIds)
           .set(UserEntity::getStatus, status);
    return update(wrapper);
}

// ✅ 正确：在 Mapper XML 中定义
// Mapper 接口
int updateStatusBatch(@Param("userIds") List<Long> userIds, 
                      @Param("status") Integer status);

// Service 实现
public boolean updateUserStatus(List<Long> userIds, Integer status) {
    return baseMapper.updateStatusBatch(userIds, status) > 0;
}

// XML 文件
<update id="updateStatusBatch">
    UPDATE t_user
    SET status = #{status},
        update_time = NOW()
    WHERE id IN
    <foreach collection="userIds" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
    AND deleted = 0
</update>
```

#### 示例 3：条件删除

```java
// ❌ 错误：在 Service 中使用 Wrapper
public boolean deleteInactiveUsers(LocalDateTime beforeTime) {
    LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(UserEntity::getStatus, 0)
           .lt(UserEntity::getCreateTime, beforeTime);
    return remove(wrapper);
}

// ✅ 正确：在 Mapper XML 中定义
// Mapper 接口
int deleteInactiveUsers(@Param("beforeTime") LocalDateTime beforeTime);

// Service 实现
public boolean deleteInactiveUsers(LocalDateTime beforeTime) {
    return baseMapper.deleteInactiveUsers(beforeTime) > 0;
}

// XML 文件
<update id="deleteInactiveUsers">
    UPDATE t_user
    SET deleted = 1,
        update_time = NOW()
    WHERE status = 0
    AND create_time &lt; #{beforeTime}
    AND deleted = 0
</update>
```

#### 示例 4：复杂查询

```java
// ❌ 错误：在 Service 中使用 Wrapper 构造复杂查询
public IPage<UserEntity> pageUsers(Page<UserEntity> page, UserQueryDTO query) {
    LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.like(StringUtils.isNotBlank(query.getUsername()), 
                 UserEntity::getUsername, query.getUsername())
           .eq(query.getStatus() != null, 
               UserEntity::getStatus, query.getStatus())
           .between(query.getStartTime() != null && query.getEndTime() != null,
                   UserEntity::getCreateTime, query.getStartTime(), query.getEndTime())
           .orderByDesc(UserEntity::getCreateTime);
    return page(page, wrapper);
}

// ✅ 正确：在 Mapper XML 中定义
// Mapper 接口
IPage<UserEntity> selectPageByCondition(Page<?> page, @Param("query") UserQueryDTO query);

// Service 实现
public IPage<UserEntity> pageUsers(Page<UserEntity> page, UserQueryDTO query) {
    return baseMapper.selectPageByCondition(page, query);
}

// XML 文件
<select id="selectPageByCondition" resultMap="BaseResultMap">
    SELECT * FROM t_user
    WHERE deleted = 0
    <if test="query.username != null and query.username != ''">
        AND username LIKE CONCAT('%', #{query.username}, '%')
    </if>
    <if test="query.status != null">
        AND status = #{query.status}
    </if>
    <if test="query.startTime != null">
        AND create_time &gt;= #{query.startTime}
    </if>
    <if test="query.endTime != null">
        AND create_time &lt;= #{query.endTime}
    </if>
    ORDER BY create_time DESC
</select>
```

### 例外情况

在极少数情况下，可以使用 Wrapper，但需要明确注释说明理由：

```java
/**
 * 动态构建复杂查询条件
 * 注意：此处使用 Wrapper 是因为查询条件完全由前端动态传入，
 * 无法在 XML 中预先定义所有可能的组合
 */
@Deprecated
public List<UserEntity> dynamicSearch(Map<String, Object> conditions) {
    // 仅在特殊场景下使用
    LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
    // ... 动态构建条件
    return list(wrapper);
}
```

### 检查清单

在代码审查时，检查以下内容：

- [ ] 所有条件查询都在 Mapper XML 中定义
- [ ] 所有条件更新都在 Mapper XML 中定义
- [ ] 所有条件删除都在 Mapper XML 中定义
- [ ] Service 层只使用 MyBatis-Plus API 进行插入、更新和主键查询
- [ ] 没有在 Service 中直接使用 Wrapper（除非有充分理由并注释说明）
- [ ] 所有 XML 中的 SQL 都有适当的注释
- [ ] 所有 SQL 都考虑了性能和索引

---

## 分页查询

### 分页查询规范

**重要**：根据项目数据操作规范，所有分页查询都必须在 Mapper XML 中定义 SQL，禁止使用 Wrapper 构造查询条件。

### 自定义分页查询

```java
// Mapper 接口
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    
    /**
     * 自定义分页查询（需要在 XML 中编写 SQL）
     */
    IPage<UserEntity> selectUserPage(Page<?> page, @Param("username") String username);
}

// XML 文件
<select id="selectUserPage" resultType="com.example.project.domain.entity.UserEntity">
    SELECT * FROM t_user
    WHERE deleted = 0
    <if test="username != null and username != ''">
        AND username LIKE CONCAT('%', #{username}, '%')
    </if>
    ORDER BY create_time DESC
</select>

// Service 调用
public IPage<UserEntity> getUserPage(Integer current, Integer size, String username) {
    Page<UserEntity> page = new Page<>(current, size);
    return baseMapper.selectUserPage(page, username);
}
```

### 分页结果处理

```java
IPage<UserEntity> page = userService.page(new Page<>(1, 10));

// 获取分页信息
long total = page.getTotal();           // 总记录数
long pages = page.getPages();           // 总页数
long current = page.getCurrent();       // 当前页
long size = page.getSize();             // 每页大小
List<UserEntity> records = page.getRecords();  // 当前页数据

// 转换为 DTO
IPage<UserDTO> dtoPage = page.convert(user -> {
    UserDTO dto = new UserDTO();
    BeanUtils.copyProperties(user, dto);
    return dto;
});
```

---

## 多数据源配置

### 使用 dynamic-datasource-spring-boot-starter

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>dynamic-datasource-spring-boot-starter</artifactId>
    <version>3.6.1</version>
</dependency>
```

### 配置多数据源

```yaml
spring:
  datasource:
    dynamic:
      # 设置默认数据源
      primary: master
      # 严格模式（未匹配到数据源时抛出异常）
      strict: true
      datasource:
        # 主库
        master:
          url: jdbc:mysql://localhost:3306/db_master
          username: root
          password: password
          driver-class-name: com.mysql.cj.jdbc.Driver
        # 从库
        slave:
          url: jdbc:mysql://localhost:3306/db_slave
          username: root
          password: password
          driver-class-name: com.mysql.cj.jdbc.Driver
```

### 使用 @DS 注解切换数据源

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
    
    /**
     * 使用主库（默认）
     */
    @DS("master")
    public boolean save(UserEntity user) {
        return super.save(user);
    }
    
    /**
     * 使用从库查询
     */
    @DS("slave")
    public List<UserEntity> list() {
        return super.list();
    }
}
```

---

## 性能优化

### 1. 合理使用索引

```sql
-- 为常用查询字段添加索引
CREATE INDEX idx_username ON t_user(username);
CREATE INDEX idx_status_create_time ON t_user(status, create_time);
```

### 2. 避免查询所有字段

```java
// 不推荐：查询所有字段
List<UserEntity> users = userService.list();

// 推荐：只查询需要的字段
LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
wrapper.select(UserEntity::getId, UserEntity::getUsername, UserEntity::getEmail);
List<UserEntity> users = userService.list(wrapper);
```

### 3. 批量操作

```java
// 批量插入（推荐使用 saveBatch）
List<UserEntity> users = new ArrayList<>();
// ... 添加数据
userService.saveBatch(users, 1000);  // 每批 1000 条

// 批量更新
userService.updateBatchById(users, 1000);
```

### 4. 使用分页查询

```java
// 避免一次性查询大量数据
Page<UserEntity> page = new Page<>(1, 100);
IPage<UserEntity> result = userService.page(page);
```

### 5. 开启二级缓存（谨慎使用）

```yaml
mybatis-plus:
  configuration:
    cache-enabled: true
```

```java
@CacheNamespace  // 在 Mapper 接口上添加
public interface UserMapper extends BaseMapper<UserEntity> {
}
```

### 6. SQL 性能分析

```java
@Configuration
public class MybatisPlusConfig {
    
    /**
     * SQL 性能分析插件（仅开发环境使用）
     */
    @Bean
    @Profile({"dev", "test"})
    public PerformanceInterceptor performanceInterceptor() {
        PerformanceInterceptor interceptor = new PerformanceInterceptor();
        interceptor.setMaxTime(1000);  // SQL 执行超过 1 秒时输出
        interceptor.setFormat(true);   // 格式化 SQL
        return interceptor;
    }
}
```

---

## 常见问题与解决方案

### 1. 逻辑删除不生效

**问题**：执行删除操作后，数据被物理删除而非逻辑删除。

**解决方案**：
```java
// 确保实体类添加了 @TableLogic 注解
@TableLogic
private Integer deleted;

// 确保配置文件中配置了逻辑删除
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 2. 自动填充不生效

**问题**：创建时间、更新时间等字段没有自动填充。

**解决方案**：
```java
// 1. 确保实体类添加了 @TableField 注解
@TableField(fill = FieldFill.INSERT)
private LocalDateTime createTime;

// 2. 确保配置了 MetaObjectHandler
@Bean
public MetaObjectHandler metaObjectHandler() {
    return new CustomMetaObjectHandler();
}

// 3. 确保 MetaObjectHandler 实现正确
@Override
public void insertFill(MetaObject metaObject) {
    this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
}
```

### 3. 分页查询总数不准确

**问题**：分页查询时，总记录数与实际不符。

**解决方案**：
```java
// 确保分页插件配置正确
@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    // 分页插件必须放在第一位
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
    return interceptor;
}
```

### 4. 乐观锁更新失败

**问题**：使用乐观锁时，更新操作总是失败。

**解决方案**：
```java
// 1. 确保实体类添加了 @Version 注解
@Version
private Integer version;

// 2. 确保配置了乐观锁插件
interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

// 3. 更新时必须先查询获取 version
UserEntity user = userService.getById(1L);
user.setUsername("newName");
userService.updateById(user);  // version 会自动 +1
```

### 5. 多数据源事务问题

**问题**：多数据源环境下，事务不生效或数据不一致。

**解决方案**：
```java
// 1. 单数据源事务正常使用 @Transactional
@Transactional(rollbackFor = Exception.class)
public void singleDataSourceMethod() {
    // ...
}

// 2. 跨数据源事务需要使用分布式事务（如 Seata）
// 或者避免在同一事务中操作多个数据源
```

---

## 微服务架构集成

### 1. 模块划分原则

在 DDD 分层架构中，MyBatis-Plus 相关代码的位置：

```
project-root/
├── domain/                    # 领域层
│   ├── domain-api/           # 领域 API
│   │   └── entity/           # 实体类（放这里）
│   └── domain-impl/          # 领域实现
│
├── infrastructure/           # 基础设施层
│   └── repository/          # 仓储实现
│       ├── mapper/          # Mapper 接口（放这里）
│       └── resources/
│           └── mapper/      # Mapper XML（放这里）
│
├── application/             # 应用层
│   ├── application-api/    # 应用 API
│   │   └── service/        # Service 接口（放这里）
│   └── application-impl/   # 应用实现
│       └── service/impl/   # Service 实现（放这里）
│
└── bootstrap/              # 启动模块
    └── config/            # MyBatis-Plus 配置（放这里）
```

### 2. 依赖管理

```xml
<!-- 父 POM 中管理版本 -->
<properties>
    <!-- MyBatis-Plus 版本 -->
    <mybatis-plus.version>3.5.7</mybatis-plus.version>
    <!-- Druid 版本 -->
    <druid.version>1.2.20</druid.version>
</properties>

<dependencyManagement>
    <dependencies>
        <!-- MyBatis-Plus Spring Boot 3 Starter -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- Druid 数据库连接池 -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>${druid.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- infrastructure/repository/mysql-impl 模块引入依赖 -->
<dependencies>
    <!-- MyBatis-Plus Spring Boot 3 Starter -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    </dependency>

    <!-- Druid 数据库连接池 -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
    </dependency>

    <!-- MySQL 驱动 -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 3. 配置中心集成

```yaml
# Nacos 配置示例
spring:
  datasource:
    url: ${spring.datasource.url}
    username: ${spring.datasource.username}
    password: ${spring.datasource.password}
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.example.project.domain.entity
  global-config:
    db-config:
      id-type: ASSIGN_ID
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```


---

## 安全最佳实践

### 1. SQL 注入防护

```java
// 推荐：使用参数化查询（MyBatis-Plus 默认防注入）
LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(UserEntity::getUsername, username);  // 安全

// 避免：字符串拼接（容易 SQL 注入）
String sql = "SELECT * FROM t_user WHERE username = '" + username + "'";  // 危险！
```

### 2. 敏感字段处理

```java
@Data
@TableName("t_user")
public class UserEntity extends BaseEntity {
    
    /**
     * 密码字段（查询时不返回）
     */
    @TableField(select = false)
    private String password;
    
    /**
     * 身份证号（需要脱敏）
     */
    @JsonSerialize(using = SensitiveJsonSerializer.class)
    private String idCard;
}
```

### 3. 防止全表更新/删除

```java
@Configuration
public class MybatisPlusConfig {
    
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 防止全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        
        return interceptor;
    }
}

// 以下操作会被拦截并抛出异常
userService.remove(new LambdaQueryWrapper<>());  // 全表删除，会被拦截
userService.update(new LambdaUpdateWrapper<>());  // 全表更新，会被拦截
```

### 4. 数据权限控制

```java
/**
 * 数据权限拦截器
 */
@Component
public class DataPermissionInterceptor implements InnerInterceptor {
    
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, 
                           RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        // 获取当前用户
        Long userId = getCurrentUserId();
        
        // 修改 SQL，添加数据权限条件
        // 例如：只能查询自己创建的数据
        // WHERE create_by = #{userId}
    }
    
    private Long getCurrentUserId() {
        // 从 Spring Security 或其他上下文获取
        return 1L;
    }
}
```

---

## 测试最佳实践

### 1. 单元测试

```java
@SpringBootTest
@Transactional  // 测试完成后自动回滚
public class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    public void testSaveUser() {
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        
        boolean result = userService.save(user);
        
        assertTrue(result);
        assertNotNull(user.getId());
    }
    
    @Test
    public void testGetByUsername() {
        // 准备测试数据
        UserEntity user = new UserEntity();
        user.setUsername("testuser");
        userService.save(user);
        
        // 执行查询
        UserEntity result = userService.getByUsername("testuser");
        
        // 验证结果
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }
}
```

### 2. Mapper 测试

```java
@SpringBootTest
public class UserMapperTest {
    
    @Autowired
    private UserMapper userMapper;
    
    @Test
    public void testSelectById() {
        UserEntity user = userMapper.selectById(1L);
        assertNotNull(user);
    }
    
    @Test
    public void testSelectByWrapper() {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, "admin");
        
        List<UserEntity> users = userMapper.selectList(wrapper);
        assertFalse(users.isEmpty());
    }
}
```

### 3. 使用 H2 内存数据库测试

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 监控与日志

### 1. SQL 日志配置

```yaml
# 开发环境：输出详细 SQL
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# 生产环境：使用 SLF4J
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl

logging:
  level:
    com.example.project.infrastructure.repository.mapper: debug
```

### 2. 慢 SQL 监控

```java
@Configuration
public class MybatisPlusConfig {
    
    /**
     * SQL 性能分析插件（仅开发/测试环境）
     */
    @Bean
    @Profile({"dev", "test"})
    public PerformanceInterceptor performanceInterceptor() {
        PerformanceInterceptor interceptor = new PerformanceInterceptor();
        interceptor.setMaxTime(1000);  // 超过 1 秒的 SQL 输出警告
        interceptor.setFormat(true);   // 格式化 SQL
        return interceptor;
    }
}
```

### 3. 集成 Druid 监控

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.2.20</version>
</dependency>
```

```yaml
spring:
  datasource:
    druid:
      # 监控统计
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        login-username: admin
        login-password: admin
      
      # SQL 监控
      filter:
        stat:
          enabled: true
          log-slow-sql: true
          slow-sql-millis: 1000
```

---

## 最佳实践总结

### 开发规范

1. **实体类规范**
   - 继承统一的基础实体类
   - 使用 Lombok 减少样板代码
   - 合理使用注解（@TableName、@TableId、@TableField 等）
   - 敏感字段使用 `select = false` 避免泄露

2. **Mapper 规范**
   - 继承 BaseMapper 获得基础 CRUD 能力
   - 复杂查询使用 XML 方式编写
   - 避免在 Mapper 中编写业务逻辑

3. **Service 规范**
   - 继承 IService 和 ServiceImpl
   - 业务逻辑放在 Service 层
   - 合理使用事务注解
   - 避免在 Service 中直接操作 Mapper

4. **数据操作规范（重要）**
   
   为了便于统一管理、代码审查和性能分析，数据操作应遵循以下规范：
   
   **允许使用 MyBatis-Plus API 的场景**：
   - ✅ **插入操作**：`save()`、`saveBatch()`、`saveOrUpdate()` 等
   - ✅ **更新操作**：`updateById()`、`updateBatchById()` 等
   - ✅ **根据主键查询**：`getById()`、`listByIds()` 等
   
   **必须在 Mapper XML 中实现的场景**：
   - ❌ **条件查询**：所有带条件的查询（不使用 Wrapper）
   - ❌ **条件删除**：所有带条件的删除操作
   - ❌ **复杂查询**：多表关联、子查询、聚合查询等
   - ❌ **批量条件操作**：批量条件更新、批量条件删除等
   
   **规范理由**：
   - 统一管理所有 SQL 语句，便于维护和优化
   - 方便进行代码审查，及时发现潜在问题
   - 便于 DBA 进行性能分析和索引优化
   - 避免动态 SQL 难以追踪和调试
   - 提高 SQL 的可读性和可维护性

4. **条件构造器规范**
   - ⚠️ **原则上不使用条件构造器**（Wrapper）
   - 所有条件查询、更新、删除都在 Mapper XML 中定义
   - 仅在极特殊场景下使用 Wrapper，并注释说明理由
   - 如必须使用，优先使用 LambdaQueryWrapper（类型安全）

5. **分页查询规范**
   - 必须限制单页最大数量
   - 避免深度分页（offset 过大）
   - 考虑使用游标分页或 ID 范围查询

### 性能优化

1. **查询优化**
   - 只查询需要的字段
   - 合理使用索引
   - 避免 N+1 查询问题
   - 使用批量操作代替循环单条操作

2. **缓存策略**
   - 热点数据使用 Redis 缓存
   - 谨慎使用 MyBatis 二级缓存
   - 注意缓存一致性问题

3. **连接池配置**
   - 合理设置连接池大小
   - 设置连接超时时间
   - 定期检测连接有效性

### 安全规范

1. **SQL 注入防护**
   - 使用参数化查询
   - 避免字符串拼接 SQL
   - 开启 SQL 注入剥离器

2. **数据权限**
   - 实现数据权限拦截器
   - 敏感字段脱敏处理
   - 防止越权访问

3. **防护措施**
   - 开启防全表更新/删除插件
   - 限制单次查询数量
   - 记录敏感操作日志

### 测试规范

1. **单元测试**
   - 为核心业务逻辑编写测试
   - 使用 @Transactional 自动回滚
   - 使用 H2 内存数据库加速测试

2. **集成测试**
   - 测试 Mapper 和数据库交互
   - 验证复杂查询的正确性
   - 测试事务和并发场景

### 监控运维

1. **日志配置**
   - 开发环境输出详细 SQL
   - 生产环境记录慢 SQL
   - 记录异常和错误信息

2. **性能监控**
   - 监控 SQL 执行时间
   - 监控数据库连接池状态
   - 监控慢查询和异常

3. **告警机制**
   - 慢 SQL 告警
   - 连接池耗尽告警
   - 异常频率告警

---

## 参考资源

- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [MyBatis-Plus GitHub](https://github.com/baomidou/mybatis-plus)
- [MyBatis 官方文档](https://mybatis.org/mybatis-3/zh/index.html)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)

---

## 版本历史

- **v1.0**（2024-01）：初始版本
- **v2.0**（2024-11）：全面优化，增加详细示例和最佳实践

---

**最后更新**：2024-11-10
