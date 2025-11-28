package com.server.internshipserver.domain.statistics.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 统计查询条件DTO
 */
@ApiModel(description = "统计查询条件")
@Data
public class StatisticsQueryDTO {
    
    @ApiModelProperty(value = "开始日期", example = "2024-01-01")
    private LocalDate startDate;
    
    @ApiModelProperty(value = "结束日期", example = "2024-12-31")
    private LocalDate endDate;
    
    @ApiModelProperty(value = "学校ID", example = "1")
    private Long schoolId;
    
    @ApiModelProperty(value = "学院ID", example = "1")
    private Long collegeId;
    
    @ApiModelProperty(value = "专业ID", example = "1")
    private Long majorId;
    
    @ApiModelProperty(value = "班级ID", example = "1")
    private Long classId;
    
    @ApiModelProperty(value = "企业ID", example = "1")
    private Long enterpriseId;
    
    @ApiModelProperty(value = "学生ID", example = "1")
    private Long studentId;
}

