# Quickstart: POST-Only API 重构

**Feature**: 024-post-only-api
**Date**: 2025-12-22

## 快速开始

### 前置条件

- Java 21
- Maven 3.9+
- Docker (MySQL, Redis)
- IDE with Spring Boot support

### 构建项目

```bash
mvn clean package -DskipTests
```

### 运行测试

```bash
mvn test
```

## 重构模式指南

### 模式 1: GET 查询接口 → POST

**原接口**:
```java
@GetMapping
public Result<PageResult<ResourceDTO>> list(
    @RequestParam(required = false) String resourceType,
    @RequestParam(defaultValue = "1") int page,
    @RequestParam(defaultValue = "20") int size) {
    // ...
}
```

**重构后**:
```java
@PostMapping("/query")
public Result<PageResult<ResourceDTO>> query(
    @RequestBody @Valid QueryResourcesRequest request) {
    // ...
}
```

**请求体**:
```java
@Data
public class QueryResourcesRequest {
    private String resourceType;
    @Min(1) private Integer page = 1;
    @Min(1) @Max(100) private Integer size = 20;
    // 网关注入字段（可选）
    private Long tenantId;
    private String traceId;
}
```

### 模式 2: GET + Path Param → POST

**原接口**:
```java
@GetMapping("/{id}")
public Result<ResourceDTO> get(@PathVariable Long id) {
    // ...
}
```

**重构后**:
```java
@PostMapping("/get")
public Result<ResourceDTO> get(@RequestBody @Valid GetResourceRequest request) {
    return resourceService.get(request.getId());
}
```

**请求体**:
```java
@Data
public class GetResourceRequest {
    @NotNull private Long id;
    private Long tenantId;
    private String traceId;
}
```

### 模式 3: PUT/PATCH + Path Param → POST

**原接口**:
```java
@PutMapping("/{id}")
public Result<ResourceDTO> update(
    @PathVariable Long id,
    @RequestBody UpdateResourceRequest request) {
    // ...
}
```

**重构后**:
```java
@PostMapping("/update")
public Result<ResourceDTO> update(@RequestBody @Valid UpdateResourceRequest request) {
    // request.getId() 替代 path param
    return resourceService.update(request);
}
```

**请求体修改**:
```java
@Data
public class UpdateResourceRequest {
    @NotNull private Long id;  // 新增：原 path param
    private String name;
    private Map<String, Object> attributes;
    private Long tenantId;
    private String traceId;
}
```

### 模式 4: DELETE + Path Param → POST

**原接口**:
```java
@DeleteMapping("/{id}")
public Result<Void> delete(@PathVariable Long id) {
    // ...
}
```

**重构后**:
```java
@PostMapping("/delete")
public Result<Void> delete(@RequestBody @Valid DeleteResourceRequest request) {
    return resourceService.delete(request.getId());
}
```

**请求体**:
```java
@Data
public class DeleteResourceRequest {
    @NotNull private Long id;
    private Long tenantId;
    private String traceId;
}
```

## API 调用示例

### 查询资源列表

```bash
# 原: GET /api/v1/resources?resourceType=server&page=1&size=10
# 新:
curl -X POST http://localhost:8080/api/v1/resources/query \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "resourceType": "server",
    "page": 1,
    "size": 10
  }'
```

### 获取单个资源

```bash
# 原: GET /api/v1/resources/123
# 新:
curl -X POST http://localhost:8080/api/v1/resources/get \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"id": 123}'
```

### 更新资源

```bash
# 原: PUT /api/v1/resources/123 -d '{"name": "new-name"}'
# 新:
curl -X POST http://localhost:8080/api/v1/resources/update \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "id": 123,
    "name": "new-name"
  }'
```

### 删除资源

```bash
# 原: DELETE /api/v1/resources/123
# 新:
curl -X POST http://localhost:8080/api/v1/resources/delete \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"id": 123}'
```

## 网关参数注入示例

网关在转发请求时，可以在请求体根级注入参数：

**客户端原始请求**:
```json
{
  "id": 123
}
```

**网关注入后的请求**:
```json
{
  "id": 123,
  "tenantId": 1001,
  "traceId": "trace-abc-123",
  "userId": "user-456"
}
```

后端服务通过 Jackson 自动忽略未知字段的配置，即使 DTO 中未定义这些字段也不会报错。需要使用时，可在 DTO 中添加对应字段。

## 验证清单

重构每个接口后，验证：

- [ ] POST 方法工作正常
- [ ] 请求体参数正确解析
- [ ] 响应格式与原接口一致
- [ ] Swagger 文档正确显示
- [ ] 单元测试通过
- [ ] 集成测试通过
