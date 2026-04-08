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

 Date: 19/03/2026 19:52:14
*/

SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cart_info
-- ----------------------------
DROP TABLE IF EXISTS `cart_info`;
CREATE TABLE `cart_info`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `cart_id`     varchar(64) NOT NULL COMMENT '业务购物车ID',
    `user_id`     varchar(64) NOT NULL COMMENT '用户ID',
    `product_id`  varchar(64) NOT NULL COMMENT '商品ID',
    `quantity`    int         NOT NULL DEFAULT '1' COMMENT '数量',
    `create_time` datetime             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cart_id` (`cart_id`),
    KEY           `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车表';

-- ----------------------------
-- Table structure for coupon
-- ----------------------------
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon`
(
    `id`          bigint         NOT NULL AUTO_INCREMENT,
    `coupon_id`   varchar(64)    NOT NULL,
    `category`    varchar(64)    NOT NULL COMMENT '商品品类',
    `coupon_type` varchar(20)    NOT NULL COMMENT 'DIRECT/FULL/DISCOUNT',
    `value`       decimal(10, 2) NOT NULL COMMENT '优惠值',
    `name`        varchar(128)   NOT NULL,
    `status`      tinyint DEFAULT '1' COMMENT '1可用 0禁用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_coupon_id` (`coupon_id`)
) ENGINE=InnoDB AUTO_INCREMENT=297 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='品类优惠券池';

