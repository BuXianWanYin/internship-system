package com.server.internshipserver.domain.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 学生查询DTO
 */
@Data
@ApiModel("学生查询DTO")
public class StudentQueryDTO {
    
    @ApiModelProperty("学号（可选）")
    private String studentNo;
    
    @ApiModelProperty("班级ID（可选）")
    private Long classId;
    
    @ApiModelProperty("专业ID（可选）")
    private Long majorId;
    
    @ApiModelProperty("学院ID（可选）")
    private Long collegeId;
    
    @ApiModelProperty("学校ID（可选）")
    private Long schoolId;
    
    @ApiModelProperty("状态：1-已审核，0-待审核（可选）")
    private Integer status;
    
    @ApiModelProperty("入学年份（可选）")
    private Integer enrollmentYear;
}

