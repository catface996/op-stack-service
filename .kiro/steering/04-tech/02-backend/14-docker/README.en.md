---
inclusion: manual
---
# Docker Containerization Best Practices

## Role Definition

You are a DevOps expert proficient in Docker and containerization, specializing in image building, container orchestration, and CI/CD integration. You can design efficient containerization solutions, optimize image sizes, and ensure container security and maintainability.

## Core Principles (NON-NEGOTIABLE)

| Principle | Description | Consequences of Violation |
|------|------|----------|
| Multi-stage Build | Use multi-stage builds to separate build and runtime environments, minimize image size | Image bloat, slow deployment, increased security risks |
| Non-root User | Run applications as non-privileged users inside containers, avoid security vulnerabilities | Container escape risk, production security hazards |
| Health Checks | Configure health checks for all long-running containers, ensure service availability | Faulty containers cannot auto-restart, impacting service stability |
| Resource Limits | Explicitly set CPU and memory limits to prevent resource exhaustion | Single container consumes excessive resources, impacting other services |
| Single Responsibility | Each container runs only one main process, avoid complex process management | Containers difficult to monitor and debug, violates microservices principles |
| Image Layer Optimization | Organize Dockerfile instructions reasonably, fully utilize layer caching | Long build times, low CI/CD efficiency |

## Prompt Templates

### Dockerfile Writing

```
Please help me write a Dockerfile:

[Application Info]
- Application Type: [Java Spring Boot/Node.js Express/Python Flask/Go/Other]
- Language Version: [Java 17/Node 20/Python 3.11/Go 1.21]
- Build Tool: [Maven/Gradle/npm/pip/go build]
- Framework Version: [Specific version]

[Build Requirements]
- Build Method: [Single-stage/Multi-stage]
- Base Image: [alpine/slim/distroless/Official standard image]
- Need Compilation: [Yes/No]
- Dependency Management: [How to install dependencies]

[Optimization Goals]
- [ ] Minimize image size (target < 200MB)
- [ ] Optimize build speed (utilize caching)
- [ ] Security hardening (non-root user, vulnerability scanning)
- [ ] Production optimization (health checks, graceful shutdown)

[Runtime Requirements]
- Exposed Ports: [8080/3000/etc.]
- Environment Variables: [List key environment variables]
- Volume Mounts: [Logs/Data/Config]
- Startup Command: [Specific command]

Please provide optimized Dockerfile and explanation.
```

### Docker Compose Writing

```
Please help me write Docker Compose configuration:

[Service Architecture]
- Application Services: [List application services and ports]
- Middleware Services: [MySQL/Redis/Nacos/Elasticsearch/RabbitMQ/etc.]
- Network Topology: [Inter-service call relationships]

[Environment Requirements]
- Deployment Environment: [Dev environment/Test environment/Production environment]
- Dev Mode: [Need hot reload/Source mount]
- Data Persistence: [Which services need data persistence]

[Service Dependencies]
- Startup Order: [Describe service dependency relationships]
- Health Checks: [Health check methods for each service]
- Restart Policy: [always/on-failure/unless-stopped]

[Network and Storage]
- Network Mode: [bridge/host/overlay]
- Volume Type: [named volume/bind mount]
- Config Files: [Config files to mount]

[Resource Limits]
- Resource Quota per Service: [CPU/Memory limits]
- Log Configuration: [Log driver and size limits]

Please provide complete docker-compose.yml configuration.
```

### Image Optimization Solution

```
Please help me optimize Docker image:

[Current Status]
- Current Image Size: [XXX MB]
- Base Image: [Current base image used]
- Dockerfile Layers: [XX layers]
- Build Time: [X minutes]

[Optimization Goals]
- [ ] Reduce image size to [Target size]
- [ ] Shorten build time to [Target time]
- [ ] Improve cache hit rate
- [ ] Eliminate security vulnerabilities

[Application Characteristics]
- Compiled/Interpreted: [Describe]
- Dependency Count: [Approximate count]
- Runtime Dependencies: [Need compilation tools]
- Static Assets: [Large amount of static files]

[Constraints]
- Compatibility Requirements: [glibc version/System dependencies]
- Security Requirements: [Need vulnerability scanning]
- Functional Requirements: [Debug tools/shell etc.]

Please provide specific optimization plan and optimized Dockerfile.
```

### CI/CD Integration Solution

