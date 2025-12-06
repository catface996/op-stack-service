---
inclusion: manual
---

# Sentinel Circuit Breaking and Rate Limiting Best Practices

## Role Definition

You are a microservice stability expert proficient in Alibaba Sentinel, with deep understanding of the principles of traffic control, circuit breaking degradation, and system protection mechanisms. You excel at designing multi-layered protection systems, formulating precise rate limiting strategies, and implementing graceful degradation solutions, ensuring the system remains stable during traffic peaks and dependency failures. Your responsibilities include: protecting core business interfaces from being overwhelmed, preventing service avalanches, implementing adaptive traffic shaping, establishing complete fault tolerance mechanisms, and enabling the system with powerful fault tolerance and self-healing capabilities.

## Core Principles (NON-NEGOTIABLE)

| Principle | Description | Consequences of Violation |
|-----------|-------------|--------------------------|
| Core Interface MUST Have Rate Limiting | All public APIs and core business interfaces must configure rate limiting rules | Traffic surge leads to system collapse |
| Dependent Services MUST Have Circuit Breaking | All external dependencies must configure circuit breaking degradation rules | Dependency failures lead to avalanche |
| Rules MUST Be Persisted | Rate limiting and circuit breaking rules must be persisted to Nacos/Apollo | Rules lost after restart, protection fails |
| Degradation Has Fallback Solutions | Each circuit breaker must provide fallback logic | Poor user experience after degradation, business damage |
| Hot Parameter Protection | Frequently accessed hot data must have separate rate limiting | Hot data overwhelms the system |
| System Adaptive Protection | MUST configure system protection rules for CPU/Load/RT | System overload without protection |
| Accurate Exception Statistics | Clearly define which exceptions count towards circuit breaking statistics | Misjudgment leads to frequent circuit breaking |
| Reasonable Rate Limiting Strategy | Set thresholds based on actual capacity with buffer reserved | Threshold too low affects business, too high provides no protection |

## Prompt Templates

### Template 1: Rate Limiting Rule Configuration

```
I need to configure rate limiting rules for an interface:

**Protection Scenario**:
- Interface Type: [REST API/RPC Service/Message Consumer]
- Interface Path: [Specific path or resource name]
- Business Importance: [Core/Important/General]

**Traffic Characteristics**:
- Normal QPS: [Daily traffic level]
- Peak QPS: [Peak traffic level]
- Traffic Pattern: [Uniform/Spiky/Periodic]
- Upstream Source: [Single source/Multiple sources]

**Rate Limiting Strategy**:
- Limiting Mode: [Fast Fail/Warm Up/Uniform Rate]
- Threshold Type: [QPS/Concurrent Threads]
- Distinguish Source: [Yes/No]
- Associate with Other Interfaces: [Yes/No]

Please provide:
1. Complete rate limiting rule configuration (with threshold calculation basis)
2. Warm-up scenario configuration
3. Use cases for uniform queuing
4. Rule JSON format (for persistence)
5. BlockHandler implementation recommendations
```

### Template 2: Circuit Breaking Degradation Configuration

```
I need to configure circuit breaking degradation for service calls:

**Dependent Service**:
- Service Name: [Called service]
- Call Method: [Feign/RestTemplate/Dubbo]
- Importance Level: [Strong dependency/Weak dependency]

**Service Characteristics**:
- Normal Response Time: [Average RT]
- Exception Rate Range: [Normal exception ratio]
- Call Frequency: [QPS]
- Timeout Configuration: [Current timeout]

**Circuit Breaking Strategy**:
- Circuit Breaking Type: [Slow Call Ratio/Exception Ratio/Exception Count]
- Circuit Breaking Threshold: [Specific value]
- Circuit Breaking Duration: [Recovery wait time]
- Minimum Requests: [Statistical minimum sample]

**Degradation Solution**:
- Degradation Strategy: [Return default value/Cache/Mock data/Throw exception]
- Business Impact: [Impact on users]

Please provide:
1. Circuit breaking rule configuration (with threshold rationality analysis)
2. Fallback implementation solution
3. Business processing logic after degradation
4. Circuit breaker recovery strategy
5. Monitoring and alerting recommendations
```

