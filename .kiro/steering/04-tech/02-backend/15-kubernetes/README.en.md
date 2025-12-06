---
inclusion: manual
---

# Kubernetes Container Orchestration Best Practices

## Role Definition

You are a cloud-native architecture expert proficient in Kubernetes, specializing in container orchestration, service governance, resource scheduling, and cluster operations. You can design highly available and scalable Kubernetes deployment solutions and optimize cluster performance and resource utilization.

## Core Principles (NON-NEGOTIABLE)

| Principle | Description | Consequences of Violation |
|------|------|----------|
| Resource Limits | All Pods must set CPU and memory requests and limits | Resource competition causes avalanche effect, node instability |
| Health Checks | Configure liveness, readiness, and startup probes to ensure service availability | Faulty Pods cannot auto-recover, traffic hits unready Pods |
| High Availability Deployment | Production environments require at least 3 replicas, configure PodDisruptionBudget | Single point of failure causes service interruption |
| Label Standards | Use unified labeling strategy (app, version, environment, etc.) | Cannot accurately select and manage resources |
| Configuration Separation | Use ConfigMap and Secret to manage configurations, prohibit hardcoding | Config changes require image rebuild, sensitive info leakage |
| Rolling Updates | Use rolling update strategy to avoid service interruption | Updates cause entire site outage |

## Prompt Templates

### K8s Deployment Solution Design

```
Please help me design Kubernetes deployment solution:

[Application Basic Info]
- Application Type: [Stateless service/Stateful service/Job task/CronJob scheduled task]
- Programming Language: [Java/Node.js/Python/Go/Other]
- Container Image: [Registry address and tag]
- Expected Replicas: [3/5/10]

[Resource Requirements]
- CPU Requirements: requests [500m] / limits [2000m]
- Memory Requirements: requests [512Mi] / limits [2Gi]
- Need Persistent Storage: [Yes/No]
  - Storage Type: [Database/Files/Logs]
  - Storage Size: [100Gi]
  - Access Mode: [ReadWriteOnce/ReadWriteMany]

[Network Requirements]
- Service Type: [ClusterIP/NodePort/LoadBalancer]
- Service Port: [8080]
- Need Ingress: [Yes/No]
  - Domain: [api.example.com]
  - TLS Certificate: [Yes/No]
  - Path Rewrite: [Yes/No]

[Deployment Strategy]
- Update Strategy: [Rolling update/Blue-green deployment/Canary release]
- maxSurge: [1]
- maxUnavailable: [0]
- Rollback History Count: [5]

[High Availability Config]
- Configure HPA: [Yes/No]
  - Min Replicas: [3]
  - Max Replicas: [10]
  - Scaling Metrics: [CPU/Memory/Custom metrics]
- Configure PDB: [Yes/No]
  - minAvailable: [2]

[Environment and Dependencies]
- Environment Label: [dev/test/prod]
- Dependent Services: [MySQL/Redis/Kafka/etc.]
- Initialization Requirements: [Need initContainers or not]

Please provide complete YAML configuration manifests.
```

### Stateful Application Deployment

```
Please help me deploy stateful application (StatefulSet):

[Application Info]
- Application Type: [MySQL cluster/Redis cluster/Kafka/Elasticsearch/Other]
- Cluster Scale: [3/5/7 nodes]
- Need Master-Slave Structure: [Yes/No]

[Storage Requirements]
- StorageClass Name: [standard/fast-ssd]
- Storage Size per Node: [100Gi/500Gi/1Ti]
- Storage Expansion Strategy: [Static allocation/Dynamic expansion]

[Network Requirements]
- Headless Service: [Yes/No]
- External Access: [Yes/No]
  - Access Method: [NodePort/LoadBalancer]

[High Availability Config]
- Data Replication Strategy: [Sync/Async]
- Automatic Failover: [Yes/No]
- Backup Strategy: [Scheduled backup/Real-time backup]

[Initialization Config]
- Config Files: [List needed configs]
- Init Scripts: [Need or not]
- Cluster Auto-Discovery: [Yes/No]

Please provide complete StatefulSet, Service, PVC configurations.
```

### Service Exposure Solution

```
Please help me design service exposure solution:

[Service Info]
- Service Name: [order-service]
- Service Port: [8080]
- Protocol: [HTTP/gRPC/TCP/UDP]

[Access Requirements]
- Access Scope: [Cluster internal/Cluster external/Public internet]
- Access Domain: [api.example.com]
- Need HTTPS: [Yes/No]
- TLS Certificate Source: [cert-manager auto-issue/Manual upload]

[Traffic Control]
- Load Balancing Strategy: [Round-robin/Session persistence]
- Rate Limiting Requirements: [100 req/s]
- Timeout Settings: [60s]
- Retry Strategy: [Yes/No]

[Security Requirements]
- Need CORS: [Yes/No]
- IP Whitelist: [Yes/No]
- Authentication Method: [None/Basic Auth/OAuth/JWT]

[Path Routing]
- URL Rewrite: [Yes/No]
- Path Prefix: [/api/v1]
- Multi-Service Routing: [Yes/No]

Please provide complete Service, Ingress configurations.
```

### Auto-Scaling Configuration

```
Please help me configure Pod auto-scaling (HPA):

[Application Characteristics]
- Application Name: [order-service]
- Current Replicas: [3]
- Business Peak Hours: [Weekdays 9:00-21:00]
- Traffic Pattern: [Stable/Volatile/Burst]

[Scaling Config]
- Min Replicas: [3]
- Max Replicas: [10]
- Scaling Metrics:
  - Target CPU Utilization: [70%]
  - Target Memory Utilization: [80%]
  - Custom Metrics: [HTTP request count/Queue length/Other]

[Scaling Strategy]
- Scale-up Strategy:
  - Scale-up Rate: [Increase X%/X Pods per time]
  - Scale-up Cooldown: [60s]
- Scale-down Strategy:
  - Scale-down Rate: [Decrease X%/X Pods per time]
  - Scale-down Cooldown: [300s]
  - Stabilization Window: [5 minutes]

[Special Requirements]
- Time-based Scaling: [Yes/No]
- Need Prometheus Metrics Integration: [Yes/No]

Please provide HPA configuration and optimization recommendations.
```

## Decision Guide (Tree Structure)

