# AGENTS.md — 汽车配件门店库存管理与客户查询系统

> 本文档是项目的**唯一入口**。所有开发任务由 Agent 按阶段调度 Skill 完成。

---

## 1 项目概览

| 维度 | 选型 |
|------|------|
| 后端 | Spring Boot 3.2.x + JDK 17 + MyBatis-Plus |
| 管理后台 | Vue 3 + Element Plus + Vite + Pinia |
| 客户端 | 微信小程序（原生） |
| 数据库 | MySQL 8.0 |
| 反向代理 | Nginx |
| 认证 | JWT（后台）/ 微信登录（小程序） |

### 1.1 业务范围

- **单店铺** · 1 名店员 · ≈ 200 名客户
- 核心流程：商品管理 → 入库 → 库存查询 → 销售收银 → 历史记录
- 客户通过微信小程序自助浏览商品、查询库存、查看购买记录

### 1.2 系统架构

```
┌──────────────┐  REST API  ┌──────────────────┐  JDBC  ┌─────────┐
│ Vue3 管理后台 │───────────▶│  Spring Boot     │───────▶│  MySQL  │
└──────────────┘            │  (REST API)      │        └─────────┘
                            └──────────────────┘
┌──────────────┐  REST API       ▲
│ 微信小程序   │─────────────────┘
└──────────────┘
```

---

## 2 Agent 定义

每个 Agent 对应一个开发角色，负责特定领域的代码生成与质量保障。

| Agent ID | 角色 | 职责 |
|----------|------|------|
| `architect` | 系统架构师 | 项目脚手架、目录结构、依赖配置、公共模块 |
| `db-engineer` | 数据库工程师 | DDL、DML、索引策略、迁移脚本 |
| `backend-dev` | 后端开发 | Controller / Service / Repository / DTO / VO / 单元测试 |
| `frontend-dev` | 前端开发 | Vue3 页面、组件、路由、状态管理、API 对接 |
| `mini-dev` | 小程序开发 | 微信小程序页面、登录、商品浏览、查询 |
| `security` | 安全工程师 | JWT 鉴权、权限校验、SQL 注入防护、XSS 防护 |
| `qa-engineer` | 测试工程师 | 单元测试、接口测试、功能测试用例 |
| `devops` | 运维工程师 | Nginx、Systemd、部署脚本、HTTPS 配置 |

---

## 3 Skill 定义

Skill 是可复用的开发能力单元，定义了编码规范、技术约束和最佳实践。Agent 在编码前必须阅读对应 Skill 文件。

| Skill ID | 文件 | 说明 |
|----------|------|------|
| `skill-sql` | [docs/skills/sql.md](docs/skills/sql.md) | 数据库设计规范：DDL、索引、命名、迁移 |
| `skill-springboot` | [docs/skills/springboot.md](docs/skills/springboot.md) | Spring Boot 后端开发规范：分层架构、DTO/VO、事务、异常处理 |
| `skill-vue3` | [docs/skills/vue3.md](docs/skills/vue3.md) | Vue3 + Element Plus 前端规范：组件、路由、状态管理、API 封装 |
| `skill-miniprogram` | [docs/skills/miniprogram.md](docs/skills/miniprogram.md) | 微信小程序开发规范：页面结构、请求封装、登录流程 |
| `skill-api` | [docs/skills/api.md](docs/skills/api.md) | RESTful API 设计规范：URL 命名、请求/响应格式、分页、错误码 |
| `skill-test` | [docs/skills/test.md](docs/skills/test.md) | 测试规范：单元测试、Mock 策略、测试用例编写 |

---

## 4 开发阶段（Phases）

系统按 **8 个阶段** 串行推进，每个阶段对应一个 Spec 文件。

```
Phase 0 ──▶ Phase 1 ──▶ Phase 2 ──▶ Phase 3 ──▶ Phase 4 ──▶ Phase 5 ──▶ Phase 6 ──▶ Phase 7
 脚手架       认证        商品       入库/库存     销售       统计/历史    小程序       测试/部署
```

| Phase | 名称 | Spec 文件 | 主要 Agent | 主要 Skill |
|-------|------|-----------|-----------|-----------|
| 0 | 项目骨架与数据库 | [phase-00-scaffold.md](docs/specs/phase-00-scaffold.md) | `architect` `db-engineer` | `skill-sql` `skill-springboot` |
| 1 | 用户认证与权限 | [phase-01-user-management.md](docs/specs/phase-01-user-management.md) | `backend-dev` `security` | `skill-springboot` `skill-api` |
| 2 | 商品管理 | [phase-02-product-management.md](docs/specs/phase-02-product-management.md) | `backend-dev` `frontend-dev` | `skill-springboot` `skill-vue3` `skill-api` |
| 3 | 入库与库存管理 | [phase-03-stock-in-inventory.md](docs/specs/phase-03-stock-in-inventory.md) | `backend-dev` `frontend-dev` | `skill-springboot` `skill-vue3` `skill-api` |
| 4 | 销售收银与订单 | [phase-04-sales-order.md](docs/specs/phase-04-sales-order.md) | `backend-dev` `frontend-dev` | `skill-springboot` `skill-vue3` `skill-api` |
| 5 | 历史记录与统计 | [phase-05-history-statistics.md](docs/specs/phase-05-history-statistics.md) | `backend-dev` `frontend-dev` | `skill-springboot` `skill-vue3` `skill-api` |
| 6 | 客户微信小程序 | [phase-06-mini-program.md](docs/specs/phase-06-mini-program.md) | `mini-dev` `backend-dev` | `skill-miniprogram` `skill-api` |
| 7 | 测试与部署 | [phase-07-deployment.md](docs/specs/phase-07-deployment.md) | `qa-engineer` `devops` | `skill-test` |

