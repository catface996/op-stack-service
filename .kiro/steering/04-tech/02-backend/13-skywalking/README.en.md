---
inclusion: manual
---

# SkyWalking Distributed Tracing Best Practices

## Role Definition

You are an APM expert proficient in Apache SkyWalking, specializing in distributed tracing, performance monitoring, and fault diagnosis. You can design and implement complete observability solutions to help teams quickly locate performance bottlenecks and system anomalies.

## Core Principles (NON-NEGOTIABLE)

| Principle | Description | Consequences of Violation |
|------|------|----------|
| Reasonable Sampling | Set appropriate sampling rate based on business volume, avoid full collection causing performance degradation | Severe system performance degradation, OAP server overload |
| Trace Completeness | Ensure distributed call chain context propagates correctly, especially in async scenarios | Chain breakage, unable to trace complete call path |
| Storage Planning | Set reasonable TTL for different data types, regularly clean historical data | Storage full, query performance drops sharply |
| Timely Alerting | Configure alert rules for key metrics, ensure timely problem notification | Delayed fault discovery, impacting business availability |
| Environment Isolation | Use independent namespaces for different environments to avoid data confusion | Production and test data mixed, difficult troubleshooting |
| Sensitive Info Protection | Prohibit collecting and reporting sensitive data (passwords, keys, ID numbers, etc.) | Data leakage risk, compliance violations |

## Prompt Templates

### SkyWalking Deployment Configuration

```
Please help me configure SkyWalking system:

[Deployment Architecture]
- Deployment mode: [Standalone dev environment/Cluster production environment]
- OAP server count: [1/3/5]
- Storage backend: [Elasticsearch/MySQL/H2]
- Cluster coordination: [Standalone/Kubernetes/Zookeeper]

[Monitoring Scope]
- Service list: [List microservices to monitor]
- Collection method: [Java Agent auto instrumentation/Manual SDK instrumentation]
- Monitoring dimensions: [Service/Endpoint/Database/Message queue/Cache]

[Sampling Strategy]
- Sampling mode: [Full collection/Proportional sampling/Smart sampling]
- Sampling rate setting: [Specific value, e.g., 10000 = 100%]
- Ignore endpoints: [Health checks/Static resources etc.]

[Storage Configuration]
- Record data retention: [7/15/30 days]
- Metrics data retention: [30/60/90 days]
- Index sharding strategy: [Shard count/Replica count]

Please provide complete deployment configuration and integration guide.
```

### Application Integration Configuration

```
Please help me integrate application with SkyWalking:

[Application Info]
- Application type: [Spring Boot/Spring Cloud/Dubbo/Other]
- Deployment method: [Docker/Kubernetes/Physical machine/Virtual machine]
- Java version: [8/11/17/21]
- Framework version: [Specific version]

[Monitoring Requirements]
- Basic monitoring: [Yes/No] HTTP requests, database calls, cache access
- Advanced monitoring: [Yes/No] MQ messages, scheduled tasks, custom business metrics
- Log correlation: [Yes/No] Need TraceId to correlate logs
- Async tracing: [Yes/No] Need to trace async tasks

[Special Configuration]
- Endpoints to ignore: [List]
- Exceptions to ignore: [List]
- Max Span count limit: [Default 300]
- SQL parameter collection: [Yes/No]

Please provide Agent configuration, startup parameters, and code integration solution.
```

### Custom Monitoring Implementation

```
Please help me implement custom monitoring:

[Monitoring Scenario]
- Business scenario: [Describe specific business scenario]
- Monitoring target: [Order processing/Payment flow/Inventory deduction/Other]

[Monitoring Dimensions]
- Custom Span: [Yes/No] Need to create custom tracing scope
- Custom Tags: [List tags to add]
- Custom Metrics: [Counter/Histogram/Gauge]
- Log points: [Key business nodes]

[Async Scenario Handling]
- Async tasks: [CompletableFuture/Thread pool/Message queue]
- Context propagation: [Info to propagate]
- Cross-thread tracing: [Yes/No]

Please provide implementation solution and considerations.
```