```
Please help me design Docker image CI/CD process:

[CI/CD Platform]
- Platform: [GitHub Actions/GitLab CI/Jenkins/Other]
- Trigger Conditions: [push/tag/PR]
- Branch Strategy: [main/develop/release]

[Build Requirements]
- Build Environment: [Runner specs]
- Parallel Build: [Need or not]
- Multi-arch Support: [amd64/arm64]
- Build Cache: [How to utilize cache]

[Image Registry]
- Registry Type: [Docker Hub/Harbor/ECR/ACR/GCR]
- Image Naming: [Naming convention]
- Tag Strategy: [latest/version number/git-sha]
- Image Cleanup: [Retention policy]

[Quality Checks]
- [ ] Unit tests
- [ ] Image security scanning (Trivy/Clair)
- [ ] Image size check
- [ ] Runtime testing

[Deployment Integration]
- Deployment Target: [Kubernetes/Docker Swarm/ECS]
- Deployment Strategy: [Rolling update/Blue-green/Canary]
- Rollback Mechanism: [How to rollback]

Please provide complete CI/CD configuration file and process description.
```

## Decision Guide (Tree Structure)

```
Base Image Selection
├── Application Type: Java
│   ├── Dev/Debug Environment
│   │   ├── eclipse-temurin:17-jdk
│   │   ├── Size: ~450MB
│   │   ├── Includes: Full JDK, debug tools
│   │   └── Use Case: Local development, troubleshooting
│   ├── Production Standard Environment
│   │   ├── eclipse-temurin:17-jre
│   │   ├── Size: ~280MB
│   │   ├── Includes: JRE, basic tools
│   │   └── Use Case: Regular production deployment
│   ├── Lightweight Environment
│   │   ├── eclipse-temurin:17-jre-alpine
│   │   ├── Size: ~170MB
│   │   ├── Includes: JRE, musl libc
│   │   ├── Note: Some native libraries may be incompatible
│   │   └── Use Case: Size-sensitive scenarios
│   └── Extreme Minimization
│       ├── gcr.io/distroless/java17
│       ├── Size: ~140MB
│       ├── Includes: JRE and runtime only
│       ├── No shell, no package manager
│       └── Use Case: High-security production environments
├── Application Type: Node.js
│   ├── Standard Environment
│   │   ├── node:20-bullseye
│   │   ├── Size: ~950MB
│   │   ├── Use Case: Need to compile native modules
│   ├── Slim Environment
│   │   ├── node:20-slim
│   │   ├── Size: ~240MB
│   │   ├── Use Case: Pure JavaScript applications
│   └── Minimal Environment
│       ├── node:20-alpine
│       ├── Size: ~140MB
│       └── Use Case: Production environment preferred
├── Application Type: Python
│   ├── Standard Environment
│   │   ├── python:3.11-bullseye
│   │   ├── Size: ~900MB
│   │   ├── Use Case: Scientific computing, machine learning
│   ├── Slim Environment
│   │   ├── python:3.11-slim
│   │   ├── Size: ~150MB
│   │   ├── Use Case: Web apps, API services
│   └── Minimal Environment
│       ├── python:3.11-alpine
│       ├── Size: ~50MB
│       ├── Note: Some packages need manual compilation dependencies
│       └── Use Case: Extremely size-sensitive
└── Application Type: Go
    ├── Build Stage
    │   ├── golang:1.21-alpine
    │   ├── Size: ~350MB
    │   └── Purpose: Compile binary files
    └── Runtime Stage
        ├── scratch (empty image)
        │   ├── Size: 0MB (application binary only)
        │   ├── Use Case: Pure static compilation
        │   └── Minimal image size
        ├── alpine:3.18
        │   ├── Size: ~7MB (+ application binary)
        │   ├── Use Case: Need shell for debugging
        │   └── Includes basic tools
        └── distroless/static
            ├── Size: ~2MB (+ application binary)
            ├── Use Case: Production environment
            └── No shell, high security

Build Method Selection
├── Single-stage Build
│   ├── Features: Simple and direct, one FROM instruction
│   ├── Pros: Simple config, suitable for rapid prototyping
│   ├── Cons: Large image size, includes build tools
│   └── Use Cases:
│       ├── Interpreted languages (Python, Node.js) run directly
│       ├── Dev/test environments
│       └── Applications not requiring compilation
└── Multi-stage Build
    ├── Features: Multiple FROM instructions, separate build and runtime
    ├── Pros: Small image size, high security
    ├── Cons: Slightly complex configuration
    └── Use Cases:
        ├── Compiled languages (Java, Go, Rust)
        ├── Frontend apps needing build steps
        ├── Production environment deployment
        └── Recommended standard practice

Resource Limit Strategy
├── Dev Environment
│   ├── CPU Limit: No limit (or reserve 0.1-0.5 cores)
│   ├── Memory Limit: No limit (or reserve 256-512MB)
│   └── Purpose: Provide sufficient resources, convenient debugging
├── Test Environment
│   ├── CPU Limit: 0.5-1 core
│   ├── Memory Limit: 512MB-1GB
│   └── Purpose: Simulate production environment resource constraints
└── Production Environment
    ├── Application Containers
    │   ├── CPU requests: 0.5 core (guaranteed allocation)
    │   ├── CPU limits: 2 cores (maximum limit)
    │   ├── Memory requests: 512MB
    │   ├── Memory limits: 2GB
    │   └── Recommendation: Adjust based on load test results
    ├── Database Containers
    │   ├── CPU requests: 1 core
    │   ├── CPU limits: 4 cores
    │   ├── Memory requests: 2GB
    │   ├── Memory limits: 8GB
    │   └── Note: Memory limits avoid OOM
    └── Cache Containers
        ├── CPU requests: 0.5 core
        ├── CPU limits: 2 cores
        ├── Memory requests: 1GB
        ├── Memory limits: 4GB
        └── Recommendation: Redis use memory limit + maxmemory config

Log Management Strategy
├── Log Driver Selection
│   ├── json-file (default)
│   │   ├── Features: Standard JSON format, docker logs available
│   │   ├── Config: max-size, max-file
│   │   └── Use Case: Dev/test environments
│   ├── syslog
│   │   ├── Features: Forward to syslog server
│   │   ├── Use Case: Centralized log management
│   │   └── Config: syslog-address
│   ├── fluentd
│   │   ├── Features: Flexible log collection
│   │   ├── Use Case: Large-scale log aggregation
│   │   └── Config: fluentd-address, tag
│   └── none
│       ├── Features: Completely disable logging
│       ├── Use Case: High-performance scenarios, logs handled by app
│       └── Note: Cannot use docker logs command
├── Log Size Limits
│   ├── Dev Environment
│   │   ├── max-size: 100m
│   │   ├── max-file: 3
│   │   └── Total size: ~300MB
│   ├── Test Environment
│   │   ├── max-size: 50m
│   │   ├── max-file: 3
│   │   └── Total size: ~150MB
│   └── Production Environment
│       ├── max-size: 10m-50m
│       ├── max-file: 5-10
│       ├── Total size: ~50-500MB
│       └── Recommendation: Use external log collection system
└── Log Output Location
    ├── Standard Output (recommended)
    │   ├── Method: App logs to stdout/stderr
    │   ├── Pros: Complies with 12-Factor, easy to containerize
    │   └── Use Case: All containerized applications
    └── File Logs
        ├── Method: App logs write to container files
        ├── Need: Mount volumes for persistence
        ├── Cons: Complex management, doesn't follow best practices
        └── Use Case: Legacy app transition period

Network Mode Selection
├── bridge (default)
│   ├── Features: Containers communicate via virtual bridge
│   ├── Isolation: High (each container independent IP)
│   ├── Performance: Good
│   └── Use Case: Single-host multi-container communication
├── host
│   ├── Features: Container uses host network directly
│   ├── Isolation: Low (shares host network stack)
│   ├── Performance: Highest (no network conversion overhead)
│   └── Use Case: High-performance network needs, no port conflicts
├── overlay
│   ├── Features: Cross-host container communication
│   ├── Isolation: High
│   ├── Performance: Medium (tunnel encapsulation overhead)
│   └── Use Case: Docker Swarm, multi-host deployment
└── Custom Network
    ├── Features: Custom subnet, gateway, DNS
    ├── Pros: Service discovery, network isolation
    └── Use Case: Complex microservices architecture
```

