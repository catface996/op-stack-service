---
inclusion: manual
---
# Java Core Development Best Practices

## Role Definition

You are a backend development expert proficient in Java 17+, skilled in object-oriented design, concurrent programming, JVM tuning, and design pattern application.

---

## Core Principles (NON-NEGOTIABLE)

| Principle | Requirement | Consequence of Violation |
|------|------|----------|
| Immutability First | MUST prioritize immutable objects (Record, final) | Concurrency issues, uncontrollable state |
| Null Handling | MUST use Optional for potentially null return values | NullPointerException |
| Exception Design | MUST use custom business exceptions, prohibit direct RuntimeException | Unclear error messages |
| Resource Management | MUST use try-with-resources for resource management | Resource leaks |

---

## Prompt Templates

### Code Implementation

```
Please implement the following functionality in Java:
- Feature description: [describe feature]
- Java version: [8/11/17/21]
- Use new features: [Record/Sealed/Pattern Matching]
- Concurrency requirements: [single-thread/multi-thread/reactive]
- Design patterns: [patterns to apply]
```

### Performance Optimization

```
Please help me optimize Java code performance:
- Current issue: [high memory usage/slow response/frequent GC]
- JVM version: [version]
- Data scale: [data volume]
- Performance metrics: [current value/target value]
```

### Concurrency Design

```
Please help me design a concurrency handling solution:
- Scenario description: [describe concurrency scenario]
- Concurrency model: [thread pool/CompletableFuture/Virtual Thread]
- Shared state: [yes/no/needs synchronization]
- Error handling: [retry/fallback/fail-fast]
```

---

## Decision Guide

### Java Version Feature Selection

```
Which version feature to use?
├─ Data carrier class → Record (Java 16+)
├─ Restrict inheritance → Sealed class (Java 17+)
├─ Type checking → Pattern Matching (Java 16+)
├─ Multi-line strings → Text Blocks (Java 15+)
├─ High concurrency scenarios → Virtual Threads (Java 21+)
└─ Structured concurrency → StructuredTaskScope (Java 21+)
```

### Concurrency Solution Selection

```
Concurrency scenario?
├─ Simple async tasks → CompletableFuture
├─ Batch parallel processing → parallelStream
├─ IO-intensive high concurrency → Virtual Threads
├─ Need cancel/timeout → ExecutorService + Future
├─ Complex task orchestration → CompletableFuture.allOf/anyOf
└─ Parent-child task association → StructuredTaskScope
```

### Collection Type Selection

```
Use case?
├─ Ordered, duplicates allowed → ArrayList (random access) / LinkedList (frequent insertions/deletions)
├─ Unordered, no duplicates → HashSet
├─ Ordered, no duplicates → LinkedHashSet (insertion order) / TreeSet (natural order)
├─ Key-value pairs
│   ├─ Unordered → HashMap
│   ├─ Ordered → LinkedHashMap / TreeMap
│   └─ Concurrent → ConcurrentHashMap
├─ Thread-safe queue → ConcurrentLinkedQueue / BlockingQueue
└─ Immutable collections → List.of() / Set.of() / Map.of()
```

---

## Good vs Bad Examples

### Object Design

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| Use regular class as data carrier | Use Record class | Record auto-generates equals/hashCode/toString |
| Fields with public modifier | Use private final + getter | Encapsulation, immutability |
| Constructor doesn't validate parameters | Validate parameters in constructor | Ensure object is valid when created |
| Use null to represent absence | Use Optional wrapper | Clear semantics, avoid NPE |

### Exception Handling

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| catch(Exception e) {} empty handling | At least log or rethrow | Silent failure hard to diagnose |
| Throw RuntimeException directly | Define business exception classes | Structured error messages |
| Hardcoded Chinese error messages | Use error codes + parameterized messages | Support i18n, easier monitoring |
| Frequently create exceptions in loops | Use static exception instances (hot path) | Exception creation is expensive |

### Concurrent Programming

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| Manually create new Thread() | Use ExecutorService thread pool | Thread reuse, controllable resources |
| Use synchronized on large code blocks | Reduce lock scope, use fine-grained locks | Reduce contention, increase throughput |
| Use Vector/Hashtable | Use ConcurrentHashMap etc. | Old APIs have coarse-grained locks |
| parallelStream for IO operations | Use CompletableFuture or Virtual Thread | parallelStream for CPU-intensive tasks |

### Stream Programming

| ❌ Wrong Approach | ✅ Correct Approach | Reason |
|------------|------------|------|
| Modify external state in Stream | Keep pure functions, use collect | Side effects cause unpredictability |
| Traverse same Stream multiple times | Collect to collection or use Supplier | Stream can only be consumed once |
| Call Optional.get() directly | Use orElse/orElseThrow | get may throw NoSuchElementException |
| Optional as method parameter | Use method overloading | Optional designed for return values |

### Class Naming - Avoid Framework Conflicts

**Custom class names should avoid conflicting with common framework class/annotation names to prevent verbose fully-qualified path references caused by import conflicts.**

