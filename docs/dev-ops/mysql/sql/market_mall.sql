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

 Date: 29/01/2026 10:15:41
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
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车表';

-- ----------------------------
-- Records of cart_info
-- ----------------------------
BEGIN;
INSERT INTO `cart_info` (`id`, `cart_id`, `user_id`, `product_id`, `quantity`, `create_time`, `update_time`) VALUES (14, '699c942a-fb91-11f0-b8e3-5e5e85cbfcd7', 'e87da4f8-cc7c-489d-8329-2147472da4ad', 'prod_001', 1, '2026-01-27 23:04:00', '2026-01-27 23:04:00');
INSERT INTO `cart_info` (`id`, `cart_id`, `user_id`, `product_id`, `quantity`, `create_time`, `update_time`) VALUES (28, 'bda3cfd2-fc58-11f0-9079-f6846f289677', '51d0f1c2-f5ce-4878-9b2d-ee0c3b73a8e8', 'prod_001', 1, '2026-01-28 22:50:51', '2026-01-28 22:50:51');
INSERT INTO `cart_info` (`id`, `cart_id`, `user_id`, `product_id`, `quantity`, `create_time`, `update_time`) VALUES (29, '3e08396e-fc5a-11f0-9079-f6846f289677', 'user_001', 'prod_001', 1, '2026-01-28 23:01:36', '2026-01-28 23:01:36');
COMMIT;

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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收藏表';

-- ----------------------------
-- Records of favorite_info
-- ----------------------------
BEGIN;
INSERT INTO `favorite_info` (`id`, `favorite_id`, `user_id`, `product_id`, `create_time`, `update_time`) VALUES (1, '1e2f682c-4833-4756-a380-6eed5d7b2bda', 'user_001', 'prod_001', '2026-01-27 13:15:16', '2026-01-27 13:15:16');
INSERT INTO `favorite_info` (`id`, `favorite_id`, `user_id`, `product_id`, `create_time`, `update_time`) VALUES (3, '90b8cc5c-1dcf-444f-b6c6-a9653edbb4a1', '51d0f1c2-f5ce-4878-9b2d-ee0c3b73a8e8', 'prod_001', '2026-01-28 22:03:14', '2026-01-28 22:03:14');
COMMIT;

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
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单信息表';

