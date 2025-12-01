package com.server.internshipserver.domain.statistics.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 系统管理员仪表盘统计数据DTO
 */
@ApiModel(description = "系统管理员仪表盘统计数据")
@Data
public class SystemAdminDashboardDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "学校总数", example = "10")
    private Long schoolCount;
    
    @ApiModelProperty(value = "用户总数", example = "1000")
    private Long userCount;
    
    @ApiModelProperty(value = "实习岗位总数", example = "200")
    private Long postCount;
    
    @ApiModelProperty(value = "实习学生总数", example = "500")
    private Long internshipStudentCount;
    
    @ApiModelProperty(value = "用户角色分布（饼图数据）")
    private List<PieItem> userRoleDistribution;
    
    @ApiModelProperty(value = "各学校实习人数对比（柱状图数据）")
    private List<BarChartItem> schoolInternshipComparison;
    
    @ApiModelProperty(value = "实习岗位类型分布（饼图数据）")
    private List<PieItem> postTypeDistribution;
    
    /**
     * 饼图数据项
     */
    @Data
    public static class PieItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "名称", example = "学生")
        private String name;
        
        @ApiModelProperty(value = "数值", example = "500")
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
        
        @ApiModelProperty(value = "名称", example = "XX大学")
        private String name;
        
        @ApiModelProperty(value = "数值", example = "100")
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

