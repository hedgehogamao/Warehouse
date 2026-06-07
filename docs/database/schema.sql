-- =============================================
-- 汽车配件门店库存管理与客户查询系统
-- 数据库建表脚本
-- 版本: 1.0
-- =============================================

CREATE DATABASE IF NOT EXISTS auto_parts_store
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE auto_parts_store;

-- =============================================
-- 1. 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    id          INT             AUTO_INCREMENT PRIMARY KEY     COMMENT '主键ID',
    username    VARCHAR(50)     NOT NULL UNIQUE                COMMENT '用户名',
    password    VARCHAR(255)    NOT NULL                       COMMENT '密码(BCrypt加密)',
    role        VARCHAR(20)     NOT NULL DEFAULT 'STAFF'       COMMENT '角色: ADMIN/STAFF',
    real_name   VARCHAR(50)                                    COMMENT '真实姓名',
    phone       VARCHAR(20)                                    COMMENT '手机号',
    status      TINYINT         NOT NULL DEFAULT 1             COMMENT '状态: 1启用 0禁用',
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 2. 商品表
-- =============================================
CREATE TABLE IF NOT EXISTS products (
    id              INT             AUTO_INCREMENT PRIMARY KEY     COMMENT '主键ID',
    name            VARCHAR(200)    NOT NULL                       COMMENT '商品名称',
    sku             VARCHAR(50)     NOT NULL UNIQUE                COMMENT '商品编码',
    brand           VARCHAR(100)                                   COMMENT '品牌',
    car_model       VARCHAR(200)                                   COMMENT '适配车型(多个用/分隔)',
    category        VARCHAR(50)                                    COMMENT '分类: 机油/滤芯/刹车片/轮胎/其他',
    unit            VARCHAR(20)     NOT NULL DEFAULT '个'           COMMENT '单位',
    cost_price      DECIMAL(10,2)                                  COMMENT '进货成本价',
    sale_price      DECIMAL(10,2)   NOT NULL                       COMMENT '销售价',
    stock           INT             NOT NULL DEFAULT 0              COMMENT '当前库存',
    min_stock       INT             DEFAULT 0                      COMMENT '最低库存预警值',
    image_url       VARCHAR(500)                                   COMMENT '商品图片URL',
    description     TEXT                                           COMMENT '商品描述',
    status          TINYINT         NOT NULL DEFAULT 1              COMMENT '状态: 1上架 0下架',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_sku (sku),
    INDEX idx_brand (brand),
    INDEX idx_category (category),
    INDEX idx_status (status),
    FULLTEXT INDEX ft_name_desc (name, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- =============================================
-- 3. 入库记录表
-- =============================================
CREATE TABLE IF NOT EXISTS stock_in (
    id              INT             AUTO_INCREMENT PRIMARY KEY     COMMENT '主键ID',
    product_id      INT             NOT NULL                       COMMENT '商品ID',
    quantity        INT             NOT NULL                       COMMENT '入库数量',
    cost_price      DECIMAL(10,2)                                  COMMENT '本次入库成本价',
    remark          VARCHAR(500)                                   COMMENT '备注',
    operator_id     INT             NOT NULL                       COMMENT '操作员ID',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
    INDEX idx_product_id (product_id),
    INDEX idx_operator_id (operator_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库记录表';

-- =============================================
-- 4. 销售订单表
-- =============================================
CREATE TABLE IF NOT EXISTS orders (
    id              INT             AUTO_INCREMENT PRIMARY KEY     COMMENT '主键ID',
    order_no        VARCHAR(50)     NOT NULL UNIQUE                COMMENT '订单编号(SO+yyyyMMdd+4位序号)',
    customer_id     INT             DEFAULT NULL                   COMMENT '客户ID(小程序用户)',
    customer_name   VARCHAR(100)                                   COMMENT '客户姓名',
    customer_phone  VARCHAR(20)                                    COMMENT '客户电话',
    total_amount    DECIMAL(10,2)   NOT NULL                       COMMENT '订单总金额',
    payment_method  VARCHAR(20)     DEFAULT 'CASH'                 COMMENT '支付方式: CASH/WECHAT/ALIPAY/CARD',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PAID'        COMMENT '状态: PAID/REFUNDED',
    remark          VARCHAR(500)                                   COMMENT '备注',
    operator_id     INT             NOT NULL                       COMMENT '操作员ID',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    INDEX idx_order_no (order_no),
    INDEX idx_customer_id (customer_id),
    INDEX idx_operator_id (operator_id),
    INDEX idx_created_at (created_at),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单表';

-- =============================================
-- 5. 销售明细表
-- =============================================
CREATE TABLE IF NOT EXISTS order_items (
    id              INT             AUTO_INCREMENT PRIMARY KEY     COMMENT '主键ID',
    order_id        INT             NOT NULL                       COMMENT '订单ID',
    product_id      INT             NOT NULL                       COMMENT '商品ID',
    product_name    VARCHAR(200)    NOT NULL                       COMMENT '商品名称(销售时快照)',
    quantity        INT             NOT NULL                       COMMENT '数量',
    unit_price      DECIMAL(10,2)   NOT NULL                       COMMENT '单价',
    subtotal        DECIMAL(10,2)   NOT NULL                       COMMENT '小计(quantity * unit_price)',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售明细表';

-- =============================================
-- 6. 库存流水表
-- =============================================
CREATE TABLE IF NOT EXISTS inventory_logs (
    id              INT             AUTO_INCREMENT PRIMARY KEY     COMMENT '主键ID',
    product_id      INT             NOT NULL                       COMMENT '商品ID',
    type            VARCHAR(20)     NOT NULL                       COMMENT '类型: STOCK_IN(入库)/SALE(销售)/ADJUST(调整)',
    quantity        INT             NOT NULL                       COMMENT '变动数量(正增负减)',
    before_stock    INT             NOT NULL                       COMMENT '变动前库存',
    after_stock     INT             NOT NULL                       COMMENT '变动后库存',
    ref_id          INT                                            COMMENT '关联ID(入库记录ID/订单ID)',
    remark          VARCHAR(500)                                   COMMENT '备注',
    operator_id     INT             NOT NULL                       COMMENT '操作员ID',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_product_id (product_id),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at),
    INDEX idx_ref_id (ref_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- =============================================
-- 7. 客户表(小程序用户)
-- =============================================
CREATE TABLE IF NOT EXISTS customers (
    id              INT             AUTO_INCREMENT PRIMARY KEY     COMMENT '主键ID',
    open_id         VARCHAR(100)    NOT NULL UNIQUE                COMMENT '微信OpenID',
    nick_name       VARCHAR(100)                                   COMMENT '微信昵称',
    avatar_url      VARCHAR(500)                                   COMMENT '微信头像URL',
    phone           VARCHAR(20)                                    COMMENT '手机号',
    name            VARCHAR(50)                                    COMMENT '真实姓名',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_open_id (open_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表(小程序用户)';

-- =============================================
-- 8. 操作日志表
-- =============================================
CREATE TABLE IF NOT EXISTS operation_logs (
    id              INT             AUTO_INCREMENT PRIMARY KEY     COMMENT '主键ID',
    module          VARCHAR(50)     NOT NULL                       COMMENT '模块: AUTH/PRODUCT/STOCK_IN/ORDER/INVENTORY',
    action          VARCHAR(50)     NOT NULL                       COMMENT '操作类型: LOGIN/CREATE_PRODUCT/STOCK_IN/CREATE_ORDER/REFUND/ADJUST',
    content         VARCHAR(500)                                   COMMENT '操作内容描述',
    ref_id          INT                                            COMMENT '关联业务ID',
    operator_id     INT                                            COMMENT '操作员ID',
    operator_name   VARCHAR(50)                                    COMMENT '操作员姓名',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_module (module),
    INDEX idx_operator_id (operator_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- =============================================
-- 外键约束（可选，由应用层保证数据一致性）
-- =============================================
-- ALTER TABLE stock_in ADD CONSTRAINT fk_stock_in_product FOREIGN KEY (product_id) REFERENCES products(id);
-- ALTER TABLE stock_in ADD CONSTRAINT fk_stock_in_operator FOREIGN KEY (operator_id) REFERENCES users(id);
-- ALTER TABLE orders ADD CONSTRAINT fk_orders_operator FOREIGN KEY (operator_id) REFERENCES users(id);
-- ALTER TABLE orders ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id);
-- ALTER TABLE order_items ADD CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id);
-- ALTER TABLE order_items ADD CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id);
-- ALTER TABLE inventory_logs ADD CONSTRAINT fk_inventory_logs_product FOREIGN KEY (product_id) REFERENCES products(id);
-- ALTER TABLE inventory_logs ADD CONSTRAINT fk_inventory_logs_operator FOREIGN KEY (operator_id) REFERENCES users(id);
