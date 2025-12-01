package com.server.internshipserver.controller.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.internship.AttendanceGroup;
import com.server.internshipserver.domain.internship.AttendanceGroupRule;
import com.server.internshipserver.domain.internship.AttendanceGroupStudent;
import com.server.internshipserver.domain.internship.AttendanceGroupTimeSlot;
import com.server.internshipserver.service.internship.AttendanceGroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 考勤组管理控制器
 */
@Api(tags = "考勤组管理")
@RestController
@RequestMapping("/internship/attendance-group")
public class AttendanceGroupController {
    
    @Autowired
    private AttendanceGroupService attendanceGroupService;
    
    @ApiOperation("创建考勤组")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @PostMapping
    public Result<AttendanceGroup> createAttendanceGroup(@RequestBody AttendanceGroupRequest request) {
        AttendanceGroup result = attendanceGroupService.createAttendanceGroup(request.getGroup(), request.getTimeSlots());
        return Result.success("创建成功", result);
    }
    
    @ApiOperation("更新考勤组")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @PutMapping
    public Result<AttendanceGroup> updateAttendanceGroup(@RequestBody AttendanceGroupRequest request) {
        AttendanceGroup result = attendanceGroupService.updateAttendanceGroup(request.getGroup(), request.getTimeSlots());
        return Result.success("更新成功", result);
    }
    
    @ApiOperation("删除考勤组")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @DeleteMapping("/{groupId}")
    public Result<?> deleteAttendanceGroup(
            @ApiParam(value = "考勤组ID", required = true) @PathVariable Long groupId) {
        attendanceGroupService.deleteAttendanceGroup(groupId);
        return Result.success("删除成功");
    }
    
