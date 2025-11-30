# 设计文档

**功能名称**: F03 - 创建和管理IT资源  
**文档版本**: v1.0  
**创建日期**: 2024-11-30  
**设计负责人**: AI Assistant  
**状态**: 设计中 🚧

---

## 1. 概述

### 1.1 设计目标

本文档定义F03功能的技术设计方案，基于需求文档（requirements.md）中定义的42个需求，采用DDD分层架构和六边形架构原则，实现高质量、可扩展、高性能的IT资源管理功能。

### 1.2 设计原则

| 原则 | 说明 | 应用 |
|------|------|------|
| **DDD分层架构** | 严格遵循领域驱动设计 | Interface → Application → Domain → Infrastructure |
| **架构前瞻** | 为F02-1预留扩展性 | 数据模型支持动态资源类型 |
| **性能优先** | 满足性能指标 | 索引优化、缓存策略、分页查询 |
| **安全第一** | 敏感信息保护 | AES-256加密、权限验证、审计日志 |
| **可测试性** | 便于单元测试和集成测试 | 依赖注入、接口抽象、Mock友好 |

### 1.3 技术栈

| 类别 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 语言 | Java | 21 (LTS) | 开发语言 |
| 框架 | Spring Boot | 3.4.1 | 应用框架 |
| ORM | MyBatis-Plus | 3.5.7 | 数据访问 |
| 数据库 | MySQL | 8.0+ | 数据存储 |
| 缓存 | Redis | 7.0+ | 缓存和会话 |
| 加密 | JCA (AES-256) | - | 敏感信息加密 |
| 日志 | Logback + SLF4J | 2.0.x | 日志记录 |
| 测试 | JUnit 5 + Mockito | 5.11.x | 单元测试 |

---

## 2. 架构设计

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                    接口层 (Interface)                    │
│  - ResourceController: REST API接口                     │
│  - Request/Response DTO: 请求响应对象                   │
│  - GlobalExceptionHandler: 全局异常处理                 │
└────────────────────┬────────────────────────────────────┘
                     │ 调用
┌────────────────────┴────────────────────────────────────┐
│                   应用层 (Application)                   │
│  - ResourceApplicationService: 资源应用服务             │
│  - Command/Query DTO: 内部传输对象                      │
│  - 事务控制: @Transactional                             │
└────────────────────┬────────────────────────────────────┘
                     │ 调用
┌────────────────────┴────────────────────────────────────┐
│                    领域层 (Domain)                       │
│  - ResourceDomainService: 资源领域服务                  │
│  - Resource: 资源聚合根                                 │
│  - ResourceType: 资源类型实体                           │
│  - 业务规则验证                                         │
└────────────────────┬────────────────────────────────────┘
                     │ 调用
┌────────────────────┴────────────────────────────────────┐
│               基础设施层 (Infrastructure)                │
│  - ResourceRepositoryImpl: 数据访问实现                 │
│  - ResourceCacheServiceImpl: 缓存实现                   │
│  - EncryptionServiceImpl: 加密实现                      │
│  - AuditLogServiceImpl: 审计日志实现                    │
└─────────────────────────────────────────────────────────┘
```

### 2.2 模块依赖关系

```
interface-http
  └─> application-api
        └─> domain-api
              ├─> repository-api
              ├─> cache-api
              └─> security-api

application-impl
  └─> domain-api

domain-impl
  ├─> repository-api
  ├─> cache-api
  └─> security-api

mysql-impl
  └─> repository-api

redis-impl
  └─> cache-api

security-impl
  └─> security-api
```

### 2.3 依赖规则 (NON-NEGOTIABLE)

| 规则 | 描述 | 强制性 |
|------|------|--------|
| ✅ MUST | Application Service 只能调用 Domain Service | 强制 |
| ✅ MUST | Domain Service 是数据访问的唯一入口 | 强制 |
| ✅ MUST | Repository/Cache 接口定义在 Domain 层 | 强制 |
| ❌ NEVER | Application Service 禁止直接调用 Repository | 强制 |

---

## 3. 领域模型设计

### 3.1 聚合根：Resource

**职责**：资源的核心业务逻辑和状态管理

```java
package com.catface996.aiops.domain.model.resource;

/**
 * 资源聚合根
 */
public class Resource {
    // 基本属性
    private Long id;
    private String name;
    private String description;
    private ResourceType type;
    private ResourceStatus status;
    private Map<String, Object> attributes;  // 扩展属性
    private Integer version;  // 乐观锁版本号
    
    // 元数据
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 标签
    private Set<String> tags;
    
