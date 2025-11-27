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
    
    @ApiModelProperty("学生ID（可选）")
    private Long studentId;
    
    @ApiModelProperty("企业ID（可选）")
    private Long enterpriseId;
    
    @ApiModelProperty("岗位ID（可选）")
    private Long postId;
    
    @ApiModelProperty("申请类型（可选）")
    private Integer applyType;
    
    @ApiModelProperty("状态（可选）")
    private Integer status;
}

