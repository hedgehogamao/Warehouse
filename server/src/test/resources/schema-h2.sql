-- H2 兼容的建表脚本（测试用）

-- 1. 用户表
CREATE TABLE IF NOT EXISTS users (
    id          INT             AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)     NOT NULL UNIQUE,
    password    VARCHAR(255)    NOT NULL,
    role        VARCHAR(20)     NOT NULL DEFAULT 'STAFF',
    real_name   VARCHAR(50),
    phone       VARCHAR(20),
    status      TINYINT         NOT NULL DEFAULT 1,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. 商品表
CREATE TABLE IF NOT EXISTS products (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200)    NOT NULL,
    sku             VARCHAR(50)     NOT NULL UNIQUE,
    brand           VARCHAR(100),
    car_model       VARCHAR(200),
    category        VARCHAR(50),
    unit            VARCHAR(20)     NOT NULL DEFAULT '个',
    cost_price      DECIMAL(10,2),
    sale_price      DECIMAL(10,2)   NOT NULL,
    stock           INT             NOT NULL DEFAULT 0,
    min_stock       INT             DEFAULT 0,
    image_url       VARCHAR(500),
    description     TEXT,
    status          TINYINT         NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. 入库记录表
CREATE TABLE IF NOT EXISTS stock_in (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    product_id      INT             NOT NULL,
    quantity        INT             NOT NULL,
    cost_price      DECIMAL(10,2),
    remark          VARCHAR(500),
    operator_id     INT             NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. 销售订单表
CREATE TABLE IF NOT EXISTS orders (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    order_no        VARCHAR(50)     NOT NULL UNIQUE,
    customer_id     INT             DEFAULT NULL,
    customer_name   VARCHAR(100),
    customer_phone  VARCHAR(20),
    total_amount    DECIMAL(10,2)   NOT NULL,
    payment_method  VARCHAR(20)     DEFAULT 'CASH',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PAID',
    remark          VARCHAR(500),
    operator_id     INT             NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 5. 销售明细表
CREATE TABLE IF NOT EXISTS order_items (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    order_id        INT             NOT NULL,
    product_id      INT             NOT NULL,
    product_name    VARCHAR(200)    NOT NULL,
    quantity        INT             NOT NULL,
    unit_price      DECIMAL(10,2)   NOT NULL,
    subtotal        DECIMAL(10,2)   NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 6. 库存变动日志表
CREATE TABLE IF NOT EXISTS inventory_logs (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    product_id      INT             NOT NULL,
    type            VARCHAR(20)     NOT NULL,
    quantity        INT             NOT NULL,
    before_stock    INT             NOT NULL,
    after_stock     INT             NOT NULL,
    ref_id          INT,
    remark          VARCHAR(500),
    operator_id     INT             NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 7. 客户表（微信小程序用户）
CREATE TABLE IF NOT EXISTS customers (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    openid          VARCHAR(100)    NOT NULL UNIQUE,
    nickname        VARCHAR(100),
    avatar_url      VARCHAR(500),
    phone           VARCHAR(20),
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 8. 操作日志表
CREATE TABLE IF NOT EXISTS operation_logs (
    id              INT             AUTO_INCREMENT PRIMARY KEY,
    operator_id     INT             NOT NULL,
    operator_name   VARCHAR(50),
    action          VARCHAR(50)     NOT NULL,
    target_type     VARCHAR(50),
    target_id       INT,
    detail          TEXT,
    ip              VARCHAR(50),
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
);
