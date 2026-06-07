package com.autoparts.service.impl;

import com.autoparts.common.BusinessException;
import com.autoparts.common.JwtUtil;
import com.autoparts.dto.LoginRequest;
import com.autoparts.dto.LoginResponse;
import com.autoparts.dto.UserDTO;
import com.autoparts.model.User;
import com.autoparts.repository.UserMapper;
import com.autoparts.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户业务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 根据用户名查找用户
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));

        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        return new LoginResponse(token, user.getId(), user.getUsername(),
                user.getRealName(), user.getRole());
    }

    @Override
    public UserDTO getUserById(Integer id) {
        User user = getById(id);
        if (user == null) {
            return null;
        }
        return toDTO(user);
    }

    @Override
    public IPage<UserDTO> getUserPage(int page, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（用户名或真实姓名）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword));
        }
        wrapper.orderByDesc(User::getCreatedAt);

        IPage<User> userPage = page(new Page<>(page, size), wrapper);

        // 转换为 DTO（排除密码）
        return userPage.convert(this::toDTO);
    }

    @Override
    @Transactional
    public void createUser(UserDTO userDTO) {
        // 检查用户名是否已存在
        long count = count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, userDTO.getUsername()));
        if (count > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRealName(userDTO.getRealName());
        user.setPhone(userDTO.getPhone());
        user.setRole(userDTO.getRole() != null ? userDTO.getRole() : "staff");
        user.setStatus(userDTO.getStatus() != null ? userDTO.getStatus() : 1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        save(user);
    }

    @Override
    @Transactional
    public void updateUser(Integer id, UserDTO userDTO) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 如果修改了用户名，检查是否冲突
        if (userDTO.getUsername() != null && !userDTO.getUsername().equals(user.getUsername())) {
            long count = count(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, userDTO.getUsername()));
            if (count > 0) {
                throw new BusinessException(400, "用户名已存在");
            }
            user.setUsername(userDTO.getUsername());
        }

        if (userDTO.getRealName() != null) user.setRealName(userDTO.getRealName());
        if (userDTO.getPhone() != null) user.setPhone(userDTO.getPhone());
        if (userDTO.getRole() != null) user.setRole(userDTO.getRole());
        if (userDTO.getStatus() != null) user.setStatus(userDTO.getStatus());

        // 如果提供了新密码，则更新
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
    }

    @Override
    @Transactional
    public void deleteUser(Integer id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // 禁止删除管理员账号
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException(400, "不能删除管理员账号");
        }
        removeById(id);
    }

    @Override
    public void resetPassword(Integer id, String newPassword) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
    }

    /**
     * User 实体转 DTO（脱敏密码）
     */
    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
