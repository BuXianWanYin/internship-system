package com.server.internshipserver.domain.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学校管理员信息实体类
 * 用于存储学校管理员的基本信息，包括所属学校、用户信息等
 * 与学校用户信息通过userId关联，负责管理学校相关的业务
 */
@ApiModel(description = "学校管理员信息")
@Data
@TableName("school_admin_info")
public class SchoolAdmin implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "管理员ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long adminId;
    
    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    @TableField("user_id")
    private Long userId;
    
    @ApiModelProperty(value = "所属学校ID", required = true, example = "1")
    @TableField("school_id")
    private Long schoolId;
    
    @ApiModelProperty(value = "状态：1-启用，0-禁用", example = "1")
    @TableField("status")
    private Integer status;
    
    @ApiModelProperty(value = "删除标志：0-未删除，1-已删除", example = "0")
    @TableField("delete_flag")
    @TableLogic(value = "0", delval = "1")
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

