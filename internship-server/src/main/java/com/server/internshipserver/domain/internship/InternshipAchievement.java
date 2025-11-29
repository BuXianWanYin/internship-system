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
 * 阶段性成果实体类
 */
@ApiModel(description = "阶段性成果信息")
@Data
@TableName("internship_achievement")
public class InternshipAchievement implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "成果ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long achievementId;
    
    @ApiModelProperty(value = "学生ID", required = true, example = "1")
    @TableField("student_id")
    private Long studentId;
    
    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    @TableField("user_id")
    private Long userId;
    
    @ApiModelProperty(value = "申请ID（关联实习申请）", required = true, example = "1")
    @TableField("apply_id")
    private Long applyId;
    
    @ApiModelProperty(value = "成果名称", required = true, example = "用户管理系统")
    @TableField("achievement_name")
    private String achievementName;
    
    @ApiModelProperty(value = "成果类型（项目文档、工作成果、学习笔记等）", required = true, example = "项目文档")
    @TableField("achievement_type")
    private String achievementType;
    
    @ApiModelProperty(value = "成果描述（富文本）", example = "完成了用户管理系统的开发，包括用户增删改查功能")
    @TableField("achievement_description")
    private String achievementDescription;
    
    @ApiModelProperty(value = "文件URL（多个用逗号分隔）", required = true, example = "/uploads/project1.zip,/uploads/doc1.pdf")
    @TableField("file_urls")
    private String fileUrls;
    
    @ApiModelProperty(value = "提交日期", required = true, example = "2024-03-15")
    @TableField("submit_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate submitDate;
    
    @ApiModelProperty(value = "指导教师ID", example = "1")
    @TableField("instructor_id")
    private Long instructorId;
    
    @ApiModelProperty(value = "审核状态：0-待审核，1-已通过，2-已拒绝", example = "0")
    @TableField("review_status")
    private Integer reviewStatus;
    
    @ApiModelProperty(value = "审核时间")
    @TableField("review_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;
    
    @ApiModelProperty(value = "审核意见", example = "成果质量较高，继续努力")
    @TableField("review_comment")
    private String reviewComment;
    
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
    
    @ApiModelProperty(value = "成果标题（用于显示）")
    @TableField(exist = false)
    private String achievementTitle;
    
    @ApiModelProperty(value = "申请类型：1-合作企业，2-自主实习")
    @TableField(exist = false)
    private Integer applyType;
}

