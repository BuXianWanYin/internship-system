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
import java.time.LocalDateTime;

/**
 * 企业注册申请院校关联实体类
 */
@ApiModel(description = "企业注册申请院校关联")
@Data
@TableName("enterprise_register_school")
public class EnterpriseRegisterSchool implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "主键ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @ApiModelProperty(value = "企业ID", required = true, example = "1")
    @TableField("enterprise_id")
    private Long enterpriseId;
    
    @ApiModelProperty(value = "学校ID", required = true, example = "1")
    @TableField("school_id")
    private Long schoolId;
    
    @ApiModelProperty(value = "审核状态：0-待审核，1-已通过，2-已拒绝", example = "0")
    @TableField("audit_status")
    private Integer auditStatus;
    
    @ApiModelProperty(value = "审核意见")
    @TableField("audit_opinion")
    private String auditOpinion;
    
    @ApiModelProperty(value = "审核人ID", example = "1")
    @TableField("auditor_id")
    private Long auditorId;
    
    @ApiModelProperty(value = "审核时间")
    @TableField("audit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    
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

