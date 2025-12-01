package com.server.internshipserver.domain.internship;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤组规则实体类
 * 用于存储考勤组的特殊规则，包括工作日、节假日、特殊日期等规则配置
 * 支持单个日期和日期范围两种规则类型，用于灵活配置考勤规则
 */
@ApiModel(description = "考勤组规则信息")
@Data
@TableName("attendance_group_rule")
public class AttendanceGroupRule implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "规则ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long ruleId;
    
    @ApiModelProperty(value = "考勤组ID", required = true, example = "1")
    @TableField("group_id")
    private Long groupId;
    
    @ApiModelProperty(value = "规则类型：1-工作日，2-节假日，3-特殊日期", required = true, example = "2")
    @TableField("rule_type")
    private Integer ruleType;
    
    @ApiModelProperty(value = "规则日期（单个日期）", example = "2025-01-01")
    @TableField("rule_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ruleDate;
    
    @ApiModelProperty(value = "规则开始日期（日期范围）", example = "2025-01-28")
    @TableField("rule_start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ruleStartDate;
    
    @ApiModelProperty(value = "规则结束日期（日期范围）", example = "2025-02-03")
    @TableField("rule_end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ruleEndDate;
    
    @ApiModelProperty(value = "规则值（JSON格式）", example = "{\"holidayName\":\"元旦\",\"isPaid\":false}")
    @TableField("rule_value")
    private String ruleValue;
    
    @ApiModelProperty(value = "规则描述", example = "元旦")
    @TableField("description")
    private String description;
    
    @ApiModelProperty(value = "删除标志：0-未删除，1-已删除", example = "0")
    @TableField("delete_flag")
    private Integer deleteFlag;
    
    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