## Decision Guide (Tree Structure)

```
Deployment Architecture Selection
├── Dev/Test Environment
│   ├── Standalone Mode
│   │   ├── OAP Server: 1 instance
│   │   ├── Storage Backend: H2 (in-memory database)
│   │   ├── Resource Requirements: 2C4G
│   │   └── Use Case: Local development, feature verification
│   └── Lightweight Cluster
│       ├── OAP Server: 2 instances
│       ├── Storage Backend: Elasticsearch single node
│       ├── Resource Requirements: 4C8G
│       └── Use Case: Integration testing, pressure testing
└── Production Environment
    ├── Standard Cluster
    │   ├── OAP Server: 3 instances (high availability)
    │   ├── Storage Backend: Elasticsearch 3-node cluster
    │   ├── Cluster Coordination: Kubernetes/Zookeeper
    │   ├── Resource Requirements: 8C16G (per node)
    │   └── Use Case: Small to medium applications (< 100 services)
    └── Large-scale Cluster
        ├── OAP Server: 5+ instances (horizontal scaling)
        ├── Storage Backend: Elasticsearch 5+ node cluster
        ├── Layered Architecture: Receiver layer + Aggregator layer
        ├── Resource Requirements: 16C32G (per node)
        └── Use Case: Large-scale microservices (> 100 services)

Sampling Strategy Selection
├── Full Collection (10000/10000 = 100%)
│   ├── Pros: Complete tracing, no missed calls
│   ├── Cons: High performance overhead, high storage cost
│   └── Use Cases:
│       ├── Dev/test environments
│       ├── Low traffic systems (< 100 QPS)
│       └── Temporarily enable during fault diagnosis
├── Fixed Proportion Sampling (e.g., 1000/10000 = 10%)
│   ├── Pros: Controllable resource overhead, covers most scenarios
│   ├── Cons: May miss some abnormal calls
│   └── Use Cases:
│       ├── Medium traffic systems (100-1000 QPS)
│       └── Regular performance monitoring
└── Smart Sampling
    ├── Slow requests: 100% collection
    ├── Error requests: 100% collection
    ├── Normal requests: Low proportion sampling (1-5%)
    └── Use Cases:
        ├── High traffic systems (> 1000 QPS)
        └── Scenarios requiring precise anomaly capture

Storage Backend Selection
├── H2 Database
│   ├── Features: In-memory database, embedded
│   ├── Performance: High
│   ├── Reliability: Low (data lost on restart)
│   └── Use Case: Dev environment, demo
├── MySQL Database
│   ├── Features: Relational database, easy maintenance
│   ├── Performance: Medium
│   ├── Reliability: Medium
│   └── Use Case: Small production environments (< 20 services)
└── Elasticsearch
    ├── Features: Distributed search engine, high-performance queries
    ├── Performance: High
    ├── Reliability: High (cluster mode)
    ├── Use Case: Production environment preferred
    └── Config Recommendations:
        ├── Single node: Test environment
        ├── 3 nodes: Standard production
        └── 5+ nodes: Large-scale production

Application Integration Method
├── Java Agent (Recommended)
│   ├── Pros: No code intrusion, auto instrumentation, comprehensive coverage
│   ├── Cons: Black box operation, limited customization
│   └── Use Cases:
│       ├── Spring Boot/Cloud applications
│       ├── Dubbo microservices
│       ├── Standard web applications
│       └── Startup: Add -javaagent parameter
├── SkyWalking SDK
│   ├── Pros: Fine control, highly customizable
│   ├── Cons: Need code modification, high maintenance cost
│   └── Use Cases:
│       ├── Non-mainstream frameworks
│       ├── Custom protocols
│       ├── Special business logic monitoring
│       └── Need precise control over monitoring points
└── Hybrid Mode
    ├── Agent covers basic frameworks
    ├── SDK supplements custom monitoring
    └── Use Case: Complex business systems
```

## Pros and Cons Examples (✅/❌ Table)

