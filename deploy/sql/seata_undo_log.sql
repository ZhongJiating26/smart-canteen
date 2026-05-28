-- Seata undo_log DDL for transactional service databases.
-- MySQL 8.x / Seata AT mode

SET NAMES utf8mb4;

USE `canteen_order`;

CREATE TABLE IF NOT EXISTS `undo_log` (
    `branch_id` BIGINT NOT NULL COMMENT 'Branch transaction id',
    `xid` VARCHAR(128) NOT NULL COMMENT 'Global transaction id',
    `context` VARCHAR(128) NOT NULL COMMENT 'Undo log context, such as serialization',
    `rollback_info` LONGBLOB NOT NULL COMMENT 'Rollback info',
    `log_status` INT NOT NULL COMMENT '0=normal, 1=defense',
    `log_created` DATETIME(6) NOT NULL COMMENT 'Create time',
    `log_modified` DATETIME(6) NOT NULL COMMENT 'Modify time',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Seata AT undo log';

USE `canteen_menu`;

CREATE TABLE IF NOT EXISTS `undo_log` (
    `branch_id` BIGINT NOT NULL COMMENT 'Branch transaction id',
    `xid` VARCHAR(128) NOT NULL COMMENT 'Global transaction id',
    `context` VARCHAR(128) NOT NULL COMMENT 'Undo log context, such as serialization',
    `rollback_info` LONGBLOB NOT NULL COMMENT 'Rollback info',
    `log_status` INT NOT NULL COMMENT '0=normal, 1=defense',
    `log_created` DATETIME(6) NOT NULL COMMENT 'Create time',
    `log_modified` DATETIME(6) NOT NULL COMMENT 'Modify time',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Seata AT undo log';
