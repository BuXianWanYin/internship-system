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
 * 实习岗位实体类
 */
@ApiModel(description = "实习岗位信息")
@Data
@TableName("internship_post")
public class InternshipPost implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "岗位ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long postId;
    
    @ApiModelProperty(value = "企业ID", required = true, example = "1")
    @TableField("enterprise_id")
    private Long enterpriseId;
    
    @ApiModelProperty(value = "岗位名称", required = true, example = "Java开发实习生")
    @TableField("post_name")
    private String postName;
    
    @ApiModelProperty(value = "岗位编号（企业内唯一）", required = true, example = "POST001")
    @TableField("post_code")
    private String postCode;
    
    @ApiModelProperty(value = "岗位描述（富文本）", example = "负责公司Java后端开发工作，参与项目开发。")
    @TableField("post_description")
    private String postDescription;
    
    @ApiModelProperty(value = "技能要求（富文本）", example = "1. 熟悉Java基础\n2. 了解Spring Boot框架")
    @TableField("skill_requirements")
    private String skillRequirements;
    
    @ApiModelProperty(value = "工作地点", required = true, example = "北京市")
    @TableField("work_location")
    private String workLocation;
    
    @ApiModelProperty(value = "详细地址", example = "北京市海淀区中关村大街1号")
    @TableField("work_address")
    private String workAddress;
    
    @ApiModelProperty(value = "招聘人数", example = "5")
    @TableField("recruit_count")
    private Integer recruitCount;
    
    @ApiModelProperty(value = "已申请人数", example = "0")
    @TableField("applied_count")
    private Integer appliedCount;
    
    @ApiModelProperty(value = "已录用人数", example = "0")
    @TableField("accepted_count")
    private Integer acceptedCount;
    
    @ApiModelProperty(value = "最低薪资（元/月）", example = "3000.00")
    @TableField("salary_min")
    private BigDecimal salaryMin;
    
    @ApiModelProperty(value = "最高薪资（元/月）", example = "5000.00")
    @TableField("salary_max")
    private BigDecimal salaryMax;
    
    @ApiModelProperty(value = "薪资类型（月薪、日薪、时薪、面议）", example = "月薪")
    @TableField("salary_type")
    private String salaryType;
    
    @ApiModelProperty(value = "实习开始日期", example = "2024-03-01")
    @TableField("internship_start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate internshipStartDate;
    
    @ApiModelProperty(value = "实习结束日期", example = "2024-06-30")
    @TableField("internship_end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate internshipEndDate;
    
    @ApiModelProperty(value = "工作时间（如：周一至周五 9:00-18:00）", example = "周一至周五 9:00-18:00")
    @TableField("work_hours")
    private String workHours;
    
    @ApiModelProperty(value = "联系人", example = "张经理")
    @TableField("contact_person")
    private String contactPerson;
    
    @ApiModelProperty(value = "联系电话", example = "13800138000")
    @TableField("contact_phone")
    private String contactPhone;
    
    @ApiModelProperty(value = "联系邮箱", example = "hr@example.com")
    @TableField("contact_email")
    private String contactEmail;
    
    @ApiModelProperty(value = "状态：0-待审核，1-已通过，2-已拒绝，3-已发布，4-已关闭", example = "3")
    @TableField("status")
    private Integer status;
    
    @ApiModelProperty(value = "审核人ID", example = "1")
    @TableField("audit_user_id")
    private Long auditUserId;
    
    @ApiModelProperty(value = "审核时间")
    @TableField("audit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    
    @ApiModelProperty(value = "审核意见", example = "审核通过")
    @TableField("audit_opinion")
    private String auditOpinion;
    
    @ApiModelProperty(value = "发布时间")
    @TableField("publish_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
    
    @ApiModelProperty(value = "关闭时间")
    @TableField("close_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closeTime;
    
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
}