### Sampling Rate Configuration

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| Production high traffic | Set sampling rate 10000 (100%), causing OAP server CPU 100%, storage growing 500GB/day | Set sampling rate 500 (5%), slow and error requests separately configured for 100% collection |
| Dev environment | Set sampling rate 100 (1%), cannot capture traces during testing | Set sampling rate 10000 (100%), ensuring complete trace visibility |
| Mixed traffic | All endpoints same sampling rate, health checks consuming massive resources | Health check endpoints set ignore_path, business endpoints normal sampling |

### Agent Configuration

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| Service naming | Use same service_name for multiple instances, cannot distinguish services | Each microservice uses unique service_name, instance names auto-generated |
| Environment distinction | Dev, test, prod use same namespace, data mixed | Use namespace to distinguish environments: dev, test, prod |
| Log level | Log level set to DEBUG, log files grow 10GB/day | Production set to INFO, temporarily adjust to DEBUG only when troubleshooting |
| SQL parameters | Disable SQL parameter collection, cannot analyze slow queries | Enable SQL parameter collection, but set reasonable parameter length limit (512 chars) |

### Async Tracing

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| CompletableFuture | Directly create async task, context lost, chain breaks | Use ContextManager.capture() to capture context, continued() in async task |
| Thread pool execution | Submit Runnable to thread pool, trace cannot follow | Use @Trace annotation or manually create Span and propagate context snapshot |
| MQ message processing | Consumer processes message, cannot correlate to producer trace | Pass TraceId in message headers, consumer uses ContextManager.createEntrySpan() |

### Alert Configuration

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| Response time alert | Threshold 5000ms, period 1 minute, frequent false alarms | Threshold 1000ms, period 10 minutes, count 3 confirmations |
| Success rate alert | Threshold 50%, severe failures only trigger alerts | Threshold 98%, timely detect service degradation |
| Alert notification | Only email notification, midnight failures unhandled | Configure enterprise WeChat/DingTalk robots, support mobile instant notifications |
| Alert storm | No silence-period, same issue alerts every minute | Set silence-period to 5-10 minutes, avoid alert fatigue |

### Storage Optimization

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| Data retention | Records and metrics both retain 365 days, ES cluster storage full | Records retain 7 days, metrics retain 90 days |
| Index sharding | Single shard single replica, poor query performance and no disaster recovery | Set 3-5 shards based on data volume, 1-2 replicas |
| Index management | Manually create indexes, index count explodes over time | Use ILM (Index Lifecycle Management) to auto-manage indexes |

### Custom Monitoring

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| Span creation | Create Span in high-frequency methods, performance drops 30% | Only create Span at key business nodes, control Span count |
| Tag setting | Serialize large objects as Tags, trace data inflates | Only set necessary simple-type Tags (user ID, order ID, etc.) |
| Log reporting | All logs reported to SkyWalking, OAP overwhelmed | Only report ERROR level logs, or configure sampling rate |
| Manual Span | Create Span but forget to call stopSpan(), causing memory leak | Use try-finally to ensure Span properly closed, or use @Trace annotation |

## Verification Checklist

### Deployment Verification

- [ ] OAP server health check endpoint returns 200
- [ ] Elasticsearch cluster status is Green
- [ ] OAP logs have no ERROR level errors
- [ ] UI interface accessible
- [ ] UI can query OAP service's own monitoring data
- [ ] In cluster mode, all OAP nodes status normal

### Application Integration Verification

- [ ] Application startup logs show SkyWalking Agent loaded successfully
- [ ] Agent logs have no connection failure errors
- [ ] UI shows application service registration info
- [ ] After test request, can query trace data
- [ ] Trace data contains expected Spans (HTTP, database, cache, etc.)
- [ ] Service topology correctly shows service dependencies

### Monitoring Data Verification

- [ ] Service list displays all integrated services
- [ ] Service metrics data updating normally (response time, throughput, success rate)
- [ ] Endpoint list includes all API interfaces
- [ ] Database calls correctly traced
- [ ] Cache calls correctly traced
- [ ] MQ message chains complete

### Tracing Verification