    // 业务方法
    public void updateInfo(String name, String description, Map<String, Object> attributes);
    public void changeStatus(ResourceStatus newStatus, Long operatorId);
    public void addTag(String tag);
    public void removeTag(String tag);
    public boolean isOwner(Long userId);
    public void validateForCreation();
    public void validateForUpdate();
}
```

### 3.2 实体：ResourceType

**职责**：资源类型定义

```java
package com.catface996.aiops.domain.model.resource;

/**
 * 资源类型实体
 */
public class ResourceType {
    private Long id;
    private String code;  // SERVER, APPLICATION, DATABASE, API, MIDDLEWARE, REPORT
    private String name;
    private String description;
    private String icon;
    private Boolean isSystem;  // 是否系统预置
    private Map<String, Object> attributeSchema;  // 属性定义Schema（为F02-1预留）
    
    // 业务方法
    public boolean isSystemType();
    public void validateAttributes(Map<String, Object> attributes);
}
```

### 3.3 值对象：ResourceStatus

**职责**：资源状态枚举

```java
package com.catface996.aiops.domain.model.resource;

/**
 * 资源状态值对象
 */
public enum ResourceStatus {
    RUNNING("运行中", "green"),
    STOPPED("已停止", "gray"),
    MAINTENANCE("维护中", "yellow"),
    OFFLINE("已下线", "red");
    
    private final String displayName;
    private final String badgeColor;
    
    // 业务方法
    public boolean canTransitionTo(ResourceStatus target);
}
```



---

## 4. 数据模型设计

### 4.1 数据库表结构

#### 4.1.1 资源类型表 (resource_type)

```sql
CREATE TABLE resource_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    code VARCHAR(50) UNIQUE NOT NULL COMMENT '类型编码：SERVER, APPLICATION, DATABASE, API, MIDDLEWARE, REPORT',
    name VARCHAR(100) NOT NULL COMMENT '类型名称',
    description TEXT COMMENT '类型描述',
    icon VARCHAR(100) COMMENT '图标URL',
    is_system BOOLEAN DEFAULT TRUE COMMENT '是否系统预置',
    attribute_schema JSON COMMENT '属性定义Schema（为F02-1预留）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    INDEX idx_code (code),
    INDEX idx_is_system (is_system)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源类型表';