### Template 3: Hot Parameter Rate Limiting

```
I need to configure hot parameter rate limiting:

**Hot Scenario**:
- Business Scenario: [Product details/User info/Resource access]
- Hot Parameter: [Product ID/User ID/etc.]
- Parameter Position: [Nth parameter]

**Access Characteristics**:
- Overall QPS: [Total access volume]
- Hot QPS: [Hot data access volume]
- Hot Ratio: [Percentage of hot data]
- Hot Data Features: [Celebrity products/VIP users/etc.]

**Protection Strategy**:
- General Rate Limiting: [Normal data threshold]
- Hot Rate Limiting: [Hot data threshold]
- Exception Items: [Special parameter value list]

Please provide:
1. Hot parameter rule configuration
2. Exception item configuration solution
3. Parameter extraction method (@SentinelParam)
4. Dynamic adjustment strategy
5. Coordination with general rate limiting
```

### Template 4: System Protection Rules

```
I need to configure system-level protection rules:

**System Environment**:
- Machine Configuration: [CPU cores/Memory/etc.]
- Application Type: [Compute-intensive/IO-intensive]
- Deployment Method: [Container/VM/Physical machine]

**System Metrics**:
- CPU Usage: [Normal range]
- System Load: [Normal Load value]
- Average RT: [Normal response time]
- Concurrent Threads: [Normal concurrency]

**Protection Target**:
- Prevention Scenario: [CPU spike/OOM/Thread exhaustion]
- Protection Priority: [Core interfaces priority]

Please provide:
1. System protection rule configuration
2. Basis for each metric threshold setting
3. Multi-metric combined protection strategy
4. Recovery mechanism after triggering protection
5. Monitoring and tuning recommendations
```

## Decision Guide

```
Sentinel Rule Configuration Decision Tree
│
├─ Select Protection Type
│  ├─ Need to control traffic rate?
│  │  ├─ Yes → Flow Control Rule (FlowRule)
│  │  │     │
│  │  │     ├─ Threshold Type
│  │  │     │  ├─ Control request rate → QPS mode
│  │  │     │  └─ Control concurrency → Thread count mode
│  │  │     │
│  │  │     ├─ Flow Control Mode
│  │  │     │  ├─ Direct limiting → STRATEGY_DIRECT
│  │  │     │  ├─ Related limiting → STRATEGY_RELATE (protect related resources)
│  │  │     │  └─ Chain limiting → STRATEGY_CHAIN (specific call chain)
│  │  │     │
│  │  │     └─ Flow Control Effect
│  │  │        ├─ Fast fail → CONTROL_BEHAVIOR_DEFAULT
│  │  │        ├─ Warm up → CONTROL_BEHAVIOR_WARM_UP
│  │  │        ├─ Uniform rate → CONTROL_BEHAVIOR_RATE_LIMITER
│  │  │        └─ Warm up + Queue → CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER
│  │  │
│  │  └─ No → Continue judgment
│  │
│  ├─ Need circuit breaking degradation?
│  │  ├─ Yes → Circuit Breaking Rule (DegradeRule)
│  │  │     │
│  │  │     ├─ Circuit Breaking Strategy
│  │  │     │  ├─ Slow call ratio → SLOW_REQUEST_RATIO
│  │  │     │  │  └─ Applicable: Unstable RT, slow response
│  │  │     │  ├─ Exception ratio → ERROR_RATIO
│  │  │     │  │  └─ Applicable: Intermittent service failures
│  │  │     │  └─ Exception count → ERROR_COUNT
│  │  │     │     └─ Applicable: Low traffic scenarios
│  │  │     │
│  │  │     ├─ Parameter Settings
│  │  │     │  ├─ count: Threshold (ratio 0-1 or exception count)
│  │  │     │  ├─ timeWindow: Circuit breaking duration (seconds)
│  │  │     │  ├─ minRequestAmount: Minimum request count
│  │  │     │  └─ statIntervalMs: Statistical duration
│  │  │     │
│  │  │     └─ Degradation Handling
│  │  │        ├─ Return default value (query interfaces)
│  │  │        ├─ Return cached data
│  │  │        ├─ Degrade to backup service
│  │  │        └─ Throw business exception
│  │  │
│  │  └─ No → Continue judgment
│  │
│  ├─ Need hot parameter protection?
│  │  └─ Yes → Hot Parameter Rule (ParamFlowRule)
│  │        ├─ Parameter index: paramIdx
│  │        ├─ General threshold: count
│  │        ├─ Exception items: paramFlowItemList
│  │        └─ Statistics window: durationInSec
│  │
│  └─ Need system protection?
│     └─ Yes → System Protection Rule (SystemRule)
│           ├─ CPU usage: highestCpuUsage
│           ├─ System Load: highestSystemLoad
│           ├─ Average RT: avgRt
│           ├─ Concurrent threads: maxThread
│           └─ Entry QPS: qps
│
├─ Rule Persistence Solution
│  ├─ Nacos (Recommended)
│  │  ├─ Advantages: Dynamic configuration, version management, gray release
│  │  └─ Configuration: sentinel-datasource-nacos
│  │
│  ├─ Apollo
│  │  └─ Applicable: Already using Apollo as config center
│  │
│  └─ File
│     └─ Applicable: Development and test environments
│
└─ Exception Handling Strategy
   ├─ Global Exception Handling
   │  ├─ FlowException → Rate limiting exception (429)
   │  ├─ DegradeException → Degradation exception (503)
   │  ├─ ParamFlowException → Hot parameter limiting (429)
   │  └─ SystemBlockException → System protection (503)
   │
   └─ Method-level Handling
      ├─ blockHandler → Rate limiting/Circuit breaking handling
      │  └─ Same parameters + BlockException
      │
      └─ fallback → Business exception degradation
         └─ Same parameters + Throwable
```

