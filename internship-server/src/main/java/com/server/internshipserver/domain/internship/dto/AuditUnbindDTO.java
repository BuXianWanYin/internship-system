package com.server.internshipserver.domain.internship.dto;

import com.server.internshipserver.common.enums.AuditStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 审核解绑DTO
 */
@Data
@ApiModel("审核解绑DTO")
public class AuditUnbindDTO {
    
    @ApiModelProperty(value = "审核状态（2-已解绑，3-解绑被拒绝）", required = true)
    private AuditStatus auditStatus;
    
    @ApiModelProperty("审核意见")
    private String auditOpinion;
}

