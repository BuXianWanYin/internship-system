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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤实体类
 */
@ApiModel(description = "考勤信息")
@Data
@TableName("attendance")
public class Attendance implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "考勤ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long attendanceId;
    
    @ApiModelProperty(value = "学生ID", required = true, example = "1")
    @TableField("student_id")
    private Long studentId;
    
    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    @TableField("user_id")
    private Long userId;
    
    @ApiModelProperty(value = "申请ID（关联实习申请）", required = true, example = "1")
    @TableField("apply_id")
    private Long applyId;
    
    @ApiModelProperty(value = "考勤组ID", example = "1")
    @TableField("group_id")
    private Long groupId;
    
    @ApiModelProperty(value = "时间段ID", example = "1")
    @TableField("time_slot_id")
    private Long timeSlotId;
    
    @ApiModelProperty(value = "考勤日期", required = true, example = "2024-03-01")
    @TableField("attendance_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate attendanceDate;
    
    @ApiModelProperty(value = "考勤类型：1-出勤，2-迟到，3-早退，4-请假，5-缺勤，6-休息", example = "1")
    @TableField("attendance_type")
    private Integer attendanceType;
    
    @ApiModelProperty(value = "签到时间", example = "2024-03-01 09:00:00")
    @TableField("check_in_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;
    
    @ApiModelProperty(value = "签退时间", example = "2024-03-01 18:00:00")
    @TableField("check_out_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkOutTime;
    
    @ApiModelProperty(value = "工作时长（小时）", example = "8.00")
    @TableField("work_hours")
    private BigDecimal workHours;
    
    @ApiModelProperty(value = "请假原因（请假时必填）", example = "身体不适")
    @TableField("leave_reason")
    private String leaveReason;
    
    @ApiModelProperty(value = "请假类型（事假、病假、调休等）", example = "病假")
    @TableField("leave_type")
    private String leaveType;
    
    @ApiModelProperty(value = "确认人ID（企业导师或企业管理员）", example = "1")
    @TableField("confirm_user_id")
    private Long confirmUserId;
    
    @ApiModelProperty(value = "确认时间")
    @TableField("confirm_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;
    
    @ApiModelProperty(value = "确认状态：0-待确认，1-已确认，2-已拒绝", example = "0")
    @TableField("confirm_status")
    private Integer confirmStatus;
    
    @ApiModelProperty(value = "确认意见", example = "确认出勤")
    @TableField("confirm_comment")
    private String confirmComment;
    
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
    
    // 非数据库字段：用于前端显示
    @ApiModelProperty(value = "学生姓名")
    @TableField(exist = false)
    private String studentName;
    
    @ApiModelProperty(value = "学号")
    @TableField(exist = false)
    private String studentNo;
}

