# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目简介

汽车配件门店库存管理与客户查询系统。面向单一门店、1 名店员、约 200 名客户。
- 后端：Spring Boot 3.2.x + JDK 17 + MyBatis-Plus
- 管理后台：Vue 3 + Element Plus + Vite + Pinia
- 客户端：微信小程序（原生）
- 数据库：MySQL 8.0（库名：`auto_parts_store`）

**当前状态：** Phase 0 进行中 — 仅有文档和空目录骨架，尚无实际代码和构建配置。

---

## 构建与运行命令

> 以下命令在 Phase 0 完成后可用。

**后端 (server/)**
```bash
cd server
mvn clean compile              # 编译
mvn spring-boot:run            # 启动（端口 8080）
mvn test                       # 运行全部测试
mvn test -Dtest=ClassName      # 运行单个测试类
mvn test -Dtest=ClassName#method  # 运行单个测试方法
```

**管理后台 (admin/)**
```bash
cd admin
npm install                    # 安装依赖
npm run dev                    # 启动开发服务器
npm run build                  # 生产构建
```

**数据库**
```bash
mysql -u root -p auto_parts_store < docs/database/schema.sql      # 建表
mysql -u root -p auto_parts_store < docs/database/init-data.sql   # 初始化数据
```

默认管理员账号：`admin / admin123`

---

## 开发工作流

### 每次会话标准流程

```
1. 读取 devlog/progress.md → 确认当前 Phase 和进度
2. 读取当前 Phase 的 Spec 文件（docs/specs/phase-XX-*.md）
3. 读取相关 Skill 文件（docs/skills/*.md）→ 了解编码规范
4. 读取 devlog/ 下最新的日志 → 了解上次做到哪里
5. 按 docs/development-plan.md 中的 Step 顺序执行
6. 每完成一个 Step → 更新 devlog 当日日志
7. Phase 完成 → 更新 devlog/progress.md 状态
```

### 核心原则

1. **小步推进** — 每次只做一个 Step，完成并验证后再做下一个
2. **先编译再填充** — 先让项目能跑起来，再逐步添加业务逻辑
3. **后端先行** — 每个 Phase 先完成 API，再做前端页面
4. **写完即测** — 每个 API 完成后立即用 curl 验证
5. **不跨 Phase** — 当前 Phase 验收全部通过后，才进入下一 Phase
6. **Spec 驱动** — 编码前必须先读对应 Spec 和 Skill 文件

---

## API 约定

- Base URL：`/api/v1`
- 响应格式：`{ "code": 200, "message": "success", "data": {...} }`
- 分页参数：`page`（从 1 开始）、`size`（默认 20）
- 认证：`Authorization: Bearer {token}`（JWT，有效期 24h）

---

## 关键技术约束

- 价格字段使用 `BigDecimal`，禁止 `float`/`double`
- 密码使用 BCrypt 加密存储
- API 响应统一使用 `Result<T>` 格式（定义在 `com.autoparts.common`）
- 中文注释覆盖率 > 80%
- 数据库共 8 张表：users, products, stock_in, orders, order_items, inventory_logs, customers, operation_logs

---

## 代码结构

### 后端包结构 (server/src/main/java/com/autoparts/)
```
controller/     → REST 控制器
service/        → 业务接口
service/impl/   → 业务实现
repository/     → MyBatis-Plus Mapper 接口
model/          → 数据库实体
dto/            → 请求/响应对象
common/         → Result, BusinessException, JwtUtil, Constants
config/         → SecurityConfig, CorsConfig, MyBatisPlusConfig, JacksonConfig
aop/            → 操作日志切面
annotation/     → 自定义注解
```

### 前端目录结构 (admin/src/)
```
api/            → Axios 请求封装
views/          → 页面组件
components/     → 公共组件
layouts/        → 布局组件
router/         → 路由（Vue Router 4）
store/          → Pinia 状态管理
utils/          → 工具函数
```

---

## 核心文件索引

| 类别 | 文件 | 用途 |
|------|------|------|
| **入口** | [AGENTS.md](AGENTS.md) | Agent 角色、Skill 定义、Phase 流水线 |
| **进度** | [devlog/progress.md](devlog/progress.md) | 当前 Phase 状态 |
| **计划** | [docs/development-plan.md](docs/development-plan.md) | 分步开发计划 |
| **数据库** | [docs/database/schema.sql](docs/database/schema.sql) | 完整建表脚本（8 张表） |

### Phase Spec（每个 Phase 要做什么）
| 文件 | Phase |
|------|-------|
| [phase-00-scaffold.md](docs/specs/phase-00-scaffold.md) | Phase 0：项目骨架与数据库 |
| [phase-01-user-management.md](docs/specs/phase-01-user-management.md) | Phase 1：用户认证与权限 |
| [phase-02-product-management.md](docs/specs/phase-02-product-management.md) | Phase 2：商品管理 |
| [phase-03-stock-in-inventory.md](docs/specs/phase-03-stock-in-inventory.md) | Phase 3：入库与库存管理 |
| [phase-04-sales-order.md](docs/specs/phase-04-sales-order.md) | Phase 4：销售收银与订单 |
| [phase-05-history-statistics.md](docs/specs/phase-05-history-statistics.md) | Phase 5：历史记录与统计 |
| [phase-06-mini-program.md](docs/specs/phase-06-mini-program.md) | Phase 6：客户微信小程序 |
| [phase-07-deployment.md](docs/specs/phase-07-deployment.md) | Phase 7：测试与部署 |

### Skill 规范（怎么写代码）
| 文件 | 适用场景 |
|------|----------|
| [sql.md](docs/skills/sql.md) | 数据库设计、命名、索引 |
| [springboot.md](docs/skills/springboot.md) | Spring Boot 分层架构、DTO/VO、事务 |
| [vue3.md](docs/skills/vue3.md) | Vue3 + Element Plus 前端规范 |
| [miniprogram.md](docs/skills/miniprogram.md) | 微信小程序开发规范 |
| [api.md](docs/skills/api.md) | RESTful API 设计 |
| [test.md](docs/skills/test.md) | 测试策略与用例编写 |

---

## gstack

- 所有网页浏览操作使用 `/browse` skill（来自 gstack），禁止使用 `mcp__claude-in-chrome__*` 工具
- 可用 skills：`/office-hours`, `/plan-ceo-review`, `/plan-eng-review`, `/plan-design-review`, `/design-consultation`, `/design-shotgun`, `/design-html`, `/review`, `/ship`, `/land-and-deploy`, `/canary`, `/benchmark`, `/browse`, `/connect-chrome`, `/qa`, `/qa-only`, `/design-review`, `/setup-browser-cookies`, `/setup-deploy`, `/setup-gbrain`, `/retro`, `/investigate`, `/document-release`, `/document-generate`, `/codex`, `/cso`, `/autoplan`, `/plan-devex-review`, `/devex-review`, `/careful`, `/freeze`, `/guard`, `/unfreeze`, `/gstack-upgrade`, `/learn`
