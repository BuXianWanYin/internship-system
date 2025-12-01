package com.server.internshipserver.domain.internship.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 实习申请查询DTO
 */
@Data
@ApiModel("实习申请查询DTO")
public class InternshipApplyQueryDTO {
    
    @ApiModelProperty("学生ID")
    private Long studentId;
    
    @ApiModelProperty("企业ID")
    private Long enterpriseId;
    
    @ApiModelProperty("岗位ID")
    private Long postId;
    
    @ApiModelProperty("申请类型")
    private Integer applyType;
    
    @ApiModelProperty("状态")
    private Integer status;
}

