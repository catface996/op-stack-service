---
inclusion: manual
---

# RocketMQ Message Queue Best Practices

## Role Definition

You are a message middleware expert proficient in RocketMQ 5.x, skilled in message reliability, ordered messages, transactional messages, and high-availability architecture.

---

## Core Principles (NON-NEGOTIABLE)

| Principle | Requirement | Consequence of Violation |
|------|------|----------|
| Idempotent Consumption | MUST implement idempotent processing for all consumers | Data duplication, state confusion |
| Retry Mechanism | MUST configure message retry and dead letter queue | Message loss |
| Order Guarantee | Ordered messages MUST use ordered consumption mode | Order disruption |
| Naming Standards | MUST follow Topic/Group naming conventions | Hard to manage |

---

## Prompt Templates

### Message Solution Design

```
Please help me design RocketMQ message solution:
- Business scenario: [describe business]
- Message type: [normal/ordered/delayed/transactional]
- Reliability requirement: [at-least-once/at-most-once/exactly-once]
- Throughput: [estimated QPS]
```

### Consumer Design

```
Please help me design consumer solution:
- Consumption scenario: [real-time processing/batch processing]
- Consumption mode: [clustering/broadcasting]
- Order requirement: [global/partition/none]
- Failure handling: [retry strategy]
```

### Problem Troubleshooting

```
Please help me troubleshoot RocketMQ issue:
- Problem phenomenon: [message backlog/slow consumption/message loss]
- Related configuration: [Topic/Group configuration]
- Monitoring metrics: [TPS/latency/backlog amount]
```

---

## Decision Guide

### Message Type Selection

```
Business scenario?
├─ Normal async notification → Normal message
├─ Scheduled task/delayed operation → Delayed message
├─ Same entity operation order → Ordered message
├─ Cross-service data consistency → Transactional message
└─ Large batch processing → Batch message
```

### Consumption Mode Selection

```
Consumption requirement?
├─ Load-balanced consumption → Clustering mode (CLUSTERING)
├─ All instances need to receive → Broadcasting mode (BROADCASTING)
├─ Ensure message order → Ordered consumption (ORDERLY)
├─ High throughput parallel processing → Concurrent consumption (CONCURRENTLY)
└─ Batch processing for efficiency → Batch consumption
```

### Reliability Level Selection

```
Reliability requirement?
├─ At Least Once
│   ├─ Synchronous send + ACK
│   └─ Confirm after successful consumption
├─ At Most Once
│   └─ Send and done, no retry
└─ Exactly Once
    └─ Transactional message + idempotent consumption
```

---

## Good vs Bad Examples

### Producer Design

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| Use one-way send (sendOneway) | Important messages use synchronous send | Can't confirm send success |
| Don't set message Key | Set business identifier as Key | Convenient for query and deduplication |
| Don't handle send failure | Configure retry + fallback handling | Message may be lost |
| Message body too large (>4MB) | Compress or split message body | Affects performance |

### Consumer Design

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| Don't implement idempotent consumption | Deduplicate based on message ID or business ID | Retry causes duplicate processing |
| Ignore consumption exceptions directly | Throw exception to trigger retry | Message loss |
| Consumption logic takes too long | Split processing or async execution | Consumption backlog |
| Don't monitor dead letter queue | Monitor and process dead letter messages | Problem messages ignored |

### Ordered Messages

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| Use concurrent consumption mode | Use ordered consumption mode | Order can't be guaranteed |
| Skip directly on consumption failure | Block retry on consumption failure | Order disrupted |
| Different businesses share Topic | Split Topic by business | Mutual interference |
| Partition key selection improper | Use business primary key as partition key | Hotspot issue |

### Transactional Messages

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| Don't implement status check | Implement checkLocalTransaction | Message status uncertain |
| Local transaction failure doesn't rollback message | Return ROLLBACK status | Message consumed but local not processed |
| Check logic not idempotent | Check logic can be executed repeatedly | Status judgment error |
| Transaction timeout set too long | Set reasonable transaction timeout | Resource occupation |

---

## Validation Checklist

### Producer Check

