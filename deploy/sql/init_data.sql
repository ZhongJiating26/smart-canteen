-- Initial demo data for local development.
-- Execute after canteen_*.sql and seata_undo_log.sql.

-- Demo accounts:
-- 13800000001 / 123456
-- 13800000002 / 123456
-- 13800000003 / 123456
SET NAMES utf8mb4;

SET @demo_password_hash = '$2a$10$76A4G48cbNH2lFWj7rjlZOl6AFtVeLLS9gDki7m7sohyncHJLAcmq';

USE `canteen_user`;

INSERT IGNORE INTO `user` (
    `id`, `username`, `phone`, `student_no`, `password_hash`, `pay_password`,
    `nickname`, `avatar`, `role`, `status`
) VALUES
    (1000001, '13800000001', '13800000001', 'USER001', @demo_password_hash, '123456', '测试用户', NULL, 'USER', 1),
    (1000002, '13800000002', '13800000002', 'MERCHANT001', @demo_password_hash, '123456', '测试商家', NULL, 'MERCHANT', 1),
    (1000003, '13800000003', '13800000003', 'ADMIN001', @demo_password_hash, '123456', '系统管理员', NULL, 'ADMIN', 1);

USE `canteen_menu`;

INSERT IGNORE INTO `merchant` (
    `id`, `user_id`, `name`, `contact_name`, `contact_phone`, `category`, `status`, `operating_hours`
) VALUES
    (2000001, 1000002, '一号窗口', '王店长', '13800000001', '综合快餐', 1, '[{"startTime":"00:00","endTime":"23:59"}]');

INSERT IGNORE INTO `dish_category` (
    `id`, `merchant_id`, `name`, `sort`
) VALUES
    (4000001, 2000001, '主食', 1),
    (4000002, 2000001, '套餐', 2),
    (4000003, 2000001, '饮品', 3);

INSERT IGNORE INTO `dish` (
    `id`, `merchant_id`, `category_id`, `name`, `image`, `price`, `description`, `status`
) VALUES
    (5000001, 2000001, 4000001, '番茄鸡蛋盖饭', NULL, 12.00, '番茄鸡蛋配米饭，酸甜开胃', 1),
    (5000002, 2000001, 4000001, '香菇滑鸡饭', NULL, 16.00, '鸡腿肉、香菇和时蔬搭配', 1),
    (5000003, 2000001, 4000001, '黑椒牛柳饭', NULL, 20.00, '黑椒牛柳配米饭', 1),
    (5000004, 2000001, 4000001, '三鲜炒面', NULL, 13.00, '鸡蛋、蔬菜和肉丝炒面', 1),
    (5000005, 2000001, 4000002, '宫保鸡丁套餐', NULL, 18.00, '宫保鸡丁、米饭和例汤', 1),
    (5000006, 2000001, 4000002, '鱼香肉丝套餐', NULL, 17.00, '鱼香肉丝、米饭和例汤', 1),
    (5000007, 2000001, 4000002, '红烧排骨套餐', NULL, 24.00, '红烧排骨、米饭和时蔬', 1),
    (5000008, 2000001, 4000002, '素菜双拼套餐', NULL, 14.00, '两款当日素菜、米饭和例汤', 1),
    (5000009, 2000001, 4000003, '柠檬红茶', NULL, 6.00, '冷饮，默认少冰', 1),
    (5000010, 2000001, 4000003, '豆浆', NULL, 3.00, '现磨热豆浆', 1);

INSERT INTO `daily_menu` (
    `id`, `merchant_id`, `meal_period`, `status`
) VALUES
    (6000001, 2000001, 'LUNCH', 1)
ON DUPLICATE KEY UPDATE
    `merchant_id` = VALUES(`merchant_id`),
    `meal_period` = VALUES(`meal_period`),
    `status` = VALUES(`status`);

INSERT IGNORE INTO `daily_menu_item` (
    `id`, `daily_menu_id`, `dish_id`, `daily_stock`, `total_stock`, `remaining_stock`,
    `reserved_stock`, `alert_threshold`, `is_listed`, `last_reset_date`
) VALUES
    (7000001, 6000001, 5000001, 50, 50, 50, 0, 10, 1, CURDATE()),
    (7000002, 6000001, 5000002, 50, 50, 50, 0, 10, 1, CURDATE()),
    (7000003, 6000001, 5000003, 50, 50, 50, 0, 10, 1, CURDATE()),
    (7000004, 6000001, 5000004, 50, 50, 50, 0, 10, 1, CURDATE()),
    (7000005, 6000001, 5000005, 50, 50, 50, 0, 10, 1, CURDATE()),
    (7000006, 6000001, 5000006, 50, 50, 50, 0, 10, 1, CURDATE()),
    (7000007, 6000001, 5000007, 50, 50, 50, 0, 10, 1, CURDATE()),
    (7000008, 6000001, 5000008, 50, 50, 50, 0, 10, 1, CURDATE()),
    (7000009, 6000001, 5000009, 50, 50, 50, 0, 10, 1, CURDATE()),
    (7000010, 6000001, 5000010, 50, 50, 50, 0, 10, 1, CURDATE());

USE `canteen_pickup`;

INSERT IGNORE INTO `pickup_number_sequence` (
    `id`, `current_letter`, `current_number`, `reset_rule`, `last_reset_time`
) VALUES
    (1, 'A', 1, 'NONE', NULL);
