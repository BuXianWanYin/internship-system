package com.server.internshipserver.domain.internship.dto;

import com.server.internshipserver.common.enums.FilterAction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 筛选申请DTO
 */
@Data
@ApiModel("筛选申请DTO")
public class FilterApplyDTO {
    
    @ApiModelProperty(value = "操作类型", required = true)
    private FilterAction action;
    
    @ApiModelProperty("备注")
    private String comment;
}