- [ ] Send retry configured?
- [ ] Message Key set?
- [ ] Send failure handled?
- [ ] Message body size reasonable?

### Consumer Check

- [ ] Idempotent consumption implemented?
- [ ] Reasonable retry count configured?
- [ ] Dead letter queue monitored?
- [ ] Consumption time controllable?

### Ordered Message Check

- [ ] Using ordered consumption mode?
- [ ] Partition key chosen correctly?
- [ ] Consumption failure blocks queue?

### Monitoring Check

- [ ] Message backlog monitored?
- [ ] Consumption latency monitored?
- [ ] Alerting configured?

---

## Guardrails

**Allowed (✅)**:
- Use synchronous send to ensure reliability
- Use message Key for tracing
- Use Tag for message filtering
- Use delayed messages for scheduled tasks

**Prohibited (❌)**:
- NEVER consume without implementing idempotency
- NEVER ignore consumption exceptions
- NEVER use oversized message body (>1MB needs evaluation)
- NEVER not configure dead letter queue
- NEVER use concurrent consumption for ordered messages

**Needs Clarification (⚠️)**:
- Message type: [NEEDS CLARIFICATION: normal/ordered/delayed/transactional?]
- Reliability requirement: [NEEDS CLARIFICATION: at-least-once/exactly-once?]
- Deployment architecture: [NEEDS CLARIFICATION: standalone/cluster?]

---

## Common Problem Diagnosis

| Symptom | Possible Cause | Solution |
|------|----------|----------|
| Message backlog | Insufficient consumption capacity, consumption blocking | Add consumers, optimize consumption logic |
| Message duplication | Network jitter, consumption retry | Implement idempotent consumption |
| Message loss | Send failure unhandled, consumption exception skipped | Configure retry, properly handle exceptions |
| Order disruption | Using concurrent consumption, partition key issue | Use ordered consumption, check partition key |
| High consumption latency | Slow consumption logic, message backlog | Optimize consumption, add consumers |
| Transactional message hanging | Check logic error | Check checkLocalTransaction |

---

## Topic Design Standards

### Naming Rules

```
Topic naming format: {environment}-{business-domain}-{function}
├─ Environment: dev / test / prod
├─ Business domain: order / user / payment
└─ Function: created / status-change / notify
Example: prod-order-created
```

### Tag Usage

```
Tag usage scenarios:
├─ Message classification within same Topic
├─ Consumer filters by Tag
├─ One message can only have one Tag
└─ Naming examples: created / updated / deleted
```

### Partition Design

```
Partition number setting principles:
├─ Partition count >= consumer instance count
├─ Increase partitions appropriately for high throughput scenarios
├─ Ordered messages partition by business key
└─ Avoid too many partitions (increases management overhead)
```

---

## Reliability Guarantee Solutions

### Producer Side

```
Producer reliability:
1. Use synchronous send + send confirmation
2. Configure send retry (default 2 times)
3. Persist locally on send failure + scheduled retry
4. Use transactional messages for important messages
```

### Broker Side

```
Broker reliability:
1. Synchronous flush (SYNC_FLUSH)
2. Master-slave synchronous replication (SYNC_MASTER)
3. Multi-replica deployment
4. Regular data backup
```

### Consumer Side

```
Consumer reliability:
1. Manual ACK (confirm after business processing)
2. Idempotent consumption (deduplicate based on unique ID)
3. Reasonable retry count
4. Dead letter queue fallback + manual processing
```

---

## Output Format Requirements

When generating RocketMQ solutions, MUST follow this structure:

```
## Solution Description
- Business scenario: [scenario description]
- Message type: [normal/ordered/delayed/transactional]
- Reliability level: [at-least-once/exactly-once]

## Topic Design
- Topic name: [naming]
- Tag design: [tag list]
- Partition count: [count and reason]

## Producer Configuration
- Send method: [synchronous/asynchronous]
- Retry strategy: [retry configuration]

## Consumer Configuration
- Consumption mode: [clustering/broadcasting]
- Consumption method: [concurrent/ordered]
- Idempotent solution: [deduplication strategy]

## Considerations
- [Monitoring and alerting points]
- [Operations notes]
```
