-- 商品分类数据
INSERT INTO product_category (category_name, parent_id, sort_order, status, create_time, update_time)
VALUES
('新鲜莲藕', 0, 1, 1, NOW(), NOW()),
('莲藕制品', 0, 2, 1, NOW(), NOW()),
('特色农产品', 0, 3, 1, NOW(), NOW());

-- 商品数据
INSERT INTO lotus_root_product (product_name, category_id, farmer_id, price, stock, description, image_url, status, origin, harvest_time, create_time, update_time)
VALUES
-- 新鲜莲藕类
('西胪特优粉藕', 1, 1002, 15.80, 200, '西胪镇特产，粉糯香甜，适合煲汤', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fresh%20lotus%20root%20vegetable%20organic%20farm&image_size=square', 'ON_SALE', '汕头市潮阳区西胪镇', CURDATE(), NOW(), NOW()),
('西胪脆嫩藕尖', 1, 1003, 22.80, 150, '新鲜采摘的藕尖，清脆爽口，适合清炒', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lotus%20root%20tips%20fresh%20vegetable&image_size=square', 'ON_SALE', '汕头市潮阳区西胪镇', CURDATE(), NOW(), NOW()),
('西胪生态莲藕', 1, 1002, 12.80, 180, '生态种植，无农药，绿色健康', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=ecological%20lotus%20root%20vegetable&image_size=square', 'ON_SALE', '汕头市潮阳区西胪镇', CURDATE(), NOW(), NOW()),

-- 莲藕制品类
('西胪纯藕粉', 2, 1004, 39.90, 100, '100%纯藕粉，无添加，营养丰富', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lotus%20root%20starch%20powder%20pure%20packaging&image_size=square', 'ON_SALE', '汕头市潮阳区西胪镇', CURDATE(), NOW(), NOW()),
('西胪莲藕干', 2, 1004, 49.90, 80, '自然晾晒，保留营养，方便存储', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=dried%20lotus%20root%20slices%20traditional&image_size=square', 'ON_SALE', '汕头市潮阳区西胪镇', CURDATE(), NOW(), NOW()),
('西胪藕片零食', 2, 1004, 29.90, 120, '脱水藕片，香脆可口，健康零食', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lotus%20root%20chips%20snack%20packaging&image_size=square', 'ON_SALE', '汕头市潮阳区西胪镇', CURDATE(), NOW(), NOW()),

-- 特色农产品类
('西胪农家土鸡蛋', 3, 1005, 2.50, 500, '农家散养土鸡下的蛋，营养丰富', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fresh%20farm%20eggs%20organic&image_size=square', 'ON_SALE', '汕头市潮阳区西胪镇', CURDATE(), NOW(), NOW()),
('西胪新鲜蔬菜礼盒', 3, 1005, 88.00, 50, '精选新鲜蔬菜组合，绿色健康', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fresh%20vegetable%20gift%20box&image_size=square', 'ON_SALE', '汕头市潮阳区西胪镇', CURDATE(), NOW(), NOW()),
('西胪农家蜂蜜', 3, 1005, 58.00, 80, '纯天然蜂蜜，香甜可口', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=natural%20honey%20jar%20farm&image_size=square', 'ON_SALE', '汕头市潮阳区西胪镇', CURDATE(), NOW(), NOW());
