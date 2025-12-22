---
description: Synchronize project documentation with actual implementation after development completion, generating a retrospective report.
---

## User Input

```text
$ARGUMENTS
```

You **MUST** consider the user input before proceeding (if not empty).

## Goal

After a feature implementation is complete and deployed, synchronize all project documentation to reflect the actual state of the codebase. This command performs a retrospective analysis comparing planned vs actual implementation, updates relevant documentation, and generates a sync report capturing lessons learned.

## Operating Constraints

**NON-DESTRUCTIVE BY DEFAULT**: Show proposed changes before applying; require user confirmation for modifications. Use `--preview` to only show changes without applying, or `--apply` to apply all changes (with confirmation).

**COMPREHENSIVE DETECTION**: Automatically scan all relevant documentation files including spec.md, plan.md, constitution.md, README.md, and other project documentation.

**FLEXIBLE TIMING**: This command can run at any time. Missing files will be noted but won't block execution. No prerequisite commands are required.

## Execution Steps

### 1. Initialize Sync Context

Run `scripts/bash/check-prerequisites.sh --json --paths-only` from repo root and parse JSON output. The script uses `--paths-only` mode which skips validation and returns all paths directly.
For single quotes in args like "I'm Groot", use escape syntax: e.g 'I'\''m Groot' (or double-quote if possible: "I'm Groot").

**JSON Output Fields:**

- `REPO_ROOT` - Repository root directory
- `BRANCH` - Current branch name
- `FEATURE_DIR` - Feature specification directory
- `FEATURE_SPEC` - Path to spec.md
- `IMPL_PLAN` - Path to plan.md
- `TASKS` - Path to tasks.md

**Additional Paths to Derive:**

- CONSTITUTION = REPO_ROOT/.specify/memory/constitution.md
- README = REPO_ROOT/README.md

**File Existence Check:**

After obtaining paths, check which files actually exist. For each missing file:

- Log it in the sync report under "Missing Artifacts"
- Skip related analysis steps (e.g., skip plan comparison if plan.md missing)
- Continue with available files

This command does NOT require any prerequisite files - it gracefully handles missing artifacts.

### 2. Scan Implementation State

Analyze the actual codebase to understand what was implemented:

**Code Analysis:**

- Scan source directories for implemented features, components, and modules
- Identify public APIs, endpoints, and interfaces
- Detect technology stack actually used (frameworks, libraries, versions)
- Map file structure and architecture patterns

**Git Analysis (if available):**

- Review recent commits related to the feature
- Identify files changed since feature branch creation
- Note any hotfixes or post-implementation changes

### 3. Compare Plan vs Reality

Create a deviation analysis by comparing:

| Aspect | Planned (from docs) | Actual (from code) | Status |
|--------|--------------------|--------------------|--------|
| Architecture | From plan.md | Detected patterns | MATCH / DEVIATION |
| Tech Stack | From plan.md | Package files, imports | MATCH / DEVIATION |
| API Endpoints | From spec.md/contracts | Actual routes/handlers | MATCH / DEVIATION |
| Data Models | From spec.md/data-model.md | Actual schemas/entities | MATCH / DEVIATION |
| Features | From spec.md user stories | Implemented functionality | MATCH / DEVIATION / PARTIAL |

**Deviation Categories:**

- **MATCH**: Implementation matches documentation
- **DEVIATION**: Implementation differs from documentation (note reason if detectable)
- **PARTIAL**: Partially implemented or modified scope
- **ADDED**: Implemented but not in original spec (scope creep or enhancement)
- **REMOVED**: In spec but not implemented (descoped)

### 4. Detect Documentation Requiring Updates

Scan each documentation file and identify sections needing updates:

#### A. spec.md Updates

- Features that were descoped or modified
- New features added during implementation
- Updated acceptance criteria based on actual behavior
- Edge cases discovered during development

#### B. plan.md Updates

- Architecture changes from original design
- Technology substitutions or version changes
- Modified file structure
- Updated dependency list

#### C. constitution.md Updates

- New architectural principles established
- Modified coding standards based on learnings
- Updated quality gates or review processes

#### D. README.md Updates

