-- 创建子订单表
CREATE TABLE IF NOT EXISTS order_sub (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '子订单ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    farmer_id BIGINT NOT NULL COMMENT '农户ID',
    farmer_name VARCHAR(50) NOT NULL COMMENT '农户名称',
    sub_total_amount DECIMAL(10,2) NOT NULL COMMENT '子订单总金额',
    status VARCHAR(20) DEFAULT 'PAID' COMMENT '子订单状态：PENDING-待处理, PAID-已支付, SHIPPED-已发货, DELIVERED-已送达',
    payment_time DATETIME COMMENT '支付时间',
    shipping_time DATETIME COMMENT '发货时间',
    delivery_time DATETIME COMMENT '送达时间',
    logistics_id BIGINT COMMENT '物流信息ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_order_sub_order FOREIGN KEY (order_id) REFERENCES order_main (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='子订单表';

-- 为物流信息表添加子订单ID字段
ALTER TABLE logistics_info
ADD COLUMN order_sub_id BIGINT COMMENT '子订单ID',
ADD CONSTRAINT fk_logistics_order_sub FOREIGN KEY (order_sub_id) REFERENCES order_sub (id) ON DELETE CASCADE;

-- 创建索引
CREATE INDEX idx_order_sub_farmer ON order_sub (farmer_id);
CREATE INDEX idx_order_sub_order ON order_sub (order_id);
CREATE INDEX idx_logistics_order_sub ON logistics_info (order_sub_id);