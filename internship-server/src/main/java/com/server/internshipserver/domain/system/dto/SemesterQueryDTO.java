package com.server.internshipserver.domain.system.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 学期查询DTO
 */
@Data
@ApiModel("学期查询DTO")
public class SemesterQueryDTO {
    
    @ApiModelProperty("学期名称（可选）")
    private String semesterName;
    
    @ApiModelProperty("年份（可选）")
    private Integer year;
    
    @ApiModelProperty("是否当前学期：1-是，0-否（可选）")
    private Integer isCurrent;
    
    @ApiModelProperty("开始日期（可选）")
    private String startDate;
    
    @ApiModelProperty("结束日期（可选）")
    private String endDate;
    
    @ApiModelProperty("学校ID（可选）")
    private Long schoolId;
}

