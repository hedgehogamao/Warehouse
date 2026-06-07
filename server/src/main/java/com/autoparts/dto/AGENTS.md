<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# dto

## Purpose
数据传输对象 (DTO) 目录。包含请求参数对象 (Request)、响应视图对象 (VO)、分页参数等。

## Expected Files
| File | Description |
|------|-------------|
| `LoginRequest.java` | 登录请求参数 |
| `LoginResponse.java` | 登录响应 (Token) |
| `ProductVO.java` | 商品展示对象 |
| `OrderVO.java` | 订单展示对象 |
| `PageParam.java` | 分页请求参数 |
| `PageResult.java` | 分页响应结果 |

## For AI Agents

### Working In This Directory
- Request 类使用 `@NotBlank`、`@NotNull` 等校验注解
- VO 类用于前端展示，可组合多个实体字段
- 分页统一使用 PageParam + PageResult 模式
- DTO 不与实体直接耦合，按需定义

<!-- MANUAL: -->
