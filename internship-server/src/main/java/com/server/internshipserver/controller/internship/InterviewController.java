package com.server.internshipserver.controller.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.internship.Interview;
import com.server.internshipserver.service.internship.InterviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 面试管理控制器
 */
@Api(tags = "面试管理")
@RestController
@RequestMapping("/internship/interview")
public class InterviewController {
    
    @Autowired
    private InterviewService interviewService;
    
    @ApiOperation("安排面试")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @PostMapping
    public Result<Interview> addInterview(@RequestBody Interview interview) {
        Interview result = interviewService.addInterview(interview);
        return Result.success("安排面试成功", result);
    }
    
    @ApiOperation("更新面试信息")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @PutMapping
    public Result<Interview> updateInterview(@RequestBody Interview interview) {
        Interview result = interviewService.updateInterview(interview);
        return Result.success("更新面试信息成功", result);
    }
    
    @ApiOperation("分页查询面试列表")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_STUDENT')")
    @GetMapping("/page")
    public Result<Page<Interview>> getInterviewPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "申请ID", required = false) @RequestParam(required = false) Long applyId,
            @ApiParam(value = "企业ID", required = false) @RequestParam(required = false) Long enterpriseId,
            @ApiParam(value = "学生ID", required = false) @RequestParam(required = false) Long studentId,
            @ApiParam(value = "状态", required = false) @RequestParam(required = false) Integer status) {
        Page<Interview> page = new Page<>(current, size);
        Page<Interview> result = interviewService.getInterviewPage(page, applyId, enterpriseId, studentId, status);
        return Result.success(result);
    }
    
    @ApiOperation("查询面试详情")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_STUDENT')")
    @GetMapping("/{interviewId}")
    public Result<Interview> getInterviewById(
            @ApiParam(value = "面试ID", required = true) @PathVariable Long interviewId) {
        Interview interview = interviewService.getInterviewById(interviewId);
        return Result.success(interview);
    }
    
    @ApiOperation("学生确认面试")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/{interviewId}/confirm")
    public Result<?> confirmInterview(
            @ApiParam(value = "面试ID", required = true) @PathVariable Long interviewId,
            @ApiParam(value = "确认（1-已确认，2-已拒绝）", required = true) @RequestParam Integer confirm) {
        interviewService.confirmInterview(interviewId, confirm);
        return Result.success("确认成功");
    }
    
    @ApiOperation("提交面试结果")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @PostMapping("/{interviewId}/result")
    public Result<?> submitInterviewResult(
            @ApiParam(value = "面试ID", required = true) @PathVariable Long interviewId,
            @ApiParam(value = "面试结果（1-通过，2-不通过，3-待定）", required = true) @RequestParam Integer interviewResult,
            @ApiParam(value = "面试评价", required = false) @RequestParam(required = false) String interviewComment) {
        interviewService.submitInterviewResult(interviewId, interviewResult, interviewComment);
        return Result.success("提交成功");
    }
    
    @ApiOperation("取消面试")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @PostMapping("/{interviewId}/cancel")
    public Result<?> cancelInterview(
            @ApiParam(value = "面试ID", required = true) @PathVariable Long interviewId) {
        interviewService.cancelInterview(interviewId);
        return Result.success("取消成功");
    }
}

