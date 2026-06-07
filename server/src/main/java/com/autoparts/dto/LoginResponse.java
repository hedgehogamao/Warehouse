package com.autoparts.dto;

/**
 * 登录响应对象
 */
public class LoginResponse {

    /** JWT Token */
    private String token;

    /** 用户ID */
    private Integer userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 角色 */
    private String role;

    public LoginResponse() {}

    public LoginResponse(String token, Integer userId, String username, String realName, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.role = role;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