```

#### 4.1.2 资源表 (resource)

```sql
CREATE TABLE resource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(255) NOT NULL COMMENT '资源名称',
    description TEXT COMMENT '资源描述',
    resource_type_id BIGINT NOT NULL COMMENT '资源类型ID',
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING' COMMENT '状态：RUNNING, STOPPED, MAINTENANCE, OFFLINE',
    attributes JSON COMMENT '扩展属性（JSON格式）',
    version INT DEFAULT 0 COMMENT '版本号（乐观锁）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建者（第一个Owner）',
    FOREIGN KEY (resource_type_id) REFERENCES resource_type(id),
    INDEX idx_name (name),
    INDEX idx_type (resource_type_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at DESC),
    INDEX idx_updated_at (updated_at DESC),
    UNIQUE KEY uk_type_name (resource_type_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源表';
```

#### 4.1.3 资源标签关联表 (resource_tag)

```sql
CREATE TABLE resource_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by BIGINT COMMENT '创建人ID',
    FOREIGN KEY (resource_id) REFERENCES resource(id) ON DELETE CASCADE,
    UNIQUE KEY uk_resource_tag (resource_id, tag_name),
    INDEX idx_tag_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源标签关联表';
```

#### 4.1.4 审计日志表 (resource_audit_log)

```sql
CREATE TABLE resource_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    operation VARCHAR(20) NOT NULL COMMENT '操作：CREATE, UPDATE, DELETE, STATUS_CHANGE',
    old_value JSON COMMENT '旧值',
    new_value JSON COMMENT '新值',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) COMMENT '操作人姓名',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    FOREIGN KEY (resource_id) REFERENCES resource(id) ON DELETE CASCADE,
    INDEX idx_resource_id (resource_id),
    INDEX idx_created_at (created_at DESC),
    INDEX idx_operator_id (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源审计日志表';
```

### 4.2 索引设计策略

| 表名 | 索引名 | 字段 | 类型 | 用途 |
|------|--------|------|------|------|
| resource | idx_name | name | BTREE | 名称搜索 |
| resource | idx_type | resource_type_id | BTREE | 类型过滤 |
| resource | idx_created_at | created_at DESC | BTREE | 时间排序 |
| resource | uk_type_name | resource_type_id, name | UNIQUE | 唯一性约束 |
| resource_tag | idx_tag_name | tag_name | BTREE | 标签过滤 |
| resource_audit_log | idx_resource_id | resource_id | BTREE | 审计查询 |

### 4.3 数据模型扩展性设计

**为F02-1预留的扩展点**：

1. **resource_type.attribute_schema**：
   - 存储资源类型的属性定义（JSON Schema格式）
   - MVP阶段为NULL，F02-1阶段填充

2. **resource.attributes**：
   - 使用JSON存储扩展属性
   - 支持动态字段，无需修改表结构

3. **resource_type.is_system**：
   - 区分系统预置类型和自定义类型
   - 系统类型不可删除



---

## 5. 组件和接口设计

### 5.1 接口层 (Interface Layer)

#### 5.1.1 ResourceController

```java
package com.catface996.aiops.http.controller.resource;

@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController {
    
    @Autowired
    private ResourceApplicationService resourceApplicationService;
    
    /**
     * 创建资源
     * REQ-FR-001, REQ-FR-002, REQ-FR-003, REQ-FR-004, REQ-FR-005
     */
    @PostMapping
    public Result<ResourceResponse> createResource(@Valid @RequestBody CreateResourceRequest request);
    
    /**
     * 查看资源列表
     * REQ-FR-006, REQ-FR-007, REQ-FR-008, REQ-FR-009, REQ-FR-010
     */
    @GetMapping
    public Result<PageResult<ResourceListItemResponse>> listResources(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Long typeId,
        @RequestParam(required = false) List<String> tags,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String sortOrder,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "20") Integer pageSize
    );
    
    /**
     * 查看资源详情
     * REQ-FR-021, REQ-FR-022, REQ-FR-023, REQ-FR-024
     */
    @GetMapping("/{id}")
    public Result<ResourceDetailResponse> getResourceDetail(@PathVariable Long id);
    
    /**
     * 编辑资源
     * REQ-FR-011, REQ-FR-012, REQ-FR-013, REQ-FR-014, REQ-FR-015
     */
    @PutMapping("/{id}")
    public Result<ResourceResponse> updateResource(
        @PathVariable Long id,
        @Valid @RequestBody UpdateResourceRequest request
    );
    
    /**
     * 删除资源
     * REQ-FR-016, REQ-FR-017, REQ-FR-018, REQ-FR-019, REQ-FR-020
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteResource(
        @PathVariable Long id,
        @RequestParam String confirmName
    );
    
    /**
     * 更新资源状态
     * REQ-FR-027, REQ-FR-028
     */
    @PatchMapping("/{id}/status")
    public Result<Void> updateResourceStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateStatusRequest request
    );
    
    /**
     * 获取资源操作历史
     * REQ-FR-025
     */
    @GetMapping("/{id}/audit-logs")
    public Result<PageResult<AuditLogResponse>> getAuditLogs(
        @PathVariable Long id,
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize
    );
}
```

### 5.2 应用层 (Application Layer)

#### 5.2.1 ResourceApplicationService

```java
package com.catface996.aiops.application.service.resource;

@Service
public class ResourceApplicationServiceImpl implements ResourceApplicationService {
    
    @Autowired
    private ResourceDomainService resourceDomainService;
    
    /**
     * 创建资源
     * 职责：事务控制、DTO转换、权限验证
     */
    @Transactional
    @Override
    public ResourceDTO createResource(CreateResourceCommand command) {
        // 1. DTO转换
        Resource resource = convertToResource(command);
        
        // 2. 调用领域服务
        Resource created = resourceDomainService.createResource(resource, command.getUserId());
        
        // 3. 返回DTO
        return convertToDTO(created);
    }
    
    /**
     * 查询资源列表
     * 职责：分页控制、DTO转换
     */
    @Override
    public PageResult<ResourceListItemDTO> listResources(ResourceQuery query) {
        // 调用领域服务
        PageResult<Resource> pageResult = resourceDomainService.listResources(query);
        
        // DTO转换
        return convertToListItemDTOs(pageResult);
    }
    
    /**
     * 更新资源
     * 职责：事务控制、权限验证、乐观锁处理
     */
    @Transactional
    @Override
    public ResourceDTO updateResource(Long id, UpdateResourceCommand command) {
        // 1. 权限验证
        resourceDomainService.checkOwnerPermission(id, command.getUserId());
        
        // 2. 调用领域服务
        Resource updated = resourceDomainService.updateResource(id, command);
        
        // 3. 返回DTO
        return convertToDTO(updated);
    }
    
    /**
     * 删除资源
     * 职责：事务控制、权限验证、关联检查
     */
    @Transactional
    @Override
    public void deleteResource(Long id, String confirmName, Long userId) {
        // 1. 权限验证
        resourceDomainService.checkOwnerPermission(id, userId);
        
        // 2. 名称确认
        resourceDomainService.validateDeleteConfirmation(id, confirmName);
        
        // 3. 关联检查
        List<String> dependencies = resourceDomainService.checkDependencies(id);
        
        // 4. 删除资源
        resourceDomainService.deleteResource(id, userId);
    }
}
```

### 5.3 领域层 (Domain Layer)

#### 5.3.1 ResourceDomainService

```java
package com.catface996.aiops.domain.service.resource;

