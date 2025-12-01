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
 * 考勤组学生关联实体类
 * 用于存储学生与考勤组的关联关系，包括生效时间范围、状态等
 * 一个学生可以关联一个考勤组，考勤组用于计算学生的考勤记录
 */
@ApiModel(description = "考勤组学生关联信息")
@Data
@TableName("attendance_group_student")
public class AttendanceGroupStudent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "关联ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @ApiModelProperty(value = "考勤组ID", required = true, example = "1")
    @TableField("group_id")
    private Long groupId;
    
    @ApiModelProperty(value = "实习申请ID", required = true, example = "1")
    @TableField("apply_id")
    private Long applyId;
    
    @ApiModelProperty(value = "学生ID", required = true, example = "1")
    @TableField("student_id")
    private Long studentId;
    
    @ApiModelProperty(value = "生效开始日期", required = true, example = "2025-01-01")
    @TableField("effective_start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveStartDate;
    
    @ApiModelProperty(value = "生效结束日期", example = "2025-06-30")
    @TableField("effective_end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveEndDate;
    
    @ApiModelProperty(value = "状态：1-生效，0-失效", example = "1")
    @TableField("status")
    private Integer status;
    
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
    
    @ApiModelProperty(value = "创建人ID", required = true, example = "1")
    @TableField("create_user_id")
    private Long createUserId;
    
    // ========== 关联字段（不映射到数据库） ==========
    @ApiModelProperty(value = "学生姓名")
    @TableField(exist = false)
    private String studentName;
    
    @ApiModelProperty(value = "学号")
    @TableField(exist = false)
    private String studentNo;
    
    @ApiModelProperty(value = "考勤组名称")
    @TableField(exist = false)
    private String groupName;
}

