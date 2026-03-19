package com.lotus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("traceability_info")
public class TraceabilityInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("product_id")
    private Long productId;
    @TableField("seed_date")
    private LocalDate seedDate;
    @TableField("fertilizer_record")
    private String fertilizerRecord;
    @TableField("pesticide_record")
    private String pesticideRecord;
    @TableField("harvest_date")
    private LocalDate harvestDate;
    @TableField("inspection_report")
    private String inspectionReport;
    @TableField("images")
    private String images;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}