# Phase 7: 测试、部署与上线

## 功能概述

完成系统的全面测试、生产环境部署和上线配置，确保系统稳定可靠地运行。

**覆盖模块：** 全部模块

---

## 1. 测试

### 1.1 单元测试

**后端单元测试 (server/src/test/)**

| 测试项 | 测试内容 | 工具 |
|--------|----------|------|
| Service 层 | 业务逻辑正确性 | JUnit 5 + Mockito |
| Repository 层 | SQL 和数据访问 | @MybatisPlusTest |
| Controller 层 | API 请求响应 | MockMvc |

**关键测试用例：**

```
UserServiceTest
  ├── testLoginSuccess()          - 正确用户名密码登录成功
  ├── testLogin_WrongPassword()   - 错误密码返回异常
  └── testLogin_DisabledUser()    - 禁用用户登录失败

ProductServiceTest
  ├── testCreateProduct()         - 新增商品
  ├── testSearchByKeyword()       - 关键字搜索
  └── testSkuUnique()             - SKU 唯一性校验

StockInServiceTest
  ├── testStockIn()               - 入库后库存增加
  └── testStockIn_Transaction()   - 事务回滚

OrderServiceTest
  ├── testCreateOrder()           - 下单成功扣库存
  ├── testCreateOrder_NoStock()   - 库存不足下单失败
  └── testRefundOrder()           - 退款后恢复库存

StatisticsServiceTest
  ├── testTodaySummary()           - 今日概况统计
  └── testSalesTrend()             - 销售趋势统计
```

**测试配置：**
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
```

### 1.2 集成测试

| 测试项 | 说明 |
|--------|------|
| API 集成测试 | 完整的请求→响应链路测试 |
| 数据库集成 | 事务一致性、并发场景测试 |
| 小程序接口 | Postman/Apifox 接口集合 |

### 1.3 功能回归测试

参见各 Phase 验收标准中的测试用例，逐个验证。

### 1.4 性能测试

| 项目 | 要求 |
|------|------|
| 并发用户 | 50 人 |
| 平均响应时间 | < 2 秒 |
| 吞吐量 | > 100 TPS |
| 测试工具 | JMeter / wrk |

**测试场景：**
- 商品列表查询（高频率接口）
- 下单操作（事务密集型）
- 数据统计查询（聚合查询）

---

## 2. 生产环境部署

### 2.1 服务器配置

| 配置项 | 规格 |
|--------|------|
| CPU | 2 核 (最低) |
| 内存 | 2 GB (最低) |
| 硬盘 | 40 GB SSD |
| 带宽 | 2 Mbps (最低) |
| 操作系统 | Ubuntu 22.04 / CentOS 7+ |

### 2.2 部署架构

```
┌─────────┐     ┌──────────┐     ┌───────────┐
│  用户    │────▶│  Nginx   │────▶│ Spring Boot│
│ (HTTPS) │     │ (反向代理) │     │  (端口 8080)│
└─────────┘     └──────────┘     └─────┬─────┘
                                       │
                                       ▼
                                  ┌──────────┐
                                  │  MySQL 8.0 │
                                  └──────────┘

前端静态文件由 Nginx 直接托管
```

### 2.3 部署步骤

#### 2.3.1 环境准备

```bash
# 1. 安装 JDK 17
sudo apt update
sudo apt install openjdk-17-jdk -y

# 2. 安装 MySQL 8.0
sudo apt install mysql-server-8.0 -y

# 3. 安装 Nginx
sudo apt install nginx -y

# 4. 安装 Node.js (用于构建前端)
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs -y
```

#### 2.3.2 配置 MySQL

```sql
CREATE DATABASE auto_parts_store DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'autoparts'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON auto_parts_store.* TO 'autoparts'@'localhost';
FLUSH PRIVILEGES;

# 导入建表脚本
mysql -u autoparts -p auto_parts_store < /path/to/schema.sql
mysql -u autoparts -p auto_parts_store < /path/to/init-data.sql
```

#### 2.3.3 部署后端

```bash
# 1. 构建
cd server
mvn clean package -DskipTests

# 2. 配置生产环境
# 修改 application-prod.yml 中的数据库连接、JWT 密钥等

# 3. 启动
nohup java -jar target/auto-parts-server.jar \
  --spring.profiles.active=prod \
  > app.log 2>&1 &

# 或使用 systemd 管理
sudo tee /etc/systemd/system/autoparts.service << 'EOF'
[Unit]
Description=Auto Parts Store Server
After=network.target mysql.service

