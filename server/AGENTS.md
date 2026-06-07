<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# server

## Purpose
Spring Boot 3.x 后端服务，提供 RESTful API 供管理后台和微信小程序调用。采用分层架构：Controller → Service → Repository → MySQL。

## Key Files
| File | Description |
|------|-------------|
| `pom.xml` | Maven 依赖配置 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/main/java/com/autoparts/` | Java 源代码 (see `src/main/java/com/autoparts/AGENTS.md`) |
| `src/main/resources/` | 配置文件 (application.yml, SQL 等) |
| `src/test/java/com/autoparts/` | 单元测试 |

## For AI Agents

### Working In This Directory
- 所有 API 路径以 `/api/v1/` 开头
- 统一使用 `Result<T>` 包装响应体
- 使用 `@Validated` 进行参数校验
- 使用 `GlobalExceptionHandler` 统一处理异常
- 数据库操作统一使用 MyBatis-Plus
- 每个表对应一个 Entity、Mapper、Service、Controller

### Testing Requirements
- Service 层必须写单元测试
- Controller 层写集成测试
- 测试覆盖率不低于 80%

### Common Patterns
- Entity 统一继承 `BaseEntity` (含 id, createdAt, updatedAt)
- Service 继承 `IService<Entity>` 和 `ServiceImpl<Mapper, Entity>`
- 分页查询使用 `PageHelper` 或 MyBatis-Plus 分页插件

## Dependencies

### Internal
- `docs/database/` - 数据库 Schema 定义

### External
- Spring Boot 3.x, Spring Security, Spring Validation
- MyBatis-Plus, MySQL 8.0
- JWT (jjwt), Lombok, Hutool

<!-- MANUAL: -->
