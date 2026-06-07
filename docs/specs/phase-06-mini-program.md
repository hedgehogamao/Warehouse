# Phase 6: 微信小程序客户端

## 功能概述

实现微信小程序客户端，供门店客户自助查询商品信息、库存状态和个人购买记录。客户通过微信授权登录后使用。

**覆盖模块：** 客户查询

---

## 1. 数据表变更

- 新增 `customers` 表（小程序用户）

```sql
CREATE TABLE customers (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    open_id         VARCHAR(100)    NOT NULL UNIQUE     COMMENT '微信 OpenID',
    nick_name       VARCHAR(100)                        COMMENT '微信昵称',
    avatar_url      VARCHAR(500)                        COMMENT '微信头像',
    phone           VARCHAR(20)                         COMMENT '手机号',
    name            VARCHAR(50)                         COMMENT '真实姓名',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';
```

- `orders` 表新增 `customer_id` 字段（关联客户）

```sql
ALTER TABLE orders ADD COLUMN customer_id INT DEFAULT NULL COMMENT '客户ID' AFTER customer_phone;
```

---

## 2. API 接口设计

Base URL: `/api/v1`

### 2.1 微信登录

```
POST /api/v1/miniapp/auth/login
```

请求体：
```json
{
  "code": "wx_login_code",
  "nickName": "微信用户",
  "avatarUrl": "https://..."
}
```

**后端流程：**
1. 通过 `code` 调用微信接口获取 `openid`
2. 根据 `openid` 查找或创建客户
3. 生成小程序专用 JWT Token
4. 返回 Token 和客户信息

> **注意：** 需配置微信小程序的 AppId 和 AppSecret

响应：
```json
{
  "code": 200,
  "data": {
    "token": "mini_app_jwt_token",
    "customerInfo": {
      "id": 1,
      "nickName": "微信用户",
      "avatarUrl": "https://...",
      "phone": ""
    }
  }
}
```

### 2.2 商品浏览

```
GET /api/v1/miniapp/products?page=1&size=20&keyword=&category=&brand=
```

响应（与后台接口类似，但返回字段更简练）：
```json
{
  "code": 200,
  "data": {
    "total": 128,
    "records": [
      {
        "id": 1,
        "name": "嘉实多极护 5W-30",
        "sku": "JSD-5W30-4L",
        "brand": "嘉实多",
        "carModel": "大众/奥迪/丰田",
        "category": "机油",
        "salePrice": 298.00,
        "stock": 60,
        "imageUrl": "http://...",
        "stockStatus": "NORMAL"
      }
    ]
  }
}
```

### 2.3 商品详情

```
GET /api/v1/miniapp/products/{id}
```

