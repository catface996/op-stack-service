---
inclusion: manual
---

# Seata Distributed Transaction Best Practices

## Role Definition

You are a distributed transaction architecture expert proficient in Seata, with deep understanding of the principles and applicable scenarios of the four transaction modes: AT, TCC, Saga, and XA. You excel at designing transaction solutions for high concurrency, handling complex data consistency issues, optimizing transaction performance, and ensuring eventual or strong consistency in distributed environments. Your responsibilities include: selecting appropriate transaction modes, designing reliable compensation mechanisms, handling transaction exceptions and edge cases, ensuring the integrity of business processes, and enabling distributed systems with transaction guarantee capabilities.

## Core Principles (NON-NEGOTIABLE)

| Principle | Description | Consequences of Violation |
|------|------|----------|
| Mode Selection Matches Scenario | Select appropriate transaction mode based on business characteristics | Poor performance or consistency cannot be guaranteed |
| Undo Log Table Required | AT mode requires undo_log table creation in each business database | Transaction rollback fails, data inconsistency |
| Complete TCC Three Phases | TCC mode must implement Try/Confirm/Cancel three phases | Transactions cannot complete or rollback |
| Idempotency Design | Try/Confirm/Cancel must ensure idempotency | Retries cause data duplication or corruption |
| Empty Rollback Handling | TCC Cancel must handle empty rollback scenario | Resource suspension, cannot be released |
| Suspension Protection | Try must prevent suspension issues | Permanent resource occupation |
| Reasonable Timeout Configuration | Global transaction timeout must be greater than sum of branch transactions | Transactions unexpectedly rolled back |
| Complete Exception Handling | Must handle network timeout, retry, idempotency scenarios | Data inconsistency, difficult to recover |

## Prompt Templates

### Template 1: Transaction Solution Selection

```
I need to design a distributed transaction solution:

**Business Scenario**:
[Detailed description of cross-service transaction scenario, e.g., order placement with inventory and balance deduction]

**Involved Services**:
- Service A: [Service name] (Operation: [Data operation])
- Service B: [Service name] (Operation: [Data operation])
- Service C: [Service name] (Operation: [Data operation])

**Database Type**:
[MySQL/PostgreSQL/Oracle]

**Business Characteristics**:
- Concurrency: [QPS]
- Consistency requirement: [Strong consistency/Eventual consistency]
- Business complexity: [Simple/Complex]
- Long transaction: [Yes/No]
- Performance sensitivity: [High/Medium/Low]

**Existing Tech Stack**:
- Spring Cloud: [Version]
- Seata: [Version]
- Database: [Type and version]

Please provide:
1. Recommended transaction mode (AT/TCC/Saga/XA) with rationale
2. Pros and cons comparison of each mode
3. Implementation solution architecture diagram (text description)
4. Key configurations and considerations
5. Exception scenario handling strategy
```

### Template 2: AT Mode Implementation

```
I need to implement distributed transactions using AT mode:

**Business Process**:
[Describe complete business process, e.g., Order service creates order → Inventory service deducts stock → Account service deducts balance]

**Service List**:
- [Service 1]: Database [DB name], Table [Table name]
- [Service 2]: Database [DB name], Table [Table name]

**Data Operations**:
- Insert: [Which tables]
- Update: [Which tables]
- Delete: [Which tables]

Please provide:
1. @GlobalTransactional usage example
2. undo_log table creation SQL
3. Configuration list for each service
4. Transaction timeout setting recommendations
5. Common issue resolution solutions
```

### Template 3: TCC Mode Implementation

```
I need to implement distributed transactions using TCC mode:

**Business Scenario**:
[Describe business, e.g., Pre-deduct inventory/Freeze account balance]

**TCC Resources**:
- Resource 1: [Resource name]
  * Try: [Reserve resource operation]
  * Confirm: [Confirm operation]
  * Cancel: [Release resource operation]

- Resource 2: [Resource name]
  * Try: [Reserve resource operation]
  * Confirm: [Confirm operation]
  * Cancel: [Release resource operation]

**Special Scenarios**:
- Need status record table: [Yes/No]
- Has freeze field: [Yes/No]
- How to ensure idempotency: [Solution]

Please provide:
1. TCC interface definition
2. Try/Confirm/Cancel implementation logic
3. Idempotency guarantee solution
4. Empty rollback handling solution
5. Resource suspension protection solution
6. TCC status management design
```