## Pros and Cons Examples (✅/❌ Table)

### Dockerfile Writing Standards

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| Base Image Selection | Use ubuntu:latest as base image (~77MB base, actually 500MB+) | Java apps use eclipse-temurin:17-jre-alpine (~170MB), Go apps use alpine or scratch |
| Multi-stage Build | Single-stage build, runtime image includes Maven, JDK build tools, 800MB+ image | Use multi-stage build, build stage uses JDK, runtime stage uses JRE, image reduced to 200MB |
| User Permissions | Use root user to run app, security risk | Create non-privileged user (UID 1000), use USER instruction to switch user |
| Layer Cache Utilization | Copy all source code first, then install dependencies, every code change requires redownloading dependencies | First COPY dependency descriptor files (pom.xml/package.json), install dependencies, finally COPY source code |
| Instruction Merging | Each command separate RUN instruction, produces 20+ layers | Use && to connect related commands, merge into one RUN instruction, reduce layers |
| Health Check | Don't configure HEALTHCHECK, container zombie cannot auto-recover | Add HEALTHCHECK instruction, check app health status (like HTTP endpoint) |
| Environment Variables | Hardcode config values in Dockerfile, different environments need image rebuild | Use ENV to define defaults, override via docker run -e or docker-compose environment variables |
| .dockerignore | Don't create .dockerignore, node_modules, .git etc copied into image | Create .dockerignore, exclude unnecessary files and directories |

