# Implementation Plan: Agent Binding Relationship Refactor

**Branch**: `040-agent-bound-refactor` | **Date**: 2025-12-29 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/040-agent-bound-refactor/spec.md`

## Summary

Refactor the agent binding system by introducing a unified `agent_bound` table to manage all agent-to-entity relationships. Currently, agent bindings are scattered across different mechanisms (topology.global_supervisor_agent_id column for Global Supervisor, node_2_agent table for Team Supervisors and Workers). This refactoring consolidates all bindings into a single, extensible data model. Old binding APIs will be removed and replaced with the new unified API.

## Technical Context

**Language/Version**: Java 21 (LTS)
**Primary Dependencies**: Spring Boot 3.4.x, MyBatis-Plus 3.5.x, SpringDoc OpenAPI
**Storage**: MySQL 8.0 (via Flyway migrations)
**Testing**: JUnit 5, Spring Boot Test
**Target Platform**: Linux server (via Docker)
**Project Type**: DDD multi-module architecture
**Performance Goals**: Hierarchical team query < 100ms for topologies with up to 100 nodes
**Constraints**: Remove old binding APIs, frontend will be refactored
**Scale/Scope**: Support existing topology/node/agent volumes, migration of existing bindings

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. DDD Architecture | PASS | New AgentBound entity follows existing domain model patterns |
| II. API URL Convention | PASS | APIs will follow `/api/service/v1/agent-bounds/{action}` |
| III. POST-Only API Design | PASS | All new APIs will use POST method |
| IV. Database Migration | PASS | Will use Flyway migration for new table |
| V. Technology Stack | PASS | Using Java 21, Spring Boot 3.4.x, MyBatis-Plus 3.5.x |
| VI. Pagination Protocol | PASS | Query APIs will follow PageResult standard |
| VII. Database Design Standards | PASS | New table follows naming conventions and templates |

**Gate Result**: PASS - No violations detected.

## Project Structure

### Documentation (this feature)

```text
specs/040-agent-bound-refactor/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── agent-bound-api.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
# DDD Multi-module Architecture (existing)
bootstrap/
├── src/main/resources/db/migration/
│   ├── V29__create_agent_bound_table.sql  # New migration
│   └── V30__migrate_agent_bindings.sql    # Data migration

domain/
├── domain-api/src/main/java/.../domain/service/
│   └── agentbound/AgentBoundDomainService.java  # New domain service interface
├── domain-impl/src/main/java/.../domain/impl/service/
│   └── agentbound/AgentBoundDomainServiceImpl.java  # New domain service impl
└── domain-model/src/main/java/.../domain/model/
    └── agentbound/
        ├── AgentBound.java              # New domain entity
        └── BoundEntityType.java         # New enum

infrastructure/repository/
├── mysql-api/src/main/java/.../repository/
│   └── agentbound/AgentBoundRepository.java  # New repository interface
├── mysql-impl/src/main/java/.../repository/mysql/
│   ├── impl/agentbound/AgentBoundRepositoryImpl.java
│   ├── mapper/agentbound/AgentBoundMapper.java
│   └── po/agentbound/AgentBoundPO.java

application/
├── application-api/src/main/java/.../application/api/
│   ├── dto/agentbound/
│   │   ├── AgentBoundDTO.java
│   │   ├── HierarchyTeamDTO.java
│   │   ├── HierarchyStructureDTO.java
│   │   └── request/
│   │       ├── BindAgentRequest.java
│   │       ├── UnbindAgentRequest.java
│   │       ├── QueryByEntityRequest.java
│   │       ├── QueryByAgentRequest.java
│   │       └── QueryHierarchyRequest.java
│   └── service/agentbound/AgentBoundApplicationService.java
└── application-impl/src/main/java/.../application/impl/
    └── service/agentbound/AgentBoundApplicationServiceImpl.java

interface/interface-http/src/main/java/.../interface_/http/
└── controller/AgentBoundController.java  # New controller

# Files to REMOVE (legacy APIs)
interface/interface-http/.../controller/TopologyController.java  # Remove bind/unbind-supervisor endpoints
interface/interface-http/.../controller/NodeController.java      # Remove bindAgent/unbindAgent endpoints
interface/interface-http/.../request/topology/BindSupervisorAgentRequest.java     # Remove
interface/interface-http/.../request/topology/UnbindSupervisorAgentRequest.java   # Remove
application/.../dto/node/request/BindAgentRequest.java           # Remove
application/.../dto/node/request/UnbindAgentRequest.java         # Remove
```

**Structure Decision**: Following existing DDD multi-module architecture. New AgentBound feature will be added as a new aggregate. Old binding APIs from TopologyController and NodeController will be removed.

## Complexity Tracking

> No Constitution violations detected. This table remains empty.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| - | - | - |
