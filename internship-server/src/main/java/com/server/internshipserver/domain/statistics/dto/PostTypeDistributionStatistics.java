package com.server.internshipserver.domain.statistics.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 岗位类型分布统计数据DTO
 */
@ApiModel(description = "岗位类型分布统计数据")
@Data
public class PostTypeDistributionStatistics implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "饼图数据列表")
    private List<PieChartItem> pieChartData;
    
    /**
     * 饼图数据项
     */
    @Data
    public static class PieChartItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "岗位类型名称", example = "Java开发")
        private String name;
        
        @ApiModelProperty(value = "人数", example = "50")
        private Long value;
        
        @ApiModelProperty(value = "颜色", example = "#409EFF")
        private String color;
        
        public PieChartItem() {
        }
        
        public PieChartItem(String name, Long value, String color) {
            this.name = name;
            this.value = value;
            this.color = color;
        }
    }
}