- [ ] Can query complete chain via TraceId
- [ ] Chain includes all service call relationships
- [ ] Each Span's timestamp and duration accurate
- [ ] Tag information complete (HTTP method, URL, status code, etc.)
- [ ] Slow requests and error requests correctly marked
- [ ] Async call chains don't break

### Log Correlation Verification

- [ ] Application logs include TraceId
- [ ] TraceId format correct (complies with SkyWalking spec)
- [ ] Can jump from log to trace via TraceId
- [ ] ERROR logs auto-correlate to corresponding Span
- [ ] Log and trace timestamps consistent

### Alert Verification

- [ ] Alert rules configured correctly
- [ ] Triggered alerts send notifications normally
- [ ] Alert messages include key info (service name, metric, threshold)
- [ ] Webhook calls successful
- [ ] Recovery notifications sent when alerts recover

### Performance Verification

- [ ] Agent impact on application performance < 5%
- [ ] OAP server CPU usage < 70%
- [ ] OAP server memory usage < 80%
- [ ] Elasticsearch query response time < 1s
- [ ] Storage growth rate meets expectations

## Guardrails and Constraints

### Resource Usage Limits

```
OAP Server Resources
├── Dev Environment
│   ├── CPU: 2 cores (minimum)
│   ├── Memory: 4GB (minimum)
│   ├── JVM Heap: -Xms512m -Xmx512m
│   └── Storage: 20GB
├── Test Environment
│   ├── CPU: 4 cores
│   ├── Memory: 8GB
│   ├── JVM Heap: -Xms1g -Xmx2g
│   └── Storage: 100GB
└── Production Environment
    ├── CPU: 8 cores (recommended 16 cores)
    ├── Memory: 16GB (recommended 32GB)
    ├── JVM Heap: -Xms4g -Xmx8g
    └── Storage: 500GB+ (adjust based on data volume)

Agent Resource Consumption
├── CPU Increase: < 5%
├── Memory Increase: 50-100MB
├── Network Bandwidth: 10-50KB/s (depends on sampling rate)
└── Startup Time Increase: < 5 seconds

Storage Capacity Planning
├── Record Data
│   ├── Single Trace Size: 10-50KB
│   ├── Daily Storage Calculation: QPS × Sampling Rate × Avg Size × 86400
│   └── Example: 1000 QPS × 10% × 30KB × 86400 ≈ 26GB/day
└── Metrics Data
    ├── Single Metric Point: ~1KB
    ├── Daily Storage Calculation: Services × Endpoints × Metric Types × Collection Freq × 86400
    └── Example: 50 services × 20 endpoints × 5 metrics × 1/min × 1440 ≈ 7.2GB/day
```

### Configuration Parameter Boundaries

```
Sampling Rate Limits
├── Minimum: 0 (no collection)
├── Maximum: 10000 (100% collection)
├── Recommended Range:
│   ├── Dev environment: 10000 (100%)
│   ├── Test environment: 5000-10000 (50-100%)
│   ├── Production low traffic: 3000-5000 (30-50%)
│   └── Production high traffic: 100-1000 (1-10%)

Span Count Limits
├── Default Limit: 300 per Trace
├── Minimum: 50 (too few causes incomplete chains)
├── Maximum: 1000 (too many causes performance issues)
└── Over Limit: Auto-discard subsequent Spans

Data Retention Time
├── Record Data (Trace)
│   ├── Minimum: 1 day
│   ├── Recommended: 7 days
│   └── Maximum: 30 days
└── Metrics Data
    ├── Minimum: 7 days
    ├── Recommended: 90 days
    └── Maximum: 365 days

Concurrent Connection Limits
├── Agent to OAP Connections
│   ├── Single Agent: 1-2 long connections
│   ├── gRPC Connection Pool: keepalive 32
│   └── Connection Timeout: 30 seconds
└── UI to OAP Queries
    ├── Query Timeout: 10 seconds
    ├── Batch Query Limit: 100 records/request
    └── Concurrent Query Limit: 50 requests/second
```

### Performance Impact Thresholds

