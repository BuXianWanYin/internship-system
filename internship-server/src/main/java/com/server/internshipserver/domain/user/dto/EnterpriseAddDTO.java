package com.server.internshipserver.domain.user.dto;

import com.server.internshipserver.domain.user.Enterprise;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 企业添加DTO
 * 用于封装添加企业时的所有信息，包括企业基本信息和企业管理员账号信息
 * 系统会自动创建企业管理员用户账号并分配ROLE_ENTERPRISE_ADMIN角色
 */
@ApiModel(description = "企业添加信息（包含企业管理员信息）")
@Data
public class EnterpriseAddDTO {
    
    @ApiModelProperty(value = "企业信息", required = true)
    private Enterprise enterprise;
    
    @ApiModelProperty(value = "企业管理员姓名", required = true, example = "张总")
    private String adminName;
    
    @ApiModelProperty(value = "企业管理员手机号", required = true, example = "13800138000")
    private String adminPhone;
    
    @ApiModelProperty(value = "企业管理员邮箱", example = "admin@example.com")
    private String adminEmail;
    
    @ApiModelProperty(value = "企业管理员初始密码", required = true, example = "123456")
    private String adminPassword;
}

