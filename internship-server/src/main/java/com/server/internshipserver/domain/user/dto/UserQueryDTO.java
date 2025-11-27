package com.server.internshipserver.domain.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户查询DTO
 */
@Data
@ApiModel("用户查询DTO")
public class UserQueryDTO {
    
    @ApiModelProperty("用户名（可选）")
    private String username;
    
    @ApiModelProperty("真实姓名（可选）")
    private String realName;
    
    @ApiModelProperty("手机号（可选）")
    private String phone;
    
    @ApiModelProperty("状态：1-启用，0-禁用（可选）")
    private Integer status;
    
    @ApiModelProperty("角色代码（可选，多个用逗号分隔）")
    private String roleCodes;
    
    @ApiModelProperty("学校ID（可选）")
    private Long schoolId;
    
    @ApiModelProperty("学院ID（可选）")
    private Long collegeId;
    
    @ApiModelProperty("班级ID（可选）")
    private Long classId;
}