```
Workload Type Selection
├── Deployment (Stateless Applications)
│   ├── Features: Pods can be freely replaced, stateless
│   ├── Use Cases:
│   │   ├── Web applications (Spring Boot, Express, Flask)
│   │   ├── API services
│   │   ├── Microservice backends
│   │   └── Frontend static resource services
│   └── Feature Support:
│       ├── Rolling updates
│       ├── Horizontal scaling
│       ├── Automatic failure recovery
│       └── Version rollback
├── StatefulSet (Stateful Applications)
│   ├── Features: Pods have fixed identities, ordered deployment
│   ├── Use Cases:
│   │   ├── Database clusters (MySQL, PostgreSQL, MongoDB)
│   │   ├── Message queues (Kafka, RabbitMQ)
│   │   ├── Distributed storage (Elasticsearch, Cassandra)
│   │   └── Cache clusters (Redis Sentinel/Cluster)
│   └── Feature Support:
│       ├── Stable network identity (hostname)
│       ├── Ordered deployment and scaling
│       ├── Persistent storage binding
│       └── Ordered rolling updates
├── DaemonSet (Node Daemon Processes)
│   ├── Features: Run one Pod per node
│   ├── Use Cases:
│   │   ├── Log collection (Fluentd, Filebeat)
│   │   ├── Monitoring agents (Node Exporter, cAdvisor)
│   │   ├── Network plugins (Calico, Flannel)
│   │   └── Storage plugins (Ceph, Gluster)
│   └── Feature Support:
│       ├── Auto-schedule to all nodes
│       ├── Auto-deploy when nodes added
│       └── Support node selectors
├── Job (One-time Tasks)
│   ├── Features: Terminates after task completion
│   ├── Use Cases:
│   │   ├── Data import/export
│   │   ├── Batch processing tasks
│   │   ├── Database migration
│   │   └── One-time script execution
│   └── Configuration Options:
│       ├── Parallelism control
│       ├── Completion count requirement
│       ├── Failure retry strategy
│       └── Timeout limit
└── CronJob (Scheduled Tasks)
    ├── Features: Execute periodically by schedule
    ├── Use Cases:
    │   ├── Scheduled backups
    │   ├── Periodic data cleanup
    │   ├── Scheduled report generation
    │   └── Health check tasks
    └── Configuration Options:
        ├── Cron time expression
        ├── Concurrency Policy (Allow/Forbid/Replace)
        ├── History Record Retention
        └── Task Timeout Settings

Service Type Selection
├── ClusterIP (Cluster Internal Access)
│   ├── Features: Accessible within cluster only
│   ├── Use Cases:
│   │   ├── Inter-microservice communication
│   │   ├── Database access
│   │   ├── Cache access
│   │   └── Internal API calls
│   ├── Access Methods:
│   │   ├── Service name: service-name.namespace.svc.cluster.local
│   │   ├── Short name: service-name (same namespace)
│   │   └── ClusterIP: Virtual IP address
│   └── Use Case: 99% of internal services
├── NodePort (Node Port Access)
│   ├── Features: Opens fixed port on all nodes
│   ├── Use Cases:
│   │   ├── Dev/test environments
│   │   ├── Temporary external access
│   │   ├── Non-cloud environment deployment
│   │   └── Services requiring specific ports
│   ├── Port Range: 30000-32767
│   ├── Access Method: <NodeIP>:<NodePort>
│   └── Cons:
│       ├── Port range limitation
│       ├── Need to remember port numbers
│       └── No load balancing feature
├── LoadBalancer (Cloud Load Balancer)
│   ├── Features: Cloud provider provides external load balancer
│   ├── Use Cases:
│   │   ├── Production external access
│   │   ├── Cloud platform deployment (AWS/Azure/GCP/Alibaba Cloud)
│   │   ├── Services needing public IP
│   │   └── TCP/UDP protocol service exposure
│   ├── Access Method: Cloud-assigned external IP
│   ├── Pros:
│   │   ├── Auto-configure load balancing
│   │   ├── Health check integration
│   │   └── High availability guarantee
│   └── Cons:
│       ├── One LB per service (high cost)
│       └── Only cloud platform support
└── Headless Service (Headless Service)
    ├── Features: No ClusterIP assigned, returns Pod IPs directly
    ├── Use Cases:
    │   ├── StatefulSet service discovery
    │   ├── Database master-slave clusters
    │   ├── Scenarios requiring direct Pod connection
    │   └── Custom load balancing
    └── Access Method: DNS returns all Pod IP list

Ingress Controller Selection
├── Nginx Ingress (Recommended)
│   ├── Maturity: ★★★★★
│   ├── Community Support: Active
│   ├── Features:
│   │   ├── Flexible routing rules
│   │   ├── TLS/SSL termination
│   │   ├── Basic authentication and OAuth
│   │   ├── Rate limiting and blacklist/whitelist
│   │   ├── URL rewrite and redirect
│   │   └── WebSocket support
│   ├── Performance: High (C/C++ implementation)
│   └── Use Case: General scenario preferred
├── Traefik
│   ├── Maturity: ★★★★☆
│   ├── Features: Cloud-native, auto service discovery
│   ├── Functions:
│   │   ├── Auto HTTPS (Let's Encrypt)
│   │   ├── Dynamic config updates
│   │   ├── Rich middleware
│   │   ├── Multi-protocol support (HTTP/TCP/UDP)
│   │   └── Visual Dashboard
│   ├── Performance: Medium (Go implementation)
│   └── Use Case: Scenarios needing dynamic config
├── Istio Gateway
│   ├── Maturity: ★★★★☆
│   ├── Features: Service mesh integration
│   ├── Functions:
│   │   ├── Traffic management and routing
│   │   ├── Circuit breaking and retry
│   │   ├── Traffic mirroring
│   │   ├── A/B testing and canary release
│   │   └── Fine-grained observability
│   ├── Performance: Medium (Envoy proxy)
│   └── Use Case: Microservices mesh scenarios
└── Kong Ingress
    ├── Maturity: ★★★★☆
    ├── Features: API gateway functionality
    ├── Functions:
    │   ├── Authentication and authorization
    │   ├── Rate limiting and quotas
    │   ├── Request/response transformation
    │   ├── Logging and monitoring
    │   └── Rich plugin ecosystem
    ├── Performance: High (Nginx + Lua)
    └── Use Case: API gateway scenarios

Storage Solution Selection
├── EmptyDir (Temporary Storage)
│   ├── Lifecycle: Data lost after Pod deletion
│   ├── Use Cases:
│   │   ├── Temporary data processing
│   │   ├── Inter-container data sharing
│   │   ├── Cache directories
│   │   └── Temporary log storage
│   ├── Storage Media:
│   │   ├── Memory: emptyDir.medium: Memory
│   │   └── Disk: Default uses node disk
│   └── Note: Not suitable for persistent data
├── HostPath (Node Path Mount)
│   ├── Features: Mount node filesystem path
│   ├── Use Cases:
│   │   ├── Access node system files (/proc, /sys)
│   │   ├── DaemonSet persistent data
│   │   ├── Local test environment
│   │   └── Log collection (mount /var/log)
│   ├── Risks:
│   │   ├── Security risk (can access host)
│   │   ├── Pod scheduled to different node loses data
│   │   └── Node failure causes data loss
│   └── Limitation: Use cautiously in production
├── PersistentVolume (Persistent Volume)
│   ├── Features: Independent of Pod lifecycle
│   ├── Access Modes:
│   │   ├── ReadWriteOnce (RWO): Single node read-write
│   │   ├── ReadOnlyMany (ROX): Multi-node read-only
│   │   └── ReadWriteMany (RWX): Multi-node read-write
│   ├── Storage Types:
│   │   ├── Local Storage (Local PV)
│   │   │   ├── Performance: Highest (local SSD)
│   │   │   ├── Availability: Low (node failure loses data)
│   │   │   └── Use Case: High-performance databases
│   │   ├── Network Storage (NFS/Ceph/GlusterFS)
│   │   │   ├── Performance: Medium
│   │   │   ├── Availability: High (cross-node)
│   │   │   └── Use Case: Shared file storage
│   │   └── Cloud Storage (EBS/Azure Disk/Cloud Disk)
│   │       ├── Performance: High (SSD)
│   │       ├── Availability: High (cloud provider guarantee)
│   │       ├── Cost: Higher
│   │       └── Use Case: Cloud platform deployment
│   └── Reclaim Policy:
│       ├── Retain: Keep data, manual cleanup
│       ├── Delete: Auto-delete
│       └── Recycle: Clear data then reuse (deprecated)
└── StorageClass (Dynamic Storage)
    ├── Features: Auto-create PV
    ├── Use Cases:
    │   ├── Production environment recommended method
    │   ├── Simplify storage management
    │   └── On-demand storage allocation
    └── Parameter Config:
        ├── Storage type (SSD/HDD)
        ├── IOPS configuration
        ├── Replica count
        └── Expansion strategy

Resource Quota Strategy
├── Dev Environment
│   ├── Requests: Low (guarantee basic running)
│   │   ├── CPU: 100m-250m
│   │   └── Memory: 128Mi-256Mi
│   ├── Limits: Loose (convenient debugging)
│   │   ├── CPU: 1000m-2000m
│   │   └── Memory: 512Mi-1Gi
│   └── Replicas: 1-2
├── Test Environment
│   ├── Requests: Medium (simulate production)
│   │   ├── CPU: 250m-500m
│   │   └── Memory: 256Mi-512Mi
│   ├── Limits: Moderate
│   │   ├── CPU: 1000m-2000m
│   │   └── Memory: 1Gi-2Gi
│   └── Replicas: 2-3
└── Production Environment
    ├── Lightweight Services
    │   ├── Requests:
    │   │   ├── CPU: 500m
    │   │   └── Memory: 512Mi
    │   ├── Limits:
    │   │   ├── CPU: 2000m
    │   │   └── Memory: 2Gi
    │   └── Replicas: 3-5
    ├── Standard Services
    │   ├── Requests:
    │   │   ├── CPU: 1000m
    │   │   └── Memory: 1Gi
    │   ├── Limits:
    │   │   ├── CPU: 4000m
    │   │   └── Memory: 4Gi
    │   └── Replicas: 3-10
    └── Resource-Intensive Services
        ├── Requests:
        │   ├── CPU: 2000m
        │   └── Memory: 4Gi
        ├── Limits:
        │   ├── CPU: 8000m
        │   └── Memory: 16Gi
        └── Replicas: 3-5

HPA Scaling Strategy
├── CPU-based Metrics
│   ├── Target Utilization: 60-80%
│   ├── Pros: Simple, stable
│   ├── Cons: Response lag
│   └── Use Case: Compute-intensive apps
├── Memory-based Metrics
│   ├── Target Utilization: 70-85%
│   ├── Note: Memory not auto-released
│   └── Use Case: Memory-intensive apps
├── Custom Metrics
│   ├── HTTP Request Count
│   │   ├── Target: 1000 req/s per pod
│   │   └── Use Case: Web applications
│   ├── Queue Length
│   │   ├── Target: 100 messages per pod
│   │   └── Use Case: Message processing services
│   └── Business Metrics
│       ├── Target: Defined by business
│       └── Use Case: Specific business scenarios
└── Scaling Behavior
    ├── Scale-up Strategy
    │   ├── Fast scale-up: Trigger immediate scale when threshold reached
    │   ├── Stabilization Window: 0-60s
    │   └── Scale-up Rate: Increase 100% or 4 Pods per time (whichever smaller)
    └── Scale-down Strategy
        ├── Conservative scale-down: Avoid frequent fluctuations
        ├── Stabilization Window: 300s
        └── Scale-down Rate: Decrease 10% or 1 Pod per time
```

