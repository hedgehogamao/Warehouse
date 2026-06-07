# Phase 3: 入库管理与库存管理

## 功能概述

实现商品入库管理和库存监控功能。店员录入入库信息后自动更新库存并生成流水记录；库存模块提供库存查询、预警和盘点调整功能。

**覆盖模块：** 入库管理、库存管理

---

## 1. 数据表变更

- 使用 Phase 0 已创建的 `stock_in` 和 `inventory_logs` 表
- 无需新增表

---

## 2. API 接口设计

Base URL: `/api/v1`

### 2.1 入库管理

```
POST   /api/v1/stock-in            # 新建入库单
GET    /api/v1/stock-in             # 入库记录列表（分页）
GET    /api/v1/stock-in/{id}       # 入库单详情
```

#### 新建入库

```
POST /api/v1/stock-in
```

请求体：
```json
{
  "productId": 1,
  "quantity": 50,
  "costPrice": 25.00,
  "remark": "2026年6月批次"
}
```

**核心逻辑（事务）：**
1. 插入 `stock_in` 记录
2. 更新 `products` 表的 `stock = stock + quantity`
3. 插入 `inventory_logs` 流水（type=STOCK_IN）

响应：
```json
{
  "code": 200,
  "message": "入库成功",
  "data": {
    "id": 1,
    "productId": 1,
    "quantity": 50,
    "beforeStock": 10,
    "afterStock": 60,
    "createdAt": "2026-06-06 14:00:00"
  }
}
```

#### 入库记录列表

```
GET /api/v1/stock-in?page=1&size=20&productId=1&startDate=2026-06-01&endDate=2026-06-30
```

查询参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| size | int | 每页条数 |
| productId | int | 按商品筛选 |
| startDate | string | 开始日期（yyyy-MM-dd） |
| endDate | string | 结束日期 |

响应：
```json
{
  "code": 200,
  "data": {
    "total": 10,
    "records": [
      {
        "id": 1,
        "productId": 1,
        "productName": "嘉实多极护 5W-30",
        "productSku": "JSD-5W30-4L",
        "quantity": 50,
        "costPrice": 25.00,
        "remark": "2026年6月批次",
        "operatorName": "张三",
        "createdAt": "2026-06-06 14:00:00"
      }
    ]
  }
}
```

### 2.2 库存查询

```
GET /api/v1/inventory                    # 库存列表（分页+筛选）
GET /api/v1/inventory/low-stock          # 库存预警列表
GET /api/v1/inventory/{productId}/logs   # 商品库存流水

PUT /api/v1/inventory/{productId}/adjust # 库存调整
```

#### 库存列表

```
GET /api/v1/inventory?page=1&size=20&keyword=&category=&lowStock=true
```

响应：
```json
{
  "code": 200,
  "data": {
    "total": 128,
    "records": [
      {
        "productId": 1,
        "productName": "嘉实多极护 5W-30",
        "sku": "JSD-5W30-4L",
        "brand": "嘉实多",
        "category": "机油",
        "stock": 60,
        "minStock": 10,
        "status": "NORMAL",
        "lastInDate": "2026-06-06 14:00:00",
        "lastSaleDate": "2026-06-05 16:30:00"
      }
    ]
  }
}
```

**库存状态计算：**
| 条件 | status | 说明 |
|------|--------|------|
| stock = 0 | OUT_OF_STOCK | 缺货 |
| 0 < stock ≤ minStock | LOW_STOCK | 库存不足 |
| stock > minStock | NORMAL | 正常 |

#### 库存预警列表

```
GET /api/v1/inventory/low-stock
```

返回所有 `stock ≤ minStock` 且 `status = 1`（上架）的商品。

#### 商品库存流水

```
GET /api/v1/inventory/{productId}/logs?page=1&size=20&startDate=&endDate=
```

响应：
```json
{
  "code": 200,
  "data": {
    "total": 30,
    "records": [
      {
        "id": 1,
        "type": "STOCK_IN",
        "typeName": "入库",
        "quantity": 50,
        "beforeStock": 10,
        "afterStock": 60,
        "refId": 1,
        "remark": "入库: 2026年6月批次",
        "operatorName": "张三",
        "createdAt": "2026-06-06 14:00:00"
      },
      {
        "id": 2,
        "type": "SALE",
        "typeName": "销售",
        "quantity": -2,
        "beforeStock": 60,
        "afterStock": 58,
        "refId": 1001,
        "remark": "销售订单: SO20260605001",
        "operatorName": "张三",
        "createdAt": "2026-06-05 16:30:00"
      }
    ]
  }
}
```

#### 库存调整

```
PUT /api/v1/inventory/{productId}/adjust
```

请求体：
```json
{
  "quantity": 5,
  "reason": "盘点调整，实物多出5个"
}
```

> quantity 可为正数（盘盈）或负数（盘亏）

**核心逻辑（事务）：**
1. 更新 `products` 表的 `stock = stock + quantity`
2. 插入 `inventory_logs`（type=ADJUST）

---

## 3. 后端实现

### 3.1 文件清单

| 层 | 文件 | 说明 |
|----|------|------|
| model | `StockInRecord.java` | 入库记录实体 |
| model | `InventoryLog.java` | 库存流水实体 |
| dto | `StockInRequest.java` | 入库请求参数 |
| dto | `StockInVO.java` | 入库记录展示对象 |
| dto | `InventoryVO.java` | 库存展示对象 |
| dto | `InventoryLogVO.java` | 库存流水展示对象 |
| dto | `InventoryAdjustRequest.java` | 库存调整请求 |
| repository | `StockInRecordMapper.java` | 入库数据访问 |
| repository | `InventoryLogMapper.java` | 库存流水数据访问 |
| service | `StockInService.java` | 入库业务逻辑 |
| service | `InventoryService.java` | 库存业务逻辑 |
| controller | `StockInController.java` | 入库管理接口 |
| controller | `InventoryController.java` | 库存管理接口 |

