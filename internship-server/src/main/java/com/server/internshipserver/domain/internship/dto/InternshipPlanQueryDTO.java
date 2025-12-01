package com.server.internshipserver.domain.internship.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 实习计划查询DTO
 */
@Data
@ApiModel("实习计划查询DTO")
public class InternshipPlanQueryDTO {
    
    @ApiModelProperty("计划名称")
    private String planName;
    
    @ApiModelProperty("学期ID")
    private Long semesterId;
    
    @ApiModelProperty("学校ID")
    private Long schoolId;
    
    @ApiModelProperty("学院ID")
    private Long collegeId;
    
    @ApiModelProperty("专业ID")
    private Long majorId;
    
    @ApiModelProperty("状态")
    private Integer status;
}