@Service
public class ResourceDomainServiceImpl implements ResourceDomainService {
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private ResourceCacheService resourceCacheService;
    
    @Autowired
    private EncryptionService encryptionService;
    
    @Autowired
    private AuditLogService auditLogService;
    
    /**
     * 创建资源
     * 职责：业务规则验证、敏感信息加密、审计日志
     */
    @Override
    public Resource createResource(Resource resource, Long userId) {
        // 1. 业务规则验证
        resource.validateForCreation();
        
        // 2. 敏感信息加密
        encryptSensitiveAttributes(resource);
        
        // 3. 设置创建者为Owner
        resource.setCreatedBy(userId);
        
        // 4. 保存资源
        Resource created = resourceRepository.save(resource);
        
        // 5. 记录审计日志
        auditLogService.logCreate(created, userId);
        
        // 6. 清除缓存
        resourceCacheService.evictListCache();
        
        return created;
    }
    
    /**
     * 查询资源列表
     * 职责：缓存查询、分页查询
     */
    @Override
    public PageResult<Resource> listResources(ResourceQuery query) {
        // 1. 尝试从缓存获取
        String cacheKey = buildCacheKey(query);
        PageResult<Resource> cached = resourceCacheService.getListCache(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // 2. 数据库查询
        PageResult<Resource> result = resourceRepository.listByQuery(query);
        
        // 3. 写入缓存（只缓存前3页）
        if (query.getPageNum() <= 3) {
            resourceCacheService.putListCache(cacheKey, result, 5 * 60); // 5分钟TTL
        }
        
        return result;
    }
    
    /**
     * 更新资源
     * 职责：权限检查、乐观锁、敏感信息加密、审计日志
     */
    @Override
    public Resource updateResource(Long id, UpdateResourceCommand command) {
        // 1. 查询资源
        Resource resource = resourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(id));
        
        // 2. 乐观锁检查
        if (!resource.getVersion().equals(command.getVersion())) {
            throw new OptimisticLockException("资源已被他人修改，请刷新后重试");
        }
        
        // 3. 更新信息
        resource.updateInfo(command.getName(), command.getDescription(), command.getAttributes());
        
        // 4. 敏感信息加密
        encryptSensitiveAttributes(resource);
        
        // 5. 保存资源
        Resource updated = resourceRepository.update(resource);
        
        // 6. 记录审计日志
        auditLogService.logUpdate(resource, updated, command.getUserId());
        
        // 7. 清除缓存
        resourceCacheService.evictResourceCache(id);
        resourceCacheService.evictListCache();
        
        return updated;
    }
    
    /**
     * 删除资源
     * 职责：关联检查、物理删除、审计日志
     */
    @Override
    public void deleteResource(Long id, Long userId) {
        // 1. 查询资源
        Resource resource = resourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(id));
        
        // 2. 物理删除
        resourceRepository.deleteById(id);
        
        // 3. 记录审计日志
        auditLogService.logDelete(resource, userId);
        
        // 4. 清除缓存
        resourceCacheService.evictResourceCache(id);
        resourceCacheService.evictListCache();
    }
    
    /**
     * 加密敏感属性
     */
    private void encryptSensitiveAttributes(Resource resource) {
        Map<String, Object> attributes = resource.getAttributes();
        if (attributes == null) return;
        
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (key.contains("password") || key.contains("secret") || key.contains("key")) {
                String encrypted = encryptionService.encrypt(entry.getValue().toString());
                entry.setValue(encrypted);
            }
        }
    }
}
```



---

## 6. 安全方案设计

### 6.1 敏感信息加密

#### 6.1.1 加密服务接口

```java
package com.catface996.aiops.security;

public interface EncryptionService {
    /**
     * 加密敏感信息
     * @param plainText 明文
     * @return 密文（Base64编码）
     */
    String encrypt(String plainText);
    
    /**
     * 解密敏感信息
     * @param cipherText 密文（Base64编码）
     * @return 明文
     */
    String decrypt(String cipherText);
}
```

#### 6.1.2 AES-256加密实现

```java
package com.catface996.aiops.security.impl;

@Service
public class AesEncryptionServiceImpl implements EncryptionService {
    
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256;
    
    @Value("${security.encryption.key}")
    private String encryptionKey;
    
    @Value("${security.encryption.iv}")
    private String initVector;
    
