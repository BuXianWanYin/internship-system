package com.server.internshipserver.domain.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 教师添加DTO
 * 用于封装添加教师时的所有信息，包括教师基本信息、用户信息和角色信息
 * 系统会自动创建对应的用户账号并分配角色
 */
@ApiModel(description = "教师添加信息")
@Data
public class TeacherAddDTO {
    
    @ApiModelProperty(value = "工号", required = true, example = "T2021001")
    private String teacherNo;
    
    @ApiModelProperty(value = "真实姓名", required = true, example = "张老师")
    private String realName;
    
    @ApiModelProperty(value = "身份证号", example = "110101199001011234")
    private String idCard;
    
    @ApiModelProperty(value = "手机号", example = "13800138000")
    private String phone;
    
    @ApiModelProperty(value = "邮箱", example = "teacher@example.com")
    private String email;
    
    @ApiModelProperty(value = "所属学院ID", required = true, example = "1")
    private Long collegeId;
    
    @ApiModelProperty(value = "所属学校ID（冗余字段）", example = "1")
    private Long schoolId;
    
    @ApiModelProperty(value = "职称", example = "教授")
    private String title;
    
    @ApiModelProperty(value = "所属学院", example = "计算机学院")
    private String department;
    
    @ApiModelProperty(value = "角色代码（可选，如不指定则默认分配ROLE_CLASS_TEACHER。注意：角色分配建议在用户管理中统一处理）", example = "ROLE_CLASS_TEACHER")
    private String roleCode;
    
    @ApiModelProperty(value = "初始密码", required = true, example = "123456")
    private String password;
    
    @ApiModelProperty(value = "状态：1-启用，0-禁用", example = "1")
    private Integer status;
}

