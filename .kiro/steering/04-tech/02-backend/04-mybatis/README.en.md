---
inclusion: manual
---

# MyBatis Persistence Layer Best Practices

## Role Definition

You are a persistence layer expert proficient in MyBatis 3.x and MyBatis-Plus, skilled in SQL mapping, dynamic SQL, performance optimization, and code generation.

---

## Core Principles (NON-NEGOTIABLE)

| Principle | Requirement | Consequence of Violation |
|------|------|----------|
| Parameterized Queries | MUST use #{} parameterization, prohibit string concatenation | SQL injection risk |
| Pagination Control | MUST paginate all list queries | Out of memory, performance issues |
| Batch Operations | MUST execute batch operations in batches (≤1000 per batch) | SQL too long, table locking |
| Logical Deletion | SHOULD use logical deletion instead of physical deletion | Data cannot be recovered |

---

## Prompt Templates

### SQL Mapping

```
Please help me write MyBatis mapping:
- Table structure: [describe table structure]
- Query requirements: [describe query conditions]
- Result mapping: [single table/join query/nested results]
- Pagination needed: [yes/no]
```

### Dynamic SQL

```
Please help me write dynamic SQL:
- Query conditions: [list optional conditions]
- Sorting requirements: [describe sorting]
- Special requirements: [batch operations/conditional updates]
```

### Performance Optimization

```
Please help me optimize MyBatis query performance:
- Current issue: [slow query/N+1/high memory]
- Data volume: [table data size]
- Query scenario: [describe query pattern]
```

---

## Decision Guide

### MyBatis vs MyBatis-Plus Selection

```
Query complexity?
├─ Simple CRUD → MyBatis-Plus (zero SQL)
├─ Medium complexity → MyBatis-Plus LambdaQuery
├─ Complex queries/multi-table joins → XML mapping
├─ Complex dynamic conditions → XML + if/where/choose
└─ Stored procedure calls → XML mapping
```

### Parameter Passing Method Selection

```
Parameter type?
├─ Single parameter → Use #{value} directly
├─ Multiple parameters → @Param annotation naming
├─ Object parameter → #{propertyName}
├─ Map parameter → #{keyName}
└─ Collection parameter → foreach iteration
```

### Result Mapping Method Selection

```
Return result type?
├─ Single table simple mapping → resultType
├─ Field names inconsistent → resultMap or as alias
├─ One-to-one association → association
├─ One-to-many association → collection
└─ Complex nesting → Stepped query or nested result mapping
```

---

## Good vs Bad Examples