-- ----------------------------
-- Records of order_info
-- ----------------------------
BEGIN;
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (1, '13f057b2-8df9-4786-a671-f1f513be3104', 'user_001', 8999.00, 0, NULL, NULL, NULL, '2026-01-27 13:29:38', '2026-01-27 13:29:38');
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (2, 'b4122c2e-9f64-496f-aca5-c0f07d94709d', '51d0f1c2-f5ce-4878-9b2d-ee0c3b73a8e8', 8999.00, 0, NULL, NULL, NULL, '2026-01-27 20:09:05', '2026-01-27 20:09:05');
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (3, '71466eed-3838-4057-95eb-33148c938780', '51d0f1c2-f5ce-4878-9b2d-ee0c3b73a8e8', 7999.00, 0, NULL, NULL, NULL, '2026-01-27 20:39:16', '2026-01-27 20:39:16');
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (4, 'b6f15d75-ef90-48fb-93a5-1e7b3e62b347', 'e87da4f8-cc7c-489d-8329-2147472da4ad', 4499.00, 0, NULL, NULL, NULL, '2026-01-27 21:04:16', '2026-01-27 21:04:16');
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (5, 'e93372b6-5ade-44d9-ab43-a79f8910a762', 'e87da4f8-cc7c-489d-8329-2147472da4ad', 799.00, 0, NULL, NULL, NULL, '2026-01-27 21:05:06', '2026-01-27 21:05:06');
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (6, '3f389c3b-f424-410a-9884-7cd8a6ce24b8', 'e87da4f8-cc7c-489d-8329-2147472da4ad', 699.00, 0, NULL, NULL, NULL, '2026-01-27 22:31:31', '2026-01-27 22:31:31');
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (7, 'fbf78c73-ae29-43f9-b94a-5a909528ad3d', 'e87da4f8-cc7c-489d-8329-2147472da4ad', 8999.00, 0, NULL, NULL, NULL, '2026-01-27 22:51:32', '2026-01-27 22:51:32');
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (8, '0aa42f3c-688f-4dfc-90f8-98765869c362', 'e87da4f8-cc7c-489d-8329-2147472da4ad', 8999.00, 0, NULL, NULL, NULL, '2026-01-27 22:51:57', '2026-01-27 22:51:57');
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (9, '21ce5ef7-be4d-44d6-b585-310e5aa06a16', 'e87da4f8-cc7c-489d-8329-2147472da4ad', 2499.00, 0, NULL, NULL, NULL, '2026-01-27 22:59:37', '2026-01-27 22:59:37');
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (10, 'd6c3e31d-b45a-4ba4-b697-686f9ea43229', 'e87da4f8-cc7c-489d-8329-2147472da4ad', 33996.00, 1, NULL, NULL, NULL, '2026-01-27 23:01:05', '2026-01-27 23:01:05');
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (11, 'bde979d7-3823-4281-aa09-82bf76be427b', 'user_001', 15998.00, 1, NULL, NULL, NULL, '2026-01-28 16:31:04', '2026-01-28 16:31:04');
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (12, '96a59903-25f5-4ffe-a134-ff5a1c4236fc', 'user_001', 34996.00, 1, NULL, NULL, NULL, '2026-01-28 16:37:57', '2026-01-28 16:37:57');
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (13, '4b4ad4b7-9f15-4c5c-b833-2a11d1bde3df', 'user_001', 2499.00, 1, '江苏 常州 ', '张三', '18923779090', '2026-01-28 19:38:43', '2026-01-28 19:38:43');
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (14, '38ee2fb0-dd39-42d9-bb5b-5185f0132535', 'user_001', 8999.00, 1, '吉林 辽源 家', '张三', '178673236666', '2026-01-28 19:39:58', '2026-01-28 19:39:58');
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (15, 'd7c0d0d8-50ad-4d09-9355-1a06a69ab4b1', 'user_001', 7999.00, 1, '吉林 辽源 ', '张三', '178673236666', '2026-01-28 20:50:33', '2026-01-28 20:50:33');
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (16, 'ef402dd6-eaa7-49db-9251-05e61c9cdee5', '51d0f1c2-f5ce-4878-9b2d-ee0c3b73a8e8', 7999.00, 1, '天津 天津 ', '李四', '16723623533', '2026-01-28 22:04:07', '2026-01-28 22:04:07');
INSERT INTO `order_info` (`id`, `order_id`, `user_id`, `total_amount`, `status`, `address`, `contact_name`, `contact_phone`, `create_time`, `update_time`) VALUES (17, 'e4bc820c-006d-462c-84a1-c0e00e04ed7b', '51d0f1c2-f5ce-4878-9b2d-ee0c3b73a8e8', 8999.00, 1, '天津 天津 ', '李四', '16723623533', '2026-01-28 22:21:11', '2026-01-28 22:21:11');
COMMIT;

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `item_id` varchar(64) NOT NULL COMMENT '业务明细ID',
  `order_id` varchar(64) NOT NULL COMMENT '订单ID',
  `product_id` varchar(64) NOT NULL COMMENT '商品ID',
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
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';

