-- ============================================
-- 大众点评系统 - 数据库创建语句
-- 数据库: MySQL 8.0+
-- ============================================

CREATE DATABASE IF NOT EXISTS `public_reviews`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `public_reviews`;

-- ============================================
-- 1. 用户表
-- status: 0=普通用户 1=商户 2=被封禁
-- shop_id: 关联店铺ID，默认NULL
-- ============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
  `name`          VARCHAR(50)      NOT NULL                 COMMENT '用户昵称',
  `phone`         CHAR(11)         NOT NULL                 COMMENT '手机号',
  `image`         VARCHAR(500)     NOT NULL DEFAULT ''      COMMENT '头像URL',
  `password`      VARCHAR(255)     NOT NULL                 COMMENT '密码(加密)',
  `fans_counts`   INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '粉丝数量',
  `follower_counts` INT UNSIGNED   NOT NULL DEFAULT 0       COMMENT '关注数量',
  `money`         DECIMAL(10,2)    NOT NULL DEFAULT 0.00    COMMENT '余额',
  `address`       VARCHAR(100)     NOT NULL DEFAULT ''      COMMENT '地区',
  `status`        TINYINT          NOT NULL DEFAULT 0       COMMENT '状态: 0普通用户 1商户 2被封禁',
  `shop_id`       BIGINT UNSIGNED  DEFAULT NULL             COMMENT '关联店铺ID',
  `create_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 店铺类型表（固定数据）
-- ============================================
DROP TABLE IF EXISTS `shop_type`;
CREATE TABLE `shop_type` (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '类型ID',
  `name`          VARCHAR(50)      NOT NULL                 COMMENT '类型名称',
  `create_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺类型表';

-- 插入6种固定类型
INSERT INTO `shop_type` (`id`, `name`) VALUES
  (1, '美食'),
  (2, '健身'),
  (3, 'KTV'),
  (4, '理发'),
  (5, '按摩足疗'),
  (6, '酒吧');

