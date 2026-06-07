# Phase 0: 项目骨架搭建与数据库初始化

## 功能概述

搭建项目基础骨架，完成三个子项目的初始化配置和数据库建表。本阶段不涉及具体业务功能，只为后续开发提供基础设施。

**覆盖模块：** 项目脚手架（所有模块的基础）

---

## 1. 项目初始化

### 1.1 后端 (server/)

| 项目 | 内容 |
|------|------|
| 构建工具 | Maven (pom.xml) |
| JDK | 17 |
| Spring Boot | 3.2.x |
| 依赖 | spring-boot-starter-web, spring-boot-starter-security, spring-boot-starter-validation, mybatis-plus-spring-boot3, mysql-connector-j, jjwt (0.12.x), lombok, hutool-all |
| 配置文件 | application.yml, application-dev.yml, application-prod.yml |

### 1.2 管理后台 (admin/)

| 项目 | 内容 |
|------|------|
| 构建工具 | npm / pnpm |
| 框架 | Vue 3 + Vite |
| UI 库 | Element Plus |
| 状态管理 | Pinia |
| 路由 | Vue Router 4 |
| HTTP | Axios |
| 其他 | @element-plus/icons-vue, dayjs, echarts |

### 1.3 小程序 (mini-program/)

| 项目 | 内容 |
|------|------|
| 项目初始化 | 微信开发者工具新建项目 |
| app.json | 配置页面路径和窗口样式 |
| app.js | 全局生命周期和配置 |
| app.wxss | 全局样式 |

---

## 2. 数据库设计 (docs/database/)

### 2.1 建表脚本 (schema.sql)

#### 2.1.1 用户表 (users)

```sql
CREATE TABLE users (
    id          INT             AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)     NOT NULL UNIQUE     COMMENT '用户名',
    password    VARCHAR(255)    NOT NULL            COMMENT '密码(BCrypt加密)',
    role        VARCHAR(20)     NOT NULL DEFAULT 'STAFF' COMMENT '角色: ADMIN/STAFF',
    real_name   VARCHAR(50)                         COMMENT '真实姓名',
    phone       VARCHAR(20)                         COMMENT '手机号',
    status      TINYINT         NOT NULL DEFAULT 1  COMMENT '状态: 1启用 0禁用',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

#### 2.1.2 商品表 (products)

```sql
CREATE TABLE products (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200)    NOT NULL            COMMENT '商品名称',
    sku             VARCHAR(50)     NOT NULL UNIQUE     COMMENT '商品编码',
    brand           VARCHAR(100)                        COMMENT '品牌',
    car_model       VARCHAR(200)                        COMMENT '适配车型',
    category        VARCHAR(50)                         COMMENT '分类: 机油/滤芯/刹车片/轮胎/其他',
    unit            VARCHAR(20)     NOT NULL DEFAULT '个' COMMENT '单位',
    cost_price      DECIMAL(10,2)                       COMMENT '进货成本价',
    sale_price      DECIMAL(10,2)   NOT NULL            COMMENT '销售价',
    stock           INT             NOT NULL DEFAULT 0  COMMENT '当前库存',
    min_stock       INT             DEFAULT 0           COMMENT '最低库存预警',
    image_url       VARCHAR(500)                        COMMENT '图片URL',
    description     TEXT                                COMMENT '商品描述',
    status          TINYINT         NOT NULL DEFAULT 1  COMMENT '状态: 1上架 0下架',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';
