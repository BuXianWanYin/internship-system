package com.server.internshipserver.controller.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.internship.Attendance;
import com.server.internshipserver.domain.internship.dto.AttendanceStatistics;
import com.server.internshipserver.service.internship.AttendanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤管理控制器
 */
@Api(tags = "考勤管理")
@RestController
@RequestMapping("/internship/attendance")
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;
    
    @ApiOperation("确认考勤")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @PostMapping
    public Result<Attendance> addAttendance(@RequestBody Attendance attendance) {
        Attendance result = attendanceService.addAttendance(attendance);
        return Result.success("确认考勤成功", result);
    }
    
    @ApiOperation("批量确认考勤")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @PostMapping("/batch")
    public Result<?> batchAddAttendance(@RequestBody List<Attendance> attendanceList) {
        attendanceService.batchAddAttendance(attendanceList);
        return Result.success("批量确认考勤成功");
    }
    
    @ApiOperation("更新考勤")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @PutMapping
    public Result<Attendance> updateAttendance(@RequestBody Attendance attendance) {
        Attendance result = attendanceService.updateAttendance(attendance);
        return Result.success("更新考勤成功", result);
    }
    
    @ApiOperation("分页查询考勤列表")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/page")
    public Result<Page<Attendance>> getAttendancePage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "学生ID", required = false) @RequestParam(required = false) Long studentId,
            @ApiParam(value = "申请ID", required = false) @RequestParam(required = false) Long applyId,
            @ApiParam(value = "考勤日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate attendanceDate,
            @ApiParam(value = "考勤类型", required = false) @RequestParam(required = false) Integer attendanceType,
            @ApiParam(value = "确认状态", required = false) @RequestParam(required = false) Integer confirmStatus) {
        Page<Attendance> page = new Page<>(current, size);
        Page<Attendance> result = attendanceService.getAttendancePage(page, studentId, applyId, attendanceDate, attendanceType, confirmStatus);
        return Result.success(result);
    }
    
    @ApiOperation("查询考勤详情")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/{attendanceId}")
    public Result<Attendance> getAttendanceById(
            @ApiParam(value = "考勤ID", required = true) @PathVariable Long attendanceId) {
        Attendance attendance = attendanceService.getAttendanceById(attendanceId);
        return Result.success(attendance);
    }
    
    @ApiOperation("确认考勤")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @PostMapping("/{attendanceId}/confirm")
    public Result<?> confirmAttendance(
            @ApiParam(value = "考勤ID", required = true) @PathVariable Long attendanceId,
            @ApiParam(value = "确认状态（1-已确认，2-已拒绝）", required = true) @RequestParam Integer confirmStatus,
            @ApiParam(value = "确认意见", required = false) @RequestParam(required = false) String confirmComment,
            @ApiParam(value = "签到时间（可选，用于编辑）", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") java.time.LocalDateTime checkInTime,
            @ApiParam(value = "签退时间（可选，用于编辑）", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") java.time.LocalDateTime checkOutTime) {
        attendanceService.confirmAttendance(attendanceId, confirmStatus, confirmComment, checkInTime, checkOutTime);
        return Result.success("确认成功");
    }
    
    @ApiOperation("删除考勤")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @DeleteMapping("/{attendanceId}")
    public Result<?> deleteAttendance(
            @ApiParam(value = "考勤ID", required = true) @PathVariable Long attendanceId) {
        attendanceService.deleteAttendance(attendanceId);
        return Result.success("删除成功");
    }
    
    @ApiOperation("考勤统计")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/statistics")
    public Result<AttendanceStatistics> getAttendanceStatistics(
            @ApiParam(value = "学生ID", required = false) @RequestParam(required = false) Long studentId,
            @ApiParam(value = "申请ID", required = false) @RequestParam(required = false) Long applyId,
            @ApiParam(value = "开始日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam(value = "结束日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        AttendanceStatistics statistics = attendanceService.getAttendanceStatistics(studentId, applyId, startDate, endDate);
        return Result.success(statistics);
    }
    
    @ApiOperation("学生签到")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/check-in")
    public Result<Attendance> studentCheckIn(
            @ApiParam(value = "考勤日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate attendanceDate,
            @ApiParam(value = "时间段ID", required = false) @RequestParam(required = false) Long timeSlotId) {
        Attendance attendance = attendanceService.studentCheckIn(attendanceDate, timeSlotId);
        return Result.success("签到成功", attendance);
    }
    
    @ApiOperation("学生签退")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/check-out")
    public Result<Attendance> studentCheckOut(
            @ApiParam(value = "考勤日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate attendanceDate,
            @ApiParam(value = "时间段ID", required = false) @RequestParam(required = false) Long timeSlotId) {
        Attendance attendance = attendanceService.studentCheckOut(attendanceDate, timeSlotId);
        return Result.success("签退成功", attendance);
    }
    
    @ApiOperation("学生申请请假")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/apply-leave")
    public Result<Attendance> studentApplyLeave(
            @ApiParam(value = "考勤日期", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate attendanceDate,
            @ApiParam(value = "请假类型", required = true) @RequestParam String leaveType,
            @ApiParam(value = "请假原因", required = true) @RequestParam String leaveReason) {
        Attendance attendance = attendanceService.studentApplyLeave(attendanceDate, leaveType, leaveReason);
        return Result.success("请假申请提交成功", attendance);
    }
    
    @ApiOperation("学生选择休息")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/select-rest")
    public Result<Attendance> studentSelectRest(
            @ApiParam(value = "考勤日期", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate attendanceDate) {
        Attendance attendance = attendanceService.studentSelectRest(attendanceDate);
        return Result.success("休息申请提交成功", attendance);
    }
    
    @ApiOperation("获取今天的考勤记录（学生端）")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @GetMapping("/today")
    public Result<Attendance> getTodayAttendance() {
        Attendance attendance = attendanceService.getTodayAttendance();
        return Result.success(attendance);
    }
}

