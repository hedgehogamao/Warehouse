<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# service

## Purpose
业务逻辑层，封装核心业务操作。Service 层调用 Repository 层进行数据持久化。

## Expected Files
| File | Description |
|------|-------------|
| `UserService.java` | 用户登录验证、Token 生成 |
| `ProductService.java` | 商品增删改查业务逻辑 |
| `StockInService.java` | 入库处理 + 库存更新 |
| `OrderService.java` | 下单、支付、库存扣减事务 |
| `InventoryService.java` | 库存查询与盘点 |
| `HistoryService.java` | 操作日志与库存流水查询 |
| `StatisticsService.java` | 销售数据统计 |

## For AI Agents

### Working In This Directory
- Service 层是事务边界，使用 `@Transactional` 管理事务
- 关键业务（如销售扣库存）务必保证事务一致性
- Service 方法命名遵循: get/find(查询), save/add(新增), update(更新), delete(删除)
- 复杂查询条件使用 MyBatis-Plus QueryWrapper

<!-- MANUAL: -->
