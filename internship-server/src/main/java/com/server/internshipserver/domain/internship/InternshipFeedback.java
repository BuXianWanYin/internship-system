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
 * 问题反馈实体类
 */
@ApiModel(description = "问题反馈信息")
@Data
@TableName("internship_feedback")
public class InternshipFeedback implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "反馈ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long feedbackId;
    
    @ApiModelProperty(value = "学生ID", required = true, example = "1")
    @TableField("student_id")
    private Long studentId;
    
    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    @TableField("user_id")
    private Long userId;
    
    @ApiModelProperty(value = "申请ID（关联实习申请）", required = true, example = "1")
    @TableField("apply_id")
    private Long applyId;
    
    @ApiModelProperty(value = "反馈类型：1-问题反馈，2-建议反馈，3-求助反馈", example = "1")
    @TableField("feedback_type")
    private Integer feedbackType;
    
    @ApiModelProperty(value = "反馈标题", required = true, example = "关于项目开发的问题")
    @TableField("feedback_title")
    private String feedbackTitle;
    
    @ApiModelProperty(value = "反馈内容（富文本）", required = true, example = "在开发过程中遇到了...")
    @TableField("feedback_content")
    private String feedbackContent;
    
    @ApiModelProperty(value = "附件URL（多个用逗号分隔）", example = "/uploads/issue1.jpg")
    @TableField("attachment_urls")
    private String attachmentUrls;
    
    @ApiModelProperty(value = "反馈状态：0-待处理，1-处理中，2-已解决，3-已关闭", example = "0")
    @TableField("feedback_status")
    private Integer feedbackStatus;
    
    @ApiModelProperty(value = "回复人ID（教师或导师）", example = "1")
    @TableField("reply_user_id")
    private Long replyUserId;
    
    @ApiModelProperty(value = "回复人类型：1-指导教师，2-企业导师", example = "1")
    @TableField("reply_user_type")
    private Integer replyUserType;
    
    @ApiModelProperty(value = "回复内容（富文本）", example = "建议你参考以下资料...")
    @TableField("reply_content")
    private String replyContent;
    
    @ApiModelProperty(value = "回复时间")
    @TableField("reply_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime replyTime;
    
    @ApiModelProperty(value = "解决时间")
    @TableField("solve_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime solveTime;
    
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
    
    @ApiModelProperty(value = "企业名称")
    @TableField(exist = false)
    private String enterpriseName;
}

