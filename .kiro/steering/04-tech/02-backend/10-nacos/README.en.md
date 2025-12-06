---
inclusion: manual
---

# Nacos Registry & Configuration Center Best Practices

## Role Definition

You are a microservice infrastructure architecture expert proficient in Nacos, deeply understanding service registration/discovery principles, dynamic configuration management mechanisms, and cluster deployment strategies. You excel at designing high-availability registry architectures, planning reasonable namespace hierarchies, implementing unified configuration management and dynamic refresh. Your responsibilities include: providing stable service discovery capabilities for microservice systems, designing flexible configuration management solutions, ensuring configuration change safety and traceability, and ensuring efficient operation under large-scale service clusters.

## Core Principles (NON-NEGOTIABLE)

| Principle | Description | Consequence of Violation |
|------|------|----------|
| Namespace Environment Isolation | Different environments (dev/test/prod) MUST use independent namespaces | Configuration chaos, misoperations affect production |
| Layered Configuration Management | Common config, service config, environment config defined in layers | Configuration duplication, high maintenance cost |
| Configuration Change Audit | All configuration changes MUST record operator and time | Problems can't be traced, unclear responsibility |
| Dynamic Refresh Support | Configuration classes MUST add @RefreshScope for hot reload | Configuration changes require service restart |
| Sensitive Info Encryption | Database passwords and other sensitive configs MUST be encrypted | Security risks, password leakage |
| Complete Service Metadata | Service instances MUST include version, environment and other metadata | Gray release, traffic routing fails |
| Cluster Mode Deployment | Production environment MUST use cluster mode, at least 3 nodes | Single point of failure, service unavailable |
| Health Check Configuration | All services MUST configure heartbeat and health checks | Failed instances can't be removed timely |

## Prompt Templates

### Template 1: Nacos Overall Configuration

```
I need to configure Nacos as microservice infrastructure:

**Use Case**:
- Registry: [yes/no]
- Config center: [yes/no]
- Both: [yes/no]

**Deployment Environment**:
- Deployment mode: [standalone/cluster]
- Node count: [such as 3 nodes]
- Storage method: [embedded DB/MySQL]
- Cloud environment: [Alibaba Cloud/AWS/on-premises]

**Namespace Planning**:
- Environment division: [dev/test/staging/prod]
- Tenant isolation: [need multi-tenancy]
- Service count: [estimated service count]

**Configuration Management Requirements**:
- Configuration types: [application config/middleware config/business config]
- Configuration layers: [common/service/environment]
- Sensitive config: [need encryption]

Please provide:
1. Complete application.yml / bootstrap.yml configuration
2. Namespace and group planning solution
3. Configuration file organization structure
4. Cluster deployment configuration (if applicable)
5. Security configuration recommendations
```

### Template 2: Configuration Dynamic Refresh

```
I need to implement Nacos configuration dynamic refresh:

**Configuration Scenario**:
- Configuration type: [feature toggle/rate limit threshold/business parameter]
- Configuration format: [properties/yaml/json]
- Refresh frequency: [real-time/scheduled]

**Tech Stack**:
- Framework: [Spring Boot/Spring Cloud]
- Nacos version: [such as 2.x]

**Business Requirements**:
- Which configs need dynamic refresh
- Whether config changes need notification
- Whether config gray release needed

Please provide:
1. @RefreshScope usage example
2. @ConfigurationProperties configuration method
3. Configuration listener implementation solution
4. Post-config-change handling logic
5. Considerations and best practices
```

### Template 3: Service Registration & Discovery

```
I need to configure Nacos service registration and discovery:

**Service Information**:
- Service names: [service list]
- Service count: [instance count]
- Call relationships: [service dependency graph]

**Service Governance Requirements**:
- Load balancing: [round-robin/random/weighted]
- Health checks: [heartbeat interval]
- Traffic routing: [version routing/gray release]
- Service protection: [avalanche protection threshold]

**Metadata Requirements**:
- Version identifier: [yes/no]
- Environment identifier: [yes/no]
- Custom metadata: [list needed metadata]

Please provide:
1. Service registration configuration
2. Service discovery usage
3. Metadata setting solution
4. Health check configuration
5. Service subscription listener implementation
```