## Pros and Cons Examples (✅/❌ Table)

### Resource Configuration

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| CPU and Memory Limits | No requests and limits set, node resources exhausted, all Pods evicted | Set reasonable requests to guarantee resource allocation, limits to prevent resource abuse |
| Requests = Limits | All Pods set requests = limits, resource utilization only 30%, severe waste | Requests set to daily usage, Limits set to 2-4x peak, allow resource overcommit |
| Excessively High Limits | Limits set too high (CPU 16 cores), single Pod may fill node | Set reasonable Limits based on load test results, single Pod not exceeding 50% of node capacity |
| QoS Category Confusion | BestEffort Pods mixed with Guaranteed Pods, Guaranteed Pods evicted during resource pressure | Core services use Guaranteed, non-core use Burstable |

### Health Checks

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| Missing Health Checks | No probe configured, app startup fails but Pod status Running, traffic hits faulty Pod | Configure livenessProbe for liveness check, readinessProbe for readiness check, startupProbe for slow startup |
| Improper Probe Config | livenessProbe timeout 1 second, app occasionally lags then restarts, frequent restart loop | Set reasonable timeout (3-5s) and failureThreshold (3 times), avoid false kills |
| Wrong Probe Endpoint | Use homepage / as health check endpoint, depends on database, database failure causes all Pod restarts | Use dedicated health check endpoint (/actuator/health), only check app self status |
| Missing startupProbe | App startup needs 2 minutes, livenessProbe 30 seconds kills Pod, never starts | Configure startupProbe to give sufficient startup time (failureThreshold * periodSeconds > startup time) |