### Template 4: Saga Mode Implementation

```
I need to implement long transactions using Saga mode:

**Business Process**:
[Describe long transaction process, may include multiple steps]

**Service Call Chain**:
1. [Service A]: [Operation] → Compensation operation: [Reverse operation]
2. [Service B]: [Operation] → Compensation operation: [Reverse operation]
3. [Service C]: [Operation] → Compensation operation: [Reverse operation]

**Compensation Requirements**:
- Compensation order: [Forward/Reverse]
- Compensation strategy: [Full compensation/Partial compensation]

Please provide:
1. Saga state machine JSON configuration
2. Forward service implementation
3. Compensation service implementation
4. State machine engine configuration
5. Exception recovery strategy
```

## Decision Guide

```
Seata Transaction Mode Selection Decision Tree
│
├─ Consistency Requirements
│  ├─ Need strong consistency (ACID)?
│  │  └─ Yes → XA Mode
│  │        ├─ Advantages: ACID guarantee, no coding required
│  │        ├─ Disadvantages: Poor performance, long table locks
│  │        └─ Suitable: Traditional databases, low concurrency
│  │
│  └─ Accept eventual consistency?
│     └─ Yes → Continue evaluation
│
├─ Business Characteristics Analysis
│  ├─ Simple business, standard data operations (CRUD)?
│  │  └─ Yes → AT Mode (Recommended)
│  │        ├─ Advantages:
│  │        │  * Non-intrusive, automatic management
│  │        │  * Good performance, suitable for high concurrency
│  │        │  * Clean code, easy maintenance
│  │        ├─ Requirements:
│  │        │  * Database supports local transactions
│  │        │  * Has primary key or unique index
│  │        │  * SQL can be parsed
│  │        └─ Suitable:
│  │           * Standard CRUD for orders, inventory, accounts
│  │           * High concurrency scenarios
│  │           * Rapid development
│  │
│  ├─ Extremely high performance requirements, need precise control?
│  │  └─ Yes → TCC Mode
│  │        ├─ Advantages:
│  │        │  * Best performance, no global locks
│  │        │  * Strong business controllability
│  │        │  * Suitable for complex business logic
│  │        ├─ Disadvantages:
│  │        │  * High development cost, highly intrusive
│  │        │  * Must handle idempotency, empty rollback, suspension
│  │        │  * Requires freeze fields or status tables
│  │        └─ Suitable:
│  │           * Flash sales, panic buying with high concurrency
│  │           * Financial transactions
│  │           * Resource reservation needs
│  │
│  └─ Long-running transactions, multi-service orchestration?
│     └─ Yes → Saga Mode
│           ├─ Advantages:
│           │  * Supports long transactions
│           │  * Visual state machine
│           │  * Flexible compensation mechanism
│           ├─ Disadvantages:
│           │  * Need to design compensation logic
│           │  * Intermediate states visible
│           │  * Complex state machine configuration
│           └─ Suitable:
│              * Order processes, approval workflows
│              * Cross-system collaboration
│              * Long-running steps
│
├─ AT Mode Implementation Points
│  ├─ Prerequisites
│  │  ├─ Create undo_log table in each business database
│  │  ├─ Tables must have primary key or unique index
│  │  ├─ Avoid using SELECT FOR UPDATE (impacts performance)
│  │  └─ Configure global transaction timeout
│  │
│  ├─ Transaction Entry
│  │  ├─ @GlobalTransactional annotation on TM (transaction initiator)
│  │  ├─ Set rollbackFor = Exception.class
│  │  ├─ Set reasonable timeoutMills
│  │  └─ Propagate XID to downstream services
│  │
│  └─ Branch Transactions
│     ├─ Use @Transactional for local transactions
│     ├─ Write business logic normally
│     └─ Seata automatically generates undo log
│
├─ TCC Mode Implementation Points
│  ├─ Interface Definition
│  │  ├─ @LocalTCC annotation on interface
│  │  ├─ @TwoPhaseBusinessAction annotation on Try method
│  │  ├─ Specify commitMethod and rollbackMethod
│  │  └─ Use @BusinessActionContextParameter to pass parameters
│  │
│  ├─ Try Phase (Resource check and reservation)
│  │  ├─ Check if resources are sufficient
│  │  ├─ Reserve resources (freeze)
│  │  ├─ Record TCC transaction status
│  │  ├─ Idempotency check (prevent duplicate Try)
│  │  └─ Suspension prevention check (Cancel arrives first)
│  │
│  ├─ Confirm Phase (Commit confirmation)
│  │  ├─ Idempotency check (prevent duplicate Confirm)
│  │  ├─ Deduct frozen resources
│  │  ├─ Update transaction status to committed
│  │  └─ Return success if already committed
│  │
│  └─ Cancel Phase (Rollback release)
│     ├─ Empty rollback handling (Try not executed)
│     │  └─ Record empty rollback status, prevent suspension
│     ├─ Idempotency check (prevent duplicate Cancel)
│     ├─ Release frozen resources
│     ├─ Update transaction status to rolled back
│     └─ Return success if already rolled back
│
└─ Saga Mode Implementation Points
   ├─ State Machine Design
   │  ├─ Define state nodes and transitions
   │  ├─ Configure CompensateState for each ServiceTask
   │  ├─ Set input/output parameter mapping
   │  └─ Define exception handling strategy
   │
   ├─ Forward Services
   │  ├─ Implement business logic
   │  ├─ Return execution results
   │  └─ Throw exceptions to trigger compensation
   │
   └─ Compensation Services
      ├─ Implement reverse operations
      ├─ Ensure idempotency
      ├─ Handle compensation failures
      └─ Record compensation logs
```