-- ----------------------------
-- Records of order_item
-- ----------------------------
BEGIN;
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (1, '2c2f7645-fb41-11f0-9641-069388b68d72', '13f057b2-8df9-4786-a671-f1f513be3104', 'prod_001', 'iPhone 15 Pro', 'https://img.yzcdn.cn/vant/apple-1.jpg', 8999.00, 1, 8999.00, '2026-01-27 13:29:38', '2026-01-27 13:29:38');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (2, 'f9fde87c-fb78-11f0-b8e3-5e5e85cbfcd7', 'b4122c2e-9f64-496f-aca5-c0f07d94709d', 'prod_001', 'iPhone 15 Pro', 'https://img.yzcdn.cn/vant/apple-1.jpg', 8999.00, 1, 8999.00, '2026-01-27 20:09:05', '2026-01-27 20:09:05');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (3, '31381f31-fb7d-11f0-b8e3-5e5e85cbfcd7', '71466eed-3838-4057-95eb-33148c938780', 'prod_002', 'MacBook Air M2', 'https://img.yzcdn.cn/vant/apple-2.jpg', 7999.00, 1, 7999.00, '2026-01-27 20:39:16', '2026-01-27 20:39:16');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (4, 'afa07add-fb80-11f0-b8e3-5e5e85cbfcd7', 'b6f15d75-ef90-48fb-93a5-1e7b3e62b347', 'prod_006', 'Dyson V12 Detect', 'https://img.yzcdn.cn/vant/sand.jpg', 4499.00, 1, 4499.00, '2026-01-27 21:04:16', '2026-01-27 21:04:16');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (5, 'cd0349d5-fb80-11f0-b8e3-5e5e85cbfcd7', 'e93372b6-5ade-44d9-ab43-a79f8910a762', 'prod_004', 'Nike Air Force 1', 'https://img.yzcdn.cn/vant/cat.jpeg', 799.00, 1, 799.00, '2026-01-27 21:05:06', '2026-01-27 21:05:06');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (6, 'dfd9bed6-fb8c-11f0-b8e3-5e5e85cbfcd7', '3f389c3b-f424-410a-9884-7cd8a6ce24b8', 'prod_008', 'Logitech MX Master 3S', 'https://img.yzcdn.cn/vant/apple-5.jpg', 699.00, 1, 699.00, '2026-01-27 22:31:31', '2026-01-27 22:31:31');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (7, 'ab6c56ed-fb8f-11f0-b8e3-5e5e85cbfcd7', 'fbf78c73-ae29-43f9-b94a-5a909528ad3d', 'prod_001', 'iPhone 15 Pro', 'https://img.yzcdn.cn/vant/apple-1.jpg', 8999.00, 1, 8999.00, '2026-01-27 22:51:32', '2026-01-27 22:51:32');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (8, 'ba4a61b4-fb8f-11f0-b8e3-5e5e85cbfcd7', '0aa42f3c-688f-4dfc-90f8-98765869c362', 'prod_001', 'iPhone 15 Pro', 'https://img.yzcdn.cn/vant/apple-1.jpg', 8999.00, 1, 8999.00, '2026-01-27 22:51:57', '2026-01-27 22:51:57');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (9, 'cccc882d-fb90-11f0-b8e3-5e5e85cbfcd7', '21ce5ef7-be4d-44d6-b585-310e5aa06a16', 'prod_003', 'Sony WH-1000XM5', 'https://img.yzcdn.cn/vant/apple-3.jpg', 2499.00, 1, 2499.00, '2026-01-27 22:59:37', '2026-01-27 22:59:37');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (10, '0153de99-fb91-11f0-b8e3-5e5e85cbfcd7', 'd6c3e31d-b45a-4ba4-b697-686f9ea43229', 'prod_001', 'iPhone 15 Pro', 'https://img.yzcdn.cn/vant/apple-1.jpg', 8999.00, 2, 17998.00, '2026-01-27 23:01:05', '2026-01-27 23:01:05');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (11, '01543e47-fb91-11f0-b8e3-5e5e85cbfcd7', 'd6c3e31d-b45a-4ba4-b697-686f9ea43229', 'prod_002', 'MacBook Air M2', 'https://img.yzcdn.cn/vant/apple-2.jpg', 7999.00, 2, 15998.00, '2026-01-27 23:01:05', '2026-01-27 23:01:05');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (12, 'af3a4afc-fc23-11f0-9079-f6846f289677', 'bde979d7-3823-4281-aa09-82bf76be427b', 'prod_002', 'MacBook Air M2', 'https://img.yzcdn.cn/vant/apple-2.jpg', 7999.00, 2, 15998.00, '2026-01-28 16:31:04', '2026-01-28 16:31:04');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (13, 'a5442fe2-fc24-11f0-9079-f6846f289677', '96a59903-25f5-4ffe-a134-ff5a1c4236fc', 'prod_001', 'iPhone 15 Pro', 'https://img.yzcdn.cn/vant/apple-1.jpg', 8999.00, 3, 26997.00, '2026-01-28 16:37:57', '2026-01-28 16:37:57');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (14, 'a544c2e9-fc24-11f0-9079-f6846f289677', '96a59903-25f5-4ffe-a134-ff5a1c4236fc', 'prod_002', 'MacBook Air M2', 'https://img.yzcdn.cn/vant/apple-2.jpg', 7999.00, 1, 7999.00, '2026-01-28 16:37:57', '2026-01-28 16:37:57');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (15, 'e64b345e-fc3d-11f0-9079-f6846f289677', '4b4ad4b7-9f15-4c5c-b833-2a11d1bde3df', 'prod_003', 'Sony WH-1000XM5', 'https://img.yzcdn.cn/vant/apple-3.jpg', 2499.00, 1, 2499.00, '2026-01-28 19:38:43', '2026-01-28 19:38:43');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (16, '12c84a2f-fc3e-11f0-9079-f6846f289677', '38ee2fb0-dd39-42d9-bb5b-5185f0132535', 'prod_001', 'iPhone 15 Pro', 'https://img.yzcdn.cn/vant/apple-1.jpg', 8999.00, 1, 8999.00, '2026-01-28 19:39:58', '2026-01-28 19:39:58');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (17, 'ef3d3430-fc47-11f0-9079-f6846f289677', 'd7c0d0d8-50ad-4d09-9355-1a06a69ab4b1', 'prod_002', 'MacBook Air M2', 'https://img.yzcdn.cn/vant/apple-2.jpg', 7999.00, 1, 7999.00, '2026-01-28 20:50:33', '2026-01-28 20:50:33');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (18, '3677b658-fc52-11f0-9079-f6846f289677', 'ef402dd6-eaa7-49db-9251-05e61c9cdee5', 'prod_002', 'MacBook Air M2', 'https://img.yzcdn.cn/vant/apple-2.jpg', 7999.00, 1, 7999.00, '2026-01-28 22:04:07', '2026-01-28 22:04:07');
INSERT INTO `order_item` (`id`, `item_id`, `order_id`, `product_id`, `product_name`, `product_image`, `current_price`, `quantity`, `total_price`, `create_time`, `update_time`) VALUES (19, '985b896b-fc54-11f0-9079-f6846f289677', 'e4bc820c-006d-462c-84a1-c0e00e04ed7b', 'prod_001', 'iPhone 15 Pro', 'https://img.yzcdn.cn/vant/apple-1.jpg', 8999.00, 1, 8999.00, '2026-01-28 22:21:11', '2026-01-28 22:21:11');
COMMIT;

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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品信息表';

