package com.server.internshipserver.domain.system;

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
 * 学期信息实体类
 */
@ApiModel(description = "学期信息")
@Data
@TableName("semester_info")
public class Semester implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "学期ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long semesterId;
    
    @ApiModelProperty(value = "学期名称", required = true, example = "2024-2025学年第一学期")
    @TableField("semester_name")
    private String semesterName;
    
    @ApiModelProperty(value = "开始日期", required = true, example = "2024-09-01")
    @TableField("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @ApiModelProperty(value = "结束日期", required = true, example = "2025-01-31")
    @TableField("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @ApiModelProperty(value = "所属学校ID", required = true, example = "1")
    @TableField("school_id")
    private Long schoolId;
    
    @ApiModelProperty(value = "是否当前学期：1-是，0-否", example = "1")
    @TableField("is_current")
    private Integer isCurrent;
    
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
    
    @ApiModelProperty(value = "是否当前学期文字（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String isCurrentText;
    
    @ApiModelProperty(value = "开始日期文字（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String startDateText;
    
    @ApiModelProperty(value = "结束日期文字（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String endDateText;
    
    @ApiModelProperty(value = "创建时间文字（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String createTimeText;
    
    @ApiModelProperty(value = "所属学校名称（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String schoolName;
}