## Positive and Negative Examples

### Example 1: Rate Limiting Rule Configuration

| Dimension | ❌ Wrong Approach | ✅ Correct Approach |
|-----------|------------------|---------------------|
| **Threshold Setting** | Set by feeling, fixed at 100 QPS | Set based on stress test results and capacity assessment |
| **Rule Persistence** | Hard-coded rules in code | Persist to Nacos for dynamic adjustment |
| **Flow Control Effect** | Fast fail for all interfaces | Choose based on scenario: warm-up/queuing/fast fail |
| **BlockHandler** | Not implemented, users see 500 error | Implement friendly rate limiting prompt |
| **Monitoring & Alerting** | No monitoring of limiting situations | Monitor limiting rate, evaluate threshold rationality |

**Scenario**: Order submission interface rate limiting

❌ **Wrong Example**:
```
Unreasonable rate limiting configuration:
- Hard-coded rate limiting rules in code, cannot be adjusted dynamically
- QPS threshold set to 100 without stress testing basis
- Returns 500 error after rate limiting, poor user experience
- Uses fast fail, large number of requests rejected during traffic surge
- No monitoring of limiting trigger situations, cannot optimize
```

✅ **Correct Example**:
```
Reasonable rate limiting configuration:
- Rules persisted to Nacos for dynamic adjustment
- Stress tested capacity at 300 QPS, threshold set to 200 (with buffer)
- Uses warm-up mode, gradually opens traffic within 10 seconds of cold start
- BlockHandler returns friendly message: System busy, please try again later
- Monitor limiting trigger rate, regularly assess threshold rationality
- Differentiate user levels, VIP users have higher thresholds
```

### Example 2: Circuit Breaking Degradation Configuration

| Dimension | ❌ Wrong Approach | ✅ Correct Approach |
|-----------|------------------|---------------------|
| **Circuit Breaking Strategy** | No circuit breaking configured, rely on timeout | Combined slow call + exception ratio circuit breaking |
| **Threshold Setting** | Exception ratio set to 0.9 (90%) | Set based on dependency stability (e.g., 0.3) |
| **Minimum Requests** | Use default value 5 | Set reasonable sample size based on traffic |
| **Fallback** | Not implemented, throw exception directly | Provide degradation solution: cache/default value |
| **Circuit Breaking Duration** | Fixed 60 seconds | Dynamically adjust based on dependency recovery time |
| **Exception Statistics** | All exceptions counted | Only count service exceptions, exclude business exceptions |

