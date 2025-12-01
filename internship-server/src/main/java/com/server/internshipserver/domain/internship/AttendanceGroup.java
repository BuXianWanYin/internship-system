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
import java.util.List;

/**
 * 考勤组实体类
 * 用于存储企业创建的考勤组配置信息，包括考勤组名称、工作日类型、默认工作时长等
 * 企业导师可以为实习学生创建考勤组，设置工作时间和考勤规则
 */
@ApiModel(description = "考勤组信息")
@Data
@TableName("attendance_group")
public class AttendanceGroup implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "考勤组ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long groupId;
    
    @ApiModelProperty(value = "所属企业ID", required = true, example = "1")
    @TableField("enterprise_id")
    private Long enterpriseId;
    
    @ApiModelProperty(value = "创建人（企业导师ID）", required = true, example = "1")
    @TableField("mentor_id")
    private Long mentorId;
    
    @ApiModelProperty(value = "考勤组名称", required = true, example = "标准5天工作制")
    @TableField("group_name")
    private String groupName;
    
    @ApiModelProperty(value = "考勤组描述", example = "周一至周五，每天8小时")
    @TableField("group_description")
    private String groupDescription;
    
    @ApiModelProperty(value = "工作日类型：1-周一到周五，2-周一到周六，3-周一到周日，4-自定义", example = "1")
    @TableField("work_days_type")
    private Integer workDaysType;
    
    @ApiModelProperty(value = "工作日配置（自定义时使用，JSON格式）", example = "{\"monday\":true,\"tuesday\":true}")
    @TableField("work_days_config")
    private String workDaysConfig;
    
    @ApiModelProperty(value = "默认工作时长（小时）", example = "8.00")
    @TableField("default_work_hours")
    private BigDecimal defaultWorkHours;
    
    @ApiModelProperty(value = "状态：1-启用，0-禁用", example = "1")
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
    
    @ApiModelProperty(value = "更新人ID", example = "1")
    @TableField("update_user_id")
    private Long updateUserId;
    
    // ========== 关联字段（不映射到数据库） ==========
    @ApiModelProperty(value = "时间段列表")
    @TableField(exist = false)
    private List<AttendanceGroupTimeSlot> timeSlots;
    
    @ApiModelProperty(value = "规则列表")
    @TableField(exist = false)
    private List<AttendanceGroupRule> rules;
    
    @ApiModelProperty(value = "学生数量")
    @TableField(exist = false)
    private Integer studentCount;
    
    @ApiModelProperty(value = "时间段数量")
    @TableField(exist = false)
    private Integer timeSlotCount;
    
    @ApiModelProperty(value = "企业名称")
    @TableField(exist = false)
    private String enterpriseName;
    
    @ApiModelProperty(value = "创建人姓名")
    @TableField(exist = false)
    private String createUserName;
}

