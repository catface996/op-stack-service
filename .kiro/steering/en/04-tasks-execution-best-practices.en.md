---
inclusion: always
---

# Task Execution Phase Best Practices

## Phase Goal

Execute tasks one by one according to task list, transform design into runnable code, ensure project is in healthy state after each task completion.

## Why Task Execution Phase is So Important

Task execution is a critical phase for turning design into reality. Good execution practices can:
- Ensure project continuous healthy state
- Discover and solve problems early
- Improve code quality and maintainability
- Support team collaborative development
- Reduce integration and deployment risks

**Core Principle**: After each task completion, project must successfully build.

## Task Execution Workflow

### Step 1: Understand Task

Before starting to execute task, fully understand task requirements.

**Key Activities**:
- Carefully read task description and acceptance criteria
- Understand task goal and expected output
- Confirm preconditions are met
- Identify possible technical difficulties

### Step 2: Implement Functionality

Implement functionality according to task description.

**Implementation Principles**:

#### 1. Keep Project Continuously Buildable

**Key Requirement**: After each task completion, entire project must be successfully buildable.

**Why Important**:
- Project always in runnable state
- Discover integration problems timely
- Support continuous integration and continuous delivery
- Reduce later integration risk

**How to Achieve**:
- Run build command immediately after completing task
- Ensure build success before entering next task
- Fix immediately when encountering build failure

#### 2. Progressive Development

**Key Idea**: In multi-module or multi-component projects, adopt progressive development strategy, avoid declaring modules or components not yet created.

**Basic Principles**:
- Only declare already created components
- Update configuration synchronously when creating components
- Same principle applies to multi-level structures

**Why Important**:
- Avoid build errors
- Keep configuration synchronized with actual structure
- Support incremental development

#### 3. Follow Code Quality Standards

**Coding Standards**:
- Follow project unified code style
- Use meaningful variable and method naming
- Add necessary comments and documentation

**Error Handling**:
- Implement unified exception handling mechanism
- Provide clear error messages
- Avoid exception messages leaking sensitive data

**Testing Requirements**:
- Write unit tests for core business logic (if task requires)
- Ensure test coverage reaches reasonable level
- Integration tests verify module collaboration

### Step 3: Verify Task

After task implementation complete, verify according to acceptance criteria.

**Verification Priority**:

#### 1. Runtime Verification (Highest Priority)

Features that can be verified by actually running application must be verified by running application.

**Applicable Scenarios**:
- Configuration verification (multi-environment, feature toggles, etc.)
- API endpoint functionality verification
- Log output format verification
- Exception handling verification
- Integration functionality verification
- External service connection verification

