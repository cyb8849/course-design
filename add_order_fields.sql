-- 为order_main表添加收货信息字段
ALTER TABLE `order_main` 
ADD COLUMN `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名' AFTER `address_id`,
ADD COLUMN `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话' AFTER `receiver_name`,
ADD COLUMN `receiver_address` VARCHAR(200) NOT NULL COMMENT '收货地址' AFTER `receiver_phone`;

-- 为现有数据设置默认值（如果需要）
UPDATE `order_main` 
SET `receiver_name` = '未知', 
    `receiver_phone` = '13800138000', 
    `receiver_address` = '未知地址' 
WHERE `receiver_name` IS NULL;