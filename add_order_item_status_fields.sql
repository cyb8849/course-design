-- 为订单项添加状态和发货时间字段
ALTER TABLE order_item
ADD COLUMN status VARCHAR(20) DEFAULT 'PENDING' COMMENT '订单项状态：PENDING-待处理, SHIPPED-已发货',
ADD COLUMN shipping_time DATETIME COMMENT '发货时间';

-- 更新已有订单项的状态为待处理
UPDATE order_item SET status = 'PENDING' WHERE status IS NULL;