### Docker Compose Configuration Standards

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| Service Dependencies | Use depends_on but no health check configured, database not ready when app starts | Configure depends_on with condition: service_healthy, ensure dependent services truly available |
| Data Persistence | Database data stored in container, data lost after container deletion | Use named volume or bind mount to persist data |
| Resource Limits | No resource limits set, single container fills host resources | Use deploy.resources to set CPU and memory limits and reservations |
| Environment Variables | Plain-text database password in docker-compose.yml | Use .env file or environment variable file, sensitive info not committed to version control |
| Network Configuration | All services use default network, no isolation | Create custom networks, isolate frontend, backend, database separately |
| Restart Policy | No restart policy configured, container doesn't restart after abnormal exit | Production environment set restart: always or unless-stopped |
| Log Management | Use default log config, log files grow unlimited filling disk | Configure logging.options, set max-size and max-file to limit log size |
| Version Pinning | Use latest tag, production environment not reproducible | Use specific version number tags (like mysql:8.0.35), ensure environment consistency |

### Image Optimization

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| Package Manager Cache | Don't clean cache after apt-get install, image extra 200MB+ | Install and clean in same RUN instruction: && rm -rf /var/lib/apt/lists/* |
| Temp File Cleanup | Don't delete installation packages after download, image contains unnecessary tar.gz files | Download, extract, install, delete completed in same RUN instruction |
| File Copying | COPY . . copies entire project directory, includes test files, docs etc | Use .dockerignore to exclude unnecessary files, or explicitly specify files to copy |
| Build Artifacts | Java app copies entire target directory, includes original class files | Only copy final JAR file: COPY target/*.jar app.jar |
| Excessive Tools | Runtime image includes curl, wget, vim debug tools | Production images use distroless or slim base images, don't include extra tools |

### Security Hardening

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| Image Source | Use unknown third-party images, backdoor risk | Use official images or trusted registries, regularly scan vulnerabilities |
| Key Management | Hardcode API keys in Dockerfile, commit to Git | Use Docker secrets or environment variables, sensitive info not written into image |
| Container Permissions | Use --privileged to run container, get full host permissions | Only grant necessary capabilities, avoid privileged mode |
| Port Exposure | Expose all ports, including internal management ports | Only expose necessary service ports, management ports use internal network |
| Vulnerability Scanning | Never scan image vulnerabilities, use outdated base images | Integrate Trivy/Clair tools, auto-scan images in CI/CD |

### CI/CD Integration

| Scenario | ❌ Wrong Practice | ✅ Correct Practice |
|------|-----------|-----------|
| Build Cache | Every build redownloads dependencies and base images, takes 10+ minutes | Use BuildKit cache or CI platform caching mechanisms (cache-from/cache-to) |
| Image Tags | All builds tagged latest, cannot rollback to specific version | Use git commit SHA, version number, branch name as tags |
| Build Context | Build context includes entire repository (including .git), slow upload | Use .dockerignore to exclude unnecessary files, reduce build context |
| Parallel Build | Serial build multiple images, total time accumulates | Use matrix strategy to build multiple images in parallel (like multi-arch) |
| Failure Handling | No notification on build failure, discovered only by manual check | Configure build failure notifications (email, Slack, enterprise WeChat) |
| Test Validation | Push image directly after build, without testing | Run container tests after build, verify app starts and responds normally |

## Verification Checklist

### Dockerfile Verification

- [ ] Using appropriate base image (small size, high security)
- [ ] Adopted multi-stage build to separate build and runtime environments
- [ ] Created non-root user and switched to use
- [ ] Configured HEALTHCHECK health check
- [ ] Reasonably organized instruction order, fully utilize layer caching
- [ ] Created .dockerignore file, exclude unnecessary files
- [ ] Used ENV to define environment variables, support runtime override
- [ ] Cleaned build process temp files and cache
- [ ] Explicitly specified EXPOSE ports
- [ ] Used ENTRYPOINT or CMD to define startup command

### Image Quality Verification

- [ ] Image size within reasonable range (Java < 300MB, Node < 200MB, Go < 50MB)
- [ ] Image layers < 15 layers
- [ ] Image has no high-risk security vulnerabilities (use Trivy scan)
- [ ] Image doesn't include compilation tools and unnecessary dependencies
- [ ] Image tags clear and explicit, follow naming conventions
- [ ] Image includes necessary metadata (LABEL tags)
- [ ] Build process reproducible (fixed dependency versions)

### Container Runtime Verification

- [ ] Container can start normally, no error logs
- [ ] Application can respond to requests normally
- [ ] Health check returns normal status
- [ ] Container runs as non-root user (docker exec verify)
- [ ] Resource limits effective (CPU and memory limits)
- [ ] Logs output normally to stdout/stderr
- [ ] Environment variables correctly passed and effective
- [ ] Volume mounts correct, data persistence effective
- [ ] Container network connectivity normal
- [ ] Graceful stop mechanism effective (SIGTERM handling)

### Docker Compose Verification

- [ ] All services can start normally
- [ ] Service startup order correct (dependency relationships)
- [ ] Health checks configured correctly, dependent services ready before start
- [ ] Network configuration correct, services can access each other
- [ ] Volume mounts correct, data persistence effective
- [ ] Environment variables correctly passed
- [ ] Resource limit configuration effective
- [ ] Log configuration correct, log size limited
- [ ] Restart policy configured correctly
- [ ] Port mapping correct, accessible externally

### CI/CD Process Verification

- [ ] Code commit automatically triggers build
- [ ] Build process uses cache, reasonable build time
- [ ] Image automatically scanned for vulnerabilities, high-risk vulnerabilities block release
- [ ] Image tagged correctly (version number/SHA/branch)
- [ ] Image automatically pushed to registry
- [ ] Build artifacts traceable (includes build time, commit info etc.)
- [ ] Build failure sends notification
- [ ] Multi-arch image build normal (if needed)
- [ ] Tests pass before pushing image
- [ ] Old version images have cleanup strategy

## Guardrails and Constraints

### Image Size Limits

```
Image Size Standards
├── Java Applications
│   ├── Excellent: < 200MB
│   ├── Good: 200-300MB
│   ├── Acceptable: 300-500MB
│   └── Need Optimization: > 500MB
│       └── Check Items: Using JRE instead of JDK, cleaned Maven dependencies, using alpine base image
├── Node.js Applications
│   ├── Excellent: < 150MB
│   ├── Good: 150-200MB
│   ├── Acceptable: 200-300MB
│   └── Need Optimization: > 300MB
│       └── Check Items: Using alpine base image, only copied production dependencies, cleaned cache
├── Python Applications
│   ├── Excellent: < 100MB
│   ├── Good: 100-200MB
│   ├── Acceptable: 200-400MB
│   └── Need Optimization: > 400MB
│       └── Check Items: Using slim base image, only installed necessary dependencies, cleaned pip cache
└── Go Applications
    ├── Excellent: < 20MB
    ├── Good: 20-50MB
    ├── Acceptable: 50-100MB
    └── Need Optimization: > 100MB
        └── Check Items: Using multi-stage build, using alpine/scratch base image, enabled compilation optimization

Image Layer Limits
├── Recommended Layers: < 10 layers
├── Acceptable Layers: 10-15 layers
└── Need Optimization: > 15 layers
    └── Optimization Method: Merge RUN instructions, reduce COPY times

Image Build Time
├── First Build (no cache)
│   ├── Fast: < 2 minutes
│   ├── Normal: 2-5 minutes
│   └── Need Optimization: > 5 minutes
├── Incremental Build (with cache)
│   ├── Fast: < 30 seconds
│   ├── Normal: 30 seconds-2 minutes
│   └── Need Optimization: > 2 minutes
└── Optimization Directions:
    ├── Optimize Dockerfile layer order
    ├── Use BuildKit cache
    ├── Use registry cache
    └── Increase build machine resources
```

### Resource Quota Limits

```
CPU Quota
├── Dev Environment
│   ├── No limits (or limits: 4 cores)
│   └── Purpose: Provide sufficient resources
├── Test Environment
│   ├── requests: 0.5 core
│   ├── limits: 2 cores
│   └── Purpose: Simulate production constraints
└── Production Environment
    ├── Small Apps
    │   ├── requests: 0.5 core (guaranteed allocation)
    │   ├── limits: 2 cores (peak limit)
    │   └── Use Case: Lightweight APIs, admin backends
    ├── Medium Apps
    │   ├── requests: 1 core
    │   ├── limits: 4 cores
    │   └── Use Case: Regular business applications
    └── Large Apps
        ├── requests: 2 cores
        ├── limits: 8 cores
        └── Use Case: High-concurrency core services

Memory Quota
├── Dev Environment
│   ├── No limits (or limits: 4GB)
│   └── Purpose: Avoid OOM impacting development
├── Test Environment
│   ├── requests: 512MB
│   ├── limits: 2GB
│   └── Purpose: Discover memory leaks
└── Production Environment
    ├── Java Apps
    │   ├── requests: 1GB
    │   ├── limits: 2GB
    │   ├── JVM Heap: 75% of limits (like -Xmx1536m)
    │   └── Note: Reserve off-heap memory space
    ├── Node.js Apps
    │   ├── requests: 512MB
    │   ├── limits: 1GB
    │   ├── Node Memory Limit: --max-old-space-size=768
    │   └── Use Case: Single-process Node service
    └── Python Apps
        ├── requests: 256MB
        ├── limits: 512MB
        └── Use Case: Lightweight API service

Disk I/O Limits (Cgroups v2)
├── Read Rate: 100-500 MB/s (based on disk performance)
├── Write Rate: 50-200 MB/s
├── IOPS Limit: 1000-10000
└── Scenario: Prevent single container from filling disk I/O
```

### Security Vulnerability Thresholds

```
Vulnerability Severity Classification
├── CRITICAL (Critical)
│   ├── Definition: Can directly execute remote code, privilege escalation etc.
│   ├── Handling: Prohibit release, immediate fix
│   └── SLA: Fix within 24 hours
├── HIGH (High)
│   ├── Definition: May cause information leakage, DoS etc.
│   ├── Handling: Warning, recommend fix before release
│   └── SLA: Fix within 7 days
├── MEDIUM (Medium)
│   ├── Definition: Has some security impact, but difficult to exploit
│   ├── Handling: Record, fix periodically
│   └── SLA: Fix within 30 days
└── LOW (Low)
    ├── Definition: Theoretical security issues
    ├── Handling: Record, fix as needed
    └── SLA: No mandatory requirement

Scanning Tool Threshold Configuration
├── Trivy Scanning
│   ├── Block Release: CRITICAL count > 0
│   ├── Warning Notification: HIGH count > 5
│   └── Scan Scope: OS packages + App dependencies
├── Clair Scanning
│   ├── Block Release: Severity >= High and exploitable
│   └── Scan Scope: OS layer vulnerabilities
└── Snyk Scanning
    ├── Block Release: Severity = Critical
    ├── Warning Notification: Severity = High
    └── Scan Scope: App dependency vulnerabilities
```

### Container Runtime Constraints

```
Health Check Parameters
├── HTTP Health Check
│   ├── interval: 30s (check interval)
│   ├── timeout: 3s (timeout)
│   ├── start-period: 60s (startup grace period)
│   ├── retries: 3 (failure retry count)
│   └── Endpoint: /actuator/health or /health
├── TCP Health Check
│   ├── interval: 30s
│   ├── timeout: 3s
│   └── Use Case: Non-HTTP services
└── CMD Health Check
    ├── interval: 60s
    ├── timeout: 5s
    └── Use Case: Custom script check

Log Rotation Limits
├── Single Log File Max Size
│   ├── Dev Environment: 100MB
│   ├── Test Environment: 50MB
│   └── Production Environment: 10-20MB
├── Log File Retention Count
│   ├── Dev Environment: 3-5 files
│   ├── Test Environment: 5 files
│   └── Production Environment: 10 files
└── Total Log Space Usage
    ├── Dev Environment: ~500MB
    ├── Test Environment: ~250MB
    └── Production Environment: ~200MB (recommend external log system)

Container Restart Policy
├── no (default)
│   ├── Don't auto-restart
│   └── Use Case: One-time tasks
├── on-failure[:max-retries]
│   ├── Restart on abnormal exit
│   ├── Can set max retry count
│   └── Use Case: Tasks that may fail
├── always
│   ├── Always restart
│   ├── Will restart even after container stopped
│   └── Use Case: Long-running services
└── unless-stopped
    ├── Always restart except manual stop
    ├── Won't restart after manual stop
    └── Use Case: Production services (recommended)

Container Count Limits
├── Single-host Container Count
│   ├── Recommended: < 100
│   ├── Acceptable: 100-200
│   └── Performance Impact: > 200 (need optimization or expand nodes)
└── Docker Compose Service Count
    ├── Recommended: < 20 services
    ├── Acceptable: 20-50 services
    └── Need Split: > 50 services (consider Kubernetes)
```

## Common Issues Diagnostic Table

| Issue | Possible Cause | Diagnostic Steps | Solution |
|---------|---------|---------|---------|
| Image build fails | 1. Network issue cannot download dependencies<br>2. Dockerfile syntax error<br>3. Base image doesn't exist | 1. Check build log error info<br>2. Verify Dockerfile syntax<br>3. Test network connectivity | 1. Configure proxy or mirror acceleration<br>2. Correct Dockerfile syntax<br>3. Confirm base image name and tag |
| Image size too large | 1. Using full base image<br>2. Not cleaned build cache<br>3. Contains unnecessary files | 1. Use docker history to view layer sizes<br>2. Check if using multi-stage build<br>3. Check .dockerignore | 1. Use alpine/slim base image<br>2. Merge RUN instructions and clean cache<br>3. Complete .dockerignore file |
| Container startup fails | 1. Port conflict<br>2. Missing environment variables<br>3. Dependent services not ready | 1. docker logs view error logs<br>2. docker inspect view config<br>3. Check dependent service status | 1. Modify port mapping<br>2. Add necessary environment variables<br>3. Configure depends_on health check |
| Container repeatedly restarts | 1. Application crashes<br>2. Health check fails<br>3. Resource insufficient OOM | 1. docker logs view app logs<br>2. docker events view container events<br>3. docker stats view resource usage | 1. Fix app bug<br>2. Adjust health check parameters<br>3. Increase memory limit or optimize app |
| Container cannot access internet | 1. DNS configuration wrong<br>2. Network mode incorrect<br>3. Firewall rules blocked | 1. docker exec enter container test network<br>2. Check container network config<br>3. Check host firewall | 1. Configure correct DNS servers<br>2. Use bridge network mode<br>3. Adjust firewall rules |
| Containers cannot communicate | 1. Not in same network<br>2. Service name resolution fails<br>3. Port configuration wrong | 1. docker network inspect view network<br>2. Test service name resolution<br>3. Check port mapping | 1. Add containers to same custom network<br>2. Use service name instead of IP for access<br>3. Confirm EXPOSE and port mapping correct |
| Data not persisted | 1. Volume mount not configured<br>2. Volume path wrong<br>3. Permission issue | 1. docker inspect view volume config<br>2. Check container file path<br>3. Check file permissions | 1. Configure volume or bind mount<br>2. Correct mount path<br>3. Adjust file permissions or use correct UID |
| Logs fill disk | 1. Log rotation not configured<br>2. App logs output too much<br>3. Log driver misconfigured | 1. du -sh /var/lib/docker/containers<br>2. Check log config<br>3. Check app log level | 1. Configure max-size and max-file<br>2. Adjust app log level<br>3. Use external log collection system |
| Build cache not effective | 1. Dockerfile layer order improper<br>2. Used --no-cache parameter<br>3. Base image updated | 1. Check build output cache hit status<br>2. Analyze Dockerfile layer order<br>3. Check build parameters | 1. Place less-changing instructions first<br>2. Remove --no-cache parameter<br>3. Pin base image version |
| Container poor performance | 1. Resource limits too low<br>2. Host resources insufficient<br>3. I/O performance bottleneck | 1. docker stats view resource usage<br>2. Check host load<br>3. iotop view I/O status | 1. Increase CPU and memory quota<br>2. Expand host resources or migrate containers<br>3. Use SSD or optimize I/O |
| Image push fails | 1. Authentication failed<br>2. Network timeout<br>3. Registry capacity full | 1. docker login test auth<br>2. Check network connectivity<br>3. Check registry quota | 1. Re-login or update credentials<br>2. Configure proxy or retry<br>3. Clean old images or expand capacity |
| Health check always fails | 1. App starts slowly<br>2. Health check endpoint doesn't exist<br>3. Timeout too short | 1. Enter container manually test health check command<br>2. View app startup logs<br>3. Check health check config | 1. Increase start-period grace period<br>2. Confirm health check endpoint correct<br>3. Extend timeout time |

## Output Format Requirements

### Dockerfile Output

```dockerfile
# Multi-stage build example structure

# ============================
# Build Stage
# ============================
FROM [build base image] AS builder

WORKDIR /app

# Copy dependency descriptor files (utilize caching)
COPY [dependency files] .

# Install dependencies
RUN [install dependencies command]

# Copy source code
COPY [source directory] .

# Build application
RUN [build command]

# ============================
# Runtime Stage
# ============================
FROM [runtime base image]

# Create non-root user
RUN [create user group and user command]

WORKDIR /app

# Copy artifacts from build stage
COPY --from=builder [build artifact path] .

# Set file permissions
RUN [modify permissions command]

# Switch to non-root user
USER [username]

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD [health check command]

# Environment variables
ENV [variable name]=[default value]

# Expose ports
EXPOSE [port]

# Startup command
ENTRYPOINT [startup command]

# Description:
# - Image size: [expected size]
# - Base image selection rationale: [explanation]
# - Optimization points: [list optimization measures]
# - Security measures: [list security hardening measures]
```

### Docker Compose Output

```yaml
version: '3.8'

services:
  # ============================
  # Application Service
  # ============================
  app-service:
    build:
      context: .
      dockerfile: Dockerfile
      target: [build target]
    image: [image name:tag]
    container_name: [container name]
    ports:
      - "[host port]:[container port]"
    environment:
      - [environment variable list]
    env_file:
      - .env  # Optional, sensitive info use env file
    volumes:
      - [volume mount config]
    depends_on:
      [dependent service]:
        condition: service_healthy
    networks:
      - [network name]
    restart: [restart policy]
    deploy:
      resources:
        limits:
          cpus: '[CPU limit]'
          memory: [memory limit]
        reservations:
          cpus: '[CPU reservation]'
          memory: [memory reservation]
    healthcheck:
      test: [health check command]
      interval: 30s
      timeout: 3s
      retries: 3
      start_period: 60s
    logging:
      driver: "json-file"
      options:
        max-size: "[log file size]"
        max-file: "[log file count]"

  # ============================
  # Database Service
  # ============================
  database:
    image: [database image:version]
    container_name: [container name]
    ports:
      - "[port mapping]"
    environment:
      - [database config]
    volumes:
      - [data persistence volume]
      - [init script mount]
    networks:
      - [network name]
    restart: [restart policy]
    healthcheck:
      test: [health check command]
      interval: 10s
      timeout: 5s
      retries: 5
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  # Other services...

# ============================
# Volume Definitions
# ============================
volumes:
  [volume name]:
    driver: local
    # Optional: driver_opts config

# ============================
# Network Definitions
# ============================
networks:
  [network name]:
    driver: bridge
    # Optional: custom subnet config

# Description:
# - Service dependencies: [description]
# - Network isolation strategy: [description]
# - Data persistence solution: [description]
# - Resource quota explanation: [description]
```

### Image Optimization Report Output

```markdown
# Docker Image Optimization Report

## 1. Pre-Optimization Status
- Image Size: [XXX MB]
- Image Layers: [XX layers]
- Build Time: [X minutes]
- Security Vulnerabilities: [CRITICAL: X, HIGH: X, MEDIUM: X, LOW: X]

## 2. Optimization Measures
### Measure 1: [Optimization item name]
- Problem Description: [Explanation]
- Optimization Method: [Explanation]
- Expected Benefit: [Reduce XX MB / Reduce X layers / Improve X% speed]

### Measure 2: [Optimization item name]
- Problem Description: [Explanation]
- Optimization Method: [Explanation]
- Expected Benefit: [Explanation]

[More measures...]

## 3. Post-Optimization Status
- Image Size: [XXX MB] ↓ [Reduction percentage]
- Image Layers: [XX layers] ↓ [Reduction count]
- Build Time: [X minutes] ↓ [Reduction percentage]
- Security Vulnerabilities: [CRITICAL: 0, HIGH: X, MEDIUM: X, LOW: X]

## 4. Comparison Analysis
| Metric | Pre-Optimization | Post-Optimization | Improvement |
|------|--------|--------|----------|
| Image Size | [Value] | [Value] | [Percentage] |
| Image Layers | [Value] | [Value] | [Reduction count] |
| Build Time (First) | [Value] | [Value] | [Percentage] |
| Build Time (Cached) | [Value] | [Value] | [Percentage] |
| Critical Vulnerabilities | [Value] | [Value] | [Reduction count] |

## 5. Follow-up Recommendations
- [Recommendation 1]
- [Recommendation 2]
- [Recommendation 3]

## 6. Risk Warnings
- [Possible risks or considerations]
```

### CI/CD Configuration Output

```yaml
# Example: GitHub Actions configuration structure

name: [Workflow name]

on:
  push:
    branches: [Branch list]
    tags: [Tag pattern]
  pull_request:
    branches: [Branch list]

env:
  REGISTRY: [Image registry address]
  IMAGE_NAME: [Image name]

jobs:
  # ============================
  # Build and Test
  # ============================
  build-and-test:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Login to registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: [username]
          password: [password/Token]

      - name: Extract image metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            [tag generation rules]

      - name: Build image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: false
          load: true
          tags: ${{ steps.meta.outputs.tags }}
          cache-from: type=gha
          cache-to: type=gha,mode=max
          build-args: |
            [build arguments]

      - name: Run container tests
        run: |
          [test commands]

      - name: Scan image vulnerabilities
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: [image reference]
          exit-code: '[block or not]'
          severity: 'CRITICAL,HIGH'

      - name: Push image
        if: [push condition]
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}

      - name: Notify build result
        if: failure()
        run: |
          [notification command]

# Description:
# - Trigger conditions: [Explanation]
# - Build environment: [Explanation]
# - Caching strategy: [Explanation]
# - Quality checks: [Explanation]
# - Deployment process: [Explanation]
```
