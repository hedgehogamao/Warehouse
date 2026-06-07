# Phase 2: 商品管理模块

## 功能概述

实现汽车配件的商品信息管理功能，包括商品的增删改查、分类管理、图片上传、库存状态显示等。

**覆盖模块：** 商品管理

---

## 1. 数据表变更

- 使用 Phase 0 已创建的 `products` 表，无需新增表
- 如有分类需求，可考虑新增 `categories` 表（本期可选）

---

## 2. API 接口设计

Base URL: `/api/v1`

### 2.1 商品 CRUD

```
GET    /api/v1/products            # 商品列表（分页+搜索+筛选）
POST   /api/v1/products            # 新增商品
GET    /api/v1/products/{id}       # 商品详情
PUT    /api/v1/products/{id}       # 修改商品信息
DELETE /api/v1/products/{id}       # 删除商品
PUT    /api/v1/products/{id}/status  # 上架/下架商品
```

### 2.2 商品列表

```
GET /api/v1/products?page=1&size=20&keyword=机油&brand=壳牌&category=机油&status=1
```

查询参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码（默认 1） |
| size | int | 每页条数（默认 20） |
| keyword | string | 关键字搜索（名称/SKU/适配车型） |
| brand | string | 品牌筛选 |
| category | string | 分类筛选 |
| status | int | 状态筛选（1上架/0下架/空=全部） |

响应：
```json
{
  "code": 200,
  "data": {
    "total": 128,
    "pages": 7,
    "records": [
      {
        "id": 1,
        "name": "嘉实多极护 5W-30",
        "sku": "JSD-5W30-4L",
        "brand": "嘉实多",
        "carModel": "大众/奥迪/丰田",
        "category": "机油",
        "unit": "桶",
        "salePrice": 298.00,
        "stock": 50,
        "minStock": 10,
        "status": 1,
        "imageUrl": "http://...",
        "createdAt": "2026-06-06 10:00:00"
      }
    ]
  }
}
```

**库存状态说明：**
- stock > 0: 有货（正常显示）
- stock = 0: 缺货（标签显示"缺货"）
- stock ≤ minStock: 库存不足（标签显示"库存不足"）

### 2.3 新增商品

```
POST /api/v1/products
```

请求体：
```json
{
  "name": "博世机油滤清器 0986AF0063",
  "sku": "BS-0986AF0063",
  "brand": "博世",
  "carModel": "大众/本田/丰田",
  "category": "滤芯",
  "unit": "个",
  "costPrice": 25.00,
  "salePrice": 45.00,
  "minStock": 20,
  "description": "适用多数主流车型机油滤清器",
  "imageUrl": ""
}
```

### 2.4 商品详情

```
GET /api/v1/products/{id}
```