### Deployment Strategy

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| Rolling Update Config | maxSurge=0, maxUnavailable=1, 3 replicas update leaves 2, service capacity drops | maxSurge=1, maxUnavailable=0, creates new Pod before update, ensure service capacity maintained |
| No PodDisruptionBudget | Node upgrade simultaneously evicts all Pods, service completely interrupted | Configure PDB minAvailable=2, ensure at least 2 Pods available during update |
| Rollback History Retention | Default retains 10 versions, occupies massive etcd storage | Set revisionHistoryLimit=5, retain 5 versions sufficient for rollback |
| Unvalidated Update | Direct rolling update to production, new version has bug causes full failure | Use blue-green deployment or canary release, first release few Pods for validation |

### Service and Ingress

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| Service Selector Wrong | Selector label misspelled, Service selects no Pods, Endpoints empty | Use kubectl get endpoints to verify Service correctly selects Pods |
| Port Mapping Wrong | Service port doesn't match Pod containerPort, traffic cannot reach | Ensure Service targetPort matches Pod containerPort |
| SessionAffinity Abuse | All services configured sessionAffinity=ClientIP, causes load imbalance | Only configure sessionAffinity for services needing session persistence (like WebSocket) |
| Ingress No Health Check | Ingress backend Pods not ready receive traffic, returns 502 error | Ensure readinessProbe correctly configured, Ingress only forwards to Ready Pods |
| TLS Certificate Expired | Manual certificate management, service interrupted after expiration | Use cert-manager to auto-manage certificates, auto-renewal |

### ConfigMap and Secret

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| Hardcoded Config | Config written directly in Deployment YAML env, changes require redeployment | Use ConfigMap to manage config, ConfigMap changes auto-update Pod (needs app support) |
| Secret Plaintext Storage | Secret data unencrypted, stored in YAML file as base64 encoding | Use SealedSecrets or cloud provider key management service, Secret not committed to Git |
| ConfigMap Too Large | Single ConfigMap exceeds 1MB, causes etcd performance degradation | Large files use Volume mount, ConfigMap only stores small config items |
| Config Change Not Effective | Modified ConfigMap but Pod config not updated, app still uses old config | Use subPath mount or rolling restart Pod after config update |

### Affinity and Scheduling

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| No Anti-Affinity Config | 3 replicas scheduled to same node, node failure causes service completely unavailable | Configure podAntiAffinity to ensure replicas spread across different nodes |
| Hard Affinity Overuse | Use requiredDuringScheduling, Pod cannot schedule when node doesn't meet condition | Use preferredDuringScheduling, soft constraint improves scheduling success rate |
| Node Selector Too Strict | Only schedule to specific node, node resources insufficient causes Pod Pending | Use node labels and taints/tolerations, flexible scheduling |
| Ignore Node Capacity | Schedule to small spec node, single Pod requests greater than node capacity, can never schedule | Reasonably plan node specs and Pod resource requirements |

### Storage Configuration

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| Using HostPath | Production database uses HostPath storage, Pod scheduled to other node loses data | Use PV/PVC + StorageClass dynamic allocation persistent storage |
| Wrong Access Mode | StatefulSet uses ReadWriteMany, but cloud disk only supports RWO, Pod cannot start | Confirm storage type supported access modes, StatefulSet typically uses RWO |
| No Storage Expansion Strategy | Storage full requires manual expansion, needs downtime operation | Use StorageClass supporting online expansion, configure allowVolumeExpansion=true |
| Data Not Backed Up | Only rely on PV storage, PV damaged or mistakenly deleted causes permanent data loss | Regularly backup to object storage (S3/OSS), use Velero backup tools |

### HPA Auto-Scaling

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| No Requests Set | Pod doesn't set CPU requests, HPA cannot calculate utilization, not effective | Ensure Pod sets resources.requests, HPA can work normally |
| Scaling Too Aggressive | Scale-down stabilization window 30 seconds, traffic fluctuation causes frequent scaling, Pods repeatedly created/destroyed | Set scale-down stabilization window 5-10 minutes, avoid frequent fluctuations |
| min = max | Set minReplicas = maxReplicas = 5, HPA cannot function | minReplicas set to daily load replica count, maxReplicas set to 2x peak |
| Multiple HPA Conflicts | Simultaneously configure CPU-based HPA and custom metrics HPA, two HPAs interfere with each other | One Deployment only configure one HPA, multiple metrics configured in same HPA |

## Verification Checklist

### Deployment Verification

- [ ] Deployment/StatefulSet created successfully
- [ ] Pod status all Running
- [ ] Pod readiness probes all passed (Ready 1/1)
- [ ] Replica count reaches desired value
- [ ] Container image pulled successfully, no ImagePullBackOff
- [ ] Container starts without CrashLoopBackOff
- [ ] View Pod logs no ERROR level errors

### Resource Configuration Verification

- [ ] All containers set CPU and memory requests
- [ ] All containers set CPU and memory limits
- [ ] Requests don't exceed node allocatable resources
- [ ] QoS category meets expectations (Guaranteed/Burstable/BestEffort)
- [ ] Node resources sufficient, no Pod in Pending status
- [ ] Use kubectl top pods to verify actual resource usage

### Health Check Verification