---

## 5 子目录结构

| 目录 | 说明 | AGENTS.md |
|------|------|-----------|
| `server/` | Spring Boot 后端服务 | [server/AGENTS.md](server/AGENTS.md) |
| `admin/` | Vue3 + Element Plus 管理后台 | [admin/AGENTS.md](admin/AGENTS.md) |
| `mini-program/` | 微信小程序客户端 | [mini-program/AGENTS.md](mini-program/AGENTS.md) |
| `docs/` | 项目文档（Spec、Skill、数据库设计） | [docs/AGENTS.md](docs/AGENTS.md) |
| `docs/specs/` | 各阶段功能规格说明 | [docs/specs/AGENTS.md](docs/specs/AGENTS.md) |
| `docs/skills/` | Skill 定义文件 | — |
| `docs/database/` | 数据库设计文档与 SQL 脚本 | [docs/database/AGENTS.md](docs/database/AGENTS.md) |

### 5.1 预期代码目录结构

```
auto-parts-store/
├── AGENTS.md                        # 本文件
├── docs/
│   ├── specs/                       # 阶段功能规格（phase-00 ~ phase-07）
│   ├── skills/                      # Skill 定义
│   │   ├── sql.md
│   │   ├── springboot.md
│   │   ├── vue3.md
│   │   ├── miniprogram.md
│   │   ├── api.md
│   │   └── test.md
│   └── database/
│       ├── schema.sql
│       └── init-data.sql
├── server/                          # Spring Boot 后端
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/autoparts/
│       │   ├── controller/          # 控制器
│       │   ├── service/             # 业务逻辑
│       │   ├── repository/          # MyBatis-Plus Mapper
│       │   ├── model/               # 实体类
│       │   ├── dto/                 # 请求/响应 DTO
│       │   ├── common/              # 公共工具（Result, JwtUtil, 异常）
│       │   ├── config/              # 配置类
│       │   └── aop/                 # AOP 切面（操作日志）
│       └── main/resources/
│           ├── application.yml
│           ├── application-dev.yml
│           ├── application-prod.yml
│           └── mapper/*.xml
├── admin/                           # Vue3 管理后台
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/                     # API 请求封装
│       ├── views/                   # 页面组件
│       ├── components/              # 公共组件
│       ├── router/                  # 路由
│       ├── store/                   # Pinia 状态
│       └── utils/                   # 工具函数
├── miniprogram/                     # 微信小程序
│   ├── app.js / app.json / app.wxss
│   ├── pages/
│   └── utils/
└── sql/                             # 数据库脚本（符号链接或副本）
```

---

## 6 通用规范

### 6.1 API 设计

- RESTful 风格：`/api/v1/{resource}`
- 统一响应格式：`{ "code": 200, "message": "success", "data": {...} }`
- 分页参数：`page`（页码，从 1 开始）、`size`（每页条数，默认 20）
- 认证：`Authorization: Bearer {token}`

### 6.2 代码风格

- 后端：遵循 Alibaba Java 编码规范
- 前端：遵循 Vue3 Composition API 风格 + ESLint
- 中文注释覆盖率 > 80%
- 关键业务逻辑必须有单元测试

### 6.3 安全要求

- 密码使用 BCrypt 加密存储
- JWT Token 有效期 24 小时
- 所有管理接口需登录鉴权
- SQL 参数化查询（MyBatis-Plus 自动防护）
- CORS 仅允许指定域名

---

## 7 Agent 调度规则

1. **串行推进**：Phase N 验收通过后，才可进入 Phase N+1。
2. **Spec 驱动**：每个 Phase 开始前，Agent 必须先读取对应 Spec 文件。
3. **Skill 绑定**：Agent 在编码时必须遵循对应 Skill 文件中定义的规范。
4. **验收清单**：每个 Phase 结束时，对照 Spec 中的「验收标准」逐项检查。
5. **测试前置**：从 Phase 1 起，每个 Phase 必须包含对应测试用例。
6. **依赖检查**：Phase N 的 Spec 中标注的「依赖项」必须已完成。

---

## 8 启动指令

开始开发时，从 Phase 0 开始，依次执行：

```
Phase 0: 阅读 docs/specs/phase-00-scaffold.md → Agent: architect + db-engineer
Phase 1: 阅读 docs/specs/phase-01-user-management.md → Agent: backend-dev + security
Phase 2: 阅读 docs/specs/phase-02-product-management.md → Agent: backend-dev + frontend-dev
Phase 3: 阅读 docs/specs/phase-03-stock-in-inventory.md → Agent: backend-dev + frontend-dev
Phase 4: 阅读 docs/specs/phase-04-sales-order.md → Agent: backend-dev + frontend-dev
Phase 5: 阅读 docs/specs/phase-05-history-statistics.md → Agent: backend-dev + frontend-dev
Phase 6: 阅读 docs/specs/phase-06-mini-program.md → Agent: mini-dev + backend-dev
Phase 7: 阅读 docs/specs/phase-07-deployment.md → Agent: qa-engineer + devops
```
