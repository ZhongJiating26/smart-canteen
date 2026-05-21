-- DDL for canteen_user.
-- MySQL 8.x

CREATE DATABASE IF NOT EXISTS `canteen_user`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `canteen_user`;

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL COMMENT '主键，雪花算法生成',
    `username` VARCHAR(20) NOT NULL COMMENT '用户名，当前与手机号一致',
    `phone` VARCHAR(11) NOT NULL COMMENT '手机号',
    `student_no` VARCHAR(20) DEFAULT NULL COMMENT '学工号',
    `password_hash` VARCHAR(128) NOT NULL COMMENT 'BCrypt 密码哈希',
    `pay_password` VARCHAR(32) NOT NULL DEFAULT '123456' COMMENT '模拟支付密码，明文存储',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称/姓名',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'USER/MERCHANT/ADMIN',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1=正常, 0=禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_student_no` (`student_no`),
    KEY `idx_role` (`role`),
    CONSTRAINT `ck_user_role` CHECK (`role` IN ('USER', 'MERCHANT', 'ADMIN')),
    CONSTRAINT `ck_user_status` CHECK (`status` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `login_log` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `login_type` VARCHAR(20) NOT NULL COMMENT 'PASSWORD/VERIFY_CODE',
    `ip` VARCHAR(45) DEFAULT NULL COMMENT '登录 IP',
    `user_agent` VARCHAR(255) DEFAULT NULL COMMENT '客户端 UA',
    `status` TINYINT NOT NULL COMMENT '1=成功, 0=失败',
    `fail_reason` VARCHAR(100) DEFAULT NULL COMMENT '失败原因',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `ck_login_log_type` CHECK (`login_type` IN ('PASSWORD', 'VERIFY_CODE')),
    CONSTRAINT `ck_login_log_status` CHECK (`status` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

CREATE TABLE IF NOT EXISTS `refresh_token` (
    `id` BIGINT NOT NULL COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `token_jti` VARCHAR(64) NOT NULL COMMENT 'JWT JTI',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1=有效, 0=已注销',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token_jti` (`token_jti`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_expire_time` (`expire_time`),
    CONSTRAINT `ck_refresh_token_status` CHECK (`status` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='刷新令牌表';