-- ============================================
-- 3. 店铺表
-- score: 评分，默认0表示未点评
-- ============================================
DROP TABLE IF EXISTS `shop`;
CREATE TABLE `shop` (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '店铺ID',
  `type_id`       BIGINT UNSIGNED  NOT NULL                 COMMENT '类型ID',
  `name`          VARCHAR(100)     NOT NULL                 COMMENT '店铺名称',
  `images`        VARCHAR(2000)    NOT NULL DEFAULT ''      COMMENT '店铺图片(逗号分隔)',
  `address`       VARCHAR(200)     NOT NULL                 COMMENT '地点',
  `price`         DECIMAL(10,2)    NOT NULL DEFAULT 0.00    COMMENT '单价(元/人)',
  `comments`      INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '评论数量',
  `sold`          INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '销量',
  `score`         DECIMAL(2,1)     NOT NULL DEFAULT 0.0     COMMENT '评分(0表示未点评)',
  `create_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_type_id` (`type_id`),
  KEY `idx_score` (`score`),
  KEY `idx_name` (`name`),
  CONSTRAINT `fk_shop_type` FOREIGN KEY (`type_id`) REFERENCES `shop_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺表';

-- ============================================
-- 4. 博客表
-- ============================================
DROP TABLE IF EXISTS `blog`;
CREATE TABLE `blog` (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '博客ID',
  `user_id`       BIGINT UNSIGNED  NOT NULL                 COMMENT '发表用户ID',
  `shop_id`       BIGINT UNSIGNED  NOT NULL                 COMMENT '关联店铺ID',
  `likes`         INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '点赞数量',
  `title`         VARCHAR(200)     NOT NULL                 COMMENT '标题',
  `content`       TEXT             NOT NULL                 COMMENT '正文',
  `images`        VARCHAR(2000)    NOT NULL DEFAULT ''      COMMENT '图片路径(逗号分隔)',
  `create_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_blog_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_blog_shop` FOREIGN KEY (`shop_id`) REFERENCES `shop` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='博客表';

-- ============================================
-- 5. 用户关注表
-- ============================================
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow` (
  `id`              BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '关注记录ID',
  `user_id`         BIGINT UNSIGNED  NOT NULL                 COMMENT '用户ID(关注者)',
  `follow_user_id`  BIGINT UNSIGNED  NOT NULL                 COMMENT '被关注用户ID',
  `create_time`     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_follow` (`user_id`, `follow_user_id`),
  KEY `idx_follow_user_id` (`follow_user_id`),
  CONSTRAINT `fk_follow_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_follow_target` FOREIGN KEY (`follow_user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注表';

-- ============================================
-- 6. 优惠卷表
-- value: 优惠卷面额(可抵扣金额)
-- price: 购买优惠卷所需金额
-- ============================================
DROP TABLE IF EXISTS `voucher`;
CREATE TABLE `voucher` (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '优惠卷ID',
  `shop_id`       BIGINT UNSIGNED  NOT NULL                 COMMENT '关联店铺ID',
  `price`         DECIMAL(10,2)    NOT NULL                 COMMENT '优惠卷售价',
  `value`         DECIMAL(10,2)    NOT NULL                 COMMENT '抵扣金额',
  `begin_time`    DATETIME         NOT NULL                 COMMENT '开始时间',
  `end_time`      DATETIME         NOT NULL                 COMMENT '结束时间',
  `stock`         INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '库存数量',
  `create_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_begin_time` (`begin_time`),
  KEY `idx_end_time` (`end_time`),
  CONSTRAINT `fk_voucher_shop` FOREIGN KEY (`shop_id`) REFERENCES `shop` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠卷表';

-- ============================================
-- 7. 优惠卷订单表
-- status: 0=未使用 1=已使用 2=已过期
-- ============================================
DROP TABLE IF EXISTS `voucher_order`;
CREATE TABLE `voucher_order` (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '订单ID',
  `user_id`       BIGINT UNSIGNED  NOT NULL                 COMMENT '下单用户ID',
  `voucher_id`    BIGINT UNSIGNED  NOT NULL                 COMMENT '优惠卷ID',
  `shop_id`       BIGINT UNSIGNED  NOT NULL                 COMMENT '店铺ID',
  `status`        TINYINT          NOT NULL DEFAULT 0       COMMENT '状态: 0未使用 1已使用 2已过期',
  `order_time`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_voucher_id` (`voucher_id`),
  KEY `idx_shop_id` (`shop_id`),
  CONSTRAINT `fk_vo_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_vo_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `voucher` (`id`),
  CONSTRAINT `fk_vo_shop`   FOREIGN KEY (`shop_id`)  REFERENCES `shop` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠卷订单表';

ALTER TABLE voucher_order ADD UNIQUE INDEX uk_user_voucher (user_id, voucher_id);

-- ============================================
-- 8. 店铺订单表
-- status: 1=支付成功 2=退款
-- ============================================
DROP TABLE IF EXISTS `shop_order`;
CREATE TABLE `shop_order` (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '订单ID',
  `shop_id`       BIGINT UNSIGNED  NOT NULL                 COMMENT '店铺ID',
  `user_id`       BIGINT UNSIGNED  NOT NULL                 COMMENT '下单用户ID',
  `voucher_id`    BIGINT UNSIGNED                           COMMENT '优惠卷id',
  `price`         DECIMAL(10,2)    NOT NULL                 COMMENT '交易金额',
  `status`        TINYINT          NOT NULL DEFAULT 1       COMMENT '状态: 1支付成功 2退款',
  `order_time`    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_voucher_id` (`voucher_id`),
  CONSTRAINT `fk_so_shop` FOREIGN KEY (`shop_id`) REFERENCES `shop` (`id`),
  CONSTRAINT `fk_so_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_so_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `voucher` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺订单表';

-- ============================================
-- 9. 用户评价表（comments）
-- ============================================
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments` (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '评价ID',
  `shop_id`       BIGINT UNSIGNED  NOT NULL                 COMMENT '店铺ID',
  `user_id`       BIGINT UNSIGNED  NOT NULL                 COMMENT '评价用户ID',
  `content`       TEXT             NOT NULL                 COMMENT '评价正文',
  `score`         TINYINT          NOT NULL                 COMMENT '评分(1-5)',
  `likes`         INT UNSIGNED     NOT NULL DEFAULT 0       COMMENT '点赞数',
  `images`        VARCHAR(2000)    NOT NULL DEFAULT ''      COMMENT '图片(逗号分隔)',
  `create_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_score` (`score`),
  CONSTRAINT `fk_comment_shop` FOREIGN KEY (`shop_id`) REFERENCES `shop` (`id`),
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户评价表';

-- ============================================
-- 10. 管理员表
-- ============================================
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `id`            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT  COMMENT '管理员ID',
  `name`          VARCHAR(50)      NOT NULL                 COMMENT '管理员昵称',
  `password`      VARCHAR(255)     NOT NULL                 COMMENT '密码(加密)',
  `create_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';