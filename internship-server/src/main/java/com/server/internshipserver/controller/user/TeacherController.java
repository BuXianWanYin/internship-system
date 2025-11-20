package com.server.internshipserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.user.Teacher;
import com.server.internshipserver.service.user.TeacherService;
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
@RequestMapping("/api/user/teacher")
public class TeacherController {
    
    @Autowired
    private TeacherService teacherService;
    
    @ApiOperation("分页查询教师列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<Page<Teacher>> getTeacherPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "工号（可选）") @RequestParam(required = false) String teacherNo,
            @ApiParam(value = "学院ID（可选）") @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "学校ID（可选）") @RequestParam(required = false) Long schoolId) {
        Page<Teacher> page = new Page<>(current, size);
        Page<Teacher> result = teacherService.getTeacherPage(page, teacherNo, collegeId, schoolId);
        return Result.success("查询成功", result);
    }
    
    @ApiOperation("根据ID查询教师详情")
    @GetMapping("/{teacherId}")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<Teacher> getTeacherById(
            @ApiParam(value = "教师ID", required = true) @PathVariable Long teacherId) {
        Teacher teacher = teacherService.getTeacherById(teacherId);
        return Result.success("查询成功", teacher);
    }
    
    @ApiOperation("根据用户ID查询教师信息")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<Teacher> getTeacherByUserId(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        Teacher teacher = teacherService.getTeacherByUserId(userId);
        if (teacher == null) {
            return Result.error("教师信息不存在");
        }
        return Result.success("查询成功", teacher);
    }
    
    @ApiOperation("添加教师")
    @PostMapping
    @PreAuthorize("hasAuthority('user:add')")
    public Result<Teacher> addTeacher(@RequestBody Teacher teacher) {
        Teacher result = teacherService.addTeacher(teacher);
        return Result.success("添加成功", result);
    }
    
    @ApiOperation("更新教师信息")
    @PutMapping
    @PreAuthorize("hasAuthority('user:edit')")
    public Result<Teacher> updateTeacher(@RequestBody Teacher teacher) {
        Teacher result = teacherService.updateTeacher(teacher);
        return Result.success("更新成功", result);
    }
    
    @ApiOperation("停用教师（软删除）")
    @DeleteMapping("/{teacherId}")
    @PreAuthorize("hasAuthority('user:delete')")
    public Result<?> deleteTeacher(
            @ApiParam(value = "教师ID", required = true) @PathVariable Long teacherId) {
        boolean success = teacherService.deleteTeacher(teacherId);
        return success ? Result.success("停用成功") : Result.error("停用失败");
    }
}

