# Research: Agent Binding Relationship Refactor

**Feature**: 040-agent-bound-refactor
**Date**: 2025-12-29

## Executive Summary

This research consolidates findings on existing binding mechanisms and recommends the unified `agent_bound` table design that follows established codebase patterns.

## Research Topics

### RT-001: Current Binding Mechanisms Analysis

**Question**: How are agent bindings currently implemented?

**Findings**:

1. **Global Supervisor Binding** (topology → agent):
   - Implemented via `topology.global_supervisor_agent_id` column (V27 migration)
   - Direct foreign key reference on topology table
   - One-to-one relationship enforced by single column
   - **Location**: `V27__add_global_supervisor_agent_to_topology.sql`

2. **Node Agent Binding** (node → agents):
   - Implemented via `node_2_agent` junction table (V21 migration)
   - Supports many-to-many relationship
   - Soft delete with `deleted` flag
   - Unique constraint: `uk_node_agent (node_id, agent_id, deleted)`
   - **Location**: `V21__Create_node_agent_relation_table.sql`

**Decision**: Unify both mechanisms into single `agent_bound` table with `entityType` discriminator

**Rationale**:
- Consistent query patterns for all binding types
- Extensible for future entity types
- Simplified codebase maintenance
- Clear data model for audit/reporting

### RT-002: Hierarchy Level Storage

**Question**: Should hierarchy level be stored in agent_bound or derived from agent table?

**Findings**:
- Agent table has `hierarchy_level` column (V28 migration) with values: GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, TEAM_WORKER
- Current `node_2_agent` table does not store hierarchy level (derives from agent)
- Storing in binding table enables:
  - Faster queries without joining agent table
  - Historical audit (if agent hierarchy changes)
  - Explicit constraint validation at binding creation

**Decision**: Store `hierarchy_level` in `agent_bound` table, validated against agent's hierarchy_level at creation

**Rationale**:
- Query optimization for hierarchical team structure
- Explicit binding semantics
- Validation at write time prevents invalid bindings

### RT-003: Entity Type Enum Design

**Question**: How should entity types be represented?

**Findings**:
- Codebase uses VARCHAR for enum storage (e.g., `role VARCHAR(32)` in agent table)
- Status enums stored as strings: RUNNING, STOPPED, etc.
- Constitution mandates: "枚举字段 MUST 列出所有可选值" in COMMENT

**Decision**: Use VARCHAR(20) for `entity_type` with values: TOPOLOGY, NODE

**Rationale**:
- Consistent with existing enum patterns
- Extensible without migration for new values
- Human-readable in database queries

### RT-004: Unique Constraint Strategy

**Question**: How to handle replace-on-duplicate for Global/Team Supervisor?

**Findings**:
- `node_2_agent` uses `UNIQUE INDEX uk_node_agent (node_id, agent_id, deleted)`
- Soft delete included in unique constraint to allow re-binding after delete
- For supervisor roles, need to enforce single-binding-per-entity constraint

**Decision**: Use application-level enforcement with transactional delete-then-insert

**Rationale**:
- Database unique constraint can't express "one supervisor per entity"
- Application service handles:
  1. Query existing supervisor binding
  2. Soft delete if exists
  3. Create new binding
  4. All in single transaction

### RT-005: Migration Strategy

**Question**: How to migrate existing data without downtime?

**Findings**:
- Existing APIs use `TopologyDomainService.bindSupervisorAgent()` and `NodeDomainService.bindAgents()`
- Feature 039 (Trigger Multi-Agent Execution) depends on current binding structure
- Need dual-write period for backward compatibility

**Decision**: Three-phase migration approach

**Phase 1 - Preparation**:
- Create `agent_bound` table
- Implement new AgentBound domain/repository/service
- New APIs use new table

**Phase 2 - Migration**:
- Flyway migration script to copy existing bindings:
  - `topology.global_supervisor_agent_id` → agent_bound (entityType=TOPOLOGY)
  - `node_2_agent` entries → agent_bound (entityType=NODE)
- Verify counts match

**Phase 3 - Cleanup** (future):
- Remove `global_supervisor_agent_id` column from topology
- Deprecate `node_2_agent` table
- Update existing APIs to use new table

**Rationale**:
- Zero-downtime migration
- Rollback capability at each phase
- Feature 039 continues working throughout

### RT-006: Query Optimization for Hierarchical Team Structure

**Question**: How to efficiently query complete team hierarchy?

**Findings**:
- Current implementation requires:
  1. Query topology for global_supervisor_agent_id
  2. Query topology_2_node for nodes
  3. Query node_2_agent for each node's agents
  4. Join with agent table for hierarchy_level
- Multiple round trips and joins

**Decision**: Single optimized query with proper indexing

```sql
-- Get all bindings for a topology's team structure
SELECT ab.*, a.name, a.hierarchy_level
FROM agent_bound ab
JOIN agent a ON ab.agent_id = a.id
WHERE (ab.entity_type = 'TOPOLOGY' AND ab.entity_id = :topologyId)
   OR (ab.entity_type = 'NODE' AND ab.entity_id IN (
       SELECT node_id FROM topology_2_node WHERE topology_id = :topologyId AND deleted = 0
   ))
AND ab.deleted = 0
ORDER BY ab.entity_type, ab.hierarchy_level;
```

**Indexes required**:
- `idx_entity (entity_type, entity_id, deleted)` - primary lookup
- `idx_agent_id (agent_id)` - reverse lookup

**Rationale**:
- Single query returns complete structure
- Application service groups by team
- Meets NFR-001 performance requirement

## Alternatives Considered

### ALT-001: Keep Separate Binding Mechanisms
**Rejected**: Inconsistent patterns, harder to query hierarchical structure, more code to maintain

### ALT-002: Generic Entity-Entity Relationship Table
**Rejected**: Over-engineering for current requirements, adds unnecessary complexity

### ALT-003: JSON Column for Bindings in Entity Tables
**Rejected**: Violates normalization, harder to query and index, no referential integrity

## Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 3.4.x | Web framework, transaction management |
| MyBatis-Plus | 3.5.x | ORM, soft delete support |
| Flyway | (managed by Spring Boot) | Database migrations |

## Technical Decisions Summary

| ID | Decision | Impact |
|----|----------|--------|
| TD-001 | Unified agent_bound table | Simplifies data model |
| TD-002 | Store hierarchy_level in binding | Optimizes queries |
| TD-003 | VARCHAR entity_type enum | Extensible, readable |
| TD-004 | Application-level supervisor constraint | Flexible replacement logic |
| TD-005 | Three-phase migration | Zero-downtime transition |
| TD-006 | Composite index for query optimization | Meets performance requirement |
