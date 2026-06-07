<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# controller

## Purpose
REST API 控制器层，处理 HTTP 请求并返回统一格式的响应。所有 Controller 调用 Service 层执行业务逻辑。

## Expected Files
| File | Description |
|------|-------------|
| `AuthController.java` | 登录/注册接口 |
| `ProductController.java` | 商品 CRUD 接口 |
| `StockInController.java` | 入库管理接口 |
| `OrderController.java` | 销售订单接口 |
| `InventoryController.java` | 库存查询接口 |
| `HistoryController.java` | 历史记录查询接口 |
| `StatisticsController.java` | 数据统计接口 |

## For AI Agents

### Working In This Directory
- 统一使用 `@RestController` 和 `@RequestMapping("/api/v1/...")`
- 方法返回类型统一为 `Result<T>`
- 参数校验使用 `@Validated` 注解
- 分页参数使用 `@PageableDefault` 或自定义 PageParam

<!-- MANUAL: -->