- [ ] livenessProbe configured correctly, Pod auto-restarts on anomaly
- [ ] readinessProbe configured correctly, unready Pods don't receive traffic
- [ ] startupProbe configured correctly (if app starts slowly)
- [ ] Probe timeout reasonable (recommend 3-5 seconds)
- [ ] Probe failure threshold reasonable (recommend 3 times)
- [ ] Manually simulate failure, verify probe can detect

### Network Configuration Verification

- [ ] Service created successfully
- [ ] Service Endpoints include all Pod IPs
- [ ] ClusterIP accessible within cluster
- [ ] Service name can be resolved via DNS
- [ ] Ingress created successfully (if needed)
- [ ] Ingress domain resolves correctly
- [ ] TLS certificate configured correctly (if needed)
- [ ] External can access service via Ingress

### Storage Configuration Verification

- [ ] PVC created successfully and bound to PV
- [ ] PVC status is Bound
- [ ] Storage capacity meets requirements
- [ ] Access mode correct (RWO/ROX/RWX)
- [ ] Pod can read/write mounted volume
- [ ] Data persistence effective (data remains after Pod rebuild)
- [ ] Storage performance meets requirements (IOPS/throughput)

### High Availability Verification

- [ ] Production environment at least 3 replicas
- [ ] Configured PodDisruptionBudget
- [ ] Configured Pod anti-affinity, replicas spread across different nodes
- [ ] Rolling update strategy configured correctly (maxSurge/maxUnavailable)
- [ ] HPA configured correctly (if needed)
- [ ] Manually delete one Pod, verify auto-recovery
- [ ] Simulate node failure, verify Pod auto-migration

### Configuration Management Verification

- [ ] Use ConfigMap to manage config files
- [ ] Use Secret to manage sensitive info
- [ ] Secret data correctly base64 encoded
- [ ] Config correctly mounted to Pod
- [ ] App can read config
- [ ] Modified ConfigMap effective (or restart Pod effective)

### Monitoring and Log Verification

- [ ] Pod tagged with correct labels (app, version, environment)
- [ ] Logs output normally to stdout/stderr
- [ ] Use kubectl logs to view logs
- [ ] Prometheus can collect metrics (if integrated)
- [ ] Monitoring dashboard displays normally (if has Grafana)
- [ ] Alert rules configured correctly (if needed)

## Guardrails and Constraints

### Resource Quota Limits

```
Pod Resource Limits
├── CPU Quota
│   ├── Requests Minimum: 50m (0.05 core)
│   ├── Requests Recommended:
│   │   ├── Lightweight services: 100m-500m
│   │   ├── Standard services: 500m-1000m
│   │   └── Heavy services: 1000m-4000m
│   ├── Limits Maximum: Not exceed 80% of node total CPU
│   └── Limits/Requests Ratio: Recommend 2-4x
├── Memory Quota
│   ├── Requests Minimum: 64Mi
│   ├── Requests Recommended:
│   │   ├── Lightweight services: 128Mi-512Mi
│   │   ├── Standard services: 512Mi-2Gi
│   │   └── Heavy services: 2Gi-8Gi
│   ├── Limits Maximum: Not exceed 80% of node total memory
│   ├── Limits/Requests Ratio: Recommend 1.5-2x
│   └── Note: Memory Limits exceeded causes OOMKilled
└── QoS Categories
    ├── Guaranteed (Guaranteed Level)
    │   ├── Condition: requests = limits
    │   ├── Priority: Highest
    │   └── Use Case: Core services, databases
    ├── Burstable (Elastic Level)
    │   ├── Condition: Set requests, limits > requests
    │   ├── Priority: Medium
    │   └── Use Case: Most application services
    └── BestEffort (Best Effort Level)
        ├── Condition: No requests and limits set
        ├── Priority: Lowest
        └── Use Case: Temporary tasks (not recommended for production)

Namespace Resource Quotas
├── Dev Environment (Namespace: dev)
│   ├── Total CPU: 20 cores
│   ├── Total Memory: 40Gi
│   ├── Pod Count: 100
│   └── PVC Count: 20
├── Test Environment (Namespace: test)
│   ├── Total CPU: 50 cores
│   ├── Total Memory: 100Gi
│   ├── Pod Count: 200
│   └── PVC Count: 50
└── Production Environment (Namespace: prod)
    ├── Total CPU: 200 cores
    ├── Total Memory: 400Gi
    ├── Pod Count: 500
    ├── PVC Count: 100
    └── Service/LoadBalancer Count: 20

Storage Quota Limits
├── Storage Capacity
│   ├── Single PVC Minimum: 1Gi
│   ├── Single PVC Maximum:
│   │   ├── Dev environment: 100Gi
│   │   ├── Test environment: 500Gi
│   │   └── Production environment: 2Ti
│   └── Namespace Total Storage Quota:
│       ├── Dev: 1Ti
│       ├── Test: 5Ti
│       └── Production: 50Ti
├── Storage Performance
│   ├── Standard Storage (HDD)
│   │   ├── IOPS: 100-1000
│   │   ├── Throughput: 50-100 MB/s
│   │   └── Use Case: Logs, backups
│   ├── Performance Storage (SSD)
│   │   ├── IOPS: 3000-10000
│   │   ├── Throughput: 250-500 MB/s
│   │   └── Use Case: Databases, caches
│   └── High-Performance Storage (NVMe)
│       ├── IOPS: 20000+
│       ├── Throughput: 1000+ MB/s
│       └── Use Case: High-concurrency databases
└── Reclaim Policy
    ├── Retain: Manual cleanup, suitable for production data
    ├── Delete: Auto-delete, suitable for temporary data
    └── Recommendation: Production use Retain, regular backups
```

### Health Check Parameter Boundaries

