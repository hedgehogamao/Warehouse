package com.autoparts.controller;

import com.autoparts.common.Result;
import com.autoparts.dto.UserDTO;
import com.autoparts.service.UserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器
 * 提供用户的增删改查接口（仅管理员可操作）
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 分页查询用户列表
     * GET /api/v1/users?page=1&size=20&keyword=xxx
     */
    @GetMapping
    public Result<IPage<UserDTO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        IPage<UserDTO> result = userService.getUserPage(page, size, keyword);
        return Result.success(result);
    }

    /**
     * 获取用户详情
     * GET /api/v1/users/{id}
     */
    @GetMapping("/{id}")
    public Result<UserDTO> detail(@PathVariable Integer id) {
        UserDTO dto = userService.getUserById(id);
        if (dto == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(dto);
    }

    /**
     * 新增用户
     * POST /api/v1/users
     */
    @PostMapping
    public Result<Void> create(@RequestBody UserDTO userDTO) {
        userService.createUser(userDTO);
        return Result.success(null);
    }

    /**
     * 修改用户
     * PUT /api/v1/users/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody UserDTO userDTO) {
        userService.updateUser(id, userDTO);
        return Result.success(null);
    }

    /**
     * 删除用户
     * DELETE /api/v1/users/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        userService.deleteUser(id);
        return Result.success(null);
    }

    /**
     * 启用/禁用用户
     * PUT /api/v1/users/{id}/status
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Integer id,
                                      @RequestBody Map<String, Integer> body) {
        userService.updateUserStatus(id, body.get("status"));
        return Result.success(null);
    }

    /**
     * 修改密码（管理员可为任意用户重置）
     * PUT /api/v1/users/{id}/password
     */
    @PutMapping("/{id}/password")
    public Result<Void> changePassword(@PathVariable Integer id,
                                        @RequestBody Map<String, String> request) {
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        userService.changePassword(id, oldPassword, newPassword);
        return Result.success(null);
    }
}
