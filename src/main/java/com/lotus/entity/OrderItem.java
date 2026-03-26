package com.lotus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_item")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("order_id")
    private Long orderId;
    @TableField("product_id")
    private Long productId;
    @TableField("farmer_id")
    private Long farmerId;
    @TableField("product_name")
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private String status;
    @TableField("shipping_time")
    private LocalDateTime shippingTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}