package com.server.internshipserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.user.Teacher;
import com.server.internshipserver.domain.user.dto.TeacherAddDTO;
import com.server.internshipserver.domain.user.dto.TeacherUpdateDTO;
import com.server.internshipserver.service.user.TeacherService;

import java.util.List;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 教师管理控制器
 */
@Api(tags = "教师管理")
@RestController
@RequestMapping("/user/teacher")
public class TeacherController {
    
    @Autowired
    private TeacherService teacherService;
    
    @ApiOperation("分页查询教师列表")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<Page<Teacher>> getTeacherPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "工号（可选）") @RequestParam(required = false) String teacherNo,
            @ApiParam(value = "学院ID（可选）") @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "学校ID（可选）") @RequestParam(required = false) Long schoolId,
            @ApiParam(value = "状态：1-启用，0-禁用（可选）") @RequestParam(required = false) Integer status) {
        Page<Teacher> page = new Page<>(current, size);
        Page<Teacher> result = teacherService.getTeacherPage(page, teacherNo, collegeId, schoolId, status);
        return Result.success("查询成功", result);
    }
    
    @ApiOperation("根据ID查询教师详情")
    @GetMapping("/{teacherId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<Teacher> getTeacherById(
            @ApiParam(value = "教师ID", required = true) @PathVariable Long teacherId) {
        Teacher teacher = teacherService.getTeacherById(teacherId);
        return Result.success("查询成功", teacher);
    }
    
    @ApiOperation("根据用户ID查询教师信息")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<Teacher> getTeacherByUserId(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        Teacher teacher = teacherService.getTeacherByUserId(userId);
        return Result.success("查询成功", teacher);
    }
    
    @ApiOperation("添加教师")
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<Teacher> addTeacher(@RequestBody TeacherAddDTO teacherAddDTO) {
        // 如果提供了userId，使用原有方法（兼容已有接口，通过Teacher对象）
        // 这里统一使用新方法，自动创建用户
        Teacher result = teacherService.addTeacherWithUser(
                teacherAddDTO.getTeacherNo(),
                teacherAddDTO.getRealName(),
                teacherAddDTO.getIdCard(),
                teacherAddDTO.getPhone(),
                teacherAddDTO.getEmail(),
                teacherAddDTO.getCollegeId(),
                teacherAddDTO.getSchoolId(),
                teacherAddDTO.getTitle(),
                teacherAddDTO.getDepartment(),
                teacherAddDTO.getRoleCode(),
                teacherAddDTO.getPassword(),
                teacherAddDTO.getStatus()
        );
        return Result.success("添加成功", result);
    }
    
    @ApiOperation("更新教师信息")
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<Teacher> updateTeacher(@RequestBody TeacherUpdateDTO teacherUpdateDTO) {
        Teacher result = teacherService.updateTeacherWithUser(
                teacherUpdateDTO.getTeacherId(),
                teacherUpdateDTO.getUserId(),
                teacherUpdateDTO.getTeacherNo(),
                teacherUpdateDTO.getRealName(),
                teacherUpdateDTO.getIdCard(),
                teacherUpdateDTO.getPhone(),
                teacherUpdateDTO.getEmail(),
                teacherUpdateDTO.getCollegeId(),
                teacherUpdateDTO.getSchoolId(),
                teacherUpdateDTO.getTitle(),
                teacherUpdateDTO.getDepartment(),
                teacherUpdateDTO.getRoleCode(),
                teacherUpdateDTO.getStatus()
        );
        return Result.success("更新成功", result);
    }
    
    @ApiOperation("停用教师（软删除）")
    @DeleteMapping("/{teacherId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<?> deleteTeacher(
            @ApiParam(value = "教师ID", required = true) @PathVariable Long teacherId) {
        boolean success = teacherService.deleteTeacher(teacherId);
        return success ? Result.success("停用成功") : Result.error("停用失败");
    }
    
    @ApiOperation("根据学校ID查询教师列表（用于下拉选择）")
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<List<Teacher>> getTeacherList(
            @ApiParam(value = "学校ID（可选）") @RequestParam(required = false) Long schoolId,
            @ApiParam(value = "学院ID（可选）") @RequestParam(required = false) Long collegeId) {
        List<Teacher> list = teacherService.getTeacherListBySchool(schoolId, collegeId);
        return Result.success("查询成功", list);
    }
}

