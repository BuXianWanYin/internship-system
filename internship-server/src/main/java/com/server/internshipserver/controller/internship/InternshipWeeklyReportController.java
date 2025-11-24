package com.server.internshipserver.controller.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.internship.InternshipWeeklyReport;
import com.server.internshipserver.service.internship.InternshipWeeklyReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 周报管理控制器
 */
@Api(tags = "周报管理")
@RestController
@RequestMapping("/internship/weekly-report")
public class InternshipWeeklyReportController {
    
    @Autowired
    private InternshipWeeklyReportService internshipWeeklyReportService;
    
    @ApiOperation("提交周报")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping
    public Result<InternshipWeeklyReport> addReport(@RequestBody InternshipWeeklyReport report) {
        InternshipWeeklyReport result = internshipWeeklyReportService.addReport(report);
        return Result.success("提交周报成功", result);
    }
    
    @ApiOperation("更新周报")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PutMapping
    public Result<InternshipWeeklyReport> updateReport(@RequestBody InternshipWeeklyReport report) {
        InternshipWeeklyReport result = internshipWeeklyReportService.updateReport(report);
        return Result.success("更新周报成功", result);
    }
    
    @ApiOperation("分页查询周报列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_STUDENT', 'ROLE_INSTRUCTOR', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/page")
    public Result<Page<InternshipWeeklyReport>> getReportPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "学生ID", required = false) @RequestParam(required = false) Long studentId,
            @ApiParam(value = "申请ID", required = false) @RequestParam(required = false) Long applyId,
            @ApiParam(value = "周次", required = false) @RequestParam(required = false) Integer weekNumber,
            @ApiParam(value = "批阅状态", required = false) @RequestParam(required = false) Integer reviewStatus) {
        Page<InternshipWeeklyReport> page = new Page<>(current, size);
        Page<InternshipWeeklyReport> result = internshipWeeklyReportService.getReportPage(page, studentId, applyId, weekNumber, reviewStatus);
        return Result.success(result);
    }
    
    @ApiOperation("查询周报详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_STUDENT', 'ROLE_INSTRUCTOR', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/{reportId}")
    public Result<InternshipWeeklyReport> getReportById(
            @ApiParam(value = "周报ID", required = true) @PathVariable Long reportId) {
        InternshipWeeklyReport report = internshipWeeklyReportService.getReportById(reportId);
        return Result.success(report);
    }
    
    @ApiOperation("批阅周报")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_INSTRUCTOR', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @PostMapping("/{reportId}/review")
    public Result<?> reviewReport(
            @ApiParam(value = "周报ID", required = true) @PathVariable Long reportId,
            @ApiParam(value = "批阅意见", required = false) @RequestParam(required = false) String reviewComment,
            @ApiParam(value = "批阅评分（0-100）", required = false) @RequestParam(required = false) BigDecimal reviewScore) {
        internshipWeeklyReportService.reviewReport(reportId, reviewComment, reviewScore);
        return Result.success("批阅成功");
    }
    
    @ApiOperation("删除周报")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @DeleteMapping("/{reportId}")
    public Result<?> deleteReport(
            @ApiParam(value = "周报ID", required = true) @PathVariable Long reportId) {
        internshipWeeklyReportService.deleteReport(reportId);
        return Result.success("删除成功");
    }
}