响应（增加描述字段）：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "嘉实多极护 5W-30",
    "sku": "JSD-5W30-4L",
    "brand": "嘉实多",
    "carModel": ["大众", "奥迪", "丰田"],
    "category": "机油",
    "salePrice": 298.00,
    "stock": 60,
    "imageUrl": "http://...",
    "description": "全合成机油，5W-30粘度，适用于多数德系日系车型",
    "stockStatus": "NORMAL"
  }
}
```

### 2.4 分类列表

```
GET /api/v1/miniapp/products/categories
```

响应：
```json
{
  "code": 200,
  "data": ["机油", "滤芯", "刹车片", "轮胎", "火花塞", "电瓶", "雨刮", "其他"]
}
```

### 2.5 商品搜索

```
GET /api/v1/miniapp/products/search?keyword=刹车片&page=1&size=20
```

与商品列表接口共用，但搜索范围更广（匹配名称、SKU、品牌、适配车型）。

### 2.6 购买记录

```
GET /api/v1/miniapp/orders?page=1&size=20
```

Headers: `Authorization: Bearer {mini_app_token}`

响应：
```json
{
  "code": 200,
  "data": {
    "total": 5,
    "records": [
      {
        "id": 1,
        "orderNo": "SO202606060001",
        "totalAmount": 641.00,
        "paymentMethod": "WECHAT",
        "paymentMethodName": "微信支付",
        "status": "PAID",
        "itemCount": 2,
        "createdAt": "2026-06-06 15:00:00",
        "items": [
          {
            "productName": "嘉实多极护 5W-30",
            "quantity": 2,
            "unitPrice": 298.00,
            "subtotal": 596.00
          },
          {
            "productName": "博世机油滤清器",
            "quantity": 1,
            "unitPrice": 45.00,
            "subtotal": 45.00
          }
        ]
      }
    ]
  }
}
```

### 2.7 客户信息

```
GET  /api/v1/miniapp/customer/profile
PUT  /api/v1/miniapp/customer/profile
```

获取和更新客户个人信息（手机号、姓名等）。

---

## 3. 后端实现

### 3.1 文件清单

| 层 | 文件 | 说明 |
|----|------|------|
| model | `Customer.java` | 客户实体 |
| repository | `CustomerMapper.java` | 客户数据访问 |
| service | `MiniAppAuthService.java` | 小程序登录认证 |
| controller | `MiniAppAuthController.java` | 小程序登录接口 |
| controller | `MiniAppProductController.java` | 小程序商品接口 |
| controller | `MiniAppOrderController.java` | 小程序订单查询接口 |
| controller | `MiniAppCustomerController.java` | 客户信息接口 |
| config | `MiniAppProperties.java` | 微信小程序配置 |

### 3.2 关键逻辑

**小程序登录流程：**
```
Client                    Server                    WeChat Server
  │                         │                         │
  │── wx.login() ──────────→│                         │
  │←── code ───────────────│                         │
  │── POST /login {code} ──→│                         │
  │                         │── GET openid via code ──→│
  │                         │←── {openid, session_key} │
  │                         │ 查询/创建客户            │
  │                         │ 生成 JWT Token          │
  │←── {token, userInfo} ──│                         │
```

**客户订单查询：** 通过 `customer_id` 关联查询，只能查到自己（当前登录客户）的订单。

### 3.3 微信小程序配置

在 `application.yml` 中添加：
```yaml
wechat:
  miniapp:
    app-id: wx_your_app_id
    app-secret: your_app_secret
    login-url: https://api.weixin.qq.com/sns/jscode2session
