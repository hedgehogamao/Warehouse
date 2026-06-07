# 开发计划 — 汽车配件门店管理系统

> 原则：**小步推进、逐步验证、每步可回滚**

---

## 总体策略

1. **不跨 Phase**：完成当前 Phase 全部验收项后，才进入下一 Phase
2. **每步只做一件事**：一个 Step 对应一个明确的交付物
3. **先跑通再完善**：先让骨架能编译运行，再逐步填充业务逻辑
4. **后端先行**：每个 Phase 先完成后端 API，再做前端页面
5. **写完即测**：每个 API 完成后立即用 curl/Postman 验证

---

## Phase 0：项目骨架与数据库（预计 1 天）

> 目标：三个子项目都能编译运行，数据库建表完成，健康检查接口可用

| Step | 任务 | 交付物 | 验证方式 |
|------|------|--------|----------|
| 0.1 | 初始化 Spring Boot 项目 | `server/pom.xml` + 启动类 + `application.yml` | `mvn clean compile` 成功 |
| 0.2 | 后端公共模块 | `Result.java`、`BusinessException.java`、`GlobalExceptionHandler.java` | 编译通过 |
| 0.3 | 后端配置类 | `SecurityConfig`、`CorsConfig`、`MyBatisPlusConfig`、`JacksonConfig` | 启动无报错 |
| 0.4 | 健康检查接口 | `HealthController` → `GET /api/v1/health` | curl 返回 200 |
| 0.5 | 数据库初始化 | 执行 `schema.sql` + `init-data.sql` | 8 张表创建成功，admin 账号存在 |
| 0.6 | 初始化 Vue3 项目 | `admin/package.json` + `vite.config.js` + 路由 + Axios 封装 | `npm run dev` 启动成功 |
| 0.7 | 管理后台登录页占位 | `Login.vue` + 路由守卫 | 浏览器显示登录页 |
| 0.8 | 初始化小程序骨架 | `app.js`、`app.json`、`app.wxss` | 微信开发者工具打开无报错 |

**Phase 0 验收**：后端启动 → curl 健康检查 → 前端登录页 → 小程序首页，全链路可访问。

---

## Phase 1：用户认证与权限（预计 1 天）

> 目标：后台登录可用，JWT 鉴权生效，用户管理接口可用

| Step | 任务 | 交付物 | 验证方式 |
|------|------|--------|----------|
| 1.1 | User 实体 + UserMapper | `User.java`、`UserMapper.java` | 编译通过 |
| 1.2 | JWT 工具类 | `JwtUtil.java` | 单元测试生成/解析 Token |
| 1.3 | 登录接口 | `AuthController.login()` | POST 登录返回 Token |
| 1.4 | JWT 过滤器 + 安全配置 | `JwtAuthFilter`、更新 `SecurityConfig` | 无 Token 访问返回 401 |
| 1.5 | 获取当前用户 + 修改密码 | `AuthController.me()` / `password()` | curl 验证 |
| 1.6 | 用户管理 CRUD | `UserController`、`UserService` | 分页查询、新增、编辑 |
| 1.7 | 管理后台登录页完善 | `Login.vue` 对接 API | 页面登录成功跳转首页 |
| 1.8 | 用户管理页面 | `UserManagement.vue` | 列表、新增、编辑可用 |

**Phase 1 验收**：登录 → Token 鉴权 → 用户管理 CRUD → 权限拦截，全部通过。

---

## Phase 2：商品管理（预计 1 天）

> 目标：商品增删改查、搜索筛选、上下架，前后端联调通过

| Step | 任务 | 交付物 | 验证方式 |
|------|------|--------|----------|
| 2.1 | Product 实体 + Mapper | `Product.java`、`ProductMapper.java` | 编译通过 |
| 2.2 | 商品列表接口（分页+搜索） | `ProductController.list()` | curl 分页查询 |
| 2.3 | 商品新增/编辑/删除接口 | `ProductController` CRUD | curl 增删改查 |
| 2.4 | 分类列表 + 上下架接口 | `categories()`、`status()` | curl 验证 |
| 2.5 | 商品列表页面 | `ProductList.vue` | 表格、搜索、分页 |
| 2.6 | 商品新增/编辑弹窗 | `ProductForm.vue`（Dialog） | 表单校验、提交 |
| 2.7 | 联调验证 | 前后端联调 | 全流程可操作 |

**Phase 2 验收**：新增商品 → 列表搜索 → 编辑 → 上下架 → 删除，前端操作流畅。

---

## Phase 3：入库与库存管理（预计 1 天）

> 目标：入库登记自动更新库存，库存查询与预警，库存调整