    @Override
    public String encrypt(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new EncryptionException("加密失败", e);
        }
    }
    
    @Override
    public String decrypt(String cipherText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(initVector.getBytes(StandardCharsets.UTF_8));
            
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionException("解密失败", e);
        }
    }
}
```

### 6.2 密钥管理方案

#### 6.2.1 配置文件存储（MVP阶段）

```yaml
# application-prod.yml
security:
  encryption:
    # 密钥（256位，32字节）- 生产环境使用环境变量
    key: ${ENCRYPTION_KEY:default-32-byte-key-for-aes256}
    # 初始化向量（128位，16字节）
    iv: ${ENCRYPTION_IV:default-16-iv-key}
```

#### 6.2.2 密钥轮换策略（未来）

```java
/**
 * 密钥轮换服务（未来实现）
 * 1. 定期轮换密钥（每90天）
 * 2. 使用新密钥加密新数据
 * 3. 后台任务重新加密旧数据
 * 4. 迁移到AWS KMS
 */
public interface KeyRotationService {
    void rotateKey();
    void reEncryptOldData(String oldKey, String newKey);
}
```

### 6.3 权限验证流程

```java
/**
 * 权限验证切面
 */
@Aspect
@Component
public class ResourcePermissionAspect {
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    /**
     * 验证Owner权限
     */
    @Before("@annotation(RequireOwnerPermission)")
    public void checkOwnerPermission(JoinPoint joinPoint) {
        Long resourceId = extractResourceId(joinPoint);
        Long userId = getCurrentUserId();
        
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new ResourceNotFoundException(resourceId));
        
        if (!resource.isOwner(userId)) {
            throw new PermissionDeniedException("无权限操作此资源");
        }
    }
}
```

---

## 7. 性能优化方案

### 7.1 数据库索引优化

#### 7.1.1 索引创建脚本

```sql
-- 列表查询优化（REQ-NFR-001）
CREATE INDEX idx_resource_list ON resource(resource_type_id, created_at DESC);

-- 名称搜索优化（REQ-NFR-002）
CREATE INDEX idx_resource_name ON resource(name);

-- 标签过滤优化
CREATE INDEX idx_tag_name ON resource_tag(tag_name);

-- 审计日志查询优化
CREATE INDEX idx_audit_resource_time ON resource_audit_log(resource_id, created_at DESC);
```

#### 7.1.2 查询优化策略

| 场景 | 优化策略 | 预期效果 |
|------|---------|---------|
| 列表查询 | 复合索引 + LIMIT | < 1秒 |
| 名称搜索 | 前缀索引 + 防抖 | < 500ms |
| 标签过滤 | 索引 + IN查询 | < 500ms |
| 详情查询 | 主键查询 + 缓存 | < 100ms |

### 7.2 Redis缓存策略

#### 7.2.1 缓存层次

```
L1: 本地缓存（Caffeine）- 热点资源详情
  └─> TTL: 5分钟，容量: 1000个

L2: Redis缓存 - 列表和详情
  └─> TTL: 5-30分钟，容量: 无限制
```

#### 7.2.2 缓存Key设计

```java
public class ResourceCacheKeys {
    // 资源详情：resource:detail:{id}
    public static String detailKey(Long id) {
        return String.format("resource:detail:%d", id);
    }
    
    // 资源列表：resource:list:{typeId}:{keyword}:{tags}:{page}:{size}
    public static String listKey(ResourceQuery query) {
        return String.format("resource:list:%s:%s:%s:%d:%d",
            query.getTypeId(),
            query.getKeyword(),
            String.join(",", query.getTags()),
            query.getPageNum(),
            query.getPageSize()
        );
    }
    
