package com.autoparts.controller;

import com.autoparts.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查接口
 * 用于验证后端服务是否正常运行
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    /**
     * 健康检查
     * GET /api/v1/health
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = Map.of(
                "status", "UP",
                "service", "auto-parts-server",
                "timestamp", System.currentTimeMillis()
        );
        return Result.success(data);
    }
}