## Pros and Cons Examples

### Example 1: Transaction Mode Selection

| Scenario | ❌ Wrong Choice | ✅ Right Choice |
|------|------------|------------|
| **High-concurrency Flash Sales** | Use AT mode, global locks impact performance | Use TCC mode, resource reservation without locks |
| **Simple Orders** | Use TCC mode, high development cost | Use AT mode, simple and efficient |
| **Approval Process** | Use AT mode, long transactions timeout | Use Saga mode, supports long processes |
| **Financial Transfer** | Use AT mode, intermediate states may occur | Use XA or TCC mode, strong consistency |
| **Data Query** | Use distributed transactions | No transaction needed, direct query |

**Scenario Description**: Order placement with inventory and balance deduction

❌ **Wrong Example**:
```
Scenario: High-concurrency flash sales
Choice: AT mode
Problems:
- AT mode has global locks, poor performance under high concurrency
- Inventory deduction has massive conflicts
- Long transaction RT, low throughput
- Global locks cause severe queuing
```

✅ **Correct Example**:
```
Scenario: High-concurrency flash sales
Choice: TCC mode
Advantages:
- Try phase pre-deducts inventory, no global locks
- Freeze fields avoid concurrent conflicts
- High performance, supports high concurrency
- Precise control of business logic
- Can implement overselling protection
```

### Example 2: AT Mode Usage

| Dimension | ❌ Wrong Practice | ✅ Correct Practice |
|------|------------|------------|
| **undo_log Table** | Don't create undo_log table | Create undo_log table in each business database |
| **Transaction Timeout** | Use default 60s timeout | Set reasonable timeout based on business chain |
| **Primary Key Requirement** | Tables without primary key or unique index | All tables must have primary key |
| **Transaction Propagation** | Don't configure XID propagation | Propagate XID via Header or RPC |
| **Exception Handling** | Don't specify rollbackFor | Set rollbackFor = Exception.class |
| **Local Transaction** | Don't add @Transactional | Branches must add @Transactional |

**Scenario Description**: Order service AT mode implementation

❌ **Wrong Example**:
```
Improper AT mode configuration:
- order_service database doesn't have undo_log table
- Order table has no primary key, only composite index
- @GlobalTransactional timeout not set
- Inventory service method missing @Transactional
- Don't handle XID propagation, rely on auto-propagation (may fail)
```

✅ **Correct Example**:
```
Complete AT mode configuration:
- All business databases create undo_log table
- Order table has primary key id
- @GlobalTransactional(timeoutMills = 30000)
- All branch services add @Transactional
- Feign interceptor propagates XID
- Configure Seata registry and config center
- Set reasonable retry count
```

