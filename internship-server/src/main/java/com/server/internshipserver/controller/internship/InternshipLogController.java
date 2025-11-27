package com.server.internshipserver.controller.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.internship.InternshipLog;
import com.server.internshipserver.service.internship.InternshipLogService;
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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 实习日志管理控制器
 */
@Api(tags = "实习日志管理")
@RestController
@RequestMapping("/internship/log")
public class InternshipLogController {
    
    @Autowired
    private InternshipLogService internshipLogService;
    
    @ApiOperation("提交实习日志")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping
    public Result<InternshipLog> addLog(@RequestBody InternshipLog log) {
        InternshipLog result = internshipLogService.addLog(log);
        return Result.success("提交日志成功", result);
    }
    
    @ApiOperation("更新实习日志")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PutMapping
    public Result<InternshipLog> updateLog(@RequestBody InternshipLog log) {
        InternshipLog result = internshipLogService.updateLog(log);
        return Result.success("更新日志成功", result);
    }
    
    @ApiOperation("分页查询实习日志列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_STUDENT', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/page")
    public Result<Page<InternshipLog>> getLogPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "学生ID", required = false) @RequestParam(required = false) Long studentId,
            @ApiParam(value = "申请ID", required = false) @RequestParam(required = false) Long applyId,
            @ApiParam(value = "日志日期", required = false) @RequestParam(required = false) LocalDate logDate,
            @ApiParam(value = "批阅状态", required = false) @RequestParam(required = false) Integer reviewStatus) {
        Page<InternshipLog> page = new Page<>(current, size);
        Page<InternshipLog> result = internshipLogService.getLogPage(page, studentId, applyId, logDate, reviewStatus);
        return Result.success(result);
    }
    
    @ApiOperation("查询实习日志详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_STUDENT', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/{logId}")
    public Result<InternshipLog> getLogById(
            @ApiParam(value = "日志ID", required = true) @PathVariable Long logId) {
        InternshipLog log = internshipLogService.getLogById(logId);
        return Result.success(log);
    }
    
    @ApiOperation("批阅日志")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @PostMapping("/{logId}/review")
    public Result<?> reviewLog(
            @ApiParam(value = "日志ID", required = true) @PathVariable Long logId,
            @ApiParam(value = "批阅意见", required = false) @RequestParam(required = false) String reviewComment,
            @ApiParam(value = "批阅评分（0-100）", required = false) @RequestParam(required = false) BigDecimal reviewScore) {
        internshipLogService.reviewLog(logId, reviewComment, reviewScore);
        return Result.success("批阅成功");
    }
    
    @ApiOperation("删除日志")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @DeleteMapping("/{logId}")
    public Result<?> deleteLog(
            @ApiParam(value = "日志ID", required = true) @PathVariable Long logId) {
        internshipLogService.deleteLog(logId);
        return Result.success("删除成功");
    }
}

