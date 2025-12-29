# Feature Specification: Agent Binding Relationship Refactor

**Feature Branch**: `040-agent-bound-refactor`
**Created**: 2025-12-29
**Status**: Clarified
**Input**: 对Topology绑定Global Supervisor Agent，资源节点绑定Team Supervisor Agent，绑定Team Worker做重构，设计一张表agent_bound，有agentId，hierarchyLevel，entityId，entityType等字段，来统一管理agent与各种实体的关联关系

## Overview

This feature refactors the agent binding system by introducing a unified `agent_bound` table to manage all agent-to-entity relationships. Currently, agent bindings are scattered across different mechanisms (topology direct binding for Global Supervisor, node_2_agent table for Team Supervisors and Workers). This refactoring consolidates all bindings into a single, extensible data model.

The new unified approach enables:
- Consistent binding management across all entity types
- Simplified queries for hierarchical team structures
- Extensibility for future entity types
- Clearer data model for agent-entity relationships

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Bind Global Supervisor to Topology (Priority: P0)

As a system administrator, I want to bind a Global Supervisor Agent to a Topology using the unified binding system, so that the topology has a designated coordinator for multi-agent execution.

**Why this priority**: Global Supervisor binding is fundamental to the hierarchical team structure and execution flow.

**Independent Test**: Can be tested by creating a binding between a Global Supervisor Agent and a Topology, then verifying the binding exists and can be queried.

**Acceptance Scenarios**:

1. **Given** a Topology and a Global Supervisor Agent, **When** I create a binding, **Then** the system stores the relationship with entityType=TOPOLOGY and hierarchyLevel=GLOBAL_SUPERVISOR
2. **Given** a Topology already has a Global Supervisor binding, **When** I try to add another, **Then** the system replaces the existing binding with the new one
3. **Given** a Topology with Global Supervisor binding, **When** I query the topology's agent bindings, **Then** I receive the bound Global Supervisor Agent details

---

### User Story 2 - Bind Team Supervisor to Resource Node (Priority: P0)

As a system administrator, I want to bind a Team Supervisor Agent to a Resource Node, so that the node has a designated coordinator for its team operations.

**Why this priority**: Team Supervisor binding is essential for hierarchical team coordination.

**Independent Test**: Can be tested by creating a binding between a Team Supervisor Agent and a Node, then verifying the binding.

**Acceptance Scenarios**:

1. **Given** a Node and a Team Supervisor Agent, **When** I create a binding, **Then** the system stores the relationship with entityType=NODE and hierarchyLevel=TEAM_SUPERVISOR
2. **Given** a Node already has a Team Supervisor binding, **When** I try to add another, **Then** the system replaces the existing binding with the new one
3. **Given** a Node with Team Supervisor binding, **When** I query the node's agent bindings, **Then** I receive the bound Team Supervisor Agent details

---

### User Story 3 - Bind Workers to Resource Node (Priority: P0)

As a system administrator, I want to bind multiple Worker Agents to a Resource Node, so that the team has workers to execute tasks.

**Why this priority**: Worker binding enables the actual task execution within teams.

**Independent Test**: Can be tested by creating multiple Worker bindings for a Node, then verifying all bindings exist.

**Acceptance Scenarios**:

1. **Given** a Node and multiple Worker Agents, **When** I create bindings for each, **Then** the system stores all relationships with entityType=NODE and hierarchyLevel=WORKER
2. **Given** a Node with Worker bindings, **When** I query the node's workers, **Then** I receive all bound Worker Agent details
3. **Given** a Node with existing Workers, **When** I add another Worker, **Then** the new Worker is added without affecting existing bindings

---

### User Story 4 - Query Hierarchical Team Structure (Priority: P0)

As a backend service, I want to query the complete hierarchical team structure for a Topology, so that I can construct the executor hierarchy for multi-agent execution.

**Why this priority**: This query is critical for the execution trigger feature to work correctly.

**Independent Test**: Can be tested by creating a complete hierarchy (Topology → Global Supervisor, Nodes → Team Supervisors + Workers) and verifying the query returns the complete structure.

**Acceptance Scenarios**:

1. **Given** a Topology with Global Supervisor and Nodes with Team Supervisors and Workers, **When** I query the hierarchical team structure, **Then** I receive the complete hierarchy grouped by team
2. **Given** a Topology with partial configuration, **When** I query the structure, **Then** I receive available bindings with appropriate indicators for missing components
3. **Given** a Topology with no bindings, **When** I query the structure, **Then** I receive an empty structure or appropriate error

---

### User Story 5 - Migrate Existing Bindings (Priority: P1)

As a system administrator, when the new binding system is deployed, I want existing bindings to be migrated automatically, so that current configurations continue to work without manual intervention.

**Why this priority**: Data migration ensures backward compatibility and smooth transition.

