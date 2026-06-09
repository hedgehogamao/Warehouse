# 汽车配件门店库存管理与客户查询系统

> 专为小型汽车配件门店设计的库存管理与客户查询系统

## 📋 项目概述

面向单一门店、1 名店员、约 200 名客户的汽车配件库存管理系统。

### 技术栈

| 组件 | 技术选型 |
|------|---------|
| 后端框架 | Spring Boot 3.2.x |
| Java 版本 | JDK 17 |
| ORM 框架 | MyBatis-Plus 3.5.6 |
| 数据库 | MySQL 8.0 |
| 管理后台前端 | Vue 3 + Element Plus + Vite + Pinia |
| 客户端 | 微信小程序（原生） |
| 认证方式 | JWT |

### 核心功能

- ✅ **用户管理** - 登录认证、角色权限、密码管理
- ✅ **商品管理** - CRUD、搜索筛选、Excel 批量导入
- ✅ **入库管理** - 入库记录、库存变动日志
- ✅ **销售收银** - POS 收银、扫码录入、多种支付方式
- ✅ **订单管理** - 订单查询、退款处理
- ✅ **数据统计** - 今日概况、销售趋势、商品排行
- ✅ **客户小程序** - 商品查询、购买记录

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Node.js 18+（前端开发）

### 1. 克隆项目

```bash
git clone <repository-url>
cd auto-parts-store
```

### 2. 初始化数据库

```bash
mysql -u root -p < docs/database/schema.sql
mysql -u root -p < docs/database/init-data.sql
```

### 3. 启动后端

```bash
cd server
mvn spring-boot:run
```

后端将运行在 http://localhost:8080

### 4. 启动前端

```bash
cd admin
npm install
npm run dev
```

前端将运行在 http://localhost:5173

### 5. 登录系统

- 地址：http://localhost:5173
- 默认账号：`admin`
- 默认密码：`admin123`

---

## 📁 项目结构

```
auto-parts-store/
├── admin/                  # 管理后台前端
│   ├── src/
│   │   ├── api/           # API 请求封装
│   │   ├── views/         # 页面组件
│   │   ├── components/    # 公共组件
│   │   ├── layouts/       # 布局组件
│   │   ├── router/        # 路由配置
│   │   ├── store/         # 状态管理
│   │   └── utils/         # 工具函数
│   └── package.json
├── mini-program/           # 微信小程序
│   ├── pages/
│   ├── utils/
│   └── app.json
├── server/                 # 后端服务
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/autoparts/
│   │   │   │   ├── controller/   # REST 控制器
│   │   │   │   ├── service/      # 业务逻辑
│   │   │   │   ├── repository/   # 数据访问
│   │   │   │   ├── model/        # 实体类
│   │   │   │   ├── dto/          # 数据传输对象
│   │   │   │   ├── common/       # 公共组件
│   │   │   │   └── config/       # 配置类
│   │   │   └── resources/
│   │   └── test/                  # 单元测试
│   └── pom.xml
├── docs/                   # 文档
│   ├── database/          # 数据库脚本
│   ├── specs/             # 需求规格
│   ├── skills/            # 开发规范
│   └── deploy/            # 部署配置
└── devlog/                # 开发日志
```

---

## 🔧 开发命令

### 后端

```bash
cd server

# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 测试
mvn test

# 打包
mvn clean package -DskipTests
```

### 前端

```bash
cd admin

# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build
```

### 数据库

```bash
# 建表
mysql -u root -p auto_parts_store < docs/database/schema.sql

# 初始化数据
mysql -u root -p auto_parts_store < docs/database/init-data.sql
```

---

## 📊 API 接口

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/auth/login | 用户登录 |

### 商品管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/products | 商品列表 |
| POST | /api/v1/products | 新增商品 |
| PUT | /api/v1/products/{id} | 修改商品 |
| DELETE | /api/v1/products/{id} | 删除商品 |
| POST | /api/v1/products/import | Excel 导入商品 |
| GET | /api/v1/products/import-template | 下载导入模板 |

### 入库管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/stock-in | 入库记录列表 |
| POST | /api/v1/stock-in | 新建入库 |

### 订单管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/orders | 订单列表 |
| GET | /api/v1/orders/{id} | 订单详情 |
| POST | /api/v1/orders | 创建订单 |
| POST | /api/v1/orders/{id}/refund | 订单退款 |

### 数据统计

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/statistics/today | 今日概况 |
| GET | /api/v1/statistics/trend | 销售趋势 |
| GET | /api/v1/statistics/top-products | 商品排行 |

---

## 🧪 测试

### 运行单元测试

```bash
cd server
mvn test -Dspring.profiles.active=test
```

### 测试覆盖率

- UserServiceTest: 11 个测试用例
- ProductServiceTest: 12 个测试用例
- StockInServiceTest: 5 个测试用例
- OrderServiceTest: 5 个测试用例
- StatisticsServiceTest: 5 个测试用例

**总计：38 个测试用例，全部通过**

---

## 🚢 部署

### 一键部署（Ubuntu）

```bash
sudo bash docs/deploy/deploy.sh
```

### 手动部署

参考 [部署文档](docs/deploy/README.md)

### Docker 部署（待完善）

```bash
docker-compose up -d
```

---

## 🔒 安全特性

- ✅ JWT Token 认证（有效期 3 天）
- ✅ BCrypt 密码加密
- ✅ CORS 跨域配置
- ✅ SQL 注入防护（MyBatis-Plus 参数化查询）
- ✅ 接口权限控制
- ✅ 操作日志记录

---

## 📝 更新日志

### v1.0.0 (2026-06-09)

- ✨ 完成全部 Phase 0-7 开发
- ✅ 38 个单元测试全部通过
- ✅ 生产部署配置完成
- ✅ Excel 批量导入功能
- ✅ POS 扫码收银功能

---

## 📄 许可证

本项目仅供学习和内部使用。

---

## 👥 贡献者

- 开发团队

---

## 📞 联系方式

如有问题，请联系开发团队。
