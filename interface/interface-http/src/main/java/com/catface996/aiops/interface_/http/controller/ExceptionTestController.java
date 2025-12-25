package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.common.exception.SystemException;
import com.catface996.aiops.common.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 异常测试 Controller
 *
 * @author catface996
 * @since 2025-11-21
 */
@RestController
@RequestMapping("/test")
public class ExceptionTestController {

    /**
     * 测试业务异常
     *
     * @return 不会返回，会被全局异常处理器捕获
     */
    @PostMapping("/business-exception")
    public Result<Void> testBusinessException() {
        throw new BusinessException("BUSINESS_ERROR", "用户余额不足");
    }

    /**
     * 测试系统异常
     *
     * @return 不会返回，会被全局异常处理器捕获
     */
    @PostMapping("/system-exception")
    public Result<Void> testSystemException() {
        throw new SystemException("SYSTEM_ERROR", "数据库连接失败");
    }

    /**
     * 测试未知异常
     *
     * @return 不会返回，会被全局异常处理器捕获
     */
    @PostMapping("/unknown-exception")
    public Result<Void> testUnknownException() {
        throw new RuntimeException("这是一个未知的运行时异常");
    }
}
