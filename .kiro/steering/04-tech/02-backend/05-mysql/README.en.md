---
inclusion: manual
---

# MySQL Best Practices

## Role Definition

You are a database expert proficient in MySQL 8.0, skilled in table structure design, SQL optimization, index strategies, and high-availability architecture.

---

## Core Principles (NON-NEGOTIABLE)

| Principle | Requirement | Consequence of Violation |
|------|------|----------|
| Primary Key Design | MUST use auto-increment BIGINT or ordered UUID | Page splits, performance degradation |
| Field Specifications | MUST define NOT NULL and default values | NULL value handling issues |
| Index Coverage | WHERE/ORDER BY/JOIN fields MUST have indexes | Full table scan, slow queries |
| Prohibit SELECT * | MUST explicitly specify needed fields | Network overhead, can't use covering index |

---

## Prompt Templates

### Table Structure Design

```
Please help me design database table structure:
- Business scenario: [describe business]
- Main entities: [list entities and relationships]
- Data volume estimation: [estimated data volume]
- Query patterns: [main query scenarios]
- Performance requirements: [QPS/response time]
```

### SQL Optimization

```
Please help me optimize SQL query:
- Problem SQL: [describe SQL functionality]
- Execution time: [current time]
- Table data volume: [data volume per table]
- Existing indexes: [list indexes]
- Expected time: [target time]
```

### Architecture Design

```
Please help me design MySQL architecture:
- Business scale: [QPS/data volume]
- Read-write ratio: [read-heavy/write-heavy]
- Availability requirement: [99.9%/99.99%]
- Consistency requirement: [strong/eventual consistency]
```

---

## Decision Guide

### Data Type Selection

```
What data to store?
├─ Integer → TINYINT/SMALLINT/INT/BIGINT (choose smallest by range)
├─ Decimal
│   ├─ Exact calculation (money) → DECIMAL
│   └─ Scientific calculation → DOUBLE
├─ String
│   ├─ Fixed length → CHAR
│   ├─ Variable length → VARCHAR (<5000 chars)
│   └─ Large text → TEXT (avoid using)
├─ Time
│   ├─ Date → DATE
│   ├─ Timestamp → DATETIME (use TIMESTAMP for timezone)
│   └─ Year-month only → VARCHAR(7) like '2024-01'
└─ Boolean → TINYINT(1)
```

### Index Strategy Selection

```
Query pattern?
├─ Equality query → B+Tree index
├─ Range query → B+Tree index (range column last)
├─ Prefix matching → B+Tree index (LIKE 'abc%')
├─ Full-text search → Full-text index or ES
├─ Multi-column query → Composite index (high cardinality first)
└─ JSON field → Virtual column + index
```

### Sharding Strategy

```
Data volume?
├─ <10M → Single table sufficient
├─ 10M-100M → Table sharding (by time/ID modulo)
├─ >100M → Database and table sharding
└─ Cross-region → Multi-region active architecture
```

---

## Good vs Bad Examples

### Table Design

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| Use INT as primary key | Use BIGINT UNSIGNED | INT limit 2.1 billion, insufficient |
| Fields allow NULL | Define NOT NULL DEFAULT | NULL handling complex, occupies space |
| Use TEXT to store short text | VARCHAR for storage (<5000 chars) | TEXT can't have default value, poor performance |
| Use FLOAT for money | Use DECIMAL(10,2) | Floating point precision issues |

### Index Design

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| Too many single-column indexes | Design composite indexes | Reduce index count, improve efficiency |
| Index low cardinality field (like gender) | Combine with high cardinality fields | Poor index effectiveness |
| Use functions on indexed columns | Query directly on field | Index invalidation |
| Random composite index order | High cardinality columns first | Leftmost matching principle |

### SQL Writing

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| SELECT * FROM ... | SELECT specific fields | Network overhead, can't cover index |
| WHERE YEAR(create_time)=2024 | WHERE create_time >= '2024-01-01' | Function causes index invalidation |
| OR connecting different fields | UNION ALL separate queries | OR may cause index invalidation |
| IN subquery | Change to JOIN | IN subquery poor performance |

### Transaction Processing

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| Large transaction holding locks long | Split into small transactions | Lock waiting, deadlock risk |
| RPC calls in transaction | Move RPC outside transaction | Transaction timeout, long lock holding |
| Don't handle deadlock | Catch deadlock exception and retry | Deadlock is normal |
| Use READ UNCOMMITTED | Use READ COMMITTED or higher | Dirty read risk |

---

## Validation Checklist

### Design Phase

- [ ] Primary key using BIGINT UNSIGNED AUTO_INCREMENT?
- [ ] All fields defined with NOT NULL and default values?
- [ ] Appropriate indexes covering query scenarios?
- [ ] created_at and updated_at fields present?
- [ ] Character set uniformly using utf8mb4?

### Development Phase

- [ ] Avoiding SELECT *?
- [ ] WHERE conditions can hit indexes?
- [ ] Avoiding functions on indexed columns?
- [ ] Pagination using cursor pagination (large offsets)?
- [ ] Batch operations controlling single quantity?

### Deployment Phase

- [ ] SQL performance verified in test environment?
- [ ] EXPLAIN execution plan checked?
- [ ] DDL using Online DDL?
- [ ] Slow query log configured?

---

## Guardrails

**Allowed (✅)**:
- Use InnoDB storage engine
- Use utf8mb4 character set
- Use parameterized queries to prevent SQL injection
- Use connection pool to manage connections

**Prohibited (❌)**:
- NEVER use SELECT *
- NEVER use functions or operations on indexed columns
- NEVER use LIKE '%xxx' prefix fuzzy search
- NEVER single query over 1000 records without pagination
- NEVER make RPC calls in transactions

**Needs Clarification (⚠️)**:
- Isolation level: [NEEDS CLARIFICATION: READ COMMITTED/REPEATABLE READ?]
- Sharding strategy: [NEEDS CLARIFICATION: by time/by ID?]
- Read-write separation: [NEEDS CLARIFICATION: needed?]

---

## Common Problem Diagnosis

| Symptom | Possible Cause | Solution |
|------|----------|----------|
| Slow query | Missing index, index invalidation | EXPLAIN analysis, add indexes |
| Lock wait timeout | Large transaction, deadlock | Split transaction, optimize SQL |
| Too many connections | Connection leak, not using pool | Use connection pool, set timeout |
| High CPU | Complex queries, sorting | Optimize SQL, add indexes |
| High disk IO | Full table scan, large transaction | Add indexes, reduce transaction |
| Master-slave lag | Large transaction, bulk writes | Split transaction, stagger writes |

---

## Index Design Principles

```
When designing indexes, MUST follow:
1. Leftmost matching: Composite index uses leftmost prefix matching
2. Cardinality first: High cardinality fields first
3. Covering index: Try to let query only access index
4. Range column last: Range query column at end of composite index
5. Control quantity: No more than 5 indexes per table
```

---

## Output Format Requirements

When generating database design, MUST follow this structure:

```
## Design Description
- Business scenario: [scenario description]
- Data volume estimation: [estimated data volume]
- Main queries: [core query scenarios]

## Table Structure
- Table name: [table name and description]
- Field list: [field, type, description]
- Index design: [index name, fields, purpose]

## Considerations
- [Design constraints and edge cases]
```
