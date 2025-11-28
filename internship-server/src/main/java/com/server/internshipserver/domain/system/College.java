package com.server.internshipserver.domain.system;

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
 * 学院信息实体类
 */
@ApiModel(description = "学院信息")
@Data
@TableName("college_info")
public class College implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "学院ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long collegeId;
    
    @ApiModelProperty(value = "学院名称", required = true, example = "计算机学院")
    @TableField("college_name")
    private String collegeName;
    
    @ApiModelProperty(value = "学院代码", required = true, example = "CS001")
    @TableField("college_code")
    private String collegeCode;
    
    @ApiModelProperty(value = "所属学校ID", required = true, example = "1")
    @TableField("school_id")
    private Long schoolId;
    
    @ApiModelProperty(value = "院长用户ID", example = "1")
    @TableField("dean_user_id")
    private Long deanUserId;
    
    @ApiModelProperty(value = "院长姓名（用于显示）", example = "刘院长")
    @TableField("dean_name")
    private String deanName;
    
    @ApiModelProperty(value = "联系电话", example = "010-68912346")
    @TableField("contact_phone")
    private String contactPhone;
    
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
    
    @ApiModelProperty(value = "状态文字（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String statusText;
    
    @ApiModelProperty(value = "创建时间文字（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String createTimeText;
    
    @ApiModelProperty(value = "所属学校名称（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String schoolName;
}