```
Liveness Probe (Liveness Probe)
├── initialDelaySeconds: 30-120s
│   ├── Fast-starting apps: 30-60s
│   ├── Slow-starting apps: 60-120s
│   └── Recommendation: Slightly larger than actual app startup time
├── periodSeconds: 10-30s
│   ├── Recommended: 10s (regular check)
│   └── Maximum: 30s (low-frequency check)
├── timeoutSeconds: 3-10s
│   ├── Recommended: 3-5s
│   └── Note: After timeout, check considered failed
├── failureThreshold: 3-5 times
│   ├── Recommended: 3 times
│   └── Total failure time: periodSeconds × failureThreshold
└── successThreshold: 1 time (fixed, not configurable)

Readiness Probe (Readiness Probe)
├── initialDelaySeconds: 10-60s
│   ├── Recommended: Slightly shorter than liveness
│   └── Purpose: Quickly start receiving traffic
├── periodSeconds: 5-10s
│   ├── Recommended: 5s (faster detect ready status)
│   └── Note: Frequency higher than liveness
├── timeoutSeconds: 3-5s
├── failureThreshold: 3 times
│   └── Total failure time: periodSeconds × failureThreshold
└── successThreshold: 1 time
    └── Note: 1 success considered ready

Startup Probe (Startup Probe)
├── initialDelaySeconds: 0-30s
│   └── Usually set to 0, controlled by failureThreshold for total time
├── periodSeconds: 10-30s
├── timeoutSeconds: 5-10s
├── failureThreshold: Maximum value
│   ├── Calculation: (App max startup time / periodSeconds) + buffer
│   ├── Example: App max startup 5 minutes, periodSeconds=10s
│   │   └── failureThreshold = (300s / 10s) + 5 = 35
│   └── Note: Before startup probe fails, liveness probe not executed
└── successThreshold: 1 time

Probe Type Selection
├── HTTP GET (Recommended)
│   ├── Use Case: HTTP/HTTPS services
│   ├── Endpoint Examples: /actuator/health, /health, /ready
│   └── Return Code: 2xx or 3xx indicates success
├── TCP Socket
│   ├── Use Case: TCP services (databases, caches, gRPC)
│   └── Check: Whether port connectable
├── Exec Command
│   ├── Use Case: Custom check logic
│   ├── Return Code: 0 indicates success
│   └── Note: Avoid time-consuming operations, impacts performance
└── gRPC
    ├── Use Case: gRPC services
    └── Requirement: Kubernetes 1.24+
```

### Rolling Update Parameter Boundaries

```
Rolling Update Strategy
├── maxSurge (Max Extra Pod Count)
│   ├── Recommended: 1 or 25%
│   ├── Description: Max number of Pods that can exceed desired replicas during update
│   ├── Calculation Method:
│   │   ├── Integer: Absolute number (like 1, 2)
│   │   └── Percentage: Percentage of desired replicas (like 25%)
│   └── Impact:
│       ├── Larger value, faster update, but more resource usage
│       └── Smaller value, slower update, but less resource usage
├── maxUnavailable (Max Unavailable Pod Count)
│   ├── Recommended: 0 or 25%
│   ├── Description: Max number of unavailable Pods during update
│   ├── Note: maxSurge and maxUnavailable cannot both be 0
│   └── Impact:
│       ├── Larger value, faster update, but service capacity drops
│       └── Set to 0 ensures service capacity maintained (recommended for production)
├── Typical Configs
│   ├── High Availability Priority (Recommended for Production)
│   │   ├── maxSurge: 1
│   │   ├── maxUnavailable: 0
│   │   └── Create new Pod first, then delete old Pod
│   ├── Fast Update Priority
│   │   ├── maxSurge: 50%
│   │   ├── maxUnavailable: 50%
│   │   └── Fast replacement, but service capacity temporarily drops
│   └── Resource-Constrained Environment
│       ├── maxSurge: 0
│       ├── maxUnavailable: 1
│       └── Delete old Pod first, then create new Pod
└── Update Interval
    ├── minReadySeconds: 0-60s
    │   ├── Recommended: 10-30s
    │   └── Description: How long to wait after Pod ready before considered available
    └── progressDeadlineSeconds: 600s
        ├── Recommended: 600s (10 minutes)
        └── Description: Mark as failed if update not completed after this time

PodDisruptionBudget (PDB)
├── minAvailable (Min Available Replica Count)
│   ├── Integer: Absolute number (like 2)
│   ├── Percentage: Percentage of desired replicas (like 50%)
│   └── Recommended:
│       ├── 3 replicas: minAvailable = 2
│       ├── 5 replicas: minAvailable = 3
│       └── Ensure sufficient replicas always available during update
├── maxUnavailable (Max Unavailable Replica Count)
│   ├── Integer: Absolute number (like 1)
│   ├── Percentage: Percentage of desired replicas (like 25%)
│   └── Note: minAvailable and maxUnavailable can only set one
└── unhealthyPodEvictionPolicy
    ├── IfHealthyBudget (default): Only evict unhealthy Pods when healthy replicas meet PDB
    └── AlwaysAllow: Evict unhealthy Pods even if not meeting PDB

Rollback Config
├── revisionHistoryLimit: 3-10
│   ├── Recommended: 5
│   ├── Description: Number of historical versions retained
│   └── Impact: Each version occupies etcd storage space
└── Rollback Commands
    ├── Rollback to previous version: kubectl rollout undo deployment/app
    ├── Rollback to specific version: kubectl rollout undo deployment/app --to-revision=3
    └── View history: kubectl rollout history deployment/app
```

### HPA Scaling Parameter Boundaries