-- ----------------------------
-- Records of product_info
-- ----------------------------
BEGIN;
INSERT INTO `product_info` (`id`, `product_id`, `name`, `description`, `price`, `stock`, `image_url`, `category`, `status`, `create_time`, `update_time`, `brand`, `keywords`) VALUES (11, 'prod_001', 'iPhone 15 Pro', '苹果 iPhone 15 Pro 256GB 黑色钛金属，强劲的A17 Pro芯片，专业的摄像系统。', 8999.00, 100, 'https://img.yzcdn.cn/vant/apple-1.jpg', 'cat_phone', 1, '2026-01-27 13:12:56', '2026-01-27 13:12:56', 'Apple', 'phone,apple,iphone,mobile');
INSERT INTO `product_info` (`id`, `product_id`, `name`, `description`, `price`, `stock`, `image_url`, `category`, `status`, `create_time`, `update_time`, `brand`, `keywords`) VALUES (12, 'prod_002', 'MacBook Air M2', '苹果 MacBook Air M2 13英寸 8GB+256GB 银色，轻薄设计，超长续航。', 7999.00, 50, 'https://img.yzcdn.cn/vant/apple-2.jpg', 'cat_laptop', 1, '2026-01-27 13:12:56', '2026-01-27 13:12:56', 'Apple', 'laptop,computer,macbook,apple');
INSERT INTO `product_info` (`id`, `product_id`, `name`, `description`, `price`, `stock`, `image_url`, `category`, `status`, `create_time`, `update_time`, `brand`, `keywords`) VALUES (13, 'prod_003', 'Sony WH-1000XM5', '索尼无线降噪耳机，业界领先的降噪技术，舒适佩戴体验。', 2499.00, 200, 'https://img.yzcdn.cn/vant/apple-3.jpg', 'cat_audio', 1, '2026-01-27 13:12:56', '2026-01-27 13:12:56', 'Sony', 'headphone,audio,music,sony');
INSERT INTO `product_info` (`id`, `product_id`, `name`, `description`, `price`, `stock`, `image_url`, `category`, `status`, `create_time`, `update_time`, `brand`, `keywords`) VALUES (14, 'prod_004', 'Nike Air Force 1', '耐克 Air Force 1 \'07 男子运动鞋，经典白色，时尚百搭。', 799.00, 300, 'https://img.yzcdn.cn/vant/cat.jpeg', 'cat_shoes', 1, '2026-01-27 13:12:56', '2026-01-27 13:12:56', 'Nike', 'shoes,sneakers,nike,fashion');
INSERT INTO `product_info` (`id`, `product_id`, `name`, `description`, `price`, `stock`, `image_url`, `category`, `status`, `create_time`, `update_time`, `brand`, `keywords`) VALUES (15, 'prod_005', 'Uniqlo T-Shirt', '优衣库 U系列圆领短袖T恤，纯棉材质，舒适透气。', 99.00, 1000, 'https://img.yzcdn.cn/vant/t-shirt.jpg', 'cat_clothing', 1, '2026-01-27 13:12:56', '2026-01-27 13:12:56', 'Uniqlo', 'clothing,t-shirt,uniqlo,casual');
INSERT INTO `product_info` (`id`, `product_id`, `name`, `description`, `price`, `stock`, `image_url`, `category`, `status`, `create_time`, `update_time`, `brand`, `keywords`) VALUES (16, 'prod_006', 'Dyson V12 Detect', '戴森 V12 Detect Slim Fluffy 轻量吸尘器，激光探测，强劲吸力。', 4499.00, 30, 'https://img.yzcdn.cn/vant/sand.jpg', 'cat_home', 1, '2026-01-27 13:12:56', '2026-01-27 13:12:56', 'Dyson', 'home,vacuum,dyson,cleaning');
INSERT INTO `product_info` (`id`, `product_id`, `name`, `description`, `price`, `stock`, `image_url`, `category`, `status`, `create_time`, `update_time`, `brand`, `keywords`) VALUES (17, 'prod_007', 'iPad Air 5', '苹果 iPad Air 5 10.9英寸 64GB Wi-Fi版 蓝色，M1芯片，性能强大。', 4399.00, 80, 'https://img.yzcdn.cn/vant/apple-4.jpg', 'cat_tablet', 1, '2026-01-27 13:12:56', '2026-01-27 13:12:56', 'Apple', 'tablet,ipad,apple,mobile');
INSERT INTO `product_info` (`id`, `product_id`, `name`, `description`, `price`, `stock`, `image_url`, `category`, `status`, `create_time`, `update_time`, `brand`, `keywords`) VALUES (18, 'prod_008', 'Logitech MX Master 3S', '罗技 MX Master 3S 高性能无线鼠标，静音滚轮，人体工学设计。', 699.00, 150, 'https://img.yzcdn.cn/vant/apple-5.jpg', 'cat_accessory', 1, '2026-01-27 13:12:56', '2026-01-27 13:12:56', 'Logitech', 'mouse,computer,accessory,logitech');
INSERT INTO `product_info` (`id`, `product_id`, `name`, `description`, `price`, `stock`, `image_url`, `category`, `status`, `create_time`, `update_time`, `brand`, `keywords`) VALUES (19, 'prod_009', 'Samsung Galaxy S24', '三星 Galaxy S24 Ultra 12GB+256GB 钛灰，AI功能加持，超高清拍摄。', 9699.00, 60, 'https://img.yzcdn.cn/vant/apple-6.jpg', 'cat_phone', 1, '2026-01-27 13:12:56', '2026-01-27 13:12:56', 'Samsung', 'phone,samsung,galaxy,android');
INSERT INTO `product_info` (`id`, `product_id`, `name`, `description`, `price`, `stock`, `image_url`, `category`, `status`, `create_time`, `update_time`, `brand`, `keywords`) VALUES (20, 'prod_010', 'PlayStation 5', '索尼 PlayStation 5 游戏主机（光驱版），次世代游戏体验，超高速SSD。', 3599.00, 40, 'https://img.yzcdn.cn/vant/apple-7.jpg', 'cat_game', 1, '2026-01-27 13:12:56', '2026-01-27 13:12:56', 'Sony', 'game,console,ps5,sony');
COMMIT;

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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收货地址表';

