<!-- Parent: ../../../../../../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# com.autoparts (Server Source Root)

## Purpose
Spring Boot 后端服务 Java 源代码根包，采用分层架构组织代码。

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `config/` | Spring 配置类 (Security, CORS, MyBatis-Plus 等) |
| `controller/` | REST API 控制器层 |
| `service/` | 业务逻辑层 |
| `repository/` | MyBatis-Plus Mapper 接口 |
| `model/` | 实体类 (Entity) |
| `dto/` | 数据传输对象 (VO, Request, Response) |
| `common/` | 公共工具类 (Result, 异常, 常量等) |

## Key Files
| File | Description |
|------|-------------|
| `AutoPartsApplication.java` | Spring Boot 启动类 |

## For AI Agents

### Working In This Directory
- 新增功能模块时，需在 controller/service/repository/model/dto 各层分别添加文件
- 遵循 RESTful 命名规范
- 所有 controller 类添加 `@RestController` 和 `@RequestMapping("/api/v1/...")`

<!-- MANUAL: -->
