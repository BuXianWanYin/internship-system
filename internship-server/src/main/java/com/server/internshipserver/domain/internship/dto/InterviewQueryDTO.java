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
    
    @ApiModelProperty("申请ID（可选）")
    private Long applyId;
    
    @ApiModelProperty("企业ID（可选）")
    private Long enterpriseId;
    
    @ApiModelProperty("学生ID（可选）")
    private Long studentId;
    
    @ApiModelProperty("状态（可选）")
    private Integer status;
}

