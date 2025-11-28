package com.server.internshipserver.domain.statistics.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 待批阅统计数据DTO
 */
@ApiModel(description = "待批阅统计数据")
@Data
public class PendingReviewStatisticsDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "待批阅日志数", example = "5")
    private Long pendingLogCount;
    
    @ApiModelProperty(value = "待批阅周报数", example = "3")
    private Long pendingReportCount;
}