**Scenario**: User service call circuit breaking

❌ **Wrong Example**:
```
Improper circuit breaking configuration:
- No circuit breaking rules, only rely on 3-second timeout
- Exception ratio threshold 90%, rarely breaks
- Minimum request count 5, insufficient sampling with high traffic
- No Fallback, error reported directly after breaking
- All exceptions counted, business exceptions cause misjudgment
```

✅ **Correct Example**:
```
Complete circuit breaking configuration:
- Slow call circuit breaking: RT>500ms and ratio>50% triggers
- Exception ratio circuit breaking: Exception rate>30% triggers
- Minimum request count: Set to 50 based on QPS
- Fallback returns user cached data or default info
- Circuit breaking duration 30 seconds, half-open state detection recovery
- Only count RemoteException, exclude business exceptions
- Monitor circuit breaking trigger frequency and recovery time
```

### Example 3: Hot Parameter Rate Limiting

| Dimension | ❌ Wrong Approach | ✅ Correct Approach |
|-----------|------------------|---------------------|
| **Hot Identification** | No hot distinction, uniform limiting | Identify hot parameters for separate limiting |
| **Exception Items** | No exception items configured | Configure stricter limiting for hot data |
| **Parameter Annotation** | Not using @SentinelParam | Clearly annotate hot parameters |
| **Threshold Setting** | Same threshold for hot and normal data | Hot threshold much smaller than normal threshold |
| **Dynamic Adjustment** | Fixed hot data | Dynamically identify hot data based on monitoring |

**Scenario**: Product detail query hot protection

❌ **Wrong Example**:
```
No hot protection:
- All product IDs uniformly limited to 1000 QPS
- Celebrity product access volume 5000 QPS, cache breakthrough
- No hot identification, leads to system collapse
- Cannot dynamically adjust when hot data changes
```

✅ **Correct Example**:
```
Complete hot protection:
- Identify product ID as hot parameter (1st parameter)
- Normal products limited to 1000 QPS
- Celebrity products (ID: 10001, 10002) limited to 100 QPS
- Use @SentinelParam to annotate parameters
- Configure exception item list, dynamically update hot IDs
- Monitor hot access distribution, adjust strategy timely
```

### Example 4: System Protection Configuration

| Dimension | ❌ Wrong Approach | ✅ Correct Approach |
|-----------|------------------|---------------------|
| **Protection Metrics** | Only configure QPS limiting | Multi-dimensional metrics: CPU/Load/RT/Concurrency |
| **CPU Threshold** | Fixed 80% trigger | Dynamically adjust based on application type |
| **Load Threshold** | Don't consider CPU core count | Load threshold = CPU cores * coefficient |
| **Trigger Strategy** | Reject all requests after trigger | Prioritize protecting core interfaces |
| **Recovery Mechanism** | Fixed wait time | Automatically release after metric recovery |

**Scenario**: System-level overload protection

❌ **Wrong Example**:
```
Missing system protection:
- Only configure interface QPS limiting
- CPU spikes to 95% still accepting requests
- System Load exceeds 20 without protection
- Average RT reaches 3 seconds still not limiting
- Thread pool exhaustion causes system hang
```

✅ **Correct Example**:
```
Complete system protection:
- CPU usage exceeds 75% triggers protection
- System Load exceeds cores*0.8 triggers (8 cores=6.4)
- Average RT exceeds 100ms starts limiting
- Concurrent thread count exceeds 500 triggers protection
- Entry QPS limit (as last line of defense)
- Prioritize core interfaces when protection triggered
- Gradually release traffic after metric recovery
```

## Verification Checklist

### Rule Configuration Phase

- [ ] Core interfaces have flow control rules configured
- [ ] External dependencies have circuit breaking degradation rules configured
- [ ] Hot data has hot parameter rate limiting configured
- [ ] System-level protection rules enabled
- [ ] All rules persisted to Nacos
- [ ] Rule thresholds set based on stress test results
- [ ] Reserved 20-30% capacity buffer
- [ ] Distinguished priority of core and non-core interfaces

