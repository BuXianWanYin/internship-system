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
 * 周报实体类
 */
@ApiModel(description = "周报信息")
@Data
@TableName("internship_weekly_report")
public class InternshipWeeklyReport implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "周报ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long reportId;
    
    @ApiModelProperty(value = "学生ID", required = true, example = "1")
    @TableField("student_id")
    private Long studentId;
    
    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    @TableField("user_id")
    private Long userId;
    
    @ApiModelProperty(value = "申请ID（关联实习申请）", required = true, example = "1")
    @TableField("apply_id")
    private Long applyId;
    
    @ApiModelProperty(value = "周次（第几周）", required = true, example = "1")
    @TableField("week_number")
    private Integer weekNumber;
    
    @ApiModelProperty(value = "周报日期", required = true, example = "2024-03-08")
    @TableField("report_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;
    
    @ApiModelProperty(value = "周报标题", required = true, example = "第一周实习周报")
    @TableField("report_title")
    private String reportTitle;
    
    @ApiModelProperty(value = "工作内容（富文本）", required = true, example = "1. 学习了Java基础\n2. 参与了项目开发")
    @TableField("work_content")
    private String workContent;
    
    @ApiModelProperty(value = "附件URL（多个用逗号分隔）", example = "/uploads/report1.pdf")
    @TableField("attachment_urls")
    private String attachmentUrls;
    
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
    
    @ApiModelProperty(value = "批阅意见", example = "周报内容详细，继续努力")
    @TableField("review_comment")
    private String reviewComment;
    
    @ApiModelProperty(value = "批阅评分（0-100）", example = "88.00")
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
    
    @ApiModelProperty(value = "开始日期")
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @ApiModelProperty(value = "结束日期")
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @ApiModelProperty(value = "周开始日期（用于前端显示，等同于startDate）")
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStartDate;
    
    @ApiModelProperty(value = "周结束日期（用于前端显示，等同于endDate）")
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekEndDate;
}

