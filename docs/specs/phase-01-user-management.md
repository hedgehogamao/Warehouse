# Phase 1: 用户管理与登录鉴权

## 功能概述

实现门店员工（店员/管理员）的登录认证和权限管理功能。包括用户登录、Token 鉴权、用户信息管理和密码修改。

**覆盖模块：** 用户管理

---

## 1. 数据表变更

- 使用 Phase 0 已创建的 `users` 表，无需新增
- 初始化数据中需预置管理员账号

---

## 2. API 接口设计

Base URL: `/api/v1`

### 2.1 用户登录

```
POST /api/v1/auth/login
```

请求体：
```json
{
  "username": "admin",
  "password": "admin123"
}
```

响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "role": "ADMIN",
      "realName": "系统管理员"
    }
  }
}
```

### 2.2 获取当前用户信息

```
GET /api/v1/auth/me
```

Headers: `Authorization: Bearer {token}`

响应：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "admin",
    "role": "ADMIN",
    "realName": "系统管理员",
    "phone": "13800138000",
    "createdAt": "2026-06-06 10:00:00"
  }
}
```

### 2.3 修改密码

```
PUT /api/v1/auth/password
```

请求体：
```json
{
  "oldPassword": "admin123",
  "newPassword": "newpass456"
}
```

### 2.4 用户管理（管理员功能）

```
GET    /api/v1/users          # 用户列表（分页）
POST   /api/v1/users          # 新增用户
PUT    /api/v1/users/{id}     # 修改用户信息
PUT    /api/v1/users/{id}/status  # 启用/禁用用户
DELETE /api/v1/users/{id}     # 删除用户
```

#### 用户列表 (GET /api/v1/users)

查询参数：`page=1&size=10&keyword=搜索关键词`

响应：
```json
{
  "code": 200,
  "data": {
    "total": 5,
    "records": [
      {
        "id": 1,
        "username": "admin",
        "role": "ADMIN",
        "realName": "系统管理员",
        "phone": "13800138000",
        "status": 1,
        "createdAt": "2026-06-06 10:00:00"
      }
    ]
  }
}
```

> **注意：** 用户列表不返回密码字段

#### 新增用户 (POST /api/v1/users)

请求体：
```json
{
  "username": "staff01",
  "password": "staff123",
  "role": "STAFF",
  "realName": "张三",
  "phone": "13900139000"
}
```

### 2.5 退出登录

```
POST /api/v1/auth/logout
```

（客户端清除 Token 即可，服务端可选择将 Token 加入黑名单）

---

## 3. 后端实现

### 3.1 文件清单

| 层 | 文件 | 说明 |
|----|------|------|
| model | `User.java` | 用户实体 |
| dto | `LoginRequest.java` | 登录请求参数 |
| dto | `LoginResponse.java` | 登录响应 VO |
| dto | `UserVO.java` | 用户展示对象（不含密码） |
| dto | `PageParam.java` | 分页请求参数 |
| dto | `PageResult.java` | 分页响应结果 |
| repository | `UserMapper.java` | 用户数据访问 |
| service | `UserService.java` | 用户业务逻辑 |
| controller | `AuthController.java` | 认证接口 |
| config | `JwtAuthFilter.java` | JWT 鉴权过滤器 |
| config | `SecurityConfig.java` | 安全配置 |

### 3.2 关键逻辑

**登录流程：**
1. 验证用户名是否存在
2. 验证密码（BCryptPasswordEncoder）
3. 生成 JWT Token（含用户 ID、角色）
4. 返回 Token 和用户信息

**鉴权流程：**
1. JwtAuthFilter 从请求头提取 Token
2. 解析 Token 获取用户 ID
3. 将用户信息存入 SecurityContext
4. 对于管理后台接口校验 ADMIN/STAFF 角色

**密码安全：**
- 使用 BCrypt 加密存储
- 密码长度 ≥ 6 位
- 新旧密码不能相同

### 3.3 API 权限矩阵

| 接口 | ADMIN | STAFF | 无需登录 |
|------|-------|-------|----------|
| POST /auth/login | - | - | ✓ |
| GET /auth/me | ✓ | ✓ | - |
| PUT /auth/password | ✓ | ✓ | - |
| GET /users | ✓ | - | - |
| POST /users | ✓ | - | - |
| PUT /users/{id} | ✓ | - | - |
| DELETE /users/{id} | ✓ | - | - |

---

## 4. 管理后台页面

### 4.1 登录页 (/login)

```
┌─────────────────────────┐
│                         │
│   汽车配件管理系统       │
│                         │
│   ┌─────────────────┐  │
│   │ 用户名            │  │
│   └─────────────────┘  │
│   ┌─────────────────┐  │
│   │ 密码              │  │
│   └─────────────────┘  │
│                         │
│   ┌─────────────────┐  │
│   │     登  录       │  │
│   └─────────────────┘  │
│                         │
└─────────────────────────┘
```

**功能：**
- 用户名/密码输入框
- 登录按钮
- 登录成功后跳转首页，Token 存入 localStorage
- 登录失败显示错误提示
- 已登录自动跳转首页

### 4.2 用户管理页 (/users) - 仅管理员可见

**功能：**
- 用户列表表格（用户名、角色、姓名、电话、状态、创建时间）
- 搜索框（按用户名/姓名搜索）
- 新增用户按钮（弹出对话框填写信息）
- 编辑用户（修改姓名、电话、角色）
- 启用/禁用用户（switch 开关）
- 重置密码按钮
- 分页功能

---

## 5. 验收标准

| # | 检查项 | 预期结果 |
|---|--------|----------|
| 1 | 登录成功 | 正确用户名密码返回 Token |
| 2 | 登录失败 | 错误密码返回 401 错误提示 |
| 3 | Token 鉴权 | 无 Token 访问接口返回 401 |
| 4 | 角色权限 | STAFF 无法访问用户管理接口 |
| 5 | 修改密码 | 新旧密码校验正确 |
| 6 | 登录页 | 输入框验证非空 |
| 7 | 用户管理 | 管理员可增删改查用户 |
| 8 | 状态管理 | 禁用后用户无法登录 |

---

## 6. 依赖项

- Phase 0: 项目骨架、数据库、基础配置

---

## 7. 交付物清单

| 文件 | 说明 |
|------|------|
| `server/.../model/User.java` | 用户实体 |
| `server/.../dto/LoginRequest.java` | 登录请求 |
| `server/.../dto/LoginResponse.java` | 登录响应 |
| `server/.../controller/AuthController.java` | 认证接口 |
| `server/.../controller/UserController.java` | 用户管理接口 |
| `server/.../service/UserService.java` | 用户业务逻辑 |
| `server/.../repository/UserMapper.java` | 用户数据访问 |
| `server/.../config/JwtAuthFilter.java` | JWT 过滤器 |
| `admin/src/views/Login.vue` | 登录页 |
| `admin/src/views/UserManagement.vue` | 用户管理页 |
| `admin/src/api/auth.js` | 认证 API |
