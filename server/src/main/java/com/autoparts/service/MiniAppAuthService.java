package com.autoparts.service;

import com.autoparts.model.Customer;

import java.util.Map;

/**
 * 小程序登录认证服务接口
 */
public interface MiniAppAuthService {

    /**
     * 微信登录（通过 code 获取 openid，查找或创建客户，生成 JWT）
     * @return { token, customerInfo }
     */
    Map<String, Object> login(String code, String nickName, String avatarUrl);

    /**
     * 根据客户 ID 获取客户信息
     */
    Customer getCustomerById(Integer id);

    /**
     * 更新客户信息（手机号、姓名）
     */
    void updateCustomer(Integer id, String phone, String name);
}
