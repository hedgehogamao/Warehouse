# Skill: RESTful API 设计规范

> 适用于后端开发（`backend-dev`）、小程序开发（`mini-dev`）

---

## 1 URL 设计

### 1.1 基础规则

- Base URL：`/api/v1`
- 使用名词复数：`/products`、`/orders`，不用动词
- 使用 kebab-case：`/stock-in`、`/order-items`
- 层级不超过 3 层：`/api/v1/inventory/{id}/logs`

### 1.2 资源命名

| 资源 | URL | 说明 |
|------|-----|------|
| 认证 | `/api/v1/auth/login` | 登录（动词例外） |
| 商品 | `/api/v1/products` | 商品 CRUD |
| 入库 | `/api/v1/stock-in` | 入库记录 |
| 库存 | `/api/v1/inventory` | 库存查询 |
| 订单 | `/api/v1/orders` | 订单管理 |
| 历史 | `/api/v1/inventory-logs` | 库存流水 |
| 统计 | `/api/v1/statistics/today` | 数据统计 |
| 小程序 | `/api/v1/miniapp/*` | 小程序专用接口 |

### 1.3 HTTP 方法

| 方法 | 用途 | 示例 |
|------|------|------|
| GET | 查询 | `GET /products?page=1&size=20` |
| POST | 新增 | `POST /products` |
| PUT | 修改 | `PUT /products/1` |
| DELETE | 删除 | `DELETE /products/1` |

> 状态变更使用 PUT：`PUT /products/1/status`、`PUT /orders/1/refund`

---

## 2 请求规范

### 2.1 请求头

```
Content-Type: application/json
Authorization: Bearer {token}
```

### 2.2 查询参数

- 分页：`page`（从 1 开始）、`size`（默认 20）
- 搜索：`keyword`
- 筛选：`{字段名}={值}`，如 `category=机油`、`brand=博世`
- 日期范围：`startDate=2026-06-01`、`endDate=2026-06-30`
- 排序：`sortBy=created_at`、`sortOrder=desc`（可选）

### 2.3 请求体

- 使用 JSON 格式
- 字段使用 camelCase：`salePrice`、`productId`
- 嵌套对象使用数组：`items: [{ productId, quantity }]`

---

## 3 响应规范

### 3.1 统一格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 3.2 成功响应

| 场景 | code | message |
|------|------|---------|
| 查询成功 | 200 | success |
| 新增成功 | 200 | 创建成功 |
| 修改成功 | 200 | 修改成功 |
| 删除成功 | 200 | 删除成功 |

### 3.3 分页响应

```json
{
  "code": 200,
  "data": {
    "total": 128,
    "pages": 7,
    "records": [...]
  }
}
```

### 3.4 错误响应

| 场景 | code | message |
|------|------|---------|
| 参数错误 | 400 | 具体错误信息 |
| 未登录 | 401 | 未登录或 Token 已过期 |
| 无权限 | 403 | 无权限访问 |
| 资源不存在 | 404 | 资源不存在 |
| 业务异常 | 400 | 具体业务错误（如"库存不足"） |
| 服务器错误 | 500 | 服务器内部错误 |

```json
{
  "code": 400,
  "message": "库存不足: 嘉实多极护 5W-30",
  "data": null
}
```

---

## 4 接口清单

### 4.1 认证模块

| 方法 | URL | 说明 | 鉴权 |
|------|-----|------|------|
| POST | `/api/v1/auth/login` | 后台登录 | 无 |
| GET | `/api/v1/auth/me` | 获取当前用户 | 需登录 |
| PUT | `/api/v1/auth/password` | 修改密码 | 需登录 |
| POST | `/api/v1/auth/logout` | 退出登录 | 需登录 |

### 4.2 用户管理

| 方法 | URL | 说明 | 权限 |
|------|-----|------|------|
| GET | `/api/v1/users` | 用户列表 | ADMIN |
| POST | `/api/v1/users` | 新增用户 | ADMIN |
| PUT | `/api/v1/users/{id}` | 修改用户 | ADMIN |
| PUT | `/api/v1/users/{id}/status` | 启用/禁用 | ADMIN |

### 4.3 商品管理

| 方法 | URL | 说明 |
|------|-----|------|
| GET | `/api/v1/products` | 商品列表（分页+搜索） |
| POST | `/api/v1/products` | 新增商品 |
| GET | `/api/v1/products/{id}` | 商品详情 |
| PUT | `/api/v1/products/{id}` | 修改商品 |
| DELETE | `/api/v1/products/{id}` | 删除商品 |
| PUT | `/api/v1/products/{id}/status` | 上架/下架 |
| GET | `/api/v1/products/categories` | 获取分类列表 |
| GET | `/api/v1/products/pos-list` | POS 快速搜索 |

### 4.4 入库管理

| 方法 | URL | 说明 |
|------|-----|------|
| POST | `/api/v1/stock-in` | 新建入库 |
| GET | `/api/v1/stock-in` | 入库记录列表 |
| GET | `/api/v1/stock-in/{id}` | 入库详情 |

### 4.5 库存管理

| 方法 | URL | 说明 |
|------|-----|------|
| GET | `/api/v1/inventory` | 库存列表 |
| GET | `/api/v1/inventory/low-stock` | 库存预警 |
| GET | `/api/v1/inventory/{productId}/logs` | 商品库存流水 |
| PUT | `/api/v1/inventory/{productId}/adjust` | 库存调整 |

### 4.6 订单管理

| 方法 | URL | 说明 |
|------|-----|------|
| POST | `/api/v1/orders` | 创建订单（收银） |
| GET | `/api/v1/orders` | 订单列表 |
| GET | `/api/v1/orders/{id}` | 订单详情 |
| PUT | `/api/v1/orders/{id}/refund` | 订单退款 |

### 4.7 历史与统计

| 方法 | URL | 说明 |
|------|-----|------|
| GET | `/api/v1/inventory-logs` | 库存流水查询 |
| GET | `/api/v1/operation-logs` | 操作日志查询 |
| GET | `/api/v1/statistics/today` | 今日概况 |
| GET | `/api/v1/statistics/sales-trend` | 销售趋势 |
| GET | `/api/v1/statistics/top-products` | 商品排行 |
| GET | `/api/v1/statistics/category-distribution` | 分类占比 |

### 4.8 小程序接口

| 方法 | URL | 说明 |
|------|-----|------|
| POST | `/api/v1/miniapp/auth/login` | 微信登录 |
| GET | `/api/v1/miniapp/products` | 商品浏览 |
| GET | `/api/v1/miniapp/products/{id}` | 商品详情 |
| GET | `/api/v1/miniapp/products/categories` | 分类列表 |
| GET | `/api/v1/miniapp/orders` | 购买记录 |
| GET | `/api/v1/miniapp/customer/profile` | 客户信息 |
| PUT | `/api/v1/miniapp/customer/profile` | 更新客户信息 |

---

## 5 接口文档工具

- 开发阶段使用 **Apifox** 或 **Postman** 维护接口集合
- 每个 Phase 完成后同步更新接口文档
- 导出为 JSON 格式提交到 `server/postman/collection.json`
