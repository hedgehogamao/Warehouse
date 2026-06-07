<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# config

## Purpose
Spring 配置类目录，管理所有框架级配置。

## Expected Files
| File | Description |
|------|-------------|
| `SecurityConfig.java` | Spring Security 配置 (JWT 鉴权、密码加密) |
| `CorsConfig.java` | 跨域配置 |
| `MyBatisPlusConfig.java` | MyBatis-Plus 分页插件与配置 |
| `WebMvcConfig.java` | Web MVC 配置 |
| `JacksonConfig.java` | JSON 序列化配置 |

## For AI Agents

### Working In This Directory
- SecurityConfig 是核心配置，所有 API 路径的权限在此定义
- CORS 配置需允许管理后台和小程序的域名
- Phase 0 先创建基础配置，后续 Phase 按需补充

<!-- MANUAL: -->