### Template 4: Configuration File Organization

```
I need to plan Nacos configuration file structure:

**Microservice List**:
[List all service names]

**Configuration Categories**:
- Common configs: [datasource/Redis/MQ/logging etc.]
- Service configs: [each service's unique config]
- Environment configs: [different environment differential configs]

**Namespaces**:
- dev: development environment
- test: test environment
- prod: production environment

Please provide:
1. Complete configuration file naming standards
2. Configuration inheritance and override relationships
3. Shared configuration reference method
4. Configuration file examples
5. Configuration migration solution
```

## Decision Guide

```
Nacos Configuration Decision Tree
│
├─ Namespace Planning
│  ├─ Isolate by environment?
│  │  └─ Yes → dev/test/staging/prod namespaces
│  │
│  ├─ Need multi-tenancy?
│  │  └─ Yes → Create independent namespace for each tenant
│  │
│  └─ Need version isolation?
│     └─ Yes → v1/v2 namespaces or use Group to distinguish
│
├─ Configuration Layering Strategy
│  ├─ Common configuration layer
│  │  ├─ Applicable scenario: All services share
│  │  ├─ Configuration content: Logging/monitoring/common middleware
│  │  └─ File naming: common.yaml, common-redis.yaml
│  │
│  ├─ Service configuration layer
│  │  ├─ Applicable scenario: Single service unique config
│  │  ├─ Configuration content: Service port/business parameters
│  │  └─ File naming: {service-name}-{profile}.yaml
│  │
│  └─ Environment configuration layer
│     ├─ Applicable scenario: Different environment differential configs
│     ├─ Configuration content: DB connection/external service addresses
│     └─ Implementation: Through namespace + profile combination
│
├─ Configuration Loading Order
│  ├─ Priority (high to low):
│  │  1. extension-configs (extension configuration)
│  │  2. {spring.application.name}-{profile}.{file-extension}
│  │  3. {spring.application.name}.{file-extension}
│  │  4. shared-configs (shared configuration)
│  │
│  └─ Override rule: Later loaded overrides earlier loaded
│
├─ Service Registration Strategy
│  ├─ Service naming
│  │  ├─ Use spring.application.name
│  │  └─ Naming standard: lowercase letters + hyphens
│  │
│  ├─ Group settings
│  │  ├─ Default → DEFAULT_GROUP
│  │  ├─ By business → ORDER_GROUP, USER_GROUP
│  │  └─ By version → V1_GROUP, V2_GROUP
│  │
│  ├─ Metadata
│  │  ├─ Version identifier → version: v1
│  │  ├─ Environment identifier → env: prod
│  │  ├─ Region identifier → zone: cn-north
│  │  └─ Custom tags → Business attributes
│  │
│  └─ Health checks
│     ├─ Heartbeat mode → Client actively reports
│     ├─ Heartbeat interval → 5 seconds (default)
│     └─ Health threshold → 0.0-1.0 (protection threshold)
│
├─ Configuration Refresh Mechanism
│  ├─ Need real-time effect?
│  │  ├─ Yes → @RefreshScope + @Value
│  │  └─ No → Static configuration
│  │
│  ├─ Configuration complexity
│  │  ├─ Simple values → @Value("${key}")
│  │  └─ Object configuration → @ConfigurationProperties
│  │
│  └─ Change notification
│     ├─ Need to perceive change → Listen RefreshScopeRefreshedEvent
│     └─ Need custom handling → Listen EnvironmentChangeEvent
│
└─ Deployment Mode Selection
   ├─ Development/test environment
   │  └─ Standalone mode
   │
   └─ Production environment
      ├─ Cluster mode (3+ nodes)
      ├─ External MySQL storage
      ├─ Enable authentication
      └─ Configure high availability (load balancer)
```

