package com.server.internshipserver.domain.cooperation;

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
 * 企业学校合作申请实体类
 * 用于存储企业向学校申请建立合作关系的申请信息，包括合作类型、合作时间、合作描述等
 * 需要经过学校审核，审核通过后可以建立正式的合作关系
 */
@ApiModel(description = "企业学校合作申请")
@Data
@TableName("enterprise_school_cooperation_apply")
public class EnterpriseSchoolCooperationApply implements Serializable {
    
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
    
    @ApiModelProperty(value = "合作类型（如：实习基地、校企合作、人才培养等）", example = "实习基地")
    @TableField("cooperation_type")
    private String cooperationType;
    
    @ApiModelProperty(value = "合作开始时间")
    @TableField("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @ApiModelProperty(value = "合作结束时间")
    @TableField("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    @ApiModelProperty(value = "合作描述")
    @TableField("cooperation_desc")
    private String cooperationDesc;
    
    @ApiModelProperty(value = "申请状态：0-待审核，1-已通过，2-已拒绝", example = "0")
    @TableField("apply_status")
    private Integer applyStatus;
    
    @ApiModelProperty(value = "审核人ID", example = "1")
    @TableField("auditor_id")
    private Long auditorId;
    
    @ApiModelProperty(value = "审核时间")
    @TableField("audit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;
    
    @ApiModelProperty(value = "审核意见")
    @TableField("audit_opinion")
    private String auditOpinion;
    
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
    
    @ApiModelProperty(value = "学校名称（非数据库字段，用于前端显示）")
    @TableField(exist = false)
    private String schoolName;
    
    @ApiModelProperty(value = "企业名称（非数据库字段，用于前端显示）")
    @TableField(exist = false)
    private String enterpriseName;
}

