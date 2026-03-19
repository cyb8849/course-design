-- 为溯源信息表添加图片字段
ALTER TABLE `traceability_info` ADD COLUMN `images` TEXT COMMENT '溯源图片URL列表，JSON格式存储';
