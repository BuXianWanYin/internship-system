package com.server.internshipserver.domain.user.dto;

import com.server.internshipserver.common.enums.AuditStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 审核企业DTO
 */
@Data
@ApiModel("审核企业DTO")
public class AuditEnterpriseDTO {
    
    @ApiModelProperty(value = "审核状态（1-通过，2-拒绝）", required = true)
    private AuditStatus auditStatus;
    
    @ApiModelProperty("审核意见")
    private String auditOpinion;
}