### Degradation Handling Phase

- [ ] All rate-limited interfaces implement BlockHandler
- [ ] All circuit broken calls implement Fallback
- [ ] Degradation logic has fallback solutions (cache/default value)
- [ ] Returns friendly prompts after degradation
- [ ] Differentiated handling of rate limiting and circuit breaking exceptions
- [ ] Implemented global exception handler
- [ ] Degradation logic tested and verified
- [ ] Degradation minimizes business impact

### Exception Statistics Phase

- [ ] Clearly defined which exceptions count towards circuit breaking statistics
- [ ] Excluded business exceptions (like parameter validation failure)
- [ ] Only count service exceptions (like timeout, connection failure)
- [ ] Use exceptionsToIgnore to exclude specific exceptions
- [ ] Exception classification clear, no misjudgment
- [ ] Regularly review exception statistics configuration

### Monitoring & Alerting Phase

- [ ] Connected to Sentinel Dashboard monitoring
- [ ] Monitor key metrics: QPS/RT/Limiting rate/Circuit breaking rate
- [ ] Configure limiting trigger rate alerts (e.g., >10%)
- [ ] Configure frequent circuit breaking trigger alerts
- [ ] Monitor rule change history
- [ ] Regularly review rule rationality
- [ ] Establish limiting and circuit breaking incident review mechanism
- [ ] Stress test validates rule effectiveness

## Guardrails

### Prohibited Operations

1. **Prohibit not persisting rules to config center**
   - Reason: Rules lost after restart, protection fails
   - Alternative: Persist to Nacos/Apollo

2. **Prohibit not providing friendly prompts after rate limiting**
   - Reason: Poor user experience, unclear situation
   - Alternative: Implement BlockHandler returning clear prompts

3. **Prohibit not providing degradation solution after circuit breaking**
   - Reason: Service completely unavailable, serious business damage
   - Alternative: Implement Fallback returning cache or default value

4. **Prohibit counting all exceptions towards circuit breaking statistics**
   - Reason: Business exceptions cause misjudgment, frequent circuit breaking
   - Alternative: Only count service exceptions, exclude business exceptions

5. **Prohibit not distinguishing core and non-core interfaces**
   - Reason: Core business also limited during resource shortage
   - Alternative: Set different priorities and protection strategies

### MUST Follow

1. **MUST configure rate limiting rules for all public APIs**
   - Reason: Prevent traffic peaks from overwhelming system

2. **MUST configure circuit breaking rules for all external dependencies**
   - Reason: Prevent dependency failures from causing avalanche

3. **MUST implement BlockHandler and Fallback**
   - Reason: Provide friendly degradation experience

4. **MUST persist rules to config center**
   - Reason: Support dynamic adjustment, not lost after restart

5. **MUST monitor limiting and circuit breaking situations**
   - Reason: Timely discover problems, optimize rules

### Performance Red Lines

- Limiting trigger rate not exceeding 5% (normal business)
- Circuit breaker recovery time not exceeding 30 seconds
- Sentinel impact on RT not exceeding 1ms
- Rule count not exceeding 1000
- Single interface rules not exceeding 10
- Dashboard response time not exceeding 100ms

## Common Problem Diagnosis Table

