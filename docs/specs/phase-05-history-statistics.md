# Phase 5: 历史记录与数据统计

## 功能概述

实现操作日志查询、库存流水追溯和数据统计看板功能。帮助门店了解经营状况、分析销售趋势和监控库存变动。

**覆盖模块：** 历史记录、数据统计

---

## 1. 数据表变更

- 无需新增表
- 使用已有的 `inventory_logs` 表

---

## 2. API 接口设计

Base URL: `/api/v1`

### 2.1 库存流水查询

```
GET /api/v1/inventory-logs?page=1&size=20&type=&productId=&startDate=&endDate=
```

查询参数：

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| size | int | 每页条数 |
| type | string | 筛选类型: STOCK_IN/SALE/ADJUST |
| productId | int | 按商品筛选 |
| productName | string | 按商品名称搜索 |
| startDate | string | 开始日期 |
| endDate | string | 结束日期 |

响应：
```json
{
  "code": 200,
  "data": {
    "total": 500,
    "records": [
      {
        "id": 100,
        "productId": 1,
        "productName": "嘉实多极护 5W-30",
        "productSku": "JSD-5W30-4L",
        "type": "SALE",
        "typeName": "销售出库",
        "quantity": -2,
        "beforeStock": 60,
        "afterStock": 58,
        "refId": 1001,
        "refName": "SO202606060001",
        "remark": "销售订单: SO202606060001",
        "operatorName": "张三",
        "createdAt": "2026-06-06 15:00:00"
      }
    ]
  }
}
```

### 2.2 操作日志查询

```
GET /api/v1/operation-logs?page=1&size=20&module=&operatorId=&startDate=&endDate=
```

操作日志记录用户的关键操作（登录、新增商品、入库、下单、退款等）。

| 参数 | 类型 | 说明 |
|------|------|------|
| page | int | 页码 |
| size | int | 每页条数 |
| module | string | 模块筛选: AUTH/PRODUCT/STOCK_IN/ORDER/INVENTORY |
| operatorId | int | 按操作员筛选 |
| startDate | string | 开始日期 |
| endDate | string | 结束日期 |

响应：
```json
{
  "code": 200,
  "data": {
    "total": 200,
    "records": [
      {
        "id": 1,
        "module": "ORDER",
        "moduleName": "销售管理",
        "action": "CREATE_ORDER",
        "actionName": "创建订单",
        "content": "创建订单 SO202606060001，金额 641.00",
        "refId": 1001,
        "operatorName": "张三",
        "createdAt": "2026-06-06 15:00:00"
      }
    ]
  }
}
```

> **说明：** 操作日志通过 AOP 切面自动记录，在 Service 层的关键方法上添加 `@OperationLog(module="ORDER", action="CREATE_ORDER")` 注解实现。

### 2.3 数据统计

#### 今日概况

```
GET /api/v1/statistics/today
```

响应：
```json
{
  "code": 200,
  "data": {
    "todaySalesAmount": 1856.00,
    "todayOrderCount": 7,
    "todayStockInCount": 3,
    "todayNewProductCount": 0,
    "lowStockCount": 5,
    "outOfStockCount": 2,
    "totalProducts": 128,
    "totalOrders": 456
  }
}
```

#### 销售趋势

```
GET /api/v1/statistics/sales-trend?days=7
```

响应：
```json
{
  "code": 200,
  "data": {
    "days": ["06-01", "06-02", "06-03", "06-04", "06-05", "06-06", "06-07"],
    "amounts": [1200, 800, 1500, 2000, 1100, 1856, 0],
    "counts": [5, 3, 6, 8, 4, 7, 0]
  }
}
```

#### 销售排行

```
GET /api/v1/statistics/top-products?days=30&limit=10
```

响应：
```json
{
  "code": 200,
  "data": [
    {
      "productId": 1,
      "productName": "嘉实多极护 5W-30",
      "totalQuantity": 15,
      "totalAmount": 4470.00,
      "category": "机油"
    },
    {
      "productId": 3,
      "productName": "博世机油滤清器",
      "totalQuantity": 20,
      "totalAmount": 900.00,
      "category": "滤芯"
    }
  ]
}
```

#### 分类占比

```
GET /api/v1/statistics/category-distribution?days=30
```

响应：
```json
{
  "code": 200,
  "data": [
    { "category": "机油", "amount": 8900.00, "percentage": 45.5 },
    { "category": "滤芯", "amount": 3500.00, "percentage": 17.9 },
    { "category": "刹车片", "amount": 4200.00, "percentage": 21.5 },
    { "category": "其他", "amount": 2950.00, "percentage": 15.1 }
  ]
}
```

---

## 3. 后端实现

### 3.1 文件清单

