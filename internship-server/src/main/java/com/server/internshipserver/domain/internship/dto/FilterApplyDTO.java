package com.server.internshipserver.domain.internship.dto;

import com.server.internshipserver.common.enums.FilterAction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 筛选申请DTO
 * 用于封装实习申请筛选操作的参数，包括操作类型和备注
 * 操作类型使用FilterAction枚举，支持通过、不通过等筛选操作
 */
@Data
@ApiModel("筛选申请DTO")
public class FilterApplyDTO {
    
    @ApiModelProperty(value = "操作类型", required = true)
    private FilterAction action;
    
    @ApiModelProperty("备注")
    private String comment;
}

