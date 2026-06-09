package com.autoparts.service;

import com.autoparts.common.BusinessException;
import com.autoparts.dto.LoginRequest;
import com.autoparts.dto.LoginResponse;
import com.autoparts.dto.UserDTO;
import com.autoparts.model.User;
import com.autoparts.repository.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务单元测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole("STAFF");
        testUser.setRealName("测试用户");
        testUser.setPhone("13800138000");
        testUser.setStatus(1);
        userMapper.insert(testUser);
    }

    @Test
    @DisplayName("登录成功 - 正确用户名密码")
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        LoginResponse response = userService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("STAFF", response.getRole());
    }

    @Test
    @DisplayName("登录失败 - 错误密码")
    void testLogin_WrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        assertThrows(BusinessException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("登录失败 - 禁用用户")
    void testLogin_DisabledUser() {
        // 禁用用户
        testUser.setStatus(0);
        userMapper.updateById(testUser);

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        assertThrows(BusinessException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("创建用户成功")
    void testCreateUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("newuser");
        userDTO.setPassword("newpassword");
        userDTO.setRole("STAFF");
        userDTO.setRealName("新用户");

        userService.createUser(userDTO);

        User savedUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "newuser")
        );

        assertNotNull(savedUser);
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("STAFF", savedUser.getRole());
        assertTrue(passwordEncoder.matches("newpassword", savedUser.getPassword()));
    }

    @Test
    @DisplayName("创建用户失败 - 用户名重复")
    void testCreateUser_DuplicateUsername() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser"); // 已存在的用户名
        userDTO.setPassword("newpassword");
        userDTO.setRole("STAFF");

        assertThrows(BusinessException.class, () -> userService.createUser(userDTO));
    }

    @Test
    @DisplayName("修改用户成功")
    void testUpdateUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setRealName("修改后的名字");
        userDTO.setPhone("13900139000");

        userService.updateUser(testUser.getId(), userDTO);

        User updatedUser = userMapper.selectById(testUser.getId());
        assertEquals("修改后的名字", updatedUser.getRealName());
        assertEquals("13900139000", updatedUser.getPhone());
    }

    @Test
    @DisplayName("删除用户成功")
    void testDeleteUser() {
        int rows = userMapper.deleteById(testUser.getId());
        assertEquals(1, rows);
        assertNull(userMapper.selectById(testUser.getId()));
    }

    @Test
    @DisplayName("重置密码成功")
    void testResetPassword() {
        userService.resetPassword(testUser.getId(), "newpassword123");

        User updatedUser = userMapper.selectById(testUser.getId());
        assertTrue(passwordEncoder.matches("newpassword123", updatedUser.getPassword()));
    }

    @Test
    @DisplayName("修改密码成功")
    void testChangePassword() {
        userService.changePassword(testUser.getId(), "password123", "newpassword123");

        User updatedUser = userMapper.selectById(testUser.getId());
        assertTrue(passwordEncoder.matches("newpassword123", updatedUser.getPassword()));
    }

    @Test
    @DisplayName("修改密码失败 - 旧密码错误")
    void testChangePassword_WrongOldPassword() {
        assertThrows(BusinessException.class,
                () -> userService.changePassword(testUser.getId(), "wrongpassword", "newpassword123"));
    }

    @Test
    @DisplayName("更新用户状态成功")
    void testUpdateUserStatus() {
        userService.updateUserStatus(testUser.getId(), 0);

        User updatedUser = userMapper.selectById(testUser.getId());
        assertEquals(0, updatedUser.getStatus());
    }
}