```

---

## 4. 小程序页面

### 4.1 首页 (pages/index/index)

```
┌──────────────────────────┐
│  🔍 搜索商品...          │
│                          │
│  ┌─────┐ ┌─────┐ ┌────┐ │
│  │ 机油 │ │ 滤芯 │ │刹车│ │
│  └─────┘ └─────┘ └────┘ │
│  ┌─────┐ ┌─────┐ ┌────┐ │
│  │ 轮胎 │ │火花塞│ │电瓶│ │
│  └─────┘ └─────┘ └────┘ │
│                          │
│  ─── 热门商品 ───        │
│  ┌────────────────────┐  │
│  │ [img] 嘉实多极护    │  │
│  │        ¥298.00 现货 │  │
│  ├────────────────────┤  │
│  │ [img] 博世机油滤清器 │  │
│  │        ¥45.00  现货  │  │
│  └────────────────────┘  │
│                          │
└──────────────────────────┘
```

### 4.2 商品列表页 (pages/products/products)

```
┌──────────────────────────┐
│  ← 商品列表            │
│  [筛选: 品牌▼ 分类▼]    │
│                          │
│  ┌──────┬──────────────┐ │
│  │ [img]│ 嘉实多极护    │ │
│  │      │ ¥298.00 ✓现货 │ │
│  ├──────┼──────────────┤ │
│  │ [img]│ 博世机油滤清器 │ │
│  │      │ ¥45.00  ✗缺货 │ │
│  └──────┴──────────────┘ │
│                          │
└──────────────────────────┘
```

### 4.3 商品详情页 (pages/products/detail)

```
┌──────────────────────────┐
│  ← 商品详情              │
│                          │
│  ┌────────────────────┐  │
│  │   商品大图           │  │
│  └────────────────────┘  │
│                          │
│  嘉实多极护 5W-30       │
│  ¥298.00                 │
│  ⚫ 现货（库存 60件）    │
│                          │
│  品牌: 嘉实多            │
│  编码: JSD-5W30-4L      │
│  适配: 大众、奥迪、丰田  │
│                          │
│  商品描述:               │
│  全合成机油，5W-30粘度..│
│                          │
│  ┌────────────────────┐  │
│  │   联系店家          │  │
│  └────────────────────┘  │
│                          │
└──────────────────────────┘
```

客户不可直接在端内下单，联系店家后由店员在后台下单。

### 4.4 购买记录页 (pages/orders/orders)

```
┌──────────────────────────┐
│  ← 我的购买记录          │
│                          │
│  ┌────────────────────┐  │
│  │ SO202606060001      │  │
│  │ 嘉实多极护 x2  596  │  │
│  │ 博世滤清器 ×1  45  │  │
│  │ 合计: ¥641.00       │  │
│  │ 2026-06-06 15:00    │  │
│  │         已支付 ✓    │  │
│  ├────────────────────┤  │
│  │ SO202606050002      │  │
│  │ ...                 │  │
│  └────────────────────┘  │
│                          │
└──────────────────────────┘
```

### 4.5 个人中心 (pages/mine/mine)

```
┌──────────────────────────┐
│  ┌────────────────────┐  │
│  │   [头像]            │  │
│  │   微信昵称           │  │
│  └────────────────────┘  │
│                          │
│  📋 我的购买记录         │
│  📞 联系方式             │
│  ⚙ 设置                 │
│                          │
└──────────────────────────┘
```

---

## 5. 页面路径配置 (app.json)

```json
{
  "pages": [
    "pages/index/index",
    "pages/products/products",
    "pages/products/detail",
    "pages/orders/orders",
    "pages/mine/mine"
  ],
  "window": {
    "navigationBarTitleText": "XX汽配商店",
    "navigationBarBackgroundColor": "#1677FF"
  },
  "tabBar": {
    "list": [
      { "pagePath": "pages/index/index", "text": "首页", "iconPath": "" },
      { "pagePath": "pages/orders/orders", "text": "记录", "iconPath": "" },
      { "pagePath": "pages/mine/mine", "text": "我的", "iconPath": "" }
    ]
  }
}
```

---

## 6. 验收标准

| # | 检查项 | 预期结果 |
|---|--------|----------|
| 1 | 微信登录 | 授权后成功获取用户信息 |
| 2 | 商品列表 | 分页加载，数据正确 |
| 3 | 商品搜索 | 关键字搜索返回匹配商品 |
| 4 | 商品详情 | 显示完整商品信息 |
| 5 | 库存状态 | 缺货/有货显示正确 |
| 6 | 购买记录 | 仅显示当前客户的订单 |
| 7 | 分类筛选 | 按分类正确筛选 |
| 8 | 页面切换 | Tab 切换流畅 |
| 9 | Token 过期 | Token 过期自动跳转登录 |

---

## 7. 依赖项

- Phase 0: 项目骨架、数据库
- Phase 1: JWT 鉴权能力
- Phase 2: 商品数据
- Phase 4: 订单数据（购买记录）

---

## 8. 交付物清单

| 文件 | 说明 |
|------|------|
| `server/.../model/Customer.java` | 客户实体 |
| `server/.../repository/CustomerMapper.java` | 客户数据访问 |
| `server/.../controller/MiniAppAuthController.java` | 小程序登录接口 |
| `server/.../controller/MiniAppProductController.java` | 小程序商品接口 |
| `server/.../controller/MiniAppOrderController.java` | 小程序订单接口 |
| `server/.../controller/MiniAppCustomerController.java` | 客户信息接口 |
| `server/.../service/MiniAppAuthService.java` | 小程序登录服务 |
| `server/.../config/MiniAppProperties.java` | 小程序配置 |
| `mini-program/app.js` | 全局逻辑 |
| `mini-program/app.json` | 全局配置 |
| `mini-program/app.wxss` | 全局样式 |
| `mini-program/pages/index/index.*` | 首页 |
| `mini-program/pages/products/products.*` | 商品列表 |
| `mini-program/pages/products/detail.*` | 商品详情 |
| `mini-program/pages/orders/orders.*` | 购买记录 |
| `mini-program/pages/mine/mine.*` | 个人中心 |
| `mini-program/utils/api.js` | 请求封装 |