[Service]
User=ubuntu
ExecStart=/usr/bin/java -jar /opt/autoparts/auto-parts-server.jar --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable autoparts
sudo systemctl start autoparts
```

#### 2.3.4 部署前端

```bash
# 1. 构建
cd admin
npm install
npm run build

# 2. 复制到 Nginx 目录
sudo cp -r dist/* /var/www/autoparts-admin/
```

#### 2.3.5 配置 Nginx

```nginx
# /etc/nginx/sites-available/autoparts

# HTTPS 配置
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;

    # 管理后台前端
    location / {
        root /var/www/autoparts-admin;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# HTTP → HTTPS 重定向
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

#### 2.3.6 配置 HTTPS

```bash
# 使用 Certbot 申请 Let's Encrypt 证书
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d your-domain.com
```

#### 2.3.7 小程序域名配置

在微信小程序管理后台配置：

| 配置项 | 值 |
|--------|-----|
| request 合法域名 | `https://your-domain.com` |
| socket 合法域名 | （可选） |
| uploadFile 合法域名 | `https://your-domain.com` |

### 2.4 生产环境配置

```yaml
# application-prod.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auto_parts_store?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: autoparts
    password: your_secure_password_here
    driver-class-name: com.mysql.cj.jdbc.Driver
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl  # 关闭 SQL 日志

jwt:
  secret: change-this-to-a-long-secure-random-string
  expiration: 86400000  # 24小时

logging:
  file:
    name: /var/log/autoparts/app.log
  level:
    com.autoparts: INFO
```

---

## 3. 生产安全检查清单

| # | 检查项 | 操作 |
|---|--------|------|
| 1 | 数据库密码 | 使用强密码，不硬编码在配置文件中 |
| 2 | JWT 密钥 | 使用 256 位以上随机字符串 |
| 3 | HTTPS | 全站强制 HTTPS |
| 4 | CORS | 仅允许管理后台域名和小程序 |
| 5 | SQL 注入 | 使用参数化查询（MyBatis-Plus 已防护） |
| 6 | XSS | 前端输出编码 |
| 7 | 敏感信息 | 密码 BCrypt 加密，Token 不存敏感信息 |
| 8 | 备份策略 | 每日自动备份数据库 |
| 9 | 日志轮转 | 配置 logrotate 防止日志撑满磁盘 |

---

## 4. 部署验证

### 4.1 健康检查

```bash
# 检查后端
curl https://your-domain.com/api/v1/health

# 预期返回:
# {"code":200,"message":"success","data":{"status":"UP","time":"2026-06-06 12:00:00"}}
```

### 4.2 功能验证清单

| # | 验证项 | 状态 |
|---|--------|------|
| 1 | 管理后台可访问 | [ ] |
| 2 | 登录功能正常 | [ ] |
| 3 | 商品 CRUD 正常 | [ ] |
| 4 | 入库操作正常 | [ ] |
| 5 | POS 收银正常 | [ ] |
| 6 | 订单查询正常 | [ ] |
| 7 | 库存预警正常 | [ ] |
| 8 | 小程序首页加载正常 | [ ] |
| 9 | 小程序登录正常 | [ ] |
| 10 | 小程序商品查询正常 | [ ] |
| 11 | 小程序购买记录正常 | [ ] |
| 12 | HTTPS 正常 | [ ] |
| 13 | Nginx 反向代理正常 | [ ] |
| 14 | 数据库备份正常 | [ ] |

---

## 5. 验收标准

| # | 检查项 | 预期结果 |
|---|--------|----------|
| 1 | 单元测试 | 全部通过，覆盖率 ≥ 80% |
| 2 | 集成测试 | API 测试全部通过 |
| 3 | 性能测试 | 50 并发下平均响应 < 2s |
| 4 | 部署 | 一键部署脚本可执行 |
| 5 | HTTPS | 浏览器显示安全锁 |
| 6 | 小程序 | 体验版审核通过 |
| 7 | 备份 | 自动备份可恢复 |
| 8 | 监控 | 日志正常输出，无异常报错 |

---

## 6. 交付物清单

| 文件 | 说明 |
|------|------|
| `docs/deploy/nginx.conf` | Nginx 配置文件 |
| `docs/deploy/autoparts.service` | Systemd 服务文件 |
| `docs/deploy/deploy.sh` | 一键部署脚本 |
| `server/src/test/` | 完整单元测试 |
| `server/postman/collection.json` | Postman/Apifox 接口测试集合 |
| `docs/API.md` | API 接口文档 |
| `README.md` | 项目说明文档 |
