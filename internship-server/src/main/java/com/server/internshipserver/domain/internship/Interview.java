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
import java.time.LocalDateTime;

/**
 * 面试实体类
 */
@ApiModel(description = "面试信息")
@Data
@TableName("interview")
public class Interview implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "面试ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long interviewId;
    
    @ApiModelProperty(value = "申请ID", required = true, example = "1")
    @TableField("apply_id")
    private Long applyId;
    
    @ApiModelProperty(value = "企业ID", required = true, example = "1")
    @TableField("enterprise_id")
    private Long enterpriseId;
    
    @ApiModelProperty(value = "学生ID", required = true, example = "1")
    @TableField("student_id")
    private Long studentId;
    
    @ApiModelProperty(value = "面试类型：1-现场面试，2-视频面试，3-电话面试", example = "1")
    @TableField("interview_type")
    private Integer interviewType;
    
    @ApiModelProperty(value = "面试时间", required = true, example = "2024-03-15 14:00:00")
    @TableField("interview_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime interviewTime;
    
    @ApiModelProperty(value = "面试地点（现场面试时必填）", example = "北京市海淀区中关村大街1号")
    @TableField("interview_location")
    private String interviewLocation;
    
    @ApiModelProperty(value = "面试链接（视频面试时必填）", example = "https://meeting.example.com/123456")
    @TableField("interview_link")
    private String interviewLink;
    
    @ApiModelProperty(value = "面试电话（电话面试时必填）", example = "13800138000")
    @TableField("interview_phone")
    private String interviewPhone;
    
    @ApiModelProperty(value = "面试官（多个用逗号分隔）", example = "张经理,李主管")
    @TableField("interviewer")
    private String interviewer;
    
    @ApiModelProperty(value = "面试内容/要求", example = "请准备个人简历和作品集")
    @TableField("interview_content")
    private String interviewContent;
    
    @ApiModelProperty(value = "学生确认：0-未确认，1-已确认，2-已拒绝", example = "0")
    @TableField("student_confirm")
    private Integer studentConfirm;
    
    @ApiModelProperty(value = "学生确认时间")
    @TableField("student_confirm_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime studentConfirmTime;
    
    @ApiModelProperty(value = "面试结果：1-通过，2-不通过，3-待定", example = "1")
    @TableField("interview_result")
    private Integer interviewResult;
    
    @ApiModelProperty(value = "面试评价", example = "技术能力较强，沟通能力良好")
    @TableField("interview_comment")
    private String interviewComment;
    
    @ApiModelProperty(value = "面试反馈时间")
    @TableField("interview_feedback_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime interviewFeedbackTime;
    
    @ApiModelProperty(value = "状态：0-待确认，1-已确认，2-已完成，3-已取消", example = "0")
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
    
    // ========== 关联字段（不映射到数据库） ==========
    @ApiModelProperty(value = "企业名称")
    @TableField(exist = false)
    private String enterpriseName;
    
    @ApiModelProperty(value = "岗位名称")
    @TableField(exist = false)
    private String postName;
    
    @ApiModelProperty(value = "学生姓名")
    @TableField(exist = false)
    private String studentName;
    
    @ApiModelProperty(value = "学号")
    @TableField(exist = false)
    private String studentNo;
    
    // ========== 前端字段映射（不映射到数据库） ==========
    @ApiModelProperty(value = "视频链接（前端字段，映射到interviewLink）")
    @TableField(exist = false)
    private String videoLink;
    
    @ApiModelProperty(value = "面试说明（前端字段，映射到interviewContent）")
    @TableField(exist = false)
    private String interviewDescription;
    
    @ApiModelProperty(value = "联系人（前端字段，映射到interviewer）")
    @TableField(exist = false)
    private String contactPerson;
    
    @ApiModelProperty(value = "联系电话（前端字段，映射到interviewPhone）")
    @TableField(exist = false)
    private String contactPhone;
}