```

#### 2.1.3 入库记录表 (stock_in)

```sql
CREATE TABLE stock_in (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    product_id      INT             NOT NULL            COMMENT '商品ID',
    quantity        INT             NOT NULL            COMMENT '入库数量',
    cost_price      DECIMAL(10,2)                       COMMENT '本次入库成本价',
    remark          VARCHAR(500)                        COMMENT '备注',
    operator_id     INT             NOT NULL            COMMENT '操作员ID(users.id)',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库记录表';
```

#### 2.1.4 销售订单表 (orders)

```sql
CREATE TABLE orders (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    order_no        VARCHAR(50)     NOT NULL UNIQUE     COMMENT '订单编号',
    customer_name   VARCHAR(100)                        COMMENT '客户名称',
    customer_phone  VARCHAR(20)                         COMMENT '客户电话',
    total_amount    DECIMAL(10,2)   NOT NULL            COMMENT '订单总金额',
    payment_method  VARCHAR(20)     DEFAULT 'CASH'      COMMENT '支付方式: CASH/WECHAT/ALIPAY/CARD',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PAID' COMMENT '状态: PAID/REFUNDED',
    remark          VARCHAR(500)                        COMMENT '备注',
    operator_id     INT             NOT NULL            COMMENT '操作员ID',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单表';
```

#### 2.1.5 销售明细表 (order_items)

```sql
CREATE TABLE order_items (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    order_id        INT             NOT NULL            COMMENT '订单ID',
    product_id      INT             NOT NULL            COMMENT '商品ID',
    product_name    VARCHAR(200)    NOT NULL            COMMENT '商品名称(快照)',
    quantity        INT             NOT NULL            COMMENT '数量',
    unit_price      DECIMAL(10,2)   NOT NULL            COMMENT '单价',
    subtotal        DECIMAL(10,2)   NOT NULL            COMMENT '小计',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售明细表';
```

#### 2.1.6 库存流水表 (inventory_logs)

```sql
CREATE TABLE inventory_logs (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    product_id      INT             NOT NULL            COMMENT '商品ID',
    type            VARCHAR(20)     NOT NULL            COMMENT '类型: STOCK_IN(入库)/SALE(销售)/ADJUST(调整)',
    quantity        INT             NOT NULL            COMMENT '变动数量(正数增加/负数减少)',
    before_stock    INT             NOT NULL            COMMENT '变动前库存',
    after_stock     INT             NOT NULL            COMMENT '变动后库存',
    ref_id          INT                                 COMMENT '关联ID(入库单ID/订单ID)',
    remark          VARCHAR(500)                        COMMENT '备注',
    operator_id     INT             NOT NULL            COMMENT '操作员ID',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';
```

### 2.2 初始化数据 (init-data.sql)

```sql
-- 默认管理员账号 (admin/admin123)
INSERT INTO users (username, password, role, real_name) VALUES
('admin', '$2a$10$...BCrypt加密密码...', 'ADMIN', '系统管理员');

-- 初始商品分类参考数据（可选预置）
```

---

## 3. 后端基础框架

### 3.1 启动类

```java
@SpringBootApplication
@MapperScan("com.autoparts.repository")
public class AutoPartsApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoPartsApplication.class, args);
    }
}
```

### 3.2 统一响应体

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
```

### 3.3 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error(500, "服务器内部错误");
    }
}
```

### 3.4 application.yml 配置

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auto_parts_store?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto

jwt:
  secret: your-secret-key-at-least-256-bits-long-for-hs256
  expiration: 86400000  # 24小时
```

---

## 4. 后端公共代码

### 4.1 common 包

| 文件 | 说明 |
|------|------|
| `Result.java` | 统一响应体 |
| `ResultCode.java` | 状态码枚举 (SUCCESS=200, BAD_REQUEST=400, UNAUTHORIZED=401, FORBIDDEN=403, NOT_FOUND=404, ERROR=500) |
| `BusinessException.java` | 业务异常 (含 code + message) |
| `GlobalExceptionHandler.java` | 全局异常处理器 |
| `JwtUtil.java` | JWT 工具 (generateToken, validateToken, getUserId) |
| `Constants.java` | 系统常量 |

### 4.2 config 包

| 文件 | 说明 |
|------|------|
| `SecurityConfig.java` | Spring Security 配置 (禁用 CSRF, 无状态会话, JWT 过滤器) |
| `CorsConfig.java` | 跨域配置 (允许 admin 域名和小程序请求) |
| `MyBatisPlusConfig.java` | 分页插件 |
| `JacksonConfig.java` | 日期序列化配置 (LocalDateTime → yyyy-MM-dd HH:mm:ss) |

---

## 5. 管理后台基础框架

### 5.1 路由配置 (src/router/index.js)

```javascript
const routes = [
  { path: '/login', component: () => import('@/views/Login.vue') },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/views/Dashboard.vue') },
    ]
  }
]
```

### 5.2 Axios 封装 (src/api/request.js)

- 基础 URL 配置
- 请求拦截器 (自动携带 Token)
- 响应拦截器 (统一错误处理, 401 跳转登录)

### 5.3 登录页占位

- 用户名/密码输入框
- 登录按钮
- 路由守卫 (未登录跳转 /login)

---

## 6. 验收标准

| # | 检查项 | 预期结果 |
|---|--------|----------|
| 1 | Maven 构建 | `mvn clean compile` 成功 |
| 2 | 数据库建表 | 执行 schema.sql，6 张表创建成功 |
| 3 | 启动后端 | Spring Boot 启动成功，无报错 |
| 4 | 健康检查 | GET /api/v1/health 返回 `{"code":200,"message":"success"}` |
| 5 | 管理后台启动 | `npm run dev` 启动成功，Vite 无报错 |
| 6 | 登录页访问 | 浏览器打开管理后台显示登录页面 |
| 7 | 小程序初始化 | 微信开发者工具打开显示首页 |

---

## 7. 依赖项

- 无（本阶段是其他所有阶段的基础）

---

## 8. 交付物清单

| 文件 | 说明 |
|------|------|
| `server/pom.xml` | Maven 依赖配置 |
| `server/src/main/resources/application.yml` | 主配置文件 |
| `server/src/main/java/com/autoparts/AutoPartsApplication.java` | 启动类 |
| `server/src/main/java/com/autoparts/common/*.java` | 公共工具类 |
| `server/src/main/java/com/autoparts/config/*.java` | 配置类 |
| `docs/database/schema.sql` | 建表脚本 |
| `docs/database/init-data.sql` | 初始化数据脚本 |
| `admin/package.json` | 前端项目配置 |
| `admin/src/api/request.js` | Axios 封装 |
| `admin/src/router/index.js` | 路由配置 |
| `admin/src/views/Login.vue` | 登录页（占位） |
| `mini-program/app.json` | 小程序配置 |
