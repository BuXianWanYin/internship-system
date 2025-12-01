package com.server.internshipserver.domain.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 学期查询DTO
 * 用于封装学期分页查询的查询条件，包括学期名称、年份、是否当前学期、日期范围、学校ID等
 */
@Data
@ApiModel("学期查询DTO")
public class SemesterQueryDTO {
    
    @ApiModelProperty("学期名称")
    private String semesterName;
    
    @ApiModelProperty("年份")
    private Integer year;
    
    @ApiModelProperty("是否当前学期：1-是，0-否")
    private Integer isCurrent;
    
    @ApiModelProperty("开始日期")
    private String startDate;
    
    @ApiModelProperty("结束日期")
    private String endDate;
    
    @ApiModelProperty("学校ID")
    private Long schoolId;
}

