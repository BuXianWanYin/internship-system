package com.server.internshipserver.domain.statistics.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 学院负责人仪表盘统计数据DTO
 */
@ApiModel(description = "学院负责人仪表盘统计数据")
@Data
public class CollegeLeaderDashboardDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "专业总数", example = "5")
    private Long majorCount;
    
    @ApiModelProperty(value = "学生总数", example = "500")
    private Long studentCount;
    
    @ApiModelProperty(value = "教师总数", example = "30")
    private Long teacherCount;
    
    @ApiModelProperty(value = "班级总数", example = "15")
    private Long classCount;
}

