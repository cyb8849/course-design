package com.lotus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流信息实体类
 * 存储订单的物流追踪信息
 */
@Data
@TableName("logistics_info")
public class LogisticsInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("order_id")
    private Long orderId;
    
    @TableField("order_sub_id")
    private Long orderSubId;
    
    /**
     * 快递公司代码
     * 如：SF(顺丰)、YTO(圆通)、ZTO(中通)等
     */
    @TableField("express_company")
    private String expressCompany;
    
    /**
     * 快递公司名称
     */
    @TableField("express_company_name")
    private String expressCompanyName;
    
    /**
     * 快递单号
     */
    @TableField("tracking_no")
    private String trackingNo;
    
    /**
     * 物流状态
     * 0-无轨迹
     * 1-已揽收
     * 2-在途中
     * 3-已签收
     * 4-问题件
     */
    @TableField("status")
    private Integer status;
    
    /**
     * 物流轨迹数据(JSON格式)
     * 存储完整的物流轨迹信息
     */
    @TableField("tracking_data")
    private String trackingData;
    
    /**
     * 最新物流信息
     */
    @TableField("latest_info")
    private String latestInfo;
    
    /**
     * 最新物流时间
     */
    @TableField("latest_time")
    private LocalDateTime latestTime;
    
    /**
     * 是否订阅物流推送
     */
    @TableField("is_subscribed")
    private Boolean isSubscribed;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
