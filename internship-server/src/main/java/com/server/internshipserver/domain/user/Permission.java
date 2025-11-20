package com.server.internshipserver.domain.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限信息实体类
 */
@ApiModel(description = "权限信息")
@Data
@TableName("permission_info")
public class Permission implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "权限ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long permissionId;
    
    @ApiModelProperty(value = "权限代码", required = true, example = "system:school:view")
    @TableField("permission_code")
    private String permissionCode;
    
    @ApiModelProperty(value = "权限名称", required = true, example = "查看学校")
    @TableField("permission_name")
    private String permissionName;
    
    @ApiModelProperty(value = "权限类型：menu-菜单，button-按钮，api-接口", example = "api")
    @TableField("permission_type")
    private String permissionType;
    
    @ApiModelProperty(value = "父权限ID", example = "0")
    @TableField("parent_id")
    private Long parentId;
    
    @ApiModelProperty(value = "权限描述", example = "查看学校列表和详情")
    @TableField("permission_desc")
    private String permissionDesc;
    
    @ApiModelProperty(value = "状态：1-启用，0-禁用", example = "1")
    @TableField("status")
    private Integer status;
    
    @ApiModelProperty(value = "删除标志：0-未删除，1-已删除", example = "0")
    @TableField("delete_flag")
    private Integer deleteFlag;
    
    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time", fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

