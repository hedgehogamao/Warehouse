package com.autoparts.controller;

import com.autoparts.common.Result;
import com.autoparts.service.MiniAppAuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 小程序登录接口
 */
@RestController
@RequestMapping("/api/v1/miniapp/auth")
public class MiniAppAuthController {

    private final MiniAppAuthService miniAppAuthService;

    public MiniAppAuthController(MiniAppAuthService miniAppAuthService) {
        this.miniAppAuthService = miniAppAuthService;
    }

    /**
     * 微信登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String nickName = body.get("nickName");
        String avatarUrl = body.get("avatarUrl");

        if (code == null || code.isEmpty()) {
            return Result.error(400, "code 不能为空");
        }

        Map<String, Object> result = miniAppAuthService.login(code, nickName, avatarUrl);
        return Result.success(result);
    }
}
