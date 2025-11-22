package com.server.internshipserver.domain.user.dto;

import com.server.internshipserver.domain.user.Enterprise;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 企业注册DTO
 */
@ApiModel(description = "企业注册信息")
@Data
public class EnterpriseRegisterDTO {
    
    @ApiModelProperty(value = "企业信息", required = true)
    private Enterprise enterprise;
    
    @ApiModelProperty(value = "意向合作院校ID列表", required = true)
    private List<Long> schoolIds;
}

