package com.server.internshipserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.service.user.StudentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 学生管理控制器
 */
@Api(tags = "学生管理")
@RestController
@RequestMapping("/user/student")
public class StudentController {
    
    @Autowired
    private StudentService studentService;
    
    @ApiOperation("分页查询学生列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<Page<Student>> getStudentPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "学号（可选）") @RequestParam(required = false) String studentNo,
            @ApiParam(value = "班级ID（可选）") @RequestParam(required = false) Long classId,
            @ApiParam(value = "专业ID（可选）") @RequestParam(required = false) Long majorId,
            @ApiParam(value = "学院ID（可选）") @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "学校ID（可选）") @RequestParam(required = false) Long schoolId) {
        Page<Student> page = new Page<>(current, size);
        Page<Student> result = studentService.getStudentPage(page, studentNo, classId, majorId, collegeId, schoolId);
        return Result.success("查询成功", result);
    }
    
    @ApiOperation("根据ID查询学生详情")
    @GetMapping("/{studentId}")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<Student> getStudentById(
            @ApiParam(value = "学生ID", required = true) @PathVariable Long studentId) {
        Student student = studentService.getStudentById(studentId);
        return Result.success("查询成功", student);
    }
    
    @ApiOperation("根据用户ID查询学生信息")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<Student> getStudentByUserId(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        Student student = studentService.getStudentByUserId(userId);
        if (student == null) {
            return Result.error("学生信息不存在");
        }
        return Result.success("查询成功", student);
    }
    
    @ApiOperation("添加学生")
    @PostMapping
    @PreAuthorize("hasAuthority('user:add')")
    public Result<Student> addStudent(@RequestBody Student student) {
        Student result = studentService.addStudent(student);
        return Result.success("添加成功", result);
    }
    
    @ApiOperation("更新学生信息")
    @PutMapping
    @PreAuthorize("hasAuthority('user:edit')")
    public Result<Student> updateStudent(@RequestBody Student student) {
        Student result = studentService.updateStudent(student);
        return Result.success("更新成功", result);
    }
    
    @ApiOperation("停用学生（软删除）")
    @DeleteMapping("/{studentId}")
    @PreAuthorize("hasAuthority('user:delete')")
    public Result<?> deleteStudent(
            @ApiParam(value = "学生ID", required = true) @PathVariable Long studentId) {
        boolean success = studentService.deleteStudent(studentId);
        return success ? Result.success("停用成功") : Result.error("停用失败");
    }
}