```
Basic Configuration
├── minReplicas: 1-10
│   ├── Production minimum: 3 (ensure high availability)
│   ├── Test environment: 2
│   └── Dev environment: 1
├── maxReplicas: minReplicas × 2 ~ 10
│   ├── Recommended: 2-5x minReplicas
│   ├── Example: minReplicas=3, maxReplicas=10
│   └── Note: Consider node resource limits
└── Metric Targets
    ├── CPU Utilization: 60-80%
    ├── Memory Utilization: 70-85%
    └── Custom Metrics: Defined by business

Scaling Behavior
├── Scale-up Strategy (ScaleUp)
│   ├── stabilizationWindowSeconds: 0-60s
│   │   ├── Recommended: 0s (fast response)
│   │   └── Description: Metric stabilization window, take max value within window
│   ├── policies:
│   │   ├── Policy 1: Percentage scale-up
│   │   │   ├── type: Percent
│   │   │   ├── value: 100 (double each time)
│   │   │   └── periodSeconds: 15 (evaluate every 15 seconds)
│   │   └── Policy 2: Fixed count scale-up
│   │       ├── type: Pods
│   │       ├── value: 4 (increase 4 each time)
│   │       └── periodSeconds: 15
│   └── selectPolicy: Max (take most scale-up among multiple policies)
│       └── Example: Double=6 Pods, Fixed=4 Pods, choose 6 Pods
├── Scale-down Strategy (ScaleDown)
│   ├── stabilizationWindowSeconds: 60-600s
│   │   ├── Recommended: 300s (5 minutes)
│   │   └── Description: Metric stabilization window, take min value within window, avoid frequent scale-down
│   ├── policies:
│   │   ├── Policy 1: Percentage scale-down
│   │   │   ├── type: Percent
│   │   │   ├── value: 10 (decrease 10% each time)
│   │   │   └── periodSeconds: 60 (evaluate every 1 minute)
│   │   └── Policy 2: Fixed count scale-down
│   │       ├── type: Pods
│   │       ├── value: 1 (decrease 1 each time)
│   │       └── periodSeconds: 60
│   └── selectPolicy: Min (take least scale-down among multiple policies, conservative scale-down)
└── Default Behavior (when Behavior not configured)
    ├── Scale-up: Double each time, max 4 Pods/time
    ├── Scale-down: Decrease 50% each time, stabilization window 5 minutes
    └── Recommendation: Explicitly configure Behavior in production

Metric Collection Cycle
├── HPA Evaluation Cycle: 15s (--horizontal-pod-autoscaler-sync-period)
├── Metric Collection Cycle: 60s (Metrics Server default)
├── Metric Delay:
│   ├── From actual load change to HPA awareness: 60-90s
│   └── From HPA decision to Pod ready: 60-120s (depends on app startup time)
└── Impact: HPA response has 2-3 minute delay, need advance scale-up
```

## Common Issues Diagnostic Table

