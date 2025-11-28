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
 * 学校评价实体类
 */
@ApiModel(description = "学校评价信息")
@Data
@TableName("school_evaluation")
public class SchoolEvaluation implements Serializable {
    
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
    
    @ApiModelProperty(value = "日志周报质量评分（0-100分）", required = true, example = "86.50")
    @TableField("log_weekly_report_score")
    private BigDecimal logWeeklyReportScore;
    
    @ApiModelProperty(value = "过程表现评分（0-100分）", required = true, example = "85.00")
    @TableField("process_performance_score")
    private BigDecimal processPerformanceScore;
    
    @ApiModelProperty(value = "成果展示评分（0-100分）", required = true, example = "88.00")
    @TableField("achievement_score")
    private BigDecimal achievementScore;
    
    @ApiModelProperty(value = "总结反思评分（0-100分）", required = true, example = "80.00")
    @TableField("summary_reflection_score")
    private BigDecimal summaryReflectionScore;
    
    @ApiModelProperty(value = "总分（4项指标的平均分）", required = true, example = "85.00")
    @TableField("total_score")
    private BigDecimal totalScore;
    
    @ApiModelProperty(value = "评价意见", example = "该学生实习表现良好，各方面都有提升")
    @TableField("evaluation_comment")
    private String evaluationComment;
    
    @ApiModelProperty(value = "评价状态：0-草稿，1-已提交", example = "1")
    @TableField("evaluation_status")
    private Integer evaluationStatus;
    
    @ApiModelProperty(value = "提交时间")
    @TableField("submit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
    
    @ApiModelProperty(value = "评价人ID（教师）", example = "1")
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

