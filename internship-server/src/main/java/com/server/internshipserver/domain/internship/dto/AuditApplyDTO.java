package com.server.internshipserver.domain.internship.dto;

import com.server.internshipserver.common.enums.AuditStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 审核申请DTO
 * 用于封装实习申请审核操作的参数，包括审核状态和审核意见
 * 审核状态使用AuditStatus枚举，支持通过、拒绝等操作
 */
@Data
@ApiModel("审核申请DTO")
public class AuditApplyDTO {
    
    @ApiModelProperty(value = "审核状态", required = true, example = "1")
    private AuditStatus auditStatus;
    
    @ApiModelProperty("审核意见")
    private String auditOpinion;
}

