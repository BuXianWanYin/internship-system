package com.server.internshipserver.domain.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 学生Excel导入DTO
 * 用于封装从Excel文件导入的学生数据，包括学号、姓名、联系方式、班级等信息
 * 支持批量导入学生信息，系统会自动创建用户账号并分配ROLE_STUDENT角色
 */
@ApiModel(description = "学生Excel导入数据")
@Data
public class StudentImportDTO {
    
    @ApiModelProperty(value = "学号", required = true)
    private String studentNo;
    
    @ApiModelProperty(value = "用户名（必填，用于登录）", required = true, example = "student001")
    private String username;
    
    @ApiModelProperty(value = "真实姓名", required = true)
    private String realName;
    
    @ApiModelProperty(value = "性别")
    private String gender;
    
    @ApiModelProperty(value = "身份证号")
    private String idCard;
    
    @ApiModelProperty(value = "手机号")
    private String phone;
    
    @ApiModelProperty(value = "邮箱")
    private String email;
    
    @ApiModelProperty(value = "入学年份", required = true)
    private Integer enrollmentYear;
    
    @ApiModelProperty(value = "班级ID", required = true)
    private Long classId;
    
    @ApiModelProperty(value = "密码（注册时可选，不提供则自动生成）")
    private String password;
    
    @ApiModelProperty(value = "错误信息（导入失败时使用）")
    private String errorMessage;
    
    @ApiModelProperty(value = "行号（导入失败时使用）")
    private Integer rowNum;
}