**Independent Test**: Can be tested by setting up existing bindings in the old structure, running migration, and verifying all bindings exist in the new table.

**Acceptance Scenarios**:

1. **Given** existing topology-agent bindings, **When** migration runs, **Then** all bindings are copied to agent_bound table with correct entityType and hierarchyLevel
2. **Given** existing node_2_agent bindings, **When** migration runs, **Then** all bindings are copied to agent_bound table preserving hierarchy level
3. **Given** migration completes, **When** querying through new APIs, **Then** all historical data is accessible

---

### Edge Cases

- What happens when binding an Agent that doesn't exist?
- What happens when binding to an Entity that doesn't exist?
- How to handle orphaned bindings when Agent or Entity is deleted?
- What happens when querying bindings for a deleted entity?
- How to handle concurrent binding operations on the same entity?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a unified agent_bound table with fields: id, agentId, hierarchyLevel, entityId, entityType
- **FR-002**: System MUST support entityType values: TOPOLOGY, NODE (extensible for future types)
- **FR-003**: System MUST support hierarchyLevel values: GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, TEAM_WORKER
- **FR-004**: System MUST enforce one Global Supervisor per Topology constraint
- **FR-005**: System MUST enforce one Team Supervisor per Node constraint
- **FR-006**: System MUST allow multiple Workers per Node
- **FR-007**: System MUST provide API to create agent-entity binding
- **FR-008**: System MUST provide API to remove agent-entity binding
- **FR-009**: System MUST provide API to query bindings by entity (entityId + entityType)
- **FR-010**: System MUST provide API to query bindings by agent (agentId)
- **FR-011**: System MUST provide API to query hierarchical team structure for a Topology
- **FR-012**: System MUST migrate existing bindings from current tables to agent_bound table
- **FR-013**: System MUST maintain soft delete support (deleted field) for audit trail
- **FR-014**: System MUST cascade soft delete bindings when Agent or Entity is deleted
- **FR-015**: System MUST validate Agent hierarchy level matches the binding hierarchy level

### Non-Functional Requirements

- **NFR-001**: Query for hierarchical team structure MUST complete within acceptable time for topologies with up to 100 nodes
- **NFR-002**: Migration MUST be idempotent and safe to run multiple times

### Key Entities

- **AgentBound**: Unified binding record containing agentId, hierarchyLevel (GLOBAL_SUPERVISOR/TEAM_SUPERVISOR/TEAM_WORKER), entityId, entityType (TOPOLOGY/NODE)
- **EntityType**: Enum representing bindable entity types (TOPOLOGY, NODE)
- **HierarchyLevel**: Enum representing agent hierarchy levels (GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, TEAM_WORKER)

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: All existing agent bindings are successfully migrated to the new unified table
- **SC-002**: Hierarchical team query returns complete structure in a single operation
- **SC-003**: Binding constraints are enforced (one Global Supervisor per Topology, one Team Supervisor per Node)
- **SC-004**: Feature 039 (Trigger Multi-Agent Execution) continues to work correctly using the new binding system
- **SC-005**: Old binding tables can be deprecated after migration verification

## Clarifications

The following ambiguities were identified and resolved during specification review:

### CL-001: Duplicate Binding Behavior
**Question**: When attempting to bind a duplicate Global Supervisor or Team Supervisor, should the system reject or replace?
**Decision**: **Replace existing binding** - The system will automatically unbind the old Agent and bind the new one. This provides flexibility for management scenarios.

### CL-002: Hierarchy Level Naming
**Question**: The spec used "WORKER" but codebase uses "TEAM_WORKER" - which naming convention?
**Decision**: **Use TEAM_WORKER** - Maintain consistency with existing `AgentHierarchyLevel` enum in codebase to avoid modifying existing code.

### CL-003: Batch Unbind Operations
**Question**: Should the API support batch unbinding of multiple agents?
**Decision**: **Single unbind API only** - Batch operations can be achieved through multiple calls. This simplifies implementation and API design.

### CL-004: Legacy API Handling
**Question**: Should old binding APIs (TopologyController.bind-supervisor, NodeController.bindAgent) be preserved for backward compatibility?
**Decision**: **Remove old APIs** - Frontend will be refactored to use new unified `/api/service/v1/agent-bounds/*` APIs. No backward compatibility period needed.

## Assumptions

- Existing node_2_agent table contains both Team Supervisor and Worker bindings, distinguished by the agent's hierarchyLevel field
- Topology currently has a direct reference to Global Supervisor Agent (topology.global_supervisor_agent_id column added in V27 migration)
- The agent table has a hierarchyLevel field with values: GLOBAL_SUPERVISOR, TEAM_SUPERVISOR, TEAM_WORKER
- Soft delete pattern is already in use across the codebase (deleted field)
- Database supports foreign key constraints for referential integrity
