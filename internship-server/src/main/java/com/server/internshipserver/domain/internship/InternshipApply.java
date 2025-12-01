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
 * 实习申请实体类
 */
@ApiModel(description = "实习申请信息")
@Data
@TableName("internship_apply")
public class InternshipApply implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "申请ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long applyId;
    
    @ApiModelProperty(value = "学生ID", required = true, example = "1")
    @TableField("student_id")
    private Long studentId;
    
    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    @TableField("user_id")
    private Long userId;
    
    @ApiModelProperty(value = "关联实习计划ID", example = "1")
    @TableField("plan_id")
    private Long planId;
    
    @ApiModelProperty(value = "申请类型：1-合作企业，2-自主实习", example = "1")
    @TableField("apply_type")
    private Integer applyType;
    
    @ApiModelProperty(value = "企业ID（仅合作企业申请使用，apply_type=1时必填）", example = "1")
    @TableField("enterprise_id")
    private Long enterpriseId;
    
    @ApiModelProperty(value = "岗位ID（仅合作企业申请使用，apply_type=1时可选）", example = "1")
    @TableField("post_id")
    private Long postId;
    
    @ApiModelProperty(value = "自主实习企业名称", example = "北京科技有限公司")
    @TableField("self_enterprise_name")
    private String selfEnterpriseName;
    
    @ApiModelProperty(value = "自主实习企业地址", example = "北京市海淀区中关村大街1号")
    @TableField("self_enterprise_address")
    private String selfEnterpriseAddress;
    
    @ApiModelProperty(value = "自主实习统一社会信用代码", example = "91110000123456789X")
    @TableField("self_unified_social_credit_code")
    private String selfUnifiedSocialCreditCode;
    
    @ApiModelProperty(value = "自主实习所属行业", example = "信息技术")
    @TableField("self_industry")
    private String selfIndustry;
    
    @ApiModelProperty(value = "自主实习法人代表", example = "张总")
    @TableField("self_legal_person")
    private String selfLegalPerson;
    
    @ApiModelProperty(value = "自主实习联系人", example = "李经理")
    @TableField("self_contact_person")
    private String selfContactPerson;
    
    @ApiModelProperty(value = "自主实习联系电话", example = "13800138000")
    @TableField("self_contact_phone")
    private String selfContactPhone;
    
    @ApiModelProperty(value = "自主实习企业性质", example = "民营企业")
    @TableField("self_enterprise_nature")
    private String selfEnterpriseNature;
    
    @ApiModelProperty(value = "自主实习岗位名称", example = "软件开发实习生")
    @TableField("self_post_name")
    private String selfPostName;
    
    @ApiModelProperty(value = "自主实习开始日期", example = "2024-03-01")
    @TableField("self_start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate selfStartDate;
    
    @ApiModelProperty(value = "自主实习结束日期", example = "2024-06-30")
    @TableField("self_end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate selfEndDate;
    
    @ApiModelProperty(value = "自主实习说明", example = "希望通过实习提升实践能力")
    @TableField("self_description")
    private String selfDescription;
    
    @ApiModelProperty(value = "简历附件URL（多个用逗号分隔）", example = "/uploads/resume1.pdf,/uploads/resume2.pdf")
    @TableField("resume_attachment")
    private String resumeAttachment;
    
    @ApiModelProperty(value = "申请理由", example = "希望在该企业实习，提升Java开发能力")
    @TableField("apply_reason")
    private String applyReason;
    
    @ApiModelProperty(value = "状态：0-待审核，1-已通过，2-已拒绝，3-已录用，4-已拒绝录用，5-已取消", example = "1")
    @TableField("status")
    private Integer status;
    
    @ApiModelProperty(value = "审核人ID（班主任或学校管理员）", example = "1")
    @TableField("audit_user_id")
    private Long auditUserId;
    
    @ApiModelProperty(value = "审核时间")
    @TableField("audit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    
    @ApiModelProperty(value = "审核意见", example = "审核通过")
    @TableField("audit_opinion")
    private String auditOpinion;
    
    @ApiModelProperty(value = "企业反馈（仅合作企业申请使用）", example = "该学生表现优秀，同意录用")
    @TableField("enterprise_feedback")
    private String enterpriseFeedback;
    
    @ApiModelProperty(value = "企业反馈时间（仅合作企业申请使用）")
    @TableField("enterprise_feedback_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime enterpriseFeedbackTime;
    
    @ApiModelProperty(value = "面试时间（仅合作企业申请使用）")
    @TableField("interview_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime interviewTime;
    
    @ApiModelProperty(value = "面试地点（仅合作企业申请使用）", example = "北京市海淀区中关村大街1号")
    @TableField("interview_location")
    private String interviewLocation;
    
    @ApiModelProperty(value = "面试结果：1-通过，2-不通过（仅合作企业申请使用）", example = "1")
    @TableField("interview_result")
    private Integer interviewResult;
    
    @ApiModelProperty(value = "面试评价（仅合作企业申请使用）", example = "技术能力较强，沟通能力良好")
    @TableField("interview_comment")
    private String interviewComment;
    
    @ApiModelProperty(value = "录用时间（仅合作企业申请使用）")
    @TableField("accept_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptTime;
    
    @ApiModelProperty(value = "学生确认状态：0-未确认，1-已确认上岗，2-已拒绝", example = "0")
    @TableField("student_confirm_status")
    private Integer studentConfirmStatus;
    
    @ApiModelProperty(value = "学生确认时间")
    @TableField("student_confirm_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime studentConfirmTime;
    
    @ApiModelProperty(value = "解绑状态：0-未解绑，2-已解绑", example = "0")
    @TableField("unbind_status")
    private Integer unbindStatus;
    
    @ApiModelProperty(value = "解绑原因", example = "个人原因需要离职")
    @TableField("unbind_reason")
    private String unbindReason;
    
    @ApiModelProperty(value = "解绑操作人ID（班主任/管理员）", example = "1")
    @TableField("unbind_audit_user_id")
    private Long unbindAuditUserId;
    
    @ApiModelProperty(value = "解绑时间")
    @TableField("unbind_audit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime unbindAuditTime;
    
    @ApiModelProperty(value = "解绑备注", example = "学生离职，已与企业沟通")
    @TableField("unbind_audit_opinion")
    private String unbindAuditOpinion;
    
    @ApiModelProperty(value = "实习开始日期", example = "2024-03-01")
    @TableField("internship_start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate internshipStartDate;
    
    @ApiModelProperty(value = "实习结束日期", example = "2024-06-30")
    @TableField("internship_end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate internshipEndDate;
    
    @ApiModelProperty(value = "企业导师ID", example = "1")
    @TableField("mentor_id")
    private Long mentorId;
    
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
    @ApiModelProperty(value = "学生姓名")
    @TableField(exist = false)
    private String studentName;
    
    @ApiModelProperty(value = "学号")
    @TableField(exist = false)
    private String studentNo;
    
    @ApiModelProperty(value = "企业名称")
    @TableField(exist = false)
    private String enterpriseName;
    
    @ApiModelProperty(value = "岗位名称")
    @TableField(exist = false)
    private String postName;
    
    @ApiModelProperty(value = "审核人姓名")
    @TableField(exist = false)
    private String auditorName;
    
    @ApiModelProperty(value = "企业地址（用于前端显示）")
    @TableField(exist = false)
    private String enterpriseAddress;
    
    @ApiModelProperty(value = "联系人（用于前端显示）")
    @TableField(exist = false)
    private String contactPerson;
    
    @ApiModelProperty(value = "联系电话（用于前端显示）")
    @TableField(exist = false)
    private String contactPhone;
    
    @ApiModelProperty(value = "企业导师姓名（用于前端显示）")
    @TableField(exist = false)
    private String mentorName;
    
    @ApiModelProperty(value = "实习计划名称（用于前端显示）")
    @TableField(exist = false)
    private String planName;
    
    @ApiModelProperty(value = "实习计划编号（用于前端显示）")
    @TableField(exist = false)
    private String planCode;
    
    @ApiModelProperty(value = "学生实习状态：0-未实习，1-实习中，3-已结束（用于前端显示）")
    @TableField(exist = false)
    private Integer studentInternshipStatus;
    
    // ========== 状态流转相关字段（不映射到数据库） ==========
    @ApiModelProperty(value = "状态流转历史")
    @TableField(exist = false)
    private java.util.List<StatusHistoryItem> statusHistory;
    
    @ApiModelProperty(value = "下一步操作提示")
    @TableField(exist = false)
    private String nextActionTip;
    
    @ApiModelProperty(value = "状态文本（用于前端显示，根据状态和面试情况动态生成）")
    @TableField(exist = false)
    private String statusText;
    
    @ApiModelProperty(value = "是否有面试记录")
    @TableField(exist = false)
    private Boolean hasInterview;
    
    @ApiModelProperty(value = "最新面试记录")
    @TableField(exist = false)
    private Interview latestInterview;
    
    @ApiModelProperty(value = "申请类型文字（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String applyTypeText;
    
    @ApiModelProperty(value = "创建时间文字（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String createTimeText;
    
    /**
     * 状态流转历史项
     */
    @Data
    public static class StatusHistoryItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "操作名称", example = "申请提交")
        private String actionName;
        
        @ApiModelProperty(value = "操作时间")
        private LocalDateTime actionTime;
        
        @ApiModelProperty(value = "操作人", example = "张三")
        private String operator;
        
        @ApiModelProperty(value = "操作说明", example = "学生提交了实习申请")
        private String description;
        
        @ApiModelProperty(value = "状态值", example = "0")
        private Integer status;
    }
}