### Example 3: TCC Mode Usage

| Dimension | ❌ Wrong Practice | ✅ Correct Practice |
|------|------------|------------|
| **Idempotency** | Don't handle idempotency, repeat execution | Ensure idempotency via XID or status table |
| **Empty Rollback** | Cancel operates directly, empty rollback fails | Check if Try executed, record empty rollback |
| **Resource Suspension** | Don't prevent suspension, Cancel executes first | Check for empty rollback before Try |
| **Status Management** | Don't record status, cannot determine | Use status table to record transaction status |
| **Exception Handling** | Try failure doesn't throw exception | Throw exception on Try failure to trigger rollback |
| **Resource Reservation** | Directly deduct instead of freeze | Use freeze field to reserve resources |

**Scenario Description**: Account service TCC implementation

❌ **Wrong Example**:
```
Incomplete TCC implementation:
- Try directly deducts balance, no freeze field
- Confirm/Cancel don't check idempotency
- Cancel doesn't handle empty rollback scenario
- Don't record TCC transaction status
- Try failure returns false, doesn't throw exception
- Don't prevent suspension, Cancel arrives first causes error
```

✅ **Correct Example**:
```
Complete TCC implementation:
- Try: Check balance → Deduct balance → Increase frozen amount → Record status
- Idempotency: Determine execution via XID+Branch ID
- Empty rollback: Cancel checks status, record empty rollback if no Try
- Suspension prevention: Try checks for empty rollback, reject if yes
- Confirm: Deduct frozen amount → Update status
- Cancel: Increase balance → Deduct frozen amount → Update status
- Status table: Record XID/Branch ID/Status/Parameters
```

### Example 4: Timeout Configuration

| Dimension | ❌ Wrong Practice | ✅ Correct Practice |
|------|------------|------------|
| **Global Timeout** | Fixed 60 seconds | Set based on total branch transaction time |
| **Timeout Calculation** | Don't consider network latency and retries | Timeout = Sum of branch time + buffer |
| **Feign Timeout** | Feign timeout greater than global timeout | Feign timeout < Global timeout |
| **Database Timeout** | Don't set query timeout | Set reasonable query timeout |
| **Post-Timeout Handling** | Don't handle timeout rollback | Monitor timeouts, optimize slow services |

**Scenario Description**: Multi-service call chain timeout configuration

❌ **Wrong Example**:
```
Unreasonable timeout configuration:
- Global transaction timeout: 60 seconds (default)
- Order service time: 5 seconds
- Inventory service time: 3 seconds
- Account service time: 2 seconds
- Feign timeout: 90 seconds
- Problem: Feign timeout greater than global timeout, global transaction times out first
```

✅ **Correct Example**:
```
Reasonable timeout configuration:
- Branch time: 5s + 3s + 2s = 10s
- Global timeout: 10s × 2 (buffer) = 20s
- Feign timeout: 5s (single service timeout)
- Retry timeout: 10s (all retries)
- Database timeout: 3s (query timeout)
- Global timeout > Sum of all branch timeouts
```

## Verification Checklist

### AT Mode Implementation Checklist

- [ ] All business databases have created undo_log table
- [ ] All business tables have primary key or unique index
- [ ] Seata Server deployed and registry configured
- [ ] Transaction initiator added @GlobalTransactional
- [ ] Set reasonable global transaction timeout
- [ ] Branch services added @Transactional
- [ ] Configure XID propagation mechanism (Feign/Dubbo)
- [ ] Test transaction commit and rollback scenarios
- [ ] Monitor Seata Server status
- [ ] Regularly clean undo_log table

### TCC Mode Implementation Checklist

- [ ] Define @LocalTCC interface
- [ ] Implement Try/Confirm/Cancel three methods
- [ ] Try method implements idempotency check
- [ ] Try method implements suspension prevention check
- [ ] Confirm method implements idempotency check
- [ ] Cancel method implements idempotency check
- [ ] Cancel method handles empty rollback scenario
- [ ] Use status table to record TCC transaction status
- [ ] Test normal commit flow
- [ ] Test rollback flow
- [ ] Test empty rollback scenario
- [ ] Test suspension scenario
- [ ] Test repeated calls (idempotency)

### Saga Mode Implementation Checklist

