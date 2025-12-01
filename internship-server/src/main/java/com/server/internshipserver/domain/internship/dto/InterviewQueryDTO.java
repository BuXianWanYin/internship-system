package com.server.internshipserver.domain.internship.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 面试查询DTO
 */
@Data
@ApiModel("面试查询DTO")
public class InterviewQueryDTO {
    
    @ApiModelProperty("申请ID")
    private Long applyId;
    
    @ApiModelProperty("企业ID")
    private Long enterpriseId;
    
    @ApiModelProperty("学生ID")
    private Long studentId;
    
    @ApiModelProperty("状态")
    private Integer status;
}

