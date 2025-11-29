---
inclusion: manual
---

# Exception Handling Best Practices

This document details best practices for exception design, inheritance hierarchy, and handling in Java/Spring Boot applications.

## Core Principles

### 1. Exception Inheritance Hierarchy Principle

**You MUST follow**:
- ✅ Use inheritance hierarchy to organize exception classes
- ✅ Handle similar exceptions uniformly through parent classes
- ✅ Exception classes MUST contain code and message fields
- ✅ Use type-safe error code enums (ErrorCode)
- ❌ DO NOT use `catch (ExceptionA | ExceptionB | ExceptionC)` pattern
- ❌ DO NOT let each exception directly extend the base class
- ❌ DO NOT use string constants for error codes (use enums instead)

**Why this is a best practice**:
- Follows OOP design principles (Open-Closed Principle, Liskov Substitution Principle)
- Code is cleaner and easier to maintain
- Adding new exceptions doesn't require modifying catch blocks
- Facilitates unified handling of similar exceptions
- Standardizes exception information for frontend processing
- Type-safe, compile-time checking, avoids typos

### 2. Exception Classification Principle

**Classification you MUST follow**:

```
BaseException (Top-level base class)
├── BusinessException (Business exceptions)
│   ├── Authentication-related exceptions
│   ├── Resource conflict exceptions
│   ├── Resource not found exceptions
│   └── Other business exceptions...
├── ParameterException (Parameter exceptions)
│   └── With validationErrors list
└── SystemException (System exceptions)
    ├── Database exceptions
    └── External service exceptions
```

## Error Code Enum Best Practices

### 1. Use ErrorCode Interface

**Design Principles**:
- ✅ Define unified ErrorCode interface
- ✅ All error code enums implement this interface
- ✅ Interface includes `getCode()` and `getMessage()` methods
- ✅ Error code is String type (e.g., "AUTH_001")
- ✅ Message is default prompt text

**File Location**: `common/src/main/java/.../enums/ErrorCode.java`

### 2. Error Code Enum Classification

**Classification you MUST follow**:

| Enum Class | Purpose | HTTP Status | Prefix |
|------------|---------|-------------|--------|
| `AuthErrorCode` | Authentication errors | 401 | AUTH_ |
| `ParamErrorCode` | Parameter validation errors | 400 | PARAM_ |
| `ResourceErrorCode` | Resource errors | 404/409/423 | NOT_FOUND_/CONFLICT_/LOCKED_ |
| `SystemErrorCode` | System errors | 500 | SYS_ |

**Naming Conventions**:
- Enum constant name: UPPERCASE_WITH_UNDERSCORES (INVALID_CREDENTIALS)
- Error code string: CATEGORY_SEQUENCE (AUTH_001)
- Each enum value must have clear JavaDoc
- Must specify use cases and HTTP status code
- Parameterized messages must document parameter meanings ({0} = remaining minutes)

### 3. Mixed Mode for Constructing Exceptions

**Four modes you MUST support**:

**Mode 1: Use Enum Default Message**
- Use case: Standard errors, no custom message needed
- Advantage: Most concise, unified message management
- Example: `throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS)`

**Mode 2: Enum + Custom Message**
- Use case: Need to override default message
- Advantage: Unified error code, customizable message
- Example: `throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS, "Account temporarily locked after 5 password errors")`

**Mode 3: Enum + Parameterized Message**
- Use case: Message contains dynamic content (numbers, names, etc.)
- Advantage: Unified message template, auto-formatting
- Uses MessageFormat.format() implementation
- Message template uses {0}, {1}, {2} placeholders
- Example: `throw new BusinessException(ResourceErrorCode.ACCOUNT_LOCKED, 30)`
  → "Account locked, please try again in 30 minutes"

**Mode 4: Traditional String-based (Backward Compatible)**
- Use case: Legacy code, gradual migration
- Not recommended for new code
- Example: `throw new BusinessException("AUTH_001", "Invalid username or password")`

## Base Exception Design

### BaseException (Top-level parent of all exceptions)

**File Location**: `common/src/main/java/.../exception/BaseException.java`

**Fields you MUST include**:
- `errorCode` (String, final) - Error code
- `errorMessage` (String, final) - Error message

**Constructors you MUST provide**:
- `BaseException(ErrorCode errorCode)` - Enum + default message
- `BaseException(ErrorCode errorCode, String customMessage)` - Enum + custom message
- `BaseException(ErrorCode errorCode, Object... args)` - Enum + parameterized message
- `BaseException(String errorCode, String errorMessage)` - Traditional (compatible)
- Throwable cause overloads for all above methods

**Design Points**:
- ✅ Extend RuntimeException (unchecked exception)
- ✅ Make fields final (immutable)
- ✅ Use @Getter annotation
- ✅ Provide formatMessage() utility method (uses MessageFormat)
- ✅ Return original template on format failure (fault tolerance)

