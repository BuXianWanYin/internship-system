package com.server.internshipserver.domain.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 学生批量导入结果DTO
 * 用于封装学生批量导入操作的结果，包括成功数量、失败数量、失败详情列表等
 * 提供详细的导入结果信息，便于用户查看导入成功和失败的情况
 */
@ApiModel(description = "学生批量导入结果")
@Data
public class StudentImportResult {
    
    @ApiModelProperty(value = "成功导入数量")
    private Integer successCount;
    
    @ApiModelProperty(value = "失败数量")
    private Integer failCount;
    
    @ApiModelProperty(value = "失败详情列表")
    private List<StudentImportDTO> failList;
    
    @ApiModelProperty(value = "成功详情列表")
    private List<StudentImportDTO> successList;
    
    @ApiModelProperty(value = "总数量")
    private Integer totalCount;
}