    // 资源类型列表：resource:types
    public static String typesKey() {
        return "resource:types";
    }
}
```

#### 7.2.3 缓存失效策略

```java
@Service
public class ResourceCacheServiceImpl implements ResourceCacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 缓存资源详情
     */
    @Override
    public void cacheResourceDetail(Long id, Resource resource) {
        String key = ResourceCacheKeys.detailKey(id);
        redisTemplate.opsForValue().set(key, resource, 5, TimeUnit.MINUTES);
    }
    
    /**
     * 缓存列表（只缓存前3页）
     */
    @Override
    public void cacheResourceList(ResourceQuery query, PageResult<Resource> result) {
        if (query.getPageNum() > 3) return;
        
        String key = ResourceCacheKeys.listKey(query);
        redisTemplate.opsForValue().set(key, result, 5, TimeUnit.MINUTES);
    }
    
    /**
     * 清除资源缓存
     */
    @Override
    public void evictResourceCache(Long id) {
        String key = ResourceCacheKeys.detailKey(id);
        redisTemplate.delete(key);
    }
    
    /**
     * 清除列表缓存（模糊匹配）
     */
    @Override
    public void evictListCache() {
        Set<String> keys = redisTemplate.keys("resource:list:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
```

### 7.3 分页查询优化

#### 7.3.1 游标分页（避免深分页）

```java
/**
 * 使用游标分页代替OFFSET
 * 适用场景：按时间排序的列表
 */
public PageResult<Resource> listResourcesByCursor(Long lastId, Integer pageSize) {
    // 使用WHERE id > lastId代替OFFSET
    List<Resource> resources = resourceRepository.selectList(
        new LambdaQueryWrapper<Resource>()
            .gt(Resource::getId, lastId)
            .orderByDesc(Resource::getCreatedAt)
            .last("LIMIT " + pageSize)
    );
    
    return new PageResult<>(resources, resources.size() == pageSize);
}
```

### 7.4 性能监控

```java
/**
 * 性能监控切面
 */
@Aspect
@Component
public class PerformanceMonitorAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitorAspect.class);
    
    @Around("execution(* com.catface996.aiops.application.service..*(..))")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // 慢查询告警（>800ms）
            if (duration > 800) {
                logger.warn("慢查询告警: {} 耗时 {}ms", 
                    joinPoint.getSignature().toShortString(), duration);
            }
        }
    }
}
```



---

## 8. API接口设计

### 8.1 REST API端点

| 端点 | 方法 | 描述 | 需求ID |
|------|------|------|--------|
| `/api/v1/resources` | POST | 创建资源 | REQ-FR-001~005 |
| `/api/v1/resources` | GET | 查询资源列表 | REQ-FR-006~010 |
| `/api/v1/resources/{id}` | GET | 查询资源详情 | REQ-FR-021~024 |
| `/api/v1/resources/{id}` | PUT | 更新资源 | REQ-FR-011~015 |
| `/api/v1/resources/{id}` | DELETE | 删除资源 | REQ-FR-016~020 |
| `/api/v1/resources/{id}/status` | PATCH | 更新状态 | REQ-FR-027~028 |
| `/api/v1/resources/{id}/audit-logs` | GET | 查询审计日志 | REQ-FR-025 |
| `/api/v1/resource-types` | GET | 查询资源类型列表 | REQ-FR-026 |

### 8.2 Request/Response DTO

#### 8.2.1 创建资源请求

```java
@Data
public class CreateResourceRequest {
    @NotNull(message = "资源类型不能为空")
    private Long typeId;
    
    @NotBlank(message = "资源名称不能为空")
    @Size(min = 2, max = 100, message = "资源名称长度必须在2-100之间")
    private String name;
    
    @Size(max = 500, message = "描述长度不能超过500")
    private String description;
    
    @Size(max = 10, message = "标签数量不能超过10个")
    private Set<@Size(max = 20, message = "标签长度不能超过20") String> tags;
    
    private Map<String, Object> attributes;
}
```

#### 8.2.2 资源列表响应

```java
@Data
public class ResourceListItemResponse {
    private Long id;
    private String name;
    private String typeName;
    private String status;
    private String statusBadgeColor;
    private Set<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### 8.2.3 资源详情响应

```java
@Data
public class ResourceDetailResponse {
    private Long id;
    private String name;
    private String description;
    private ResourceTypeDTO type;
    private String status;
    private Map<String, Object> attributes;  // 敏感信息已脱敏
    private Integer version;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<String> tags;
    private List<OwnerDTO> owners;
    private List<ViewerDTO> viewers;
}
```

### 8.3 错误码定义

```java
public enum ResourceErrorCode {
    // 通用错误 (1000-1099)
    RESOURCE_NOT_FOUND(1001, "资源不存在"),
    RESOURCE_NAME_DUPLICATE(1002, "资源名称已存在"),
    
    // 权限错误 (1100-1199)
    PERMISSION_DENIED(1101, "无权限操作此资源"),
    NOT_OWNER(1102, "只有Owner可以执行此操作"),
    
    // 验证错误 (1200-1299)
    INVALID_RESOURCE_NAME(1201, "资源名称格式不正确"),
    TOO_MANY_TAGS(1202, "标签数量超过限制"),
    INVALID_ATTRIBUTES(1203, "扩展属性格式不正确"),
    
    // 并发错误 (1300-1399)
    OPTIMISTIC_LOCK_FAILED(1301, "资源已被他人修改，请刷新后重试"),
    
    // 删除错误 (1400-1499)
    DELETE_CONFIRMATION_FAILED(1401, "删除确认失败，请输入正确的资源名称"),
    RESOURCE_HAS_DEPENDENCIES(1402, "资源存在关联关系，无法删除"),
    
    // 加密错误 (1500-1599)
    ENCRYPTION_FAILED(1501, "敏感信息加密失败"),
    DECRYPTION_FAILED(1502, "敏感信息解密失败");
    
    private final int code;
    private final String message;
}
```

### 8.4 统一响应格式

```java
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
    
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("success");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
    
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
}
```

---

## 9. 错误处理设计

### 9.1 全局异常处理器

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        logger.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 资源未找到异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public Result<Void> handleResourceNotFoundException(ResourceNotFoundException e) {
        logger.warn("资源未找到: {}", e.getMessage());
        return Result.error(ResourceErrorCode.RESOURCE_NOT_FOUND.getCode(), e.getMessage());
    }
    
