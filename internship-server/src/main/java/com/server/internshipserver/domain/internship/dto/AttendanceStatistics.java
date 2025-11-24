package com.server.internshipserver.domain.internship.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 考勤统计DTO
 */
@ApiModel(description = "考勤统计信息")
@Data
public class AttendanceStatistics implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "总出勤天数", example = "30")
    private Integer totalDays;
    
    @ApiModelProperty(value = "正常出勤天数", example = "25")
    private Integer normalDays;
    
    @ApiModelProperty(value = "迟到天数", example = "2")
    private Integer lateDays;
    
    @ApiModelProperty(value = "早退天数", example = "1")
    private Integer earlyLeaveDays;
    
    @ApiModelProperty(value = "请假天数", example = "2")
    private Integer leaveDays;
    
    @ApiModelProperty(value = "缺勤天数", example = "0")
    private Integer absentDays;
    
    @ApiModelProperty(value = "休息天数", example = "0")
    private Integer restDays;
    
    @ApiModelProperty(value = "出勤率（百分比）", example = "83.33")
    private Double attendanceRate;
    
    @ApiModelProperty(value = "总工作时长（小时）", example = "200.00")
    private Double totalWorkHours;
}

