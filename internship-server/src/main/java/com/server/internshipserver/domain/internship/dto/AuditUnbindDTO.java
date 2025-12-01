package com.server.internshipserver.domain.internship.dto;

import com.server.internshipserver.common.enums.AuditStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 审核解绑DTO
 * 用于封装实习申请解绑审核操作的参数，包括审核状态和审核意见
 * 审核状态使用AuditStatus枚举，支持已解绑、解绑被拒绝等操作
 */
@Data
@ApiModel("审核解绑DTO")
public class AuditUnbindDTO {
    
    @ApiModelProperty(value = "审核状态（2-已解绑，3-解绑被拒绝）", required = true)
    private AuditStatus auditStatus;
    
    @ApiModelProperty("审核意见")
    private String auditOpinion;
}

