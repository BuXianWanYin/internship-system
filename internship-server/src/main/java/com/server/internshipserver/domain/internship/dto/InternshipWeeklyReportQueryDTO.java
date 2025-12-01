package com.server.internshipserver.domain.internship.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 实习周报查询DTO
 */
@Data
@ApiModel("实习周报查询DTO")
public class InternshipWeeklyReportQueryDTO {
    
    @ApiModelProperty("学生ID")
    private Long studentId;
    
    @ApiModelProperty("申请ID")
    private Long applyId;
    
    @ApiModelProperty("周次")
    private Integer weekNumber;
    
    @ApiModelProperty("批阅状态")
    private Integer reviewStatus;
}

