/*
 拼团模块数据库表
 包含：拼团活动表、拼团团队表、拼团团队成员表
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 拼团活动表（配置每个商品的拼团规则：拼团人数、拼团价、有效时长）
-- ----------------------------
DROP TABLE IF EXISTS `group_buy_activity`;
CREATE TABLE `group_buy_activity` (
  `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `activity_id`      varchar(64)  NOT NULL COMMENT '活动ID（业务唯一标识）',
  `product_id`       varchar(64)  NOT NULL COMMENT '关联商品ID',
  `activity_name`    varchar(128) NOT NULL COMMENT '活动名称',
  `group_buy_price`  decimal(10,2) NOT NULL COMMENT '拼团价格',
  `original_price`   decimal(10,2) NOT NULL COMMENT '原价（冗余，快照）',
  `required_people`  int          NOT NULL DEFAULT 2 COMMENT '拼团所需人数（最少2人）',
  `valid_duration`   int          NOT NULL DEFAULT 24 COMMENT '拼团有效时长（小时），从第一人参团开始计时',
  `status`           tinyint      NOT NULL DEFAULT 1 COMMENT '活动状态：1-进行中，0-已结束，2-未开始',
  `start_time`       datetime     NOT NULL COMMENT '活动开始时间',
  `end_time`         datetime     NOT NULL COMMENT '活动结束时间',
  `create_time`      datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_id` (`activity_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拼团活动配置表';

-- ----------------------------
-- 拼团团队表（记录每一个拼团团队的状态和进度）
-- ----------------------------
DROP TABLE IF EXISTS `group_buy_team`;
CREATE TABLE `group_buy_team` (
  `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `team_id`          varchar(64)  NOT NULL COMMENT '团队ID（业务唯一标识）',
  `activity_id`      varchar(64)  NOT NULL COMMENT '关联活动ID',
  `product_id`       varchar(64)  NOT NULL COMMENT '关联商品ID',
  `leader_user_id`   varchar(64)  NOT NULL COMMENT '团长用户ID（第一个发起人）',
  `required_people`  int          NOT NULL COMMENT '拼团所需人数（从活动配置冗余）',
  `current_people`   int          NOT NULL DEFAULT 0 COMMENT '当前已参团人数',
  `status`           tinyint      NOT NULL DEFAULT 0 COMMENT '团队状态：0-拼团中，1-拼团成功，2-拼团失败（超时未满）',
  `group_buy_price`  decimal(10,2) NOT NULL COMMENT '本次拼团价格（快照）',
  `start_time`       datetime     NOT NULL COMMENT '开团时间（第一人参团时间）',
  `end_time`         datetime     NOT NULL COMMENT '拼团截止时间（start_time + valid_duration）',
  `create_time`      datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_id` (`team_id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_leader_user_id` (`leader_user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拼团团队表';

-- ----------------------------
-- 拼团团队成员表（记录每个成员参团信息及订单关联）
-- ----------------------------
DROP TABLE IF EXISTS `group_buy_team_member`;
CREATE TABLE `group_buy_team_member` (
  `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id`        varchar(64)  NOT NULL COMMENT '成员记录ID（业务唯一标识）',
  `team_id`          varchar(64)  NOT NULL COMMENT '关联团队ID',
  `activity_id`      varchar(64)  NOT NULL COMMENT '关联活动ID',
  `product_id`       varchar(64)  NOT NULL COMMENT '关联商品ID',
  `user_id`          varchar(64)  NOT NULL COMMENT '用户ID',
  `order_id`         varchar(64)  DEFAULT NULL COMMENT '关联订单ID',
  `pay_price`        decimal(10,2) NOT NULL COMMENT '实际支付价格（拼团价快照）',
  `is_leader`        tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否为团长：1-是，0-否',
  `address`          varchar(255) DEFAULT NULL COMMENT '收货地址快照',
  `contact_name`     varchar(64)  DEFAULT NULL COMMENT '联系人姓名快照',
  `contact_phone`    varchar(20)  DEFAULT NULL COMMENT '联系电话快照',
  `status`           tinyint      NOT NULL DEFAULT 0 COMMENT '成员状态：0-拼团中，1-拼团成功，2-拼团失败退款',
  `join_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '参团时间',
  `create_time`      datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_id` (`member_id`),
  UNIQUE KEY `uk_team_user` (`team_id`, `user_id`) COMMENT '同一团队同一用户只能参团一次',
  KEY `idx_team_id` (`team_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_activity_id` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拼团团队成员表';

SET FOREIGN_KEY_CHECKS = 1;