    /**
     * 权限异常
     */
    @ExceptionHandler(PermissionDeniedException.class)
    public Result<Void> handlePermissionDeniedException(PermissionDeniedException e) {
        logger.warn("权限异常: {}", e.getMessage());
        return Result.error(ResourceErrorCode.PERMISSION_DENIED.getCode(), e.getMessage());
    }
    
    /**
     * 乐观锁异常
     */
    @ExceptionHandler(OptimisticLockException.class)
    public Result<Void> handleOptimisticLockException(OptimisticLockException e) {
        logger.warn("乐观锁冲突: {}", e.getMessage());
        return Result.error(ResourceErrorCode.OPTIMISTIC_LOCK_FAILED.getCode(), e.getMessage());
    }
    
    /**
     * 参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));
        logger.warn("参数验证失败: {}", message);
        return Result.error(400, message);
    }
    
    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        logger.error("系统异常", e);
        return Result.error(500, "系统内部错误");
    }
}
```

### 9.2 异常类层次

```
Exception
  └─> RuntimeException
        ├─> BusinessException (业务异常基类)
        │     ├─> ResourceNotFoundException (资源未找到)
        │     ├─> PermissionDeniedException (权限拒绝)
        │     ├─> OptimisticLockException (乐观锁冲突)
        │     ├─> ValidationException (验证失败)
        │     └─> EncryptionException (加密失败)
        └─> SystemException (系统异常基类)
```

---

## 10. 测试策略

### 10.1 单元测试

#### 10.1.1 领域服务测试

```java
@ExtendWith(MockitoExtension.class)
class ResourceDomainServiceTest {
    
    @Mock
    private ResourceRepository resourceRepository;
    
    @Mock
    private EncryptionService encryptionService;
    
    @Mock
    private AuditLogService auditLogService;
    
    @InjectMocks
    private ResourceDomainServiceImpl resourceDomainService;
    
    @Test
    @DisplayName("创建资源 - 成功场景")
    void testCreateResource_Success() {
        // Given
        Resource resource = buildTestResource();
        when(resourceRepository.save(any())).thenReturn(resource);
        
        // When
        Resource created = resourceDomainService.createResource(resource, 1L);
        
        // Then
        assertNotNull(created);
        assertEquals("test-resource", created.getName());
        verify(encryptionService).encrypt(anyString());
        verify(auditLogService).logCreate(any(), anyLong());
    }
    
    @Test
    @DisplayName("更新资源 - 乐观锁冲突")
    void testUpdateResource_OptimisticLockFailed() {
        // Given
        Resource resource = buildTestResource();
        resource.setVersion(1);
        UpdateResourceCommand command = new UpdateResourceCommand();
        command.setVersion(0);  // 版本不匹配
        
        when(resourceRepository.findById(anyLong())).thenReturn(Optional.of(resource));
        
        // When & Then
        assertThrows(OptimisticLockException.class, () -> {
            resourceDomainService.updateResource(1L, command);
        });
    }
}
```

#### 10.1.2 测试覆盖率目标

| 层级 | 覆盖率目标 | 说明 |
|------|-----------|------|
| Domain层 | ≥ 80% | 核心业务逻辑 |
| Application层 | ≥ 70% | 用例编排 |
| Infrastructure层 | ≥ 60% | 技术实现 |
| **总体** | **≥ 70%** | 项目要求 |

### 10.2 集成测试

```java
@SpringBootTest
@Transactional
class ResourceIntegrationTest {
    
    @Autowired
    private ResourceApplicationService resourceApplicationService;
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @Test
    @DisplayName("资源完整生命周期测试")
    void testResourceLifecycle() {
        // 1. 创建资源
        CreateResourceCommand createCommand = buildCreateCommand();
        ResourceDTO created = resourceApplicationService.createResource(createCommand);
        assertNotNull(created.getId());
        
        // 2. 查询资源
        ResourceDTO queried = resourceApplicationService.getResourceDetail(created.getId());
        assertEquals(created.getName(), queried.getName());
        
        // 3. 更新资源
        UpdateResourceCommand updateCommand = buildUpdateCommand(created.getId(), created.getVersion());
        ResourceDTO updated = resourceApplicationService.updateResource(created.getId(), updateCommand);
        assertEquals(updateCommand.getName(), updated.getName());
        
        // 4. 删除资源
        resourceApplicationService.deleteResource(created.getId(), created.getName(), 1L);
        assertFalse(resourceRepository.findById(created.getId()).isPresent());
    }
}
```

### 10.3 性能测试

```java
@SpringBootTest
class ResourcePerformanceTest {
    
    @Autowired
    private ResourceApplicationService resourceApplicationService;
    
    @Test
    @DisplayName("列表查询性能测试 - 10000资源场景")
    void testListPerformance_10000Resources() {
        // Given: 准备10000条测试数据
        prepareTestData(10000);
        
        // When: 查询列表
        long startTime = System.currentTimeMillis();
        ResourceQuery query = new ResourceQuery();
        query.setPageNum(1);
        query.setPageSize(20);
        PageResult<ResourceListItemDTO> result = resourceApplicationService.listResources(query);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then: 验证性能
        assertNotNull(result);
        assertTrue(duration < 1000, "列表查询耗时应小于1秒，实际耗时: " + duration + "ms");
    }
}
```

---

## 11. 部署方案

### 11.1 环境配置

#### 11.1.1 开发环境 (local)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aiops_dev
    username: root
    password: ${DB_PASSWORD}
  
  redis:
    host: localhost
    port: 6379

security:
  encryption:
    key: dev-32-byte-key-for-aes-256!!
    iv: dev-16-byte-iv!!

logging:
  level:
    com.catface996.aiops: DEBUG
```

#### 11.1.2 生产环境 (prod)

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:3306/aiops_prod
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  
  redis:
    host: ${REDIS_HOST}
    port: 6379
    password: ${REDIS_PASSWORD}

security:
  encryption:
    key: ${ENCRYPTION_KEY}
    iv: ${ENCRYPTION_IV}

logging:
  level:
    com.catface996.aiops: INFO
```

### 11.2 监控指标

| 指标类型 | 指标名称 | 告警阈值 | 说明 |
|---------|---------|---------|------|
| 性能 | 列表查询时间 | > 800ms | REQ-NFR-001 |
| 性能 | 搜索响应时间 | > 400ms | REQ-NFR-002 |
| 容量 | 资源总数 | > 8000 | 接近容量上限 |
| 错误 | 乐观锁冲突率 | > 5% | 并发冲突频繁 |
| 安全 | 加密失败次数 | > 0 | 加密异常 |

---

## 12. 风险和缓解

### 12.1 技术风险

| 风险ID | 风险描述 | 缓解方案 | 责任人 |
|-------|---------|---------|--------|
| RISK-001 | 列表查询性能 | 索引优化+Redis缓存+游标分页 | 后端团队 |
| RISK-002 | JSON查询性能 | 虚拟列索引+限制JSON深度 | 后端团队 |
| RISK-005 | 密钥管理复杂 | 配置文件存储+未来迁移KMS | 安全团队 |

### 12.2 实施风险

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|---------|
| F11权限系统延期 | 中 | 中 | 先实现基础权限，后续集成 |
| 性能测试不达标 | 低 | 高 | 提前进行性能测试，预留优化时间 |
| 数据迁移问题 | 低 | 中 | 提供数据迁移脚本和回滚方案 |

---

## 13. 附录

### 13.1 设计决策记录 (ADR)

**ADR-001: 使用JSON存储扩展属性**
- **决策**: 使用JSON字段存储资源的扩展属性
- **理由**: 为F02-1预留扩展性，避免频繁修改表结构
- **权衡**: 查询性能略低，但可通过虚拟列索引优化
- **状态**: 已采纳

**ADR-002: 采用乐观锁而非悲观锁**
- **决策**: 使用version字段实现乐观锁
- **理由**: 并发冲突概率低，乐观锁性能更好
- **权衡**: 冲突时需要用户重试
- **状态**: 已采纳

**ADR-003: 物理删除而非软删除**
- **决策**: 删除资源时执行物理删除（DELETE）
- **理由**: 用户明确要求，且有审计日志记录
- **权衡**: 无法恢复已删除资源
- **状态**: 已采纳

### 13.2 参考文档

- 需求规格文档: `.kiro/specs/f03-resource-management/requirements.md`
- 需求澄清文档: `.kiro/specs/f03-resource-management/requirement-clarification.md`
- 需求验证文档: `.kiro/specs/f03-resource-management/requirement-verification.md`
- 技术栈文档: `tech.md`
- 架构指南: `ARCHITECTURE_GUIDELINES.md`

---

**文档版本**: v1.0  
**最后更新**: 2024-11-30  
**下一步**: 创建任务拆分文档 (tasks.md)
