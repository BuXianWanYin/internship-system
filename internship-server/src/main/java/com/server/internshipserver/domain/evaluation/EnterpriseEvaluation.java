package com.server.internshipserver.domain.evaluation;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 企业评价实体类
 */
@ApiModel(description = "企业评价信息")
@Data
@TableName("enterprise_evaluation")
public class EnterpriseEvaluation implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "评价ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long evaluationId;
    
    @ApiModelProperty(value = "申请ID（关联实习申请）", required = true, example = "1")
    @TableField("apply_id")
    private Long applyId;
    
    @ApiModelProperty(value = "学生ID", required = true, example = "1")
    @TableField("student_id")
    private Long studentId;
    
    @ApiModelProperty(value = "企业ID", required = true, example = "1")
    @TableField("enterprise_id")
    private Long enterpriseId;
    
    @ApiModelProperty(value = "工作态度评分（0-100分）", required = true, example = "85.00")
    @TableField("work_attitude_score")
    private BigDecimal workAttitudeScore;
    
    @ApiModelProperty(value = "专业知识应用评分（0-100分）", required = true, example = "80.00")
    @TableField("knowledge_application_score")
    private BigDecimal knowledgeApplicationScore;
    
    @ApiModelProperty(value = "专业技能评分（0-100分）", required = true, example = "82.00")
    @TableField("professional_skill_score")
    private BigDecimal professionalSkillScore;
    
    @ApiModelProperty(value = "团队协作评分（0-100分）", required = true, example = "88.00")
    @TableField("teamwork_score")
    private BigDecimal teamworkScore;
    
    @ApiModelProperty(value = "创新意识评分（0-100分）", required = true, example = "75.00")
    @TableField("innovation_score")
    private BigDecimal innovationScore;
    
    @ApiModelProperty(value = "日志周报质量评分（0-100分）", required = true, example = "85.00")
    @TableField("log_weekly_report_score")
    private BigDecimal logWeeklyReportScore;
    
    @ApiModelProperty(value = "日志周报质量自动计算分数（参考值）", example = "86.50")
    @TableField("log_weekly_report_score_auto")
    private BigDecimal logWeeklyReportScoreAuto;
    
    @ApiModelProperty(value = "总分（6项指标的平均分）", required = true, example = "82.00")
    @TableField("total_score")
    private BigDecimal totalScore;
    
    @ApiModelProperty(value = "评价意见", example = "该学生工作认真负责，专业能力较强")
    @TableField("evaluation_comment")
    private String evaluationComment;
    
    @ApiModelProperty(value = "评价状态：0-草稿，1-已提交", example = "1")
    @TableField("evaluation_status")
    private Integer evaluationStatus;
    
    @ApiModelProperty(value = "提交时间")
    @TableField("submit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
    
    @ApiModelProperty(value = "评价人ID（企业管理员或企业导师）", example = "1")
    @TableField("evaluator_id")
    private Long evaluatorId;
    
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
    
    @ApiModelProperty(value = "评价人姓名")
    @TableField(exist = false)
    private String evaluatorName;
}