### 3.2 关键逻辑

**入库事务：**
```java
@Transactional
public void stockIn(StockInRequest request) {
    // 1. 查询商品
    Product product = productService.getById(request.getProductId());
    int beforeStock = product.getStock();

    // 2. 保存入库记录
    StockInRecord record = new StockInRecord();
    // ... 设置字段
    stockInRecordMapper.insert(record);

    // 3. 更新商品库存
    product.setStock(beforeStock + request.getQuantity());
    productMapper.updateById(product);

    // 4. 记录库存流水
    InventoryLog log = new InventoryLog();
    log.setProductId(request.getProductId());
    log.setType("STOCK_IN");
    log.setQuantity(request.getQuantity());
    log.setBeforeStock(beforeStock);
    log.setAfterStock(beforeStock + request.getQuantity());
    log.setRefId(record.getId());
    log.setRemark("入库: " + request.getRemark());
    log.setOperatorId(CurrentUser.getId());
    inventoryLogMapper.insert(log);
}
```

**库存预警：** 每次库存变动后检查是否触发预警，可在管理后台首页展示预警信息。

---

## 4. 管理后台页面

### 4.1 入库管理页 (/stock-in)

```
┌─────────────────────────────────────────────────┐
│  [商品搜索]  [日期筛选]  [+ 新增入库]           │
├─────────────────────────────────────────────────┤
│  # │ 时间 │ 商品名称 │ SKU │ 数量 │ 成本价 │ 操作人│
│  1 │ 06-06 │ 嘉实多.. │ JSD │ 50  │ 25.00 │ 张三  │
│  2 │ 06-05 │ 博世...  │ BS  │ 100 │ 18.00 │ 张三  │
├─────────────────────────────────────────────────┤
│                  分页                           │
└─────────────────────────────────────────────────┘
```

**新增入库对话框：**
- 商品选择器（搜索选择商品）
- 入库数量（Input Number）
- 成本价（Input Number）
- 备注（Textarea）
- 确认后显示入库前后的库存变化

### 4.2 库存管理页 (/inventory)

```
┌─────────────────────────────────────────────────┐
│  [搜索框]  [分类筛选]  [☐ 仅显示库存不足]  [库存调整] │
├─────────────────────────────────────────────────┤
│  # │ 商品名称 │ SKU │ 当前库存 │ 预警值 │ 状态 │ 操作    │
│  1 │ 嘉实多.. │ JSD │ 60      │ 10     │ 🟢正常 │ 流水│调整│
│  2 │ 博世...  │ BS  │ 5       │ 20     │ 🟡不足 │ 流水│调整│
│  3 │ 曼牌...  │ MAN │ 0       │ 10     │ 🔴缺货 │ 流水│调整│
├─────────────────────────────────────────────────┤
│                  分页                           │
└─────────────────────────────────────────────────┘
```

**库存流水弹窗：** 点击"流水"显示该商品详细的库存变动记录（入库/销售/调整）。

**库存调整弹窗：** 输入调整数量和原因，确认后更新库存。

### 4.3 首页预警展示（Dashboard）

```
┌─────────────────────────────────┐
│ ⚠ 库存预警                      │
│ ┌─────────────────────────────┐ │
│ │ 曼牌机油滤清器         库存: 0  │ │
│ │ 博世刹车片            库存: 5  │ │
│ │ ...                           │ │
│ └─────────────────────────────┘ │
└─────────────────────────────────┘
```

---

## 5. 验收标准

| # | 检查项 | 预期结果 |
|---|--------|----------|
| 1 | 商品入库 | 库存数量正确增加，流水记录生成 |
| 2 | 入库记录 | 列表可查询按商品和时间的入库历史 |
| 3 | 库存列表 | 显示所有商品库存，可搜索筛选 |
| 4 | 库存预警 | low-stock 接口返回库存不足的商品 |
| 5 | 库存流水 | 显示完整的入库/销售/调整记录 |
| 6 | 库存调整 | 盘盈盘亏后库存正确更新 |
| 7 | 预警展示 | 首页显示库存不足商品列表 |
| 8 | 事务一致性 | 入库失败时库存不回滚（异常测试） |

---

## 6. 依赖项

- Phase 0: 项目骨架、数据库
- Phase 1: 用户鉴权
- Phase 2: 商品数据

---

## 7. 交付物清单

| 文件 | 说明 |
|------|------|
| `server/.../model/StockInRecord.java` | 入库记录实体 |
| `server/.../model/InventoryLog.java` | 库存流水实体 |
| `server/.../controller/StockInController.java` | 入库接口 |
| `server/.../controller/InventoryController.java` | 库存接口 |
| `server/.../service/StockInService.java` | 入库业务逻辑 |
| `server/.../service/InventoryService.java` | 库存业务逻辑 |
| `server/.../repository/StockInRecordMapper.java` | 入库数据访问 |
| `server/.../repository/InventoryLogMapper.java` | 库存流水数据访问 |
| `admin/src/views/StockIn.vue` | 入库管理页 |
| `admin/src/views/Inventory.vue` | 库存管理页 |
| `admin/src/components/InventoryLogDialog.vue` | 库存流水弹窗 |
| `admin/src/components/InventoryAdjustDialog.vue` | 库存调整弹窗 |
| `admin/src/api/stock-in.js` | 入库 API |
| `admin/src/api/inventory.js` | 库存 API |
