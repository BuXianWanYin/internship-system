package com.server.internshipserver.domain.statistics.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 实习进度统计数据DTO
 */
@ApiModel(description = "实习进度统计数据")
@Data
public class InternshipProgressStatistics implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "总实习人数", example = "500")
    private Long totalCount;
    
    @ApiModelProperty(value = "已完成人数（status=8）", example = "150")
    private Long completedCount;
    
    @ApiModelProperty(value = "进行中人数（status=6）", example = "320")
    private Long inProgressCount;
    
    @ApiModelProperty(value = "待开始人数（status=3,4,5）", example = "30")
    private Long pendingCount;
    
    @ApiModelProperty(value = "完成率（百分比）", example = "30.00")
    private BigDecimal completionRate;
    
    @ApiModelProperty(value = "饼图数据")
    private ProgressPieChartData pieChartData;
    
    /**
     * 饼图数据内部类
     */
    @Data
    public static class ProgressPieChartData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "待开始数据")
        private PieItem pending;
        
        @ApiModelProperty(value = "进行中数据")
        private PieItem inProgress;
        
        @ApiModelProperty(value = "已完成数据")
        private PieItem completed;
    }
    
    /**
     * 饼图数据项
     */
    @Data
    public static class PieItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "名称", example = "待开始")
        private String name;
        
        @ApiModelProperty(value = "数值", example = "30")
        private Long value;
        
        @ApiModelProperty(value = "颜色", example = "#C0C4CC")
        private String color;
        
        public PieItem() {
        }
        
        public PieItem(String name, Long value, String color) {
            this.name = name;
            this.value = value;
            this.color = color;
        }
    }
}

