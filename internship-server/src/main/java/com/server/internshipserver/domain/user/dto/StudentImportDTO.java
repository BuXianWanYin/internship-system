package com.server.internshipserver.domain.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 学生Excel导入DTO
 */
@ApiModel(description = "学生Excel导入数据")
@Data
public class StudentImportDTO {
    
    @ApiModelProperty(value = "学号", required = true)
    private String studentNo;
    
    @ApiModelProperty(value = "真实姓名", required = true)
    private String realName;
    
    @ApiModelProperty(value = "身份证号")
    private String idCard;
    
    @ApiModelProperty(value = "手机号")
    private String phone;
    
    @ApiModelProperty(value = "邮箱")
    private String email;
    
    @ApiModelProperty(value = "入学年份", required = true)
    private Integer enrollmentYear;
    
    @ApiModelProperty(value = "班级ID", required = true)
    private Long classId;
    
    @ApiModelProperty(value = "错误信息（导入失败时使用）")
    private String errorMessage;
    
    @ApiModelProperty(value = "行号（导入失败时使用）")
    private Integer rowNum;
}

