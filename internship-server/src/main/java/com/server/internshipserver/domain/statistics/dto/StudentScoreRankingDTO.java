package com.server.internshipserver.domain.statistics.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 学生评价分数排行DTO
 */
@ApiModel(description = "学生评价分数排行数据")
@Data
public class StudentScoreRankingDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "柱状图数据列表")
    private List<BarChartItem> barChartData;
    
    /**
     * 柱状图数据项
     */
    @Data
    public static class BarChartItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "学生姓名", example = "张三")
        private String name;
        
        @ApiModelProperty(value = "综合成绩", example = "85.5")
        private BigDecimal value;
        
        @ApiModelProperty(value = "颜色", example = "#409EFF")
        private String color;
        
        public BarChartItem() {
        }
        
        public BarChartItem(String name, BigDecimal value, String color) {
            this.name = name;
            this.value = value;
            this.color = color;
        }
    }
}

