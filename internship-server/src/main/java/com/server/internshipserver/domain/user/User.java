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
 * 用户基础信息实体类
 */
@ApiModel(description = "用户基础信息")
@Data
@TableName("user_info")
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "用户ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long userId;
    
    @ApiModelProperty(value = "用户名", required = true, example = "admin")
    @TableField("username")
    private String username;
    
    @ApiModelProperty(value = "密码（BCrypt加密）", required = true)
    @TableField("password")
    private String password;
    
    @ApiModelProperty(value = "真实姓名", required = true, example = "张三")
    @TableField("real_name")
    private String realName;
    
    @ApiModelProperty(value = "身份证号", example = "110101199001011234")
    @TableField("id_card")
    private String idCard;
    
    @ApiModelProperty(value = "手机号", example = "13800000001")
    @TableField("phone")
    private String phone;
    
    @ApiModelProperty(value = "邮箱", example = "user@example.com")
    @TableField("email")
    private String email;
    
    @ApiModelProperty(value = "头像URL", example = "/avatar/default.jpg")
    @TableField("avatar")
    private String avatar;
    
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

