-- 校园二手交易平台 MySQL 建表脚本（MySQL 8.0+）
-- 执行顺序：mysql-schema.sql -> data.sql

CREATE DATABASE IF NOT EXISTS campus_market
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE campus_market;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户 id',
    username VARCHAR(20) NOT NULL COMMENT '登录账号',
    password VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密密码',
    nickname VARCHAR(20) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像地址',
    phone VARCHAR(20) COMMENT '手机号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类 id',
    name VARCHAR(20) NOT NULL COMMENT '分类名称',
    sort_order INT DEFAULT 0 COMMENT '排序值',
    UNIQUE KEY uk_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品分类表';

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品 id',
    title VARCHAR(60) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    price DECIMAL(10, 2) NOT NULL COMMENT '售价',
    original_price DECIMAL(10, 2) COMMENT '原价',
    category_id BIGINT COMMENT '分类 id',
    seller_id BIGINT NOT NULL COMMENT '卖家 id',
    cover VARCHAR(255) COMMENT '封面图',
    condition_level INT DEFAULT 10 COMMENT '成色 1-10',
    status VARCHAR(20) NOT NULL DEFAULT 'ON_SALE' COMMENT 'ON_SALE/OFF_SHELF/LOCKED/SOLD',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览量',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_status (status),
    KEY idx_category (category_id),
    KEY idx_seller (seller_id),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_products_seller FOREIGN KEY (seller_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品表';

CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图片 id',
    product_id BIGINT NOT NULL COMMENT '商品 id',
    url VARCHAR(255) NOT NULL COMMENT '图片地址',
    sort_order INT DEFAULT 0 COMMENT '展示顺序',
    KEY idx_product (product_id),
    CONSTRAINT fk_images_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品图片表';

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单 id',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    product_id BIGINT NOT NULL COMMENT '商品 id',
    buyer_id BIGINT NOT NULL COMMENT '买家 id',
    seller_id BIGINT NOT NULL COMMENT '卖家 id',
    amount DECIMAL(10, 2) NOT NULL COMMENT '成交金额',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAY' COMMENT 'PENDING_PAY/PAID/COMPLETED/CANCELLED',
    remark VARCHAR(200) COMMENT '买家留言',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    paid_at DATETIME COMMENT '支付时间',
    finished_at DATETIME COMMENT '完成时间',
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_buyer (buyer_id),
    KEY idx_seller (seller_id),
    CONSTRAINT fk_orders_buyer FOREIGN KEY (buyer_id) REFERENCES users (id),
    CONSTRAINT fk_orders_seller FOREIGN KEY (seller_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单表';

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '明细 id',
    order_id BIGINT NOT NULL COMMENT '订单 id',
    product_id BIGINT NOT NULL COMMENT '商品 id',
    product_title VARCHAR(60) COMMENT '下单时商品标题快照',
    product_cover VARCHAR(255) COMMENT '下单时商品封面快照',
    price DECIMAL(10, 2) COMMENT '成交单价',
    quantity INT DEFAULT 1 COMMENT '数量',
    KEY idx_order (order_id),
    CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单明细表';
