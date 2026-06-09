package com.autoparts.service.impl;

import com.autoparts.common.BusinessException;
import com.autoparts.common.JwtUtil;
import com.autoparts.config.MiniAppProperties;
import com.autoparts.model.Customer;
import com.autoparts.repository.CustomerMapper;
import com.autoparts.service.MiniAppAuthService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 小程序登录认证服务实现
 */
@Service
public class MiniAppAuthServiceImpl implements MiniAppAuthService {

    private static final Logger log = LoggerFactory.getLogger(MiniAppAuthServiceImpl.class);

    private final CustomerMapper customerMapper;
    private final MiniAppProperties miniAppProperties;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public MiniAppAuthServiceImpl(CustomerMapper customerMapper,
                                   MiniAppProperties miniAppProperties,
                                   JwtUtil jwtUtil,
                                   ObjectMapper objectMapper) {
        this.customerMapper = customerMapper;
        this.miniAppProperties = miniAppProperties;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public Map<String, Object> login(String code, String nickName, String avatarUrl) {
        // 1. 调用微信接口获取 openid
        String openId = getOpenIdFromWechat(code);

        // 2. 查找或创建客户
        Customer customer = customerMapper.selectOne(
                new LambdaQueryWrapper<Customer>().eq(Customer::getOpenId, openId));

        if (customer == null) {
            customer = new Customer();
            customer.setOpenId(openId);
            customer.setNickName(nickName != null ? nickName : "微信用户");
            customer.setAvatarUrl(avatarUrl);
            customer.setCreatedAt(LocalDateTime.now());
            customer.setUpdatedAt(LocalDateTime.now());
            customerMapper.insert(customer);
        } else {
            // 更新昵称和头像
            if (nickName != null) customer.setNickName(nickName);
            if (avatarUrl != null) customer.setAvatarUrl(avatarUrl);
            customer.setUpdatedAt(LocalDateTime.now());
            customerMapper.updateById(customer);
        }

        // 3. 生成小程序专用 JWT Token（role=CUSTOMER）
        String token = jwtUtil.generateToken(customer.getId(), "CUSTOMER_" + customer.getId(), "CUSTOMER");

        // 4. 返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);

        Map<String, Object> customerInfo = new LinkedHashMap<>();
        customerInfo.put("id", customer.getId());
        customerInfo.put("nickName", customer.getNickName());
        customerInfo.put("avatarUrl", customer.getAvatarUrl());
        customerInfo.put("phone", customer.getPhone());
        result.put("customerInfo", customerInfo);

        return result;
    }

    @Override
    public Customer getCustomerById(Integer id) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BusinessException(404, "客户不存在");
        }
        return customer;
    }

    @Override
    public void updateCustomer(Integer id, String phone, String name) {
        Customer customer = getCustomerById(id);
        if (phone != null) customer.setPhone(phone);
        if (name != null) customer.setName(name);
        customer.setUpdatedAt(LocalDateTime.now());
        customerMapper.updateById(customer);
    }

    /**
     * 调用微信 jscode2session 接口获取 openid
     */
    private String getOpenIdFromWechat(String code) {
        // 开发模式：如果没有配置 AppID，使用 mock openid
        if (miniAppProperties.getAppId() == null || miniAppProperties.getAppId().isEmpty()
                || "your-appid-here".equals(miniAppProperties.getAppId())) {
            log.warn("微信小程序 AppID 未配置，使用 mock openid: mock_openid_{}", code.hashCode());
            return "mock_openid_" + Math.abs(code.hashCode());
        }

        String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                miniAppProperties.getLoginUrl(),
                miniAppProperties.getAppId(),
                miniAppProperties.getAppSecret(),
                code);

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.has("openid")) {
                return jsonNode.get("openid").asText();
            } else {
                String errMsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "未知错误";
                log.error("微信登录失败: {}", errMsg);
                throw new BusinessException(400, "微信登录失败: " + errMsg);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信接口异常: {}", e.getMessage());
            throw new BusinessException(500, "微信登录服务异常");
        }
    }
}
