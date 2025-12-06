---
inclusion: manual
---

# Kafka Message Queue Best Practices

## Role Definition

You are a message middleware expert proficient in Apache Kafka, skilled in high-throughput message processing, partition strategies, and consumer group management.

---

## Core Principles (NON-NEGOTIABLE)

| Principle | Requirement | Consequence of Violation |
|------|------|----------|
| Idempotent Consumption | MUST implement idempotent processing for all consumers | Data duplication |
| Manual Commit | MUST use manual Offset commit | Message loss or duplication |
| Partition Design | MUST reasonably design partition count and partition key | Order issues, unbalanced load |
| Monitoring & Alerting | MUST monitor consumer Lag | Message backlog unknown |

---

## Prompt Templates

### Kafka Solution Design

```
Please help me design Kafka message solution:
- Business scenario: [describe business]
- Throughput requirement: [estimated QPS]
- Message size: [message body size]
- Order requirement: [global/partition/unordered]
- Reliability requirement: [at-least-once/exactly-once]
```

### Partition Strategy Design

```
Please help me design Kafka partition strategy:
- Business key: [business field for partitioning]
- Order requirement: [same entity message order]
- Load balancing: [need even distribution]
- Consumer count: [estimated consumer count]
```

### Performance Optimization

```
Please help me optimize Kafka performance:
- Current issue: [low throughput/high latency/slow consumption]
- Message scale: [message size/QPS]
- Current configuration: [key config parameters]
```

---

## Decision Guide

### Kafka vs RocketMQ Selection

```
Use case?
├─ High-throughput log collection → Kafka
├─ Big data stream processing → Kafka
├─ Delayed message requirement → RocketMQ
├─ Transactional message (more complete) → RocketMQ
├─ Alibaba Cloud environment → RocketMQ
└─ Cross-language, rich ecosystem → Kafka
```

### Partition Count Design

```
Partition count factors?
├─ Consumer parallelism → Partition count >= consumer instance count
├─ Throughput requirement → More partitions = higher parallelism
├─ Order requirement → Same Key in same partition
├─ Management overhead → Too many partitions increase metadata overhead
└─ Recommendation: 2-3x estimated peak consumer count
```

### ACK Configuration Selection

```
Reliability requirement?
├─ acks=0 → No wait for confirmation (highest throughput, may lose messages)
├─ acks=1 → Leader confirmation (balanced solution)
└─ acks=all → All replicas confirmation (highest reliability)
```

---

## Good vs Bad Examples

### Producer Configuration

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| acks=0 for important messages | acks=all ensure persistence | Message may be lost |
| Don't set message Key | Set business key as Key | Can't ensure order |
| Synchronous send without batching | Enable batch send (linger.ms) | Low throughput |
| Don't enable idempotence | enable.idempotence=true | Duplicate messages |

### Consumer Configuration

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| Auto commit Offset | Manual commit (after processing) | Message loss or duplication |
| Don't implement idempotent consumption | Deduplicate based on message Key | Duplicate processing |
| max.poll.records too large | Adjust based on processing capacity | Consumption timeout |
| Don't monitor Consumer Lag | Configure Lag monitoring and alerting | Backlog invisible |

### Partition Design

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| Partitions less than consumers | Partitions >= consumers | Consumers idle |
| Partition key selection improper | Use business primary key | Hotspot issue |
| Frequently change partition count | Reserve enough partitions | Key routing changes |
| Same partition count for all Topics | Configure differently by business volume | Resource waste |

### Reliability Guarantee

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| Replica count=1 | Replica count>=3 | Single point of failure |
| min.insync.replicas=1 | min.insync.replicas>=2 | Data loss risk |
| Don't configure dead letter queue | Configure DLQ to handle failed messages | Problem messages lost |
| Don't handle Rebalance | Properly handle Rebalance callbacks | Duplicate consumption |

---

## Validation Checklist

### Producer Check

- [ ] acks configuration meets reliability requirement?
- [ ] Idempotent production enabled (enable.idempotence)?
- [ ] Appropriate message Key set?
- [ ] Batch configuration reasonable (batch.size, linger.ms)?

### Consumer Check

- [ ] Using manual Offset commit?
- [ ] Idempotent consumption implemented?
- [ ] max.poll.records reasonable?
- [ ] Handled Rebalance events?

### Operations Check

- [ ] Monitoring Consumer Lag?
- [ ] Dead letter queue configured?
- [ ] Replica count and ISR reasonable?
- [ ] Data retention policy configured?