**Verification Method**:
- Start application (using project's startup command)
- Access relevant endpoints or trigger relevant functionality
- Check actual runtime results meet expectations

#### 2. Build Verification (Second Priority)

For structural requirements that cannot be verified through runtime, verify through project build.

**Applicable Scenarios**:
- Module/component structure verification
- Dependency relationship verification
- Build configuration verification
- Code syntax correctness verification

**Verification Method**:
- Execute project's build command
- Check build logs confirm no errors
- Confirm all components built in correct order

#### 3. Static Check (Last Resort)

Only use static file check when cannot verify through above two methods.

**Applicable Scenarios**:
- File existence check
- Configuration file content check
- Directory structure check

### Step 4: Task Completion Confirmation

Confirm task is completely done, meets all acceptance criteria.

**Check Points**:
- All acceptance criteria passed
- Project can build successfully
- Code quality meets standards
- Related documentation updated (if needed)

### Step 5: Requirements and Design Consistency Check

**Key**: After task verification passes, must conduct requirements and design consistency check to ensure implementation conforms to original intent.

**Why Important**:
- Task may pass acceptance criteria but deviate from requirement intent
- Implementation may violate design's architecture principles
- Discover deviations early, avoid accumulating technical debt
- Ensure overall solution consistency and completeness

**Check Dimensions**:

#### 1. Requirements Consistency Check

Verify whether task implementation truly meets corresponding requirements.

**Check Points**:
- [ ] What are task-related requirements? (Check `_Requirements:_` marking in task)
- [ ] Does implementation completely cover all acceptance criteria of requirements?
- [ ] Does implementation accurately understand requirement intent?
- [ ] Is there implementation beyond requirement scope (over-design)?
- [ ] Are there missing requirement points?

**Verification Method**:
- Open requirements document, find corresponding requirement item
- Check each requirement acceptance criterion
- Confirm implementation meets every acceptance criterion
- If deviation exists, record reason and assess impact

#### 2. Design Consistency Check

Verify whether task implementation follows architecture and technical solution in design document.

**Check Points**:
- [ ] Does implementation follow architecture pattern in design document?
- [ ] Do module division and responsibility boundaries conform to design?
- [ ] Are interface definitions consistent with design?
- [ ] Does data model conform to design specification?
- [ ] Is technology selection consistent with design?
- [ ] Does it follow non-functional requirements in design (performance, security, etc.)?

**Verification Method**:
- Open design document, find related design chapter
- Compare with architecture diagrams, module division, interface definitions in design
- Confirm implementation conforms to design specifications
- If deviation exists, assess whether there's reasonable reason

#### 3. Deviation Handling

If implementation deviates from requirements or design:

**Minor Deviation** (doesn't affect core functionality and architecture):
- Record deviation reason and impact
- Assess whether adjustment needed
- If adjustment needed, create follow-up task

**Major Deviation** (affects core functionality or architecture):
- Immediately stop subsequent tasks
- Communicate deviation situation with user
- Determine whether to correct implementation or update requirements/design
- Re-verify after correction

**Reasonable Deviation** (improvement with sufficient reason):
- Record deviation reason and benefits
- Update design document (if needed)
- Confirm changes with user
- Assess impact on subsequent tasks

#### 4. Self-check Prompt

Use following prompt for self-check:

> "Please check whether just completed task implementation meets corresponding requirement (requirement number: X.X), whether it follows architecture and technical solution in design document. If deviation exists, please explain reason."

**Check Process**:
1. Find task-related requirement number
2. Open requirements document, locate corresponding requirement
3. Check each requirement acceptance criterion is met
4. Open design document, find related design chapter
5. Check whether implementation conforms to design specification
6. Record check results and found problems

## Progressive Development Implementation Steps

### 1. Confirm Current State
Check configuration file only contains already created components

### 2. Create New Component
Create component directory and necessary configuration files

### 3. Update Configuration
Add new component declaration in related configuration files

### 4. Verify Build
Run project's build command to ensure build success

## Common Problems and Mitigations

### Problem 1: Build Failure

**Manifestation**: Errors appear after executing build command, project cannot build successfully.

**Common Causes**:
- Component declaration inconsistent with actual structure
- Dependency relationship configuration error
- Language/framework version incompatible
- Code syntax error

**Mitigation**:
- Check whether component declaration correct
- Confirm dependency versions compatible
- Check code syntax errors
- Review build logs to locate specific problem

### Problem 2: Runtime Error

**Manifestation**: Build succeeds but runtime errors occur.

**Common Causes**:
- Configuration file format error
- External service connection configuration error
- Port occupied
- Resource files missing

**Mitigation**:
- Check configuration file format and content
- Verify external service connection configuration
- Confirm port not occupied
- Check resource files exist

### Problem 3: Integration Issues

**Manifestation**: Individual components normal, but component collaboration has problems.

**Common Causes**:
- Component interface inconsistent
- Data format incompatible
- Boundary and responsibility division unclear

**Mitigation**:
- Verify component interface consistency
- Check data format compatibility
- Confirm boundary and responsibility division
- Write integration tests to verify

### Problem 4: Verification Criteria Not Clear

**Manifestation**: Uncertain how to verify task completion.

**Mitigation**:
- Review task's acceptance criteria
- Choose verification method according to verification priority (runtime > build > static)
- If uncertain, confirm verification method with user

## Task Completion Checklist

Before marking task as complete, confirm all items below:

### Basic Check
- [ ] Have fully understood task requirements and acceptance criteria
- [ ] Code implemented to correct location
- [ ] Related configuration files updated
- [ ] Project can build successfully
- [ ] No build errors or warnings
- [ ] Component declarations consistent with actual directory structure
- [ ] Dependency relationships configured correctly
- [ ] Code meets quality standards (coding standards, error handling)

### Verification Check
- [ ] **If functionality can runtime verify, verified by running application**
- [ ] **If structural change, verified through build**
- [ ] All acceptance criteria passed
- [ ] Related documentation updated (if needed)

### Consistency Check (Important)
- [ ] **Found task-related requirement number**
- [ ] **Checked against requirements document, confirmed implementation meets all requirement acceptance criteria**
- [ ] **Checked against design document, confirmed implementation follows architecture and technical solution**
- [ ] **If deviation exists, recorded reason and assessed impact**
- [ ] **Major deviations communicated and confirmed with user**

## Key Benefits

Following task execution phase best practices can:

- ✅ Ensure project continuous healthy state
- ✅ Discover and solve problems early
- ✅ Improve code quality and maintainability
- ✅ Support team collaborative development
- ✅ Reduce integration and deployment risks
- ✅ Facilitate continuous integration and automated testing
- ✅ **Ensure implementation consistent with requirements and design**
- ✅ **Avoid accumulating technical debt and architecture deviation**

**Remember**:
1. After each task completion, project must successfully build
2. After each task verification passes, must check consistency with requirements and design

These are the two iron rules of task execution phase.
