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
    
    @ApiModelProperty("计划名称（可选）")
    private String planName;
    
    @ApiModelProperty("学期ID（可选）")
    private Long semesterId;
    
    @ApiModelProperty("学校ID（可选）")
    private Long schoolId;
    
    @ApiModelProperty("学院ID（可选）")
    private Long collegeId;
    
    @ApiModelProperty("专业ID（可选）")
    private Long majorId;
    
    @ApiModelProperty("状态（可选）")
    private Integer status;
}

