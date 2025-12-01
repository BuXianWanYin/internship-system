package com.server.internshipserver.domain.internship.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 实习岗位查询DTO
 */
@Data
@ApiModel("实习岗位查询DTO")
public class InternshipPostQueryDTO {
    
    @ApiModelProperty("岗位名称")
    private String postName;
    
    @ApiModelProperty("企业ID")
    private Long enterpriseId;
    
    @ApiModelProperty("状态")
    private Integer status;
    
    @ApiModelProperty("仅显示合作企业岗位（学生端使用）")
    private Boolean cooperationOnly;
}

