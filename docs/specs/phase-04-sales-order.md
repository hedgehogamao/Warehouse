# Phase 4: 销售收银与订单管理

## 功能概述

实现门店销售收银功能，店员选择商品、确认数量、生成订单并完成扣库存操作。同时提供订单查询和退款处理功能。

**覆盖模块：** 销售管理

---

## 1. 数据表变更

- 使用 Phase 0 已创建的 `orders` 和 `order_items` 表
- 无需新增表

---

## 2. API 接口设计

Base URL: `/api/v1`

### 2.1 销售下单

```
POST /api/v1/orders
```

请求体：
```json
{
  "customerName": "李四",
  "customerPhone": "13700137000",
  "paymentMethod": "WECHAT",
  "remark": "",
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "unitPrice": 298.00
    },
    {
      "productId": 3,
      "quantity": 1,
      "unitPrice": 45.00
    }
  ]
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| customerName | string | | 客户姓名（可选） |
| customerPhone | string | | 客户电话（可选） |
| paymentMethod | string | ✓ | CASH/WECHAT/ALIPAY/CARD |
| remark | string | | 备注 |
| items[].productId | int | ✓ | 商品 ID |
| items[].quantity | int | ✓ | 购买数量（≥ 1） |
| items[].unitPrice | decimal | ✓ | 销售单价（可不同于商品表售价） |

**核心逻辑（事务）：**
1. 校验所有商品是否存在且已上架
2. 校验库存是否充足
3. 生成订单编号（规则: SO + yyyyMMdd + 4位序号，如 SO202606060001）
4. 计算总金额
5. 插入 `orders`
6. 遍历 items 插入 `order_items`（含商品名称快照）
7. 逐个扣减商品库存
8. 记录库存流水（type=SALE）
9. 返回完整订单信息

响应：
```json
{
  "code": 200,
  "message": "下单成功",
  "data": {
    "id": 1,
    "orderNo": "SO202606060001",
    "customerName": "李四",
    "customerPhone": "13700137000",
    "totalAmount": 641.00,
    "paymentMethod": "WECHAT",
    "status": "PAID",
    "createdAt": "2026-06-06 15:00:00",
    "items": [
      {
        "productId": 1,
        "productName": "嘉实多极护 5W-30",
        "quantity": 2,
        "unitPrice": 298.00,
        "subtotal": 596.00
      },
      {
        "productId": 3,
        "productName": "博世机油滤清器",
        "quantity": 1,
        "unitPrice": 45.00,
        "subtotal": 45.00
      }
    ]
  }
}
```

### 2.2 订单列表

```
GET /api/v1/orders?page=1&size=20&orderNo=&customerName=&startDate=&endDate=
```

查询参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| size | int | 每页条数 |
| orderNo | string | 按订单编号搜索 |
| customerName | string | 按客户姓名搜索 |
| startDate | string | 开始日期 |
| endDate | string | 结束日期 |

响应：
```json
{
  "code": 200,
  "data": {
    "total": 50,
    "records": [
      {
        "id": 1,
        "orderNo": "SO202606060001",
        "customerName": "李四",
        "totalAmount": 641.00,
        "paymentMethod": "WECHAT",
        "paymentMethodName": "微信支付",
        "status": "PAID",
        "itemCount": 2,
        "operatorName": "张三",
        "createdAt": "2026-06-06 15:00:00"
      }
    ]
  }
}
```

### 2.3 订单详情

```
GET /api/v1/orders/{id}
```

响应：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "orderNo": "SO202606060001",
    "customerName": "李四",
    "customerPhone": "13700137000",
    "totalAmount": 641.00,
    "paymentMethod": "WECHAT",
    "paymentMethodName": "微信支付",
    "status": "PAID",
    "remark": "",
    "operatorId": 1,
    "operatorName": "张三",
    "createdAt": "2026-06-06 15:00:00",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "嘉实多极护 5W-30",
        "quantity": 2,
        "unitPrice": 298.00,
        "subtotal": 596.00
      }
    ]
  }
}
```

### 2.4 订单退款

```
PUT /api/v1/orders/{id}/refund
```

请求体：
```json
{
  "remark": "客户退货退款"
}
```

**核心逻辑（事务）：**
1. 校验订单状态为 PAID（已支付）
2. 将订单状态改为 REFUNDED
3. 逐项恢复商品库存（+原购买数量）
4. 记录库存流水（type=SALE, quantity=正数表示恢复）

### 2.5 获取商品信息（POS 使用）

```
GET /api/v1/products/pos-list?keyword=嘉实多
```

快速搜索商品用于 POS 选择（仅返回 id, name, sku, salePrice, stock）。