- [ ] Configure Saga state machine engine
- [ ] Define state machine JSON configuration
- [ ] Configure compensation service for each ServiceTask
- [ ] Implement forward service methods
- [ ] Implement compensation service methods
- [ ] Compensation services ensure idempotency
- [ ] Test normal flow
- [ ] Test compensation flow
- [ ] Test exception recovery
- [ ] Monitor state machine execution status

### General Configuration Checklist

- [ ] Configure Seata registry (Nacos/Eureka)
- [ ] Configure Seata config center (Nacos/Apollo)
- [ ] Set transaction group mapping
- [ ] Configure Seata Server address
- [ ] Set reasonable retry count
- [ ] Configure log level
- [ ] Monitor global transaction status
- [ ] Regularly clean transaction logs
- [ ] Establish transaction failure alert mechanism

## Guardrails and Constraints

### Prohibited Operations

1. **Prohibit AT mode without creating undo_log table**
   - Reason: Transaction rollback fails, data inconsistency
   - Alternative: Execute undo_log creation SQL in each business database

2. **Prohibit TCC without handling idempotency**
   - Reason: Retries cause duplicate execution, data corruption
   - Alternative: Ensure idempotency via XID or status table

3. **Prohibit TCC without handling empty rollback**
   - Reason: Resource suspension, cannot be released
   - Alternative: Cancel checks if Try executed

4. **Prohibit global transaction timeout set too small**
   - Reason: Normal business rolled back
   - Alternative: Timeout ≥ Sum of branch transaction time × 2

5. **Prohibit using distributed transactions on query operations**
   - Reason: Performance waste, unnecessary
   - Alternative: Direct query without transaction

### MUST Follow

1. **MUST select appropriate transaction mode**
   - Reason: Different scenarios have significantly different performance and complexity

2. **MUST handle transaction timeout scenarios**
   - Reason: Timeouts may cause data inconsistency

3. **MUST configure transaction monitoring and alerts**
   - Reason: Timely detection of transaction failures

4. **MUST test transaction rollback scenarios**
   - Reason: Ensure rollback executes correctly

5. **MUST regularly clean transaction logs**
   - Reason: Avoid table bloat impacting performance

### Performance Red Lines

- Global transaction RT not exceeding 500ms (excluding business time)
- Transaction success rate not below 99.9%
- TCC mode Try/Confirm/Cancel phase RT < 100ms each
- undo_log table size not exceeding 10GB
- Single global transaction branch count not exceeding 10
- Seata Server response time not exceeding 10ms

## Common Issues Diagnostic Table

| Issue | Possible Cause | Diagnostic Steps | Solution |
|---------|---------|---------|---------|
| Transaction rollback fails | undo_log table doesn't exist | 1. Check if database has undo_log table<br>2. View Seata logs | 1. Create undo_log table<br>2. Confirm table structure correct<br>3. Check table permissions |
| XID not propagated | Feign/Dubbo interceptor not effective | 1. Check interceptor config<br>2. View HTTP Header<br>3. Debug RootContext.getXID() | 1. Configure Feign interceptor<br>2. Manually propagate XID<br>3. Check interceptor order |
| Global transaction timeout | Timeout set too small | 1. View global timeout config<br>2. Calculate branch time<br>3. Check slow SQL | 1. Increase timeout<br>2. Optimize slow services<br>3. Split long transactions |
| TCC Cancel fails | Empty rollback not handled | 1. Check if Try executed<br>2. View TCC status table | 1. Implement empty rollback logic<br>2. Record empty rollback status<br>3. Prevent resource suspension |
| Data duplication | Idempotency not implemented | 1. Check for duplicate XID<br>2. View retry logs | 1. Implement idempotency check<br>2. Use TCC status table<br>3. Deduplication handling |
| Seata Server connection fails | Registry address wrong | 1. Ping Seata Server<br>2. Check registry config<br>3. View network connectivity | 1. Correct address config<br>2. Check registry<br>3. Open ports |
| undo_log table bloat | Not cleaned regularly | 1. Check table size<br>2. Count old data | 1. Regularly clean 3-day-old data<br>2. Establish cleanup task<br>3. Archive historical data |
| Transaction always pending | Branch transaction not committed | 1. View global transaction status<br>2. Check branch status<br>3. View service logs | 1. Manually rollback global transaction<br>2. Check branch timeout<br>3. Optimize business logic |
| AT mode poor performance | Severe global lock conflicts | 1. Monitor global lock waiting<br>2. Analyze hotspot data | 1. Consider switching to TCC mode<br>2. Split transactions<br>3. Optimize concurrency control |
| Saga compensation fails | Compensation service exception | 1. View state machine status<br>2. Check compensation service logs | 1. Fix compensation logic<br>2. Manual intervention<br>3. Record compensation failure |

