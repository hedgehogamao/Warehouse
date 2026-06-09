package com.autoparts.service;

import java.util.Map;

/**
 * 客户网站认证服务接口
 */
public interface CustomerAuthService {

    /**
     * 客户注册（手机号 + 密码）
     */
    Map<String, Object> register(String phone, String password, String name);

    /**
     * 客户登录（手机号 + 密码）
     */
    Map<String, Object> login(String phone, String password);

    /**
     * 获取当前客户信息
     */
    Map<String, Object> getCurrentCustomer();
}
