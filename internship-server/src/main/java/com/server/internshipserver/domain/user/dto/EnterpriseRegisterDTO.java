package com.server.internshipserver.domain.user.dto;

import com.server.internshipserver.domain.user.Enterprise;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 企业注册DTO
 */
@ApiModel(description = "企业注册信息")
@Data
public class EnterpriseRegisterDTO {
    
    @ApiModelProperty(value = "企业信息", required = true)
    private Enterprise enterprise;
    
    @ApiModelProperty(value = "管理员用户名（用于登录）", required = true)
    private String username;
    
    @ApiModelProperty(value = "管理员密码（用于登录）", required = true)
    private String password;
}