-- ----------------------------
-- Records of shipping_address
-- ----------------------------
BEGIN;
INSERT INTO `shipping_address` (`id`, `user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `detail_address`, `is_default`, `create_time`, `update_time`) VALUES (1, 'user_001', '张三', '18923779090', '江苏', '常州', '', 0, '2026-01-28 19:30:23', '2026-01-28 23:14:22');
INSERT INTO `shipping_address` (`id`, `user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `detail_address`, `is_default`, `create_time`, `update_time`) VALUES (2, 'user_001', '张三', '178673236666', '吉林', '辽源', '', 1, '2026-01-28 19:31:05', '2026-01-28 23:14:22');
INSERT INTO `shipping_address` (`id`, `user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `detail_address`, `is_default`, `create_time`, `update_time`) VALUES (3, '51d0f1c2-f5ce-4878-9b2d-ee0c3b73a8e8', '李四', '16723623533', '天津', '天津', '', 1, '2026-01-28 22:03:58', '2026-01-28 22:56:59');
COMMIT;

-- ----------------------------
-- Table structure for user_behavior_log
-- ----------------------------
DROP TABLE IF EXISTS `user_behavior_log`;
CREATE TABLE `user_behavior_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `product_id` varchar(64) NOT NULL COMMENT '商品ID',
  `behavior_type` varchar(16) NOT NULL COMMENT '行为类型: click, view, cart, buy',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_product` (`user_id`,`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户行为流水表';

-- ----------------------------
-- Records of user_behavior_log
-- ----------------------------
BEGIN;
COMMIT;

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
  `gender` tinyint DEFAULT '0' COMMENT '性别: 0-未知, 1-男, 2-女',
  `city` varchar(64) DEFAULT '' COMMENT '城市',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息表';

-- ----------------------------
-- Records of user_info
-- ----------------------------
BEGIN;
INSERT INTO `user_info` (`id`, `user_id`, `username`, `password`, `phone`, `create_time`, `update_time`, `age`, `gender`, `city`) VALUES (2, 'user_001', 'test_user', 'e10adc3949ba59abbe56e057f20f883e', '13800138000', '2026-01-27 13:12:56', '2026-01-27 13:12:56', 25, 1, 'Shanghai');
INSERT INTO `user_info` (`id`, `user_id`, `username`, `password`, `phone`, `create_time`, `update_time`, `age`, `gender`, `city`) VALUES (4, '51d0f1c2-f5ce-4878-9b2d-ee0c3b73a8e8', 'hxx', '4297f44b13955235245b2497399d7a93', '13077994723', '2026-01-27 19:38:14', '2026-01-27 19:38:14', 23, 1, '深圳');
INSERT INTO `user_info` (`id`, `user_id`, `username`, `password`, `phone`, `create_time`, `update_time`, `age`, `gender`, `city`) VALUES (5, 'e87da4f8-cc7c-489d-8329-2147472da4ad', 'hww', '4297f44b13955235245b2497399d7a93', '13077994722', '2026-01-27 20:53:17', '2026-01-27 20:53:17', 26, 1, '青岛');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