| 层 | 文件 | 说明 |
|----|------|------|
| model | `OperationLog.java` | 操作日志实体 |
| repository | `InventoryLogMapper.java` | (已有) 库存流水查询 |
| repository | `OperationLogMapper.java` | 操作日志数据访问 |
| service | `HistoryService.java` | 历史记录查询业务逻辑 |
| service | `StatisticsService.java` | 数据统计业务逻辑 |
| controller | `HistoryController.java` | 历史记录接口 |
| controller | `StatisticsController.java` | 数据统计接口 |
| aop | `OperationLogAspect.java` | 操作日志 AOP 切面 |
| annotation | `OperationLog.java` | 操作日志注解 |

### 3.2 关键逻辑

**操作日志自动记录（AOP）：**

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {
    String module();
    String action();
    String description() default "";
}
```

```java
@Aspect
@Component
public class OperationLogAspect {
    @Around("@annotation(operationLog)")
    public Object logOperation(ProceedingJoinPoint joinPoint, OperationLog operationLog) {
        // 记录操作内容、操作人、时间
        // 执行目标方法
        // 异常时记录失败日志
    }
}
```

使用示例：
```java
@OperationLog(module = "ORDER", action = "CREATE_ORDER", description = "创建订单")
public OrderVO createOrder(OrderCreateRequest request) {
    // ...
}
```

### 3.3 统计数据说明

| 统计项 | SQL 实现 |
|--------|----------|
| 今日销售额 | `SELECT SUM(total_amount) FROM orders WHERE DATE(created_at) = CURDATE() AND status='PAID'` |
| 销售趋势 | `SELECT DATE(created_at), SUM(total_amount), COUNT(*) FROM orders WHERE ... GROUP BY DATE(created_at)` |
| 商品排行 | `SELECT product_id, SUM(quantity), SUM(subtotal) FROM order_items GROUP BY product_id ORDER BY SUM(subtotal) DESC` |
| 分类占比 | `SELECT p.category, SUM(oi.subtotal) ... JOIN products p GROUP BY p.category` |

---

## 4. 管理后台页面

### 4.1 首页看板 (/dashboard)

```
┌──────────────────────────────────────────────────────┐
│  今日销售额     今日订单    入库次数    库存预警       │
│  ¥1,856.00     7 单        3 次       7 项           │
├──────────────────────────────────────────────────────┤
│ ┌─ 销售趋势(7日) ───────────────────────────┐        │
│ │          📈 折线图                        │        │
│ │  (ECharts 折线图展示每日销售额和订单量)     │        │
│ └──────────────────────────────────────────┘        │
├──────────────────┬───────────────────────────────────┤
│ 销售排行 TOP5    │ 分类占比                          │
│ 1. 嘉实多极护     │ ┌─────────┐                     │
│ 2. 博世滤清器     │ │  🥧 饼图  │                     │
│ 3. ...            │ └─────────┘                     │
└──────────────────┴───────────────────────────────────┘
```

### 4.2 历史记录页 (/history)

**标签页切换：**
- Tab 1: 库存流水
- Tab 2: 操作日志

库存流水：表格展示，支持按类型/商品/时间筛选
操作日志：表格展示，支持按模块/操作人/时间筛选

### 4.3 数据统计页 (/statistics)

**更全面的统计分析：**
- 时间范围选择器（今日/本周/本月/自定义）
- 销售趋势图（ECharts 折线图）
- 商品销售排行（柱状图 + 表格）
- 分类占比（饼图）
- 每日/每月对比

---

## 5. 验收标准

| # | 检查项 | 预期结果 |
|---|--------|----------|
| 1 | 库存流水 | 可按多种条件查询，数据准确 |
| 2 | 操作日志 | 关键操作自动记录，可追溯 |
| 3 | 今日概况 | 数据实时准确 |
| 4 | 销售趋势 | 折线图展示正确 |
| 5 | 商品排行 | 按销售额排序正确 |
| 6 | 分类占比 | 饼图百分比计算正确 |
| 7 | 首页看板 | 数据加载正常，无报错 |

---

## 6. 依赖项

- Phase 0: 项目骨架、数据库
- Phase 1: 用户鉴权
- Phase 2: 商品数据
- Phase 3: 库存流水数据
- Phase 4: 订单数据

---

## 7. 交付物清单

| 文件 | 说明 |
|------|------|
| `server/.../model/OperationLog.java` | 操作日志实体 |
| `server/.../repository/OperationLogMapper.java` | 操作日志数据访问 |
| `server/.../service/HistoryService.java` | 历史记录业务逻辑 |
| `server/.../service/StatisticsService.java` | 数据统计业务逻辑 |
| `server/.../controller/HistoryController.java` | 历史记录接口 |
| `server/.../controller/StatisticsController.java` | 数据统计接口 |
| `server/.../aop/OperationLogAspect.java` | 操作日志切面 |
| `server/.../annotation/OperationLog.java` | 操作日志注解 |
| `admin/src/views/Dashboard.vue` | 首页看板 |
| `admin/src/views/History.vue` | 历史记录页 |
| `admin/src/views/Statistics.vue` | 数据统计页 |
| `admin/src/api/history.js` | 历史记录 API |
| `admin/src/api/statistics.js` | 数据统计 API |
