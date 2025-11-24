package com.catface996.aiops.interface_.http.exception;

import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.common.exception.ParameterException;
import com.catface996.aiops.common.exception.SystemException;
import com.catface996.aiops.interface_.http.response.ApiResponse;
import com.catface996.aiops.interface_.http.response.ErrorDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局异常处理器（优化版）
 *
 * <p>统一处理应用中的所有异常，将异常映射到标准的HTTP响应。</p>
 *
 * <p>核心设计理念：</p>
 * <ul>
 *   <li>利用异常继承体系，通过父类统一处理同类异常</li>
 *   <li>根据错误码前缀自动判断 HTTP 状态码，无需为每个异常单独写处理器</li>
 *   <li>只为有特殊数据结构的异常提供专门的处理器</li>
 * </ul>
 *
 * <p>异常继承体系：</p>
 * <pre>
 * BaseException
 * ├── BusinessException（根据错误码动态返回状态码）
 * ├── ParameterException（400，有专门的处理器）
 * └── SystemException（500）
 * </pre>
 *
 * <p>所有自定义异常类已移除，统一使用通用异常 + 错误码：</p>
 * <ul>
 *   <li>认证失败、会话过期等 → BusinessException(ErrorCodes.AUTH_XXX, message)</li>
 *   <li>账号不存在、锁定等 → BusinessException(ErrorCodes.XXX, message)</li>
 *   <li>参数验证失败 → ParameterException(ErrorCodes.PARAM_XXX, message, validationErrors)</li>
 * </ul>
 *
 * <p>处理器列表（共4个）：</p>
 * <ol>
 *   <li>ParameterException - 需要返回 validationErrors 列表</li>
 *   <li>MethodArgumentNotValidException - Spring Validation 异常</li>
 *   <li>BusinessException - 统一处理所有业务异常（根据 errorCode 动态判断状态码）</li>
 *   <li>SystemException + Exception - 系统异常和兜底</li>
 * </ol>
 *
 * <p>状态码映射规则（自动判断）：</p>
 * <ul>
 *   <li>AUTH_xxx → 401 Unauthorized（认证错误）</li>
 *   <li>AUTHZ_xxx → 403 Forbidden（授权错误）</li>
 *   <li>PARAM_xxx → 400 Bad Request（参数错误）</li>
 *   <li>NOT_FOUND_xxx → 404 Not Found（资源不存在）</li>
 *   <li>CONFLICT_xxx → 409 Conflict（资源冲突）</li>
 *   <li>LOCKED_xxx → 423 Locked（资源被锁定）</li>
 *   <li>SYS_xxx → 500 Internal Server Error（系统错误）</li>
 *   <li>其他 → 200 OK（业务异常，通过响应体中的code区分）</li>
 * </ul>
 *
 * <p>错误码格式：</p>
 * <ul>
 *   <li>String格式：AUTH_001, PARAM_001, NOT_FOUND_001 等（便于代码维护）</li>
 *   <li>Integer格式：401001, 400001, 404001 等（便于前端处理）</li>
 *   <li>自动转换：AUTH_001 → 401001</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-11-24
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 参数相关异常 (400) ====================

    /**
     * 统一处理所有参数异常
     *
     * <p>包括：ParameterException 及其所有子类</p>
     * <ul>
     *   <li>InvalidPasswordException - 密码不符合要求</li>
     * </ul>
     *
     * @param e 参数异常
     * @return 400 错误响应
     */
    @ExceptionHandler(ParameterException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleParameterException(ParameterException e) {
        log.warn("[全局异常处理] 参数异常: code={}, message={}, errors={}",
                e.getErrorCode(), e.getMessage(), e.getValidationErrors());

        Integer httpErrorCode = parseHttpErrorCode(e.getErrorCode());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(httpErrorCode, e.getMessage(), e.getValidationErrors()));
    }

    /**
     * 处理 Spring Validation 异常
     *
     * @param e 参数验证异常
     * @return 400 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ErrorDetail>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.warn("[全局异常处理] Spring参数验证失败: {} 个字段错误", e.getBindingResult().getFieldErrorCount());

        List<ErrorDetail> errors = new ArrayList<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.add(ErrorDetail.of(fieldError.getField(), fieldError.getDefaultMessage()));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400002, "请求参数无效", errors));
    }

    // ==================== 业务异常（根据错误码动态返回状态码） ====================

    /**
     * 统一处理所有业务异常
     *
     * <p>根据错误码自动判断并返回合适的 HTTP 状态码：</p>
     * <ul>
     *   <li>401xxx - 认证相关异常 → 401 Unauthorized</li>
     *   <li>403xxx - 授权相关异常 → 403 Forbidden</li>
     *   <li>404xxx - 资源不存在 → 404 Not Found</li>
     *   <li>409xxx - 资源冲突 → 409 Conflict</li>
     *   <li>其他 - 业务异常 → 200 OK</li>
     * </ul>
     *
     * <p>自动处理的异常包括：</p>
     * <ul>
     *   <li>AuthenticationException（及其子类：InvalidTokenException, SessionExpiredException, SessionNotFoundException）</li>
     *   <li>AccountNotFoundException</li>
     *   <li>DuplicateUsernameException, DuplicateEmailException</li>
     *   <li>其他 BusinessException 子类</li>
     * </ul>
     *
     * <p>注意：此处理器不处理以下异常（它们有专门的处理器）：</p>
     * <ul>
     *   <li>ParameterException - 需要返回 validationErrors 列表</li>
     *   <li>AccountLockedException - 需要返回 remainingMinutes 字段</li>
     * </ul>
     *
     * @param e 业务异常
     * @return 错误响应（状态码根据 errorCode 动态判断）
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("[全局异常处理] 业务异常: code={}, message={}", e.getErrorCode(), e.getErrorMessage());

        Integer httpErrorCode = parseHttpErrorCode(e.getErrorCode());

        // 根据错误码前缀判断 HTTP 状态码
        HttpStatus httpStatus = determineHttpStatus(e.getErrorCode());

        return ResponseEntity.status(httpStatus)
                .body(ApiResponse.error(httpErrorCode, e.getErrorMessage()));
    }

    // ==================== 系统异常 (500) ====================

    /**
     * 处理系统异常
     *
     * @param e 系统异常
     * @return 500 错误响应
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ApiResponse<Void>> handleSystemException(SystemException e) {
        log.error("[全局异常处理] 系统异常: code={}, message={}", e.getErrorCode(), e.getErrorMessage(), e);

        // 系统异常不向用户暴露内部细节
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500001, "系统异常，请稍后重试"));
    }

    /**
     * 兜底：处理未预期的异常
     *
     * @param e 未知异常
     * @return 500 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[全局异常处理] 未知异常", e);

        // 不暴露内部实现细节
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500002, "系统错误，请稍后重试"));
    }

    // ==================== 工具方法 ====================

    /**
     * 根据错误码前缀判断 HTTP 状态码
     *
     * <p>映射规则：</p>
     * <ul>
     *   <li>AUTH_ → 401 Unauthorized（认证失败）</li>
     *   <li>AUTHZ_ → 403 Forbidden（授权失败）</li>
     *   <li>NOT_FOUND_ → 404 Not Found（资源不存在）</li>
     *   <li>CONFLICT_ → 409 Conflict（资源冲突）</li>
     *   <li>LOCKED_ → 423 Locked（资源被锁定）</li>
     *   <li>其他 → 200 OK（业务异常，通过响应体中的code区分）</li>
     * </ul>
     *
     * @param errorCode 错误码（如 "AUTH_001"）
     * @return HTTP 状态码
     */
    private HttpStatus determineHttpStatus(String errorCode) {
        if (errorCode == null || errorCode.isEmpty()) {
            return HttpStatus.OK;
        }

        // 提取错误码前缀
        String prefix = errorCode.contains("_") ? errorCode.substring(0, errorCode.indexOf("_")) : errorCode;

        return switch (prefix) {
            case "AUTH" -> HttpStatus.UNAUTHORIZED;          // 401
            case "AUTHZ" -> HttpStatus.FORBIDDEN;            // 403
            case "NOT_FOUND" -> HttpStatus.NOT_FOUND;        // 404
            case "CONFLICT" -> HttpStatus.CONFLICT;          // 409
            case "LOCKED" -> HttpStatus.LOCKED;              // 423
            default -> HttpStatus.OK;                         // 200（业务异常）
        };
    }

    /**
     * 将字符串错误码转换为HTTP错误码
     *
     * <p>转换规则：</p>
     * <ul>
     *   <li>AUTH_001 → 401001</li>
     *   <li>AUTH_002 → 401002</li>
     *   <li>PARAM_001 → 400001</li>
     *   <li>NOT_FOUND_001 → 404001</li>
     *   <li>CONFLICT_001 → 409001</li>
     *   <li>LOCKED_001 → 423001</li>
     *   <li>SYS_001 → 500001</li>
     * </ul>
     *
     * @param errorCode 错误码（如 "AUTH_001"）
     * @return HTTP错误码（如 401001）
     */
    private Integer parseHttpErrorCode(String errorCode) {
        if (errorCode == null || errorCode.isEmpty()) {
            return 500000;
        }

        try {
            // 分割错误码，例如 "AUTH_001" -> ["AUTH", "001"]
            String[] parts = errorCode.split("_");
            if (parts.length != 2) {
                log.warn("[全局异常处理] 错误码格式不正确: {}", errorCode);
                return 500000;
            }

            String category = parts[0];
            int sequence = Integer.parseInt(parts[1]);

            // 根据类别前缀映射到HTTP状态码
            int httpStatusPrefix = switch (category) {
                case "AUTH" -> 401;
                case "AUTHZ" -> 403;
                case "PARAM" -> 400;
                case "NOT_FOUND" -> 404;
                case "CONFLICT" -> 409;
                case "LOCKED" -> 423;
                case "BIZ" -> 200;
                case "SYS" -> 500;
                default -> {
                    log.warn("[全局异常处理] 未知的错误码类别: {}", category);
                    yield 500;
                }
            };

            return httpStatusPrefix * 1000 + sequence;
        } catch (Exception e) {
            log.error("[全局异常处理] 解析错误码失败: errorCode={}", errorCode, e);
            return 500000;
        }
    }
}
