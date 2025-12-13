package com.server.internshipserver.domain.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 教师注册DTO
 * 用于封装教师自主注册时的信息
 */
@ApiModel(description = "教师注册信息")
@Data
public class TeacherRegisterDTO {
    
    @ApiModelProperty(value = "工号", required = true, example = "T2021001")
    private String teacherNo;
    
    @ApiModelProperty(value = "用户名（必填，用于登录）", required = true, example = "teacher001")
    private String username;
    
    @ApiModelProperty(value = "真实姓名", required = true, example = "张老师")
    private String realName;
    
    @ApiModelProperty(value = "性别", example = "男")
    private String gender;
    
    @ApiModelProperty(value = "身份证号", example = "110101199001011234")
    private String idCard;
    
    @ApiModelProperty(value = "手机号", example = "13800138000")
    private String phone;
    
    @ApiModelProperty(value = "邮箱", example = "teacher@example.com")
    private String email;
    
    @ApiModelProperty(value = "所属学校ID", example = "1")
    private Long schoolId;
    
    @ApiModelProperty(value = "所属学院ID", required = true, example = "1")
    private Long collegeId;
    
    @ApiModelProperty(value = "职称", example = "教授")
    private String title;
    
    @ApiModelProperty(value = "密码", required = true, example = "123456")
    private String password;
}

