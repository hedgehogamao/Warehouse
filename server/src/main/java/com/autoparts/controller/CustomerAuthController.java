package com.autoparts.controller;

import com.autoparts.common.Result;
import com.autoparts.service.CustomerAuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 客户网站登录注册接口
 */
@RestController
@RequestMapping("/api/v1/customer/auth")
public class CustomerAuthController {

    private final CustomerAuthService customerAuthService;

    public CustomerAuthController(CustomerAuthService customerAuthService) {
        this.customerAuthService = customerAuthService;
    }

    /**
     * 客户注册（手机号 + 密码）
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String password = body.get("password");
        String name = body.get("name");

        if (phone == null || phone.trim().isEmpty()) {
            return Result.error(400, "手机号不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error(400, "密码不能为空");
        }
        if (password.length() < 6) {
            return Result.error(400, "密码长度不能少于6位");
        }

        Map<String, Object> result = customerAuthService.register(phone.trim(), password, name);
        return Result.success(result);
    }

    /**
     * 客户登录（手机号 + 密码）
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String password = body.get("password");

        if (phone == null || phone.trim().isEmpty()) {
            return Result.error(400, "手机号不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.error(400, "密码不能为空");
        }

        Map<String, Object> result = customerAuthService.login(phone.trim(), password);
        return Result.success(result);
    }

    /**
     * 获取当前客户信息
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentCustomer() {
        Map<String, Object> result = customerAuthService.getCurrentCustomer();
        return Result.success(result);
    }
}
