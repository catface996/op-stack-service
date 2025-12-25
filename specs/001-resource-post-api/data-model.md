# Data Model: 资源管理接口统一改为POST方式

**Feature Branch**: `001-resource-post-api`
**Date**: 2025-12-25

## 概述

本文档定义了接口改造所需的请求对象（Request DTO）数据模型。本次变更不涉及数据库实体或持久化对象的修改。

## 请求对象定义

### 1. GetResourceRequest (新增)

用于查询单个资源详情。

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "查询资源详情请求")
public class GetResourceRequest {

    @Schema(description = "资源ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源ID不能为空")
    private Long id;
}
```

**字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 资源ID |

### 2. GetResourceAuditLogsRequest (新增)

用于查询资源审计日志。

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "查询资源审计日志请求")
public class GetResourceAuditLogsRequest {

    @Schema(description = "资源ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源ID不能为空")
    private Long id;

    @Schema(description = "页码（从1开始）", example = "1")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer size = 10;
}
```

**字段说明**:

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| id | Long | 是 | - | 资源ID |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 10 | 每页大小 |

### 3. UpdateResourceRequest (修改)

添加 `id` 字段。

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新资源请求")
public class UpdateResourceRequest {

    @Schema(description = "资源ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源ID不能为空")
    private Long id;  // 新增字段

    @Schema(description = "资源名称（null表示不修改）", example = "web-server-02")
    @Size(max = 100, message = "资源名称最长100个字符")
    private String name;

    @Schema(description = "资源描述（null表示不修改）", example = "测试环境Web服务器")
    @Size(max = 500, message = "资源描述最长500个字符")
    private String description;

    @Schema(description = "扩展属性（JSON格式，null表示不修改）")
    private String attributes;

    @Schema(description = "当前版本号（乐观锁）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "版本号不能为空")
    private Integer version;
}
```

**变更点**: 新增 `id` 字段，原 `@PathVariable Long id` 参数移入请求体。

### 4. DeleteResourceRequest (修改)

添加 `id` 字段。

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "删除资源请求")
public class DeleteResourceRequest {

    @Schema(description = "资源ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源ID不能为空")
    private Long id;  // 新增字段

    @Schema(description = "确认的资源名称（必须与资源名称完全匹配）",
            example = "web-server-01", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "确认名称不能为空")
    private String confirmName;
}
```

**变更点**: 新增 `id` 字段，原 `@PathVariable Long id` 参数移入请求体。

### 5. UpdateResourceStatusRequest (修改)

添加 `id` 字段。

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新资源状态请求")
public class UpdateResourceStatusRequest {

    @Schema(description = "资源ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "资源ID不能为空")
    private Long id;  // 新增字段

    @Schema(description = "新状态（RUNNING/STOPPED/MAINTENANCE/OFFLINE）",
            example = "STOPPED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "状态不能为空")
    private String status;

    @Schema(description = "当前版本号（乐观锁）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "版本号不能为空")
    private Integer version;
}
```

**变更点**: 新增 `id` 字段，原 `@PathVariable Long id` 参数移入请求体。

## 无需修改的请求对象

以下请求对象已满足新接口要求，无需修改：

### CreateResourceRequest

已包含创建资源所需的所有字段（name, description, resourceTypeId, attributes 等）。

### ListResourcesRequest

已包含分页和过滤字段（resourceTypeId, status, keyword, page, size）。

## 校验规则汇总

| 请求类 | 字段 | 校验规则 |
|--------|------|----------|
| GetResourceRequest | id | @NotNull |
| GetResourceAuditLogsRequest | id | @NotNull |
| GetResourceAuditLogsRequest | page | @Min(1) |
| GetResourceAuditLogsRequest | size | @Min(1), @Max(100) |
| UpdateResourceRequest | id | @NotNull |
| DeleteResourceRequest | id | @NotNull |
| UpdateResourceStatusRequest | id | @NotNull |

## 文件清单

| 文件 | 路径 | 操作 |
|------|------|------|
| GetResourceRequest.java | application/application-api/src/main/java/.../request/ | 新增 |
| GetResourceAuditLogsRequest.java | application/application-api/src/main/java/.../request/ | 新增 |
| UpdateResourceRequest.java | application/application-api/src/main/java/.../request/ | 修改 |
| DeleteResourceRequest.java | application/application-api/src/main/java/.../request/ | 修改 |
| UpdateResourceStatusRequest.java | application/application-api/src/main/java/.../request/ | 修改 |
