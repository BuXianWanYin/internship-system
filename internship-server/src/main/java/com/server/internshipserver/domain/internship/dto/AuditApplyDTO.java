package com.server.internshipserver.domain.internship.dto;

import com.server.internshipserver.common.enums.AuditStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 审核申请DTO
 */
@Data
@ApiModel("审核申请DTO")
public class AuditApplyDTO {
    
    @ApiModelProperty(value = "审核状态", required = true, example = "1")
    private AuditStatus auditStatus;
    
    @ApiModelProperty("审核意见")
    private String auditOpinion;
}

