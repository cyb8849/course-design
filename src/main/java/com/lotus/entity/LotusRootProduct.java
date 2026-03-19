package com.lotus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("lotus_root_product")
public class LotusRootProduct {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("product_name")
    private String productName;
    @TableField("category_id")
    private Long categoryId;
    @TableField("farmer_id")
    private Long farmerId;
    private Double price;
    private Integer stock;
    private Integer sales;
    private String description;
    @TableField("image_url")
    private String imageUrl;
    private String status;
    private String origin;
    @TableField("harvest_time")
    private LocalDate harvestTime;
    @TableField("planting_info")
    private String plantingInfo;
    @TableField("nutrition_info")
    private String nutritionInfo;
    @TableField("cooking_suggestion")
    private String cookingSuggestion;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}