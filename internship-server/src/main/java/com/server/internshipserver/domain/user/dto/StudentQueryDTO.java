package com.server.internshipserver.domain.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 学生查询DTO
 * 用于封装学生分页查询的查询条件，包括学号、班级、专业、学院、学校、状态、入学年份等
 */
@Data
@ApiModel("学生查询DTO")
public class StudentQueryDTO {
    
    @ApiModelProperty("学号")
    private String studentNo;
    
    @ApiModelProperty("班级ID")
    private Long classId;
    
    @ApiModelProperty("专业ID")
    private Long majorId;
    
    @ApiModelProperty("学院ID")
    private Long collegeId;
    
    @ApiModelProperty("学校ID")
    private Long schoolId;
    
    @ApiModelProperty("状态：1-已审核，0-待审核")
    private Integer status;
    
    @ApiModelProperty("入学年份")
    private Integer enrollmentYear;
}

