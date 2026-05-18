-- DDL for canteen_order.
-- MySQL 8.x

CREATE DATABASE IF NOT EXISTS `canteen_order`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `canteen_order`;

CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT NOT NULL COMMENT '主键，雪花算法生成',
    `order_no` VARCHAR(32) NOT NULL COMMENT '系统内部订单编号，全局唯一',
    `user_id` BIGINT NOT NULL COMMENT '下单用户 ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商家 ID',
    `stall_id` BIGINT NOT NULL COMMENT '窗口 ID',
    `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAY' COMMENT '订单状态',
    `pickup_no` VARCHAR(10) DEFAULT NULL COMMENT '取餐号',
    `pickup_qrcode_id` BIGINT DEFAULT NULL COMMENT '取餐二维码关联 ID',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '用户备注',
    `cancel_reason` VARCHAR(200) DEFAULT NULL COMMENT '取消原因',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `accept_time` DATETIME DEFAULT NULL COMMENT '商家接单时间',
    `complete_time` DATETIME DEFAULT NULL COMMENT '核销完成时间',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id_status` (`user_id`, `status`),
    KEY `idx_stall_id_status` (`stall_id`, `status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_pickup_no` (`pickup_no`),
    CONSTRAINT `ck_order_amount` CHECK (`total_amount` >= 0),
    CONSTRAINT `ck_order_status` CHECK (`status` IN ('PENDING_PAY', 'PAID', 'PREPARING', 'READY', 'COMPLETED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单主表';

CREATE TABLE IF NOT EXISTS `order_item` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `order_id` BIGINT NOT NULL COMMENT '关联订单 ID',
    `dish_id` BIGINT NOT NULL COMMENT '菜品 ID',
    `dish_name` VARCHAR(100) NOT NULL COMMENT '下单时菜品名称快照',
    `dish_image` MEDIUMBLOB DEFAULT NULL COMMENT '下单时菜品图片二进制快照',
    `unit_price` DECIMAL(10, 2) NOT NULL COMMENT '下单时单价快照',
    `quantity` INT NOT NULL COMMENT '数量',
    `subtotal` DECIMAL(10, 2) NOT NULL COMMENT '小计',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_dish_id` (`dish_id`),
    CONSTRAINT `ck_order_item_price` CHECK (`unit_price` >= 0 AND `subtotal` >= 0),
    CONSTRAINT `ck_order_item_quantity` CHECK (`quantity` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';

CREATE TABLE IF NOT EXISTS `order_status_log` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `order_id` BIGINT NOT NULL COMMENT '关联订单 ID',
    `from_status` VARCHAR(20) NOT NULL COMMENT '变更前状态',
    `to_status` VARCHAR(20) NOT NULL COMMENT '变更后状态',
    `operator_id` BIGINT NOT NULL COMMENT '操作者 ID，系统操作为 0',
    `operator_role` VARCHAR(20) NOT NULL COMMENT 'USER/MERCHANT/ADMIN/SYSTEM',
    `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `ck_order_status_log_from` CHECK (`from_status` IN ('PENDING_PAY', 'PAID', 'PREPARING', 'READY', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT `ck_order_status_log_to` CHECK (`to_status` IN ('PENDING_PAY', 'PAID', 'PREPARING', 'READY', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT `ck_order_status_log_operator_role` CHECK (`operator_role` IN ('USER', 'MERCHANT', 'ADMIN', 'SYSTEM'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单状态变更日志表';

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
    CONSTRAINT `ck_order_config_type` CHECK (`config_type` IN ('INT', 'STRING', 'BOOL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单服务配置表';

INSERT IGNORE INTO `config` (`id`, `config_key`, `config_value`, `config_type`, `description`, `updated_by`)
VALUES
    (1, 'pay_timeout_minutes', '10', 'INT', '待支付超时（分钟）', NULL),
    (2, 'accept_timeout_minutes', '10', 'INT', '商家接单超时（分钟）', NULL),
    (3, 'cancel_window_minutes', '1', 'INT', '接单后可取消窗口（分钟）', NULL)
;
