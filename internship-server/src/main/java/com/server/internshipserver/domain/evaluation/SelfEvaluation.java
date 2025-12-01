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
 * 学生自评实体类
 * 用于存储学生对自己实习过程的自评信息，包括自评分数、自我反思和总结等
 * 学生可以在实习结束后提交自评，作为综合成绩计算的组成部分
 */
@ApiModel(description = "学生自评信息")
@Data
@TableName("self_evaluation")
public class SelfEvaluation implements Serializable {
    
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
    
    @ApiModelProperty(value = "自评分数（0-100分）", required = true, example = "90.00")
    @TableField("self_score")
    private BigDecimal selfScore;
    
    @ApiModelProperty(value = "自我反思和总结（富文本）", example = "通过这次实习，我学到了很多...")
    @TableField("reflection_summary")
    private String reflectionSummary;
    
    @ApiModelProperty(value = "评价状态：0-草稿，1-已提交", example = "1")
    @TableField("evaluation_status")
    private Integer evaluationStatus;
    
    @ApiModelProperty(value = "提交时间")
    @TableField("submit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
    
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
}