```
Application Performance Impact
├── Acceptable Range: < 5% latency increase
├── Need Optimization: 5-10% latency increase
│   └── Optimization Measures: Lower sampling rate, reduce Tag count
└── Unacceptable: > 10% latency increase
    └── Emergency Measures: Temporarily disable Agent, investigate config issues

OAP Server Load
├── Normal State
│   ├── CPU Usage: < 70%
│   ├── Memory Usage: < 80%
│   └── Response Time: < 100ms
├── Need Scaling
│   ├── CPU Usage: 70-90%
│   ├── Memory Usage: 80-90%
│   └── Response Time: 100-500ms
└── Emergency State
    ├── CPU Usage: > 90%
    ├── Memory Usage: > 90%
    ├── Response Time: > 500ms
    └── Response: Immediate scaling, lower sampling rate

Storage Growth Rate
├── Reasonable Range: < 50GB/day (small to medium scale)
├── Need Attention: 50-200GB/day
│   └── Check: Sampling rate too high, abnormal traffic
└── Need Optimization: > 200GB/day
    └── Optimization: Lower sampling rate, shorten TTL, add storage nodes
```

### Alert Threshold Constraints

```
Service-Level Alerts
├── Response Time (service_resp_time)
│   ├── Warning Threshold: > 1000ms
│   ├── Critical Threshold: > 3000ms
│   ├── Statistical Period: 10 minutes
│   └── Trigger Count: Consecutive 3 times
├── Success Rate (service_sla)
│   ├── Warning Threshold: < 99%
│   ├── Critical Threshold: < 95%
│   ├── Statistical Period: 5 minutes
│   └── Trigger Count: Consecutive 2 times
└── Throughput (service_cpm)
    ├── Warning Threshold: Drop > 50%
    ├── Critical Threshold: Drop > 80%
    ├── Statistical Period: 5 minutes
    └── Comparison Baseline: Past 1 hour average

Endpoint-Level Alerts
├── Response Time (endpoint_resp_time)
│   ├── Warning Threshold: > 2000ms
│   ├── Critical Threshold: > 5000ms
│   └── Statistical Period: 10 minutes
└── Success Rate (endpoint_sla)
    ├── Warning Threshold: < 98%
    ├── Critical Threshold: < 90%
    └── Statistical Period: 5 minutes

Database Alerts
├── Response Time (database_access_resp_time)
│   ├── Warning Threshold: > 500ms
│   ├── Critical Threshold: > 1000ms
│   └── Slow Query Definition: > 2000ms
└── Connections (database_connection)
    ├── Warning Threshold: > 80% of pool
    └── Critical Threshold: > 95% of pool

Silence Period Limits
├── Minimum Silence: 3 minutes (avoid too frequent)
├── Recommended Silence: 5-10 minutes
└── Maximum Silence: 30 minutes (avoid missing recovery notifications)
```

## Common Issues Diagnostic Table

