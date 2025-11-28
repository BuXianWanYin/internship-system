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
 * 综合成绩实体类
 */
@ApiModel(description = "综合成绩信息")
@Data
@TableName("comprehensive_score")
public class ComprehensiveScore implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "成绩ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long scoreId;
    
    @ApiModelProperty(value = "申请ID（关联实习申请）", required = true, example = "1")
    @TableField("apply_id")
    private Long applyId;
    
    @ApiModelProperty(value = "学生ID", required = true, example = "1")
    @TableField("student_id")
    private Long studentId;
    
    @ApiModelProperty(value = "企业评价分数", example = "82.00")
    @TableField("enterprise_score")
    private BigDecimal enterpriseScore;
    
    @ApiModelProperty(value = "学校评价分数", example = "85.00")
    @TableField("school_score")
    private BigDecimal schoolScore;
    
    @ApiModelProperty(value = "学生自评分数", example = "90.00")
    @TableField("self_score")
    private BigDecimal selfScore;
    
    @ApiModelProperty(value = "综合成绩（企业40% + 学校40% + 自评20%）", required = true, example = "84.80")
    @TableField("comprehensive_score")
    private BigDecimal comprehensiveScore;
    
    @ApiModelProperty(value = "等级：优秀、良好、中等、及格、不及格", example = "良好")
    @TableField("grade_level")
    private String gradeLevel;
    
    @ApiModelProperty(value = "计算时间", required = true)
    @TableField("calculate_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime calculateTime;
    
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