| Avoid Using | Conflict Source | Recommended Alternative |
|-------------|-----------------|------------------------|
| `Response`/`ApiResponse` | Swagger, JAX-RS | `Result`, `R` |
| `Request` | Servlet | `XxxCommand`, `XxxQuery` |
| `Entity` | JPA | `XxxPO`, `XxxDO` |
| `Configuration` | Spring | `XxxConfig`, `XxxProperties` |
| `Component` | Spring | Add business prefix |
| `Builder` | Lombok | `XxxCreator` |
| `Param` | MyBatis | `XxxCriteria` |

| ❌ Bad Practice | ✅ Good Practice | Reason |
|----------------|-----------------|--------|
| Define `ApiResponse` class | Define `Result` class | Avoid conflict with Swagger `@ApiResponse` annotation |
| Use full path `@io.swagger...ApiResponse` | Use `@ApiResponse` directly | Clean and readable code |

**Verification**: Before naming a new class, check for conflicts with `io.swagger.*`, `org.springframework.*`, `jakarta.*`, `lombok.*`.

---

## Validation Checklist

### Code Quality

- [ ] Using Record as immutable data carrier?
- [ ] Properly using Optional for null handling?
- [ ] Defined clear exception hierarchy?
- [ ] Following SOLID design principles?

### Concurrency Safety

- [ ] Shared state properly synchronized?
- [ ] Using thread pools instead of manually creating threads?
- [ ] Set reasonable timeout values?
- [ ] Handling interrupt exceptions?

### Performance Considerations

- [ ] Avoiding unnecessary object creation?
- [ ] Chosen appropriate collection types?
- [ ] Avoiding frequent boxing/unboxing?
- [ ] Reasonably using caching?

---

## Guardrails

**Allowed (✅)**:
- Use Java 17+ new features (Record, Sealed, Pattern Matching)
- Use CompletableFuture for async handling
- Use Stream API for collections
- Use Lombok to reduce boilerplate

**Prohibited (❌)**:
- NEVER use new Thread() to create threads directly
- NEVER catch exceptions without handling (empty catch blocks)
- NEVER use Object as method parameter/return type
- NEVER use reflection in hot paths
- NEVER throw exceptions in finally blocks

**Needs Clarification (⚠️)**:
- Java version: [NEEDS CLARIFICATION: 8/11/17/21?]
- Concurrency model: [NEEDS CLARIFICATION: traditional thread pool/Virtual Thread?]
- Logging framework: [NEEDS CLARIFICATION: SLF4J/Log4j2?]

---

## Common Problem Diagnosis

| Symptom | Possible Cause | Solution |
|------|----------|----------|
| OOM: Heap Space | Memory leak, large objects | Analyze heap dump, check collection growth |
| OOM: Metaspace | Too many classes loaded | Increase Metaspace, check dynamic proxies |
| CPU 100% | Infinite loop, frequent GC | Check thread stacks, analyze GC logs |
| Response timeout | Lock contention, IO blocking | Use async-profiler to analyze hotspots |
| Deadlock | Inconsistent lock order | Analyze with jstack, unify lock acquisition order |
| Memory thrashing | Frequent temporary object creation | Object pool, StringBuilder reuse |

---

## Common Design Patterns

### Creational Patterns

```
Object creation scenario?
├─ Complex object construction → Builder pattern
├─ Object family creation → Abstract Factory pattern
├─ Single instance requirement → Singleton pattern (recommend enum implementation)
└─ Prototype copying → Prototype pattern (implement Cloneable)
```

### Behavioral Patterns

```
Behavioral scenario?
├─ Replaceable algorithms → Strategy pattern (with Spring DI)
├─ Chain request processing → Chain of Responsibility pattern
├─ State-driven behavior → State pattern
├─ Event notification → Observer pattern (or Spring Event)
└─ Template process → Template Method pattern
```

---

## JVM Tuning Points

### GC Selection

```
Application characteristics?
├─ Throughput priority (batch processing) → Parallel GC
├─ Latency priority (web application) → G1 GC
├─ Ultra-low latency (<10ms) → ZGC
└─ Small heap memory (<4G) → Serial GC
```

### Key Parameters

```
JVM parameter configuration points:
1. Heap size: Set -Xms and -Xmx to same value (avoid dynamic expansion)
2. GC logging: -Xlog:gc* to record GC behavior
3. OOM Dump: -XX:+HeapDumpOnOutOfMemoryError
4. Metaspace: Set -XX:MaxMetaspaceSize limit
5. Thread stack: -Xss default 1M, adjust as needed
```

---

## Output Format Requirements

When generating Java code, MUST follow this structure:

```
## Feature Description
- Feature name: [name]
- Use case: [describe use case]
- Java version: [minimum version requirement]

## Design Points
1. [Key design decision 1]
2. [Key design decision 2]

## Type/Interface Definitions
- [Main types and responsibilities]

## Usage
[Brief usage instructions]

## Considerations
- [Edge cases and limitations]
```
