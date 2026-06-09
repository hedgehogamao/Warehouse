package com.autoparts.controller;

import com.autoparts.common.Result;
import com.autoparts.model.Customer;
import com.autoparts.service.MiniAppAuthService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 小程序客户信息接口
 */
@RestController
@RequestMapping("/api/v1/miniapp/customer")
public class MiniAppCustomerController {

    private final MiniAppAuthService miniAppAuthService;

    public MiniAppCustomerController(MiniAppAuthService miniAppAuthService) {
        this.miniAppAuthService = miniAppAuthService;
    }

    /**
     * 获取客户信息
     */
    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile(Authentication authentication) {
        Integer customerId = extractCustomerId(authentication);
        Customer customer = miniAppAuthService.getCustomerById(customerId);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", customer.getId());
        map.put("nickName", customer.getNickName());
        map.put("avatarUrl", customer.getAvatarUrl());
        map.put("phone", customer.getPhone());
        map.put("name", customer.getName());

        return Result.success(map);
    }

    /**
     * 更新客户信息
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, String> body,
                                       Authentication authentication) {
        Integer customerId = extractCustomerId(authentication);
        miniAppAuthService.updateCustomer(customerId, body.get("phone"), body.get("name"));
        return Result.success(null);
    }

    private Integer extractCustomerId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Integer) {
            return (Integer) principal;
        }
        return null;
    }
}