- Installation/setup changes
- Updated usage examples
- New configuration options
- Changed prerequisites

#### E. Other Files

- ARCHITECTURE.md or docs/architecture.md
- API documentation
- Configuration documentation
- Deployment guides

### 5. Generate Sync Report

Create a comprehensive sync report at `FEATURE_DIR/sync-report.md` with the following structure:

```markdown
# Sync Report: [FEATURE_NAME]

**Generated**: [TIMESTAMP]
**Feature Directory**: [FEATURE_DIR]
**Implementation Status**: [COMPLETED/PARTIAL]

## Executive Summary

[Brief 2-3 sentence summary of sync findings]

## Deviation Analysis

### Matched Items
| Item | Document | Status |
|------|----------|--------|

### Deviations
| Item | Planned | Actual | Impact | Recommendation |
|------|---------|--------|--------|----------------|

### Scope Changes
- **Added**: [Features added beyond original spec]
- **Removed**: [Features descoped]
- **Modified**: [Features with changed behavior]

## Documentation Updates Required

| File | Section | Change Type | Priority |
|------|---------|-------------|----------|

## Lessons Learned

### What Went Well
- [Positive observations]

### Challenges Encountered
- [Deviations that required workarounds]

### Recommendations for Future
- [Suggestions based on deviations and challenges]

## Technical Debt Identified

| Item | Description | Priority | Suggested Resolution |
|------|-------------|----------|---------------------|

## Next Actions

- [ ] [Action items based on findings]
```

### 6. Display Terminal Summary

Output a concise summary to the terminal:

```text
Sync Analysis Complete
======================

Feature: [FEATURE_NAME]
Status: [X] deviations found, [Y] docs need updates

Deviations:
  - HIGH: [count]
  - MEDIUM: [count]
  - LOW: [count]

Documentation Updates:
  - plan.md: [sections needing update]
  - spec.md: [sections needing update]
  - README.md: [sections needing update]

Full report: [FEATURE_DIR]/sync-report.md
```

### 7. Propose Documentation Updates

For each file requiring updates, show the proposed changes:

```text
Proposed Updates for plan.md:
-----------------------------
Section: ## Architecture
Change: Update to reflect microservices pattern instead of monolith

[Show diff or before/after preview]

Apply this change? (yes/no/skip)
```

**Update Modes:**

- **Interactive** (default): Ask for confirmation before each change
- **Preview Only** (`--preview`): Show all proposed changes without applying
- **Auto Apply** (`--apply`): Apply all changes automatically (requires explicit user confirmation first)
- **Report Only** (`--report-only`): Only generate sync report, don't propose updates

### 8. Apply Updates (with confirmation)

After user approval, apply the confirmed changes:

1. Create backup of original files (optional, based on user preference)
2. Apply approved modifications
3. Verify changes were applied correctly
4. Update sync-report.md with applied changes log

### 9. Final Summary

Output completion status:

```text
Sync Complete
=============

Documents Updated:
  [x] plan.md - Architecture section
  [x] spec.md - User stories
  [ ] README.md - Skipped by user

Sync Report: [FEATURE_DIR]/sync-report.md

Suggested commit message:
  docs: sync documentation with [feature-name] implementation

  - Updated architecture in plan.md
  - Added new user stories to spec.md
  - Generated sync report with lessons learned
```

## Operating Principles

### Documentation Accuracy

- **Reflect reality**: Ensure all docs match the current implementation state
- **Preserve history**: Don't delete original intent; document what changed and why
- **Actionable updates**: Each proposed change should be specific and immediately applicable

### Knowledge Preservation

- **Capture decisions**: Record architectural decisions made during implementation
- **Document deviations**: Explain why implementation differs from plan
- **Lessons learned**: Extract reusable insights for future projects

### Analysis Guidelines

- **NEVER auto-apply changes** without user confirmation (unless `--apply` with explicit consent)
- **NEVER hallucinate implementation details** (only report what's actually in the code)
- **Prioritize high-impact deviations** (architecture > API > minor details)
- **Report zero issues gracefully** (emit success report with alignment statistics)

## Context

{ARGS}
