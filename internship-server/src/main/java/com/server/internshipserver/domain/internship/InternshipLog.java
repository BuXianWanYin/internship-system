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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 实习日志实体类
 */
@ApiModel(description = "实习日志信息")
@Data
@TableName("internship_log")
public class InternshipLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "日志ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long logId;
    
    @ApiModelProperty(value = "学生ID", required = true, example = "1")
    @TableField("student_id")
    private Long studentId;
    
    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    @TableField("user_id")
    private Long userId;
    
    @ApiModelProperty(value = "申请ID（关联实习申请）", required = true, example = "1")
    @TableField("apply_id")
    private Long applyId;
    
    @ApiModelProperty(value = "关联实习计划ID", example = "1")
    @TableField("plan_id")
    private Long planId;
    
    @ApiModelProperty(value = "日志日期", required = true, example = "2024-03-01")
    @TableField("log_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate logDate;
    
    @ApiModelProperty(value = "日志标题", required = true, example = "第一天实习日志")
    @TableField("log_title")
    private String logTitle;
    
    @ApiModelProperty(value = "日志内容（富文本）", required = true, example = "今天学习了Java基础语法...")
    @TableField("log_content")
    private String logContent;
    
    @ApiModelProperty(value = "附件URL（多个用逗号分隔）", example = "/uploads/file1.pdf,/uploads/file2.jpg")
    @TableField("attachment_urls")
    private String attachmentUrls;
    
    @ApiModelProperty(value = "工作时长（小时）", example = "8.00")
    @TableField("work_hours")
    private BigDecimal workHours;
    
    @ApiModelProperty(value = "指导教师ID", example = "1")
    @TableField("instructor_id")
    private Long instructorId;
    
    @ApiModelProperty(value = "批阅状态：0-未批阅，1-已批阅", example = "0")
    @TableField("review_status")
    private Integer reviewStatus;
    
    @ApiModelProperty(value = "批阅时间")
    @TableField("review_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;
    
    @ApiModelProperty(value = "批阅意见", example = "日志内容详细，继续努力")
    @TableField("review_comment")
    private String reviewComment;
    
    @ApiModelProperty(value = "批阅评分（0-100）", example = "85.00")
    @TableField("review_score")
    private BigDecimal reviewScore;
    
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
    
    @ApiModelProperty(value = "实习计划名称（用于前端显示）")
    @TableField(exist = false)
    private String planName;
    
    @ApiModelProperty(value = "申请类型：1-合作企业，2-自主实习")
    @TableField(exist = false)
    private Integer applyType;
}

