package com.server.internshipserver.domain.statistics.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 实习时长统计数据DTO
 */
@ApiModel(description = "实习时长统计数据")
@Data
public class InternshipDurationStatisticsDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "平均实习时长（天）", example = "90.50")
    private BigDecimal averageDuration;
    
    @ApiModelProperty(value = "总实习时长（天）", example = "4500")
    private Long totalDuration;
    
    @ApiModelProperty(value = "最长实习时长（天）", example = "120")
    private Long maxDuration;
    
    @ApiModelProperty(value = "最短实习时长（天）", example = "60")
    private Long minDuration;
    
    @ApiModelProperty(value = "折线图数据（按月份）")
    private List<LineChartItem> lineChartData;
    
    /**
     * 折线图数据项
     */
    @Data
    public static class LineChartItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "月份", example = "2024-01")
        private String month;
        
        @ApiModelProperty(value = "平均天数", example = "85.5")
        private BigDecimal averageDays;
    }
}