-- ----------------------------
-- Table structure for favorite_info
-- ----------------------------
DROP TABLE IF EXISTS `favorite_info`;
CREATE TABLE `favorite_info`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `favorite_id` varchar(64) NOT NULL COMMENT '业务收藏ID',
    `user_id`     varchar(64) NOT NULL COMMENT '用户ID',
    `product_id`  varchar(64) NOT NULL COMMENT '商品ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_favorite_id` (`favorite_id`),
    KEY           `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收藏表';

-- ----------------------------
-- Table structure for group_buy_activity
-- ----------------------------
DROP TABLE IF EXISTS `group_buy_activity`;
CREATE TABLE `group_buy_activity`
(
    `id`              bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `activity_id`     varchar(64) NOT NULL COMMENT '活动ID（业务唯一标识，UUID）',
    `required_people` int         NOT NULL DEFAULT '2' COMMENT '拼团所需人数（最少2人）',
    `valid_duration`  int         NOT NULL DEFAULT '24' COMMENT '拼团有效时长（小时），从第一人开团开始计时',
    `status`          tinyint     NOT NULL DEFAULT '1' COMMENT '活动状态：1-进行中，0-已结束',
    `start_time`      datetime    NOT NULL COMMENT '活动开始时间',
    `end_time`        datetime    NOT NULL COMMENT '活动结束时间',
    `create_time`     datetime             DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_id` (`activity_id`),
    KEY               `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='全局拼团活动配置表（公共规则，不绑定商品）';

-- ----------------------------
-- Table structure for group_buy_product
-- ----------------------------
DROP TABLE IF EXISTS `group_buy_product`;
CREATE TABLE `group_buy_product`
(
    `id`               bigint         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `activity_id`      varchar(64)    NOT NULL COMMENT '关联活动ID',
    `product_id`       varchar(64)    NOT NULL COMMENT '关联商品ID',
    `group_buy_price`  decimal(10, 2) NOT NULL COMMENT '策略算法计算出的拼团优惠价',
    `original_price`   decimal(10, 2) NOT NULL COMMENT '商品原价快照',
    `discount_rate`    decimal(5, 4)  NOT NULL DEFAULT '0.8000' COMMENT '折扣率（0~1），算法计算结果',
    `interested_users` int            NOT NULL DEFAULT '0' COMMENT '对该商品感兴趣的用户数（算法统计）',
    `target_user_tags` varchar(500)            DEFAULT NULL COMMENT '推送目标用户标签（逗号分隔，如：男,18-25,26-35）',
    `behavior_score`   decimal(8, 4)           DEFAULT '0.0000' COMMENT '商品行为热度分（浏览+收藏+加购综合分）',
    `purchase_count`   int                     DEFAULT '0' COMMENT '近30天购买次数',
    `view_count`       int                     DEFAULT '0' COMMENT '近30天浏览次数',
    `favorite_count`   int                     DEFAULT '0' COMMENT '近30天收藏次数',
    `status`           tinyint        NOT NULL DEFAULT '0' COMMENT '上架状态：0-未上架，1-已上架（推送中），2-已下架',
    `online_time`      datetime                DEFAULT NULL COMMENT '上架时间',
    `offline_time`     datetime                DEFAULT NULL COMMENT '下架时间（超过活动结束时间自动下架）',
    `create_time`      datetime                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（策略分析时间）',
    `update_time`      datetime                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_product` (`activity_id`,`product_id`) COMMENT '同一活动同一商品只有一条策略记录',
    KEY                `idx_product_id` (`product_id`),
    KEY                `idx_activity_id` (`activity_id`),
    KEY                `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拼团商品策略表（策略算法分析结果与上下架管理）';

-- ----------------------------
-- Table structure for group_buy_team
-- ----------------------------
DROP TABLE IF EXISTS `group_buy_team`;
CREATE TABLE `group_buy_team`
(
    `id`              bigint         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `team_id`         varchar(64)    NOT NULL COMMENT '团队ID（业务唯一标识）',
    `activity_id`     varchar(64)    NOT NULL COMMENT '关联活动ID',
    `product_id`      varchar(64)    NOT NULL COMMENT '关联商品ID',
    `leader_user_id`  varchar(64)    NOT NULL COMMENT '团长用户ID（第一个发起人）',
    `required_people` int            NOT NULL COMMENT '拼团所需人数（从活动配置冗余）',
    `current_people`  int            NOT NULL DEFAULT '0' COMMENT '当前已参团人数',
    `status`          tinyint        NOT NULL DEFAULT '0' COMMENT '团队状态：0-拼团中，1-拼团成功，2-拼团失败（超时未满）',
    `group_buy_price` decimal(10, 2) NOT NULL COMMENT '本次拼团价格（快照）',
    `start_time`      datetime       NOT NULL COMMENT '开团时间（第一人参团时间）',
    `end_time`        datetime       NOT NULL COMMENT '拼团截止时间（start_time + valid_duration）',
    `create_time`     datetime                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     datetime                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_id` (`team_id`),
    KEY               `idx_activity_id` (`activity_id`),
    KEY               `idx_leader_user_id` (`leader_user_id`),
    KEY               `idx_status` (`status`),
    KEY               `idx_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拼团团队表';

-- ----------------------------
-- Table structure for group_buy_team_member
-- ----------------------------
DROP TABLE IF EXISTS `group_buy_team_member`;
CREATE TABLE `group_buy_team_member`
(
    `id`            bigint         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `member_id`     varchar(64)    NOT NULL COMMENT '成员记录ID（业务唯一标识）',
    `team_id`       varchar(64)    NOT NULL COMMENT '关联团队ID',
    `activity_id`   varchar(64)    NOT NULL COMMENT '关联活动ID',
    `product_id`    varchar(64)    NOT NULL COMMENT '关联商品ID',
    `user_id`       varchar(64)    NOT NULL COMMENT '用户ID',
    `order_id`      varchar(64)             DEFAULT NULL COMMENT '关联订单ID',
    `pay_price`     decimal(10, 2) NOT NULL COMMENT '实际支付价格（拼团价快照）',
    `is_leader`     tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为团长：1-是，0-否',
    `address`       varchar(255)            DEFAULT NULL COMMENT '收货地址快照',
    `contact_name`  varchar(64)             DEFAULT NULL COMMENT '联系人姓名快照',
    `contact_phone` varchar(20)             DEFAULT NULL COMMENT '联系电话快照',
    `status`        tinyint        NOT NULL DEFAULT '0' COMMENT '成员状态：0-拼团中，1-拼团成功，2-拼团失败退款',
    `join_time`     datetime       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '参团时间',
    `create_time`   datetime                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_id` (`member_id`),
    UNIQUE KEY `uk_team_user` (`team_id`,`user_id`) COMMENT '同一团队同一用户只能参团一次',
    KEY             `idx_team_id` (`team_id`),
    KEY             `idx_user_id` (`user_id`),
    KEY             `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拼团团队成员表';

-- ----------------------------
-- Table structure for lottery_coupon_strategy
-- ----------------------------
DROP TABLE IF EXISTS `lottery_coupon_strategy`;
CREATE TABLE `lottery_coupon_strategy`
(
    `id`                  bigint         NOT NULL AUTO_INCREMENT,
    `strategy_id`         varchar(64)    NOT NULL COMMENT '策略ID',
    `coupon_id`           varchar(64)    NOT NULL COMMENT '关联优惠券ID',
    `category`            varchar(64)    NOT NULL COMMENT '品类',
    `coupon_type`         varchar(20)    NOT NULL COMMENT 'DIRECT/FULL/DISCOUNT',
    `coupon_value`        decimal(10, 2) NOT NULL COMMENT '优惠值',
    `min_order_amount`    decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT 'FULL满减门槛',
    `avg_order_price`     decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '品类近30天平均客单价',
    `category_margin`     decimal(5, 4)  NOT NULL DEFAULT '0.2000' COMMENT '品类毛利率',
    `order_count_30d`     int            NOT NULL DEFAULT '0' COMMENT '近30天品类订单数',
    `user_count_30d`      int            NOT NULL DEFAULT '0' COMMENT '近30天品类活跃买家数',
    `repeat_buy_rate`     decimal(5, 4)  NOT NULL DEFAULT '0.0000' COMMENT '复购率',
    `elasticity_score`    decimal(6, 2)  NOT NULL DEFAULT '0.00' COMMENT '用户价格弹性分(0~100)',
    `conversion_lift`     decimal(5, 4)  NOT NULL DEFAULT '0.0000' COMMENT '预期转化提升率',
    `volume_lift`         decimal(5, 4)  NOT NULL DEFAULT '0.0000' COMMENT '预期销量提升率',
    `actual_discount`     decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '实际优惠金额',
    `net_profit_rate`     decimal(5, 4)  NOT NULL DEFAULT '0.0000' COMMENT '预期净利润率',
    `roi_score`           decimal(6, 2)  NOT NULL DEFAULT '0.00' COMMENT 'ROI评分(0~100)',
    `break_even_discount` decimal(10, 2)          DEFAULT NULL COMMENT '保本让利上限',
    `recommend_reason`    varchar(500)            DEFAULT NULL COMMENT '算法推荐理由',
    `status`              tinyint        NOT NULL DEFAULT '0' COMMENT '0-未上架,1-已上架,2-已下架',
    `is_fallback`         tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为保底优惠券',
    `create_time`         datetime                DEFAULT CURRENT_TIMESTAMP,
    `update_time`         datetime                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_strategy_id` (`strategy_id`),
    UNIQUE KEY `uk_coupon_id` (`coupon_id`),
    KEY                   `idx_category` (`category`),
    KEY                   `idx_status` (`status`),
    KEY                   `idx_is_fallback` (`is_fallback`)
) ENGINE=InnoDB AUTO_INCREMENT=111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='抽奖优惠券策略分析表';

