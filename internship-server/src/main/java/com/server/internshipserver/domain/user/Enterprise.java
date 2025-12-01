package com.server.internshipserver.domain.user;

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
import java.time.LocalDateTime;

/**
 * 企业信息实体类
 * 用于存储企业的详细信息，包括企业名称、统一社会信用代码、法人代表、联系方式等
 * 与企业管理员用户信息通过userId关联，支持企业审核和实习岗位管理
 */
@ApiModel(description = "企业信息")
@Data
@TableName("enterprise_info")
public class Enterprise implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "企业ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long enterpriseId;
    
    @ApiModelProperty(value = "用户ID（企业管理员账号）", example = "1")
    @TableField("user_id")
    private Long userId;
    
    @ApiModelProperty(value = "企业名称", required = true, example = "北京科技有限公司")
    @TableField("enterprise_name")
    private String enterpriseName;
    
    @ApiModelProperty(value = "企业代码（系统内部唯一标识，必填）", required = true, example = "ENT001")
    @TableField("enterprise_code")
    private String enterpriseCode;
    
    @ApiModelProperty(value = "统一社会信用代码", example = "91110000123456789X")
    @TableField("unified_social_credit_code")
    private String unifiedSocialCreditCode;
    
    @ApiModelProperty(value = "法人代表", example = "张总")
    @TableField("legal_person")
    private String legalPerson;
    
    @ApiModelProperty(value = "注册资本（万元）", example = "1000.00")
    @TableField("registered_capital")
    private BigDecimal registeredCapital;
    
    @ApiModelProperty(value = "所属行业", example = "信息技术")
    @TableField("industry")
    private String industry;
    
    @ApiModelProperty(value = "企业地址", example = "北京市海淀区中关村大街1号")
    @TableField("address")
    private String address;
    
    @ApiModelProperty(value = "联系人", example = "张总")
    @TableField("contact_person")
    private String contactPerson;
    
    @ApiModelProperty(value = "联系电话", example = "010-12345678")
    @TableField("contact_phone")
    private String contactPhone;
    
    @ApiModelProperty(value = "联系邮箱", example = "contact@tech.com")
    @TableField("contact_email")
    private String contactEmail;
    
    @ApiModelProperty(value = "企业规模", example = "中型企业")
    @TableField("enterprise_scale")
    private String enterpriseScale;
    
    @ApiModelProperty(value = "经营范围")
    @TableField("business_scope")
    private String businessScope;
    
    @ApiModelProperty(value = "审核状态：0-待审核，1-已通过，2-已拒绝", example = "1")
    @TableField("audit_status")
    private Integer auditStatus;
    
    @ApiModelProperty(value = "审核意见")
    @TableField("audit_opinion")
    private String auditOpinion;
    
    @ApiModelProperty(value = "审核时间")
    @TableField("audit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    
    @ApiModelProperty(value = "审核人ID", example = "1")
    @TableField("auditor_id")
    private Long auditorId;
    
    @ApiModelProperty(value = "状态：1-启用，0-禁用", example = "1")
    @TableField("status")
    private Integer status;
    
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