    @ApiOperation("分页查询考勤组列表")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @GetMapping("/page")
    public Result<Page<AttendanceGroup>> getAttendanceGroupPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "考勤组名称") @RequestParam(required = false) String groupName) {
        Page<AttendanceGroup> page = new Page<>(current, size);
        Page<AttendanceGroup> result = attendanceGroupService.getAttendanceGroupPage(page, groupName);
        return Result.success(result);
    }
    
    @ApiOperation("查询考勤组详情")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @GetMapping("/{groupId}")
    public Result<AttendanceGroup> getAttendanceGroupDetail(
            @ApiParam(value = "考勤组ID", required = true) @PathVariable Long groupId) {
        AttendanceGroup result = attendanceGroupService.getAttendanceGroupDetail(groupId);
        return Result.success(result);
    }
    
    @ApiOperation("添加考勤组规则")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @PostMapping("/{groupId}/rule")
    public Result<AttendanceGroupRule> addRule(
            @ApiParam(value = "考勤组ID", required = true) @PathVariable Long groupId,
            @RequestBody AttendanceGroupRule rule) {
        rule.setGroupId(groupId);
        AttendanceGroupRule result = attendanceGroupService.addRule(rule);
        return Result.success("添加成功", result);
    }
    
    @ApiOperation("删除考勤组规则")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @DeleteMapping("/rule/{ruleId}")
    public Result<?> deleteRule(
            @ApiParam(value = "规则ID", required = true) @PathVariable Long ruleId) {
        attendanceGroupService.deleteRule(ruleId);
        return Result.success("删除成功");
    }
    
    @ApiOperation("查询考勤组规则列表")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @GetMapping("/{groupId}/rules")
    public Result<List<AttendanceGroupRule>> getRuleList(
            @ApiParam(value = "考勤组ID", required = true) @PathVariable Long groupId) {
        List<AttendanceGroupRule> result = attendanceGroupService.getRuleList(groupId);
        return Result.success(result);
    }
    
    @ApiOperation("分配学生到考勤组")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @PostMapping("/{groupId}/assign-student")
    public Result<?> assignStudentToGroup(
            @ApiParam(value = "考勤组ID", required = true) @PathVariable Long groupId,
            @ApiParam(value = "实习申请ID", required = true) @RequestParam Long applyId,
            @ApiParam(value = "生效开始日期", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate effectiveStartDate,
            @ApiParam(value = "生效结束日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate effectiveEndDate) {
        attendanceGroupService.assignStudentToGroup(groupId, applyId, effectiveStartDate, effectiveEndDate);
        return Result.success("分配成功");
    }
    
    @ApiOperation("批量分配学生到考勤组")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @PostMapping("/{groupId}/assign-students")
    public Result<?> batchAssignStudentsToGroup(
            @ApiParam(value = "考勤组ID", required = true) @PathVariable Long groupId,
            @RequestBody BatchAssignRequest request) {
        attendanceGroupService.batchAssignStudentsToGroup(groupId, request.getApplyIds(), 
                request.getEffectiveStartDate(), request.getEffectiveEndDate());
        return Result.success("批量分配成功");
    }
    
    @ApiOperation("解除学生考勤组关联")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @DeleteMapping("/student/{applyId}")
    public Result<?> unassignStudentFromGroup(
            @ApiParam(value = "实习申请ID", required = true) @PathVariable Long applyId) {
        attendanceGroupService.unassignStudentFromGroup(applyId);
        return Result.success("解除关联成功");
    }
    
    @ApiOperation("查询考勤组的学生列表")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @GetMapping("/{groupId}/students")
    public Result<List<AttendanceGroupStudent>> getGroupStudents(
            @ApiParam(value = "考勤组ID", required = true) @PathVariable Long groupId) {
        List<AttendanceGroupStudent> result = attendanceGroupService.getGroupStudents(groupId);
        return Result.success(result);
    }
    
    @ApiOperation("根据实习申请ID查询学生所属的考勤组")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @GetMapping("/student/{applyId}")
    public Result<AttendanceGroup> getGroupByApplyId(
            @ApiParam(value = "实习申请ID", required = true) @PathVariable Long applyId) {
        AttendanceGroup result = attendanceGroupService.getGroupByApplyId(applyId);
        return Result.success(result);
    }
    
    @ApiOperation("获取应出勤日期列表")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/expected-dates")
    public Result<Map<String, Object>> getExpectedDates(
            @ApiParam(value = "考勤组ID", required = true) @RequestParam Long groupId,
            @ApiParam(value = "开始日期", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam(value = "结束日期", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<LocalDate> expectedDates = attendanceGroupService.calculateExpectedDates(groupId, startDate, endDate);
        Map<String, Object> result = new HashMap<>();
        result.put("expectedDates", expectedDates);
        result.put("totalExpectedDays", expectedDates.size());
        return Result.success(result);
    }
    
    @ApiOperation("计算缺勤天数")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/absent-days")
    public Result<Map<String, Object>> calculateAbsentDays(
            @ApiParam(value = "实习申请ID", required = true) @RequestParam Long applyId,
            @ApiParam(value = "开始日期", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam(value = "结束日期", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        int absentDays = attendanceGroupService.calculateAbsentDays(applyId, startDate, endDate);
        Map<String, Object> result = new HashMap<>();
        result.put("absentDays", absentDays);
        return Result.success(result);
    }
    
    /**
     * 考勤组请求对象（包含考勤组和时间段列表）
     */
    public static class AttendanceGroupRequest {
        private AttendanceGroup group;
        private List<AttendanceGroupTimeSlot> timeSlots;
        
        public AttendanceGroup getGroup() {
            return group;
        }
        
        public void setGroup(AttendanceGroup group) {
            this.group = group;
        }
        
        public List<AttendanceGroupTimeSlot> getTimeSlots() {
            return timeSlots;
        }
        
        public void setTimeSlots(List<AttendanceGroupTimeSlot> timeSlots) {
            this.timeSlots = timeSlots;
        }
    }
    
    /**
     * 批量分配请求对象
     */
    public static class BatchAssignRequest {
        private List<Long> applyIds;
        private LocalDate effectiveStartDate;
        private LocalDate effectiveEndDate;
        
        public List<Long> getApplyIds() {
            return applyIds;
        }
        
        public void setApplyIds(List<Long> applyIds) {
            this.applyIds = applyIds;
        }
        
        public LocalDate getEffectiveStartDate() {
            return effectiveStartDate;
        }
        
        public void setEffectiveStartDate(LocalDate effectiveStartDate) {
            this.effectiveStartDate = effectiveStartDate;
        }
        
        public LocalDate getEffectiveEndDate() {
            return effectiveEndDate;
        }
        
        public void setEffectiveEndDate(LocalDate effectiveEndDate) {
            this.effectiveEndDate = effectiveEndDate;
        }
    }
}