-- ----------------------------
-- Table structure for order_info
-- ----------------------------
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info`
(
    `id`            bigint         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id`      varchar(64)    NOT NULL COMMENT '业务订单ID',
    `user_id`       varchar(64)    NOT NULL COMMENT '用户ID',
    `total_amount`  decimal(10, 2) NOT NULL COMMENT '订单总金额',
    `status`        tinyint      DEFAULT '0' COMMENT '状态: 0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消',
    `address`       varchar(255) DEFAULT NULL COMMENT '收货地址快照',
    `contact_name`  varchar(64)  DEFAULT NULL COMMENT '联系人姓名',
    `contact_phone` varchar(20)  DEFAULT NULL COMMENT '联系电话',
    `coupon_id`     varchar(64)  DEFAULT NULL,
    `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY             `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单信息表';

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`
(
    `id`            bigint         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `item_id`       varchar(64)    NOT NULL COMMENT '业务明细ID',
    `order_id`      varchar(64)    NOT NULL COMMENT '订单ID',
    `product_id`    varchar(64)    NOT NULL COMMENT '商品ID',
    `product_type`  varchar(20)    NOT NULL DEFAULT 'NORMAL' COMMENT '商品类型：NORMAL-普通商品、GROUP_BUY-拼团商品、AWARD-抽奖商品',
    `product_name`  varchar(128)   NOT NULL COMMENT '商品名称快照',
    `product_image` varchar(255)            DEFAULT NULL COMMENT '商品图片快照',
    `current_price` decimal(10, 2) NOT NULL COMMENT '购买时价格',
    `quantity`      int            NOT NULL COMMENT '购买数量',
    `total_price`   decimal(10, 2) NOT NULL COMMENT '该项总价',
    `create_time`   datetime                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_item_id` (`item_id`),
    KEY             `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=81 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';

-- ----------------------------
-- Table structure for product_info
-- ----------------------------
DROP TABLE IF EXISTS `product_info`;
CREATE TABLE `product_info`
(
    `id`          bigint         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_id`  varchar(64)    NOT NULL COMMENT '业务商品ID',
    `name`        varchar(128)   NOT NULL COMMENT '商品名称',
    `description` text COMMENT '商品描述',
    `price`       decimal(10, 2) NOT NULL COMMENT '价格',
    `stock`       int            NOT NULL DEFAULT '0' COMMENT '库存数量',
    `image_url`   varchar(255)            DEFAULT NULL COMMENT '商品图片URL',
    `category`    varchar(64)             DEFAULT NULL COMMENT '商品品类',
    `status`      tinyint                 DEFAULT '1' COMMENT '状态: 1-上架, 0-下架',
    `create_time` datetime                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `brand`       varchar(64)             DEFAULT '' COMMENT '品牌',
    `keywords`    varchar(255)            DEFAULT '' COMMENT '关键词',
    `user_tags`   varchar(255)   NOT NULL DEFAULT '' COMMENT '用户适配标签，英文逗号分隔（示例：男,18-25,26-35/女,18-35/通用,18-45）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1261 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品信息表';

