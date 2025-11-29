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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 考勤组时间段实体类
 */
@ApiModel(description = "考勤组时间段信息")
@Data
@TableName("attendance_group_time_slot")
public class AttendanceGroupTimeSlot implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "时间段ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long slotId;
    
    @ApiModelProperty(value = "考勤组ID", required = true, example = "1")
    @TableField("group_id")
    private Long groupId;
    
    @ApiModelProperty(value = "时间段名称（如：早班、晚班、标准班）", required = true, example = "标准班")
    @TableField("slot_name")
    private String slotName;
    
    @ApiModelProperty(value = "上班时间", required = true, example = "09:00:00")
    @TableField("start_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;
    
    @ApiModelProperty(value = "下班时间", required = true, example = "18:00:00")
    @TableField("end_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;
    
    @ApiModelProperty(value = "工作时长（小时）", required = true, example = "8.00")
    @TableField("work_hours")
    private BigDecimal workHours;
    
    @ApiModelProperty(value = "是否默认：1-是，0-否", example = "1")
    @TableField("is_default")
    private Integer isDefault;
    
    @ApiModelProperty(value = "排序顺序", example = "0")
    @TableField("sort_order")
    private Integer sortOrder;
    
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

