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
 * 企业导师信息实体类
 */
@ApiModel(description = "企业导师信息")
@Data
@TableName("enterprise_mentor_info")
public class EnterpriseMentor implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "企业导师ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long mentorId;
    
    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    @TableField("user_id")
    private Long userId;
    
    @ApiModelProperty(value = "所属企业ID", required = true, example = "1")
    @TableField("enterprise_id")
    private Long enterpriseId;
    
    @ApiModelProperty(value = "导师姓名", required = true, example = "导师一")
    @TableField("mentor_name")
    private String mentorName;
    
    @ApiModelProperty(value = "职位", example = "技术总监")
    @TableField("position")
    private String position;
    
    @ApiModelProperty(value = "部门", example = "技术部")
    @TableField("department")
    private String department;
    
    @ApiModelProperty(value = "联系电话", example = "13700000001")
    @TableField("phone")
    private String phone;
    
    @ApiModelProperty(value = "邮箱", example = "mentor@example.com")
    @TableField("email")
    private String email;
    
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