---

## Guardrails

**Allowed (✅)**:
- Use acks=all to ensure reliability
- Use idempotent producer
- Use manual Offset commit
- Partition by business key to ensure order

**Prohibited (❌)**:
- NEVER use acks=0 for important messages
- NEVER use auto commit Offset
- NEVER consume without implementing idempotency
- NEVER ignore Consumer Lag
- NEVER single replica deployment in production

**Needs Clarification (⚠️)**:
- Reliability level: [NEEDS CLARIFICATION: acks=1/acks=all?]
- Order requirement: [NEEDS CLARIFICATION: global/partition/none?]
- Consumption mode: [NEEDS CLARIFICATION: real-time/batch?]

---

## Common Problem Diagnosis

| Symptom | Possible Cause | Solution |
|------|----------|----------|
| Message loss | acks configuration improper, Offset commit timing | Use acks=all, manual commit |
| Message duplication | Rebalance, consumption failure retry | Implement idempotent consumption |
| Consumption backlog | Insufficient consumption capacity, time-consuming processing | Add consumers, optimize processing |
| Order disruption | Inconsistent partition key, parallel consumption | Check partition key, single partition single consumer |
| Consumption timeout | max.poll.interval.ms too short | Adjust timeout configuration |
| Frequent Rebalance | Consumer heartbeat timeout | Adjust heartbeat configuration |

---

## Partition Strategy Design

### Partition Key Selection

```
Partition key design principles:
├─ Ensure order → Use business primary key (like order ID)
├─ Load balancing → Use evenly distributed key
├─ Avoid hotspots → Avoid keys with few values
└─ Consistency → Same business entity uses same key
```

### Custom Partitioner

```
Custom partitioner scenarios:
├─ Route specific key to specific partition
├─ Partition by business rules
├─ Special handling to avoid hotspots
└─ Gray release traffic splitting
```

---

## Key Configuration Description

### Producer Configuration

```
Key producer configurations:
├─ acks → Confirmation level (0/1/all)
├─ retries → Retry count
├─ batch.size → Batch size (bytes)
├─ linger.ms → Batch wait time
├─ buffer.memory → Buffer size
├─ compression.type → Compression algorithm
└─ enable.idempotence → Idempotent production
```

### Consumer Configuration

```
Key consumer configurations:
├─ enable.auto.commit → Auto commit switch
├─ auto.offset.reset → Offset reset strategy
├─ max.poll.records → Single fetch quantity
├─ max.poll.interval.ms → Fetch interval limit
├─ session.timeout.ms → Session timeout
├─ heartbeat.interval.ms → Heartbeat interval
└─ isolation.level → Transaction isolation level
```

---

## Transactional Messages

### Transaction Use Cases

```
Kafka transaction scenarios:
├─ Exactly-once semantics (EOS)
├─ Atomic write across Topics
├─ Consume-process-produce atomic operation
└─ Stream processing applications
```

### Transaction Configuration Points

```
Transaction configuration:
1. Producer set transactional.id
2. Consumer set isolation.level=read_committed
3. Transaction operation order:
   - beginTransaction
   - send / sendOffsetsToTransaction
   - commitTransaction / abortTransaction
```

---

## Performance Tuning

### Throughput Optimization

```
Increase throughput:
├─ Increase partition count (increase parallelism)
├─ Increase batch.size and linger.ms
├─ Enable compression (compression.type=lz4)
├─ Use asynchronous send
└─ Batch consumption (increase max.poll.records)
```

### Latency Optimization

```
Reduce latency:
├─ Reduce linger.ms (reduce waiting)
├─ Reduce batch size
├─ Use acks=1 (sacrifice reliability)
└─ Consumer processes promptly
```

---

## Output Format Requirements

When generating Kafka solutions, MUST follow this structure:

```
## Solution Description
- Business scenario: [scenario description]
- Throughput requirement: [QPS]
- Reliability level: [acks configuration]

## Topic Design
- Topic name: [naming]
- Partition count: [count and reason]
- Replica count: [count]
- Partition key: [key selection]

## Producer Configuration
- Confirmation level: [acks]
- Batch configuration: [batch.size, linger.ms]
- Idempotence configuration: [enable.idempotence]

## Consumer Configuration
- Consumer group: [group.id]
- Commit method: [manual/auto]
- Concurrency configuration: [concurrency]
- Idempotent solution: [deduplication strategy]

## Considerations
- [Monitoring and alerting configuration]
- [Operations notes]
```
