package com.server.internshipserver.domain.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.server.internshipserver.domain.user.UserInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学校信息实体类
 * 用于存储学校的基本信息，包括学校名称、学校代码、地址、联系方式等
 * 是系统组织架构的顶层单位，下辖学院、专业、班级等
 */
@ApiModel(description = "学校信息")
@Data
@TableName("school_info")
public class School implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "学校ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long schoolId;
    
    @ApiModelProperty(value = "学校名称", required = true, example = "北京理工大学")
    @TableField("school_name")
    private String schoolName;
    
    @ApiModelProperty(value = "学校代码", required = true, example = "BIT001")
    @TableField("school_code")
    private String schoolCode;
    
    @ApiModelProperty(value = "地址", example = "北京市海淀区中关村南大街5号")
    @TableField("address")
    private String address;
    
    @ApiModelProperty(value = "联系人", example = "张校长")
    @TableField("contact_person")
    private String contactPerson;
    
    @ApiModelProperty(value = "联系电话", example = "010-68912345")
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
    
    @ApiModelProperty(value = "负责人姓名（非数据库字段，从学校管理员中获取）")
    @TableField(exist = false)
    private String managerName;
    
    @ApiModelProperty(value = "负责人电话（非数据库字段，从学校管理员中获取）")
    @TableField(exist = false)
    private String managerPhone;
    
    @ApiModelProperty(value = "学校管理员用户ID（非数据库字段，用于新增/编辑时绑定管理员）")
    @TableField(exist = false)
    private Long managerUserId;
    
    @ApiModelProperty(value = "是否创建新用户（非数据库字段，用于新增/编辑时创建新管理员用户）")
    @TableField(exist = false)
    private Boolean createNewUser;
    
    @ApiModelProperty(value = "新用户信息（非数据库字段，用于创建新管理员用户）")
    @TableField(exist = false)
    private UserInfo newUserInfo;
    
    @ApiModelProperty(value = "状态文字（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String statusText;
    
    @ApiModelProperty(value = "创建时间文字（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String createTimeText;
}