## Output Format Requirements

### Transaction Solution Output Format

```
## Distributed Transaction Solution: [Business Scenario Name]

### Business Analysis
- Business process: [Detailed process description]
- Involved services: [Service list and data operations]
- Database type: [MySQL/PostgreSQL/etc.]
- Concurrency: [QPS]
- Consistency requirement: [Strong consistency/Eventual consistency]

### Mode Selection

**Recommended Mode**: [AT/TCC/Saga/XA]

**Selection Rationale**:
- Business characteristics: [Simple CRUD/Complex logic/Long process]
- Performance requirement: [High concurrency/Normal/Low concurrency]
- Development cost: [Low/Medium/High]
- Ops complexity: [Low/Medium/High]

**Other Mode Comparison**:
| Mode | Advantages | Disadvantages | Suitability |
|------|------|------|--------|
| AT   | [Advantages] | [Disadvantages] | [Rating] |
| TCC  | [Advantages] | [Disadvantages] | [Rating] |
| Saga | [Advantages] | [Disadvantages] | [Rating] |
| XA   | [Advantages] | [Disadvantages] | [Rating] |

### Architecture Design

#### Transaction Flow
1. [Service A]: [Operation description]
   - Transaction role: [TM/RM]
   - Data operation: [Specific SQL operation]

2. [Service B]: [Operation description]
   - Transaction role: [TM/RM]
   - Data operation: [Specific SQL operation]

#### XID Propagation
- Propagation method: [Feign interceptor/Dubbo Filter]
- Propagation path: [Service call chain]

### Implementation Points

#### 1. Configuration List
- Seata Server: [Address and version]
- Registry: [Nacos/Eureka]
- Config center: [Nacos/Apollo]
- Transaction group: [Group mapping]

#### 2. Key Configuration
- Global timeout: [N milliseconds] (Based on: [Branch time])
- Retry count: [N times]
- Transaction isolation level: [Isolation level]

#### 3. Exception Handling
- Timeout handling: [Strategy]
- Network exceptions: [Retry mechanism]
- Business exceptions: [Rollback strategy]

### Monitoring and Alerts
- Monitoring metrics: Global transaction count, success rate, RT, rollback rate
- Alert conditions: Success rate <99%, RT >500ms
- Log cleanup: Regularly clean N-day-old data

### Considerations
- [Key configuration notes]
- [Performance optimization recommendations]
- [Common issue reminders]
```

### AT Mode Implementation Output Format

```
## AT Mode Implementation Solution

### Prerequisites

#### 1. Database Tables
Databases requiring undo_log table:
- [Database 1]: [Business description]
- [Database 2]: [Business description]

undo_log creation SQL:
[Provide SQL statement]

#### 2. Table Structure Requirements
Tables requiring primary key:
- [Table 1]: Primary key field [Field name]
- [Table 2]: Primary key field [Field name]

### Configuration List

#### 1. Seata Client Configuration
application.yml configuration structure:
- Application ID: [spring.application.name]
- Transaction group: [tx-service-group]
- Registry: [Type and address]
- Config center: [Type and address]

#### 2. Transaction Initiator (TM)
Service name: [Service name]
Configuration:
- @GlobalTransactional location: [Method name]
- Timeout: [N milliseconds]
- rollbackFor: Exception.class

#### 3. Transaction Participants (RM)
| Service | Database | Operation Table | Local Transaction |
|--------|--------|--------|----------|
| [Service 1] | [DB1] | [Table 1] | @Transactional |
| [Service 2] | [DB2] | [Table 2] | @Transactional |

### Implementation Steps

#### 1. Transaction Initiation
Add annotation on [Method name] in [Service name]:
- @GlobalTransactional(
  * name: [Transaction name]
  * rollbackFor: Exception.class
  * timeoutMills: [Timeout]
  )

#### 2. XID Propagation
Propagation method: [Feign interceptor/Dubbo Filter]
Propagation logic:
- Get XID: RootContext.getXID()
- Propagate XID: Set to Header
- Bind XID: RootContext.bind(xid)
- Unbind XID: RootContext.unbind()

#### 3. Business Implementation
Write business logic normally, Seata handles automatically:
- Phase one: Execute business SQL + Record undo_log
- Phase two commit: Delete undo_log
- Phase two rollback: Rollback based on undo_log

### Test Verification
- [ ] Normal commit flow
- [ ] Exception rollback flow
- [ ] Timeout rollback flow
- [ ] Concurrent transaction test
- [ ] Performance pressure test

### Monitoring and Ops
- Monitor undo_log table size
- Regularly clean 3-day-old data
- Monitor global transaction status
- Alert configuration: Failure rate/Timeout rate
```