| Issue | Possible Cause | Diagnostic Steps | Solution |
|---------|---------|---------|---------|
| Pod always Pending | 1. Insufficient resources<br>2. Node selector mismatch<br>3. PVC cannot bind<br>4. Image pull failure | 1. kubectl describe pod <pod-name><br>2. View Events section<br>3. Check node resources | 1. Add nodes or lower resource requests<br>2. Correct node labels or selectors<br>3. Check StorageClass config<br>4. Check image registry access |
| Pod repeatedly restarts (CrashLoopBackOff) | 1. App startup failure<br>2. Health check failure<br>3. Resource limits too low<br>4. Config error | 1. kubectl logs <pod-name><br>2. View app error logs<br>3. kubectl describe pod<br>4. Check env vars and config | 1. Fix app bug<br>2. Adjust probe config<br>3. Increase resource limits<br>4. Correct config and env vars |
| Service cannot access | 1. Endpoints empty<br>2. Pod not ready<br>3. Port mapping error<br>4. NetworkPolicy blocks | 1. kubectl get endpoints <svc><br>2. kubectl get pods<br>3. kubectl describe svc<br>4. Test Pod IP direct access | 1. Check selector matches Pod labels<br>2. Ensure readinessProbe passes<br>3. Confirm targetPort matches containerPort<br>4. Check NetworkPolicy rules |
| Ingress returns 502/504 | 1. Backend Pod not ready<br>2. Service doesn't exist<br>3. Ingress config error<br>4. Backend app timeout | 1. kubectl get pods<br>2. kubectl get svc<br>3. kubectl describe ingress<br>4. View Ingress Controller logs | 1. Wait for Pod ready<br>2. Create or correct Service<br>3. Correct Ingress config<br>4. Increase timeout or optimize app |
| PVC always Pending | 1. StorageClass doesn't exist<br>2. Storage quota insufficient<br>3. Access mode not supported<br>4. Dynamic provisioning failure | 1. kubectl describe pvc <pvc-name><br>2. kubectl get storageclass<br>3. View Events<br>4. Check storage backend logs | 1. Create or specify correct StorageClass<br>2. Increase storage quota<br>3. Change to supported access mode<br>4. Check storage plugin config |
| HPA not effective | 1. Metrics Server not installed<br>2. Pod no requests set<br>3. Metric collection failure<br>4. Reached maxReplicas limit | 1. kubectl top pods<br>2. kubectl describe hpa<br>3. Check Metrics Server logs<br>4. kubectl get hpa | 1. Install Metrics Server<br>2. Set resources.requests for Pods<br>3. Fix Metrics Server<br>4. Increase maxReplicas or optimize app |
| Rolling update stuck | 1. New Pod cannot ready<br>2. PDB blocks eviction<br>3. Insufficient resources<br>4. readinessProbe failure | 1. kubectl rollout status<br>2. kubectl get pods<br>3. kubectl describe pod<br>4. View new Pod logs | 1. Fix app issues<br>2. Adjust PDB config<br>3. Add node resources<br>4. Correct readinessProbe config |
| ConfigMap change not effective | 1. Using subPath mount<br>2. Environment variable injection (won't auto-update)<br>3. App not monitoring file changes | 1. kubectl describe pod<br>2. View mount method<br>3. Enter container check file | 1. Avoid using subPath<br>2. Restart Pod after modifying ConfigMap<br>3. App implements config hot reload |
| Node resources insufficient | 1. Resource overcommit<br>2. No Requests/Limits set<br>3. Node spec too small | 1. kubectl describe node<br>2. kubectl top node<br>3. View resource allocation | 1. Add nodes or expand existing nodes<br>2. Set Requests for all Pods<br>3. Evict low-priority Pods |
| Pod evicted | 1. Node memory/disk insufficient<br>2. Exceeded resource Limits<br>3. Node pressure eviction | 1. kubectl describe pod<br>2. View eviction reason<br>3. kubectl describe node | 1. Clean node disk space<br>2. Increase Pod resource Limits<br>3. Add node resources or reduce Pod count |
| DNS resolution failure | 1. CoreDNS abnormal<br>2. Service doesn't exist<br>3. Network policy blocks<br>4. DNS config error | 1. kubectl get pods -n kube-system<br>2. kubectl exec <pod> -- nslookup <service><br>3. View CoreDNS logs | 1. Restart CoreDNS<br>2. Confirm Service name correct<br>3. Open DNS port (UDP 53)<br>4. Check Pod's dnsPolicy |
| Liveness probe frequently kills Pod | 1. Probe timeout too short<br>2. App occasionally lags<br>3. Probe endpoint depends on external services | 1. kubectl describe pod<br>2. View probe config<br>3. View app logs | 1. Increase timeout and failureThreshold<br>2. Optimize app performance<br>3. Probe endpoint only checks self status |

## Output Format Requirements

### Deployment Configuration Output

```yaml
# Application Name: order-service
# Description: Order service, stateless app, supports horizontal scaling

apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
  namespace: production
  labels:
    app: order-service
    version: v1.0.0
    environment: production
spec:
  replicas: 3
  selector:
    matchLabels:
      app: order-service
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  revisionHistoryLimit: 5
  template:
    metadata:
      labels:
        app: order-service
        version: v1.0.0
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
    spec:
      # Node Selection and Scheduling
      nodeSelector:
        node-type: application
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 100
              podAffinityTerm:
                labelSelector:
                  matchLabels:
                    app: order-service
                topologyKey: kubernetes.io/hostname

      # Security Context
      securityContext:
        runAsUser: 1000
        runAsGroup: 1000
        fsGroup: 1000

      # Init Containers (Optional)
      initContainers:
        - name: wait-for-db
          image: busybox:1.36
          command: ['sh', '-c', 'until nc -z mysql 3306; do sleep 2; done']

      # Application Container
      containers:
        - name: order-service
          image: registry.example.com/order-service:v1.0.0
          imagePullPolicy: IfNotPresent

          ports:
            - name: http
              containerPort: 8080
              protocol: TCP

          # Environment Variables
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
            - name: POD_NAME
              valueFrom:
                fieldRef:
                  fieldPath: metadata.name

          envFrom:
            - configMapRef:
                name: order-service-config
            - secretRef:
                name: order-service-secret

          # Resource Limits
          resources:
            requests:
              cpu: "500m"
              memory: "1Gi"
            limits:
              cpu: "2000m"
              memory: "2Gi"

          # Health Checks
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 10
            timeoutSeconds: 5
            failureThreshold: 3

          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 5
            timeoutSeconds: 3
            failureThreshold: 3

          startupProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 10
            timeoutSeconds: 5
            failureThreshold: 30

          # Lifecycle Hooks
          lifecycle:
            preStop:
              exec:
                command: ["/bin/sh", "-c", "sleep 15"]

          # Volume Mounts
          volumeMounts:
            - name: logs
              mountPath: /app/logs

      # Volume Definitions
      volumes:
        - name: logs
          emptyDir: {}

      # Image Pull Secrets
      imagePullSecrets:
        - name: registry-secret

      # Graceful Termination Time
      terminationGracePeriodSeconds: 30

---
# Service Configuration
apiVersion: v1
kind: Service
metadata:
  name: order-service
  namespace: production
  labels:
    app: order-service
spec:
  type: ClusterIP
  selector:
    app: order-service
  ports:
    - name: http
      port: 80
      targetPort: 8080
      protocol: TCP

---
# HPA Configuration
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: order-service-hpa
  namespace: production
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: order-service
  minReplicas: 3
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
        - type: Percent
          value: 10
          periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
        - type: Percent
          value: 100
          periodSeconds: 15
        - type: Pods
          value: 4
          periodSeconds: 15
      selectPolicy: Max

---
# PDB Configuration
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: order-service-pdb
  namespace: production
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: order-service

# Configuration Notes:
# - Replicas: 3 (ensure high availability)
# - Resources: requests 500m/1Gi, limits 2000m/2Gi
# - Health Checks: Configure three types of probes, ensure service availability
# - Rolling Update: maxSurge=1, maxUnavailable=0, ensure service not interrupted
# - HPA: Auto-scale based on CPU 70% and memory 80%, 3-10 replicas
# - PDB: Ensure at least 2 replicas available during update
```

### StatefulSet Configuration Output

```yaml
# Application Name: mysql
# Description: MySQL stateful service, master-slave replication cluster

apiVersion: v1
kind: Service
metadata:
  name: mysql-headless
  namespace: production
spec:
  clusterIP: None
  selector:
    app: mysql
  ports:
    - name: mysql
      port: 3306
      targetPort: 3306

---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql
  namespace: production
spec:
  serviceName: mysql-headless
  replicas: 3
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
        - name: mysql
          image: mysql:8.0
          ports:
            - containerPort: 3306
              name: mysql
          env:
            - name: MYSQL_ROOT_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mysql-secret
                  key: root-password
          volumeMounts:
            - name: data
              mountPath: /var/lib/mysql
          resources:
            requests:
              cpu: "1000m"
              memory: "2Gi"
            limits:
              cpu: "4000m"
              memory: "8Gi"
          livenessProbe:
            exec:
              command: ["mysqladmin", "ping", "-h", "localhost"]
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            exec:
              command: ["mysql", "-h", "localhost", "-e", "SELECT 1"]
            initialDelaySeconds: 10
            periodSeconds: 5
  volumeClaimTemplates:
    - metadata:
        name: data
      spec:
        accessModes: ["ReadWriteOnce"]
        storageClassName: fast-ssd
        resources:
          requests:
            storage: 100Gi

# Access Method:
# - Pod 0: mysql-0.mysql-headless.production.svc.cluster.local
# - Pod 1: mysql-1.mysql-headless.production.svc.cluster.local
# - Pod 2: mysql-2.mysql-headless.production.svc.cluster.local
```

### Ingress Configuration Output

```yaml
# Ingress Configuration: Multi-service routing and TLS termination

apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: api-ingress
  namespace: production
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/proxy-body-size: "50m"
    nginx.ingress.kubernetes.io/limit-rps: "100"
    nginx.ingress.kubernetes.io/enable-cors: "true"
    nginx.ingress.kubernetes.io/cors-allow-origin: "*"
spec:
  tls:
    - hosts:
        - api.example.com
      secretName: api-tls-cert
  rules:
    - host: api.example.com
      http:
        paths:
          - path: /api/v1/orders
            pathType: Prefix
            backend:
              service:
                name: order-service
                port:
                  number: 80
          - path: /api/v1/users
            pathType: Prefix
            backend:
              service:
                name: user-service
                port:
                  number: 80
          - path: /api/v1/products
            pathType: Prefix
            backend:
              service:
                name: product-service
                port:
                  number: 80

# Access Method:
# - https://api.example.com/api/v1/orders -> order-service
# - https://api.example.com/api/v1/users -> user-service
# - https://api.example.com/api/v1/products -> product-service
```
