-- DDL for canteen_menu.
-- MySQL 8.x

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS `canteen_menu`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `canteen_menu`;

CREATE TABLE IF NOT EXISTS `merchant` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '关联用户服务用户 ID',
    `name` VARCHAR(100) NOT NULL COMMENT '商家名称',
    `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '负责人姓名',
    `contact_phone` VARCHAR(11) DEFAULT NULL COMMENT '联系电话',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '经营类型/窗口分类',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常, 0=停用',
    `operating_hours` VARCHAR(1000) DEFAULT NULL COMMENT '营业时间，JSON数组格式',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `ck_merchant_status` CHECK (`status` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家表';

CREATE TABLE IF NOT EXISTS `dish_category` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `merchant_id` BIGINT NOT NULL COMMENT '所属商家 ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越前',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜品分类表';

CREATE TABLE IF NOT EXISTS `dish` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `merchant_id` BIGINT NOT NULL COMMENT '所属商家 ID',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类 ID',
    `name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
    `image` MEDIUMBLOB DEFAULT NULL COMMENT '菜品图片二进制数据，最大 5MB',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '单价',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '菜品描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1=启用, 0=停用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `ck_dish_price` CHECK (`price` >= 0),
    CONSTRAINT `ck_dish_status` CHECK (`status` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜品表';

CREATE TABLE IF NOT EXISTS `daily_menu` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `merchant_id` BIGINT NOT NULL COMMENT '所属商家 ID，当前商家即窗口',
    `meal_period` VARCHAR(20) NOT NULL COMMENT 'BREAKFAST/LUNCH/DINNER/SNACK',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '1=已发布, 0=草稿',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_merchant_period` (`merchant_id`, `meal_period`),
    CONSTRAINT `ck_daily_menu_meal_period` CHECK (`meal_period` IN ('BREAKFAST', 'LUNCH', 'DINNER', 'SNACK')),
    CONSTRAINT `ck_daily_menu_status` CHECK (`status` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每日菜单配置表';

CREATE TABLE IF NOT EXISTS `daily_menu_item` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `daily_menu_id` BIGINT NOT NULL COMMENT '关联每日菜单 ID',
    `dish_id` BIGINT NOT NULL COMMENT '关联菜品 ID',
    `daily_stock` INT NOT NULL DEFAULT 0 COMMENT '每日自动重置库存数量',
    `total_stock` INT NOT NULL DEFAULT 0 COMMENT '当天总库存',
    `remaining_stock` INT NOT NULL DEFAULT 0 COMMENT '当天剩余库存',
    `reserved_stock` INT NOT NULL DEFAULT 0 COMMENT '当天预占库存',
    `alert_threshold` INT NOT NULL DEFAULT 0 COMMENT '库存预警阈值',
    `is_listed` TINYINT NOT NULL DEFAULT 1 COMMENT '1=上架, 0=下架',
    `last_reset_date` DATE DEFAULT NULL COMMENT '最近一次库存重置日期',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_menu_dish` (`daily_menu_id`, `dish_id`),
    KEY `idx_menu_id` (`daily_menu_id`),
    KEY `idx_dish_id` (`dish_id`),
    KEY `idx_is_listed` (`is_listed`),
    CONSTRAINT `ck_daily_menu_item_stock` CHECK (`daily_stock` >= 0 AND `total_stock` >= 0 AND `remaining_stock` >= 0 AND `reserved_stock` >= 0),
    CONSTRAINT `ck_daily_menu_item_alert` CHECK (`alert_threshold` >= 0),
    CONSTRAINT `ck_daily_menu_item_listed` CHECK (`is_listed` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每日菜单菜品项';

CREATE TABLE IF NOT EXISTS `inventory_log` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `daily_menu_item_id` BIGINT NOT NULL COMMENT '关联每日菜单菜品项 ID',
    `change_type` VARCHAR(20) NOT NULL COMMENT 'RESET/RESERVE/RELEASE/CONSUME',
    `change_amount` INT NOT NULL COMMENT '变更数量，正=增加，负=减少',
    `before_remaining` INT NOT NULL COMMENT '变更前剩余库存',
    `after_remaining` INT NOT NULL COMMENT '变更后剩余库存',
    `order_id` BIGINT DEFAULT NULL COMMENT '关联订单 ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    PRIMARY KEY (`id`),
    KEY `idx_daily_menu_item_id` (`daily_menu_item_id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `ck_inventory_log_type` CHECK (`change_type` IN ('RESET', 'RESERVE', 'RELEASE', 'CONSUME')),
    CONSTRAINT `ck_inventory_log_remaining` CHECK (`before_remaining` >= 0 AND `after_remaining` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存变更日志表';

CREATE TABLE IF NOT EXISTS `config` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `config_key` VARCHAR(50) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(255) NOT NULL COMMENT '配置值',
    `config_type` VARCHAR(20) NOT NULL COMMENT 'INT/STRING/BOOL',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '配置说明',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近修改时间',
    `updated_by` BIGINT DEFAULT NULL COMMENT '最近修改者管理员 ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`),
    CONSTRAINT `ck_menu_config_type` CHECK (`config_type` IN ('INT', 'STRING', 'BOOL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单服务配置表';

INSERT IGNORE INTO `config` (`id`, `config_key`, `config_value`, `config_type`, `description`, `updated_by`)
VALUES (1, 'stock_alert_scan_interval', '300', 'INT', '库存预警扫描间隔（秒）', NULL)
;