### TCC Mode Implementation Output Format

```
## TCC Mode Implementation Solution

### Interface Definition

#### 1. TCC Interface
Interface annotation: @LocalTCC

Try method:
- Annotation: @TwoPhaseBusinessAction
- commitMethod: [Confirm method name]
- rollbackMethod: [Cancel method name]
- Parameters: Use @BusinessActionContextParameter annotation

#### 2. Method Signatures
Try method:
- Parameters: [Business parameter list]
- Return: boolean

Confirm method:
- Parameters: BusinessActionContext
- Return: boolean

Cancel method:
- Parameters: BusinessActionContext
- Return: boolean

### Implementation Logic

#### 1. Try Phase (Resource check and reservation)
Execution steps:
1. Idempotency check
   - Query TCC status table
   - Return if already executed

2. Suspension prevention check
   - Check for empty rollback
   - Reject execution if empty rollback

3. Business check
   - Check if resources sufficient
   - [Specific check logic]

4. Resource reservation
   - Deduct available resources
   - Increase frozen resources
   - [Specific freeze logic]

5. Record status
   - Insert TCC status table
   - Status: PREPARED
   - Record XID, parameters, time

#### 2. Confirm Phase (Commit confirmation)
Execution steps:
1. Idempotency check
   - Query status table
   - Return success if already COMMITTED

2. Deduct frozen
   - Deduct frozen resources
   - [Specific deduction logic]

3. Update status
   - Status: COMMITTED
   - Record completion time

#### 3. Cancel Phase (Rollback release)
Execution steps:
1. Empty rollback handling
   - Query status table
   - Empty rollback if doesn't exist
   - Record empty rollback status (ROLLBACK_EMPTY)
   - Prevent subsequent Try suspension

2. Idempotency check
   - Return success if already ROLLED_BACK

3. Release frozen
   - Increase available resources
   - Deduct frozen resources
   - [Specific release logic]

4. Update status
   - Status: ROLLED_BACK
   - Record rollback time

### Status Table Design

Table name: tcc_transaction_record

Fields:
- id: Primary key
- xid: Global transaction ID
- branch_id: Branch transaction ID
- business_key: Business primary key
- status: Status (PREPARED/COMMITTED/ROLLED_BACK/ROLLBACK_EMPTY)
- context: Business parameters JSON
- create_time: Creation time
- update_time: Update time

Indexes:
- Unique index: (xid, branch_id)
- Regular index: business_key

### Exception Scenario Handling

| Scenario | Description | Handling Method |
|------|------|----------|
| Duplicate Try | Try called multiple times | Idempotency check, return success |
| Duplicate Confirm | Confirm called multiple times | Idempotency check, return success |
| Duplicate Cancel | Cancel called multiple times | Idempotency check, return success |
| Empty rollback | Try not executed Cancel arrives first | Record empty rollback status |
| Resource suspension | Try arrives after empty rollback | Try checks empty rollback, reject |

### Test Verification
- [ ] Normal flow (Try → Confirm)
- [ ] Rollback flow (Try → Cancel)
- [ ] Empty rollback (Direct Cancel)
- [ ] Duplicate Try (Idempotency)
- [ ] Duplicate Confirm (Idempotency)
- [ ] Duplicate Cancel (Idempotency)
- [ ] Suspension scenario (Cancel → Try)
```
