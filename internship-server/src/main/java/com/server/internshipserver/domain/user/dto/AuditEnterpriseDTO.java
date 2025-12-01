package com.server.internshipserver.domain.user.dto;

import com.server.internshipserver.common.enums.AuditStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 审核企业DTO
 * 用于封装企业审核操作的参数，包括审核状态和审核意见
 * 审核状态使用AuditStatus枚举，支持通过、拒绝等操作
 */
@Data
@ApiModel("审核企业DTO")
public class AuditEnterpriseDTO {
    
    @ApiModelProperty(value = "审核状态（1-通过，2-拒绝）", required = true)
    private AuditStatus auditStatus;
    
    @ApiModelProperty("审核意见")
    private String auditOpinion;
}

