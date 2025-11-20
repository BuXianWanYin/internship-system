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
 * 角色权限关联实体类
 */
@ApiModel(description = "角色权限关联")
@Data
@TableName("role_permission")
public class RolePermission implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "关联ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long rolePermissionId;
    
    @ApiModelProperty(value = "角色ID", required = true, example = "1")
    @TableField("role_id")
    private Long roleId;
    
    @ApiModelProperty(value = "权限ID", required = true, example = "1")
    @TableField("permission_id")
    private Long permissionId;
    
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

