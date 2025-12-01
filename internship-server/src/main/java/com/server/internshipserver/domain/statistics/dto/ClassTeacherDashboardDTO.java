package com.server.internshipserver.domain.statistics.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 班主任仪表盘统计数据DTO
 */
@ApiModel(description = "班主任仪表盘统计数据")
@Data
public class ClassTeacherDashboardDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "管理班级数", example = "2")
    private Long classCount;
    
    @ApiModelProperty(value = "学生总数", example = "60")
    private Long studentCount;
    
    @ApiModelProperty(value = "实习岗位数", example = "20")
    private Long postCount;
    
    @ApiModelProperty(value = "平均评价分数", example = "85.5")
    private Double averageScore;
    
    @ApiModelProperty(value = "实习岗位列表")
    private List<PostInfo> posts;
    
    @ApiModelProperty(value = "评价分数分布（饼图数据）")
    private List<PieItem> scoreDistribution;
    
    /**
     * 实习岗位信息
     */
    @Data
    public static class PostInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "岗位ID", example = "1")
        private Long postId;
        
        @ApiModelProperty(value = "岗位名称", example = "Java开发实习生")
        private String postName;
        
        @ApiModelProperty(value = "企业名称", example = "XX科技有限公司")
        private String enterpriseName;
        
        @ApiModelProperty(value = "申请人数", example = "5")
        private Long applyCount;
    }
    
    /**
     * 饼图数据项
     */
    @Data
    public static class PieItem implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "名称", example = "优秀(90-100)")
        private String name;
        
        @ApiModelProperty(value = "数值", example = "10")
        private Long value;
        
        @ApiModelProperty(value = "颜色", example = "#67C23A")
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