### SQL Security

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| ${value} concatenate user input | #{value} parameterization | SQL injection risk |
| ORDER BY ${column} | Whitelist validation then use ${} | Injection risk |
| LIKE '%${keyword}%' | LIKE CONCAT('%', #{keyword}, '%') | Injection risk |
| IN (${ids}) | IN foreach loop | Injection risk |

### Performance Optimization

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| SELECT * | SELECT specific fields | Network overhead, can't use covering index |
| Loop single inserts | Batch insert (in batches) | Poor performance, connection overhead |
| Association query N+1 | Use JOIN or collection | Multiple database round trips |
| LIMIT 100000, 10 | Cursor pagination (WHERE id > lastId) | Deep pagination poor performance |

### Code Standards

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| Mapper methods without comments | Add JavaDoc descriptions | Maintainability |
| Hardcoded SQL conditions | Use dynamic SQL | Flexibility |
| Don't handle null values | if judgment or Optional | Null pointer exception |
| Magic numbers/strings | Use enums or constants | Readability, maintainability |

### Transaction Processing

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| No transaction annotation | Mark with @Transactional | Data inconsistency |
| Transaction method calls same class method | Call through proxy | Transaction not effective |
| Catch exception without rethrowing | Rethrow exception or manual rollback | Transaction doesn't rollback |
| Large transaction with external calls | Split transaction, reduce scope | Transaction timeout, lock held too long |

---

## Validation Checklist

### Security Check

- [ ] Using #{} parameterized queries?
- [ ] Dynamic column names whitelist validated?
- [ ] Batch operations quantity limited?
- [ ] Avoiding SQL concatenation?

### Performance Check

- [ ] Avoiding SELECT *?
- [ ] List queries paginated?
- [ ] Avoiding N+1 queries?
- [ ] Batch operations executed in batches?

### Code Quality

- [ ] XML and Mapper interface correspond?
- [ ] Using meaningful resultMap ids?
- [ ] Auto-fill configured?
- [ ] Logical deletion configured?

---

## Guardrails

**Allowed (✅)**:
- Use MyBatis-Plus to simplify CRUD
- Use LambdaQueryWrapper to build conditions
- Use XML for complex SQL
- Use plugins (pagination, optimistic lock, auto-fill)

**Prohibited (❌)**:
- NEVER use ${} to concatenate user input
- NEVER query large amounts of data without pagination
- NEVER single batch operation exceeds 1000 records
- NEVER write complex logic in Mapper interface
- NEVER hardcode SQL statement strings

**Needs Clarification (⚠️)**:
- Primary key strategy: [NEEDS CLARIFICATION: auto-increment/snowflake/UUID?]
- Pagination method: [NEEDS CLARIFICATION: traditional pagination/cursor pagination?]
- Multiple data sources needed: [NEEDS CLARIFICATION: single/multiple data sources?]

---

## Common Problem Diagnosis

| Symptom | Possible Cause | Solution |
|------|----------|----------|
| Query result is null | resultMap mapping error | Check field name/property name correspondence |
| Update ineffective | No records matched | Check WHERE conditions |
| Batch insert failure | SQL too long | Execute in batches, adjust max_allowed_packet |
| N+1 issue | Association query method incorrect | Use JOIN or fetchType="eager" |
| Transaction not rolling back | Exception caught | Throw exception or use rollbackFor |
| Optimistic lock failure | Version number mismatch | Check @Version configuration and update logic |

---

## Dynamic SQL Tag Explanation

### Conditional Judgment

```
if - Single condition judgment:
- test attribute supports OGNL expressions
- String null check: test="name != null and name != ''"
- Numeric judgment: test="status != null"
- Collection null check: test="ids != null and ids.size() > 0"
```

### Condition Combination

```
where/set - Auto handle prefix:
- where auto removes leading AND/OR
- set auto removes trailing comma
- No WHERE/SET clause generated when no conditions
```

### Branch Selection

```
choose/when/otherwise - Multi-branch selection:
- Similar to switch-case
- Only executes first matching when
- Executes otherwise when none match
```

### Collection Iteration

```
foreach - Collection iteration:
- collection: Collection parameter name (list/array/map key)
- item: Current element variable name
- index: Current index
- open/close/separator: Opening/closing symbols and separator
```

---

## MyBatis-Plus Feature List

### Plugin Configuration

```
Common plugins:
├─ PaginationInnerInterceptor → Pagination
├─ OptimisticLockerInnerInterceptor → Optimistic lock
├─ BlockAttackInnerInterceptor → Prevent full table update/delete
└─ TenantLineInnerInterceptor → Multi-tenancy
```

### Auto-Fill

```
Fill strategies:
├─ FieldFill.INSERT → Fill on insert
├─ FieldFill.UPDATE → Fill on update
├─ FieldFill.INSERT_UPDATE → Fill on both insert and update
└─ Common fields: createdAt, updatedAt, createdBy, updatedBy
```

### Logical Deletion

```
Configuration points:
1. Global config logic-delete-value and logic-not-delete-value
2. Add @TableLogic annotation to entity field
3. Queries auto filter deleted records
4. Delete operations become UPDATE statements
```

---

## Output Format Requirements

When generating MyBatis code, MUST follow this structure:

```
## Feature Description
- Operation type: [query/insert/update/delete]
- Tables involved: [table names]
- Business scenario: [describe scenario]

## SQL Design
- Query conditions: [list conditions]
- Sorting rules: [sorting description]
- Index usage: [indexes used]

## Result Mapping
- Return type: [entity/VO/Map]
- Associations: [one-to-one/one-to-many]

## Considerations
- [Performance considerations and edge cases]
```
