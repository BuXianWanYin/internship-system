package com.server.internshipserver.domain.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 企业导师添加DTO（包含用户信息）
 */
@ApiModel(description = "企业导师添加信息")
@Data
public class EnterpriseMentorAddDTO {
    
    @ApiModelProperty(value = "导师姓名", required = true, example = "张导师")
    private String mentorName;
    
    @ApiModelProperty(value = "所属企业ID", required = true, example = "1")
    private Long enterpriseId;
    
    @ApiModelProperty(value = "职位", example = "技术总监")
    private String position;
    
    @ApiModelProperty(value = "部门", example = "技术部")
    private String department;
    
    @ApiModelProperty(value = "联系电话", example = "13800138000")
    private String phone;
    
    @ApiModelProperty(value = "邮箱", example = "mentor@example.com")
    private String email;
    
    @ApiModelProperty(value = "初始密码", required = true, example = "123456")
    private String password;
    
    @ApiModelProperty(value = "状态：1-启用，0-禁用", example = "1")
    private Integer status;
}

