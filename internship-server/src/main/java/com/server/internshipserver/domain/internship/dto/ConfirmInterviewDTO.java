package com.server.internshipserver.domain.internship.dto;

import com.server.internshipserver.common.enums.ConfirmStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 确认面试DTO
 * 用于封装学生确认面试操作的参数，包括确认状态
 * 确认状态使用ConfirmStatus枚举，支持已确认、已拒绝等操作
 */
@Data
@ApiModel("确认面试DTO")
public class ConfirmInterviewDTO {
    
    @ApiModelProperty(value = "确认状态（1-已确认，2-已拒绝）", required = true)
    private ConfirmStatus confirm;
}

