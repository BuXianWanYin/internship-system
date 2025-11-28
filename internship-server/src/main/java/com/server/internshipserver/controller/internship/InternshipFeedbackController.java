package com.server.internshipserver.controller.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.internship.InternshipFeedback;
import com.server.internshipserver.service.internship.InternshipFeedbackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问题反馈管理控制器
 */
@Api(tags = "问题反馈管理")
@RestController
@RequestMapping("/internship/feedback")
public class InternshipFeedbackController {
    
    @Autowired
    private InternshipFeedbackService internshipFeedbackService;
    
    @ApiOperation("提交问题反馈")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping
    public Result<InternshipFeedback> addFeedback(@RequestBody InternshipFeedback feedback) {
        InternshipFeedback result = internshipFeedbackService.addFeedback(feedback);
        return Result.success("提交反馈成功", result);
    }
    
    @ApiOperation("更新问题反馈")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PutMapping
    public Result<InternshipFeedback> updateFeedback(@RequestBody InternshipFeedback feedback) {
        InternshipFeedback result = internshipFeedbackService.updateFeedback(feedback);
        return Result.success("更新反馈成功", result);
    }
    
    @ApiOperation("分页查询问题反馈列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @GetMapping("/page")
    public Result<Page<InternshipFeedback>> getFeedbackPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "学生ID", required = false) @RequestParam(required = false) Long studentId,
            @ApiParam(value = "申请ID", required = false) @RequestParam(required = false) Long applyId,
            @ApiParam(value = "反馈类型", required = false) @RequestParam(required = false) Integer feedbackType,
            @ApiParam(value = "反馈状态", required = false) @RequestParam(required = false) Integer feedbackStatus) {
        Page<InternshipFeedback> page = new Page<>(current, size);
        Page<InternshipFeedback> result = internshipFeedbackService.getFeedbackPage(page, studentId, applyId, feedbackType, feedbackStatus);
        return Result.success(result);
    }
    
    @ApiOperation("查询问题反馈详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @GetMapping("/{feedbackId}")
    public Result<InternshipFeedback> getFeedbackById(
            @ApiParam(value = "反馈ID", required = true) @PathVariable Long feedbackId) {
        InternshipFeedback feedback = internshipFeedbackService.getFeedbackById(feedbackId);
        return Result.success(feedback);
    }
    
    @ApiOperation("回复问题反馈")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @PostMapping("/{feedbackId}/reply")
    public Result<?> replyFeedback(
            @ApiParam(value = "反馈ID", required = true) @PathVariable Long feedbackId,
            @ApiParam(value = "回复内容", required = true) @RequestParam String replyContent,
            @ApiParam(value = "回复人类型（1-指导教师，2-企业导师）", required = true) @RequestParam Integer replyUserType) {
        internshipFeedbackService.replyFeedback(feedbackId, replyContent, replyUserType);
        return Result.success("回复成功");
    }
    
    @ApiOperation("标记问题已解决")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @PostMapping("/{feedbackId}/solve")
    public Result<?> solveFeedback(
            @ApiParam(value = "反馈ID", required = true) @PathVariable Long feedbackId) {
        internshipFeedbackService.solveFeedback(feedbackId);
        return Result.success("标记成功");
    }
    
    @ApiOperation("关闭问题反馈")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @PostMapping("/{feedbackId}/close")
    public Result<?> closeFeedback(
            @ApiParam(value = "反馈ID", required = true) @PathVariable Long feedbackId) {
        internshipFeedbackService.closeFeedback(feedbackId);
        return Result.success("关闭成功");
    }
    
    @ApiOperation("删除问题反馈")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @DeleteMapping("/{feedbackId}")
    public Result<?> deleteFeedback(
            @ApiParam(value = "反馈ID", required = true) @PathVariable Long feedbackId) {
        internshipFeedbackService.deleteFeedback(feedbackId);
        return Result.success("删除成功");
    }
}

