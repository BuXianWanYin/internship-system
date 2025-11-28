package com.server.internshipserver.domain.statistics.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 企业维度统计数据DTO
 */
@ApiModel(description = "企业维度统计数据")
@Data
public class EnterpriseStatisticsDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "柱状图数据列表")
    private List<BarChartItem> barChartData;
    
    /**
     * 柱状图数据项
     */
    @Data
    public static class BarChartItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "企业名称", example = "XX科技有限公司")
        private String name;
        
        @ApiModelProperty(value = "实习人数", example = "20")
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