响应（在列表数据基础上增加 description 字段）：

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "嘉实多极护 5W-30",
    "sku": "JSD-5W30-4L",
    "brand": "嘉实多",
    "carModel": "大众/奥迪/丰田",
    "category": "机油",
    "unit": "桶",
    "costPrice": 180.00,
    "salePrice": 298.00,
    "stock": 50,
    "minStock": 10,
    "status": 1,
    "imageUrl": "http://...",
    "description": "全合成机油，5W-30粘度",
    "createdAt": "2026-06-06 10:00:00",
    "updatedAt": "2026-06-06 10:00:00"
  }
}
```

### 2.5 修改商品

```
PUT /api/v1/products/{id}
```

请求体与新增相同（全部字段可修改，SKU 不可修改）。

### 2.6 删除/状态变更

```
DELETE /api/v1/products/{id}
```

逻辑删除（将 status 置为 -1 或直接物理删除）。

```
PUT /api/v1/products/{id}/status
```

请求体：
```json
{
  "status": 0
}
```

### 2.7 获取商品分类列表

```
GET /api/v1/products/categories
```

响应：
```json
{
  "code": 200,
  "data": ["机油", "滤芯", "刹车片", "轮胎", "火花塞", "电瓶", "雨刮", "其他"]
}
```

> 分类从现有商品数据中 DISTINCT 获取，无需单独维护分类表。

---

## 3. 后端实现

### 3.1 文件清单

| 层 | 文件 | 说明 |
|----|------|------|
| model | `Product.java` | 商品实体 |
| dto | `ProductSaveRequest.java` | 新增/修改商品请求 |
| dto | `ProductVO.java` | 商品展示对象 |
| repository | `ProductMapper.java` | 商品数据访问 |
| service | `ProductService.java` | 商品业务逻辑 |
| controller | `ProductController.java` | 商品管理接口 |

### 3.2 关键逻辑

**搜索实现：**
- 使用 MyBatis-Plus QueryWrapper 拼接条件
- keyword 同时匹配 `name`、`sku`、`car_model` 三个字段（OR 条件）
- brand、category、status 为精确匹配

**SKU 唯一性校验：**
- 新增和修改时校验 SKU 不与其他商品重复
- SKU 一旦创建不可修改

**图片处理：**
- 本阶段支持 URL 形式存储图片地址
- 图片上传功能可选实现（可使用本地存储或对象存储）

---

## 4. 管理后台页面

### 4.1 商品列表页 (/products)

**页面布局：**

```
┌─────────────────────────────────────────────────────┐
│ [搜索框]  [品牌筛选]  [分类筛选]  [状态筛选]  [新增商品] │
├─────────────────────────────────────────────────────┤
│  # │ 图片 │ 商品名称│ SKU  │ 品牌│ 库存│ 售价 │ 状态│ 操作 │
│  1 │ [img]│ 嘉实多..│ JSD..│ 嘉实多│ 50 │ 298 │ 上架 │ 编辑│
│  2 │ [img]│ 博世... │ BS.. │ 博世 │ 0  │ 45  │ 缺货 │ 编辑│
│ ...                                                    │
├─────────────────────────────────────────────────────┤
│                       分页组件                        │
└─────────────────────────────────────────────────────┘
```

**功能要求：**
- 表格展示商品信息
- 多条件搜索与筛选
- 库存状态颜色标识（正常=绿色，不足=黄色，缺货=红色）
- 点击"编辑"打开编辑对话框
- 新增/编辑使用对话框（Dialog）
- 支持上下架快捷操作
- 删除需二次确认

### 4.2 商品新增/编辑对话框

**字段表单：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| 商品名称 | Input | ✓ | |
| SKU 编码 | Input | ✓ | 新增时可填，编辑时不可修改 |
| 品牌 | Input | | |
| 适配车型 | Input | | 多车型用"/"分隔 |
| 分类 | Select | | 从已有分类中选择 |
| 单位 | Input | ✓ | 默认"个" |
| 成本价 | Input Number | | |
| 销售价 | Input Number | ✓ | |
| 最低库存预警 | Input Number | | |
| 商品描述 | Textarea | | |
| 图片 | Upload | | 支持 URL 输入 |

---

## 5. 验收标准

| # | 检查项 | 预期结果 |
|---|--------|----------|
| 1 | 商品列表 | 分页显示，搜索筛选正常 |
| 2 | 新增商品 | 保存成功，列表显示新商品 |
| 3 | 编辑商品 | 修改后数据正确更新 |
| 4 | 商品详情 | 查看详情包含完整字段 |
| 5 | SKU 唯一性 | 重复 SKU 提示错误 |
| 6 | 上下架 | 下架商品在客户端不可见 |
| 7 | 库存标识 | 缺货/不足状态正确显示 |
| 8 | 搜索 | 关键字匹配名称/SKU/车型 |
| 9 | 分类列表 | 自动从已有数据生成 |

---

## 6. 依赖项

- Phase 0: 项目骨架、数据库
- Phase 1: 用户鉴权（管理后台需登录后操作）

---

## 7. 交付物清单

| 文件 | 说明 |
|------|------|
| `server/.../model/Product.java` | 商品实体 |
| `server/.../controller/ProductController.java` | 商品接口 |
| `server/.../service/ProductService.java` | 商品业务逻辑 |
| `server/.../repository/ProductMapper.java` | 商品数据访问 |
| `server/.../dto/ProductVO.java` | 商品展示对象 |
| `server/.../dto/ProductSaveRequest.java` | 商品保存请求 |
| `admin/src/views/ProductList.vue` | 商品列表页 |
| `admin/src/views/ProductForm.vue` | 商品编辑对话框 |
| `admin/src/api/product.js` | 商品 API |
