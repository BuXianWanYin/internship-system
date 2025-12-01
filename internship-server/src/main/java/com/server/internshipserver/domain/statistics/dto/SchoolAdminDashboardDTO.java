package com.server.internshipserver.domain.statistics.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 学校管理员仪表盘统计数据DTO
 */
@ApiModel(description = "学校管理员仪表盘统计数据")
@Data
public class SchoolAdminDashboardDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "学院总数", example = "8")
    private Long collegeCount;
    
    @ApiModelProperty(value = "专业总数", example = "20")
    private Long majorCount;
    
    @ApiModelProperty(value = "学生总数", example = "2000")
    private Long studentCount;
    
    @ApiModelProperty(value = "男生人数", example = "1200")
    private Long maleCount;
    
    @ApiModelProperty(value = "女生人数", example = "800")
    private Long femaleCount;
    
    @ApiModelProperty(value = "教师总数", example = "150")
    private Long teacherCount;
    
    @ApiModelProperty(value = "不同专业实习人数对比（柱状图数据）")
    private List<BarChartItem> majorInternshipComparison;
    
    @ApiModelProperty(value = "学生性别分布（饼图数据）")
    private List<PieItem> genderDistribution;
    
    /**
     * 饼图数据项
     */
    @Data
    public static class PieItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "名称", example = "男生")
        private String name;
        
        @ApiModelProperty(value = "数值", example = "1200")
        private Long value;
        
        @ApiModelProperty(value = "颜色", example = "#409EFF")
        private String color;
        
        public PieItem() {
        }
        
        public PieItem(String name, Long value, String color) {
            this.name = name;
            this.value = value;
            this.color = color;
        }
    }
    
    /**
     * 柱状图数据项
     */
    @Data
    public static class BarChartItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "名称", example = "软件工程")
        private String name;
        
        @ApiModelProperty(value = "数值", example = "50")
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

