<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# repository

## Purpose
MyBatis-Plus Mapper 接口层，提供数据库 CRUD 操作。每个 Mapper 对应一个实体类和一张数据库表。

## Expected Files
| File | Description |
|------|-------------|
| `UserMapper.java` | 用户表数据访问 |
| `ProductMapper.java` | 商品表数据访问 |
| `StockInRecordMapper.java` | 入库记录表数据访问 |
| `OrderMapper.java` | 订单表数据访问 |
| `OrderItemMapper.java` | 订单明细表数据访问 |
| `InventoryLogMapper.java` | 库存流水表数据访问 |

## For AI Agents

### Working In This Directory
- 所有 Mapper 继承 `MyBatis-Plus BaseMapper<Entity>`
- 复杂查询使用 `@Select` 注解写自定义 SQL
- 多表关联查询使用 XML 或 `@Results` 注解
- 无需手动编写基础 CRUD，MyBatis-Plus 自动提供
- Mapper 接口上添加 `@Mapper` 或 `@Repository` 注解

<!-- MANUAL: -->
