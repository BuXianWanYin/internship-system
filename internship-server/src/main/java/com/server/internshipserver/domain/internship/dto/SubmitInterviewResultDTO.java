package com.server.internshipserver.domain.internship.dto;

import com.server.internshipserver.common.enums.InterviewResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 提交面试结果DTO
 */
@Data
@ApiModel("提交面试结果DTO")
public class SubmitInterviewResultDTO {
    
    @ApiModelProperty(value = "面试结果（1-通过，2-不通过，3-待定）", required = true)
    private InterviewResult interviewResult;
    
    @ApiModelProperty("面试评价")
    private String interviewComment;
}