### BusinessException (Business exceptions)

**File Location**: `common/src/main/java/.../exception/BusinessException.java`

**Design Points**:
- Extends BaseException
- Provides same constructor overloads as BaseException
- For predictable business errors
- Log at WARN level

**Use Cases**:
- ✅ Authentication failure (wrong username/password)
- ✅ Resource conflict (username already exists)
- ✅ Resource not found (account not found)
- ✅ Account locked, session expired
- ❌ DO NOT use for system-level errors

### ParameterException (Parameter exceptions)

**File Location**: `common/src/main/java/.../exception/ParameterException.java`

**Special Field**:
- `validationErrors` (List<String>) - Validation error details list

**Constructor Requirements**:
- Supports all ErrorCode enum modes
- Supports passing validationErrors list
- Uses empty list when no validation errors

**Use Cases**:
- ✅ Password strength requirements not met (with detailed error list)
- ✅ Email/phone format error
- ✅ Parameter length exceeds limit
- ✅ Enum value not in allowed range

### SystemException (System exceptions)

**File Location**: `common/src/main/java/.../exception/SystemException.java`

**Design Points**:
- Extends BaseException
- Provides same constructor overloads as BaseException
- For unpredictable system errors
- Log at ERROR level
- Return generic error message to user (don't expose internal details)

**Use Cases**:
- ✅ Database connection failure
- ✅ Redis/MQ connection failure
- ✅ Third-party API timeout
- ❌ DO NOT use for business rule validation failures

## Global Exception Handler Design

### Core Principles

**1. Unified Handling Using Inheritance**
- ✅ Catch parent class to handle all subclasses automatically
- ✅ Reduce code duplication
- ✅ New exceptions automatically handled
- ❌ DO NOT use OR combinations of multiple exception types

**2. Dynamic HTTP Status Code Mapping**
- Auto-determine HTTP status code from error code prefix
- AUTH_ → 401, AUTHZ_ → 403, PARAM_ → 400
- NOT_FOUND_ → 404, CONFLICT_ → 409, LOCKED_ → 423
- SYS_ → 500, other business exceptions → 200

**3. Minimize Handler Count**
- This project needs only 4 handlers:
  - `ParameterException` - Returns validationErrors list
  - `MethodArgumentNotValidException` - Spring Validation
  - `BusinessException` - Dynamic status code
  - `SystemException` + `Exception` - System exceptions and fallback

### Exception Handler Responsibilities

**SHOULD do**:
- ✅ Catch all unhandled exceptions
- ✅ Log at appropriate levels
- ✅ Convert exceptions to unified response format
- ✅ Map exceptions to appropriate HTTP status codes
- ✅ Protect sensitive information (don't expose internal details)

**SHOULD NOT do**:
- ❌ Implement business logic
- ❌ Call external services
- ❌ Modify database
- ❌ Expose stack traces to users

### Log Level Rules

| Exception Type | Log Level | Reason |
|---------------|-----------|--------|
| BusinessException | WARN | Predictable business errors |
| ParameterException | WARN | Client input errors |
| SystemException | ERROR | System-level errors, need ops attention |
| Exception (unknown) | ERROR | Unexpected errors, need urgent handling |

### HTTP Status Code Mapping Rules

| Error Code Prefix | HTTP Status Code | Description |
|------------------|------------------|-------------|
| AUTH_ | 401 Unauthorized | Authentication failed |
| AUTHZ_ | 403 Forbidden | Insufficient permissions |
| PARAM_ | 400 Bad Request | Parameter error |
| NOT_FOUND_ | 404 Not Found | Resource not found |
| CONFLICT_ | 409 Conflict | Resource conflict |
| LOCKED_ | 423 Locked | Resource locked |
| BIZ_/Others | 200 OK | Business exception (differentiate by code) |
| SYS_ | 500 Internal Server Error | System exception |

## Error Code Design Standards

### Error Code Format

**String Format** (for exception classes):
- Format: `{CATEGORY}_{SEQUENCE}`
- Examples: `AUTH_001`, `PARAM_001`, `NOT_FOUND_001`

**Integer Format** (for HTTP response):
- Format: `{HTTP_STATUS_CODE}{3_DIGIT_SEQUENCE}`
- Conversion: AUTH_001 → 401001, PARAM_001 → 400001
- Examples: `401001`, `400001`, `404001`

### Error Code Category Table

| Category Prefix | HTTP Code Range | Description |
|----------------|----------------|-------------|
| AUTH_ | 401001-401999 | Authentication errors |
| AUTHZ_ | 403001-403999 | Authorization errors |
| PARAM_ | 400001-400999 | Parameter validation errors |
| NOT_FOUND_ | 404001-404999 | Resource not found |
| CONFLICT_ | 409001-409999 | Resource conflict |
| LOCKED_ | 423001-423999 | Resource locked |
| BIZ_ | 200001-200999 | Business exceptions |
| SYS_ | 500001-500999 | System exceptions |

### Error Code Management Recommendations

**Deprecated Methods** (marked @Deprecated):
- ❌ ErrorCodes string constants class
- ❌ Hardcoded error code strings
- Only for backward compatibility, prohibited in new code

**Recommended Methods**:
- ✅ Use ErrorCode enums
- ✅ Each enum value includes code and message
- ✅ Centrally manage related errors in enum classes
- ✅ Provide clear JavaDoc documentation

## Exception Usage Best Practices

### Exception Throwing Rules

**In Domain Layer**:
- ✅ Throw domain exceptions directly
- ✅ Use error code enums
- ✅ Prefer default messages
- ✅ Use parameterized messages for dynamic content
- ✅ Provide detailed error list for validation failures
- ❌ DO NOT catch and rethrow

**In Application Layer**:
- ✅ Throw exceptions directly, don't catch
- ✅ Let exceptions propagate automatically to GlobalExceptionHandler
- ❌ DO NOT use try-catch in main flow
- ❌ DO NOT catch just for logging

**In Repository Layer**:
- ✅ Convert underlying exceptions to domain exceptions
- ✅ Preserve original exception as cause
- ✅ Add business context information
- Example: DuplicateKeyException → BusinessException(ResourceErrorCode.USERNAME_CONFLICT)

**In Infrastructure Layer (JWT, Redis, HTTP Client, etc.)**:
- ✅ MUST catch third-party library exceptions and convert to business exceptions
- ✅ Use parent class to catch similar exceptions uniformly (e.g., JwtException)
- ✅ Preserve original exception as cause
- ✅ Distinguish expected exceptions (WARN) from system exceptions (ERROR)
- ❌ DO NOT directly throw third-party library exceptions (e.g., ExpiredJwtException, RedisConnectionFailureException)

### Exception Handling Rules

**Scenarios that NEED catch**:
- ✅ Convert exception type (SQLException → SystemException)
- ✅ Add context information
- ✅ Release resources (prefer try-with-resources)
- ✅ Fallback handling (use cache when third-party service fails)

**Scenarios that SHOULD NOT catch**:
- ❌ Only for logging
- ❌ Catch and rethrow unchanged
- ❌ Catch and do nothing (swallow exceptions)
- ❌ Catch and lose original exception

**After catching MUST**:
- ✅ Log with key context
- ✅ Preserve original exception (pass cause)
- ✅ Provide meaningful error message
- ❌ DO NOT expose sensitive information

## Common Usage Patterns

### Pattern 1: Resource Not Found
- Use Optional.orElseThrow()
- Use ResourceErrorCode.ACCOUNT_NOT_FOUND enum, etc.
- HTTP 404 status code

### Pattern 2: Parameter Validation Failure
- Collect all validation errors
- Use ParameterException + validationErrors list
- Use ParamErrorCode enum
- HTTP 400 status code

### Pattern 3: Business Rule Check
- Throw directly after conditional check
- Use corresponding business exception enum
- Use parameterized message for dynamic content
- Auto-map HTTP status code from error code prefix

### Pattern 4: Database Exception Wrapping
- Catch DataAccessException
- Check specific error type (unique constraint, foreign key, etc.)
- Convert to appropriate BusinessException or SystemException
- Preserve original exception as cause

## Migration Guide

### From String Constants to Enums

**Migration Steps**:
1. Find places using ErrorCodes constants
2. Import corresponding ErrorCode enum
3. Replace string constants with enum values
4. Remove String message parameter (if using default message)
5. Change dynamic content to use parameterized messages

**Migration Benefits**:
- Compile-time type checking
- IDE smart suggestions and refactoring support
- Unified message template management
- More concise code

### Backward Compatibility

- Keep all String-based constructors
- ErrorCodes class marked as @Deprecated
- Old code continues to work, no breaking changes
- New code enforces enum usage

## Frontend Integration Standards

### Unified Response Format

**Required fields in ApiResponse**:
- `code` (Integer) - Response code, 0 = success, non-zero = error code
- `message` (String) - Response message
- `data` (T) - Response data (generic)

**Frontend Handling Rules**:
- Check code === 0 for success
- Display corresponding error message based on code
- Special error codes (e.g., 423001) can trigger special handling
- HTTP errors (network issues) show generic message

### Error Information Security

**You MUST follow**:
- ✅ Business exceptions return user-friendly messages
- ✅ System exceptions return generic error messages
- ❌ DO NOT expose database structure
- ❌ DO NOT expose internal paths
- ❌ DO NOT expose tech stack details
- ❌ DO NOT return stack traces to frontend

## Checklist

### Code Review Verification

**Exception Definition**:
- [ ] All business exceptions extend BusinessException
- [ ] Parameter exceptions extend ParameterException
- [ ] System exceptions extend SystemException
- [ ] Don't directly extend BaseException
- [ ] Use ErrorCode enums instead of string constants
- [ ] Enum values have complete JavaDoc

**Exception Usage**:
- [ ] Domain layer throws exceptions directly
- [ ] Application layer doesn't use try-catch in main flow
- [ ] Preserve original exception after catch (pass cause)
- [ ] Don't swallow exceptions
- [ ] Use enum default messages or parameterized messages
- [ ] Use parameters instead of string concatenation for dynamic content

**Exception Handler**:
- [ ] Uses @RestControllerAdvice
- [ ] Handles similar exceptions uniformly through parent class
- [ ] Logs at appropriate levels
- [ ] Returns unified ApiResponse format
- [ ] Doesn't expose sensitive information
- [ ] System exceptions return generic messages
- [ ] HTTP status code mapping is correct

**Error Code Management**:
- [ ] Error codes use type-safe enums
- [ ] Naming follows CATEGORY_SEQUENCE format
- [ ] No duplicate error codes
- [ ] Enums classified by function (Auth/Param/Resource/System)
- [ ] Parameterized messages document parameter meanings

## Best Practices Summary

**Type Safety**:
- Use ErrorCode enums instead of string constants
- Compile-time checking, avoid typos
- IDE smart suggestions and refactoring support

**Simple and Easy to Use**:
- Supports 4 usage modes (default message, custom, parameterized, compatible)
- Prefer default messages
- Parameterized messages auto-format

**Unified Standards**:
- Clear exception inheritance hierarchy
- Unified error response format
- Standardized error code management
- Automatic HTTP status code mapping

**Secure and Reliable**:
- Protect system implementation details
- Distinguish user-friendly messages from technical logs
- Preserve complete exception chain
- Backward compatible

Following these principles ensures consistency, maintainability, and user experience in exception handling.

## Infrastructure Layer Exception Handling Rules

### General Rules

**You MUST follow**:
- ✅ Catch third-party library exceptions and convert to BusinessException or SystemException
- ✅ Use parent class to catch similar exceptions uniformly (e.g., JwtException catches all JWT-related exceptions)
- ✅ Preserve original exception as cause: `throw new BusinessException(ErrorCode.XXX, e)`
- ✅ Place exceptions needing separate handling before parent class catch
- ❌ DO NOT directly rethrow third-party library exceptions

### JWT Token Exception Handling

**Exception Conversion Rules**:
| Third-party Exception | Convert To | ErrorCode |
|----------------------|------------|-----------|
| ExpiredJwtException | BusinessException | TOKEN_EXPIRED |
| JwtException (parent, catches others) | BusinessException | TOKEN_INVALID |
| IllegalArgumentException | BusinessException | TOKEN_INVALID |

**Handling Order**: Catch ExpiredJwtException first (needs separate handling), then use JwtException parent to catch the rest.

### Redis Cache Exception Handling

**Fallback Mode Rules**:
- Redis exceptions should NOT block the main flow
- After catching, log WARN and fallback to database query
- Common exceptions: RedisConnectionFailureException, RedisCommandTimeoutException

### HTTP Client Exception Handling

**Exception Classification Rules**:
| Third-party Exception | Convert To | Description |
|----------------------|------------|-------------|
| HttpClientErrorException (4xx) | BusinessException | Client/request error |
| HttpServerErrorException (5xx) | SystemException | Server error |
| ResourceAccessException | SystemException | Network connection failure |

### Database Exception Handling

**Exception Conversion Rules**:
| Third-party Exception | Convert To | ErrorCode |
|----------------------|------------|-----------|
| DuplicateKeyException | BusinessException | USERNAME_CONFLICT / EMAIL_CONFLICT |
| DataAccessException (parent) | SystemException | DATABASE_ERROR |

**Unique Constraint Conflict Handling**: Determine which field caused the conflict based on exception message, convert to corresponding business error code.

## Infrastructure Layer Exception Handling Checklist

**Code Review Verification**:

**Exception Handling**:
- [ ] Third-party library exceptions converted to BusinessException or SystemException
- [ ] Using ErrorCode enums, not string constants
- [ ] Original exception preserved as cause (passed to exception constructor)
- [ ] Using parent class to catch similar exceptions uniformly (e.g., JwtException, DataAccessException)
- [ ] Fallback operations (like caching) don't block main flow

**Logging Standards** (see `06-spring-boot-best-practices.en.md` Logging Standards section):
- [ ] Log levels correct: business errors use WARN, system errors use ERROR
- [ ] Exception logs include full stack trace (exception object as last parameter)
- [ ] Logs include key context information (userId, sessionId, etc.)
- [ ] No sensitive information logged (passwords, full tokens, etc.)
