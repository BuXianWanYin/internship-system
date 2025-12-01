package com.server.internshipserver.domain.internship.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 实习计划查询DTO
 * 用于封装实习计划分页查询的查询条件，包括计划名称、学期、学校、学院、专业、状态等
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

