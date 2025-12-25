package com.catface996.aiops.interface_.http.controller;

import com.catface996.aiops.common.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查 Controller
 *
 * @author catface996
 * @since 2025-11-21
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 健康检查端点
     *
     * @return 健康状态
     */
    @PostMapping
    public Result<Map<String, String>> health() {
        Map<String, String> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "aiops-service");
        return Result.success(data);
    }
}
