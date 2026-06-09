package com.autoparts.service.impl;

import com.autoparts.common.BusinessException;
import com.autoparts.common.JwtUtil;
import com.autoparts.model.Customer;
import com.autoparts.repository.CustomerMapper;
import com.autoparts.service.CustomerAuthService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 客户网站认证服务实现
 */
@Service
public class CustomerAuthServiceImpl implements CustomerAuthService {

    private final CustomerMapper customerMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public CustomerAuthServiceImpl(CustomerMapper customerMapper,
                                    JwtUtil jwtUtil,
                                    PasswordEncoder passwordEncoder) {
        this.customerMapper = customerMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Map<String, Object> register(String phone, String password, String name) {
        // 检查手机号是否已注册
        Customer existing = customerMapper.selectOne(
                new LambdaQueryWrapper<Customer>().eq(Customer::getPhone, password != null ? phone : null)
                        .isNotNull(Customer::getPassword));
        // 更精确的查询：手机号 + 有密码的记录
        existing = customerMapper.selectOne(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getPhone, phone)
                        .isNotNull(Customer::getPassword));

        if (existing != null) {
            throw new BusinessException(400, "该手机号已注册");
        }

        // 创建客户
        Customer customer = new Customer();
        customer.setOpenId("web_" + phone);
        customer.setPhone(phone);
        customer.setPassword(passwordEncoder.encode(password));
        customer.setName(name != null ? name : "");
        customer.setNickName(name != null ? name : "客户");
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customerMapper.insert(customer);

        // 生成 JWT Token（role=CUSTOMER，与小程序一致）
        String token = jwtUtil.generateToken(customer.getId(), "CUSTOMER_" + customer.getId(), "CUSTOMER");

        // 返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);

        Map<String, Object> customerInfo = new LinkedHashMap<>();
        customerInfo.put("id", customer.getId());
        customerInfo.put("phone", customer.getPhone());
        customerInfo.put("name", customer.getName());
        customerInfo.put("nickName", customer.getNickName());
        result.put("customerInfo", customerInfo);

        return result;
    }

    @Override
    public Map<String, Object> login(String phone, String password) {
        // 根据手机号查找有密码的客户
        Customer customer = customerMapper.selectOne(
                new LambdaQueryWrapper<Customer>()
                        .eq(Customer::getPhone, phone)
                        .isNotNull(Customer::getPassword));

        if (customer == null) {
            throw new BusinessException(401, "手机号或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, customer.getPassword())) {
            throw new BusinessException(401, "手机号或密码错误");
        }

        // 生成 JWT Token
        String token = jwtUtil.generateToken(customer.getId(), "CUSTOMER_" + customer.getId(), "CUSTOMER");

        // 返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);

        Map<String, Object> customerInfo = new LinkedHashMap<>();
        customerInfo.put("id", customer.getId());
        customerInfo.put("phone", customer.getPhone());
        customerInfo.put("name", customer.getName());
        customerInfo.put("nickName", customer.getNickName());
        result.put("customerInfo", customerInfo);

        return result;
    }

    @Override
    public Map<String, Object> getCurrentCustomer() {
        Integer customerId = (Integer) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Customer customer = customerMapper.selectById(customerId);

        if (customer == null) {
            throw new BusinessException(404, "客户不存在");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", customer.getId());
        result.put("phone", customer.getPhone());
        result.put("name", customer.getName());
        result.put("nickName", customer.getNickName());
        result.put("avatarUrl", customer.getAvatarUrl());
        return result;
    }
}
