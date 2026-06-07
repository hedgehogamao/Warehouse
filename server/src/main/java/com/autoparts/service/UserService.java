package com.autoparts.service;

import com.autoparts.dto.LoginRequest;
import com.autoparts.dto.LoginResponse;
import com.autoparts.dto.UserDTO;
import com.autoparts.model.User;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户业务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 根据ID获取用户（脱敏）
     */
    UserDTO getUserById(Integer id);

    /**
     * 分页查询用户
     */
    IPage<UserDTO> getUserPage(int page, int size, String keyword);

    /**
     * 创建用户
     */
    void createUser(UserDTO userDTO);

    /**
     * 修改用户
     */
    void updateUser(Integer id, UserDTO userDTO);

    /**
     * 删除用户
     */
    void deleteUser(Integer id);

    /**
     * 重置密码（管理员操作）
     */
    void resetPassword(Integer id, String newPassword);

    /**
     * 修改密码（用户自己操作）
     */
    void changePassword(Integer userId, String oldPassword, String newPassword);

    /**
     * 更新用户状态（启用/禁用）
     */
    void updateUserStatus(Integer id, Integer status);
}
