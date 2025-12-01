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
import java.util.List;

/**
 * 教师信息实体类
 * 用于存储教师的基本信息，包括工号、所属学院、职称、部门等
 * 与教师用户信息通过userId关联，支持教师管理学生实习过程
 */
@ApiModel(description = "教师信息")
@Data
@TableName("teacher_info")
public class Teacher implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "教师ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long teacherId;
    
    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    @TableField("user_id")
    private Long userId;
    
    @ApiModelProperty(value = "工号", required = true, example = "T2021001")
    @TableField("teacher_no")
    private String teacherNo;
    
    @ApiModelProperty(value = "所属学院ID", example = "1")
    @TableField("college_id")
    private Long collegeId;
    
    @ApiModelProperty(value = "所属学校ID（冗余字段）", example = "1")
    @TableField("school_id")
    private Long schoolId;
    
    @ApiModelProperty(value = "职称", example = "教授")
    @TableField("title")
    private String title;
    
    @ApiModelProperty(value = "所属学院", example = "计算机学院")
    @TableField("department")
    private String department;
    
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
    
    @ApiModelProperty(value = "用户角色代码列表（非数据库字段）")
    @TableField(exist = false)
    private List<String> roles;
    
    @ApiModelProperty(value = "状态文字（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String statusText;
    
    @ApiModelProperty(value = "创建时间文字（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String createTimeText;
    
    @ApiModelProperty(value = "所属学院名称（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String collegeName;
    
    @ApiModelProperty(value = "所属学校名称（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String schoolName;
    
    @ApiModelProperty(value = "教师姓名（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String teacherName;
    
    @ApiModelProperty(value = "联系电话（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String phone;
    
    @ApiModelProperty(value = "邮箱（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String email;
}