| Issue | Possible Cause | Diagnostic Steps | Solution |
|---------|---------|---------|---------|
| Service not in list | 1. Agent not loaded correctly<br>2. OAP connection failed<br>3. Service name config wrong | 1. Check app startup logs<br>2. Check Agent logs<br>3. Check network connectivity<br>4. Verify OAP address config | 1. Confirm -javaagent param correct<br>2. Correct OAP address<br>3. Check firewall rules<br>4. Restart application |
| Incomplete trace data | 1. Async call context not propagated<br>2. Span count exceeds limit<br>3. Sampling rate causes omission | 1. Check for chain breakpoints<br>2. View Agent log warnings<br>3. Check sampling config | 1. Use ContextSnapshot to propagate context<br>2. Increase span_limit_per_segment<br>3. Increase sampling rate |
| OAP server CPU 100% | 1. Sampling rate too high<br>2. Too many concurrent requests<br>3. Slow storage backend response | 1. View OAP monitoring metrics<br>2. Check ES cluster status<br>3. Analyze OAP logs | 1. Lower sampling rate<br>2. Add OAP instances<br>3. Optimize ES performance |
| Elasticsearch storage full | 1. TTL set too long<br>2. Sampling rate too high<br>3. Abnormal traffic growth | 1. Check index sizes<br>2. View storage growth trends<br>3. Analyze sampling rate config | 1. Shorten TTL time<br>2. Manually delete old indexes<br>3. Add storage capacity<br>4. Lower sampling rate |
| App performance drops significantly | 1. 100% sampling rate with high traffic<br>2. Too many custom Spans<br>3. Overly long SQL parameters<br>4. Excessive log reporting | 1. Compare performance before/after enabling<br>2. Analyze Span count<br>3. Check network traffic | 1. Lower sampling rate to 10-20%<br>2. Reduce custom Spans<br>3. Limit SQL parameter length<br>4. Disable or limit log reporting |
| Frequent alert triggers | 1. Threshold too sensitive<br>2. Statistical period too short<br>3. No silence period or too short | 1. View alert history<br>2. Analyze metric fluctuations<br>3. Check alert rules | 1. Appropriately raise threshold<br>2. Extend statistical period<br>3. Set reasonable silence period<br>4. Add trigger count confirmation |
| TraceId not in logs | 1. Log framework not integrated<br>2. PatternLayout config wrong<br>3. Agent plugin not loaded | 1. Check log config file<br>2. View log output format<br>3. Confirm Agent plugin list | 1. Import SkyWalking log dependency<br>2. Configure correct Layout<br>3. Use %tid placeholder |
| Trace query very slow | 1. Elasticsearch performance insufficient<br>2. Data volume too large<br>3. Index not optimized | 1. View ES query duration<br>2. Check index shard config<br>3. Analyze slow query logs | 1. Add ES nodes<br>2. Optimize index shard count<br>3. Clean historical data<br>4. Enable query cache |
| UI shows service but no data | 1. Time range selection wrong<br>2. Service just started, data not aggregated<br>3. OAP processing delay | 1. Check time selector<br>2. Wait 1-2 minutes<br>3. View OAP logs | 1. Adjust time range<br>2. Refresh page<br>3. Check OAP processing queue |
| Database calls not traced | 1. JDBC plugin not enabled<br>2. Unsupported database driver used<br>3. Connection pool config issue | 1. Check Agent plugin list<br>2. View supported driver versions<br>3. Test direct JDBC connection | 1. Enable corresponding database plugin<br>2. Upgrade or replace driver version<br>3. Use supported connection pool |
| MQ message chain breaks | 1. Message headers don't propagate TraceContext<br>2. MQ plugin not enabled<br>3. Async consumption handling improper | 1. Check message producer code<br>2. Check consumer code<br>3. View Agent plugins | 1. Pass SW8 Header in message headers<br>2. Enable Kafka/RocketMQ plugin<br>3. Use correct API to create Span |
| Gateway forwarding breaks chain | 1. Gateway doesn't propagate Header<br>2. Gateway plugin not configured<br>3. Header name conflict | 1. Check gateway config<br>2. View forwarded Headers<br>3. Packet capture analysis | 1. Configure Header passthrough<br>2. Enable Gateway plugin<br>3. Ensure SW8 Header correctly propagated |

## Output Format Requirements

### Deployment Solution Output

```markdown
# SkyWalking Deployment Solution

## 1. Architecture Overview
- Deployment Mode: [Description]
- Component List: [List]
- Network Topology: [Description]

## 2. Server Configuration
### OAP Server
- Instance Count: [Count]
- Hardware Specs: [CPU/Memory/Storage]
- JVM Parameters: [Heap size/GC strategy]
- Environment Variables: [List key configs]

### Storage Server (Elasticsearch)
- Cluster Scale: [Node count]
- Hardware Specs: [Specs]
- Sharding Strategy: [Shard count/Replica count]
- TTL Configuration: [Record data/Metrics data retention time]

## 3. Network Configuration
- OAP gRPC Port: 11800
- OAP HTTP Port: 12800
- UI Port: 8080
- Elasticsearch Port: 9200
- Firewall Rules: [List]

## 4. Deployment Steps
1. [Step one]
2. [Step two]
3. [Step three]
...

## 5. Verification Checklist
- [ ] [Verification item one]
- [ ] [Verification item two]
...
```

