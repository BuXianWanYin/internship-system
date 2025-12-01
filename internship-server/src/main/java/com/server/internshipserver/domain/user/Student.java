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
 * 学生信息实体类
 */
@ApiModel(description = "学生信息")
@Data
@TableName("student_info")
public class Student implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "学生ID", example = "1")
    @TableId(type = IdType.AUTO)
    private Long studentId;
    
    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    @TableField("user_id")
    private Long userId;
    
    @ApiModelProperty(value = "学号", required = true, example = "202101001")
    @TableField("student_no")
    private String studentNo;
    
    @ApiModelProperty(value = "所属班级ID", required = true, example = "1")
    @TableField("class_id")
    private Long classId;
    
    @ApiModelProperty(value = "入学年份", required = true, example = "2021")
    @TableField("enrollment_year")
    private Integer enrollmentYear;
    
    @ApiModelProperty(value = "毕业年份", example = "2025")
    @TableField("graduation_year")
    private Integer graduationYear;
    
    @ApiModelProperty(value = "所属专业ID（冗余字段）", example = "1")
    @TableField("major_id")
    private Long majorId;
    
    @ApiModelProperty(value = "所属学院ID（冗余字段）", example = "1")
    @TableField("college_id")
    private Long collegeId;
    
    @ApiModelProperty(value = "所属学校ID（冗余字段）", example = "1")
    @TableField("school_id")
    private Long schoolId;
    
    @ApiModelProperty(value = "当前实习申请ID", example = "1")
    @TableField("current_apply_id")
    private Long currentApplyId;
    
    @ApiModelProperty(value = "当前实习企业ID", example = "1")
    @TableField("current_enterprise_id")
    private Long currentEnterpriseId;
    
    @ApiModelProperty(value = "实习状态：0-未实习，1-实习中，3-已结束", example = "0")
    @TableField("internship_status")
    private Integer internshipStatus;
    
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
    
    // ========== 关联字段（不映射到数据库） ==========
    @ApiModelProperty(value = "当前实习企业名称（用于前端显示）")
    @TableField(exist = false)
    private String currentEnterpriseName;
    
    @ApiModelProperty(value = "真实姓名（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String realName;
    
    @ApiModelProperty(value = "手机号（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String phone;
    
    @ApiModelProperty(value = "邮箱（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String email;
    
    @ApiModelProperty(value = "班级名称（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String className;
    
    @ApiModelProperty(value = "专业名称（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String majorName;
    
    @ApiModelProperty(value = "学院名称（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String collegeName;
    
    @ApiModelProperty(value = "状态文字（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String statusText;
    
    @ApiModelProperty(value = "创建时间文字（非数据库字段，用于导出）")
    @TableField(exist = false)
    private String createTimeText;
}