| Problem Phenomenon | Possible Causes | Diagnosis Steps | Solutions |
|-------------------|-----------------|-----------------|-----------|
| Rate limiting rule not effective | Resource name configured incorrectly or rule not loaded | 1. Check @SentinelResource value<br>2. View Dashboard rule list<br>3. Confirm rule persistence configuration | 1. Correct resource name matching<br>2. Check if Nacos config loaded<br>3. Restart app to reload rules |
| Frequent circuit breaking | Threshold too strict or exception statistics improper | 1. Check actual exception rate and RT<br>2. Check exception statistics config<br>3. Analyze business exception ratio | 1. Adjust circuit breaking threshold<br>2. Exclude business exceptions<br>3. Increase minimum request count |
| No degradation after circuit breaking | Fallback method not implemented | 1. Check if fallback configured<br>2. Confirm method signature correct | 1. Implement Fallback method<br>2. Or use FallbackFactory<br>3. Provide degradation data |
| Hot parameter limiting not effective | Parameter not annotated or index wrong | 1. Check @SentinelParam annotation<br>2. Confirm parameter index<br>3. View hot rule config | 1. Add @SentinelParam<br>2. Correct paramIdx<br>3. Verify parameter extraction logic |
| Rule modification not effective | Nacos config not pushed or listening failed | 1. Check if Nacos config updated<br>2. View app logs<br>3. Confirm dataId and group | 1. Confirm Nacos connection normal<br>2. Check data-id config<br>3. Restart app |
| BlockHandler not executed | Method signature mismatch | 1. Check if parameter list consistent<br>2. Confirm BlockException present | 1. Correct method signature<br>2. Add BlockException parameter<br>3. Ensure method public |
| System protection frequently triggered | Threshold set too low or system under pressure | 1. View system actual metrics<br>2. Analyze trigger timing<br>3. Compare historical data | 1. Adjust system protection threshold<br>2. Optimize app performance<br>3. Scale out to add resources |
| Feign call not degraded | FallbackFactory not effective | 1. Confirm Feign config<br>2. Check fallbackFactory<br>3. View Sentinel logs | 1. Configure Feign Sentinel support<br>2. Implement FallbackFactory<br>3. Ensure fallback parameter correct |
| Dashboard connection failed | Port conflict or network unreachable | 1. telnet Dashboard address<br>2. Check transport.port<br>3. View firewall rules | 1. Modify transport.port<br>2. Open port<br>3. Confirm Dashboard address |
| Concurrent thread limiting misjudgment | Thread not correctly released or statistics error | 1. Check if finally block executed<br>2. View thread occupation | 1. Ensure resource correctly released<br>2. Adjust thread count threshold<br>3. Check code for deadlock |

## Output Format Requirements

### Rate Limiting Solution Output Format

```
## Rate Limiting Rule Configuration Solution: [Interface/Service Name]

### Interface Analysis
- Interface Path: [REST path or resource name]
- Business Importance: [Core/Important/General]
- Normal QPS: [Daily traffic]
- Peak QPS: [Peak traffic]
- System Capacity: [Maximum QPS from stress test]

### Rate Limiting Strategy

#### 1. Basic Rate Limiting
- Resource Name: [resource name]
- Limiting Mode: [Direct/Related/Chain]
- Threshold Type: [QPS/Thread Count]
- Threshold Setting: [Specific value] (Basis: [Capacity*0.8 reserve buffer])

#### 2. Flow Control Effect
- Control Behavior: [Fast Fail/Warm Up/Uniform Rate]
- Selection Reason: [Scenario description]
- Parameter Configuration:
  * Warm-up Duration: [N seconds] (if applicable)
  * Queue Timeout: [N milliseconds] (if applicable)

#### 3. Advanced Configuration
- Source Limiting: [Whether to distinguish callers]
- Related Resource: [Related other resources] (if applicable)
- Exception Items: [Whitelist configuration] (if applicable)

### BlockHandler Implementation
- Return Type: [Unified response format]
- Error Code: [HTTP 429]
- Prompt Message: [System busy, please try again later]
- Degradation Logic: [Specific handling method]

### Rule JSON (Nacos Configuration)
Rule structure description:
- resource: [Resource name]
- grade: [1-QPS / 0-Thread count]
- count: [Threshold]
- limitApp: [Source application, default for no limit]
- strategy: [0-Direct / 1-Related / 2-Chain]
- controlBehavior: [0-Fast fail / 1-Warm up / 2-Queue]

### Monitoring & Alerting
- Monitoring Metrics: Limiting trigger count, trigger rate
- Alert Threshold: Trigger rate>5%
- Optimization Suggestions: [Adjust threshold based on monitoring data]
```

### Circuit Breaking Degradation Solution Output Format

