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
 * 实习计划实体类
 */
@ApiModel(description = "实习计划信息")
@Data
@TableName("internship_plan")
public class InternshipPlan implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "计划ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long planId;
    
    @ApiModelProperty(value = "实习计划名称", required = true, example = "2024年春季生产实习计划")
    @TableField("plan_name")
    private String planName;
    
    @ApiModelProperty(value = "实习计划编号（唯一）", required = true, example = "PLAN2024001")
    @TableField("plan_code")
    private String planCode;
    
    @ApiModelProperty(value = "关联学期ID", example = "1")
    @TableField("semester_id")
    private Long semesterId;
    
    @ApiModelProperty(value = "所属学校ID", required = true, example = "1")
    @TableField("school_id")
    private Long schoolId;
    
    @ApiModelProperty(value = "所属学院ID（可选，NULL表示全校）", example = "1")
    @TableField("college_id")
    private Long collegeId;
    
    @ApiModelProperty(value = "所属专业ID（可选，NULL表示全院）", example = "1")
    @TableField("major_id")
    private Long majorId;
    
    @ApiModelProperty(value = "实习类型（生产实习、毕业实习等）", required = true, example = "生产实习")
    @TableField("plan_type")
    private String planType;
    
    @ApiModelProperty(value = "实习开始日期", required = true, example = "2024-03-01")
    @TableField("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @ApiModelProperty(value = "实习结束日期", required = true, example = "2024-06-30")
    @TableField("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @ApiModelProperty(value = "实习大纲（富文本）", example = "本次生产实习旨在让学生深入了解企业实际工作流程，提升实践能力。")
    @TableField("plan_outline")
    private String planOutline;
    
    @ApiModelProperty(value = "任务要求（富文本）", example = "1. 完成企业分配的工作任务\n2. 每周提交实习日志")
    @TableField("task_requirements")
    private String taskRequirements;
    
    @ApiModelProperty(value = "考核标准（富文本）", example = "1. 工作态度（30%）\n2. 工作能力（40%）")
    @TableField("assessment_standards")
    private String assessmentStandards;
    
    @ApiModelProperty(value = "状态：0-草稿，1-待审核，2-已通过，3-已拒绝，4-已发布", example = "4")
    @TableField("status")
    private Integer status;
    
    @ApiModelProperty(value = "审核人ID", example = "1")
    @TableField("audit_user_id")
    private Long auditUserId;
    
    @ApiModelProperty(value = "审核时间")
    @TableField("audit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    
    @ApiModelProperty(value = "审核意见", example = "审核通过")
    @TableField("audit_opinion")
    private String auditOpinion;
    
    @ApiModelProperty(value = "发布时间")
    @TableField("publish_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
    
    @ApiModelProperty(value = "创建人ID", required = true, example = "1")
    @TableField("create_user_id")
    private Long createUserId;
    
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
    
    // ========== 关联字段（不映射到数据库） ==========
    @ApiModelProperty(value = "学期名称")
    @TableField(exist = false)
    private String semesterName;
    
    @ApiModelProperty(value = "所属学校名称")
    @TableField(exist = false)
    private String schoolName;
    
    @ApiModelProperty(value = "所属学院名称")
    @TableField(exist = false)
    private String collegeName;
    
    @ApiModelProperty(value = "所属专业名称")
    @TableField(exist = false)
    private String majorName;
}

