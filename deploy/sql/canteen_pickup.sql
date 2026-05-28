-- DDL for canteen_pickup.
-- MySQL 8.x

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS `canteen_pickup`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `canteen_pickup`;

CREATE TABLE IF NOT EXISTS `pickup_number_sequence` (
    `id` BIGINT NOT NULL COMMENT '主键，固定为 1',
    `current_letter` CHAR(1) NOT NULL DEFAULT 'A' COMMENT '当前字母位 A-Z',
    `current_number` INT NOT NULL DEFAULT 1 COMMENT '当前数字位 1-999',
    `reset_rule` VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT 'NONE/DAILY/WEEKLY',
    `last_reset_time` DATETIME DEFAULT NULL COMMENT '上次重置时间',
    PRIMARY KEY (`id`),
    CONSTRAINT `ck_pickup_sequence_letter` CHECK (`current_letter` BETWEEN 'A' AND 'Z'),
    CONSTRAINT `ck_pickup_sequence_number` CHECK (`current_number` BETWEEN 1 AND 999),
    CONSTRAINT `ck_pickup_sequence_reset_rule` CHECK (`reset_rule` IN ('NONE', 'DAILY', 'WEEKLY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='取餐号全局序列表';

CREATE TABLE IF NOT EXISTS `pickup_qrcode` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `order_id` BIGINT NOT NULL COMMENT '关联订单 ID',
    `qrcode_content` VARCHAR(255) NOT NULL COMMENT '二维码编码内容',
    `pickup_code` VARCHAR(32) NOT NULL COMMENT '取餐码字符串',
    `is_verified` TINYINT NOT NULL DEFAULT 0 COMMENT '0=未核销, 1=已核销',
    `verify_time` DATETIME DEFAULT NULL COMMENT '核销时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    UNIQUE KEY `uk_pickup_code` (`pickup_code`),
    KEY `idx_is_verified` (`is_verified`),
    CONSTRAINT `ck_pickup_qrcode_verified` CHECK (`is_verified` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='取餐二维码表';

CREATE TABLE IF NOT EXISTS `preparing_queue_item` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `merchant_id` BIGINT NOT NULL COMMENT '商家窗口 ID',
    `order_id` BIGINT NOT NULL COMMENT '订单 ID',
    `pickup_no` VARCHAR(10) NOT NULL COMMENT '取餐号',
    `enter_time` DATETIME NOT NULL COMMENT '进入制作中队列时间',
    `position` INT NOT NULL COMMENT '队列位置序号，从 1 开始',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_merchant_id_enter` (`merchant_id`, `enter_time`),
    KEY `idx_pickup_no` (`pickup_no`),
    CONSTRAINT `ck_preparing_queue_position` CHECK (`position` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='制作中队列项';

CREATE TABLE IF NOT EXISTS `waiting_queue_item` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `merchant_id` BIGINT NOT NULL COMMENT '商家窗口 ID',
    `order_id` BIGINT NOT NULL COMMENT '订单 ID',
    `pickup_no` VARCHAR(10) NOT NULL COMMENT '取餐号',
    `enter_time` DATETIME NOT NULL COMMENT '进入待取餐队列时间',
    `position` INT NOT NULL COMMENT '队列位置序号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_merchant_id_enter` (`merchant_id`, `enter_time`),
    KEY `idx_pickup_no` (`pickup_no`),
    CONSTRAINT `ck_waiting_queue_position` CHECK (`position` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='待取餐队列项';

CREATE TABLE IF NOT EXISTS `call_event` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `merchant_id` BIGINT NOT NULL COMMENT '商家窗口 ID',
    `order_id` BIGINT NOT NULL COMMENT '订单 ID',
    `pickup_no` VARCHAR(10) NOT NULL COMMENT '取餐号',
    `call_type` VARCHAR(10) NOT NULL COMMENT 'AUTO/MANUAL',
    `call_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '叫号时间',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id_call_time` (`merchant_id`, `call_time`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_pickup_no` (`pickup_no`),
    CONSTRAINT `ck_call_event_type` CHECK (`call_type` IN ('AUTO', 'MANUAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='叫号事件表';

CREATE TABLE IF NOT EXISTS `verify_record` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `order_id` BIGINT NOT NULL COMMENT '订单 ID',
    `qrcode_id` BIGINT NOT NULL COMMENT '取餐二维码 ID',
    `merchant_id` BIGINT NOT NULL COMMENT '核销商家窗口 ID',
    `verify_type` VARCHAR(10) NOT NULL COMMENT 'SCAN/MANUAL',
    `operator_id` BIGINT NOT NULL COMMENT '核销操作商家用户 ID',
    `verify_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '核销时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_qrcode_id` (`qrcode_id`),
    KEY `idx_merchant_id_verify_time` (`merchant_id`, `verify_time`),
    CONSTRAINT `ck_verify_record_type` CHECK (`verify_type` IN ('SCAN', 'MANUAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='核销记录表';

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
    CONSTRAINT `ck_pickup_config_type` CHECK (`config_type` IN ('INT', 'STRING', 'BOOL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='取餐服务配置表';

INSERT IGNORE INTO `pickup_number_sequence` (`id`, `current_letter`, `current_number`, `reset_rule`, `last_reset_time`)
VALUES (1, 'A', 1, 'NONE', NULL)
;

INSERT IGNORE INTO `config` (`id`, `config_key`, `config_value`, `config_type`, `description`, `updated_by`)
VALUES (1, 'pickup_number_reset_rule', 'NONE', 'STRING', '取餐号重置规则', NULL)
;