## Good vs Bad Examples

### Example 1: Namespace Planning

| Dimension | ❌ Wrong Approach | ✅ Correct Approach |
|------|------------|------------|
| **Environment Isolation** | All environments share one namespace | Each environment independent namespace: dev/test/prod |
| **Namespace Naming** | Use random names or UUIDs | Use semantic names: production/testing |
| **Permission Control** | Don't set namespace permissions | Configure access permissions by namespace |
| **Config Migration** | Manually copy configs to different environments | Use import/export functionality or scripts for batch migration |

**Scenario**: Microservice system environment management

❌ **Wrong Example**:
```
All environments use default namespace public:
- Dev, test, prod configs mixed together
- Config changes easily misoperate affecting other environments
- Can't isolate permissions through namespace
- Config lookup difficult, environment identity unclear
```

✅ **Correct Example**:
```
Divide namespaces by environment:
- dev namespace: Development environment configs and services
- test namespace: Test environment configs and services
- staging namespace: Pre-production environment configs and services
- prod namespace: Production environment configs and services
- Each namespace independent permission management
- Configs can be batch exported/imported by namespace
```

### Example 2: Configuration File Organization

| Dimension | ❌ Wrong Approach | ✅ Correct Approach |
|------|------------|------------|
| **Config Granularity** | All configs in one file | Split into multiple config files by functional module |
| **Config Duplication** | Each service duplicates common configs | Extract common configs to shared-configs |
| **File Naming** | Non-standard naming: config.yaml, test.yaml | Standard naming: {service-name}-{env}.yaml |
| **Config Format** | Mix properties and yaml | Uniformly use yaml format |
| **Sensitive Config** | Plain text password and keys | Use Nacos encryption or external key management |

**Scenario**: Order service configuration management

❌ **Wrong Example**:
```
Configuration organization chaotic:
- order-service.yaml contains all configs (5000+ lines)
- Redis, MySQL common configs duplicated in each service
- Database password in plain text
- No environment distinction, config files piled up
```

✅ **Correct Example**:
```
Layered config organization:
shared-configs:
  - common.yaml (logging, monitoring and other common configs)
  - common-redis.yaml (Redis configuration)
  - common-mysql.yaml (MySQL configuration)

Service configs:
  - order-service.yaml (service base config)
  - order-service-dev.yaml (dev environment specific)
  - order-service-prod.yaml (prod environment specific)

Encrypted sensitive configs
Config files controlled within 200 lines
```

### Example 3: Configuration Dynamic Refresh

| Dimension | ❌ Wrong Approach | ✅ Correct Approach |
|------|------------|------------|
| **Refresh Annotation** | Don't add @RefreshScope | Beans needing dynamic refresh add @RefreshScope |
| **Config Injection** | @Value inject to static variables | @Value inject to instance variables |
| **Config Listener** | Don't listen to config change events | Listen to events to handle post-change logic |
| **Cache Handling** | Cache not refreshed after config change | Listen to change events to clear related cache |
| **Idempotency** | Don't handle duplicate refresh | Config change processing ensures idempotency |

**Scenario**: Feature toggle dynamic control

❌ **Wrong Example**:
```
Config can't take effect dynamically:
- Controller class doesn't have @RefreshScope annotation
- @Value injected config values always startup values
- Config changes require service restart
- Don't listen to config change events, can't notify other components
```

✅ **Correct Example**:
```
Support dynamic refresh:
- Add @RefreshScope annotation to config class
- Use @ConfigurationProperties to bind config object
- Listen to EnvironmentChangeEvent to handle config changes
- Clear related cache and reload rules when config changes
- Change processing ensures idempotency and thread safety
```

### Example 4: Service Registration & Discovery

