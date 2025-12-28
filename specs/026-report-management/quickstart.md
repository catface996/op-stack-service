# Quickstart: Report Management

## Prerequisites

- Java 21+
- MySQL 8.0
- Maven 3.9+

## Setup

1. **Apply database migrations**

```bash
# Flyway 会自动执行 V14__create_report_tables.sql
mvn flyway:migrate -pl bootstrap
```

2. **Build the project**

```bash
mvn clean package -DskipTests
```

3. **Start the service**

```bash
java -jar bootstrap/target/bootstrap-1.0.0-SNAPSHOT.jar --spring.profiles.active=local --server.port=8081
```

## API Examples

### Report Management

#### List Reports

```bash
curl -X POST http://localhost:8081/api/service/v1/reports/list \
  -H "Content-Type: application/json" \
  -d '{
    "page": 1,
    "size": 10,
    "type": "Security",
    "status": "Final",
    "keyword": "audit",
    "sortBy": "created_at",
    "sortOrder": "desc"
  }'
```

#### Get Report Detail

```bash
curl -X POST http://localhost:8081/api/service/v1/reports/get \
  -H "Content-Type: application/json" \
  -d '{
    "reportId": 1
  }'
```

#### Create Report

```bash
curl -X POST http://localhost:8081/api/service/v1/reports/create \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Q4 Security Audit Report",
    "type": "Security",
    "status": "Final",
    "author": "System Admin",
    "summary": "Comprehensive security assessment for Q4 2024",
    "content": "# Security Audit Report\n\n## Executive Summary\n\nThis report covers...",
    "tags": ["security", "compliance", "Q4"],
    "topologyId": 1
  }'
```

#### Delete Report

```bash
curl -X POST http://localhost:8081/api/service/v1/reports/delete \
  -H "Content-Type: application/json" \
  -d '{
    "reportId": 1
  }'
```

### Report Template Management

#### List Templates

```bash
curl -X POST http://localhost:8081/api/service/v1/report-templates/list \
  -H "Content-Type: application/json" \
  -d '{
    "page": 1,
    "size": 10,
    "category": "Security",
    "keyword": "audit"
  }'
```

#### Get Template Detail

```bash
curl -X POST http://localhost:8081/api/service/v1/report-templates/get \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": 1
  }'
```

#### Create Template

```bash
curl -X POST http://localhost:8081/api/service/v1/report-templates/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Security Audit Template",
    "description": "Template for security audit reports",
    "category": "Security",
    "content": "# {{Report_Title}}\n\n## Executive Summary\n{{Executive_Summary}}\n\n## Findings\n{{Findings}}",
    "tags": ["security", "audit"]
  }'
```

#### Update Template

```bash
curl -X POST http://localhost:8081/api/service/v1/report-templates/update \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": 1,
    "name": "Updated Security Audit Template",
    "description": "Updated description",
    "version": 0
  }'
```

#### Delete Template

```bash
curl -X POST http://localhost:8081/api/service/v1/report-templates/delete \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": 1
  }'
```

## Response Format

### Success Response

```json
{
  "code": 0,
  "success": true,
  "message": "Success",
  "data": { ... }
}
```

### Paginated Response

```json
{
  "code": 0,
  "success": true,
  "message": "Success",
  "data": {
    "content": [ ... ],
    "page": 1,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3,
    "first": true,
    "last": false
  }
}
```

### Error Response

```json
{
  "code": 404001,
  "success": false,
  "message": "Report not found",
  "data": null
}
```

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| 0 | 200 | Success |
| 400001 | 400 | Invalid request parameters |
| 400002 | 400 | Report title is required |
| 400003 | 400 | Invalid report type |
| 400004 | 400 | Invalid report status |
| 404001 | 404 | Report not found |
| 404002 | 404 | Report template not found |
| 404003 | 404 | Topology not found |
| 409001 | 409 | Version conflict |
| 500001 | 500 | Internal server error |

## Swagger UI

访问 Swagger UI 查看完整的 API 文档：

```
http://localhost:8081/swagger-ui.html
```
