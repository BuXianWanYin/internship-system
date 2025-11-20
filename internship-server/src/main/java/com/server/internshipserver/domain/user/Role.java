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
 * 角色信息实体类
 */
@ApiModel(description = "角色信息")
@Data
@TableName("role_info")
public class Role implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "角色ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long roleId;
    
    @ApiModelProperty(value = "角色代码", required = true, example = "ROLE_SYSTEM_ADMIN")
    @TableField("role_code")
    private String roleCode;
    
    @ApiModelProperty(value = "角色名称", required = true, example = "系统管理员")
    @TableField("role_name")
    private String roleName;
    
    @ApiModelProperty(value = "角色描述", example = "系统管理员，拥有最高权限")
    @TableField("role_desc")
    private String roleDesc;
    
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