-- ----------------------------
-- Table structure for shipping_address
-- ----------------------------
DROP TABLE IF EXISTS `shipping_address`;
CREATE TABLE `shipping_address`
(
    `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`        varchar(64)  NOT NULL COMMENT '用户ID',
    `receiver_name`  varchar(64)  NOT NULL COMMENT '收货人姓名',
    `receiver_phone` varchar(20)  NOT NULL COMMENT '收货人电话',
    `province`       varchar(64)  NOT NULL COMMENT '省份',
    `city`           varchar(64)  NOT NULL COMMENT '城市',
    `detail_address` varchar(255) NOT NULL COMMENT '详细地址',
    `is_default`     tinyint(1) DEFAULT '0' COMMENT '是否默认地址',
    `create_time`    datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY              `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收货地址表';

-- ----------------------------
-- Table structure for sys_admin
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `admin_id`    varchar(64)  NOT NULL COMMENT '业务管理员ID',
    `username`    varchar(64)  NOT NULL COMMENT '后台登录账号',
    `password`    varchar(128) NOT NULL COMMENT '加密后的密码',
    `status`      tinyint      NOT NULL DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
    `create_time` datetime              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_id` (`admin_id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='后台管理员表';

-- ----------------------------
-- Table structure for user_behavior
-- ----------------------------
DROP TABLE IF EXISTS `user_behavior`;
CREATE TABLE `user_behavior`
(
    `id`            bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       varchar(64) NOT NULL COMMENT '用户ID',
    `product_id`    varchar(64) NOT NULL COMMENT '商品ID',
    `behavior_type` varchar(16) NOT NULL COMMENT '行为类型: CLICK, COLLECT, BUY',
    `create_time`   datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
    PRIMARY KEY (`id`),
    KEY             `idx_user_product` (`user_id`,`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=324 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户行为流水表';

-- ----------------------------
-- Table structure for user_coupon
-- ----------------------------
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT,
    `user_id`     varchar(64) NOT NULL,
    `coupon_id`   varchar(64) NOT NULL,
    `category`    varchar(64) NOT NULL,
    `status`      tinyint  DEFAULT '0' COMMENT '0未使用 1已使用',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY           `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户优惠券';

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     varchar(64) NOT NULL COMMENT '业务用户ID',
    `username`    varchar(64)  DEFAULT NULL COMMENT '用户名',
    `password`    varchar(128) DEFAULT NULL COMMENT '密码',
    `phone`       varchar(20)  DEFAULT NULL COMMENT '手机号',
    `create_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `age`         int          DEFAULT '0' COMMENT '年龄',
    `gender`      tinyint      DEFAULT '0' COMMENT '性别: 0-女, 1-男',
    `city`        varchar(64)  DEFAULT '' COMMENT '城市',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息表';

-- ----------------------------
-- Table structure for user_lottery
-- ----------------------------
DROP TABLE IF EXISTS `user_lottery`;
CREATE TABLE `user_lottery`
(
    `id`             bigint      NOT NULL AUTO_INCREMENT,
    `user_id`        varchar(64) NOT NULL,
    `lottery_count`  int         NOT NULL DEFAULT '0' COMMENT '可用抽奖次数',
    `last_sign_date` date                 DEFAULT NULL COMMENT '最后签到日期',
    `create_time`    datetime             DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户抽奖次数&签到';

SET
FOREIGN_KEY_CHECKS = 1;

-- 32品类优惠券（每类3张：立减/满减/折扣）
INSERT INTO `coupon` (`coupon_id`, `category`, `coupon_type`, `value`, `name`, `status`)
VALUES ('c-3c-01', '3C数码', 'DIRECT', 15.00, '3C数码立减15元券', 1),
       ('c-3c-02', '3C数码', 'FULL', 50.00, '3C数码满300减50券', 1),
       ('c-3c-03', '3C数码', 'DISCOUNT', 0.92, '3C数码92折券', 1),
       ('c-huwai-01', '户外装备', 'DIRECT', 10.00, '户外装备立减10元券', 1),
       ('c-huwai-02', '户外装备', 'FULL', 30.00, '户外装备满200减30券', 1),
       ('c-huwai-03', '户外装备', 'DISCOUNT', 0.90, '户外装备九折券', 1),
       ('c-car-01', '汽车用品', 'DIRECT', 12.00, '汽车用品立减12元券', 1),
       ('c-car-02', '汽车用品', 'FULL', 40.00, '汽车用品满250减40券', 1),
       ('c-car-03', '汽车用品', 'DISCOUNT', 0.88, '汽车用品88折券', 1),
       ('c-biz-01', '商务服饰', 'DIRECT', 20.00, '商务服饰立减20元券', 1),
       ('c-biz-02', '商务服饰', 'FULL', 60.00, '商务服饰满400减60券', 1),
       ('c-biz-03', '商务服饰', 'DISCOUNT', 0.85, '商务服饰85折券', 1),
       ('c-fish-01', '垂钓装备', 'DIRECT', 8.00, '垂钓装备立减8元券', 1),
       ('c-fish-02', '垂钓装备', 'FULL', 25.00, '垂钓装备满150减25券', 1),
       ('c-fish-03', '垂钓装备', 'DISCOUNT', 0.90, '垂钓装备九折券', 1),
       ('c-mach-01', '机械器材', 'DIRECT', 18.00, '机械器材立减18元券', 1),
       ('c-mach-02', '机械器材', 'FULL', 50.00, '机械器材满300减50券', 1),
       ('c-mach-03', '机械器材', 'DISCOUNT', 0.88, '机械器材88折券', 1),
       ('c-watch-01', '男士腕表', 'DIRECT', 30.00, '男士腕表立减30元券', 1),
       ('c-watch-02', '男士腕表', 'FULL', 100.00, '男士腕表满600减100券', 1),
       ('c-watch-03', '男士腕表', 'DISCOUNT', 0.88, '男士腕表88折券', 1),
       ('c-sport-01', '运动护具', 'DIRECT', 8.00, '运动护具立减8元券', 1),
       ('c-sport-02', '运动护具', 'FULL', 20.00, '运动护具满120减20券', 1),
       ('c-sport-03', '运动护具', 'DISCOUNT', 0.90, '运动护具九折券', 1),
       ('c-wine-01', '酒具酒品', 'DIRECT', 15.00, '酒具酒品立减15元券', 1),
       ('c-wine-02', '酒具酒品', 'FULL', 40.00, '酒具酒品满200减40券', 1),
       ('c-wine-03', '酒具酒品', 'DISCOUNT', 0.88, '酒具酒品88折券', 1),
       ('c-bike-01', '骑行装备', 'DIRECT', 10.00, '骑行装备立减10元券', 1),
       ('c-bike-02', '骑行装备', 'FULL', 30.00, '骑行装备满180减30券', 1),
       ('c-bike-03', '骑行装备', 'DISCOUNT', 0.90, '骑行装备九折券', 1),
       ('c-tool-01', '五金工具', 'DIRECT', 6.00, '五金工具立减6元券', 1),
       ('c-tool-02', '五金工具', 'FULL', 20.00, '五金工具满100减20券', 1),
       ('c-tool-03', '五金工具', 'DISCOUNT', 0.90, '五金工具九折券', 1),
       ('c-game-01', '电竞外设', 'DIRECT', 15.00, '电竞外设立减15元券', 1),
       ('c-game-02', '电竞外设', 'FULL', 50.00, '电竞外设满300减50券', 1),
       ('c-game-03', '电竞外设', 'DISCOUNT', 0.90, '电竞外设九折券', 1),
       ('c-beauty-01', '美妆护肤', 'DIRECT', 20.00, '美妆护肤立减20元券', 1),
       ('c-beauty-02', '美妆护肤', 'FULL', 50.00, '美妆护肤满200减50券', 1),
       ('c-beauty-03', '美妆护肤', 'DISCOUNT', 0.85, '美妆护肤85折券', 1),
       ('c-jewelry-01', '轻奢首饰', 'DIRECT', 25.00, '轻奢首饰立减25元券', 1),
       ('c-jewelry-02', '轻奢首饰', 'FULL', 80.00, '轻奢首饰满400减80券', 1),
       ('c-jewelry-03', '轻奢首饰', 'DISCOUNT', 0.88, '轻奢首饰88折券', 1),
       ('c-baby-01', '母婴用品', 'DIRECT', 10.00, '母婴用品立减10元券', 1),
       ('c-baby-02', '母婴用品', 'FULL', 30.00, '母婴用品满150减30券', 1),
       ('c-baby-03', '母婴用品', 'DISCOUNT', 0.90, '母婴用品九折券', 1),
       ('c-perf-01', '香氛香水', 'DIRECT', 20.00, '香氛香水立减20元券', 1),
       ('c-perf-02', '香氛香水', 'FULL', 60.00, '香氛香水满300减60券', 1),
       ('c-perf-03', '香氛香水', 'DISCOUNT', 0.85, '香氛香水85折券', 1),
       ('c-textile-01', '布艺家纺', 'DIRECT', 12.00, '布艺家纺立减12元券', 1),
       ('c-textile-02', '布艺家纺', 'FULL', 30.00, '布艺家纺满150减30券', 1),
       ('c-textile-03', '布艺家纺', 'DISCOUNT', 0.90, '布艺家纺九折券', 1),
       ('c-nail-01', '美甲彩妆', 'DIRECT', 8.00, '美甲彩妆立减8元券', 1),
       ('c-nail-02', '美甲彩妆', 'FULL', 20.00, '美甲彩妆满100减20券', 1),
       ('c-nail-03', '美甲彩妆', 'DISCOUNT', 0.88, '美甲彩妆88折券', 1),
       ('c-acc-01', '网红配饰', 'DIRECT', 8.00, '网红配饰立减8元券', 1),
       ('c-acc-02', '网红配饰', 'FULL', 20.00, '网红配饰满100减20券', 1),
       ('c-acc-03', '网红配饰', 'DISCOUNT', 0.88, '网红配饰88折券', 1),
       ('c-food-01', '代餐轻食', 'DIRECT', 5.00, '代餐轻食立减5元券', 1),
       ('c-food-02', '代餐轻食', 'FULL', 15.00, '代餐轻食满80减15券', 1),
       ('c-food-03', '代餐轻食', 'DISCOUNT', 0.90, '代餐轻食九折券', 1),
       ('c-cosm-01', '化妆工具', 'DIRECT', 8.00, '化妆工具立减8元券', 1),
       ('c-cosm-02', '化妆工具', 'FULL', 20.00, '化妆工具满100减20券', 1),
       ('c-cosm-03', '化妆工具', 'DISCOUNT', 0.90, '化妆工具九折券', 1),
       ('c-flower-01', '家居花艺', 'DIRECT', 6.00, '家居花艺立减6元券', 1),
       ('c-flower-02', '家居花艺', 'FULL', 15.00, '家居花艺满80减15券', 1),
       ('c-flower-03', '家居花艺', 'DISCOUNT', 0.90, '家居花艺九折券', 1),
       ('c-lingerie-01', '内衣睡衣', 'DIRECT', 10.00, '内衣睡衣立减10元券', 1),
       ('c-lingerie-02', '内衣睡衣', 'FULL', 25.00, '内衣睡衣满120减25券', 1),
       ('c-lingerie-03', '内衣睡衣', 'DISCOUNT', 0.88, '内衣睡衣88折券', 1),
       ('c-teaware-01', '餐具茶具', 'DIRECT', 8.00, '餐具茶具立减8元券', 1),
       ('c-teaware-02', '餐具茶具', 'FULL', 20.00, '餐具茶具满100减20券', 1),
       ('c-teaware-03', '餐具茶具', 'DISCOUNT', 0.90, '餐具茶具九折券', 1),
       ('c-grain-01', '粮油米面', 'DIRECT', 3.00, '粮油米面立减3元券', 1),
       ('c-grain-02', '粮油米面', 'FULL', 10.00, '粮油米面满60减10券', 1),
       ('c-grain-03', '粮油米面', 'DISCOUNT', 0.95, '粮油米面95折券', 1),
       ('c-clean-01', '洗护清洁', 'DIRECT', 5.00, '洗护清洁立减5元券', 1),
       ('c-clean-02', '洗护清洁', 'FULL', 15.00, '洗护清洁满80减15券', 1),
       ('c-clean-03', '洗护清洁', 'DISCOUNT', 0.92, '洗护清洁92折券', 1),
       ('c-appliance-01', '家用电器', 'DIRECT', 20.00, '家用电器立减20元券', 1),
       ('c-appliance-02', '家用电器', 'FULL', 60.00, '家用电器满400减60券', 1),
       ('c-appliance-03', '家用电器', 'DISCOUNT', 0.90, '家用电器九折券', 1),
       ('c-office-01', '办公设备', 'DIRECT', 15.00, '办公设备立减15元券', 1),
       ('c-office-02', '办公设备', 'FULL', 50.00, '办公设备满300减50券', 1),
       ('c-office-03', '办公设备', 'DISCOUNT', 0.90, '办公设备九折券', 1),
       ('c-cookware-01', '厨房用具', 'DIRECT', 8.00, '厨房用具立减8元券', 1),
       ('c-cookware-02', '厨房用具', 'FULL', 20.00, '厨房用具满100减20券', 1),
       ('c-cookware-03', '厨房用具', 'DISCOUNT', 0.90, '厨房用具九折券', 1),
       ('c-drink-01', '饮用水饮', 'DIRECT', 3.00, '饮用水饮立减3元券', 1),
       ('c-drink-02', '饮用水饮', 'FULL', 10.00, '饮用水饮满50减10券', 1),
       ('c-drink-03', '饮用水饮', 'DISCOUNT', 0.92, '饮用水饮92折券', 1),
       ('c-storage-01', '收纳整理', 'DIRECT', 6.00, '收纳整理立减6元券', 1),
       ('c-storage-02', '收纳整理', 'FULL', 15.00, '收纳整理满80减15券', 1),
       ('c-storage-03', '收纳整理', 'DISCOUNT', 0.90, '收纳整理九折券', 1),
       ('c-daily-01', '日用百货', 'DIRECT', 5.00, '日用百货立减5元券', 1),
       ('c-daily-02', '日用百货', 'FULL', 15.00, '日用百货满80减15券', 1),
       ('c-daily-03', '日用百货', 'DISCOUNT', 0.92, '日用百货92折券', 1),
-- 6条保底通用优惠券
       ('c-fallback-01', '通用', 'FULL', 1.00, '满10减1通用券', 1),
       ('c-fallback-02', '通用', 'DIRECT', 2.00, '无门槛2元通用券', 1),
       ('c-fallback-03', '通用', 'FULL', 3.00, '满20减3通用券', 1),
       ('c-fallback-04', '通用', 'DIRECT', 1.00, '无门槛1元通用券', 1),
       ('c-fallback-05', '通用', 'DISCOUNT', 0.99, '通用99折券', 1),
       ('c-fallback-06', '通用', 'FULL', 5.00, '满30减5通用券', 1);

-- 保底优惠券策略记录（is_fallback=1，自动展示无需商家上架）
INSERT INTO `lottery_coupon_strategy`
(`strategy_id`, `coupon_id`, `category`, `coupon_type`, `coupon_value`, `min_order_amount`,
 `avg_order_price`, `category_margin`, `order_count_30d`, `user_count_30d`, `repeat_buy_rate`,
 `elasticity_score`, `conversion_lift`, `volume_lift`, `actual_discount`, `net_profit_rate`,
 `roi_score`, `recommend_reason`, `status`, `is_fallback`)
VALUES ('s-fb-01', 'c-fallback-01', '通用', 'FULL', 1.00, 10.00, 50.00, 0.2000, 0, 0, 0.0000, 50.00, 0.0500, 0.0500,
        1.00, 0.1900, 60.00, '保底兜底券，确保每位用户都有优惠券可抽', 1, 1),
       ('s-fb-02', 'c-fallback-02', '通用', 'DIRECT', 2.00, 0.00, 50.00, 0.2000, 0, 0, 0.0000, 50.00, 0.0500, 0.0500,
        2.00, 0.1900, 60.00, '保底兜底券，确保每位用户都有优惠券可抽', 1, 1),
       ('s-fb-03', 'c-fallback-03', '通用', 'FULL', 3.00, 20.00, 50.00, 0.2000, 0, 0, 0.0000, 50.00, 0.0500, 0.0500,
        3.00, 0.1900, 60.00, '保底兜底券，确保每位用户都有优惠券可抽', 1, 1),
       ('s-fb-04', 'c-fallback-04', '通用', 'DIRECT', 1.00, 0.00, 50.00, 0.2000, 0, 0, 0.0000, 50.00, 0.0500, 0.0500,
        1.00, 0.1900, 60.00, '保底兜底券，确保每位用户都有优惠券可抽', 1, 1),
       ('s-fb-05', 'c-fallback-05', '通用', 'DISCOUNT', 0.99, 0.00, 50.00, 0.2000, 0, 0, 0.0000, 50.00, 0.0500, 0.0500,
        0.50, 0.1900, 60.00, '保底兜底券，确保每位用户都有优惠券可抽', 1, 1),
       ('s-fb-06', 'c-fallback-06', '通用', 'FULL', 5.00, 30.00, 50.00, 0.2000, 0, 0, 0.0000, 50.00, 0.0500, 0.0500,
        5.00, 0.1900, 60.00, '保底兜底券，确保每位用户都有优惠券可抽', 1, 1);

SET
FOREIGN_KEY_CHECKS = 1;

-- 32品类优惠券策略初始记录（status=0未上架，is_fallback=0，等待商家分析后上架）
INSERT INTO `lottery_coupon_strategy`
(`strategy_id`, `coupon_id`, `category`, `coupon_type`, `coupon_value`, `min_order_amount`,
 `avg_order_price`, `category_margin`, `order_count_30d`, `user_count_30d`, `repeat_buy_rate`,
 `elasticity_score`, `conversion_lift`, `volume_lift`, `actual_discount`, `net_profit_rate`,
 `roi_score`, `recommend_reason`, `status`, `is_fallback`)
VALUES ('s-3c-01', 'c-3c-01', '3C数码', 'DIRECT', 15.00, 0.00, 800.00, 0.1500, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        15.00, 0.1500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-3c-02', 'c-3c-02', '3C数码', 'FULL', 50.00, 300.00, 800.00, 0.1500, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        50.00, 0.1500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-3c-03', 'c-3c-03', '3C数码', 'DISCOUNT', 0.92, 0.00, 800.00, 0.1500, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        64.00, 0.1500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-huwai-01', 'c-huwai-01', '户外装备', 'DIRECT', 10.00, 0.00, 300.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 10.00, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-huwai-02', 'c-huwai-02', '户外装备', 'FULL', 30.00, 200.00, 300.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 30.00, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-huwai-03', 'c-huwai-03', '户外装备', 'DISCOUNT', 0.90, 0.00, 300.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 30.00, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-car-01', 'c-car-01', '汽车用品', 'DIRECT', 12.00, 0.00, 250.00, 0.3500, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        12.00, 0.3500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-car-02', 'c-car-02', '汽车用品', 'FULL', 40.00, 250.00, 250.00, 0.3500, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        40.00, 0.3500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-car-03', 'c-car-03', '汽车用品', 'DISCOUNT', 0.88, 0.00, 250.00, 0.3500, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        30.00, 0.3500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-biz-01', 'c-biz-01', '商务服饰', 'DIRECT', 20.00, 0.00, 500.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        20.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-biz-02', 'c-biz-02', '商务服饰', 'FULL', 60.00, 400.00, 500.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        60.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-biz-03', 'c-biz-03', '商务服饰', 'DISCOUNT', 0.85, 0.00, 500.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        75.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-fish-01', 'c-fish-01', '垂钓装备', 'DIRECT', 8.00, 0.00, 200.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        8.00, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-fish-02', 'c-fish-02', '垂钓装备', 'FULL', 25.00, 150.00, 200.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        25.00, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-fish-03', 'c-fish-03', '垂钓装备', 'DISCOUNT', 0.90, 0.00, 200.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 20.00, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-mach-01', 'c-mach-01', '机械器材', 'DIRECT', 18.00, 0.00, 400.00, 0.2000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        18.00, 0.2000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-mach-02', 'c-mach-02', '机械器材', 'FULL', 50.00, 300.00, 400.00, 0.2000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        50.00, 0.2000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-mach-03', 'c-mach-03', '机械器材', 'DISCOUNT', 0.88, 0.00, 400.00, 0.2000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 48.00, 0.2000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-watch-01', 'c-watch-01', '男士腕表', 'DIRECT', 30.00, 0.00, 1200.00, 0.3500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 30.00, 0.3500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-watch-02', 'c-watch-02', '男士腕表', 'FULL', 100.00, 600.00, 1200.00, 0.3500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 100.00, 0.3500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-watch-03', 'c-watch-03', '男士腕表', 'DISCOUNT', 0.88, 0.00, 1200.00, 0.3500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 144.00, 0.3500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-sport-01', 'c-sport-01', '运动护具', 'DIRECT', 8.00, 0.00, 150.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 8.00, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-sport-02', 'c-sport-02', '运动护具', 'FULL', 20.00, 120.00, 150.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 20.00, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-sport-03', 'c-sport-03', '运动护具', 'DISCOUNT', 0.90, 0.00, 150.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 15.00, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0);
INSERT INTO `lottery_coupon_strategy`
(`strategy_id`, `coupon_id`, `category`, `coupon_type`, `coupon_value`, `min_order_amount`,
 `avg_order_price`, `category_margin`, `order_count_30d`, `user_count_30d`, `repeat_buy_rate`,
 `elasticity_score`, `conversion_lift`, `volume_lift`, `actual_discount`, `net_profit_rate`,
 `roi_score`, `recommend_reason`, `status`, `is_fallback`)
VALUES ('s-wine-01', 'c-wine-01', '酒具酒品', 'DIRECT', 15.00, 0.00, 300.00, 0.3500, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        15.00, 0.3500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-wine-02', 'c-wine-02', '酒具酒品', 'FULL', 40.00, 200.00, 300.00, 0.3500, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        40.00, 0.3500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-wine-03', 'c-wine-03', '酒具酒品', 'DISCOUNT', 0.88, 0.00, 300.00, 0.3500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 36.00, 0.3500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-bike-01', 'c-bike-01', '骑行装备', 'DIRECT', 10.00, 0.00, 250.00, 0.2800, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        10.00, 0.2800, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-bike-02', 'c-bike-02', '骑行装备', 'FULL', 30.00, 180.00, 250.00, 0.2800, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        30.00, 0.2800, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-bike-03', 'c-bike-03', '骑行装备', 'DISCOUNT', 0.90, 0.00, 250.00, 0.2800, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 25.00, 0.2800, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-tool-01', 'c-tool-01', '五金工具', 'DIRECT', 6.00, 0.00, 120.00, 0.2500, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        6.00, 0.2500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-tool-02', 'c-tool-02', '五金工具', 'FULL', 20.00, 100.00, 120.00, 0.2500, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        20.00, 0.2500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-tool-03', 'c-tool-03', '五金工具', 'DISCOUNT', 0.90, 0.00, 120.00, 0.2500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 12.00, 0.2500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-game-01', 'c-game-01', '电竞外设', 'DIRECT', 15.00, 0.00, 400.00, 0.2000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        15.00, 0.2000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-game-02', 'c-game-02', '电竞外设', 'FULL', 50.00, 300.00, 400.00, 0.2000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        50.00, 0.2000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-game-03', 'c-game-03', '电竞外设', 'DISCOUNT', 0.90, 0.00, 400.00, 0.2000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 40.00, 0.2000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-beauty-01', 'c-beauty-01', '美妆护肤', 'DIRECT', 20.00, 0.00, 200.00, 0.4500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 20.00, 0.4500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-beauty-02', 'c-beauty-02', '美妆护肤', 'FULL', 50.00, 200.00, 200.00, 0.4500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 50.00, 0.4500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-beauty-03', 'c-beauty-03', '美妆护肤', 'DISCOUNT', 0.85, 0.00, 200.00, 0.4500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 30.00, 0.4500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-jewelry-01', 'c-jewelry-01', '轻奢首饰', 'DIRECT', 25.00, 0.00, 500.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 25.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-jewelry-02', 'c-jewelry-02', '轻奢首饰', 'FULL', 80.00, 400.00, 500.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 80.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-jewelry-03', 'c-jewelry-03', '轻奢首饰', 'DISCOUNT', 0.88, 0.00, 500.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 60.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-baby-01', 'c-baby-01', '母婴用品', 'DIRECT', 10.00, 0.00, 180.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        10.00, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-baby-02', 'c-baby-02', '母婴用品', 'FULL', 30.00, 150.00, 180.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        30.00, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-baby-03', 'c-baby-03', '母婴用品', 'DISCOUNT', 0.90, 0.00, 180.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 18.00, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-perf-01', 'c-perf-01', '香氛香水', 'DIRECT', 20.00, 0.00, 350.00, 0.5000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        20.00, 0.5000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-perf-02', 'c-perf-02', '香氛香水', 'FULL', 60.00, 300.00, 350.00, 0.5000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        60.00, 0.5000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-perf-03', 'c-perf-03', '香氛香水', 'DISCOUNT', 0.85, 0.00, 350.00, 0.5000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 52.50, 0.5000, 0.00, '待商家分析后决定是否上架', 0, 0);
INSERT INTO `lottery_coupon_strategy`
(`strategy_id`, `coupon_id`, `category`, `coupon_type`, `coupon_value`, `min_order_amount`,
 `avg_order_price`, `category_margin`, `order_count_30d`, `user_count_30d`, `repeat_buy_rate`,
 `elasticity_score`, `conversion_lift`, `volume_lift`, `actual_discount`, `net_profit_rate`,
 `roi_score`, `recommend_reason`, `status`, `is_fallback`)
VALUES ('s-textile-01', 'c-textile-01', '布艺家纺', 'DIRECT', 12.00, 0.00, 200.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 12.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-textile-02', 'c-textile-02', '布艺家纺', 'FULL', 30.00, 150.00, 200.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 30.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-textile-03', 'c-textile-03', '布艺家纺', 'DISCOUNT', 0.90, 0.00, 200.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 20.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-nail-01', 'c-nail-01', '美甲彩妆', 'DIRECT', 8.00, 0.00, 120.00, 0.4500, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        8.00, 0.4500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-nail-02', 'c-nail-02', '美甲彩妆', 'FULL', 20.00, 100.00, 120.00, 0.4500, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        20.00, 0.4500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-nail-03', 'c-nail-03', '美甲彩妆', 'DISCOUNT', 0.88, 0.00, 120.00, 0.4500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 14.40, 0.4500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-acc-01', 'c-acc-01', '网红配饰', 'DIRECT', 8.00, 0.00, 100.00, 0.5000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        8.00, 0.5000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-acc-02', 'c-acc-02', '网红配饰', 'FULL', 20.00, 100.00, 100.00, 0.5000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        20.00, 0.5000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-acc-03', 'c-acc-03', '网红配饰', 'DISCOUNT', 0.88, 0.00, 100.00, 0.5000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        12.00, 0.5000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-food-01', 'c-food-01', '代餐轻食', 'DIRECT', 5.00, 0.00, 80.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        5.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-food-02', 'c-food-02', '代餐轻食', 'FULL', 15.00, 80.00, 80.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        15.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-food-03', 'c-food-03', '代餐轻食', 'DISCOUNT', 0.90, 0.00, 80.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        8.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-cosm-01', 'c-cosm-01', '化妆工具', 'DIRECT', 8.00, 0.00, 120.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        8.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-cosm-02', 'c-cosm-02', '化妆工具', 'FULL', 20.00, 100.00, 120.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        20.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-cosm-03', 'c-cosm-03', '化妆工具', 'DISCOUNT', 0.90, 0.00, 120.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 12.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-flower-01', 'c-flower-01', '家居花艺', 'DIRECT', 6.00, 0.00, 100.00, 0.4500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 6.00, 0.4500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-flower-02', 'c-flower-02', '家居花艺', 'FULL', 15.00, 80.00, 100.00, 0.4500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 15.00, 0.4500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-flower-03', 'c-flower-03', '家居花艺', 'DISCOUNT', 0.90, 0.00, 100.00, 0.4500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 10.00, 0.4500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-lingerie-01', 'c-lingerie-01', '内衣睡衣', 'DIRECT', 10.00, 0.00, 150.00, 0.4500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 10.00, 0.4500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-lingerie-02', 'c-lingerie-02', '内衣睡衣', 'FULL', 25.00, 120.00, 150.00, 0.4500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 25.00, 0.4500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-lingerie-03', 'c-lingerie-03', '内衣睡衣', 'DISCOUNT', 0.88, 0.00, 150.00, 0.4500, 0, 0, 0.0000, 0.00,
        0.0000, 0.0000, 18.00, 0.4500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-teaware-01', 'c-teaware-01', '餐具茶具', 'DIRECT', 8.00, 0.00, 130.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 8.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-teaware-02', 'c-teaware-02', '餐具茶具', 'FULL', 20.00, 100.00, 130.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 20.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-teaware-03', 'c-teaware-03', '餐具茶具', 'DISCOUNT', 0.90, 0.00, 130.00, 0.4000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 13.00, 0.4000, 0.00, '待商家分析后决定是否上架', 0, 0);
INSERT INTO `lottery_coupon_strategy`
(`strategy_id`, `coupon_id`, `category`, `coupon_type`, `coupon_value`, `min_order_amount`,
 `avg_order_price`, `category_margin`, `order_count_30d`, `user_count_30d`, `repeat_buy_rate`,
 `elasticity_score`, `conversion_lift`, `volume_lift`, `actual_discount`, `net_profit_rate`,
 `roi_score`, `recommend_reason`, `status`, `is_fallback`)
VALUES ('s-grain-01', 'c-grain-01', '粮油米面', 'DIRECT', 3.00, 0.00, 60.00, 0.1500, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        3.00, 0.1500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-grain-02', 'c-grain-02', '粮油米面', 'FULL', 10.00, 60.00, 60.00, 0.1500, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        10.00, 0.1500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-grain-03', 'c-grain-03', '粮油米面', 'DISCOUNT', 0.95, 0.00, 60.00, 0.1500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 3.00, 0.1500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-clean-01', 'c-clean-01', '洗护清洁', 'DIRECT', 5.00, 0.00, 90.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        5.00, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-clean-02', 'c-clean-02', '洗护清洁', 'FULL', 15.00, 80.00, 90.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        15.00, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-clean-03', 'c-clean-03', '洗护清洁', 'DISCOUNT', 0.92, 0.00, 90.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 7.20, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-appliance-01', 'c-appliance-01', '家用电器', 'DIRECT', 20.00, 0.00, 600.00, 0.1800, 0, 0, 0.0000, 0.00,
        0.0000, 0.0000, 20.00, 0.1800, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-appliance-02', 'c-appliance-02', '家用电器', 'FULL', 60.00, 400.00, 600.00, 0.1800, 0, 0, 0.0000, 0.00,
        0.0000, 0.0000, 60.00, 0.1800, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-appliance-03', 'c-appliance-03', '家用电器', 'DISCOUNT', 0.90, 0.00, 600.00, 0.1800, 0, 0, 0.0000, 0.00,
        0.0000, 0.0000, 60.00, 0.1800, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-office-01', 'c-office-01', '办公设备', 'DIRECT', 15.00, 0.00, 400.00, 0.2000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 15.00, 0.2000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-office-02', 'c-office-02', '办公设备', 'FULL', 50.00, 300.00, 400.00, 0.2000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 50.00, 0.2000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-office-03', 'c-office-03', '办公设备', 'DISCOUNT', 0.90, 0.00, 400.00, 0.2000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 40.00, 0.2000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-cookware-01', 'c-cookware-01', '厨房用具', 'DIRECT', 8.00, 0.00, 120.00, 0.3500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 8.00, 0.3500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-cookware-02', 'c-cookware-02', '厨房用具', 'FULL', 20.00, 100.00, 120.00, 0.3500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 20.00, 0.3500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-cookware-03', 'c-cookware-03', '厨房用具', 'DISCOUNT', 0.90, 0.00, 120.00, 0.3500, 0, 0, 0.0000, 0.00,
        0.0000, 0.0000, 12.00, 0.3500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-drink-01', 'c-drink-01', '饮用水饮', 'DIRECT', 3.00, 0.00, 50.00, 0.2500, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        3.00, 0.2500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-drink-02', 'c-drink-02', '饮用水饮', 'FULL', 10.00, 50.00, 50.00, 0.2500, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        10.00, 0.2500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-drink-03', 'c-drink-03', '饮用水饮', 'DISCOUNT', 0.92, 0.00, 50.00, 0.2500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 4.00, 0.2500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-storage-01', 'c-storage-01', '收纳整理', 'DIRECT', 6.00, 0.00, 100.00, 0.3500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 6.00, 0.3500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-storage-02', 'c-storage-02', '收纳整理', 'FULL', 15.00, 80.00, 100.00, 0.3500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 15.00, 0.3500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-storage-03', 'c-storage-03', '收纳整理', 'DISCOUNT', 0.90, 0.00, 100.00, 0.3500, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 10.00, 0.3500, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-daily-01', 'c-daily-01', '日用百货', 'DIRECT', 5.00, 0.00, 80.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        5.00, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-daily-02', 'c-daily-02', '日用百货', 'FULL', 15.00, 80.00, 80.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000, 0.0000,
        15.00, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0),
       ('s-daily-03', 'c-daily-03', '日用百货', 'DISCOUNT', 0.92, 0.00, 80.00, 0.3000, 0, 0, 0.0000, 0.00, 0.0000,
        0.0000, 6.40, 0.3000, 0.00, '待商家分析后决定是否上架', 0, 0);