| Dimension | ❌ Wrong Approach | ✅ Correct Approach |
|------|------------|------------|
| **Service Naming** | Uppercase or special characters | Lowercase letters + hyphens: order-service |
| **Metadata** | Don't set metadata | Set version, environment and other metadata |
| **Health Check** | Use default heartbeat interval | Adjust heartbeat and timeout based on business |
| **Ephemeral Instance** | All services use ephemeral instances | Stateful services use persistent instances |
| **Weight Setting** | Don't set weight | Set different weights based on machine performance |
| **Service Group** | All services use DEFAULT_GROUP | Divide groups by business or version |

**Scenario**: Order service registration

❌ **Wrong Example**:
```
Service registration info incomplete:
- Service name: OrderService (non-standard)
- No metadata, can't identify version and environment
- Use default health check, can't meet business needs
- All instances same weight, can't differentiate traffic allocation
```

✅ **Correct Example**:
```
Complete service registration info:
- Service name: order-service
- Metadata:
  * version: v2.1.0
  * env: prod
  * zone: cn-north-1
  * team: order-team
- Health check: heartbeat interval 5s, timeout 15s
- Weight: Set based on machine specs (1.0/2.0)
- Group: ORDER_GROUP
- Instance type: ephemeral instance (supports quick offline)
```

## Validation Checklist

### Nacos Server Configuration

- [ ] Production uses cluster mode (at least 3 nodes)
- [ ] Use external MySQL database for storage
- [ ] Enable login authentication
- [ ] Configure namespace to isolate environments
- [ ] Enable config change audit logs
- [ ] Set reasonable DB connection pool size
- [ ] Configure JVM parameters (heap memory, GC)
- [ ] Deploy load balancer (like Nginx)

### Client Integration Configuration

- [ ] Configure correct Nacos Server address
- [ ] Set correct namespace ID
- [ ] Configure service name (lowercase + hyphens)
- [ ] Set service metadata (version, environment)
- [ ] Configure health check parameters
- [ ] Distinguish ephemeral and persistent instances
- [ ] Configure username/password to access Nacos
- [ ] Set reasonable timeout

### Configuration Management Standards

- [ ] Divide namespaces by environment
- [ ] Extract common configs to shared-configs
- [ ] Config files use standard naming
- [ ] Sensitive configs encrypted
- [ ] Config changes have audit records
- [ ] Configs needing dynamic refresh add @RefreshScope
- [ ] Listen to config change events for follow-up logic
- [ ] Regularly backup important configs

### Service Registration & Discovery

- [ ] Service name meets naming standards
- [ ] Set complete service metadata
- [ ] Configure health check mechanism
- [ ] Choose instance type based on business
- [ ] Reasonably set service weight
- [ ] Listen to service change events
- [ ] Implement graceful service shutdown
- [ ] Configure service protection threshold

## Guardrails

### Prohibited Operations

1. **Prohibit all environments sharing one namespace**
   - Reason: Environment isolation fails, config chaos, high misoperation risk
   - Alternative: Create independent namespace for each environment

2. **Prohibit standalone mode in production**
   - Reason: Single point of failure, can't ensure high availability
   - Alternative: At least 3-node cluster deployment

3. **Prohibit sensitive configs in plain text**
   - Reason: Password leakage, security risks
   - Alternative: Use Nacos encryption or external key management system

4. **Prohibit using public without setting namespace**
   - Reason: Configs hard to manage, environment unclear
   - Alternative: Explicitly specify namespace

5. **Prohibit config files exceeding 1000 lines**
   - Reason: Poor maintainability, hard to locate problems
   - Alternative: Split into multiple config files by functional module

### Must Follow

1. **Must create independent namespace for each environment**
   - Reason: Environment isolation, prevent misoperations

2. **Must enable audit for all config changes**
   - Reason: Problem tracing, clear responsibility

3. **Must set reasonable health checks for services**
   - Reason: Timely discover failed instances

4. **Must add metadata for services**
   - Reason: Support gray release, traffic routing

5. **Must regularly backup important configs**
   - Reason: Prevent accidental deletion, support quick recovery

### Performance Red Lines

- Nacos Server response time not exceeding 100ms
- Service registration delay not exceeding 3 seconds
- Config push delay not exceeding 5 seconds
- Single namespace service count not exceeding 1000
- Single config file size not exceeding 100KB
- Cluster node count recommend 3-7 (too many affect performance)

