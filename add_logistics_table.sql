-- 创建物流信息表
CREATE TABLE `logistics_info` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '物流ID',
  `order_id` BIGINT(20) NOT NULL COMMENT '订单ID',
  `express_company` VARCHAR(20) NOT NULL COMMENT '快递公司代码',
  `express_company_name` VARCHAR(50) NOT NULL COMMENT '快递公司名称',
  `tracking_no` VARCHAR(50) NOT NULL COMMENT '快递单号',
  `status` INT(1) DEFAULT 0 COMMENT '物流状态：0-无轨迹，1-已揽收，2-在途中，3-已签收，4-问题件',
  `tracking_data` TEXT COMMENT '物流轨迹数据(JSON格式)',
  `latest_info` VARCHAR(500) COMMENT '最新物流信息',
  `latest_time` DATETIME COMMENT '最新物流时间',
  `is_subscribed` TINYINT(1) DEFAULT 0 COMMENT '是否订阅物流推送',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_tracking_no` (`tracking_no`),
  FOREIGN KEY (`order_id`) REFERENCES `order_main` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='物流信息表';