| Step | 任务 | 交付物 | 验证方式 |
|------|------|--------|----------|
| 3.1 | StockIn + InventoryLog 实体 | 实体类 + Mapper | 编译通过 |
| 3.2 | 入库接口（事务） | `StockInController.create()` | 入库后库存增加、流水生成 |
| 3.3 | 入库记录列表 | `StockInController.list()` | 按商品/时间筛选 |
| 3.4 | 库存查询 + 预警接口 | `InventoryController` | 库存列表、low-stock |
| 3.5 | 库存调整接口 | `InventoryController.adjust()` | 调整后库存正确 |
| 3.6 | 库存流水查询 | `InventoryController.logs()` | 按商品查流水 |
| 3.7 | 入库管理页面 | `StockIn.vue` | 入库表单 + 记录列表 |
| 3.8 | 库存管理页面 | `Inventory.vue` + 弹窗组件 | 库存列表、流水、调整 |

**Phase 3 验收**：入库 → 库存增加 → 流水记录 → 预警显示 → 库存调整，事务一致。

---

## Phase 4：销售收银与订单（预计 1~2 天）

> 目标：POS 收银、订单生成、库存扣减、退款

| Step | 任务 | 交付物 | 验证方式 |
|------|------|--------|----------|
| 4.1 | Order + OrderItem 实体 | 实体类 + Mapper | 编译通过 |
| 4.2 | POS 商品快速搜索接口 | `ProductController.posList()` | 搜索返回精简数据 |
| 4.3 | 创建订单接口（事务） | `OrderController.create()` | 下单扣库存、流水记录 |
| 4.4 | 订单列表 + 详情接口 | `OrderController` list/detail | 按条件查询 |
| 4.5 | 订单退款接口（事务） | `OrderController.refund()` | 退款恢复库存 |
| 4.6 | POS 收银页面 | `POS.vue` | 搜索→加购→收款 |
| 4.7 | 订单管理页面 | `OrderList.vue` + 详情弹窗 | 列表、详情、退款 |
| 4.8 | 联调验证 | 前后端联调 | 完整收银流程 |

**Phase 4 验收**：搜索商品 → 加入购物车 → 确认收款 → 库存扣减 → 退款恢复。

---

## Phase 5：历史记录与统计（预计 1 天）

> 目标：操作日志自动记录、库存流水追溯、数据统计看板

| Step | 任务 | 交付物 | 验证方式 |
|------|------|--------|----------|
| 5.1 | OperationLog 实体 + Mapper | 实体类 | 编译通过 |
| 5.2 | 操作日志 AOP 切面 | `OperationLogAspect` + 注解 | 关键操作自动记录 |
| 5.3 | 库存流水 + 操作日志查询接口 | `HistoryController` | 多条件查询 |
| 5.4 | 统计接口（今日概况/趋势/排行/分类） | `StatisticsController` | 返回正确数据 |
| 5.5 | 首页看板页面 | `Dashboard.vue` + ECharts | 数据卡片 + 图表 |
| 5.6 | 历史记录页面 | `History.vue`（Tab 切换） | 流水 + 日志 |
| 5.7 | 数据统计页面 | `Statistics.vue` | 趋势图 + 排行 + 饼图 |

**Phase 5 验收**：Dashboard 数据正确 → 图表展示 → 历史查询可追溯。

---

## Phase 6：客户微信小程序（预计 1~2 天）

> 目标：微信登录、商品浏览、库存查询、购买记录

| Step | 任务 | 交付物 | 验证方式 |
|------|------|--------|----------|
| 6.1 | Customer 实体 + Mapper | 实体类 | 编译通过 |
| 6.2 | 小程序登录接口 | `MiniAppAuthController` | 登录返回 Token |
| 6.3 | 小程序商品接口 | `MiniAppProductController` | 浏览、搜索、详情 |
| 6.4 | 小程序订单查询接口 | `MiniAppOrderController` | 仅返回当前客户订单 |
| 6.5 | 小程序请求封装 + 首页 | `utils/api.js` + `pages/index/` | 首页加载正常 |
| 6.6 | 商品列表 + 详情页 | `pages/products/` | 搜索、筛选、详情 |
| 6.7 | 购买记录 + 个人中心 | `pages/orders/` + `pages/mine/` | 记录查看正常 |

**Phase 6 验收**：微信登录 → 浏览商品 → 查看库存 → 查看购买记录。

---

## Phase 7：测试与部署（预计 1~2 天）

> 目标：测试通过、生产环境部署、小程序审核

| Step | 任务 | 交付物 | 验证方式 |
|------|------|--------|----------|
| 7.1 | 后端单元测试 | `src/test/` 全部测试类 | `mvn test` 全部通过 |
| 7.2 | 接口测试集合 | Postman/Apifox 导出 | 全部接口可调通 |
| 7.3 | 功能回归测试 | 按各 Phase 验收清单 | 全部通过 |
| 7.4 | 生产环境配置 | `application-prod.yml`、Nginx 配置 | 配置无误 |
| 7.5 | 部署脚本 | `deploy.sh`、Systemd 服务文件 | 脚本可执行 |
| 7.6 | 部署上线 | 云服务器部署 | HTTPS 可访问 |
| 7.7 | 小程序审核 | 提交微信审核 | 体验版通过 |

**Phase 7 验收**：单元测试通过 → 部署成功 → HTTPS 可访问 → 小程序可用。
