# Skill: SQL 数据库设计规范

> 适用于数据库工程师（`db-engineer`）和后端开发（`backend-dev`）

---

## 1 命名规范

### 1.1 数据库

- 数据库名：`auto_parts_store`
- 字符集：`utf8mb4`
- 排序规则：`utf8mb4_unicode_ci`
- 引擎：`InnoDB`

### 1.2 表名

- 使用 **snake_case**，复数名词：`users`、`products`、`orders`
- 模块前缀：关联表使用主表前缀，如 `order_items`
- 禁止使用 MySQL 保留字

### 1.3 字段名

- 使用 **snake_case**：`created_at`、`sale_price`、`operator_id`
- 主键统一使用 `id`（INT AUTO_INCREMENT）
- 外键命名：`{关联表单数}_id`，如 `product_id`、`order_id`
- 布尔状态字段：`status`（TINYINT，1=启用/0=禁用）
- 时间字段：`created_at`、`updated_at`

---

## 2 数据类型规范

| 用途 | 类型 | 示例 |
|------|------|------|
| 主键 | INT AUTO_INCREMENT | `id INT AUTO_INCREMENT PRIMARY KEY` |
| 外键 | INT | `product_id INT NOT NULL` |
| 字符串（短） | VARCHAR(N) | `username VARCHAR(50)` |
| 字符串（长） | TEXT | `description TEXT` |
| 金额 | DECIMAL(10,2) | `sale_price DECIMAL(10,2)` |
| 整数数量 | INT | `stock INT NOT NULL DEFAULT 0` |
| 布尔标志 | TINYINT | `status TINYINT NOT NULL DEFAULT 1` |
| 时间 | DATETIME | `created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP` |

---

## 3 每张表的标配字段

```sql
id          INT      AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
-- 业务字段 ...
created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
```

> `updated_at` 可选，但推荐包含。

---

## 4 索引规范

| 场景 | 索引类型 | 说明 |
|------|----------|------|
| 主键 | PRIMARY KEY | 自动创建 |
| 外键 | INDEX | `idx_{field}`，如 `idx_product_id` |
| 唯一约束 | UNIQUE INDEX | `sku VARCHAR(50) NOT NULL UNIQUE` |
| 高频查询字段 | INDEX | 如 `idx_created_at`、`idx_status` |
| 全文搜索 | FULLTEXT INDEX | 如 `FULLTEXT INDEX ft_name_desc (name, description)` |

**索引命名**：`idx_{字段名}` 或 `idx_{表名缩写}_{字段名}`

---

## 5 注释规范

- 每张表必须有 `COMMENT`
- 每个字段必须有 `COMMENT`
- 使用中文注释
- 枚举值在 COMMENT 中列出：`COMMENT '状态: 1上架 0下架'`

---

## 6 外键策略

- **不使用数据库级外键约束**（由应用层保证数据一致性）
- 外键约束作为注释保留在 SQL 文件中，便于理解关系
- 应用层通过 Service 层事务保证引用完整性

---

## 7 SQL 编写规范

- 关键字大写：`CREATE TABLE`、`INSERT INTO`、`SELECT`
- 每个建表语句前加模块注释分隔线
- 使用 `IF NOT EXISTS` 防止重复创建
- `CREATE DATABASE` 放在脚本最前面
- 初始化数据单独放在 `init-data.sql`

---

## 8 本项目表清单

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `users` | 用户表 | username, password, role, status |
| `products` | 商品表 | name, sku, brand, category, stock, sale_price |
| `stock_in` | 入库记录 | product_id, quantity, cost_price, operator_id |
| `orders` | 销售订单 | order_no, customer_id, total_amount, payment_method, status |
| `order_items` | 订单明细 | order_id, product_id, quantity, unit_price, subtotal |
| `inventory_logs` | 库存流水 | product_id, type, quantity, before_stock, after_stock |
| `customers` | 客户表 | open_id, nick_name, phone |
| `operation_logs` | 操作日志 | module, action, content, operator_id |

---

## 9 文件产出

| 文件 | 说明 |
|------|------|
| `docs/database/schema.sql` | 完整建表脚本 |
| `docs/database/init-data.sql` | 初始数据（管理员账号等） |