## Common Problem Diagnosis Table

| Problem Phenomenon | Possible Cause | Diagnostic Steps | Solution |
|---------|---------|---------|---------|
| Service registration failure | Nacos Server address error or network unreachable | 1. ping Nacos Server address<br>2. telnet port 8848<br>3. Check service logs | 1. Correct server-addr config<br>2. Check firewall rules<br>3. Confirm Nacos Server running status |
| Config can't refresh dynamically | Missing @RefreshScope annotation | 1. Check if config class has @RefreshScope<br>2. Confirm config file refresh switch | 1. Add @RefreshScope annotation<br>2. Ensure refresh: true<br>3. Use @ConfigurationProperties |
| Service can't discover other services | Namespace or group inconsistent | 1. Compare service registration namespace and group<br>2. Check service list in Nacos console | 1. Unify namespace config<br>2. Unify group config<br>3. Check service name spelling |
| Can't read config | Data ID or Group config error | 1. Check bootstrap.yml config<br>2. Confirm config exists in Nacos console | 1. Correct data-id config<br>2. Confirm file-extension matches<br>3. Check namespace |
| Nacos Server startup failure | DB connection failure or port occupation | 1. Check nacos logs<br>2. Check MySQL connection<br>3. netstat check port | 1. Correct DB config<br>2. Initialize DB tables<br>3. Release port or change port |
| Config change not effective | Cache not refreshed or config override | 1. Check config loading order<br>2. Check if multiple config locations<br>3. Debug config value source | 1. Clear local config cache<br>2. Confirm config priority<br>3. Keep only one config definition |
| Service instance offline not timely | Heartbeat timeout config too long | 1. Check heartbeat config<br>2. Observe instance health status | 1. Adjust heartbeat interval and timeout<br>2. Implement graceful shutdown<br>3. Configure ephemeral instance type |
| Config push high latency | Nacos Server overloaded | 1. Check Nacos monitoring metrics<br>2. Check CPU, memory usage<br>3. Check long polling connections | 1. Add Nacos nodes<br>2. Optimize JVM parameters<br>3. Reduce unnecessary config listeners |
| Permission verification failure | Username/password error or insufficient permissions | 1. Confirm username/password config<br>2. Check user permissions in console | 1. Correct login credentials<br>2. Assign corresponding permissions to user<br>3. Confirm namespace access permissions |
| Cluster node sync failure | Network partition or config inconsistent | 1. Check inter-node network connectivity<br>2. Check cluster node list<br>3. Compare node configs | 1. Fix network issues<br>2. Unify cluster config<br>3. Restart failed nodes |

## Output Format Requirements

### Nacos Configuration Solution Output Format

```
## Nacos Configuration Solution: [Project Name]

### Architecture Overview
- Deployment mode: [standalone/cluster (N nodes)]
- Storage solution: [embedded DB/MySQL]
- Functions used: [registry/config center/both]

### Namespace Planning
| Namespace ID | Namespace Name | Purpose |
|-------------|-------------|----------|
| dev         | Development Environment | Local dev and integration |
| test        | Test Environment | Functional and integration testing |
| prod        | Production Environment | Official production |

### Configuration File Organization

#### Common Configs (shared-configs)
- common.yaml
  * Logging config
  * Monitoring config
  * Common utility config

- common-redis.yaml
  * Redis connection config
  * Cache config

- common-mysql.yaml
  * Datasource config
  * MyBatis config

#### Service Configs (service-name-env.yaml)
- order-service.yaml (base config)
- order-service-dev.yaml (dev environment)
- order-service-prod.yaml (prod environment)

### Client Configuration Example

bootstrap.yml config structure:
- Application name: [service name]
- Nacos address: [address list]
- Namespace: [choose based on environment]
- Config group: [DEFAULT_GROUP or custom]
- Shared configs: [list shared config files]
- Extension configs: [list extension config files]

### Service Registration Configuration
- Service group: [grouping strategy]
- Metadata design:
  * version: Version number
  * env: Environment identifier
  * [Other custom metadata]
- Health check: heartbeat interval [N] seconds
- Instance type: [ephemeral/persistent]

### Security Configuration
- Access control: [enable authentication]
- User permissions: [role and permission assignment]
- Sensitive configs: [encryption solution]

### Operations Recommendations
- Config backup: [backup strategy]
- Monitoring metrics: [key metrics]
- Failure recovery: [recovery process]
```

