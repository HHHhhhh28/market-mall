/*
 Navicat Premium Dump SQL

 Source Server         : localhost_mysql
 Source Server Type    : MySQL
 Source Server Version : 80032 (8.0.32)
 Source Host           : localhost:13306
 Source Schema         : market_mall

 Target Server Type    : MySQL
 Target Server Version : 80032 (8.0.32)
 File Encoding         : 65001

 Date: 22/02/2026 22:37:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cart_info
-- ----------------------------
DROP TABLE IF EXISTS `cart_info`;
CREATE TABLE `cart_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cart_id` varchar(64) NOT NULL COMMENT '业务购物车ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `product_id` varchar(64) NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_id` (`cart_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车表';

-- ----------------------------
-- Table structure for coupon
-- ----------------------------
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `coupon_id` varchar(64) NOT NULL,
  `category` varchar(64) NOT NULL COMMENT '商品品类',
  `coupon_type` varchar(20) NOT NULL COMMENT 'DIRECT/FULL/DISCOUNT',
  `value` decimal(10,2) NOT NULL COMMENT '优惠值',
  `name` varchar(128) NOT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1可用 0禁用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='品类优惠券池';

-- ----------------------------
-- Table structure for favorite_info
-- ----------------------------
DROP TABLE IF EXISTS `favorite_info`;
CREATE TABLE `favorite_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `favorite_id` varchar(64) NOT NULL COMMENT '业务收藏ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `product_id` varchar(64) NOT NULL COMMENT '商品ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_favorite_id` (`favorite_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收藏表';

-- ----------------------------
-- Table structure for order_info
-- ----------------------------
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` varchar(64) NOT NULL COMMENT '业务订单ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消',
  `address` varchar(255) DEFAULT NULL COMMENT '收货地址快照',
  `contact_name` varchar(64) DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `coupon_id` varchar(64) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单信息表';

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_id` varchar(64) NOT NULL COMMENT '业务明细ID',
  `order_id` varchar(64) NOT NULL COMMENT '订单ID',
  `product_id` varchar(64) NOT NULL COMMENT '商品ID',
  `product_type` varchar(20) NOT NULL DEFAULT 'NORMAL' COMMENT '商品类型：NORMAL-普通商品、GROUP_BUY-拼团商品、AWARD-抽奖商品',
  `product_name` varchar(128) NOT NULL COMMENT '商品名称快照',
  `product_image` varchar(255) DEFAULT NULL COMMENT '商品图片快照',
  `current_price` decimal(10,2) NOT NULL COMMENT '购买时价格',
  `quantity` int NOT NULL COMMENT '购买数量',
  `total_price` decimal(10,2) NOT NULL COMMENT '该项总价',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_id` (`item_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';

-- ----------------------------
-- Table structure for product_info
-- ----------------------------
DROP TABLE IF EXISTS `product_info`;
CREATE TABLE `product_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` varchar(64) NOT NULL COMMENT '业务商品ID',
  `name` varchar(128) NOT NULL COMMENT '商品名称',
  `description` text COMMENT '商品描述',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `stock` int NOT NULL DEFAULT '0' COMMENT '库存数量',
  `image_url` varchar(255) DEFAULT NULL COMMENT '商品图片URL',
  `category` varchar(64) DEFAULT NULL COMMENT '商品品类',
  `status` tinyint DEFAULT '1' COMMENT '状态: 1-上架, 0-下架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `brand` varchar(64) DEFAULT '' COMMENT '品牌',
  `keywords` varchar(255) DEFAULT '' COMMENT '关键词',
  `user_tags` varchar(255) NOT NULL DEFAULT '' COMMENT '用户适配标签，英文逗号分隔（示例：男,18-25,26-35/女,18-35/通用,18-45）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=941 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品信息表';

-- ----------------------------
-- Table structure for shipping_address
-- ----------------------------
DROP TABLE IF EXISTS `shipping_address`;
CREATE TABLE `shipping_address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(64) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `province` varchar(64) NOT NULL COMMENT '省份',
  `city` varchar(64) NOT NULL COMMENT '城市',
  `detail_address` varchar(255) NOT NULL COMMENT '详细地址',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认地址',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收货地址表';

-- ----------------------------
-- Table structure for user_behavior
-- ----------------------------
DROP TABLE IF EXISTS `user_behavior`;
CREATE TABLE `user_behavior` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `product_id` varchar(64) NOT NULL COMMENT '商品ID',
  `behavior_type` varchar(16) NOT NULL COMMENT '行为类型: click, view, cart, buy',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_product` (`user_id`,`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=217 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户行为流水表';

-- ----------------------------
-- Table structure for user_coupon
-- ----------------------------
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(64) NOT NULL,
  `coupon_id` varchar(64) NOT NULL,
  `category` varchar(64) NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0未使用 1已使用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户优惠券';

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) NOT NULL COMMENT '业务用户ID',
  `username` varchar(64) DEFAULT NULL COMMENT '用户名',
  `password` varchar(128) DEFAULT NULL COMMENT '密码',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `age` int DEFAULT '0' COMMENT '年龄',
  `gender` tinyint DEFAULT '0' COMMENT '性别: 0-女, 1-男',
  `city` varchar(64) DEFAULT '' COMMENT '城市',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息表';

-- ----------------------------
-- Table structure for user_lottery
-- ----------------------------
DROP TABLE IF EXISTS `user_lottery`;
CREATE TABLE `user_lottery` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(64) NOT NULL,
  `lottery_count` int NOT NULL DEFAULT '0' COMMENT '可用抽奖次数',
  `last_sign_date` date DEFAULT NULL COMMENT '最后签到日期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户抽奖次数&签到';

SET FOREIGN_KEY_CHECKS = 1;
