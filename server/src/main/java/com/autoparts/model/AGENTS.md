<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# model

## Purpose
数据实体类目录，每个实体对应数据库中的一张表。使用 MyBatis-Plus 注解映射表结构和字段。

## Expected Files
| File | Description |
|------|-------------|
| `User.java` | 用户实体 |
| `Product.java` | 商品实体 |
| `StockInRecord.java` | 入库记录实体 |
| `Order.java` | 销售订单实体 |
| `OrderItem.java` | 销售明细实体 |
| `InventoryLog.java` | 库存流水实体 |

## For AI Agents

### Working In This Directory
- 使用 `@TableName` 指定表名，`@TableId` 指定主键
- 使用 Lombok `@Data` 注解生成 getter/setter
- 所有实体继承 BaseEntity (id, createdAt, updatedAt)
- 时间字段类型使用 `LocalDateTime`
- 金额字段类型使用 `BigDecimal`

<!-- MANUAL: -->
