-- 为商品表添加销量字段
ALTER TABLE `lotus_root_product` ADD COLUMN `sales` INT(11) DEFAULT 0 COMMENT '销量' AFTER `stock`;