```
## Circuit Breaking Degradation Solution: [Service Call]

### Dependency Analysis
- Called Service: [Service name]
- Call Method: [Feign/Dubbo/RestTemplate]
- Importance Level: [Strong dependency/Weak dependency]
- Normal RT: [Average response time]
- Normal Exception Rate: [Normal exception ratio]
- Call QPS: [Call frequency]

### Circuit Breaking Strategy

#### 1. Slow Call Ratio Circuit Breaking
Applicable Scenario: [Unstable response time]
Configuration Parameters:
- Circuit Breaking Strategy: SLOW_REQUEST_RATIO
- RT Threshold: [N milliseconds] (Basis: [Normal RT*1.5])
- Ratio Threshold: [0.5] (50% requests exceed RT threshold)
- Circuit Breaking Duration: [30 seconds]
- Minimum Requests: [50] (Basis: [QPS per second*Statistical duration])
- Statistical Duration: [10 seconds]

#### 2. Exception Ratio Circuit Breaking
Applicable Scenario: [Intermittent service failures]
Configuration Parameters:
- Circuit Breaking Strategy: ERROR_RATIO
- Ratio Threshold: [0.3] (30% requests exception)
- Circuit Breaking Duration: [30 seconds]
- Minimum Requests: [50]

#### 3. Exception Count Circuit Breaking
Applicable Scenario: [Low traffic services]
Configuration Parameters:
- Circuit Breaking Strategy: ERROR_COUNT
- Exception Count Threshold: [10]
- Circuit Breaking Duration: [60 seconds]

### Exception Statistics Configuration
Exceptions counted in statistics:
- RemoteException (Remote call exception)
- TimeoutException (Timeout exception)
- [Other service exceptions]

Excluded exceptions (exceptionsToIgnore):
- IllegalArgumentException (Parameter validation)
- BusinessException (Business exception)
- [Other business exceptions]

### Fallback Implementation

#### 1. Degradation Strategy
Strong Dependency Service:
- Return cached data (Redis/Local cache)
- Timeout: [N milliseconds]

Weak Dependency Service:
- Return default value or empty data
- Log degradation

#### 2. Degradation Logic
Behavior after degradation:
- [Specific degradation handling flow]
- [Impact description on users]
- [Data consistency handling]

### Circuit Breaker Recovery
- Recovery Mode: Half-open state detection
- Detection Request Count: [10]
- Recovery Condition: [Success rate>80%]
- Re-break: [Continue breaking if exception, extend duration]

### Monitoring & Alerting
- Monitoring Metrics: Circuit breaking count, duration, degradation rate
- Alert Condition: Frequent circuit breaking (N times/hour)
- Handling Process: [SOP after alert]
```

### Hot Parameter Limiting Solution Output Format

```
## Hot Parameter Rate Limiting Solution: [Interface Name]

### Hot Analysis
- Business Scenario: [Product details/User info/etc.]
- Hot Parameter: [Parameter name]
- Parameter Position: [Nth parameter]
- Parameter Type: [Long/String/etc.]

### Access Characteristics
- Overall QPS: [Total access volume]
- Hot QPS: [Hot data access volume]
- Hot Ratio: [Percentage]
- Hot Identification: [Top N data IDs]

### Hot Rule Configuration

#### 1. General Rate Limiting
- Resource Name: [resource name]
- Parameter Index: [0-based index]
- Threshold Type: QPS
- General Threshold: [N] (Normal parameter limiting)
- Statistics Window: [1 second]

#### 2. Exception Item Configuration
Hot data special rate limiting:

| Parameter Value | Threshold | Description |
|-----------------|-----------|-------------|
| 10001           | 100       | Celebrity product A |
| 10002           | 150       | Celebrity product B |
| ...             | ...       | ... |

#### 3. Dynamic Adjustment
Hot identification mechanism:
- Monitor top 10 access parameters
- Update exception item list regularly (hourly)
- Automatically adjust hot thresholds

### Parameter Extraction
- Annotation Method: @SentinelParam annotate parameter
- Extraction Location: [Method parameter or object property]
- Type Conversion: [If needed]

### Coordination Strategy
Coordination with general rate limiting:
- Execute hot parameter limiting first
- Then execute interface overall limiting
- Two-layer protection ensures safety

### Monitoring & Optimization
- Monitor hot access distribution
- Identify new hot data
- Dynamically adjust thresholds and exception items
```