响应：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "嘉实多极护 5W-30",
      "sku": "JSD-5W30-4L",
      "salePrice": 298.00,
      "stock": 60
    }
  ]
}
```

---

## 3. 后端实现

### 3.1 文件清单

| 层 | 文件 | 说明 |
|----|------|------|
| model | `Order.java` | 订单实体 |
| model | `OrderItem.java` | 订单明细实体 |
| dto | `OrderCreateRequest.java` | 下单请求 |
| dto | `OrderVO.java` | 订单展示对象 |
| dto | `OrderItemVO.java` | 订单明细展示对象 |
| dto | `PosProductVO.java` | POS 商品选择展示对象 |
| repository | `OrderMapper.java` | 订单数据访问 |
| repository | `OrderItemMapper.java` | 订单明细数据访问 |
| service | `OrderService.java` | 订单业务逻辑 |
| controller | `OrderController.java` | 订单管理接口 |

### 3.2 关键逻辑

**订单编号生成：**
```java
private String generateOrderNo() {
    String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    // 从数据库查询当日最大序号
    String maxNo = orderMapper.getMaxOrderNo(date);
    int seq = 1;
    if (maxNo != null) {
        seq = Integer.parseInt(maxNo.substring(maxNo.length() - 4)) + 1;
    }
    return "SO" + date + String.format("%04d", seq);
}
```

**下单事务：**
```java
@Transactional
public OrderVO createOrder(OrderCreateRequest request) {
    // 1. 校验商品和库存
    for (ItemDTO item : request.getItems()) {
        Product product = productService.getById(item.getProductId());
        if (product == null || product.getStatus() != 1) {
            throw new BusinessException("商品不存在或已下架: " + item.getProductId());
        }
        if (product.getStock() < item.getQuantity()) {
            throw new BusinessException("库存不足: " + product.getName());
        }
    }

    // 2. 插入订单
    // 3. 插入明细 + 扣库存 + 记流水
    // ...
}
```

**库存扣减：** 使用乐观锁或 `UPDATE ... WHERE stock >= quantity` 防止超卖。

---

## 4. 管理后台页面

### 4.1 POS 收银页 (/sales)

```
┌──────────────────────────────┬──────────────────────────┐
│  POS 收银                    │  购物清单                  │
│                              │                          │
│  [搜索商品: 输入名称/SKU...]  │  ┌────────────────────┐  │
│                              │  │ 嘉实多极护 x2  596  │  │
│  ┌────────────────────────┐  │  │ 博世滤清器 x1  45   │  │
│  │ 嘉实多极护 5W-30  ¥298 │  │  └────────────────────┘  │
│  │ 库存: 60  [加入]       │  │                          │
│  ├────────────────────────┤  │  合计: ¥641.00           │
│  │ 博世机油滤清器  ¥45    │  │                          │
│  │ 库存: 100  [加入]      │  │  客户姓名: [________]    │
│  ├────────────────────────┤  │  联系电话: [________]    │
│  │ ...                    │  │                          │
│  └────────────────────────┘  │  支付方式:                │
│                              │  [现金] [微信] [支付宝] [刷卡] │
│                              │                          │
│                              │  [  确认收款  ] [  清空  ] │
└──────────────────────────────┴──────────────────────────┘
```

**功能要求：**
- 左侧商品搜索（实时搜索，防抖 300ms）
- 点击"加入"将商品添加到右侧购物清单
- 购物清单支持修改数量和删除
- 自动计算合计金额
- 客户信息可选录入
- 支付方式选择
- 确认收款后生成订单，清空购物清单
- 快捷键支持（F8 收款、F9 清空）

### 4.2 订单管理页 (/orders)

```
┌──────────────────────────────────────────────────────┐
│  订单编号: [____]  客户: [____]  日期: [____]～[____]  [查询] │
├──────────────────────────────────────────────────────┤
│  # │ 订单号 │ 客户 │ 金额 │ 支付方式 │ 商品数 │ 时间 │ 操作 │
│  1 │ SO202..│ 李四 │ 641 │ 微信     │ 2     │ 06-06│ 详情│退款│
│  2 │ SO202..│ 王五 │ 128 │ 现金     │ 1     │ 06-05│ 详情│    │
├──────────────────────────────────────────────────────┤
│                   分页                               │
└──────────────────────────────────────────────────────┘
```

**订单详情弹窗：**
- 显示订单完整信息
- 商品明细列表
- 退款操作（仅已支付订单可退款，需二次确认）
- 打印小票（可选功能）

---

## 5. 验收标准

| # | 检查项 | 预期结果 |
|---|--------|----------|
| 1 | POS 搜索 | 关键字搜索返回匹配商品 |
| 2 | 添加/删除商品 | 购物清单可增减商品 |
| 3 | 自动计算 | 金额按数量和单价正确计算 |
| 4 | 下单成功 | 库存扣减，订单和明细生成 |
| 5 | 库存不足提示 | 超过库存时禁止下单并提示 |
| 6 | 订单列表 | 可按条件搜索和筛选 |
| 7 | 订单详情 | 显示完整商品和金额信息 |
| 8 | 退款 | 退款后库存恢复，状态变更 |
| 9 | 支付方式 | 四种支付方式可选 |
| 10 | 事务安全 | 异常时所有操作回滚 |

---

## 6. 依赖项

- Phase 0: 项目骨架、数据库
- Phase 1: 用户鉴权
- Phase 2: 商品数据
- Phase 3: 库存管理（库存扣减和流水写入）

---

## 7. 交付物清单

| 文件 | 说明 |
|------|------|
| `server/.../model/Order.java` | 订单实体 |
| `server/.../model/OrderItem.java` | 订单明细实体 |
| `server/.../controller/OrderController.java` | 订单接口 |
| `server/.../service/OrderService.java` | 订单业务逻辑 |
| `server/.../repository/OrderMapper.java` | 订单数据访问 |
| `server/.../repository/OrderItemMapper.java` | 订单明细数据访问 |
| `admin/src/views/POS.vue` | POS 收银页 |
| `admin/src/views/OrderList.vue` | 订单管理页 |
| `admin/src/components/OrderDetailDialog.vue` | 订单详情弹窗 |
| `admin/src/api/order.js` | 订单 API |
