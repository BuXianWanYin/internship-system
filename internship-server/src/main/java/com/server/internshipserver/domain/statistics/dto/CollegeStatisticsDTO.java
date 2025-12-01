package com.server.internshipserver.domain.statistics.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 学院维度统计数据DTO
 */
@ApiModel(description = "学院维度统计数据")
@Data
public class CollegeStatisticsDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "柱状图数据列表")
    private List<BarChartItem> barChartData;
    
    /**
     * 柱状图数据项
     */
    @Data
    public static class BarChartItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "学院名称", example = "计算机学院")
        private String name;
        
        @ApiModelProperty(value = "实习人数", example = "50")
        private Long value;
        
        @ApiModelProperty(value = "颜色", example = "#409EFF")
        private String color;
        
        public BarChartItem() {
        }
        
        public BarChartItem(String name, Long value, String color) {
            this.name = name;
            this.value = value;
            this.color = color;
        }
    }
}

