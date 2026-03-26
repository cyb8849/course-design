package com.lotus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_sub")
public class OrderSub {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("order_id")
    private Long orderId;
    @TableField("farmer_id")
    private Long farmerId;
    @TableField("farmer_name")
    private String farmerName;
    @TableField("sub_total_amount")
    private BigDecimal subTotalAmount;
    private String status;
    @TableField("payment_time")
    private LocalDateTime paymentTime;
    @TableField("shipping_time")
    private LocalDateTime shippingTime;
    @TableField("delivery_time")
    private LocalDateTime deliveryTime;
    @TableField("logistics_id")
    private Long logisticsId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}