<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# database

## Purpose
数据库设计文档与 SQL 脚本。包含完整的建表语句、初始化数据脚本和数据库设计说明。

## Key Files
| File | Description |
|------|-------------|
| `schema.sql` | 完整建表语句（所有表） |
| `init-data.sql` | 初始化数据（管理员账号、初始商品分类等） |
| `schema.md` | 数据库设计说明（ER 图、表关系等） |

## Database Overview

### Tables
| Table | Description |
|-------|-------------|
| `users` | 用户表（店员/管理员） |
| `products` | 商品表 |
| `stock_in` | 入库记录表 |
| `orders` | 销售订单表 |
| `order_items` | 销售明细表 |
| `inventory_logs` | 库存流水表 |

### Core Relationships
```
products ——< stock_in        (商品 1:N 入库记录)
products ——< inventory_logs  (商品 1:N 库存流水)
orders   ——< order_items     (订单 1:N 销售明细)
order_items ——< products     (明细 N:1 商品)
```

## For AI Agents

### Working In This Directory
- 所有表使用 `utf8mb4` 字符集
- 使用 InnoDB 引擎
- 时间字段统一使用 `datetime` 类型
- 价格字段使用 `decimal(10,2)`
- 新增字段需同步更新 `schema.sql`
- 每个表需包含 `id`(主键) 和 `created_at`(创建时间)

<!-- MANUAL: -->
