package com.autoparts.controller;

import com.autoparts.common.Result;
import com.autoparts.dto.LoginRequest;
import com.autoparts.dto.LoginResponse;
import com.autoparts.dto.UserDTO;
import com.autoparts.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * 处理登录、登出、获取当前用户信息等请求
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户登录
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success(response);
    }

    /**
     * 获取当前登录用户信息
     * GET /api/v1/auth/me
     * 从 JWT Token 中解析用户ID，返回用户详情
     */
    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        Integer userId;
        if (principal instanceof Integer) {
            userId = (Integer) principal;
        } else {
            // CUSTOMER 角色的 principal 是字符串
            userId = Integer.parseInt(principal.toString());
        }
        UserDTO user = userService.getUserById(userId);
        return Result.success(user);
    }

    /**
     * 修改密码（当前用户）
     * PUT /api/v1/auth/password
     */
    @PutMapping("/password")
    public Result<Void> changePassword(Authentication authentication,
                                        @RequestBody Map<String, String> body) {
        Integer userId = (Integer) authentication.getPrincipal();
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success(null);
    }

    /**
     * 退出登录
     * POST /api/v1/auth/logout
     * 服务端无状态，客户端清除 Token 即可
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success(null);
    }
}