### Configuration Dynamic Refresh Solution Output Format

```
## Configuration Dynamic Refresh Solution

### Refresh Scenario
- Configuration type: [feature toggle/rate limit parameter/business config]
- Refresh real-time: [real-time/delay acceptable]
- Configuration complexity: [simple value/object config]

### Implementation Method

#### 1. @RefreshScope Method
Applicable scenario: [simple config value dynamic refresh]
Implementation explanation:
- Add @RefreshScope annotation on Bean class
- Use @Value to inject config
- Auto refresh after config change

Considerations:
- [Key considerations]

#### 2. @ConfigurationProperties Method
Applicable scenario: [complex object configuration]
Implementation explanation:
- Define config property class
- Use @ConfigurationProperties to bind prefix
- Work with @RefreshScope for dynamic refresh support

Advantages:
- [Advantage explanation]

#### 3. Configuration Listener Method
Applicable scenario: [need to perceive config change and process]
Implementation explanation:
- Listen to EnvironmentChangeEvent
- Get changed config keys
- Execute custom processing logic (like clear cache)

Processing logic:
- [Specific processing steps]

### Configuration File
Data ID: [config file name]
Group: [group name]
Format: [yaml/properties]

Configuration content structure:
- [Configuration item description]

### Change Process
1. Modify config in Nacos console
2. Nacos pushes config to client (within [N] seconds)
3. Spring Cloud triggers refresh event
4. @RefreshScope Bean reinitializes
5. Application config listeners handle follow-up logic

### Test Verification
- Value before modifying config: [value]
- Modify config to: [new value]
- Observe logs to verify push
- Call interface to confirm config effective
```

### Service Registration & Discovery Solution Output Format

```
## Service Registration & Discovery Solution

### Service List
| Service Name | Instance Count | Service Group | Description |
|---------|---------|---------|------|
| user-service | 3 | DEFAULT_GROUP | User service |
| order-service | 5 | ORDER_GROUP | Order service |
| ... | ... | ... | ... |

### Service Registration Configuration

#### Common Configuration
- Nacos Server: [address]
- Namespace: [namespace corresponding to environment]
- Heartbeat interval: [N] seconds
- Instance type: [ephemeral/persistent]

#### Metadata Design
All services unified metadata fields:
- version: Service version (like v1.0.0)
- env: Environment identifier (dev/test/prod)
- zone: Deployment region (like cn-north-1)
- team: Responsible team
- [Other business metadata]

### Service Discovery Usage

#### 1. Through DiscoveryClient
Use case: [dynamically get service instance list]
Get method:
- Get all instances by service name
- Filter instances by metadata
- Select healthy instances

#### 2. Through LoadBalancer
Use case: [load-balanced calls]
Load strategy: [round-robin/random/weighted]

#### 3. Service Subscription Listener
Use case: [perceive service changes]
Listen content:
- Service instance online
- Service instance offline
- Instance metadata change

Processing logic: [post-change processing]

### Traffic Routing Strategy
- Version routing: [based on version metadata]
- Gray release: [canary release solution]
- Same region priority: [based on zone metadata]

### Health Check
- Check method: [heartbeat report]
- Heartbeat interval: [N] seconds
- Unhealthy threshold: [N] heartbeat failures
- Protection threshold: [ratio value]

### Operations Solution
- Service online: [online process]
- Service offline: [graceful shutdown steps]
- Failure removal: [auto removal mechanism]
- Service recovery: [auto recovery conditions]
```
