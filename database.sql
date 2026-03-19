-- 西胪莲藕电商管理系统数据库脚本 (MySQL 8.0)

-- 创建数据库
CREATE DATABASE IF NOT EXISTS lotus_shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE lotus_shop;

-- 用户表（农户/客户/管理员）
CREATE TABLE `sys_user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `role` ENUM('FARMER', 'CUSTOMER', 'ADMIN') NOT NULL COMMENT '角色：农户、客户、管理员',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：1启用，0禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品分类表
CREATE TABLE `product_category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父分类ID',
  `sort_order` INT(11) DEFAULT 0 COMMENT '排序',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：1启用，0禁用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`category_name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 莲藕商品表
CREATE TABLE `lotus_root_product` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `category_id` BIGINT(20) NOT NULL COMMENT '分类ID',
  `farmer_id` BIGINT(20) NOT NULL COMMENT '农户ID',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `stock` INT(11) NOT NULL COMMENT '库存',
  `description` TEXT COMMENT '商品描述',
  `image_url` VARCHAR(200) COMMENT '商品图片',
  `status` ENUM('PENDING', 'APPROVED', 'REJECTED', 'ON_SALE', 'OFF_SALE') DEFAULT 'PENDING' COMMENT '状态：待审核、已审核、已拒绝、上架、下架',
  `origin` VARCHAR(100) NOT NULL COMMENT '产地',
  `harvest_time` DATE NOT NULL COMMENT '采摘时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`category_id`) REFERENCES `product_category` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`farmer_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='莲藕商品表';

-- 购物车表
CREATE TABLE `shopping_cart` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `product_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `quantity` INT(11) NOT NULL COMMENT '数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`product_id`) REFERENCES `lotus_root_product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 收货地址表
CREATE TABLE `user_address` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `consignee` VARCHAR(50) NOT NULL COMMENT '收货人',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `city` VARCHAR(50) NOT NULL COMMENT '城市',
  `district` VARCHAR(50) NOT NULL COMMENT '区县',
  `detail_address` VARCHAR(200) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT(1) DEFAULT 0 COMMENT '是否默认地址',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- 订单主表
CREATE TABLE `order_main` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `address_id` BIGINT(20) NOT NULL COMMENT '地址ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
  `receiver_address` VARCHAR(200) NOT NULL COMMENT '收货地址',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '总金额',
  `status` ENUM('PENDING', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING' COMMENT '状态：待支付、已支付、已发货、已送达、已取消',
  `payment_time` DATETIME COMMENT '支付时间',
  `shipping_time` DATETIME COMMENT '发货时间',
  `delivery_time` DATETIME COMMENT '送达时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`address_id`) REFERENCES `user_address` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 订单项表
CREATE TABLE `order_item` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
  `order_id` BIGINT(20) NOT NULL COMMENT '订单ID',
  `product_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `farmer_id` BIGINT(20) NOT NULL COMMENT '农户ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `quantity` INT(11) NOT NULL COMMENT '数量',
  `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`order_id`) REFERENCES `order_main` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`product_id`) REFERENCES `lotus_root_product` (`id`) ON DELETE CASCADE,
  FOREIGN KEY (`farmer_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

-- 溯源信息表（扩展表）
CREATE TABLE `traceability_info` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '溯源ID',
  `product_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `seed_date` DATE NOT NULL COMMENT '播种日期',
  `fertilizer_record` TEXT COMMENT '施肥记录',
  `pesticide_record` TEXT COMMENT '农药使用记录',
  `harvest_date` DATE NOT NULL COMMENT '收获日期',
  `inspection_report` VARCHAR(200) COMMENT '检测报告',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_id` (`product_id`),
  FOREIGN KEY (`product_id`) REFERENCES `lotus_root_product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='溯源信息表';

-- 初始化数据

-- 初始化分类数据
INSERT INTO `product_category` (`category_name`, `parent_id`, `sort_order`) VALUES
('莲藕', 0, 1),
('粉藕', 1, 1),
('脆藕', 1, 2),
('藕尖', 1, 3);

-- 初始化管理员账号
INSERT INTO `sys_user` (`username`, `password`, `name`, `phone`, `role`, `status`) VALUES
('admin', '$2a$10$e5eX1U2eZ6y9f5Q7c8d9e0f1g2h3i4j5k6l7m8n9o0p1q2r3s4t5u6v7w8x9y0z', '管理员', '13800138000', 'ADMIN', 1);

-- 初始化农户账号
INSERT INTO `sys_user` (`username`, `password`, `name`, `phone`, `role`, `status`) VALUES
('farmer1', '$2a$10$e5eX1U2eZ6y9f5Q7c8d9e0f1g2h3i4j5k6l7m8n9o0p1q2r3s4t5u6v7w8x9y0z', '张三', '13800138001', 'FARMER', 1),
('farmer2', '$2a$10$e5eX1U2eZ6y9f5Q7c8d9e0f1g2h3i4j5k6l7m8n9o0p1q2r3s4t5u6v7w8x9y0z', '李四', '13800138002', 'FARMER', 1);

-- 初始化客户账号
INSERT INTO `sys_user` (`username`, `password`, `name`, `phone`, `role`, `status`) VALUES
('customer1', '$2a$10$e5eX1U2eZ6y9f5Q7c8d9e0f1g2h3i4j5k6l7m8n9o0p1q2r3s4t5u6v7w8x9y0z', '王五', '13800138003', 'CUSTOMER', 1),
('customer2', '$2a$10$e5eX1U2eZ6y9f5Q7c8d9e0f1g2h3i4j5k6l7m8n9o0p1q2r3s4t5u6v7w8x9y0z', '赵六', '13800138004', 'CUSTOMER', 1);

-- 初始化商品数据
INSERT INTO `lotus_root_product` (`product_name`, `category_id`, `farmer_id`, `price`, `stock`, `description`, `origin`, `harvest_time`, `status`) VALUES
('西胪粉藕', 2, 1002, 12.8, 100, '正宗西胪粉藕，口感粉糯', '汕头市潮阳区西胪镇', '2024-01-15', 'APPROVED'),
('西胪脆藕', 3, 1002, 9.8, 150, '新鲜脆藕，适合清炒', '汕头市潮阳区西胪镇', '2024-01-16', 'APPROVED'),
('西胪藕尖', 4, 1003, 19.8, 80, '鲜嫩藕尖，清脆可口', '汕头市潮阳区西胪镇', '2024-01-17', 'APPROVED');

-- 初始化溯源信息
INSERT INTO `traceability_info` (`product_id`, `seed_date`, `fertilizer_record`, `pesticide_record`, `harvest_date`, `inspection_report`) VALUES
(1, '2023-09-01', '有机肥料，每月一次', '无农药使用', '2024-01-15', '合格'),
(2, '2023-09-05', '有机肥料，每月一次', '无农药使用', '2024-01-16', '合格'),
(3, '2023-09-10', '有机肥料，每月一次', '无农药使用', '2024-01-17', '合格');