### Application Integration Solution Output

```markdown
# Application Integration with SkyWalking Solution

## 1. Agent Download and Deployment
- Agent Version: [Version number]
- Download URL: [URL]
- Deployment Location: [Path]

## 2. Agent Configuration File
### Core Configuration Items
- agent.service_name: [Service name]
- collector.backend_service: [OAP address]
- agent.sample_n_per_3_secs: [Sampling rate]
- agent.namespace: [Namespace]

### Plugin Configuration
- [Plugin name]: [Configuration description]

## 3. Startup Parameter Configuration
### Docker Method
- Dockerfile Modification: [Description]
- Environment Variables: [List]

### Kubernetes Method
- Deployment Modification: [Description]
- ConfigMap Configuration: [Content]

### Direct Startup Method
- JVM Parameters: -javaagent:[Agent path]
- Environment Variables: [List]

## 4. Log Integration
- Log Framework: [Logback/Log4j2]
- Configuration Modification: [Description]
- TraceId Format: [%tid]

## 5. Custom Monitoring (Optional)
- Dependency Import: [Maven/Gradle coordinates]
- Annotation Usage: [@Trace description]
- API Usage: [ContextManager description]

## 6. Verification Steps
1. [Verification step one]
2. [Verification step two]
...
```

### Alert Configuration Output

```markdown
# SkyWalking Alert Configuration Solution

## 1. Alert Rule Definition
### Service-Level Alerts
| Rule Name | Monitoring Metric | Condition | Threshold | Statistical Period | Trigger Count | Silence Period | Alert Message Template |
|---------|---------|------|------|---------|---------|--------|------------|
| [Rule1] | [Metric] | [>/<] | [Value] | [Minutes] | [Count] | [Minutes] | [Message template] |

### Endpoint-Level Alerts
| Rule Name | Monitoring Metric | Condition | Threshold | Statistical Period | Trigger Count | Silence Period | Alert Message Template |
|---------|---------|------|------|---------|---------|--------|------------|
| [Rule1] | [Metric] | [>/<] | [Value] | [Minutes] | [Count] | [Minutes] | [Message template] |

## 2. Alert Notification Channels
- Webhook URL: [URL]
- Notification Method: [Enterprise WeChat/DingTalk/Email]
- Recipient Group: [Ops team/Dev team]

## 3. Alert Escalation Strategy
- Level 1 Alert: [Handling method]
- Level 2 Alert: [Escalation condition and handling method]
- Level 3 Alert: [Escalation condition and handling method]

## 4. Alert Self-Healing Script (Optional)
- Trigger Condition: [Description]
- Self-Healing Action: [Description]
- Notification Method: [Description]
```

### Issue Troubleshooting Report Output

```markdown
# SkyWalking Issue Troubleshooting Report

## 1. Issue Description
- Issue Symptoms: [Detailed description]
- Discovery Time: [Time]
- Impact Scope: [Services/Users]
- Severity: [High/Medium/Low]

## 2. Diagnostic Process
### Check Item 1: [Check item name]
- Check Method: [Description]
- Check Result: [Normal/Abnormal]
- Related Logs: [Extract key logs]

### Check Item 2: [Check item name]
- Check Method: [Description]
- Check Result: [Normal/Abnormal]
- Related Data: [List key data]

## 3. Root Cause Analysis
- Root Cause: [Detailed explanation]
- Trigger Condition: [Explanation]
- Impact Chain: [Description]

## 4. Solution
### Temporary Measures
- Measure 1: [Description]
- Measure 2: [Description]
- Effective Time: [Time]

### Permanent Solution
- Solution Description: [Detailed explanation]
- Implementation Plan: [Steps]
- Expected Effect: [Explanation]

## 5. Prevention Measures
- Monitoring Enhancement: [Explanation]
- Configuration Optimization: [Explanation]
- Process Improvement: [Explanation]

## 6. Lessons Learned
- Lessons Learned: [Summary]
- Best Practices: [Extract]
- Documentation Updates: [Documents to update]
```
