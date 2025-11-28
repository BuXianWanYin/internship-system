package com.server.internshipserver.domain.statistics.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 评价分数统计数据DTO
 */
@ApiModel(description = "评价分数统计数据")
@Data
public class EvaluationScoreStatistics implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "平均分", example = "82.50")
    private BigDecimal averageScore;
    
    @ApiModelProperty(value = "最高分", example = "98.00")
    private BigDecimal maxScore;
    
    @ApiModelProperty(value = "最低分", example = "60.00")
    private BigDecimal minScore;
    
    @ApiModelProperty(value = "优秀人数（90-100分）", example = "50")
    private Long excellentCount;
    
    @ApiModelProperty(value = "良好人数（80-89分）", example = "80")
    private Long goodCount;
    
    @ApiModelProperty(value = "中等人数（70-79分）", example = "60")
    private Long mediumCount;
    
    @ApiModelProperty(value = "及格人数（60-69分）", example = "30")
    private Long passCount;
    
    @ApiModelProperty(value = "不及格人数（0-59分）", example = "10")
    private Long failCount;
    
    @ApiModelProperty(value = "柱状图数据")
    private List<BarChartItem> barChartData;
    
    /**
     * 柱状图数据项
     */
    @Data
    public static class BarChartItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "等级名称", example = "优秀")
        private String name;
        
        @ApiModelProperty(value = "人数", example = "50")
        private Long value;
        
        @ApiModelProperty(value = "颜色", example = "#67C23A")
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

