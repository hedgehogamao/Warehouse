<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-06 | Updated: 2026-06-06 -->

# common

## Purpose
公共工具类和基础设施代码，包括统一响应封装、全局异常处理、常量定义、工具类等。

## Expected Files
| File | Description |
|------|-------------|
| `Result.java` | 统一响应体 `{code, message, data}` |
| `ResultCode.java` | 响应状态码枚举 |
| `GlobalExceptionHandler.java` | 全局异常拦截器 |
| `BusinessException.java` | 自定义业务异常 |
| `JwtUtil.java` | JWT Token 工具类 |
| `Constants.java` | 系统常量定义 |

## For AI Agents

### Working In This Directory
- `Result.java` 是所有 API 响应的统一包装，必须全局使用
- `GlobalExceptionHandler` 处理所有已知和未知异常
- `BusinessException` 用于业务逻辑中主动抛出异常
- `JwtUtil` 负责 Token 的生成和验证

<!-- MANUAL: -->
